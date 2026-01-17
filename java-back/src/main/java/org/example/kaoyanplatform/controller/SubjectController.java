package org.example.kaoyanplatform.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.example.kaoyanplatform.common.Result;
import org.example.kaoyanplatform.constant.SubjectLevelConstants;
import org.example.kaoyanplatform.entity.Subject;
import org.example.kaoyanplatform.entity.dto.SubjectDTO;
import org.example.kaoyanplatform.service.SubjectService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Tag(name = "科目管理", description = "科目管理相关接口")
@RestController
@RequestMapping("/subject")
public class SubjectController {

    @Autowired
    private SubjectService subjectService;

    // 1. 获取所有科目列表
    @GetMapping("/list")
    @Operation(summary = "获取所有科目", description = "获取系统中所有科目列表（无分页，扁平化数据）。")
    public Result getList() {
        return Result.success(subjectService.list());
    }

    // 2. 获取科目树
    @GetMapping("/tree")
    @Operation(summary = "获取科目树", description = "获取科目树形结构。支持通过 userId 过滤用户相关的树。")
    public Result getTree(
            @Parameter(description = "用户ID，可选") @RequestParam(required = false) Long userId,
            @Parameter(description = "根节点ID，可选。不传则从顶级开始") @RequestParam(required = false) Integer rootId) {
        return Result.success(subjectService.getTree(userId, rootId));
    }

    // 3. 按层级获取科目列表
    @GetMapping("/by-level")
    @Operation(summary = "按层级获取科目", description = "根据层级值（CATEGORY/EXAM_SPEC等）获取科目树。")
    public Result getTreeByLevel(
            @Parameter(description = "层级值", required = true) @RequestParam String level,
            @Parameter(description = "用户ID，可选") @RequestParam(required = false) Long userId) {

        // 校验层级常量
        if (!SubjectLevelConstants.isValidLevel(level)) {
            return Result.error("无效的层级值");
        }
        return Result.success(subjectService.getTreeByLevel(level, userId));
    }

    // 4. 获取考试规格列表（Level 2）
    @GetMapping("/exam-specs")
    @Operation(summary = "获取考试规格列表", description = "获取 Level 2 考试规格列表（如：数学一、英语二等）。")
    public Result getExamSpecList() {
        return Result.success(subjectService.getExamSpecList());
    }

    // 5. 根据考试规格获取科目树
    @GetMapping("/by-exam-spec/{examSpecId}")
    @Operation(summary = "根据考试规格获取科目树", description = "获取指定考试规格下的科目树（Level 3 及以下）。")
    public Result getTreeByExamSpec(
            @PathVariable Integer examSpecId,
            @Parameter(description = "用户ID，可选") @RequestParam(required = false) Long userId) {
        return Result.success(subjectService.getTreeByExamSpec(examSpecId, userId));
    }

    // 6. 获取管理用科目树
    @GetMapping("/manage-tree")
    @Operation(summary = "获取管理用科目树", description = "后台管理使用的完整科目树结构。")
    public Result getManageTree() {
        return Result.success(subjectService.getManageTree());
    }

    // 6.5. 根据科目ID获取支持的题型列表
    @GetMapping("/{subjectId}/question-types")
    @Operation(summary = "获取科目支持的题型", description = "根据科目ID获取该科目支持的题型列表")
    public Result getQuestionTypesBySubject(
            @Parameter(description = "科目ID", required = true) @PathVariable Integer subjectId) {
        try {
            Subject subject = subjectService.getById(subjectId);
            if (subject == null) {
                return Result.error("科目不存在");
            }

            // 解析题型字符串
            java.util.List<Integer> typeCodes = new java.util.ArrayList<>();
            if (subject.getQuestionTypes() != null && !subject.getQuestionTypes().isEmpty()) {
                String[] codes = subject.getQuestionTypes().split(",");
                for (String code : codes) {
                    try {
                        typeCodes.add(Integer.parseInt(code.trim()));
                    } catch (NumberFormatException e) {
                        // 忽略无效的题型代码
                    }
                }
            }

            // 如果没有配置题型，返回所有题型
            if (typeCodes.isEmpty()) {
                return Result.success(org.example.kaoyanplatform.enums.QuestionType.getList());
            }

            // 返回配置的题型
            java.util.List<Map<String, Object>> result = new java.util.ArrayList<>();
            for (Integer code : typeCodes) {
                org.example.kaoyanplatform.enums.QuestionType type =
                    org.example.kaoyanplatform.enums.QuestionType.getByCode(code);
                if (type != null) {
                    Map<String, Object> typeMap = new java.util.HashMap<>();
                    typeMap.put("code", type.getCode());
                    typeMap.put("name", type.getName());
                    result.add(typeMap);
                }
            }

            return Result.success(result);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    // 7. 新增科目
    @PostMapping("/add")
    @Operation(summary = "新增科目")
    public Result addSubject(@RequestBody Subject subject) {
        if (subject.getName() == null || subject.getLevel() == null) {
            return Result.error("科目名称或层级不能为空");
        }
        boolean success = subjectService.addSubject(subject);
        return success ? Result.success("添加成功") : Result.error("添加失败");
    }

    // 8. 更新科目
    @PostMapping("/update")
    @Operation(summary = "更新科目")
    public Result updateSubject(@RequestBody Subject subject) {
        if (subject.getId() == null) {
            return Result.error("科目ID不能为空");
        }
        boolean success = subjectService.updateSubject(subject);
        return success ? Result.success("更新成功") : Result.error("更新失败");
    }

    // 9. 删除科目
    @DeleteMapping("/delete/{id}")
    @Operation(summary = "删除科目", description = "如果科目有下级或关联数据，将无法删除。")
    public Result deleteSubject(@PathVariable Integer id) {
        String message = subjectService.deleteSubject(id);
        if ("删除成功".equals(message)) {
            return Result.success(message);
        } else {
            return Result.error(message);
        }
    }

    // 10. 根据ID获取科目详情
    @GetMapping("/{id}")
    @Operation(summary = "获取科目详情")
    public Result getSubjectById(@PathVariable Integer id) {
        Subject subject = subjectService.getById(id);
        return subject != null ? Result.success(subject) : Result.error("科目不存在");
    }

    // 11. 批量更新科目排序
    @PostMapping("/batch-update-sort")
    @Operation(summary = "批量更新排序", description = "用于拖拽排序。")
    public Result batchUpdateSort(@RequestBody List<Subject> subjects) {
        if (subjects == null || subjects.isEmpty()) {
            return Result.error("更新列表不能为空");
        }
        boolean success = subjectService.batchUpdateSort(subjects);
        return success ? Result.success("排序更新成功") : Result.error("排序更新失败");
    }
}