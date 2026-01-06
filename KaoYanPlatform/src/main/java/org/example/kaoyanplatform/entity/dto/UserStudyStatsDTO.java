package org.example.kaoyanplatform.entity.dto;

import lombok.Data;

import java.util.List;

/**
 * 用户学习统计数据传输对象
 */
@Data
public class UserStudyStatsDTO {
    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 用户名
     */
    private String username;

    /**
     * 昵称
     */
    private String nickname;

    /**
     * 目标院校
     */
    private String targetSchool;

    /**
     * 考研年份
     */
    private String examYear;

    /**
     * 各科目统计数据
     */
    private List<SubjectStats> subjectStats;

    /**
     * 总体统计
     */
    private OverallStats overallStats;

    /**
     * 科目统计
     */
    @Data
    public static class SubjectStats {
        /**
         * 科目ID
         */
        private Integer subjectId;

        /**
         * 科目名称
         */
        private String subjectName;

        /**
         * 完成题目数
         */
        private Integer finishedCount;

        /**
         * 正确题目数
         */
        private Integer correctCount;

        /**
         * 正确率（百分比）
         */
        private Double accuracy;

        /**
         * 覆盖度（完成题数/总题数，百分比）
         */
        private Double coverage;

        /**
         * 总题数
         */
        private Integer totalCount;
    }

    /**
     * 总体统计
     */
    @Data
    public static class OverallStats {
        /**
         * 总完成题目数
         */
        private Integer totalFinished;

        /**
         * 总正确题目数
         */
        private Integer totalCorrect;

        /**
         * 总正确率（百分比）
         */
        private Double overallAccuracy;

        /**
         * 活跃度（最近7天完成的题目数）
         */
        private Integer activityScore;
    }
}
