package org.example.kaoyanplatform.entity.dto;

import lombok.Data;

import java.util.List;

/**
 * 错题热力统计 DTO
 */
@Data
public class MistakeHeatmapDTO {
    /**
     * 科目ID
     */
    private Integer subjectId;

    /**
     * 科目名称
     */
    private String subjectName;

    /**
     * 总错误次数（该科目下所有题目的 errorCount 总和）
     */
    private Integer totalErrorCount;

    /**
     * 错题题目数（有错题记录的题目数量）
     */
    private Integer mistakeQuestionCount;

    /**
     * 涉及用户数（做错该科目题目的不同用户数）
     */
    private Integer affectedUserCount;

    /**
     * 高频错题列表（TOP 10）
     */
    private List<HotMistakeQuestion> hotQuestions;

    /**
     * 高频错题
     */
    @Data
    public static class HotMistakeQuestion {
        /**
         * 题目ID
         */
        private Integer questionId;

        /**
         * 题目内容（截取前100字）
         */
        private String questionContent;

        /**
         * 总错误次数
         */
        private Integer totalErrorCount;

        /**
         * 错误用户数
         */
        private Integer errorUserCount;

        /**
         * 科目ID
         */
        private Integer subjectId;
    }
}
