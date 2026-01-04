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
    private String name;
    private Integer parentId;
    private String icon;
    private Integer sort;
    private String level; // 1-专业课/公共课；2-章;3-节/知识点
    private String scope; // 1-数一,2-数二, 3-数三
}
