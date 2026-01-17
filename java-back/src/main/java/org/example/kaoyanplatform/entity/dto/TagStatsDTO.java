package org.example.kaoyanplatform.entity.dto;

import lombok.Data;

import java.util.List;

/**
 * 全局标签统计 DTO
 */
@Data
public class TagStatsDTO {
    /**
     * 标签名称
     */
    private String tagName;

    /**
     * 使用次数（该标签被多少个收藏记录使用）
     */
    private Integer usageCount;

    /**
     * 标签类型（system-系统预设, user-用户自定义）
     */
    private String tagType;

    /**
     * 所有标签统计列表
     */
    private List<TagStatsDTO> allTags;

    /**
     * 热门标签列表（按使用次数排序，TOP 20）
     */
    private List<TagStatsDTO> hotTags;
}
