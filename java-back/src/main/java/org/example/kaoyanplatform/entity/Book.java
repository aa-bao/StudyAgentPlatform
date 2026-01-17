package org.example.kaoyanplatform.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@TableName("tb_book")
public class Book {
    @TableId(type = IdType.AUTO)
    private Integer id;
    private String name;
    // 移除外键字段：subjectId
    // 现在通过映射表 map_subject_book 管理书本与科目的关系
    private String description;
    private LocalDateTime createTime;

    // 非数据库字段：用于查询时的关联信息
    @TableField(exist = false)
    private Integer subjectId; // 所属科目ID（通过map_subject_book查询，已废弃，请使用subjectIds）

    @TableField(exist = false)
    private String subjectName; // 科目名称（关联查询时使用）

    @TableField(exist = false)
    private List<Integer> subjectIds; // 所属科目ID列表（支持多科目关联）

    @TableField(exist = false)
    private List<String> subjectNames; // 所属科目名称列表（与subjectIds对应）
}
