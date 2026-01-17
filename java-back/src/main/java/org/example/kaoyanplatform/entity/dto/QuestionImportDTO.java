package org.example.kaoyanplatform.entity.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 题目导入请求DTO
 * 用于接收JSON批量导入的请求数据
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class QuestionImportDTO {

    /** 习题册ID */
    private Integer bookId;

    /** 科目ID列表（支持多个科目关联） */
    private List<Integer> subjectIds;

    /** 题目列表 */
    private List<QuestionImportItem> questions;

    /**
     * 单个题目导入项
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class QuestionImportItem {
        /** 题号（可选，用于试卷导入时的题目排序） */
        private Integer questionNumber;

        /** 题目类型: 1-单选, 2-多选, 3-填空, 4-简答 */
        private Integer type;

        /** 题干内容 */
        private String content;

        /** 选项数组 */
        private List<String> options;

        /** 正确答案 */
        private String answer;

        /** 解析 */
        private String analysis;

        /** 题目标签 */
        private List<String> tags;

        /** 题目来源（可选） */
        private String source;
    }
}
