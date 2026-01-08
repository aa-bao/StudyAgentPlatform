package org.example.kaoyanplatform.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("tb_exam_answer_detail")
public class ExamAnswerDetail {
    @TableId(type = IdType.ASSIGN_UUID)
    private String id;
    private String sessionId;
    private String questionId;
    private String userAnswer;
    private Integer isCorrect;
    private BigDecimal scoreEarned;
    private Integer durationSeconds;
    private String aiFeedback;
    private String knowledgePointId;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
