package org.example.kaoyanplatform.controller;

import org.example.kaoyanplatform.common.Result;
import org.example.kaoyanplatform.constant.SubjectLevelConstants;
import org.example.kaoyanplatform.entity.Subject;
import org.example.kaoyanplatform.entity.dto.SubjectDTO;
import org.example.kaoyanplatform.service.SubjectService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping("/subject")
public class SubjectController {

    @Autowired
    private SubjectService subjectService;

    // 获取所有科目列表
    @GetMapping("/list")
    public Result getList() {
        return Result.success(subjectService.list());
    }

    // 取科目树
    @GetMapping("/tree")
    public Result getTree(@RequestParam(required = false) Long userId,
                         @RequestParam(required = false) Integer rootId) {
        return Result.success(subjectService.getTree(userId, rootId));
    }

    // 按层级获取科目列表
    @GetMapping("/by-level")
    public Result getTreeByLevel(@RequestParam String level,
                                 @RequestParam(required = false) Long userId) {
        if (!SubjectLevelConstants.isValidLevel(level)) {
            return Result.error("无效的层级值");
        }
        return Result.success(subjectService.getTreeByLevel(level, userId));
    }

    /**
     * 获取考试规格列表（Level 2）
     * 用于 SubjectList 页面展示考试规格选择
     */
    @GetMapping("/exam-specs")
    public Result getExamSpecList() {
        return Result.success(subjectService.getExamSpecList());
    }

    /**
     * 根据考试规格获取科目树
     * 返回指定考试规格下的所有科目（Level 3 及以下）
     */
    @GetMapping("/by-exam-spec/{examSpecId}")
    public Result getTreeByExamSpec(@PathVariable Integer examSpecId,
                                   @RequestParam(required = false) Long userId) {
        return Result.success(subjectService.getTreeByExamSpec(examSpecId, userId));
    }

    // 获取管理用科目树
    @GetMapping("/manage-tree")
    public Result getManageTree() {
        List<SubjectDTO> tree = subjectService.getManageTree();
        return Result.success(tree);
    }

    // 新增科目
    @PostMapping("/add")
    public Result addSubject(@RequestBody Subject subject) {
        boolean success = subjectService.addSubject(subject);
        return success ? Result.success("添加成功") : Result.error("添加失败");
    }

    // 更新科目
    @PostMapping("/update")
    public Result updateSubject(@RequestBody Subject subject) {
        boolean success = subjectService.updateSubject(subject);
        return success ? Result.success("更新成功") : Result.error("更新失败");
    }

    // 删除科目
    @DeleteMapping("/delete/{id}")
    public Result deleteSubject(@PathVariable Integer id) {
        String message = subjectService.deleteSubject(id);
        if ("删除成功".equals(message)) {
            return Result.success(message);
        } else {
            return Result.error(message);
        }
    }

    // 根据ID获取科目详情
    @GetMapping("/{id}")
    public Result getSubjectById(@PathVariable Integer id) {
        Subject subject = subjectService.getById(id);
        if (subject == null) {
            return Result.error("科目不存在");
        }
        return Result.success(subject);
    }

    // 批量更新科目排序（用于拖拽排序）
    @PostMapping("/batch-update-sort")
    public Result batchUpdateSort(@RequestBody List<Subject> subjects) {
        boolean success = subjectService.batchUpdateSort(subjects);
        return success ? Result.success("排序更新成功") : Result.error("排序更新失败");
    }
}