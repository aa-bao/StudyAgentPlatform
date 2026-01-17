package org.example.kaoyanplatform.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.example.kaoyanplatform.common.Result;
import org.example.kaoyanplatform.enums.QuestionType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * 公共接口控制器
 * 提供枚举类型、字典数据等公共接口
 */
@Tag(name = "公共数据", description = "提供系统中的枚举类型、字典数据等")
@RestController
@RequestMapping("/common")
public class CommonController {

    @GetMapping("/question-types")
    @Operation(summary = "获取题目类型列表", description = "获取所有题目类型的代码和名称")
    public Result<List<Map<String, Object>>> getQuestionTypes() {
        return Result.success(QuestionType.getList());
    }
}
