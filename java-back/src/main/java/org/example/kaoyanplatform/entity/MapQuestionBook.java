package org.example.kaoyanplatform.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 题目-书本关联映射表实体类
 * 用于管理题目与习题册的多对多关系
 */
@Data
@TableName("map_question_book")
public class MapQuestionBook {
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 题目ID
     */
    private Long questionId;

    /**
     * 习题册ID
     */
    private Integer bookId;
}
