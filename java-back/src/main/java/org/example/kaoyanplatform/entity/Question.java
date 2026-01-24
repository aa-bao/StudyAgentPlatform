package org.example.kaoyanplatform.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * 题目实体类（重构版：使用 content_json 存储题目核心内容）
 */
@Data
@TableName(value = "tb_question", autoResultMap = true)
public class Question {
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 题目类型 (1:单选, 2:多选, 3:填空, 4:简答)
     */
    private Integer type;

    /**
     * 难度等级 (1-5)
     */
    private Integer difficulty;

    /**
     * 题目内容JSON（包含题干、选项、答案、解析、标签等）
     * 单选/多选题结构:
     * {
     *   "content": "题目内容...",
     *   "options": [
     *     {"label": "A", "text": "选项1内容"},
     *     {"label": "B", "text": "选项2内容"},
     *     ...
     *   ],
     *   "answer": "A",
     *   "analysis": "解析内容...",
     *   "tags": ["标签1", "标签2"],
     *   "source": "题目来源"
     * }
     *
     * 填空/简答题结构:
     * {
     *   "content": "题目内容...",
     *   "answer": "答案内容",
     *   "analysis": "解析内容...",
     *   "tags": ["标签1", "标签2"],
     *   "source": "题目来源"
     * }
     */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private Map<String, Object> contentJson;

    /**
     * 创建时间
     */
    private java.time.LocalDateTime createTime;

    // ==================== 便捷访问方法 ====================

    /**
     * 获取题干内容
     */
    public String getContent() {
        return contentJson != null ? (String) contentJson.get("content") : null;
    }

    /**
     * 设置题干内容
     */
    public void setContent(String content) {
        if (contentJson == null) {
            contentJson = new java.util.HashMap<>();
        }
        contentJson.put("content", content);
    }

    /**
     * 获取选项列表（仅选择题型）
     */
    public List<Map<String, String>> getOptions() {
        if (contentJson == null) return null;
        Object options = contentJson.get("options");
        return options != null ? (List<Map<String, String>>) options : null;
    }

    /**
     * 设置选项列表（仅选择题型）
     */
    public void setOptions(List<Map<String, String>> options) {
        if (contentJson == null) {
            contentJson = new java.util.HashMap<>();
        }
        contentJson.put("options", options);
    }

    /**
     * 获取答案
     */
    public String getAnswer() {
        return contentJson != null ? (String) contentJson.get("answer") : null;
    }

    /**
     * 设置答案
     */
    public void setAnswer(String answer) {
        if (contentJson == null) {
            contentJson = new java.util.HashMap<>();
        }
        contentJson.put("answer", answer);
    }

    /**
     * 获取解析
     */
    public String getAnalysis() {
        return contentJson != null ? (String) contentJson.get("analysis") : null;
    }

    /**
     * 设置解析
     */
    public void setAnalysis(String analysis) {
        if (contentJson == null) {
            contentJson = new java.util.HashMap<>();
        }
        contentJson.put("analysis", analysis);
    }

    /**
     * 获取标签列表
     */
    public List<String> getTags() {
        if (contentJson == null) return null;
        Object tags = contentJson.get("tags");
        return tags != null ? (List<String>) tags : null;
    }

    /**
     * 设置标签列表
     */
    public void setTags(List<String> tags) {
        if (contentJson == null) {
            contentJson = new java.util.HashMap<>();
        }
        contentJson.put("tags", tags);
    }

    /**
     * 获取题目来源
     */
    public String getSource() {
        return contentJson != null ? (String) contentJson.get("source") : null;
    }

    /**
     * 设置题目来源
     */
    public void setSource(String source) {
        if (contentJson == null) {
            contentJson = new java.util.HashMap<>();
        }
        contentJson.put("source", source);
    }

    // ==================== 非数据库字段（关联信息） ====================

    /**
     * 非数据库字段：用于前端展示错题时间
     */
    @TableField(exist = false)
    private java.time.LocalDateTime mistakeTime;

    /**
     * 非数据库字段：所属科目ID列表（通过map_question_subject查询）
     */
    @TableField(exist = false)
    private List<Integer> subjectIds;

    /**
     * 非数据库字段：所属习题册ID列表（通过map_question_book查询）
     */
    @TableField(exist = false)
    private List<Integer> bookIds;

    /**
     * 非数据库字段：习题册名称（兼容旧版本，已废弃，请使用bookNames）
     */
    @TableField(exist = false)
    private String bookName;

    /**
     * 非数据库字段：习题册名称列表
     */
    @TableField(exist = false)
    private List<String> bookNames;

    /**
     * 非数据库字段：科目名称（兼容旧版本，已废弃，请使用subjectNames）
     */
    @TableField(exist = false)
    private String subjectName;

    /**
     * 非数据库字段：科目名称列表
     */
    @TableField(exist = false)
    private List<String> subjectNames;

    /**
     * 非数据库字段：题号（在试卷中的顺序）
     */
    @TableField(exist = false)
    private Integer sortOrder;

    /**
     * 非数据库字段：分值（在试卷中的分值）
     */
    @TableField(exist = false)
    private java.math.BigDecimal scoreValue;

    /**
     * 非数据库字段：题型（在试卷中的题型，可能覆盖原题型）
     */
    @TableField(exist = false)
    private Integer paperType;
}
