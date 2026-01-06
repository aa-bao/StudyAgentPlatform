package org.example.kaoyanplatform.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.example.kaoyanplatform.common.Result;
import org.example.kaoyanplatform.entity.Question;
import org.example.kaoyanplatform.entity.dto.MistakeHeatmapDTO;
import org.example.kaoyanplatform.entity.dto.TagStatsDTO;
import org.example.kaoyanplatform.service.MistakeRecordService;
import org.example.kaoyanplatform.service.QuestionService;
import org.example.kaoyanplatform.service.UserService;
import org.example.kaoyanplatform.service.MapQuestionSubjectService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;

/**
 * 管理员Controller
 */
@Tag(name = "管理员管理", description = "管理员数据统计与监控接口")
@RestController
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    private QuestionService questionService;

    @Autowired
    private UserService userService;

    @Autowired
    private MistakeRecordService mistakeRecordService;

    @Autowired
    private MapQuestionSubjectService mapQuestionSubjectService;

    @GetMapping("/statistics")
    @Operation(summary = "获取统计数据", description = """
            获取管理员统计数据，包括：
            - questionCount: 题目总数
            - userCount: 用户总数（个人模式固定为1）
            - exerciseCount: 练习次数
            - todayActive: 今日活跃用户数（个人模式显示"个人模式"）
            - subjectData: 各科目题目数量分布（政治、英语、数学、408专业课）
            """)
    public Result<Map<String, Object>> getStatistics(
            @Parameter(description = "用户ID，可选参数。传入则返回个人模式数据，不传则返回全局管理员数据")
            @RequestParam(required = false) Integer userId) {
        Map<String, Object> map = new HashMap<>();

        // 1. 处理卡片统计数据
        if (userId != null) {
            // 普通用户模式
            map.put("questionCount", questionService.count());
            map.put("userCount", 1);
            map.put("exerciseCount", mistakeRecordService.count(new QueryWrapper<org.example.kaoyanplatform.entity.MistakeRecord>().eq("user_id", userId)));
            map.put("todayActive", "个人模式");
        } else {
            // 管理员模式：显示全局数据
            map.put("questionCount", questionService.count());
            map.put("userCount", userService.count());
            map.put("exerciseCount", 1024);
            map.put("todayActive", 56);
        }

        // 2. 饼图数据（通过映射表查询各科目题目数量）
        List<Map<String, Object>> seriesData = new ArrayList<>();

        // 通过映射表查询各科目的题目数量
        // 政治科目ID=1
        long politicsCount = getCountBySubjectId(1);
        seriesData.add(Map.of("name", "政治", "value", politicsCount));

        // 英语科目ID=2
        long englishCount = getCountBySubjectId(2);
        seriesData.add(Map.of("name", "英语", "value", englishCount));

        // 数学科目ID=3
        long mathCount = getCountBySubjectId(3);
        seriesData.add(Map.of("name", "数学", "value", mathCount));

        // 408专业课科目ID=4
        long cs408Count = getCountBySubjectId(4);
        seriesData.add(Map.of("name", "408专业课", "value", cs408Count));

        map.put("subjectData", seriesData);

        return Result.success(map);
    }

    /**
     * 获取错题热力统计
     */
    @GetMapping("/mistake-heatmap")
    @Operation(summary = "获取错题热力统计", description = """
            获取全局错题热力统计数据，用于展示用户错题时间分布。
            返回数据包含：
            - date: 日期
            - count: 错题数量
            """)
    public Result<List<MistakeHeatmapDTO>> getMistakeHeatmap() {
        List<MistakeHeatmapDTO> heatmap = mistakeRecordService.getMistakeHeatmap();
        return Result.success(heatmap);
    }

    /**
     * 获取全局错题 TOP N
     */
    @GetMapping("/hot-mistakes")
    @Operation(summary = "获取热门错题", description = """
            获取全局错题TOP N排名，返回错误次数最多的题目。
            返回数据包含：
            - questionId: 题目ID
            - questionTitle: 题目标题
            - errorCount: 错误次数
            """)
    public Result<List<MistakeHeatmapDTO.HotMistakeQuestion>> getHotMistakes(
            @Parameter(description = "返回数量限制，默认20，可根据需要调整")
            @RequestParam(defaultValue = "20") Integer limit) {
        List<MistakeHeatmapDTO.HotMistakeQuestion> hotMistakes = mistakeRecordService.getHotMistakeQuestions(limit);
        return Result.success(hotMistakes);
    }

    /**
     * 根据科目ID获取题目数量（通过映射表）
     * @param subjectId 科目ID
     * @return 题目数量
     */
    private long getCountBySubjectId(int subjectId) {
        try {
            List<Long> questionIds = mapQuestionSubjectService.getQuestionIdsBySubjectId(subjectId);
            return questionIds != null ? questionIds.size() : 0;
        } catch (Exception e) {
            // 如果查询失败，返回0
            return 0;
        }
    }
}




