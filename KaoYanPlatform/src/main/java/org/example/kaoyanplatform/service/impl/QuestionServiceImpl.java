package org.example.kaoyanplatform.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.example.kaoyanplatform.entity.Question;
import org.example.kaoyanplatform.entity.QuestionSubject;
import org.example.kaoyanplatform.mapper.QuestionMapper;
import org.example.kaoyanplatform.mapper.QuestionSubjectMapper;
import org.example.kaoyanplatform.service.QuestionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class QuestionServiceImpl extends ServiceImpl<QuestionMapper, Question> implements QuestionService {

    @Autowired
    private QuestionSubjectMapper questionSubjectMapper;

    @Override
    public List<Question> getQuestionsBySubjectIds(List<Integer> subjectIds) {
        if (subjectIds == null || subjectIds.isEmpty()) {
            return Collections.emptyList();
        }

        QueryWrapper<QuestionSubject> qsWrapper = new QueryWrapper<>();
        qsWrapper.in("subject_id", subjectIds);
        qsWrapper.select("question_id");
        List<Object> qIds = questionSubjectMapper.selectObjs(qsWrapper);

        if (qIds == null || qIds.isEmpty()) {
            return Collections.emptyList();
        }

        List<Long> questionIds = qIds.stream().map(o -> Long.valueOf(o.toString())).collect(Collectors.toList());
        return listByIds(questionIds);
    }
}