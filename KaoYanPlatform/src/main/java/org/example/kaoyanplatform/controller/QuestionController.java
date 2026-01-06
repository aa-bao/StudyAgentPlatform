package org.example.kaoyanplatform.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.example.kaoyanplatform.common.Result;
import org.example.kaoyanplatform.entity.Question;
import org.example.kaoyanplatform.entity.MistakeRecord;
import org.example.kaoyanplatform.entity.dto.QuestionDTO;
import org.example.kaoyanplatform.mapper.ExamRecordMapper;
import org.example.kaoyanplatform.mapper.QuestionMapper;
import org.example.kaoyanplatform.service.QuestionService;
import org.example.kaoyanplatform.service.MistakeRecordService;
import org.example.kaoyanplatform.service.SubjectService;
import org.example.kaoyanplatform.service.MapQuestionSubjectService;
import org.example.kaoyanplatform.service.MapQuestionBookService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 题目Controller
 * 使用映射表（map_question_subject、map_question_book）管理题目与科目、书本的关系
 */
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

    @Autowired
    private MapQuestionSubjectService mapQuestionSubjectService;

    @Autowired
    private MapQuestionBookService mapQuestionBookService;

    @GetMapping("/list-by-knowledge-point")
    public Result getQuestionsByKnowledgePoint(@RequestParam Integer subjectId) {
        List<Integer> subjectIds = subjectService.getDescendantIds(subjectId);
        List<Question> questions = questionService.getQuestionsBySubjectIds(subjectIds);
        return Result.success(questions);
    }

    @GetMapping("/list-by-subject")
    public Result getQuestionsBySubject(@RequestParam(required = false) Integer subjectId,
            @RequestParam(required = false) Integer bookId) {
        List<Long> questionIds = null;

        if (subjectId != null) {
            questionIds = mapQuestionSubjectService.getQuestionIdsBySubjectId(subjectId);
        } else if (bookId != null) {
            questionIds = mapQuestionBookService.getQuestionIdsByBookId(bookId);
        }

        if (questionIds == null || questionIds.isEmpty()) {
            if (subjectId != null || bookId != null) {
                return Result.success(new ArrayList<>());
            }
            return Result.success(questionMapper.selectList(null));
        }

        List<Question> questions = questionService.listByIds(questionIds);
        questions.sort((a, b) -> a.getId().compareTo(b.getId()));
        return Result.success(questions);
    }

    @GetMapping("/all")
    public Result getAllQuestions() {
        return Result.success(questionMapper.selectList(null));
    }

    /**
     * 新增题目（包含关联关系）
     */
    @PostMapping("/add")
    public Result addQuestion(@RequestBody QuestionDTO questionDTO) {
        boolean success = questionService.saveQuestionWithRelations(questionDTO);
        return success ? Result.success("添加成功") : Result.error("添加失败");
    }

    /**
     * 更新题目（包含关联关系）
     */
    @PostMapping("/update")
    public Result updateQuestion(@RequestBody QuestionDTO questionDTO) {
        boolean success = questionService.updateQuestionWithRelations(questionDTO);
        return success ? Result.success("修改成功") : Result.error("修改失败");
    }

    /**
     * 删除题目（同时删除关联关系）
     */
    @DeleteMapping("/delete/{id}")
    public Result deleteQuestion(@PathVariable Long id) {
        // 删除题目-书本关联
        mapQuestionBookService.removeAllQuestionBookRelations(id);
        // 删除题目-科目关联
        mapQuestionSubjectService.removeAllQuestionSubjectRelations(id);
        // 删除题目
        questionMapper.deleteById(id);
        return Result.success("删除成功");
    }

    /**
     * 根据ID获取题目详情（包含关联关系）
     */
    @GetMapping("/{id}")
    public Result getQuestionById(@PathVariable Long id) {
        Question question = questionMapper.selectById(id);
        if (question == null) {
            return Result.error("题目不存在");
        }

        // 获取关联的书本ID列表
        List<Integer> bookIds = mapQuestionBookService.getBookIdsByQuestionId(id);
        question.setBookId(bookIds != null && !bookIds.isEmpty() ? bookIds.get(0) : null);

        // 获取关联的科目ID列表
        List<Integer> subjectIds = mapQuestionSubjectService.getSubjectIdsByQuestionId(id);
        question.setSubjectId(subjectIds != null && !subjectIds.isEmpty() ? subjectIds.get(0) : null);

        return Result.success(question);
    }

    @GetMapping("/getErrorBook")
    public Result getErrorBook(@RequestParam Integer userId) {
        List<MistakeRecord> list = mistakeRecordService
                .list(new LambdaQueryWrapper<MistakeRecord>().eq(MistakeRecord::getUserId, userId));

        if (list.isEmpty())
            return Result.success(new ArrayList<>());

        List<Integer> qIds = list.stream().map(MistakeRecord::getQuestionId).collect(Collectors.toList());
        List<Question> questions = questionService.listByIds(qIds);

        for (Question q : questions) {
            for (MistakeRecord wb : list) {
                if (wb.getQuestionId().longValue() == q.getId().longValue()) {
                    q.setMistakeTime(wb.getUpdateTime() != null ? wb.getUpdateTime() : wb.getCreateTime());
                    break;
                }
            }
        }

        return Result.success(questions);
    }

    /**
     * 分页查询题目（支持按科目/书本筛选）
     */
    @GetMapping("/page")
    public Result findPage(@RequestParam Integer pageNum,
            @RequestParam Integer pageSize,
            @RequestParam(required = false) Integer subjectId,
            @RequestParam(required = false) Integer bookId) {
        Page<Question> page = new Page<>(pageNum, pageSize);
        Page<Question> result = questionService.questionPage(page, subjectId, bookId);
        return Result.success(result);
    }

    @PostMapping("/saveWrong")
    public Result saveWrong(@RequestBody MistakeRecord mistakeRecord) {
        LambdaQueryWrapper<MistakeRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(MistakeRecord::getUserId, mistakeRecord.getUserId())
                .eq(MistakeRecord::getQuestionId, mistakeRecord.getQuestionId());

        if (mistakeRecordService.count(wrapper) == 0) {
            mistakeRecordService.save(mistakeRecord);
        }
        return Result.success();
    }
}