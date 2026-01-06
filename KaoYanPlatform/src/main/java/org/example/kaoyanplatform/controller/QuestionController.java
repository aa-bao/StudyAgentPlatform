package org.example.kaoyanplatform.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.example.kaoyanplatform.common.Result;
import org.example.kaoyanplatform.entity.Question;
import org.example.kaoyanplatform.entity.MistakeRecord;
import org.example.kaoyanplatform.entity.dto.QuestionDTO;
import org.example.kaoyanplatform.mapper.QuestionMapper;
import org.example.kaoyanplatform.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Tag(name = "题目管理", description = "题目增删改查接口")
@RestController
@RequestMapping("/question")
public class QuestionController {

    @Autowired
    private QuestionMapper questionMapper;

    @Autowired
    private QuestionService questionService;

    @Autowired
    private MistakeRecordService mistakeRecordService;

    @Autowired
    private SubjectService subjectService;

    @Autowired
    private MapQuestionSubjectService mapQuestionSubjectService;

    @Autowired
    private MapQuestionBookService mapQuestionBookService;

    // 1. 按知识点获取题目（递归下级）
    @GetMapping("/list-by-knowledge-point")
    @Operation(summary = "按知识点获取题目", description = "根据科目ID及其所有子科目递归查询题目。")
    public Result getQuestionsByKnowledgePoint(@RequestParam Integer subjectId) {
        List<Integer> subjectIds = subjectService.getDescendantIds(subjectId);
        List<Question> questions = questionService.getQuestionsBySubjectIds(subjectIds);
        return Result.success(questions);
    }

    // 2. 按科目或书本获取题目
    @GetMapping("/list-by-subject")
    @Operation(summary = "按科目或书本获取题目", description = "根据科目ID或书本ID获取题目。")
    public Result getQuestionsBySubject(
            @RequestParam(required = false) Integer subjectId,
            @RequestParam(required = false) Integer bookId) {

        List<Long> questionIds = null;
        if (subjectId != null) {
            questionIds = mapQuestionSubjectService.getQuestionIdsBySubjectId(subjectId);
        } else if (bookId != null) {
            questionIds = mapQuestionBookService.getQuestionIdsByBookId(bookId);
        }

        if (questionIds == null || questionIds.isEmpty()) {
            return (subjectId != null || bookId != null) ? Result.success(new ArrayList<>()) : Result.success(questionMapper.selectList(null));
        }

        List<Question> questions = questionService.listByIds(questionIds);
        questions.sort((a, b) -> a.getId().compareTo(b.getId()));
        return Result.success(questions);
    }

    // 3. 获取所有题目
    @GetMapping("/all")
    @Operation(summary = "获取所有题目")
    public Result getAllQuestions() {
        return Result.success(questionMapper.selectList(null));
    }

    // 4. 新增题目
    @PostMapping("/add")
    @Operation(summary = "新增题目", description = "新增题目并建立与科目、书本的关联。")
    public Result addQuestion(@RequestBody QuestionDTO questionDTO) {
        boolean success = questionService.saveQuestionWithRelations(questionDTO);
        return success ? Result.success("添加成功") : Result.error("添加失败");
    }

    // 5. 更新题目
    @PostMapping("/update")
    @Operation(summary = "更新题目")
    public Result updateQuestion(@RequestBody QuestionDTO questionDTO) {
        if (questionDTO.getId() == null) return Result.error("题目ID不能为空");
        boolean success = questionService.updateQuestionWithRelations(questionDTO);
        return success ? Result.success("修改成功") : Result.error("修改失败");
    }

    // 6. 删除题目
    @DeleteMapping("/delete/{id}")
    @Operation(summary = "删除题目", description = "级联删除题目及其科目、书本关联关系。")
    public Result deleteQuestion(@PathVariable Long id) {
        mapQuestionBookService.removeAllQuestionBookRelations(id);
        mapQuestionSubjectService.removeAllQuestionSubjectRelations(id);
        questionMapper.deleteById(id);
        return Result.success("删除成功");
    }

    // 7. 获取题目详情
    @GetMapping("/{id}")
    @Operation(summary = "获取题目详情", description = "包含题目基本信息及关联的第一个书本和科目ID。")
    public Result getQuestionById(@PathVariable Long id) {
        Question question = questionMapper.selectById(id);
        if (question == null) return Result.error("题目不存在");

        List<Integer> bookIds = mapQuestionBookService.getBookIdsByQuestionId(id);
        question.setBookId(bookIds != null && !bookIds.isEmpty() ? bookIds.get(0) : null);

        List<Integer> subjectIds = mapQuestionSubjectService.getSubjectIdsByQuestionId(id);
        question.setSubjectId(subjectIds != null && !subjectIds.isEmpty() ? subjectIds.get(0) : null);

        return Result.success(question);
    }

    // 8. 获取错题本
    @GetMapping("/getErrorBook")
    @Operation(summary = "获取错题本")
    public Result getErrorBook(@RequestParam Integer userId) {
        List<MistakeRecord> list = mistakeRecordService.list(
                new LambdaQueryWrapper<MistakeRecord>().eq(MistakeRecord::getUserId, userId));

        if (list.isEmpty()) return Result.success(new ArrayList<>());

        List<Integer> qIds = list.stream().map(MistakeRecord::getQuestionId).collect(Collectors.toList());
        List<Question> questions = questionService.listByIds(qIds);

        // 将错题时间合并到题目对象中
        questions.forEach(q -> {
            list.stream()
                    .filter(m -> m.getQuestionId().longValue() == q.getId())
                    .findFirst()
                    .ifPresent(m -> q.setMistakeTime(m.getUpdateTime() != null ? m.getUpdateTime() : m.getCreateTime()));
        });

        return Result.success(questions);
    }

    // 9. 分页查询
    @GetMapping("/page")
    @Operation(summary = "分页查询题目")
    public Result findPage(
            @RequestParam Integer pageNum,
            @RequestParam Integer pageSize,
            @RequestParam(required = false) Integer subjectId,
            @RequestParam(required = false) Integer bookId) {
        Page<Question> page = new Page<>(pageNum, pageSize);
        return Result.success(questionService.questionPage(page, subjectId, bookId));
    }

    // 10. 手动保存错题
    @PostMapping("/saveWrong")
    @Operation(summary = "保存错题")
    public Result saveWrong(@RequestBody MistakeRecord mistakeRecord) {
        if (mistakeRecord.getUserId() == null || mistakeRecord.getQuestionId() == null) {
            return Result.error("参数不完整");
        }
        LambdaQueryWrapper<MistakeRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(MistakeRecord::getUserId, mistakeRecord.getUserId())
                .eq(MistakeRecord::getQuestionId, mistakeRecord.getQuestionId());

        if (mistakeRecordService.count(wrapper) == 0) {
            mistakeRecordService.save(mistakeRecord);
        }
        return Result.success("错题已记录");
    }
}