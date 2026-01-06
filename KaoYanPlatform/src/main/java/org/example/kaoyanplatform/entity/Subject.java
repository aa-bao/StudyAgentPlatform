package org.example.kaoyanplatform.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("tb_subject")
public class Subject {
    @TableId(type = IdType.AUTO)
    private Integer id;

    private String name;        // 科目名称
    private Integer parentId;   // 父级 ID，0 表示根节点
    private String icon;    // 图标
    private Integer sort;  // 排序号（值越小越靠前）

    /**
     * 层级
     * - 1: 考试大类（CATEGORY）- 如：公共课、专业课
     * - 2: 考试规格（EXAM_SPEC）- 如：数学一、数学二、英语一
     * - 3: 具体学科（SUBJECT）- 如：高等数学、线性代数、概率论
     * - 4: 知识点/章节（KNOWLEDGE_POINT）- 如：函数与极限、三重积分
     * - 5: 题型/解题方法（QUESTION_TYPE）- 如：泰勒公式
     */
    private String level;
    private String scope;  // 适用范围
}
