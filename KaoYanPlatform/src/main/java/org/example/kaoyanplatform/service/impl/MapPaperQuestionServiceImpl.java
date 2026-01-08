package org.example.kaoyanplatform.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.example.kaoyanplatform.entity.MapPaperQuestion;
import org.example.kaoyanplatform.entity.Question;
import org.example.kaoyanplatform.mapper.MapPaperQuestionMapper;
import org.example.kaoyanplatform.service.MapPaperQuestionService;
import org.example.kaoyanplatform.service.QuestionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class MapPaperQuestionServiceImpl extends ServiceImpl<MapPaperQuestionMapper, MapPaperQuestion> implements MapPaperQuestionService {

    @Autowired
    private QuestionService questionService;

    @Override
    public List<Question> getQuestionsWithDetails(String paperId) {
        LambdaQueryWrapper<MapPaperQuestion> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(MapPaperQuestion::getPaperId, paperId)
               .orderByAsc(MapPaperQuestion::getSortOrder);
        
        List<MapPaperQuestion> mappings = list(wrapper);
        
        if (mappings == null || mappings.isEmpty()) {
            return Collections.emptyList();
        }
        
        List<String> questionIds = mappings.stream()
                .map(MapPaperQuestion::getQuestionId)
                .collect(Collectors.toList());
        
        LambdaQueryWrapper<Question> questionWrapper = new LambdaQueryWrapper<>();
        questionWrapper.in(Question::getId, questionIds);
        
        List<Question> questions = questionService.list(questionWrapper);
        
        return questions;
    }
}
