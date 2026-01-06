package org.example.kaoyanplatform.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.example.kaoyanplatform.entity.MapQuestionSubject;
import org.example.kaoyanplatform.entity.MistakeRecord;
import org.example.kaoyanplatform.entity.Question;
import org.example.kaoyanplatform.entity.Subject;
import org.example.kaoyanplatform.entity.dto.MistakeHeatmapDTO;
import org.example.kaoyanplatform.mapper.MistakeRecordMapper;
import org.example.kaoyanplatform.mapper.MapQuestionSubjectMapper;
import org.example.kaoyanplatform.mapper.QuestionMapper;
import org.example.kaoyanplatform.mapper.SubjectMapper;
import org.example.kaoyanplatform.service.MistakeRecordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 错题记录服务实现类
 */
@Service
public class MistakeRecordServiceImpl extends ServiceImpl<MistakeRecordMapper, MistakeRecord> implements MistakeRecordService {

    @Autowired
    private MapQuestionSubjectMapper mapQuestionSubjectMapper;

    @Autowired
    private SubjectMapper subjectMapper;

    @Autowired
    private QuestionMapper questionMapper;

    @Override
    public List<MistakeHeatmapDTO> getMistakeHeatmap() {
        // 1. 获取所有错题记录
        List<MistakeRecord> allMistakes = list();

        // 2. 先过滤掉没有科目关联的错题记录，再按科目ID分组统计
        Map<Integer, List<MistakeRecord>> subjectMistakes = allMistakes.stream()
                .filter(mistake -> {
                    // 过滤掉科目ID为null的记录
                    Integer subjectId = getSubjectIdByQuestionId(mistake.getQuestionId());
                    return subjectId != null;
                })
                .collect(Collectors.groupingBy(mistake -> {
                    // 通过题目ID找到科目ID
                    return getSubjectIdByQuestionId(mistake.getQuestionId());
                }));

        // 3. 构建统计结果
        List<MistakeHeatmapDTO> result = new ArrayList<>();

        for (Map.Entry<Integer, List<MistakeRecord>> entry : subjectMistakes.entrySet()) {
            Integer subjectId = entry.getKey();
            List<MistakeRecord> mistakes = entry.getValue();

            MistakeHeatmapDTO dto = new MistakeHeatmapDTO();
            dto.setSubjectId(subjectId);

            // 获取科目名称
            Subject subject = subjectMapper.selectById(subjectId);
            dto.setSubjectName(subject != null ? subject.getName() : "未知科目");

            // 计算总错误次数
            int totalErrorCount = mistakes.stream()
                    .mapToInt(MistakeRecord::getErrorCount)
                    .sum();
            dto.setTotalErrorCount(totalErrorCount);

            // 计算错题题目数
            Set<Integer> questionIds = mistakes.stream()
                    .map(MistakeRecord::getQuestionId)
                    .collect(Collectors.toSet());
            dto.setMistakeQuestionCount(questionIds.size());

            // 计算涉及用户数
            Set<Integer> userIds = mistakes.stream()
                    .map(MistakeRecord::getUserId)
                    .collect(Collectors.toSet());
            dto.setAffectedUserCount(userIds.size());

            // 获取该科目的高频错题 TOP 10
            List<MistakeHeatmapDTO.HotMistakeQuestion> hotQuestions = getHotQuestionsBySubject(subjectId, 10);
            dto.setHotQuestions(hotQuestions);

            result.add(dto);
        }

        // 按总错误次数降序排序
        result.sort((a, b) -> b.getTotalErrorCount().compareTo(a.getTotalErrorCount()));

        return result;
    }

    @Override
    public List<MistakeHeatmapDTO.HotMistakeQuestion> getHotMistakeQuestions(Integer limit) {
        // 获取所有错题记录
        List<MistakeRecord> allMistakes = list();

        // 先过滤掉没有科目关联的错题记录，再按题目ID分组统计
        Map<Integer, List<MistakeRecord>> questionMistakes = allMistakes.stream()
                .filter(mistake -> {
                    // 过滤掉科目ID为null的记录
                    Integer subjectId = getSubjectIdByQuestionId(mistake.getQuestionId());
                    return subjectId != null;
                })
                .collect(Collectors.groupingBy(MistakeRecord::getQuestionId));

        // 构建高频错题列表
        List<MistakeHeatmapDTO.HotMistakeQuestion> hotQuestions = new ArrayList<>();

        for (Map.Entry<Integer, List<MistakeRecord>> entry : questionMistakes.entrySet()) {
            Integer questionId = entry.getKey();
            List<MistakeRecord> mistakes = entry.getValue();

            MistakeHeatmapDTO.HotMistakeQuestion hotQuestion = new MistakeHeatmapDTO.HotMistakeQuestion();
            hotQuestion.setQuestionId(questionId);

            // 获取题目内容
            Question question = questionMapper.selectById(questionId);
            if (question != null) {
                String content = question.getContent();
                if (content != null && content.length() > 100) {
                    content = content.substring(0, 100) + "...";
                }
                hotQuestion.setQuestionContent(content);
            }

            // 计算总错误次数
            int totalErrorCount = mistakes.stream()
                    .mapToInt(MistakeRecord::getErrorCount)
                    .sum();
            hotQuestion.setTotalErrorCount(totalErrorCount);

            // 计算错误用户数
            Set<Integer> errorUserIds = mistakes.stream()
                    .map(MistakeRecord::getUserId)
                    .collect(Collectors.toSet());
            hotQuestion.setErrorUserCount(errorUserIds.size());

            // 获取科目ID
            hotQuestion.setSubjectId(getSubjectIdByQuestionId(questionId));

            hotQuestions.add(hotQuestion);
        }

        // 按总错误次数降序排序，取前N个
        hotQuestions.sort((a, b) -> b.getTotalErrorCount().compareTo(a.getTotalErrorCount()));
        return hotQuestions.stream()
                .limit(limit)
                .collect(Collectors.toList());
    }

    /**
     * 通过题目ID获取科目ID
     */
    private Integer getSubjectIdByQuestionId(Integer questionId) {
        QueryWrapper<MapQuestionSubject> wrapper = new QueryWrapper<>();
        wrapper.eq("question_id", questionId);
        wrapper.last("LIMIT 1");
        MapQuestionSubject relation = mapQuestionSubjectMapper.selectOne(wrapper);
        return relation != null ? relation.getSubjectId() : null;
    }

    /**
     * 获取指定科目的高频错题
     */
    private List<MistakeHeatmapDTO.HotMistakeQuestion> getHotQuestionsBySubject(Integer subjectId, Integer limit) {
        // 1. 获取该科目下的所有题目ID
        QueryWrapper<MapQuestionSubject> wrapper = new QueryWrapper<>();
        wrapper.eq("subject_id", subjectId);
        List<MapQuestionSubject> relations = mapQuestionSubjectMapper.selectList(wrapper);

        // 【修改处】显式转换为 Integer
        List<Integer> questionIds = relations.stream()
                .map(item -> {
                    Object qid = item.getQuestionId();
                    if (qid instanceof Long) {
                        return ((Long) qid).intValue();
                    }
                    return (Integer) qid;
                })
                .collect(Collectors.toList());

        if (questionIds.isEmpty()) {
            return new ArrayList<>();
        }

        // 获取这些题目的错题记录
        QueryWrapper<MistakeRecord> mistakeWrapper = new QueryWrapper<>();
        mistakeWrapper.in("question_id", questionIds);
        List<MistakeRecord> mistakes = list(mistakeWrapper);

        // 按题目ID分组统计
        Map<Integer, List<MistakeRecord>> questionMistakes = mistakes.stream()
                .collect(Collectors.groupingBy(MistakeRecord::getQuestionId));

        List<MistakeHeatmapDTO.HotMistakeQuestion> hotQuestions = new ArrayList<>();

        for (Map.Entry<Integer, List<MistakeRecord>> entry : questionMistakes.entrySet()) {
            Integer questionId = entry.getKey();
            List<MistakeRecord> questionMistakeList = entry.getValue();

            MistakeHeatmapDTO.HotMistakeQuestion hotQuestion = new MistakeHeatmapDTO.HotMistakeQuestion();
            hotQuestion.setQuestionId(questionId);

            Question question = questionMapper.selectById(questionId);
            if (question != null) {
                String content = question.getContent();
                if (content != null && content.length() > 100) {
                    content = content.substring(0, 100) + "...";
                }
                hotQuestion.setQuestionContent(content);
            }

            int totalErrorCount = questionMistakeList.stream()
                    .mapToInt(MistakeRecord::getErrorCount)
                    .sum();
            hotQuestion.setTotalErrorCount(totalErrorCount);

            Set<Integer> errorUserIds = questionMistakeList.stream()
                    .map(MistakeRecord::getUserId)
                    .collect(Collectors.toSet());
            hotQuestion.setErrorUserCount(errorUserIds.size());

            hotQuestion.setSubjectId(subjectId);

            hotQuestions.add(hotQuestion);
        }

        hotQuestions.sort((a, b) -> b.getTotalErrorCount().compareTo(a.getTotalErrorCount()));
        return hotQuestions.stream()
                .limit(limit)
                .collect(Collectors.toList());
    }
}
