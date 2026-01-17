package org.example.kaoyanplatform.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
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
    private MapPaperQuestionService mapPaperQuestionService;

    @Autowired
    private PaperService paperService;

    @Autowired
    private QuestionService questionService;

    @Autowired
    private ExamAnswerDetailService examAnswerDetailService;

    @Autowired
    private GLMService glmService;

    @Autowired
    private MathAnswerMatcher mathAnswerMatcher;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    @Transactional
    public ExamStartDTO startOrResumeExam(String userId, String paperId) {
        Paper paper = paperService.getById(paperId);
        if (paper == null) {
            throw new RuntimeException("试卷不存在");
        }

        // 1. 先查询是否有未完成的会话（status=0）
        LambdaQueryWrapper<ExamSession> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ExamSession::getUserId, userId)
                   .eq(ExamSession::getPaperId, paperId)
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
                submitExam(session.getId());
                throw new RuntimeException("考试时间已到，本次考试已自动提交");
            }
        } else {
            // 3. 如果没有未完成的会话，创建新会话
            isNewSession = true;

            Integer timeLimit = paper.getTimeLimit() != null ? paper.getTimeLimit() : 180;
            LocalDateTime startTime = LocalDateTime.now();
            LocalDateTime expectedEndTime = startTime.plusMinutes(timeLimit);

            session = new ExamSession();
            session.setUserId(userId);
            session.setPaperId(paperId);
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
        ExamSession session = getById(sessionId);
        if (session == null) {
            return false;
        }
        session.setSnapshotAnswers(snapshotJson);
        return updateById(session);
    }

    @Override
    public boolean recordSwitch(String sessionId) {
        ExamSession session = getById(sessionId);
        if (session == null) {
            return false;
        }
        session.setSwitchCount(session.getSwitchCount() + 1);
        return updateById(session);
    }

    @Override
    @Transactional
    public void submitExam(String sessionId) {
        ExamSession session = getById(sessionId);
        if (session == null) {
            throw new RuntimeException("考试会话不存在");
        }

        if (session.getStatus() == 1) {
            throw new RuntimeException("考试已提交");
        }

        List<MapPaperQuestion> mappings = mapPaperQuestionService.list(
                new LambdaQueryWrapper<MapPaperQuestion>()
                        .eq(MapPaperQuestion::getPaperId, session.getPaperId())
                        .orderByAsc(MapPaperQuestion::getSortOrder)
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

        List<ExamAnswerDetail> details = new ArrayList<>();
        BigDecimal objectiveScore = BigDecimal.ZERO;
        BigDecimal subjectiveTotalScore = BigDecimal.ZERO;

        for (MapPaperQuestion mapping : mappings) {
            Question question = questionService.getById(Long.parseLong(mapping.getQuestionId()));
            if (question == null) continue;

            String questionId = mapping.getQuestionId();
            String userAnswer = (String) snapshotAnswers.get(questionId);

            ExamAnswerDetail detail = new ExamAnswerDetail();
            detail.setSessionId(sessionId);
            detail.setQuestionId(questionId);
            detail.setUserAnswer(userAnswer != null ? userAnswer : "");

            if (isObjectiveQuestion(question.getType())) {
                // 客观题：立即批改
                Integer result = gradeObjectiveQuestion(question, userAnswer);
                detail.setIsCorrect(result);
                detail.setScoreEarned(result == 1 ? mapping.getScoreValue() : BigDecimal.ZERO);
                objectiveScore = objectiveScore.add(detail.getScoreEarned());
            } else {
                // 主观题：标记为待批改（isCorrect=3，初始分数为0）
                detail.setIsCorrect(3);
                detail.setScoreEarned(BigDecimal.ZERO);
                detail.setAiFeedback("AI正在批改中，请稍候...");
                subjectiveTotalScore = subjectiveTotalScore.add(mapping.getScoreValue());
            }

            details.add(detail);
        }

        // 保存答题明细
        examAnswerDetailService.saveBatch(details);

        // 立即更新考试状态：设置客观题分数，主观题分数为0（待批改）
        session.setStatus(1);
        session.setSubmitTime(LocalDateTime.now());
        session.setTotalScore(objectiveScore);
        session.setAiSummary("客观题已批改完成，主观题正在AI批改中，请稍后刷新查看完整成绩。\n\n客观题得分：" + objectiveScore + " 分\n主观题总分：" + subjectiveTotalScore + " 分（批改中）");
        updateById(session);

        // 触发异步批改主观题
        gradeSubjectiveQuestionsAsync(sessionId);
    }

    @Override
    @Async
    public void gradeSubjectiveQuestionsAsync(String sessionId) {
        System.out.println("开始异步批改主观题 - sessionId: " + sessionId);

        try {
            ExamSession session = getById(sessionId);
            if (session == null || session.getStatus() != 1) {
                System.out.println("会话不存在或未提交，跳过批改 - sessionId: " + sessionId);
                return;
            }

            // 查询所有待批改的主观题（isCorrect=3）
            LambdaQueryWrapper<ExamAnswerDetail> pendingWrapper = new LambdaQueryWrapper<>();
            pendingWrapper.eq(ExamAnswerDetail::getSessionId, sessionId)
                    .eq(ExamAnswerDetail::getIsCorrect, 3);

            List<ExamAnswerDetail> pendingDetails = examAnswerDetailService.list(pendingWrapper);

            if (pendingDetails.isEmpty()) {
                System.out.println("没有待批改的主观题 - sessionId: " + sessionId);
                return;
            }

            System.out.println("找到 " + pendingDetails.size() + " 道待批改的主观题");

            // 获取试卷题目映射（用于获取分值）
            List<MapPaperQuestion> mappings = mapPaperQuestionService.list(
                    new LambdaQueryWrapper<MapPaperQuestion>()
                            .eq(MapPaperQuestion::getPaperId, session.getPaperId())
            );

            Map<String, MapPaperQuestion> mappingMap = mappings.stream()
                    .collect(Collectors.toMap(
                            MapPaperQuestion::getQuestionId,
                            m -> m,
                            (m1, m2) -> m1
                    ));

            BigDecimal subjectiveScoreEarned = BigDecimal.ZERO;

            // 逐道批改主观题
            for (ExamAnswerDetail detail : pendingDetails) {
                try {
                    Question question = questionService.getById(Long.parseLong(detail.getQuestionId()));
                    if (question == null) continue;

                    MapPaperQuestion mapping = mappingMap.get(detail.getQuestionId());
                    if (mapping == null) continue;

                    // 调用GLM进行批改
                    String prompt = glmService.generateGradingPrompt(
                            question.getContent(),
                            detail.getUserAnswer(),
                            question.getAnswer(),
                            mapping.getScoreValue().doubleValue()
                    );

                    String glmResponse = glmService.callGLMAPI(prompt);
                    Map<String, Object> gradingResult = glmService.parseGradingResult(glmResponse);

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
            LambdaQueryWrapper<ExamAnswerDetail> allWrapper = new LambdaQueryWrapper<>();
            allWrapper.eq(ExamAnswerDetail::getSessionId, sessionId);
            List<ExamAnswerDetail> allDetails = examAnswerDetailService.list(allWrapper);

            BigDecimal totalScore = allDetails.stream()
                    .map(ExamAnswerDetail::getScoreEarned)
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

    private String generateAISummary(ExamSession session, List<ExamAnswerDetail> details, List<MapPaperQuestion> mappings) {
        int correctCount = 0;
        int totalQuestions = details.size();
        int objectiveCorrect = 0;
        int objectiveTotal = 0;

        for (int i = 0; i < details.size(); i++) {
            ExamAnswerDetail detail = details.get(i);
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
        summary.append("\nAI 批改反馈：\n");

        for (int i = 0; i < details.size(); i++) {
            ExamAnswerDetail detail = details.get(i);
            if (detail.getIsCorrect() == 2 && detail.getAiFeedback() != null && !detail.getAiFeedback().isEmpty()) {
                Question q = questionService.getById(Long.parseLong(detail.getQuestionId()));
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
