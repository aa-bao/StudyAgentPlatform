package org.example.kaoyanplatform.entity.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 题目导出请求DTO
 * 用于PDF导出的查询条件
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class QuestionExportDTO {

    /** 科目ID（根据科目导出） */
    private Integer subjectId;

    /** 习题册ID（根据习题册导出） */
    private Integer bookId;

    /** 题目ID列表（精确指定题目） */
    private List<Long> questionIds;

    /** 是否包含答案（安全考虑，默认false） */
    private Boolean includeAnswers = false;
}
