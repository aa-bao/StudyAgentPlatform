package org.example.kaoyanplatform.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.example.kaoyanplatform.common.Result;
import org.example.kaoyanplatform.entity.Question;
import org.example.kaoyanplatform.entity.MistakeRecord;
import org.example.kaoyanplatform.entity.Book;
import org.example.kaoyanplatform.entity.Subject;
import org.example.kaoyanplatform.entity.dto.QuestionDTO;
import org.example.kaoyanplatform.entity.dto.QuestionImportDTO;
import org.example.kaoyanplatform.entity.dto.QuestionExportDTO;
import org.example.kaoyanplatform.mapper.QuestionMapper;
import org.example.kaoyanplatform.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.FileNotFoundException;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;
import org.springframework.web.multipart.MultipartFile;

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
    private BookService bookService;

    @Autowired
    private MapQuestionSubjectService mapQuestionSubjectService;

    @Autowired
    private MapQuestionBookService mapQuestionBookService;

    @Autowired
    private PdfExportService pdfExportService;

    @Autowired
    private MapPaperQuestionService mapPaperQuestionService;

    @Autowired
    private GLMService glmService;

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
    @Operation(summary = "获取题目详情", description = "包含题目基本信息及关联的所有书本和科目ID列表。")
    public Result getQuestionById(@PathVariable Long id) {
        Question question = questionMapper.selectById(id);
        if (question == null) return Result.error("题目不存在");

        List<Integer> bookIds = mapQuestionBookService.getBookIdsByQuestionId(id);
        question.setBookIds(bookIds != null ? bookIds : Collections.emptyList());
        
        // 填充所有书本名称
        if (bookIds != null && !bookIds.isEmpty()) {
            List<String> bookNames = new ArrayList<>();
            for (Integer bookId : bookIds) {
                Book book = bookService.getById(bookId);
                if (book != null) {
                    bookNames.add(book.getName());
                }
            }
            question.setBookNames(bookNames);
            question.setBookName(bookNames.isEmpty() ? null : bookNames.get(0));
        } else {
            question.setBookNames(Collections.emptyList());
            question.setBookName(null);
        }

        List<Integer> subjectIds = mapQuestionSubjectService.getSubjectIdsByQuestionId(id);
        question.setSubjectIds(subjectIds != null ? subjectIds : Collections.emptyList());
        
        // 填充所有科目名称
        if (subjectIds != null && !subjectIds.isEmpty()) {
            List<String> subjectNames = new ArrayList<>();
            for (Integer subjectId : subjectIds) {
                Subject subject = subjectService.getById(subjectId);
                if (subject != null) {
                    subjectNames.add(subject.getName());
                }
            }
            question.setSubjectNames(subjectNames);
            question.setSubjectName(subjectNames.isEmpty() ? null : subjectNames.get(0));
        } else {
            question.setSubjectNames(Collections.emptyList());
            question.setSubjectName(null);
        }

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
            @RequestParam(required = false) String subjectIds,
            @RequestParam(required = false) Integer bookId) {
        Page<Question> page = new Page<>(pageNum, pageSize);
        List<Integer> subjectIdList = null;
        if (subjectIds != null && !subjectIds.isEmpty()) {
            subjectIdList = Arrays.stream(subjectIds.split(","))
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .map(Integer::parseInt)
                    .collect(Collectors.toList());
        }
        return Result.success(questionService.questionPage(page, subjectIdList, bookId));
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

    // 11. JSON批量导入题目
    @PostMapping("/import")
    @Operation(summary = "JSON批量导入题目", description = "接收JSON格式的题目数据，批量导入题库")
    public Result importQuestions(@RequestBody QuestionImportDTO importDTO) {
        if (importDTO.getQuestions() == null || importDTO.getQuestions().isEmpty()) {
            return Result.error("题目列表不能为空");
        }

        if (importDTO.getSubjectIds() == null || importDTO.getSubjectIds().isEmpty()) {
            return Result.error("科目ID不能为空");
        }

        // 验证科目是否存在
        for (Integer subjectId : importDTO.getSubjectIds()) {
            if (subjectService.getById(subjectId) == null) {
                return Result.error("科目ID: " + subjectId + " 不存在");
            }
        }

        try {
            Integer bookId = importDTO.getBookId();

            // 如果新建习题册
            if (bookId == null && importDTO.getNewBookName() != null && !importDTO.getNewBookName().trim().isEmpty()) {
                Book newBook = new Book();
                newBook.setName(importDTO.getNewBookName().trim());
                newBook.setDescription("通过JSON导入自动创建");
                bookService.save(newBook);
                bookId = newBook.getId();
            } else if (bookId != null) {
                // 验证书本是否存在
                if (bookService.getById(bookId) == null) {
                    return Result.error("习题册不存在");
                }
            }

            // 最终使用的习题册ID
            Integer finalBookId = bookId;

            // 去重检查，默认启用
            boolean checkDuplicate = importDTO.getCheckDuplicate() != null && importDTO.getCheckDuplicate();

            // 批量保存题目
            int successCount = 0;
            int duplicateCount = 0;
            int failCount = 0;
            List<String> errorMessages = new ArrayList<>();

            for (QuestionImportDTO.QuestionImportItem item : importDTO.getQuestions()) {
                try {
                    // 去重检查
                    if (checkDuplicate && questionService.isQuestionExist(item.getContent())) {
                        duplicateCount++;
                        continue;
                    }

                    // 构造 QuestionDTO
                    QuestionDTO questionDTO = new QuestionDTO();
                    questionDTO.setType(item.getType());
                    questionDTO.setContent(item.getContent());

                    // 处理选项：支持旧格式（字符串数组）和新格式（对象数组）
                    if (item.getOptions() != null) {
                        // 如果传入的是字符串数组，转换为对象数组格式
                        if (item.getOptions() instanceof List) {
                            List<?> optionsList = (List<?>) item.getOptions();
                            if (!optionsList.isEmpty() && optionsList.get(0) instanceof String) {
                                // 旧格式：["选项1", "选项2", "选项3", "选项4"]
                                // 转换为新格式：[{"label": "A", "text": "选项1"}, ...]
                                List<Map<String, String>> formattedOptions = new ArrayList<>();
                                String[] labels = {"A", "B", "C", "D", "E", "F"};
                                for (int i = 0; i < optionsList.size() && i < labels.length; i++) {
                                    Map<String, String> option = new java.util.HashMap<>();
                                    option.put("label", labels[i]);
                                    option.put("text", (String) optionsList.get(i));
                                    formattedOptions.add(option);
                                }
                                questionDTO.setOptions(formattedOptions);
                            } else if (optionsList.get(0) instanceof Map) {
                                // 新格式：已经是对象数组，直接使用
                                questionDTO.setOptions((List<Map<String, String>>) (List<?>) optionsList);
                            }
                        }
                    }

                    questionDTO.setAnswer(item.getAnswer());
                    questionDTO.setAnalysis(item.getAnalysis());
                    questionDTO.setTags(item.getTags());
                    questionDTO.setSource(item.getSource());
                    questionDTO.setSubjectIds(importDTO.getSubjectIds());

                    // 只有在有习题册ID时才设置
                    if (finalBookId != null) {
                        questionDTO.setBookIds(Collections.singletonList(finalBookId));
                    }

                    // 保存题目
                    boolean success = questionService.saveQuestionWithRelations(questionDTO);
                    if (success) {
                        successCount++;
                    } else {
                        failCount++;
                        String content = item.getContent() != null
                            ? item.getContent().substring(0, Math.min(50, item.getContent().length()))
                            : "(无内容)";
                        errorMessages.add("题目保存失败: " + content);
                    }
                } catch (Exception e) {
                    failCount++;
                    errorMessages.add("题目导入失败: " + e.getMessage());
                }
            }

            String resultMessage = String.format("导入完成！成功: %d, 跳过重复: %d, 失败: %d", successCount, duplicateCount, failCount);
            if (!errorMessages.isEmpty()) {
                resultMessage += "\n错误信息:\n" + String.join("\n", errorMessages.subList(0, Math.min(5, errorMessages.size())));
                if (errorMessages.size() > 5) {
                    resultMessage += "\n...还有 " + (errorMessages.size() - 5) + " 条错误";
                }
            }

            return Result.success(resultMessage);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("导入失败: " + e.getMessage());
        }
    }

    // 12. 预览要导出的题目
    @PostMapping("/export/preview")
    @Operation(summary = "预览导出题目", description = "根据导出条件预览将要导出的题目列表")
    public Result previewExportQuestions(@RequestBody QuestionExportDTO exportDTO) {
        try {
            List<Question> questions = getQuestionsByExportConfig(exportDTO);

            // 加载题目详情（科目、书本等）
            for (Question question : questions) {
                List<Integer> bookIds = mapQuestionBookService.getBookIdsByQuestionId(question.getId());
                question.setBookIds(bookIds != null ? bookIds : Collections.emptyList());

                if (bookIds != null && !bookIds.isEmpty()) {
                    List<String> bookNames = new ArrayList<>();
                    for (Integer bookId : bookIds) {
                        Book book = bookService.getById(bookId);
                        if (book != null) {
                            bookNames.add(book.getName());
                        }
                    }
                    question.setBookNames(bookNames);
                }

                List<Integer> subjectIds = mapQuestionSubjectService.getSubjectIdsByQuestionId(question.getId());
                question.setSubjectIds(subjectIds != null ? subjectIds : Collections.emptyList());

                if (subjectIds != null && !subjectIds.isEmpty()) {
                    List<String> subjectNames = new ArrayList<>();
                    for (Integer subjectId : subjectIds) {
                        Subject subject = subjectService.getById(subjectId);
                        if (subject != null) {
                            subjectNames.add(subject.getName());
                        }
                    }
                    question.setSubjectNames(subjectNames);
                }
            }

            return Result.success(questions);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("预览失败: " + e.getMessage());
        }
    }

    // 13. 导出题目为PDF
    @PostMapping("/export/pdf")
    @Operation(summary = "导出题目为PDF", description = "根据导出条件生成PDF文件")
    public ResponseEntity<byte[]> exportQuestionsToPdf(@RequestBody QuestionExportDTO exportDTO) {
        try {
            byte[] pdfBytes = pdfExportService.exportQuestionsToPdf(exportDTO);

            String filename = "题目导出_" + System.currentTimeMillis() + ".pdf";

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            headers.setContentDispositionFormData("attachment", filename);
            headers.setContentLength(pdfBytes.length);

            return new ResponseEntity<>(pdfBytes, headers, HttpStatus.OK);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            String errorMsg = "未找到符合条件的题目";
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(errorMsg.getBytes(StandardCharsets.UTF_8));
        } catch (Exception e) {
            e.printStackTrace();
            String errorMsg = "PDF生成失败: " + e.getMessage();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(errorMsg.getBytes(StandardCharsets.UTF_8));
        }
    }

    /**
     * 根据导出配置获取题目列表
     */
    private List<Question> getQuestionsByExportConfig(QuestionExportDTO exportDTO) {
        List<Question> questions = new ArrayList<>();

        // 优先级1: 自定义题目ID列表
        if (exportDTO.getQuestionIds() != null && !exportDTO.getQuestionIds().isEmpty()) {
            questions.addAll(questionService.listByIds(exportDTO.getQuestionIds()));
        }
        // 优先级2: 按试卷导出
        else if (exportDTO.getPaperId() != null) {
            List<Question> paperQuestions = mapPaperQuestionService.getQuestionsWithDetails(exportDTO.getPaperId());
            questions.addAll(paperQuestions);
        }
        // 优先级3: 按习题册导出
        else if (exportDTO.getBookId() != null) {
            List<Question> bookQuestions = questionService.getQuestionsByBookId(exportDTO.getBookId());
            questions.addAll(bookQuestions);
        }
        // 优先级4: 按科目导出
        else if (exportDTO.getSubjectId() != null) {
            List<Question> subjectQuestions = questionService.getQuestionsBySubjectIds(Collections.singletonList(exportDTO.getSubjectId()));
            questions.addAll(subjectQuestions);
        }

        return questions;
    }

    // 13. AI 图片识别题目
    @PostMapping("/recognize")
    @Operation(summary = "AI图片识别题目", description = "使用智谱GLM-4.6V-Flash API识别图片中的题目内容")
    public Result recognizeQuestion(@RequestParam("file") MultipartFile file) {
        try {
            QuestionDTO questionDTO = glmService.recognizeQuestionFromImage(file);
            return Result.success(questionDTO);
        } catch (IllegalArgumentException e) {
            return Result.error(e.getMessage());
        } catch (RuntimeException e) {
            return Result.error("AI识别失败: " + e.getMessage());
        }
    }

}