package org.example.kaoyanplatform.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("tb_exam_record")
public class ExamRecord {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private Long questionId;
    private String userAnswer;
    private Integer isCorrect;
    private Integer score;
    private Integer duration;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}