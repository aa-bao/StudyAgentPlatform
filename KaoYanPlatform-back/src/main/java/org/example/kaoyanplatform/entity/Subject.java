package org.example.kaoyanplatform.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.List;

/**
 * 科目实体类
 * 对应数据库表：tb_subject
 */
@Data
@TableName("tb_subject")
public class Subject {
    @TableId(type = IdType.AUTO)
    private Integer id;

    /**
     * 科目名称
     */
    private String name;

    /**
     * 父级 ID，0 表示根节点
     */
    private Integer parentId;

    /**
     * 图标
     */
    private String icon;

    /**
     * 排序号（值越小越靠前）
     */
    private Integer sort;

    /**
     * 层级
     * - 1: 考试大类（CATEGORY）- 如：政治、英语一、数学一、408
     * - 2: 考试规格（EXAM_SPEC）- 如：马原、毛中特、完形填空、高等数学
     * - 3: 具体学科（SUBJECT）- 如：函数与极限、行列式
     * - 4: 知识点/章节（KNOWLEDGE_POINT）- 如：数列敛散性判定
     * - 5: 题型/解题方法（QUESTION_TYPE）- 如：泰勒公式
     */
    private String level;

    /**
     * 适用范围
     * 格式：逗号分隔的考试规格ID，如 "2,3" 表示适用于英语一和英语二
     */
    private String scope;

    /**
     * 该分类下的题目总数
     */
    private Integer questionCount;

    /**
     * 动态权重（根据当前考试规格计算得出）
     * 该字段不存储在数据库中，通过查询 map_subject_weight 表动态获取
     *
     * 注意：原 weight 字段已删除，统一使用 dynamicWeight 实现不同考试规格下的权重差异化
     */
    @TableField(exist = false)
    private Float dynamicWeight;

    /**
     * 子科目列表（用于构建树形结构）
     */
    @TableField(exist = false)
    private List<Subject> children;

    /**
     * 掌握度（用于前端展示）
     */
    @TableField(exist = false)
    private Integer mastery;

    /**
     * 解题通法（用于前端展示）
     */
    @TableField(exist = false)
    private List<String> solutionPatterns;

    /**
     * 高频误区（用于前端展示）
     */
    @TableField(exist = false)
    private List<String> commonMistakes;

    /**
     * 考察热度（用于前端展示）
     */
    @TableField(exist = false)
    private Integer examFrequency;

    /**
     * 展开状态（用于前端树形组件）
     */
    @TableField(exist = false)
    private Boolean expanded;
}
