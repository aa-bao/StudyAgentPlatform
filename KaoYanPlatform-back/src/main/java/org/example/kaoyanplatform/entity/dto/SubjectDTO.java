package org.example.kaoyanplatform.entity.dto;

import lombok.Data;
import java.util.List;

/**
 * 科目数据传输对象
 * 用于前后端数据交互
 */
@Data
public class SubjectDTO {
    private Integer id;

    private String name;

    private Integer parentId;

    private String icon;

    private Integer sort;

    private String level;

    private String scope;

    /**
     * 动态权重（根据当前考试规格计算得出）
     * 用于解决英语一/英语二等不同考试规格下，相同科目名称但权重不同的问题
     *
     * 注意：原 weight 字段已删除，统一使用 dynamicWeight
     */
    private Float dynamicWeight;

    /**
     * 树形结构字段
     */
    private List<SubjectDTO> children;

    /**
     * 题目总数
     */
    private Integer questionCount;

    /**
     * 已完成题目数
     */
    private Integer finishedCount;

    /**
     * 树形ID，格式为 "父级ID-自身ID"
     */
    private String treeId;

    /**
     * 掌握度（用于前端展示）
     */
    private Integer mastery;

    /**
     * 解题通法（用于前端展示）
     */
    private List<String> solutionPatterns;

    /**
     * 高频误区（用于前端展示）
     */
    private List<String> commonMistakes;

    /**
     * 考察热度（用于前端展示）
     */
    private Integer examFrequency;

    /**
     * 展开状态（用于前端树形组件）
     */
    private Boolean expanded;
}
