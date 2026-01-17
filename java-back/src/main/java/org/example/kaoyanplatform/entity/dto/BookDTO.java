package org.example.kaoyanplatform.entity.dto;

import lombok.Data;

import java.util.List;

/**
 * 习题册数据传输对象
 * 支持多对多关联：一本书可以关联多个科目
 */
@Data
public class BookDTO {
    private Integer id;
    private String name;
    private String description;

    // ✅ 支持多对多关联
    private List<Integer> subjectIds;   // 关联的科目ID列表
}
