package org.example.kaoyanplatform.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.example.kaoyanplatform.common.Result;
import org.example.kaoyanplatform.entity.ExamAnswerDetail;
import org.example.kaoyanplatform.entity.ExamSession;
import org.example.kaoyanplatform.entity.dto.ExamStartDTO;
import org.example.kaoyanplatform.service.ExamAnswerDetailService;
import org.example.kaoyanplatform.service.ExamSessionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "套卷刷题管理", description = "套卷刷题相关接口，包括考试会话管理、答题快照、提交批改等")
@RestController
@RequestMapping("/exam-session")
public class ExamSessionController {

    @Autowired
    private ExamSessionService examSessionService;

    @Autowired
    private ExamAnswerDetailService examAnswerDetailService;

    @PostMapping("/start")
    @Operation(summary = "开始考试", description = "初始化考试会话，返回试卷完整内容和题目列表")
    public Result<ExamStartDTO> startExam(
            @Parameter(description = "用户ID", required = true) @RequestParam String userId,
            @Parameter(description = "试卷ID", required = true) @RequestParam String paperId) {
        try {
            ExamStartDTO result = examSessionService.startExam(userId, paperId);
            return Result.success(result);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @PostMapping("/snapshot")
    @Operation(summary = "保存答题快照", description = "自动存盘用户的答题进度，JSON格式存储")
    public Result<String> saveSnapshot(
            @Parameter(description = "考试会话ID", required = true) @RequestParam String sessionId,
            @Parameter(description = "答题快照JSON", required = true) @RequestParam String snapshotJson) {
        try {
            boolean success = examSessionService.saveSnapshot(sessionId, snapshotJson);
            return success ? Result.success("快照保存成功") : Result.error("快照保存失败");
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @PostMapping("/switch")
    @Operation(summary = "记录题目切换", description = "用户切换题目时调用，记录切换次数")
    public Result<String> recordSwitch(
            @Parameter(description = "考试会话ID", required = true) @RequestParam String sessionId) {
        try {
            boolean success = examSessionService.recordSwitch(sessionId);
            return success ? Result.success("切换记录成功") : Result.error("切换记录失败");
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @PostMapping("/submit")
    @Operation(summary = "提交考试", description = "提交考试并触发AI批改流程，客观题自动判分，主观题AI批改")
    public Result<ExamSession> submitExam(
            @Parameter(description = "考试会话ID", required = true) @RequestParam String sessionId) {
        try {
            examSessionService.submitExam(sessionId);
            ExamSession session = examSessionService.getById(sessionId);
            return Result.success(session);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @GetMapping("/{sessionId}")
    @Operation(summary = "获取考试会话详情", description = "根据会话ID获取完整的考试会话信息，包括总分、AI总结等")
    public Result<ExamSession> getSession(
            @Parameter(description = "考试会话ID", required = true) @PathVariable String sessionId) {
        try {
            ExamSession session = examSessionService.getById(sessionId);
            if (session == null) {
                return Result.error("考试会话不存在");
            }
            return Result.success(session);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @GetMapping("/{sessionId}/details")
    @Operation(summary = "获取答题明细", description = "根据会话ID获取所有题目的答题详情，包括AI批改反馈")
    public Result<List<ExamAnswerDetail>> getSessionDetails(
            @Parameter(description = "考试会话ID", required = true) @PathVariable String sessionId) {
        try {
            List<ExamAnswerDetail> details = examAnswerDetailService.getDetailsBySessionId(sessionId);
            return Result.success(details);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @GetMapping("/user/{userId}")
    @Operation(summary = "获取用户考试历史", description = "根据用户ID获取所有考试会话记录")
    public Result<List<ExamSession>> getUserSessions(
            @Parameter(description = "用户ID", required = true) @PathVariable String userId) {
        try {
            com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<ExamSession> wrapper =
                    new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<>();
            wrapper.eq(ExamSession::getUserId, userId)
                   .orderByDesc(ExamSession::getCreateTime);
            List<ExamSession> sessions = examSessionService.list(wrapper);
            return Result.success(sessions);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
}
