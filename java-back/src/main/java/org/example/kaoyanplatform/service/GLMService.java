package org.example.kaoyanplatform.service;

import org.example.kaoyanplatform.entity.dto.QuestionDTO;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

public interface GLMService {

    String callGLMAPI(String prompt);

    String generateGradingPrompt(String question, String userAnswer, String standardAnswer, Double scoreValue);

    Map<String, Object> parseGradingResult(String response);

    QuestionDTO recognizeQuestionFromImage(MultipartFile file);
}
