package org.example.kaoyanplatform.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import lombok.Data;

import java.util.List;

@Data
@TableName(value = "tb_question", autoResultMap = true)
public class Question {
    @TableId(type = IdType.AUTO)
    private Long id;

    // 现在通过映射表管理：map_question_subject, map_question_book
    private Integer type; // 1-单选, 2-多选
    private String content;

    // 指定使用 JacksonTypeHandler 来处理 JSON 字段
    @TableField(typeHandler = JacksonTypeHandler.class)
    private List<String> options;
    private String answer;
    private String analysis;
    private Integer difficulty;

    // 指定使用 JacksonTypeHandler 来处理 JSON 字段 (存储标签数组)
    @TableField(typeHandler = JacksonTypeHandler.class)
    private List<String> tags;
    private String source; // 题目来源
    private java.time.LocalDateTime createTime; // 创建时间

    // 非数据库字段：用于前端展示错题时间
    @TableField(exist = false)
    private java.time.LocalDateTime mistakeTime; // 错题时间

    // 非数据库字段：用于查询时的关联信息
    @TableField(exist = false)
    private Integer subjectId; // 所属科目ID（通过map_question_subject查询）

    @TableField(exist = false)
    private Integer bookId; // 所属习题册ID（通过map_question_book查询）

    @TableField(exist = false)
    private String bookName; // 习题册名称（关联查询时使用）

    @TableField(exist = false)
    private String subjectName; // 科目名称（关联查询时使用）
}