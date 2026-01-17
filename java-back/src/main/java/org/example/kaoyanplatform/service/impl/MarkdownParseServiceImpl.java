package org.example.kaoyanplatform.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.example.kaoyanplatform.entity.dto.QuestionImportDTO;
import org.example.kaoyanplatform.service.MarkdownParseService;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * Markdown解析服务实现类
 * 专门适配 MinerU 导出的考研真题格式
 */
@Slf4j
@Service
public class MarkdownParseServiceImpl implements MarkdownParseService {

    @Override
    public QuestionImportDTO parseMarkdownToQuestions(File mdFile, File imageBaseDir, Integer bookId, List<Integer> subjectIds, String source) {
        try {
            // 读取Markdown内容
            String mdContent = Files.readString(mdFile.toPath());

            log.info("开始解析Markdown文件: {}, 文件大小: {} 字符", mdFile.getName(), mdContent.length());

            // 1. 分割题目部分和答案解析部分
            // 查找 "# 计算机专业基础综合考试真题思路分析" 作为分隔符
            String analysisMarker = "#\\s*计算机专业基础综合考试真题思路分析";
            String[] parts = mdContent.split(analysisMarker, 2);
            String questionPart = parts[0];
            String answerPart = parts.length > 1 ? parts[1] : "";

            log.info("分割完成 - 题目部分: {} 字符, 答案部分: {} 字符", questionPart.length(), answerPart.length());

            // 2. 解析题目部分
            List<QuestionImportItem> questionItems = parseQuestions(questionPart, answerPart, source);

            log.info("题目解析完成，共提取 {} 道题目", questionItems.size());

            // 3. 构建返回DTO
            QuestionImportDTO importDTO = new QuestionImportDTO();
            importDTO.setBookId(bookId);
            importDTO.setSubjectIds(subjectIds);

            List<QuestionImportDTO.QuestionImportItem> questions = new ArrayList<>();
            for (QuestionImportItem item : questionItems) {
                QuestionImportDTO.QuestionImportItem q = new QuestionImportDTO.QuestionImportItem();
                q.setType(item.type);
                q.setContent(item.content);
                q.setOptions(item.options);
                q.setAnswer(item.answer);
                q.setAnalysis(item.analysis);
                q.setTags(item.tags);
                q.setSource(item.source);
                questions.add(q);
            }
            importDTO.setQuestions(questions);

            log.info("✅ 成功解析Markdown文件: {}, 提取题目数: {}", mdFile.getName(), questions.size());
            return importDTO;

        } catch (Exception e) {
            log.error("❌ 解析Markdown文件失败: {}", mdFile.getPath(), e);
            throw new RuntimeException("解析Markdown文件失败: " + e.getMessage());
        }
    }

    /**
     * 解析题目列表
     * 适配格式：数字．（全角点）+ 题目内容（选项可能在一行或多行）
     */
    private List<QuestionImportItem> parseQuestions(String questionPart, String answerPart, String source) {
        List<QuestionImportItem> questions = new ArrayList<>();

        // 正则匹配题目: 换行 + 数字 + 全角点（．）+ 题目内容（直到下一个换行+数字+全角点或文件结束）
        Pattern questionPattern = Pattern.compile("\\n(\\d+)．\\s*([^\\n]+(?:\\n[^\\n\\d])*?)(?=\\n\\d+．|$)", Pattern.DOTALL);
        Matcher matcher = questionPattern.matcher(questionPart);

        log.info("🔍 开始解析题目，使用正则: \\n(\\d+)．...");

        int matchCount = 0;
        while (matcher.find()) {
            matchCount++;
            int questionNum = Integer.parseInt(matcher.group(1));
            String fullQuestionText = matcher.group(2).trim();

            log.debug("找到题目 {}: 题干前100字符={}", questionNum,
                fullQuestionText.length() > 100 ? fullQuestionText.substring(0, 100) : fullQuestionText);

            QuestionImportItem item = new QuestionImportItem();
            item.number = questionNum;

            // 提取图片
            List<String> images = extractImages(fullQuestionText);
            item.images = images;

            // 提取选项和题干
            OptionResult optionResult = extractOptionsV2(fullQuestionText);
            item.hasOptions = optionResult.hasOptions;
            item.type = optionResult.hasOptions ? 1 : 4; // 1=单选, 4=简答
            item.options = optionResult.options;
            item.content = optionResult.cleanContent;

            // 提取答案和解析
            extractAnswerAndAnalysis(answerPart, questionNum, item);

            item.tags = new ArrayList<>();
            item.source = source != null ? source : "2009年考研真题";

            questions.add(item);

            log.debug("题目 {} 解析完成: 类型={}, 选项数={}, 答案={}",
                questionNum, item.type, item.options.size(), item.answer);
        }

        log.info("正则匹配完成，共匹配到 {} 道题目", matchCount);
        return questions;
    }

    /**
     * 提取图片路径
     */
    private List<String> extractImages(String text) {
        List<String> images = new ArrayList<>();
        Pattern imgPattern = Pattern.compile("!\\[.*?\\]\\((.*?)\\)");
        Matcher matcher = imgPattern.matcher(text);
        while (matcher.find()) {
            images.add(matcher.group(1));
        }
        return images;
    }

    /**
     * 提取选项 V2 - 适配一行多个选项格式
     * 例如：A.栈 B.队列 C.树 D.图
     */
    private OptionResult extractOptionsV2(String questionText) {
        OptionResult result = new OptionResult();
        result.cleanContent = questionText;

        // 匹配选项：支持 A. xxx B. xxx 格式（在同一行）
        // 正则说明：([A-D])\. 匹配字母+点，\s* 匹配空格，(.*?) 匹配选项内容（非贪婪）
        // (?=\s*[A-D]\.|$) 前瞻断言：直到下一个选项或行尾
        Pattern optPattern = Pattern.compile("([A-D])\\.\\s*([^A-D]*?)(?=\\s*[A-D]\\.|$|\\n)", Pattern.DOTALL);
        Matcher matcher = optPattern.matcher(questionText);

        List<String> options = new ArrayList<>();
        int lastOptEndIdx = -1;

        while (matcher.find()) {
            String label = matcher.group(1);
            String content = matcher.group(2).trim();

            // 清理选项内容（移除多余空格和换行）
            content = content.replaceAll("\\s+", " ").trim();

            if (!content.isEmpty()) {
                options.add(label + ". " + content);

                // 记录最后一个选项的结束位置
                if (lastOptEndIdx == -1) {
                    // 第一个选项的开始位置
                    lastOptEndIdx = matcher.start();
                }
                lastOptEndIdx = Math.max(lastOptEndIdx, matcher.end());
            }
        }

        if (options.size() >= 2) {
            result.hasOptions = true;
            result.options = options;

            // 移除选项，保留题干
            if (lastOptEndIdx != -1 && lastOptEndIdx < questionText.length()) {
                // 从开始到最后选项结束位置之前的内容作为题干
                // 但需要找到第一个选项的开始位置
                int firstOptStart = questionText.length();
                Matcher firstOptMatcher = optPattern.matcher(questionText);
                if (firstOptMatcher.find()) {
                    firstOptStart = firstOptMatcher.start();
                    result.cleanContent = questionText.substring(0, firstOptStart).trim();
                }
            } else if (lastOptEndIdx != -1) {
                result.cleanContent = questionText.substring(0, lastOptEndIdx).trim();
            }

            log.debug("选项提取: 找到 {} 个选项，题干长度={}", options.size(), result.cleanContent.length());
        } else {
            result.hasOptions = false;
            result.options = new ArrayList<>();
            log.debug("选项提取: 未找到选项");
        }

        return result;
    }

    /**
     * 提取答案和解析
     * 适配格式：\n数字．解答： 或 \n数字.解答：
     */
    private void extractAnswerAndAnalysis(String answerPart, int questionNum, QuestionImportItem item) {
        // 匹配解析: "\n45．解答：" 或 "\n45.解答："
        // 使用更宽松的正则，匹配全角点或半角点
        String[] patterns = {
            "\n" + questionNum + "[．.]\\s*解答：(.*?)(?=\\n\\d+[．.]\\s*解答：|$)",
            "\n" + questionNum + "[．.]\\s*解析：(.*?)(?=\\n\\d+[．.]\\s*解析：|$)",
            "#" + questionNum + "[．.]\\s*解答：(.*?)(?=#\\d+[．.]\\s*解答：|$)"
        };

        for (String patternStr : patterns) {
            Pattern analysisPattern = Pattern.compile(patternStr, Pattern.DOTALL);
            Matcher matcher = analysisPattern.matcher(answerPart);

            if (matcher.find()) {
                item.analysis = matcher.group(1).trim();

                // 尝试从解析中提取答案 "故选B" 或 "答案：B"
                Pattern[] ansPatterns = {
                    Pattern.compile("故选\\s*([A-D])"),
                    Pattern.compile("答案[：:]\\s*([A-D])"),
                    Pattern.compile("因此选\\s*([A-D])"),
                    Pattern.compile("正确答案[是为]\\s*([A-D])")
                };

                for (Pattern ansPattern : ansPatterns) {
                    Matcher ansMatcher = ansPattern.matcher(item.analysis);
                    if (ansMatcher.find()) {
                        item.answer = ansMatcher.group(1);
                        break;
                    }
                }

                log.debug("找到题目 {} 的解析，长度={}, 答案={}", questionNum, item.analysis.length(), item.answer);
                return;
            }
        }

        // 如果没找到解析，尝试找简单答案
        log.debug("未找到题目 {} 的解析", questionNum);

        if (item.answer == null || item.answer.isEmpty()) {
            // 尝试在答案部分查找简单答案格式
            Pattern simpleAnsPattern = Pattern.compile("\\n" + questionNum + "\\s*[.．]?\\s*[【\\[]?答案[}\\]：:]?\\s*([A-D])");
            Matcher simpleMatcher = simpleAnsPattern.matcher(answerPart);
            if (simpleMatcher.find()) {
                item.answer = simpleMatcher.group(1);
                log.debug("找到简单答案: {}", item.answer);
            }
        }

        if (item.analysis == null) {
            item.analysis = "";
        }
        if (item.answer == null) {
            item.answer = "";
        }
    }

    @Override
    public File unzipToTempDir(MultipartFile zipFile) throws Exception {
        // 创建临时目录
        Path tempDir = Files.createTempDirectory("question_import_");
        File tempDirFile = tempDir.toFile();

        try (ZipInputStream zis = new ZipInputStream(zipFile.getInputStream())) {
            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                File newFile = new File(tempDirFile, entry.getName());

                if (entry.isDirectory()) {
                    newFile.mkdirs();
                    continue;
                }

                // 确保父目录存在
                newFile.getParentFile().mkdirs();

                // 写入文件
                try (FileOutputStream fos = new FileOutputStream(newFile)) {
                    byte[] buffer = new byte[1024];
                    int len;
                    while ((len = zis.read(buffer)) > 0) {
                        fos.write(buffer, 0, len);
                    }
                }
                zis.closeEntry();
            }
        }

        log.info("解压Zip文件到临时目录: {}", tempDirFile.getPath());
        return tempDirFile;
    }

    /**
     * 内部类：题目解析项
     */
    private static class QuestionImportItem {
        int number;
        int type;
        String content;
        List<String> options;
        String answer;
        String analysis;
        List<String> tags;
        String source;
        boolean hasOptions;
        List<String> images;
    }

    /**
     * 内部类：选项解析结果
     */
    private static class OptionResult {
        boolean hasOptions;
        List<String> options;
        String cleanContent;
    }
}
