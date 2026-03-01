package org.example.kaoyanplatform.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.kaoyanplatform.client.PythonBackendClient;
import org.example.kaoyanplatform.entity.*;
import org.example.kaoyanplatform.entity.dto.ExamStartDTO;
import org.example.kaoyanplatform.mapper.ExamSessionMapper;
import org.example.kaoyanplatform.service.*;
import org.example.kaoyanplatform.util.MathAnswerMatcher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ExamSessionServiceImpl extends ServiceImpl<ExamSessionMapper, ExamSession> implements ExamSessionService {

    @Autowired
    private QuestionPaperRelService mapPaperQuestionService;

    @Autowired
    private ExamPaperService paperService;

    @Autowired
    private QuestionService questionService;

    @Autowired
    private ExamRecordService examAnswerDetailService;

    @Autowired
    private PythonBackendClient pythonBackendClient;

    @Autowired
    private MathAnswerMatcher mathAnswerMatcher;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    @Transactional
    public ExamStartDTO startOrResumeExam(String userId, String paperId) {
        ExamPaper paper = paperService.getById(Long.parseLong(paperId));
        if (paper == null) {
            throw new RuntimeException("试卷不存在");
        }

        // 1. 先查询是否有未完成的会话（status=0）
        LambdaQueryWrapper<ExamSession> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ExamSession::getUserId, Long.parseLong(userId))
                   .eq(ExamSession::getPaperId, Long.parseLong(paperId))
                   .eq(ExamSession::getStatus, 0)  // 0-进行中
                   .orderByDesc(ExamSession::getCreateTime)
                   .last("LIMIT 1");

        System.out.println("查询未完成会话 - userId: " + userId + ", paperId: " + paperId);

        ExamSession existingSession = getOne(queryWrapper);

        System.out.println("查询结果: " + (existingSession != null ? "找到会话 " + existingSession.getId() : "未找到会话"));

        ExamSession session;
        boolean isNewSession = false;

        if (existingSession != null) {
            // 2. 如果有未完成的会话，复用该会话
            session = existingSession;
            System.out.println("复用现有会话 - startTime: " + session.getStartTime() + ", expectedEndTime: " + session.getExpectedEndTime());

            // 检查考试是否已超时
            if (session.getExpectedEndTime() != null &&
                LocalDateTime.now().isAfter(session.getExpectedEndTime())) {
                // 考试已超时，自动提交
                submitExam(session.getId().toString());
                throw new RuntimeException("考试时间已到，本次考试已自动提交");
            }
        } else {
            // 3. 如果没有未完成的会话，创建新会话
            isNewSession = true;

            Integer timeLimit = paper.getTimeLimit() != null ? paper.getTimeLimit() : 180;
            LocalDateTime startTime = LocalDateTime.now();
            LocalDateTime expectedEndTime = startTime.plusMinutes(timeLimit);

            session = new ExamSession();
            session.setUserId(Long.parseLong(userId));
            session.setPaperId(Long.parseLong(paperId));
            session.setStatus(0);
            session.setStartTime(startTime);
            session.setExpectedEndTime(expectedEndTime);
            session.setSwitchCount(0);
            session.setTotalScore(BigDecimal.ZERO);
            session.setSnapshotAnswers("{}");

            System.out.println("创建新会话 - userId: " + userId + ", startTime: " + startTime + ", expectedEndTime: " + expectedEndTime);

            save(session);
        }

        // 获取题目列表
        List<Question> questions = mapPaperQuestionService.getQuestionsWithDetails(paperId);

        ExamStartDTO dto = new ExamStartDTO();
        dto.setSession(session);
        dto.setPaper(paper);
        dto.setQuestions(questions);

        return dto;
    }

    @Override
    public boolean saveSnapshot(String sessionId, String snapshotJson) {
        ExamSession session = getById(Long.parseLong(sessionId));
        if (session == null) {
            return false;
        }
        session.setSnapshotAnswers(snapshotJson);
        return updateById(session);
    }

    @Override
    public boolean recordSwitch(String sessionId) {
        ExamSession session = getById(Long.parseLong(sessionId));
        if (session == null) {
            return false;
        }
        session.setSwitchCount(session.getSwitchCount() + 1);
        return updateById(session);
    }

    @Override
    @Transactional
    public void submitExam(String sessionId) {
        ExamSession session = getById(Long.parseLong(sessionId));
        if (session == null) {
            throw new RuntimeException("考试会话不存在");
        }

        if (session.getStatus() == 1) {
            throw new RuntimeException("考试已提交");
        }

        List<QuestionPaperRel> mappings = mapPaperQuestionService.list(
                new LambdaQueryWrapper<QuestionPaperRel>()
                        .eq(QuestionPaperRel::getPaperId, session.getPaperId().toString())
                        .orderByAsc(QuestionPaperRel::getSortOrder)
        );

        if (mappings == null || mappings.isEmpty()) {
            throw new RuntimeException("试卷题目数据异常");
        }

        Map<String, Object> snapshotAnswers;
        try {
            snapshotAnswers = objectMapper.readValue(session.getSnapshotAnswers(), new TypeReference<Map<String, Object>>() {});
        } catch (Exception e) {
            throw new RuntimeException("答题快照解析失败");
        }

        List<ExamRecord> details = new ArrayList<>();
        BigDecimal objectiveScore = BigDecimal.ZERO;
        BigDecimal subjectiveTotalScore = BigDecimal.ZERO;
        BigDecimal objectiveTotalScore = BigDecimal.ZERO; // 计算客观题总分（不保存到数据库）

        for (QuestionPaperRel mapping : mappings) {
            Question question = questionService.getById(mapping.getQuestionId());
            if (question == null) continue;

            String questionId = mapping.getQuestionId().toString();
            String userAnswer = (String) snapshotAnswers.get(questionId);

            ExamRecord detail = new ExamRecord();
            detail.setSessionId(session.getId());
            detail.setQuestionId(mapping.getQuestionId());
            detail.setUserAnswer(userAnswer != null ? userAnswer : "");

            if (isObjectiveQuestion(question.getType())) {
                // 客观题：立即批改
                objectiveTotalScore = objectiveTotalScore.add(mapping.getScoreValue()); // 计算客观题总分
                Integer result = gradeObjectiveQuestion(question, userAnswer);
                detail.setIsCorrect(result);
                detail.setScoreEarned(result == 1 ? mapping.getScoreValue() : BigDecimal.ZERO);
                objectiveScore = objectiveScore.add(detail.getScoreEarned());
            } else {
                // 主观题：不批改（isCorrect=3，分数为0）
                detail.setIsCorrect(3);
                detail.setScoreEarned(BigDecimal.ZERO);
                detail.setAiFeedback("主观题需自行批改");
                subjectiveTotalScore = subjectiveTotalScore.add(mapping.getScoreValue());
            }

            details.add(detail);
        }

        // 保存答题明细
        examAnswerDetailService.saveBatch(details);

        // 立即更新考试状态：只设置客观题分数，主观题不批改
        session.setStatus(1);
        session.setSubmitTime(LocalDateTime.now());
        session.setTotalScore(objectiveScore);
        session.setAiSummary("客观题已批改完成。\n\n客观题得分：" + objectiveScore + " 分\n客观题总分：" + objectiveTotalScore + " 分\n主观题总分：" + subjectiveTotalScore + " 分");
        updateById(session);

    }

    @Override
    @Async
    public void gradeSubjectiveQuestionsAsync(String sessionId) {
        System.out.println("开始异步批改主观题 - sessionId: " + sessionId);

        try {
            ExamSession session = getById(Long.parseLong(sessionId));
            if (session == null || session.getStatus() != 1) {
                System.out.println("会话不存在或未提交，跳过批改 - sessionId: " + sessionId);
                return;
            }

            // 查询所有待批改的主观题（isCorrect=3）
            LambdaQueryWrapper<ExamRecord> pendingWrapper = new LambdaQueryWrapper<>();
            pendingWrapper.eq(ExamRecord::getSessionId, session.getId())
                    .eq(ExamRecord::getIsCorrect, 3);

            List<ExamRecord> pendingDetails = examAnswerDetailService.list(pendingWrapper);

            if (pendingDetails.isEmpty()) {
                System.out.println("没有待批改的主观题 - sessionId: " + sessionId);
                return;
            }

            System.out.println("找到 " + pendingDetails.size() + " 道待批改的主观题");

            // 获取试卷题目映射（用于获取分值）
            List<QuestionPaperRel> mappings = mapPaperQuestionService.list(
                    new LambdaQueryWrapper<QuestionPaperRel>()
                            .eq(QuestionPaperRel::getPaperId, session.getPaperId().toString())
            );

            Map<String, QuestionPaperRel> mappingMap = mappings.stream()
                    .collect(Collectors.toMap(
                            m -> m.getQuestionId().toString(),
                            m -> m,
                            (m1, m2) -> m1
                    ));

            BigDecimal subjectiveScoreEarned = BigDecimal.ZERO;

            // 逐道批改主观题
            for (ExamRecord detail : pendingDetails) {
                try {
                    Question question = questionService.getById(detail.getQuestionId().toString());
                    if (question == null) continue;

                    QuestionPaperRel mapping = mappingMap.get(detail.getQuestionId().toString());
                    if (mapping == null) continue;

                    // 调用 Python 服务进行批改
                    Map<String, Object> gradingResult = pythonBackendClient.gradeAnswer(
                            question.getContent(),
                            detail.getUserAnswer(),
                            question.getAnswer(),
                            4  // 简答题类型
                    );

                    // 更新答题明细
                    Double earnedScore = (Double) gradingResult.getOrDefault("score", 0.0);
                    BigDecimal score = BigDecimal.valueOf(earnedScore).setScale(2, RoundingMode.HALF_UP);

                    detail.setScoreEarned(score);
                    detail.setIsCorrect(2); // 标记为已批改
                    detail.setAiFeedback((String) gradingResult.getOrDefault("feedback", "批改完成"));
                    examAnswerDetailService.updateById(detail);

                    subjectiveScoreEarned = subjectiveScoreEarned.add(score);

                    System.out.println("批改完成 - 题目ID: " + detail.getQuestionId() + ", 得分: " + score);

                    // 避免API限流，每次调用间隔1秒
                    Thread.sleep(1000);

                } catch (Exception e) {
                    System.err.println("批改题目失败 - 题目ID: " + detail.getQuestionId() + ", 错误: " + e.getMessage());
                    detail.setAiFeedback("AI批改失败：" + e.getMessage());
                    examAnswerDetailService.updateById(detail);
                }
            }

            // 重新计算总分
            LambdaQueryWrapper<ExamRecord> allWrapper = new LambdaQueryWrapper<>();
            allWrapper.eq(ExamRecord::getSessionId, session.getId());
            List<ExamRecord> allDetails = examAnswerDetailService.list(allWrapper);

            BigDecimal totalScore = allDetails.stream()
                    .map(ExamRecord::getScoreEarned)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            // 生成最终总结
            String finalSummary = generateAISummary(session, allDetails, mappings);

            // 更新会话
            session.setTotalScore(totalScore);
            session.setAiSummary(finalSummary);
            updateById(session);

            System.out.println("异步批改完成 - sessionId: " + sessionId + ", 最终总分: " + totalScore);

        } catch (Exception e) {
            System.err.println("异步批改过程出错 - sessionId: " + sessionId + ", 错误: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private boolean isObjectiveQuestion(Integer type) {
        return type != null && (type == 1 || type == 2 || type == 3);
    }

    private Integer gradeObjectiveQuestion(Question question, String userAnswer) {
        if (userAnswer == null || userAnswer.trim().isEmpty()) {
            return 0;
        }

        String standardAnswer = question.getAnswer().trim();
        String submittedAnswer = userAnswer.trim();

        if (question.getType() == 1) {
            // 单选题：完全匹配
            return standardAnswer.equalsIgnoreCase(submittedAnswer) ? 1 : 0;
        } else if (question.getType() == 2) {
            // 多选题
            String[] correctOptions = standardAnswer.split("[,，]");
            String[] userOptions = submittedAnswer.split("[,，]");

            int correctCount = 0;
            for (String opt : userOptions) {
                for (String correct : correctOptions) {
                    if (correct.trim().equalsIgnoreCase(opt.trim())) {
                        correctCount++;
                        break;
                    }
                }
            }

            if (correctCount == correctOptions.length && userOptions.length == correctOptions.length) {
                return 1;
            }
        } else if (question.getType() == 3) {
            // 填空题：使用数学答案匹配工具
            boolean isCorrect = mathAnswerMatcher.matchMathAnswer(standardAnswer, submittedAnswer);
            return isCorrect ? 1 : 0;
        }

        return 0;
    }

    private String generateAISummary(ExamSession session, List<ExamRecord> details, List<QuestionPaperRel> mappings) {
        int correctCount = 0;
        int totalQuestions = details.size();
        int objectiveCorrect = 0;
        int objectiveTotal = 0;

        for (int i = 0; i < details.size(); i++) {
            ExamRecord detail = details.get(i);
            if (detail.getIsCorrect() == 1) {
                correctCount++;
                objectiveCorrect++;
                objectiveTotal++;
            } else if (detail.getIsCorrect() == 2) {
                objectiveTotal++;
            }
        }

        double correctRate = totalQuestions > 0 ? (double) correctCount / totalQuestions * 100 : 0;
        double avgScore = totalQuestions > 0 ? session.getTotalScore().doubleValue() / totalQuestions : 0;

        StringBuilder summary = new StringBuilder();
        summary.append(String.format("本次考试共 %d 题，其中客观题 %d 题。\n", totalQuestions, objectiveTotal));
        summary.append(String.format("客观题正确数：%d/%d，正确率：%.1f%%\n", objectiveCorrect, objectiveTotal,
                objectiveTotal > 0 ? (double) objectiveCorrect / objectiveTotal * 100 : 0));
        summary.append(String.format("总得分：%.1f 分，平均每题得分：%.2f 分\n", session.getTotalScore().doubleValue(), avgScore));
        summary.append(String.format("答题时长：%d 分钟，切换题目次数：%d 次\n",
                session.getCreateTime() != null && session.getSubmitTime() != null ?
                        java.time.Duration.between(session.getCreateTime(), session.getSubmitTime()).toMinutes() : 0,
                session.getSwitchCount()));
        summary.append("\n建议：\n");

        for (int i = 0; i < details.size(); i++) {
            ExamRecord detail = details.get(i);
            if (detail.getIsCorrect() == 2 && detail.getAiFeedback() != null && !detail.getAiFeedback().isEmpty()) {
                Question q = questionService.getById(detail.getQuestionId().toString());
                if (q != null) {
                    summary.append(String.format("【第%d题】%s\n", i + 1, q.getContent()));
                    summary.append(String.format("得分：%.1f 分\n", detail.getScoreEarned().doubleValue()));
                    summary.append(String.format("反馈：%s\n\n", detail.getAiFeedback()));
                }
            }
        }

        return summary.toString();
    }
}
