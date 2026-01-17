package org.example.kaoyanplatform.entity.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

/**
 * 压缩包导入题目DTO
 * 用于接收包含 .md 和 images/ 的 Zip 包
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "压缩包导入题目请求")
public class QuestionZipImportDTO {

    @Schema(description = "上传的压缩包文件（zip格式）")
    private MultipartFile zipFile;

    @Schema(description = "习题册ID")
    private Integer bookId;

    @Schema(description = "科目ID列表")
    private String subjectIds; // 逗号分隔的字符串，如 "101,102"

    @Schema(description = "题目来源（可选）")
    private String source;

    @Schema(description = "是否上传并替换图片路径（默认false）")
    private Boolean uploadImages;
}
