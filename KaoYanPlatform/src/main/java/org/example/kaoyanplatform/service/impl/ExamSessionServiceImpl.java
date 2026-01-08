package org.example.kaoyanplatform.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.kaoyanplatform.entity.*;
import org.example.kaoyanplatform.entity.dto.ExamStartDTO;
import org.example.kaoyanplatform.mapper.ExamSessionMapper;
import org.example.kaoyanplatform.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    @Transactional
    public ExamStartDTO startExam(String userId, String paperId) {
        Paper paper = paperService.getById(paperId);
        if (paper == null) {
            throw new RuntimeException("试卷不存在");
        }

        ExamSession session = new ExamSession();
        session.setUserId(userId);
        session.setPaperId(paperId);
        session.setStatus(0);
        session.setStartTime(LocalDateTime.now());
        session.setSwitchCount(0);
        session.setTotalScore(BigDecimal.ZERO);
        session.setSnapshotAnswers("{}");
        
        save(session);

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
        BigDecimal totalScore = BigDecimal.ZERO;

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
                Integer result = gradeObjectiveQuestion(question, userAnswer);
                detail.setIsCorrect(result);
                detail.setScoreEarned(result == 1 ? mapping.getScoreValue() : BigDecimal.ZERO);
            } else {
                String prompt = glmService.generateGradingPrompt(
                        question.getContent(),
                        userAnswer != null ? userAnswer : "未作答",
                        question.getAnswer(),
                        mapping.getScoreValue().doubleValue()
                );

                String aiResponse = glmService.callGLMAPI(prompt);
                Map<String, Object> gradingResult = glmService.parseGradingResult(aiResponse);

                detail.setIsCorrect(2);
                detail.setScoreEarned(BigDecimal.valueOf((Double) gradingResult.get("score")));
                detail.setAiFeedback((String) gradingResult.get("feedback"));
            }

            totalScore = totalScore.add(detail.getScoreEarned());
            details.add(detail);
        }

        examAnswerDetailService.saveBatch(details);

        String aiSummary = generateAISummary(session, details, mappings);

        session.setStatus(1);
        session.setSubmitTime(LocalDateTime.now());
        session.setTotalScore(totalScore);
        session.setAiSummary(aiSummary);
        updateById(session);
    }

    private boolean isObjectiveQuestion(Integer type) {
        return type != null && (type == 1 || type == 2);
    }

    private Integer gradeObjectiveQuestion(Question question, String userAnswer) {
        if (userAnswer == null || userAnswer.trim().isEmpty()) {
            return 0;
        }

        String standardAnswer = question.getAnswer().trim();
        String submittedAnswer = userAnswer.trim();

        if (question.getType() == 1) {
            return standardAnswer.equalsIgnoreCase(submittedAnswer) ? 1 : 0;
        } else if (question.getType() == 2) {
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
                session.getStartTime() != null && session.getSubmitTime() != null ?
                        java.time.Duration.between(session.getStartTime(), session.getSubmitTime()).toMinutes() : 0,
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
