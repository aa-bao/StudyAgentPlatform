package org.example.kaoyanplatform.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.example.kaoyanplatform.common.Result;
import org.example.kaoyanplatform.entity.Question;
import org.example.kaoyanplatform.entity.MistakeRecord;
import org.example.kaoyanplatform.mapper.ExamRecordMapper;
import org.example.kaoyanplatform.mapper.QuestionMapper;
import org.example.kaoyanplatform.service.QuestionService;
import org.example.kaoyanplatform.service.MistakeRecordService;
import org.example.kaoyanplatform.service.SubjectService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/question")
public class QuestionController {

    @Autowired
    private QuestionMapper questionMapper;

    @Autowired
    private ExamRecordMapper examRecordMapper;

    @Autowired
    private QuestionService questionService;

    @Autowired
    private MistakeRecordService mistakeRecordService;

    @Autowired
    private SubjectService subjectService;

    @GetMapping("/list-by-knowledge-point")
    public Result getQuestionsByKnowledgePoint(@RequestParam Integer subjectId) {
        List<Integer> subjectIds = subjectService.getDescendantIds(subjectId);

        List<Question> questions = questionService.getQuestionsBySubjectIds(subjectIds);

        return Result.success(questions);
    }

    @GetMapping("/list-by-subject")
    public Result getQuestionsBySubject(@RequestParam(required = false) Integer subjectId,
            @RequestParam(required = false) Integer bookId) {
        LambdaQueryWrapper<Question> queryWrapper = new LambdaQueryWrapper<>();

        // 如果有 subjectId
        if (subjectId != null) {
            queryWrapper.eq(Question::getSubjectId, subjectId);
        }

        // 如果有 bookId (按习题册查询)
        if (bookId != null) {
            queryWrapper.eq(Question::getBookId, bookId);
        }

        queryWrapper.orderByAsc(Question::getId); // 按 ID 顺序排序

        List<Question> list = questionMapper.selectList(queryWrapper);
        return Result.success(list);
    }

    // 在 QuestionController 类中添加
    @GetMapping("/all")
    public Result getAllQuestions() {
        return Result.success(questionMapper.selectList(null));
    }

    @PostMapping("/update")
    public Result updateQuestion(@RequestBody Question question) {
        questionMapper.updateById(question);
        return Result.success("修改成功");
    }

    @DeleteMapping("/delete/{id}")
    public Result deleteQuestion(@PathVariable Integer id) {
        questionMapper.deleteById(id);
        return Result.success("删除成功");
    }

    @PostMapping("/add")
    public Result addQuestion(@RequestBody Question question) {
        questionMapper.insert(question);
        return Result.success("添加成功");
    }

    @GetMapping("/getErrorBook")
    public Result getErrorBook(@RequestParam Integer userId) {
        List<MistakeRecord> list = mistakeRecordService
                .list(new LambdaQueryWrapper<MistakeRecord>().eq(MistakeRecord::getUserId, userId));

        if (list.isEmpty())
            return Result.success(new ArrayList<>());

        List<Integer> qIds = list.stream().map(MistakeRecord::getQuestionId).collect(Collectors.toList());
        List<Question> questions = questionService.listByIds(qIds);

        // 将 updateTime 注入到 Question 对象中返回
        for (Question q : questions) {
            for (MistakeRecord wb : list) {
                // 解决 Integer 和 Long 的比较问题
                if (wb.getQuestionId().longValue() == q.getId().longValue()) {
                    q.setCreateTime(wb.getUpdateTime() != null ? wb.getUpdateTime() : wb.getCreateTime());
                    break;
                }
            }
        }

        return Result.success(questions);
    }

    // 分页查询
    @GetMapping("/page")
    public Result findPage(@RequestParam Integer pageNum,
            @RequestParam Integer pageSize,
            @RequestParam(required = false) Integer subjectId) {
        // 1. 创建分页对象
        Page<Question> page = new Page<>(pageNum, pageSize);

        // 2. 构建查询条件（可选，比如按科目过滤）
        LambdaQueryWrapper<Question> queryWrapper = new LambdaQueryWrapper<>();
        if (subjectId != null) {
            queryWrapper.eq(Question::getSubjectId, subjectId);
        }
        queryWrapper.orderByDesc(Question::getId); // 按ID倒序，新题在前

        // 3. 执行查询
        return Result.success(questionService.page(page, queryWrapper));
    }

    @PostMapping("/saveWrong")
    public Result saveWrong(@RequestBody MistakeRecord mistakeRecord) {
        // 1. 检查是否已存在，避免重复
        LambdaQueryWrapper<MistakeRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(MistakeRecord::getUserId, mistakeRecord.getUserId())
                .eq(MistakeRecord::getQuestionId, mistakeRecord.getQuestionId());

        if (mistakeRecordService.count(wrapper) == 0) {
            mistakeRecordService.save(mistakeRecord); // 只有不存在才保存
        }
        return Result.success();
    }

}