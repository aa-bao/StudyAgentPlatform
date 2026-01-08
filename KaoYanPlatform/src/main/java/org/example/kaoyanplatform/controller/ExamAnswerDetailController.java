package org.example.kaoyanplatform.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.example.kaoyanplatform.common.Result;
import org.example.kaoyanplatform.entity.ExamAnswerDetail;
import org.example.kaoyanplatform.service.ExamAnswerDetailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "答题明细管理", description = "答题明细查询和管理接口")
@RestController
@RequestMapping("/exam-answer-detail")
public class ExamAnswerDetailController {

    @Autowired
    private ExamAnswerDetailService examAnswerDetailService;

    @GetMapping("/session/{sessionId}")
    @Operation(summary = "根据会话ID查询答题明细", description = "获取指定考试会话的所有答题明细，包括AI批改结果")
    public Result<List<ExamAnswerDetail>> getDetailsBySessionId(
            @Parameter(description = "考试会话ID", required = true) @PathVariable String sessionId) {
        try {
            List<ExamAnswerDetail> details = examAnswerDetailService.getDetailsBySessionId(sessionId);
            return Result.success(details);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @GetMapping("/question/{questionId}")
    @Operation(summary = "根据题目ID查询答题记录", description = "查询指定题目在所有考试中的答题情况")
    public Result<List<ExamAnswerDetail>> getDetailsByQuestionId(
            @Parameter(description = "题目ID", required = true) @PathVariable String questionId) {
        try {
            LambdaQueryWrapper<ExamAnswerDetail> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(ExamAnswerDetail::getQuestionId, questionId)
                   .orderByDesc(ExamAnswerDetail::getCreateTime);
            List<ExamAnswerDetail> details = examAnswerDetailService.list(wrapper);
            return Result.success(details);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @GetMapping("/{id}")
    @Operation(summary = "获取答题明细详情", description = "根据答题明细ID获取详细信息")
    public Result<ExamAnswerDetail> getDetail(
            @Parameter(description = "答题明细ID", required = true) @PathVariable String id) {
        try {
            ExamAnswerDetail detail = examAnswerDetailService.getById(id);
            if (detail == null) {
                return Result.error("答题明细不存在");
            }
            return Result.success(detail);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @GetMapping("/session/{sessionId}/correct-rate")
    @Operation(summary = "获取考试正确率统计", description = "统计指定考试会话的客观题和主观题得分情况")
    public Result<Object> getCorrectRate(
            @Parameter(description = "考试会话ID", required = true) @PathVariable String sessionId) {
        try {
            List<ExamAnswerDetail> details = examAnswerDetailService.getDetailsBySessionId(sessionId);
            
            int total = details.size();
            int correct = 0;
            int subjective = 0;
            int objective = 0;
            double totalScore = 0;
            double maxScore = 0;

            for (ExamAnswerDetail detail : details) {
                totalScore += detail.getScoreEarned().doubleValue();
                if (detail.getIsCorrect() == 1) {
                    correct++;
                    objective++;
                } else if (detail.getIsCorrect() == 2) {
                    subjective++;
                }
            }

            java.util.Map<String, Object> stats = new java.util.HashMap<>();
            stats.put("totalQuestions", total);
            stats.put("objectiveCorrect", correct);
            stats.put("objectiveTotal", objective);
            stats.put("subjectiveCount", subjective);
            stats.put("objectiveRate", objective > 0 ? (double) correct / objective * 100 : 0);
            stats.put("totalScore", totalScore);

            return Result.success(stats);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
}
