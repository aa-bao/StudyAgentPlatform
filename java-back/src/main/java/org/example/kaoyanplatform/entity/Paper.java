package org.example.kaoyanplatform.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
@TableName(value = "tb_paper", autoResultMap = true)
public class Paper {
    @TableId(type = IdType.ASSIGN_UUID)
    private String id;
    private String title;
    private String examSpecId;
    private Integer totalScore;
    private Integer timeLimit;
    private Integer paperType;
    private Integer year;

    @TableField(typeHandler = JacksonTypeHandler.class)
    private List<PaperSection> structureJson;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    
    @Data
    public static class PaperSection {
        private String name;
        private Integer start;
        private Integer end;
    }
}
