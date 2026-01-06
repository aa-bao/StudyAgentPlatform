package org.example.kaoyanplatform.constant;

/**
 * 科目层级常量定义
 *
 * 层级结构说明：
 * - Level 1 (EXAM_SPEC): 考试规格（如：政治、数学一、数学二、英语一）
 * - Level 2 (SUBJECT): 具体学科（如：高等数学、线性代数、概率论）
 * - Level 3 (KNOWLEDGE_POINT): 知识点（如：函数与极限、三重积分）
 * - Level 4 (QUESTION_TYPE): 题型与方法（如：泰勒公式、洛必达法则）
 *
 * @author KaoYanPlatform
 * @since 2026-01-06
 */
public final class SubjectLevelConstants {

    private SubjectLevelConstants() {
        // 私有构造函数，防止实例化
    }

    // ==================== 层级值定义 ====================

    /**
     * Level 1: 考试规格
     * 示例：政治、数学一、数学二、英语一、英语二、408
     */
    public static final String LEVEL_EXAM_SPEC = "1";

    /**
     * Level 2: 具体学科
     * 示例：高等数学、线性代数、概率论、数据结构、马原
     */
    public static final String LEVEL_SUBJECT = "2";

    /**
     * Level 3: 知识点/章节
     * 示例：函数与极限、导数与微分、积分
     */
    public static final String LEVEL_KNOWLEDGE_POINT = "3";

    /**
     * Level 4: 题型/解题方法
     * 示例：泰勒公式、洛必达法则
     */
    public static final String LEVEL_QUESTION_TYPE = "4";

    // ==================== 层级标签定义 ====================

    /**
     * 层级标签映射
     */
    public static final String LABEL_EXAM_SPEC = "规格";
    public static final String LABEL_SUBJECT = "学科";
    public static final String LABEL_KNOWLEDGE_POINT = "知识点";
    public static final String LABEL_QUESTION_TYPE = "题型";

    // ==================== 工具方法 ====================

    /**
     * 根据层级值获取层级标签
     *
     * @param level 层级值（1-4）
     * @return 层级标签
     */
    public static String getLevelLabel(String level) {
        if (level == null) {
            return "未知";
        }
        switch (level) {
            case LEVEL_EXAM_SPEC:
                return LABEL_EXAM_SPEC;
            case LEVEL_SUBJECT:
                return LABEL_SUBJECT;
            case LEVEL_KNOWLEDGE_POINT:
                return LABEL_KNOWLEDGE_POINT;
            case LEVEL_QUESTION_TYPE:
                return LABEL_QUESTION_TYPE;
            default:
                return "未知";
        }
    }

    /**
     * 验证层级值是否有效
     *
     * @param level 层级值
     * @return 是否有效
     */
    public static boolean isValidLevel(String level) {
        if (level == null) {
            return false;
        }
        return LEVEL_EXAM_SPEC.equals(level)
                || LEVEL_SUBJECT.equals(level)
                || LEVEL_KNOWLEDGE_POINT.equals(level)
                || LEVEL_QUESTION_TYPE.equals(level);
    }

    /**
     * 获取下一个层级
     *
     * @param currentLevel 当前层级
     * @return 下一个层级，如果已经是最高层级则返回 null
     */
    public static String getNextLevel(String currentLevel) {
        if (currentLevel == null) {
            return null;
        }
        switch (currentLevel) {
            case LEVEL_EXAM_SPEC:
                return LEVEL_SUBJECT;
            case LEVEL_SUBJECT:
                return LEVEL_KNOWLEDGE_POINT;
            case LEVEL_KNOWLEDGE_POINT:
                return LEVEL_QUESTION_TYPE;
            case LEVEL_QUESTION_TYPE:
                return null;
            default:
                return null;
        }
    }

    /**
     * 获取上一个层级
     *
     * @param currentLevel 当前层级
     * @return 上一个层级，如果已经是最低层级则返回 null
     */
    public static String getPreviousLevel(String currentLevel) {
        if (currentLevel == null) {
            return null;
        }
        switch (currentLevel) {
            case LEVEL_SUBJECT:
                return LEVEL_EXAM_SPEC;
            case LEVEL_KNOWLEDGE_POINT:
                return LEVEL_SUBJECT;
            case LEVEL_QUESTION_TYPE:
                return LEVEL_KNOWLEDGE_POINT;
            case LEVEL_EXAM_SPEC:
                return null;
            default:
                return null;
        }
    }

    /**
     * 检查是否为根层级（考试规格）
     *
     * @param level 层级值
     * @return 是否为根层级
     */
    public static boolean isRootLevel(String level) {
        return LEVEL_EXAM_SPEC.equals(level);
    }

    /**
     * 检查是否为叶子层级（题型）
     *
     * @param level 层级值
     * @return 是否为叶子层级
     */
    public static boolean isLeafLevel(String level) {
        return LEVEL_QUESTION_TYPE.equals(level);
    }

    /**
     * 检查是否为考试规格层级
     *
     * @param level 层级值
     * @return 是否为考试规格层级
     */
    public static boolean isExamSpecLevel(String level) {
        return LEVEL_EXAM_SPEC.equals(level);
    }

    /**
     * 检查是否为具体学科层级
     *
     * @param level 层级值
     * @return 是否为具体学科层级
     */
    public static boolean isSubjectLevel(String level) {
        return LEVEL_SUBJECT.equals(level);
    }
}
