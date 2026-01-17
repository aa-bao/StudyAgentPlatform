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
import org.example.kaoyanplatform.entity.dto.LLMQuestionOutputDTO;
import org.example.kaoyanplatform.entity.dto.QuestionImportDTO;
import org.example.kaoyanplatform.entity.dto.QuestionExportDTO;
import org.example.kaoyanplatform.mapper.QuestionMapper;
import org.example.kaoyanplatform.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
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
    private BookService bookService;

    @Autowired
    private MapQuestionSubjectService mapQuestionSubjectService;

    @Autowired
    private MapQuestionBookService mapQuestionBookService;

    @Autowired
    private PdfExportService pdfExportService;

    @Autowired
    private MarkdownParseService markdownParseService;

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

    // 11. LLM进行识别题目
    @PostMapping("/recognize")
    @Operation(summary = "AI 图片识别题目", description = "利用LLM识别图片中的题目和 LaTeX 公式，返回结构化 JSON")
    public Result<LLMQuestionOutputDTO> recognize(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return Result.error("文件不能为空");
        }

        try {
            // 调用 LLM 服务，返回结构化 DTO
            LLMQuestionOutputDTO result = questionService.recognizeImageToText(file);
            return Result.success(result);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("识别出错：" + e.getMessage());
        }
    }

    // 12. JSON批量导入题目
    @PostMapping("/import")
    @Operation(summary = "JSON批量导入题目", description = "接收JSON格式的题目数据，批量导入题库")
    public Result importQuestions(@RequestBody QuestionImportDTO importDTO) {
        if (importDTO.getQuestions() == null || importDTO.getQuestions().isEmpty()) {
            return Result.error("题目列表不能为空");
        }

        if (importDTO.getBookId() == null) {
            return Result.error("习题册ID不能为空");
        }

        if (importDTO.getSubjectIds() == null || importDTO.getSubjectIds().isEmpty()) {
            return Result.error("科目ID不能为空");
        }

        try {
            // 验证书本和科目是否存在
            if (bookService.getById(importDTO.getBookId()) == null) {
                return Result.error("习题册不存在");
            }

            for (Integer subjectId : importDTO.getSubjectIds()) {
                if (subjectService.getById(subjectId) == null) {
                    return Result.error("科目ID: " + subjectId + " 不存在");
                }
            }

            // 批量保存题目
            int successCount = 0;
            int failCount = 0;
            List<String> errorMessages = new ArrayList<>();

            for (QuestionImportDTO.QuestionImportItem item : importDTO.getQuestions()) {
                try {
                    // 构造 QuestionDTO
                    QuestionDTO questionDTO = new QuestionDTO();
                    questionDTO.setType(item.getType());
                    questionDTO.setContent(item.getContent());
                    questionDTO.setOptions(item.getOptions());
                    questionDTO.setAnswer(item.getAnswer());
                    questionDTO.setAnalysis(item.getAnalysis());
                    questionDTO.setTags(item.getTags());
                    questionDTO.setSource(item.getSource());
                    questionDTO.setBookIds(Collections.singletonList(importDTO.getBookId()));
                    questionDTO.setSubjectIds(importDTO.getSubjectIds());

                    // 保存题目
                    boolean success = questionService.saveQuestionWithRelations(questionDTO);
                    if (success) {
                        successCount++;
                    } else {
                        failCount++;
                        errorMessages.add("题目保存失败: " + item.getContent().substring(0, Math.min(50, item.getContent().length())));
                    }
                } catch (Exception e) {
                    failCount++;
                    errorMessages.add("题目导入失败: " + e.getMessage());
                }
            }

            String resultMessage = String.format("导入完成！成功: %d, 失败: %d", successCount, failCount);
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

    // 13. 压缩包批量导入题目（Zip包含MD和图片）
    @PostMapping("/import/zip")
    @Operation(summary = "压缩包批量导入题目", description = "接收包含 .md 和 images/ 的 Zip 包，自动解析并导入")
    public Result importQuestionsFromZip(
            @RequestParam("zipFile") MultipartFile zipFile,
            @RequestParam("bookId") Integer bookId,
            @RequestParam("subjectIds") String subjectIds,
            @RequestParam(value = "source", required = false) String source,
            @RequestParam(value = "checkDuplicate", defaultValue = "true") Boolean checkDuplicate) {

        if (zipFile.isEmpty()) {
            return Result.error("压缩包文件不能为空");
        }

        if (bookId == null) {
            return Result.error("习题册ID不能为空");
        }

        if (subjectIds == null || subjectIds.trim().isEmpty()) {
            return Result.error("科目ID不能为空");
        }

        try {
            // 验证书本和科目是否存在
            if (bookService.getById(bookId) == null) {
                return Result.error("习题册不存在");
            }

            List<Integer> subjectIdList = Arrays.stream(subjectIds.split(","))
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .map(Integer::parseInt)
                    .collect(Collectors.toList());

            for (Integer sid : subjectIdList) {
                if (subjectService.getById(sid) == null) {
                    return Result.error("科目ID: " + sid + " 不存在");
                }
            }

            // 1. 解压Zip到临时目录
            File tempDir = markdownParseService.unzipToTempDir(zipFile);

            // 2. 查找MD文件
            File mdFile = findMarkdownFile(tempDir);
            if (mdFile == null) {
                // 清理临时目录
                deleteDirectory(tempDir);
                return Result.error("未找到Markdown文件，请确保压缩包中包含 .md 文件");
            }

            // 3. 查找images目录
            File imageDir = new File(tempDir, "images");
            if (!imageDir.exists()) {
                imageDir = tempDir; // 如果没有images目录，使用根目录
            }

            // 4. 解析Markdown
            QuestionImportDTO importDTO = markdownParseService.parseMarkdownToQuestions(
                    mdFile, imageDir, bookId, subjectIdList, source);

            // 5. 批量导入（含去重检查）
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
                    questionDTO.setOptions(item.getOptions());
                    questionDTO.setAnswer(item.getAnswer());
                    questionDTO.setAnalysis(item.getAnalysis());
                    questionDTO.setTags(item.getTags());
                    questionDTO.setSource(item.getSource());
                    questionDTO.setBookIds(Collections.singletonList(bookId));
                    questionDTO.setSubjectIds(subjectIdList);

                    // 保存题目
                    boolean success = questionService.saveQuestionWithRelations(questionDTO);
                    if (success) {
                        successCount++;
                    } else {
                        failCount++;
                        errorMessages.add("题目保存失败");
                    }
                } catch (Exception e) {
                    failCount++;
                    errorMessages.add("题目导入失败: " + e.getMessage());
                }
            }

            // 6. 清理临时目录
            deleteDirectory(tempDir);

            String resultMessage = String.format(
                    "导入完成！成功: %d, 跳过重复: %d, 失败: %d",
                    successCount, duplicateCount, failCount);

            if (!errorMessages.isEmpty()) {
                resultMessage += "\n错误信息:\n" + String.join("\n",
                        errorMessages.subList(0, Math.min(5, errorMessages.size())));
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

    /**
     * 在目录中查找Markdown文件
     */
    private File findMarkdownFile(File directory) {
        if (directory == null || !directory.exists()) {
            return null;
        }

        File[] files = directory.listFiles();
        if (files == null) {
            return null;
        }

        for (File file : files) {
            if (file.isFile() && file.getName().toLowerCase().endsWith(".md")) {
                return file;
            }
            if (file.isDirectory()) {
                File found = findMarkdownFile(file);
                if (found != null) {
                    return found;
                }
            }
        }

        return null;
    }

    /**
     * 递归删除目录
     */
    private void deleteDirectory(File directory) {
        if (directory == null || !directory.exists()) {
            return;
        }

        if (directory.isDirectory()) {
            File[] files = directory.listFiles();
            if (files != null) {
                for (File file : files) {
                    deleteDirectory(file);
                }
            }
        }

        directory.delete();
    }

    // 14. 导出题目为PDF
    @PostMapping("/export/pdf")
    @Operation(summary = "导出题目为PDF", description = "根据条件导出题目为PDF文件")
    public Result exportToPDF(@RequestBody QuestionExportDTO exportDTO) {
        try {
            // 默认不包含答案（安全考虑）
            if (exportDTO.getIncludeAnswers() == null) {
                exportDTO.setIncludeAnswers(false);
            }

            // 生成PDF
            String pdfPath = pdfExportService.generateQuestionsPDF(
                    exportDTO.getSubjectId(),
                    exportDTO.getBookId(),
                    exportDTO.getQuestionIds(),
                    exportDTO.getIncludeAnswers()
            );

            return Result.success(pdfPath);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("PDF生成失败: " + e.getMessage());
        }
    }
}