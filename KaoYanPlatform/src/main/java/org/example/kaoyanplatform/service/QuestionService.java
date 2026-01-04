package org.example.kaoyanplatform.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.example.kaoyanplatform.entity.Question;

import java.util.List;

public interface QuestionService extends IService<Question> {
    List<Question> getQuestionsBySubjectIds(List<Integer> subjectIds);
}