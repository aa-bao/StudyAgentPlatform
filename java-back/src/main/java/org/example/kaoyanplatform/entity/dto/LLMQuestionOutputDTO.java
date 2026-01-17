package org.example.kaoyanplatform.entity.dto;

import lombok.Data;
import java.util.List;

@Data
public class LLMQuestionOutputDTO {
    private String content;
    private List<String> options;
    private String answer;
    private String analysis;
}