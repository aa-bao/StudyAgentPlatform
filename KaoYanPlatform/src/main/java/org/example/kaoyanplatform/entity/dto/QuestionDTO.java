package org.example.kaoyanplatform.entity.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 题目数据传输对象
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

    /** 题干内容 */
    private String content;

    /** 选项(JSON数组: ["A.选项1", "B.选项2"]) */
    private List<String> options;

    /** 正确答案 */
    private String answer;

    /** 解析 */
    private String analysis;

    /** 难度: 1-5 */
    private Integer difficulty;

    /** 题目标签 */
    private List<String> tags;

    /** 题目来源 */
    private String source;

    /** 关联的书本ID列表（多对多） */
    private List<Integer> bookIds;

    /** 关联的科目ID列表（多对多） */
    private List<Integer> subjectIds;

    /** 创建时间（编辑时返回） */
    @JsonProperty("create_time")
    private LocalDateTime createTime;
}
