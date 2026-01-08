package org.example.kaoyanplatform.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("tb_exam_session")
public class ExamSession {
    @TableId(type = IdType.ASSIGN_UUID)
    private String id;
    private String userId;
    private String paperId;
    private Integer status;
    private LocalDateTime startTime;
    private LocalDateTime submitTime;
    private BigDecimal totalScore;
    private Integer switchCount;
    private String aiSummary;
    
    private String snapshotAnswers;
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
