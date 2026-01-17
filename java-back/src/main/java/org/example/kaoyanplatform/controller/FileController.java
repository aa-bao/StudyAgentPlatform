package org.example.kaoyanplatform.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.example.kaoyanplatform.common.Result;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

@Tag(name = "文件管理", description = "文件上传接口")
@RestController
@RequestMapping("/file")
public class FileController {

    // 定义头像保存的本地路径（确保该目录存在）
    private String uploadPath = System.getProperty("user.dir") + "/uploads/";

    @PostMapping("/upload")
    @Operation(summary = "上传文件", description = """
            通用文件上传接口，支持任意格式文件上传。
            上传的文件将保存到服务器uploads目录，返回可访问的文件URL。
            返回数据：
            - 成功：返回文件访问URL（格式：http://localhost:8081/uploads/文件名）
            - 失败：返回错误信息
            """)
    public Result upload(
            @Parameter(
                description = "上传的文件，支持任意格式（图片、文档等）",
                required = true,
                content = @io.swagger.v3.oas.annotations.media.Content(
                    mediaType = "multipart/form-data"
                )
            )
            MultipartFile file) throws IOException {
        if (file.isEmpty()) return Result.error("文件不能为空");

        // 生成唯一文件名
        String originalFilename = file.getOriginalFilename();
        String suffix = originalFilename.substring(originalFilename.lastIndexOf("."));
        String fileName = UUID.randomUUID().toString() + suffix;

        File dest = new File(uploadPath + fileName);
        if (!dest.getParentFile().exists()) dest.getParentFile().mkdirs();

        file.transferTo(dest);

        // 返回文件的访问路径 (前端需要配置静态资源映射)
        String url = "http://localhost:8081/uploads/" + fileName;
        return Result.success(url);
    }
}