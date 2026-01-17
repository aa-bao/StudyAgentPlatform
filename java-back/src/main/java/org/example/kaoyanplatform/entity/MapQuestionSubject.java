package org.example.kaoyanplatform.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 题目-科目关联映射表实体类
 * 用于管理题目与科目/知识点的多对多关系
 * 一道题可以属于多个科目或知识点
 */
@Data
@TableName("map_question_subject")
public class MapQuestionSubject {
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 题目ID
     */
    private Long questionId;

    /**
     * 科目ID或知识点ID
     */
    private Integer subjectId;
}
