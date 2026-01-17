package org.example.kaoyanplatform.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 书本-科目关联映射表实体类
 * 用于管理习题册与科目的多对多关系
 * 一本习题册可以包含多个科目的内容
 */
@Data
@TableName("map_subject_book")
public class MapSubjectBook {
    @TableId(type = IdType.AUTO)
    private Integer id;

    /**
     * 习题册ID (如: 数一:4; 数二:5; 数三:6)
     */
    private Integer bookId;

    /**
     * 科目ID或知识点ID (如: 高数:401; 线代:402; 概率:403)
     */
    private Integer subjectId;
}
