package org.example.kaoyanplatform.controller;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.example.kaoyanplatform.common.Result;
import org.example.kaoyanplatform.entity.User;
import org.example.kaoyanplatform.entity.dto.UserStudyStatsDTO;
import org.example.kaoyanplatform.entity.dto.HomePageDataDTO;
import org.example.kaoyanplatform.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.UUID;

@Tag(name = "用户管理", description = "提供用户登录、注册、资料管理及学习统计等接口")
@RestController
@RequestMapping("/user")
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    @PostMapping("/login")
    @Operation(summary = "用户登录", description = "验证用户名密码，成功后返回用户信息")
    @ApiResponse(responseCode = "200", description = "登录成功")
    public Result login(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "登录参数",
                    content = @Content(examples = @ExampleObject(value = "{\"username\": \"admin\", \"password\": \"123456\"}"))
            )
            @RequestBody Map<String, String> loginData) {
        String username = loginData.get("username");
        String password = loginData.get("password");

        User user = userService.getOne(new LambdaQueryWrapper<User>().eq(User::getUsername, username));

        if (user != null && passwordEncoder.matches(password, user.getPassword())) {
            user.setPassword(null);
            return Result.success(user);
        } else {
            return Result.error("用户名或密码错误");
        }
    }

    @PostMapping("/register")
    @Operation(summary = "用户注册", description = "创建新用户。用户名具有唯一性限制。")
    public Result register(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "注册用户信息",
                    content = @Content(examples = @ExampleObject(value = "{\"username\": \"testuser\", \"password\": \"123456\", \"nickname\": \"考研加油\"}"))
            )
            @RequestBody User user) {
        if (StrUtil.isBlank(user.getUsername()) || StrUtil.isBlank(user.getPassword())) {
            return Result.error("用户名或密码不能为空");
        }
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        boolean success = userService.register(user);
        return success ? Result.success("注册成功") : Result.error("用户名已存在");
    }

    @PostMapping("/update")
    @Operation(summary = "更新个人信息", description = "根据ID更新用户的昵称、头像、目标院校等非敏感信息。")
    public Result update(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "更新数据（必须包含id）",
                    content = @Content(examples = @ExampleObject(value = "{\"id\": 1, \"nickname\": \"新昵称\", \"targetSchool\": \"清华大学\"}"))
            )
            @RequestBody User user) {
        if (user.getId() == null) {
            return Result.error("更新失败：用户ID不能为空");
        }

        User dbUser = userService.getById(user.getId());
        if (dbUser == null) return Result.error("用户不存在");

        if (user.getNickname() != null) dbUser.setNickname(user.getNickname());
        if (user.getAvatar() != null) dbUser.setAvatar(user.getAvatar());
        if (user.getPhone() != null) dbUser.setPhone(user.getPhone());
        if (user.getEmail() != null) dbUser.setEmail(user.getEmail());
        if (user.getExamYear() != null) dbUser.setExamYear(user.getExamYear());
        if (user.getExamSubjects() != null) dbUser.setExamSubjects(user.getExamSubjects());
        if (user.getTargetSchool() != null) dbUser.setTargetSchool(user.getTargetSchool());
        if (user.getTargetTotalScore() != null) dbUser.setTargetTotalScore(user.getTargetTotalScore());

        boolean success = userService.updateById(dbUser);
        if (success) {
            dbUser.setPassword(null);
            return Result.success(dbUser);
        }
        return Result.error("服务器更新失败");
    }

    @PostMapping(value = "/upload", consumes = "multipart/form-data")
    @Operation(summary = "上传头像", description = "上传图片文件并返回静态资源访问URL。")
    public Result upload(
            @Parameter(description = "要上传的头像图片文件", required = true)
            @RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) return Result.error("上传文件不能为空");

        try {
            String fileName = UUID.randomUUID() + "_" + file.getOriginalFilename();
            String uploadDir = System.getProperty("user.dir") + "/uploads/";

            File dest = new File(uploadDir + fileName);
            if (!dest.getParentFile().exists()) dest.getParentFile().mkdirs();

            file.transferTo(dest);
            String fileUrl = "/uploads/" + fileName;
            return Result.success(fileUrl);
        } catch (IOException e) {
            e.printStackTrace();
            return Result.error("文件上传失败");
        }
    }

    @PostMapping("/updatePwd")
    @Operation(summary = "修改密码", description = "校验旧密码并设置新密码。")
    public Result updatePwd(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "密码修改参数",
                    content = @Content(examples = @ExampleObject(value = "{\"userId\": \"1\", \"oldPassword\": \"123456\", \"newPassword\": \"654321\"}"))
            )
            @RequestBody Map<String, String> params) {
        String userId = params.get("userId");
        String oldPassword = params.get("oldPassword");
        String newPassword = params.get("newPassword");

        if (StrUtil.hasBlank(userId, oldPassword, newPassword)) {
            return Result.error("参数缺失");
        }

        User user = userService.getById(userId);
        if (user == null) return Result.error("用户不存在");

        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            return Result.error("旧密码错误");
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        return userService.updateById(user) ? Result.success("密码修改成功") : Result.error("更新失败");
    }

    @PostMapping("/resetPwd")
    @Operation(summary = "管理员重置用户密码", description = "管理员直接设置用户新密码，无需验证旧密码。")
    public Result resetPwd(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "重置密码参数",
                    content = @Content(examples = @ExampleObject(value = "{\"id\": 1, \"password\": \"123456\"}"))
            )
            @RequestBody Map<String, String> params) {
        String id = params.get("id");
        String password = params.get("password");

        if (StrUtil.hasBlank(id, password)) {
            return Result.error("参数缺失");
        }

        User user = userService.getById(id);
        if (user == null) return Result.error("用户不存在");

        user.setPassword(passwordEncoder.encode(password));
        return userService.updateById(user) ? Result.success("密码重置成功") : Result.error("更新失败");
    }

    @GetMapping("/page")
    @Operation(summary = "分页获取用户列表", description = "供管理员使用。支持按角色过滤及关键词模糊搜索（用户名/昵称/目标院校）。")
    public Result getUserPage(
            @Parameter(description = "当前页码", example = "1") @RequestParam(defaultValue = "1") Integer pageNum,
            @Parameter(description = "每页条数", example = "10") @RequestParam(defaultValue = "10") Integer pageSize,
            @Parameter(description = "用户角色(ADMIN/USER)", example = "USER") @RequestParam(required = false) String role,
            @Parameter(description = "搜索关键词(用户名/昵称/院校)") @RequestParam(required = false) String keyword) {

        Page<User> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();

        if (StrUtil.isNotBlank(role)) wrapper.eq(User::getRole, role);
        if (StrUtil.isNotBlank(keyword)) {
            wrapper.and(w -> w.like(User::getUsername, keyword)
                    .or().like(User::getNickname, keyword)
                    .or().like(User::getTargetSchool, keyword));
        }
        wrapper.orderByDesc(User::getCreateTime);

        Page<User> result = userService.page(page, wrapper);
        result.getRecords().forEach(user -> user.setPassword(null));

        return Result.success(result);
    }

    @GetMapping("userInfo")
    @Operation(summary = "根据用户ID获取用户信息", description = "通过传入的用户ID查询并返回对应的用户详细信息")
    @ApiResponse(responseCode = "200", description = "成功返回用户信息")
    public Result getUserInfo(
            @Parameter(description = "用户唯一标识 ID", required = true, example = "12345")
            @RequestParam Long userId) {
        User userInfo = userService.getById(userId);
        return Result.success(userInfo);
    }

    @GetMapping("/studyStats/{userId}")
    @Operation(summary = "获取用户学习统计数据", description = "获取指定用户的总答题量、正确率、连续打卡天数等统计信息。")
    public Result getUserStudyStats(
            @Parameter(description = "用户ID", required = true, example = "1")
            @PathVariable Long userId) {
        UserStudyStatsDTO stats = userService.getUserStudyStats(userId);
        if (stats == null) {
            return Result.error("用户不存在");
        }
        return Result.success(stats);
    }

    @GetMapping("/homeData/{userId}")
    @Operation(summary = "获取用户首页数据", description = "获取用户首页所需的所有数据，包括用户信息、学习统计、科目列表、每日语录和推荐内容。")
    public Result getHomePageData(
            @Parameter(description = "用户ID", required = true, example = "1")
            @PathVariable Long userId) {
        var homeData = userService.getHomePageData(userId);
        if (homeData == null) {
            return Result.error("用户不存在");
        }
        return Result.success(homeData);
    }
}
