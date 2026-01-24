package org.example.kaoyanplatform.entity.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 题目数据传输对象（重构版：使用 contentJson 存储题目核心内容）
 * 用于新增/编辑题目时接收前端数据
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class QuestionDTO {

    /** 题目ID（编辑时需要） */
    private Long id;

    /** 题目类型: 1-单选, 2-多选, 3-填空, 4-简答 */
    private Integer type;

    /** 难度: 1-5 */
    private Integer difficulty;

    /**
     * 题目内容JSON（包含题干、选项、答案、解析、标签等）
     * 前端传入的格式与 Question.contentJson 相同
     */
    private Map<String, Object> contentJson;

    /**
     * 题干内容（兼容字段，自动同步到 contentJson.content）
     */
    private String content;

    /**
     * 选项列表（兼容字段，自动同步到 contentJson.options）
     * 单选/多选题: [{"label": "A", "text": "选项1"}, {"label": "B", "text": "选项2"}, ...]
     */
    private List<Map<String, String>> options;

    /**
     * 正确答案（兼容字段，自动同步到 contentJson.answer）
     */
    private String answer;

    /**
     * 解析（兼容字段，自动同步到 contentJson.analysis）
     */
    private String analysis;

    /**
     * 题目标签（兼容字段，自动同步到 contentJson.tags）
     */
    private List<String> tags;

    /**
     * 题目来源（兼容字段，自动同步到 contentJson.source）
     */
    private String source;

    /** 关联的书本ID列表（多对多） */
    private List<Integer> bookIds;

    /** 关联的科目ID列表（多对多） */
    private List<Integer> subjectIds;

    /** 创建时间（编辑时返回） */
    @JsonProperty("create_time")
    private LocalDateTime createTime;

    // ==================== 便捷方法：将兼容字段同步到 contentJson ====================

    /**
     * 获取完整的 contentJson（合并所有字段）
     * 这个方法在保存时调用，确保兼容字段被合并到 contentJson 中
     */
    public Map<String, Object> getMergedContentJson() {
        Map<String, Object> json = contentJson != null ? contentJson : new java.util.HashMap<>();

        // 将兼容字段合并到 contentJson（如果 contentJson 中没有对应字段）
        if (content != null) {
            json.put("content", content);
        }
        if (options != null && !json.containsKey("options")) {
            json.put("options", options);
        }
        if (answer != null && !json.containsKey("answer")) {
            json.put("answer", answer);
        }
        if (analysis != null && !json.containsKey("analysis")) {
            json.put("analysis", analysis);
        }
        if (tags != null && !json.containsKey("tags")) {
            json.put("tags", tags);
        }
        if (source != null && !json.containsKey("source")) {
            json.put("source", source);
        }

        return json;
    }
}
