package org.example.kaoyanplatform.entity.dto;

import lombok.Data;

import java.util.List;

/**
 * 首页数据传输对象
 * 包含用户首页所需的所有数据
 */
@Data
public class HomePageDataDTO {
    /**
     * 用户基本信息
     */
    private UserInfo userInfo;

    /**
     * 学习统计数据
     */
    private StudyStats studyStats;

    /**
     * 考试科目列表
     */
    private List<Subject> subjects;

    /**
     * 每日励志语录
     */
    private DailyQuote dailyQuote;

    /**
     * 个性化推荐
     */
    private List<Recommendation> recommendations;

    /**
     * 用户信息
     */
    @Data
    public static class UserInfo {
        private Long id;
        private String username;
        private String nickname;
        private String avatar;
        private String role;
        private String targetSchool;
        private Integer targetTotalScore;
        private String examYear;
        private String examDate;
        private String examSubjects;
    }

    /**
     * 学习统计
     */
    @Data
    public static class StudyStats {
        /**
         * 累计刷题数
         */
        private Integer questionsDone;

        /**
         * 今日学习时长(小时)
         */
        private Double todayStudyTime;

        /**
         * 总体正确率
         */
        private Double accuracy;

        /**
         * 错题本数量
         */
        private Integer mistakesCount;

        /**
         * 总学习时长(小时)
         */
        private Double totalStudyHours;

        /**
         * 连续打卡天数
         */
        private Integer consecutiveDays;
    }

    /**
     * 科目信息
     */
    @Data
    public static class Subject {
        private Integer id;
        private String name;
        private Integer level;
    }

    /**
     * 每日励志语录
     */
    @Data
    public static class DailyQuote {
        private String content;
        private String author;
    }

    /**
     * 个性化推荐
     */
    @Data
    public static class Recommendation {
        private Integer id;
        private String subjectName;
        private String subjectColor;
        private Integer difficulty;
        private String content;
        private String reason;
    }
}
