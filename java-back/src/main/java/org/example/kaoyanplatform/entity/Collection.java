package org.example.kaoyanplatform.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import lombok.Data;

import java.util.Date;
import java.util.List;

@Data
@TableName(value = "tb_collection", autoResultMap = true)
public class Collection {
    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    private Long questionId;

    // 使用 JacksonTypeHandler 自动处理 JSON 转换
    @TableField(typeHandler = JacksonTypeHandler.class)
    private List<String> tags;

    private Date createTime;
}
