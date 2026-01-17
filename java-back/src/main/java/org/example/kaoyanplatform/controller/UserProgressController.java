package org.example.kaoyanplatform.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.example.kaoyanplatform.common.Result;
import org.example.kaoyanplatform.entity.UserProgress;
import org.example.kaoyanplatform.service.UserProgressService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 用户学习进度管理控制器
 * 包含用户端和管理员端接口
 */
@Tag(name = "学习进度管理", description = "用户学习进度相关接口")
@RestController
@RequestMapping("/user-progress")
public class UserProgressController {

    @Autowired
    private UserProgressService userProgressService;

    /**
     * 获取用户所有科目的学习进度
     */
    @GetMapping("/user/{userId}")
    @Operation(summary = "获取用户学习进度", description = "获取指定用户在所有科目上的学习进度")
    public Result<List<UserProgress>> getUserProgress(
            @Parameter(description = "用户ID", required = true) @PathVariable Long userId) {
        try {
            LambdaQueryWrapper<UserProgress> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(UserProgress::getUserId, userId);
            List<UserProgress> progressList = userProgressService.list(wrapper);
            return Result.success(progressList);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 获取用户指定科目的学习进度
     */
    @GetMapping("/user/{userId}/subject/{subjectId}")
    @Operation(summary = "获取用户科目进度", description = "获取指定用户在指定科目上的学习进度")
    public Result<UserProgress> getUserSubjectProgress(
            @Parameter(description = "用户ID", required = true) @PathVariable Long userId,
            @Parameter(description = "科目ID", required = true) @PathVariable Integer subjectId) {
        try {
            LambdaQueryWrapper<UserProgress> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(UserProgress::getUserId, userId)
                   .eq(UserProgress::getSubjectId, subjectId);
            UserProgress progress = userProgressService.getOne(wrapper);
            return Result.success(progress);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    // ==================== 管理员接口 ====================

    /**
     * 获取所有用户的学习进度统计（管理员）
     */
    @GetMapping("/admin/all")
    @Operation(summary = "获取所有用户学习进度（管理员）", description = "分页获取所有用户的学习进度，支持按用户ID、科目ID筛选")
    public Result<com.baomidou.mybatisplus.extension.plugins.pagination.Page<UserProgress>> getAllProgress(
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Integer pageNum,
            @Parameter(description = "每页数量") @RequestParam(defaultValue = "10") Integer pageSize,
            @Parameter(description = "用户ID") @RequestParam(required = false) Long userId,
            @Parameter(description = "科目ID") @RequestParam(required = false) Integer subjectId) {
        try {
            com.baomidou.mybatisplus.extension.plugins.pagination.Page<UserProgress> page =
                    new com.baomidou.mybatisplus.extension.plugins.pagination.Page<>(pageNum, pageSize);
            LambdaQueryWrapper<UserProgress> wrapper = new LambdaQueryWrapper<>();

            if (userId != null) {
                wrapper.eq(UserProgress::getUserId, userId);
            }
            if (subjectId != null) {
                wrapper.eq(UserProgress::getSubjectId, subjectId);
            }

            wrapper.orderByDesc(UserProgress::getUpdateTime);
            userProgressService.page(page, wrapper);
            return Result.success(page);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 获取学习进度统计数据（管理员）
     */
    @GetMapping("/admin/stats")
    @Operation(summary = "获取学习进度统计（管理员）", description = "获取总体学习统计：总学习人数、平均完成数等")
    public Result<Map<String, Object>> getProgressStats() {
        try {
            Map<String, Object> stats = new HashMap<>();

            // 统计有学习记录的用户数
            long totalUsers = userProgressService.count(
                    new LambdaQueryWrapper<UserProgress>()
                            .select(UserProgress::getUserId)
                            .groupBy(UserProgress::getUserId)
            );

            // 统计总学习记录数
            long totalRecords = userProgressService.count();

            // 统计总完成题目数
            com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<UserProgress> wrapper =
                    new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<>();
            wrapper.select("IFNULL(sum(finished_count), 0) as totalFinished");
            Map<String, Object> sumMap = userProgressService.getMap(wrapper);
            long totalFinished = sumMap != null && sumMap.get("totalFinished") != null
                    ? Long.parseLong(sumMap.get("totalFinished").toString())
                    : 0;

            // 统计总正确题目数
            com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<UserProgress> correctWrapper =
                    new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<>();
            correctWrapper.select("IFNULL(sum(correct_count), 0) as totalCorrect");
            Map<String, Object> correctMap = userProgressService.getMap(correctWrapper);
            long totalCorrect = correctMap != null && correctMap.get("totalCorrect") != null
                    ? Long.parseLong(correctMap.get("totalCorrect").toString())
                    : 0;

            stats.put("totalUsers", totalUsers);
            stats.put("totalRecords", totalRecords);
            stats.put("totalFinished", totalFinished);
            stats.put("totalCorrect", totalCorrect);

            // 计算平均正确率
            double avgAccuracy = totalFinished > 0
                    ? Math.round((double) totalCorrect / totalFinished * 10000) / 100.0
                    : 0;
            stats.put("avgAccuracy", avgAccuracy);

            return Result.success(stats);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 获取学习排行榜（管理员）
     */
    @GetMapping("/admin/ranking")
    @Operation(summary = "获取学习排行榜（管理员）", description = "获取学习完成数排行榜，前N名")
    public Result<List<Map<String, Object>>> getLearningRanking(
            @Parameter(description = "排行榜前N名") @RequestParam(defaultValue = "10") Integer limit) {
        try {
            // 按用户分组，统计每个用户总完成数和正确数
            com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<UserProgress> wrapper =
                    new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<>();
            wrapper.select("user_id", "sum(finished_count) as total_finished", "sum(correct_count) as total_correct")
                   .groupBy("user_id")
                   .orderByDesc("total_finished")
                   .last("limit " + limit);

            List<Map<String, Object>> ranking = userProgressService.listMaps(wrapper);
            return Result.success(ranking);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
}
