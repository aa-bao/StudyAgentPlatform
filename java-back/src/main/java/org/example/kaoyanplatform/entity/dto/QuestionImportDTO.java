package org.example.kaoyanplatform.entity.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * 题目导入请求DTO（重构版：支持新的 contentJson 格式）
 * 用于接收JSON批量导入的请求数据
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class QuestionImportDTO {

    /** 习题册ID（如果新建习题册则为null） */
    private Integer bookId;

    /** 新建习题册名称（如果选择现有习题册则为null） */
    private String newBookName;

    /** 新建习题册类型（1=习题册, 2=试卷，仅在新建时有效） */
    private Integer newBookType;

    /** 科目ID列表（必填，支持多个科目关联） */
    private List<Integer> subjectIds;

    /** 是否启用去重检查 */
    private Boolean checkDuplicate;

    /** 题目列表 */
    private List<QuestionImportItem> questions;

    /**
     * 单个题目导入项（重构版：支持新的选项格式）
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

        /**
         * 选项数组（支持两种格式）
         * 格式1（旧格式，兼容）: ["选项1", "选项2", "选项3", "选项4"]
         * 格式2（新格式，推荐）: [
         *   {"label": "A", "text": "选项1"},
         *   {"label": "B", "text": "选项2"},
         *   {"label": "C", "text": "选项3"},
         *   {"label": "D", "text": "选项4"}
         * ]
         */
        private Object options;

        /** 正确答案 */
        private String answer;

        /** 解析 */
        private String analysis;

        /** 题目标签 */
        private List<String> tags;

        /** 题目来源（可选） */
        private String source;

        /** 难度（可选） */
        private Integer difficulty;
    }
}
