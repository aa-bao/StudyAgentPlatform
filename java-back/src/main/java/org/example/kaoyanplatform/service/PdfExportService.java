package org.example.kaoyanplatform.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.example.kaoyanplatform.entity.Question;
import org.example.kaoyanplatform.entity.Subject;
import org.example.kaoyanplatform.entity.Book;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.xhtmlrenderer.pdf.ITextRenderer;

import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

/**
 * PDF导出服务
 * 使用Flying Saucer生成题目PDF
 */
@Service
public class PdfExportService {

    @Autowired
    private QuestionService questionService;

    @Autowired
    private SubjectService subjectService;

    @Autowired
    private BookService bookService;

    @Autowired
    private MapQuestionSubjectService mapQuestionSubjectService;

    @Autowired
    private MapQuestionBookService mapQuestionBookService;

    /**
     * 生成题目PDF
     * @param subjectId 科目ID
     * @param bookId 习题册ID
     * @param questionIds 题目ID列表
     * @param includeAnswers 是否包含答案
     * @return PDF文件路径
     */
    public String generateQuestionsPDF(Integer subjectId, Integer bookId, List<Long> questionIds, boolean includeAnswers) {
        try {
            // 1. 获取题目列表
            List<Question> questions = fetchQuestions(subjectId, bookId, questionIds);

            if (questions.isEmpty()) {
                throw new RuntimeException("没有找到符合条件的题目");
            }

            // 2. 生成HTML内容
            String htmlContent = generateHtmlContent(questions, subjectId, bookId, includeAnswers);

            // 3. 创建输出目录
            String outputDir = "uploads/pdfs";
            Path dirPath = Paths.get(outputDir);
            if (!Files.exists(dirPath)) {
                Files.createDirectories(dirPath);
            }

            // 4. 生成文件名
            String filename = "questions_" + System.currentTimeMillis() + ".pdf";
            String outputPath = outputDir + "/" + filename;

            // 5. 使用Flying Saucer生成PDF
            ITextRenderer renderer = new ITextRenderer();
            renderer.setDocumentFromString(htmlContent);

            // 添加中文字体支持（使用系统字体）
            renderer.getFontResolver().addFont(
                    "C:/Windows/Fonts/msyh.ttc",
                    com.lowagie.text.pdf.BaseFont.IDENTITY_H,
                    com.lowagie.text.pdf.BaseFont.EMBEDDED
            );

            renderer.layout();
            FileOutputStream outputStream = new FileOutputStream(outputPath);
            renderer.createPDF(outputStream);
            outputStream.close();

            return outputPath;
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("PDF生成失败: " + e.getMessage());
        }
    }

    /**
     * 获取题目列表
     */
    private List<Question> fetchQuestions(Integer subjectId, Integer bookId, List<Long> questionIds) {
        List<Long> targetQuestionIds = null;

        // 根据条件获取题目ID
        if (questionIds != null && !questionIds.isEmpty()) {
            targetQuestionIds = questionIds;
        } else if (subjectId != null) {
            targetQuestionIds = mapQuestionSubjectService.getQuestionIdsBySubjectId(subjectId);
        } else if (bookId != null) {
            targetQuestionIds = mapQuestionBookService.getQuestionIdsByBookId(bookId);
        }

        if (targetQuestionIds == null || targetQuestionIds.isEmpty()) {
            return new ArrayList<>();
        }

        List<Question> questions = questionService.listByIds(targetQuestionIds);
        questions.sort(Comparator.comparing(Question::getId));
        return questions;
    }

    /**
     * 生成HTML内容
     */
    private String generateHtmlContent(List<Question> questions, Integer subjectId, Integer bookId, boolean includeAnswers) {
        StringBuilder html = new StringBuilder();

        // HTML头部
        html.append("<!DOCTYPE html>");
        html.append("<html xmlns=\"http://www.w3.org/1999/xhtml\">");
        html.append("<head>");
        html.append("<meta charset=\"UTF-8\"/>");
        html.append("<title>题目集</title>");
        html.append("<style>");
        html.append(generateCSS());
        html.append("</style>");
        html.append("</head>");
        html.append("<body>");

        // 标题部分
        html.append("<div class=\"header\">");
        html.append("<h1>考研题库</h1>");

        if (subjectId != null) {
            Subject subject = subjectService.getById(subjectId);
            if (subject != null) {
                html.append("<h2>").append(subject.getName()).append("</h2>");
            }
        }

        if (bookId != null) {
            Book book = bookService.getById(bookId);
            if (book != null) {
                html.append("<p>习题册：").append(book.getName()).append("</p>");
            }
        }

        html.append("<p>共 ").append(questions.size()).append(" 道题目</p>");
        html.append("</div>");

        // 题目列表
        int questionNum = 1;
        for (Question question : questions) {
            html.append("<div class=\"question-block\">");
            html.append("<div class=\"question-title\">");
            html.append(questionNum).append(". ");
            html.append(escapeHtml(question.getContent()));
            html.append("</div>");

            // 选项
            if (question.getOptions() != null && !question.getOptions().isEmpty()) {
                html.append("<div class=\"options\">");
                for (String option : question.getOptions()) {
                    html.append("<div class=\"option\">").append(escapeHtml(option)).append("</div>");
                }
                html.append("</div>");
            }

            // 答案（如果包含）
            if (includeAnswers && question.getAnswer() != null && !question.getAnswer().isEmpty()) {
                html.append("<div class=\"answer\">");
                html.append("<strong>答案：</strong>").append(escapeHtml(question.getAnswer()));
                html.append("</div>");
            }

            // 解析（如果包含）
            if (includeAnswers && question.getAnalysis() != null && !question.getAnalysis().isEmpty()) {
                html.append("<div class=\"analysis\">");
                html.append("<strong>解析：</strong>").append(escapeHtml(question.getAnalysis()));
                html.append("</div>");
            }

            html.append("</div>");
            questionNum++;
        }

        // HTML尾部
        html.append("</body>");
        html.append("</html>");

        return html.toString();
    }

    /**
     * 生成CSS样式
     */
    private String generateCSS() {
        return """
                body {
                    font-family: 'Microsoft YaHei', 'SimSun', sans-serif;
                    font-size: 14px;
                    line-height: 1.8;
                    color: #333;
                    margin: 20px;
                }

                .header {
                    text-align: center;
                    margin-bottom: 30px;
                    padding-bottom: 20px;
                    border-bottom: 2px solid #333;
                }

                .header h1 {
                    font-size: 28px;
                    font-weight: bold;
                    margin-bottom: 10px;
                }

                .header h2 {
                    font-size: 20px;
                    font-weight: bold;
                    margin-bottom: 5px;
                }

                .header p {
                    font-size: 14px;
                    color: #666;
                }

                .question-block {
                    margin-bottom: 25px;
                    padding: 15px;
                    background-color: #f9f9f9;
                    border-radius: 5px;
                }

                .question-title {
                    font-size: 16px;
                    font-weight: bold;
                    margin-bottom: 12px;
                    line-height: 1.6;
                }

                .options {
                    margin-left: 20px;
                    margin-bottom: 10px;
                }

                .option {
                    margin: 5px 0;
                    padding: 3px 0;
                }

                .answer {
                    margin-top: 10px;
                    padding: 8px;
                    background-color: #e8f5e9;
                    border-left: 3px solid #4caf50;
                    border-radius: 3px;
                }

                .analysis {
                    margin-top: 8px;
                    padding: 8px;
                    background-color: #fff3e0;
                    border-left: 3px solid #ff9800;
                    border-radius: 3px;
                }

                strong {
                    font-weight: bold;
                }
                """;
    }

    /**
     * HTML转义
     */
    private String escapeHtml(String text) {
        if (text == null) return "";
        return text.replace("&", "&amp;")
                   .replace("<", "&lt;")
                   .replace(">", "&gt;")
                   .replace("\"", "&quot;")
                   .replace("'", "&#x27;");
    }
}
