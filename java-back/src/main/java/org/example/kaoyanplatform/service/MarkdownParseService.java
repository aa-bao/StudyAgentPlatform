package org.example.kaoyanplatform.service;

import org.example.kaoyanplatform.entity.dto.QuestionImportDTO;

import java.io.File;

/**
 * Markdown解析服务接口
 * 用于解析MinerU生成的Markdown文件并转换为题目JSON
 */
public interface MarkdownParseService {

    /**
     * 解析Markdown文件为题目导入DTO
     * @param mdFile Markdown文件
     * @param imageBaseDir 图片基础目录
     * @param bookId 习题册ID
     * @param subjectIds 科目ID列表
     * @param source 题目来源
     * @return 题目导入DTO
     */
    QuestionImportDTO parseMarkdownToQuestions(File mdFile, File imageBaseDir, Integer bookId, java.util.List<Integer> subjectIds, String source);

    /**
     * 解压Zip文件到临时目录
     * @param zipFile Zip文件
     * @return 解压后的目录
     */
    File unzipToTempDir(org.springframework.web.multipart.MultipartFile zipFile) throws Exception;
}
