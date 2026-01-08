package org.example.kaoyanplatform.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;

@Data
@TableName("map_paper_question")
public class MapPaperQuestion {
    @TableId(type = IdType.ASSIGN_UUID)
    private String id;
    private String paperId;
    private String questionId;
    private Integer sortOrder;
    private BigDecimal scoreValue;
    private String parentSectionName;
}
