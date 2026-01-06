package org.example.kaoyanplatform.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
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
    public Result<Map<String, Object>> getStatistics(@RequestParam(required = false) Integer userId) {
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
    public Result<List<MistakeHeatmapDTO>> getMistakeHeatmap() {
        List<MistakeHeatmapDTO> heatmap = mistakeRecordService.getMistakeHeatmap();
        return Result.success(heatmap);
    }

    /**
     * 获取全局错题 TOP N
     */
    @GetMapping("/hot-mistakes")
    public Result<List<MistakeHeatmapDTO.HotMistakeQuestion>> getHotMistakes(
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




