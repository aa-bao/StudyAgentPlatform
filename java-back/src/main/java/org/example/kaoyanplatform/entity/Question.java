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
    private List<Integer> subjectIds; // 所属科目ID列表（通过map_question_subject查询）

    @TableField(exist = false)
    private List<Integer> bookIds; // 所属习题册ID列表（通过map_question_book查询）

    @TableField(exist = false)
    private String bookName; // 习题册名称（兼容旧版本，已废弃，请使用bookNames）

    @TableField(exist = false)
    private List<String> bookNames; // 习题册名称列表

    @TableField(exist = false)
    private String subjectName; // 科目名称（兼容旧版本，已废弃，请使用subjectNames）

    @TableField(exist = false)
    private List<String> subjectNames; // 科目名称列表

    // 试卷相关字段（非数据库字段）
    @TableField(exist = false)
    private Integer sortOrder; // 题号（在试卷中的顺序）

    @TableField(exist = false)
    private java.math.BigDecimal scoreValue; // 分值（在试卷中的分值）

    @TableField(exist = false)
    private Integer paperType; // 题型（在试卷中的题型，可能覆盖原题型）
}