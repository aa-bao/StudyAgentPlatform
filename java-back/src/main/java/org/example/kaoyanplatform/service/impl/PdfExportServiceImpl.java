package org.example.kaoyanplatform.service.impl;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.example.kaoyanplatform.entity.Book;
import org.example.kaoyanplatform.entity.Paper;
import org.example.kaoyanplatform.entity.Question;
import org.example.kaoyanplatform.entity.dto.QuestionExportDTO;
import org.example.kaoyanplatform.service.*;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.xhtmlrenderer.pdf.ITextRenderer;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * PDF导出服务实现类
 */
@Service
@RequiredArgsConstructor
public class PdfExportServiceImpl implements PdfExportService {

    private final QuestionService questionService;
    private final BookService bookService;
    private final PaperService paperService;
    private final MapQuestionBookService mapQuestionBookService;
    private final MapPaperQuestionService mapPaperQuestionService;
    private final SubjectService subjectService;
    private final TemplateEngine templateEngine;

    @Override
    public byte[] exportQuestionsToPdf(QuestionExportDTO exportDTO) throws FileNotFoundException {
        // 1. 根据导出条件获取题目列表
        List<Question> questions = getQuestionsByExportConfig(exportDTO);

        if (questions.isEmpty()) {
            throw new FileNotFoundException("没有找到符合条件的题目");
        }

        // 2. 处理题目内容：将LaTeX公式转换为HTML，同时处理特殊字符
        processQuestionContent(questions);

        // 3. 获取导出元数据
        String title = "考研题目";
        String subtitle = null;
        String bookName = null;
        String subjectName = null;
        boolean isPaperExport = exportDTO.getPaperId() != null;

        if (exportDTO.getBookId() != null) {
            Book book = bookService.getById(exportDTO.getBookId());
            if (book != null) {
                bookName = book.getName();
                title = bookName + " - 题目导出";
            }
        } else if (isPaperExport) {
            Paper paper = paperService.getById(exportDTO.getPaperId());
            if (paper != null) {
                subtitle = paper.getTitle();
                title = "试卷题目导出";
            }
        }

        // 4. 加载题目详情（科目、标签等）
        loadQuestionDetails(questions);

        // 5. 根据导出类型选择模板
        Context context = new Context();
        context.setVariable("title", title);
        context.setVariable("subtitle", subtitle);
        context.setVariable("bookName", bookName);
        context.setVariable("subjectName", subjectName);
        context.setVariable("exportTime", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        context.setVariable("totalQuestions", questions.size());
        context.setVariable("mode", exportDTO.getMode() != null ? exportDTO.getMode() : 1);
        context.setVariable("includeDifficulty", exportDTO.getIncludeDifficulty() != null && exportDTO.getIncludeDifficulty());
        context.setVariable("includeTags", exportDTO.getIncludeTags() != null && exportDTO.getIncludeTags());
        context.setVariable("includeSource", exportDTO.getIncludeSource() != null && exportDTO.getIncludeSource());
        context.setVariable("questions", questions);
        context.setVariable("currentPage", 1);
        context.setVariable("totalPages", 1);

        String templateName;
        if (isPaperExport) {
            // 试卷模板：按试卷结构分组
            Paper paper = paperService.getById(exportDTO.getPaperId());
            List<PaperSectionData> sections = groupQuestionsBySection(questions, paper);
            context.setVariable("sections", sections);
            context.setVariable("allQuestions", questions);
            templateName = "pdf/paper_template";
        } else {
            // 题目模板：直接列出题目
            templateName = "pdf/question_template";
        }

        // 6. 渲染HTML模板
        String htmlContent = templateEngine.process(templateName, context);

        // 7. 转换为PDF
        byte[] pdfBytes = convertHtmlToPdf(htmlContent);

        return pdfBytes;
    }

    /**
     * 处理题目内容：转换LaTeX公式为HTML，同时转义HTML特殊字符
     */
    private void processQuestionContent(List<Question> questions) {
        for (Question question : questions) {
            if (question.getContent() != null) {
                question.setContent(convertLatexToHtml(question.getContent()));
            }
            if (question.getAnswer() != null) {
                question.setAnswer(convertLatexToHtml(question.getAnswer()));
            }
            if (question.getAnalysis() != null) {
                question.setAnalysis(convertLatexToHtml(question.getAnalysis()));
            }
            if (question.getOptions() != null) {
                // 处理新的选项格式：List<Map<String, String>>
                List<Map<String, String>> processedOptions = question.getOptions().stream()
                    .map(opt -> {
                        Map<String, String> newOpt = new java.util.HashMap<>();
                        newOpt.put("label", opt.get("label"));
                        newOpt.put("text", convertLatexToHtml(opt.get("text")));
                        return newOpt;
                    })
                    .collect(Collectors.toList());
                question.setOptions(processedOptions);
            }
        }
    }

    /**
     * 将LaTeX公式转换为HTML span，同时处理特殊字符
     * 使用占位符保护LaTeX公式，先转义HTML，再恢复并转换LaTeX
     */
    private String convertLatexToHtml(String text) {
        if (text == null) return null;

        // 第一步：提取并保护LaTeX公式
        String placeholderPrefix = "___LATEX___";
        List<String> latexFormulas = new ArrayList<>();

        // 提取块级公式 $$...$$
        Pattern blockPattern = Pattern.compile("\\$\\$([^$]+)\\$\\$");
        Matcher blockMatcher = blockPattern.matcher(text);
        int counter = 0;
        StringBuffer sb = new StringBuffer();
        while (blockMatcher.find()) {
            String placeholder = placeholderPrefix + "BLOCK_" + counter + "___";
            latexFormulas.add(blockMatcher.group(1).trim());
            blockMatcher.appendReplacement(sb, Matcher.quoteReplacement(placeholder));
            counter++;
        }
        blockMatcher.appendTail(sb);

        // 提取行内公式 $...$
        String tempText = sb.toString();
        Pattern inlinePattern = Pattern.compile("\\$([^$\\n]+)\\$");
        Matcher inlineMatcher = inlinePattern.matcher(tempText);
        sb = new StringBuffer();
        counter = 0;
        while (inlineMatcher.find()) {
            String placeholder = placeholderPrefix + "INLINE_" + counter + "___";
            latexFormulas.add(inlineMatcher.group(1).trim());
            inlineMatcher.appendReplacement(sb, Matcher.quoteReplacement(placeholder));
            counter++;
        }
        inlineMatcher.appendTail(sb);

        // 第二步：转义HTML特殊字符
        String escaped = sb.toString()
            .replace("&", "&amp;")
            .replace("<", "&lt;")
            .replace(">", "&gt;")
            .replace("\"", "&quot;")
            .replace("'", "&#x27;");

        // 第三步：将LaTeX公式转换为HTML span
        for (int i = 0; i < latexFormulas.size(); i++) {
            String formula = latexFormulas.get(i);
            boolean isBlock = i < latexFormulas.size() && text.contains("$$" + formula + "$$") &&
                !text.substring(0, text.indexOf("$$" + formula + "$$")).contains("$" + formula + "$");

            // 简化处理：直接用原始公式替换占位符
            // KaTeX会在前端渲染，所以这里只做占位符处理
            String placeholder = placeholderPrefix + (i < 10 ? "0" + i : i) + "___";
            String latexHtml = "$" + formula + "$";
            escaped = escaped.replace(placeholder, latexHtml);
        }

        return escaped;
    }

    /**
     * 将题目按试卷结构分组
     */
    private List<PaperSectionData> groupQuestionsBySection(List<Question> questions, Paper paper) {
        List<PaperSectionData> sections = new ArrayList<>();

        if (paper != null && paper.getStructureJson() != null && !paper.getStructureJson().isEmpty()) {
            for (Paper.PaperSection sectionDef : paper.getStructureJson()) {
                PaperSectionData section = new PaperSectionData();
                section.setName(sectionDef.getName() != null ? sectionDef.getName() : "第" + (sections.size() + 1) + "部分");

                List<Question> sectionQuestions = new ArrayList<>();
                for (Question q : questions) {
                    if (q.getSortOrder() != null &&
                        q.getSortOrder() >= sectionDef.getStart() &&
                        q.getSortOrder() <= sectionDef.getEnd()) {
                        sectionQuestions.add(q);
                    }
                }
                sectionQuestions.sort(Comparator.comparing(Question::getSortOrder));
                section.setQuestions(sectionQuestions);
                sections.add(section);
            }
        } else {
            PaperSectionData defaultSection = new PaperSectionData();
            defaultSection.setName("第一部分");
            List<Question> sortedQuestions = new ArrayList<>(questions);
            sortedQuestions.sort(Comparator.comparing(Question::getSortOrder));
            defaultSection.setQuestions(sortedQuestions);
            sections.add(defaultSection);
        }

        return sections;
    }

    @Data
    public static class PaperSectionData {
        private String name;
        private List<Question> questions;
    }

    /**
     * 根据导出配置获取题目列表
     */
    private List<Question> getQuestionsByExportConfig(QuestionExportDTO exportDTO) {
        List<Question> questions = new ArrayList<>();

        if (exportDTO.getQuestionIds() != null && !exportDTO.getQuestionIds().isEmpty()) {
            questions.addAll(questionService.listByIds(exportDTO.getQuestionIds()));
        } else if (exportDTO.getPaperId() != null) {
            List<Question> paperQuestions = mapPaperQuestionService.getQuestionsWithDetails(exportDTO.getPaperId());
            questions.addAll(paperQuestions);
        } else if (exportDTO.getBookId() != null) {
            List<Question> bookQuestions = questionService.getQuestionsByBookId(exportDTO.getBookId());
            questions.addAll(bookQuestions);
        } else if (exportDTO.getSubjectId() != null) {
            List<Question> subjectQuestions = questionService.getQuestionsBySubjectIds(Collections.singletonList(exportDTO.getSubjectId()));
            questions.addAll(subjectQuestions);
        }

        return questions;
    }

    /**
     * 加载题目详情
     */
    private void loadQuestionDetails(List<Question> questions) {
        for (Question question : questions) {
            if (question.getSubjectIds() == null || question.getSubjectIds().isEmpty()) {
                question.setSubjectNames(new ArrayList<>());
            }
        }
    }

    /**
     * 将HTML转换为PDF
     */
    private byte[] convertHtmlToPdf(String htmlContent) {
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            ITextRenderer renderer = new ITextRenderer();

            String os = System.getProperty("os.name").toLowerCase();

            if (os.contains("win")) {
                renderer.getFontResolver().addFont(
                    "C:\\Windows\\Fonts\\simsun.ttc",
                    "Identity-H",
                    false
                );
            } else if (os.contains("mac")) {
                renderer.getFontResolver().addFont(
                    "/System/Library/Fonts/PingFang.ttc",
                    "Identity-H",
                    false
                );
            } else {
                File fontFile = findSystemFont();
                if (fontFile != null && fontFile.exists()) {
                    renderer.getFontResolver().addFont(
                        fontFile.getAbsolutePath(),
                        "Identity-H",
                        false
                    );
                }
            }

            renderer.setDocumentFromString(htmlContent);
            renderer.layout();
            renderer.createPDF(outputStream);

            return outputStream.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("PDF生成失败: " + e.getMessage(), e);
        }
    }

    /**
     * 查找系统中的中文字体文件
     */
    private File findSystemFont() {
        String[] fontPaths = {
            "/usr/share/fonts/truetype/droid/DroidSansFallbackFull.ttf",
            "/usr/share/fonts/truetype/wqy/wqy-microhei.ttc",
            "/usr/share/fonts/truetype/wqy/wqy-zenhei.ttc",
            "/usr/share/fonts/opentype/noto/NotoSansCJK-Regular.ttc",
            "/usr/share/fonts/truetype/dejavu/DejaVuSans.ttf"
        };

        for (String path : fontPaths) {
            File file = new File(path);
            if (file.exists()) {
                return file;
            }
        }
        return null;
    }
}
