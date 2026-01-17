package org.example.kaoyanplatform.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.*;

/**
 * 题目类型枚举
 */
@Getter
@AllArgsConstructor
public enum QuestionType {

    SINGLE_CHOICE(1, "单选题"),
    MULTIPLE_CHOICE(2, "多选题"),
    FILL_BLANK(3, "填空题"),
    COMPREHENSIVE(4, "综合应用题/大题"),
    CLOZE_TEST(5, "完型填空"),
    READING_COMPREHENSION(6, "阅读理解"),
    NEW_TYPE(7, "新题型"),
    TRANSLATION(8, "翻译题"),
    SHORT_ESSAY(9, "小作文"),
    LONG_ESSAY(10, "大作文");

    private final Integer code;
    private final String name;

    /**
     * 根据代码获取枚举
     */
    public static QuestionType getByCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (QuestionType type : values()) {
            if (type.code.equals(code)) {
                return type;
            }
        }
        return null;
    }

    /**
     * 根据代码获取名称
     */
    public static String getNameByCode(Integer code) {
        QuestionType type = getByCode(code);
        return type != null ? type.name : "未知类型";
    }

    /**
     * 获取所有题目类型列表（用于前端下拉选择）
     */
    public static List<Map<String, Object>> getList() {
        List<Map<String, Object>> list = new ArrayList<>();
        for (QuestionType type : values()) {
            Map<String, Object> map = new HashMap<>();
            map.put("code", type.code);
            map.put("name", type.name);
            list.add(map);
        }
        return list;
    }
}
