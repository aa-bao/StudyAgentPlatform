package org.example.kaoyanplatform.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.example.kaoyanplatform.entity.MapPaperQuestion;
import org.example.kaoyanplatform.entity.Question;

import java.util.List;

public interface MapPaperQuestionService extends IService<MapPaperQuestion> {

    List<Question> getQuestionsWithDetails(String paperId);
}
