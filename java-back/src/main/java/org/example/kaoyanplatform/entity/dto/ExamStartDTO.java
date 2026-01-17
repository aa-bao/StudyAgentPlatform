package org.example.kaoyanplatform.entity.dto;

import lombok.Data;
import org.example.kaoyanplatform.entity.ExamSession;
import org.example.kaoyanplatform.entity.Paper;
import org.example.kaoyanplatform.entity.Question;

import java.util.List;

@Data
public class ExamStartDTO {
    private ExamSession session;
    private Paper paper;
    private List<Question> questions;
}
