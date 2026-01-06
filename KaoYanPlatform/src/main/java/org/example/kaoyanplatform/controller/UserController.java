package org.example.kaoyanplatform.controller;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.example.kaoyanplatform.common.Result;
import org.example.kaoyanplatform.entity.User;
import org.example.kaoyanplatform.entity.dto.UserStudyStatsDTO;
import org.example.kaoyanplatform.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/user")
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    // 登录
    @PostMapping("/login")
    public Result login(@RequestBody Map<String, String> loginData) {
        String username = loginData.get("username");
        String password = loginData.get("password");

        // 先按用户名查询
        User user = userService.getOne(new LambdaQueryWrapper<User>().eq(User::getUsername, username));

        // 校验：用户存在 且 密码匹配 (BCrypt 必须使用 matches)
        if (user != null && passwordEncoder.matches(password, user.getPassword())) {
            user.setPassword(null); // 安全起见，返回前端前擦除密码
            return Result.success(user);
        } else {
            return Result.error("用户名或密码错误");
        }
    }

    // 注册
    @PostMapping("/register")
    public Result register(@RequestBody User user) {
        if (StrUtil.isBlank(user.getUsername()) || StrUtil.isBlank(user.getPassword())) {
            return Result.error("用户名或密码不能为空");
        }
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        boolean success = userService.register(user);
        return success ? Result.success("注册成功") : Result.error("用户名已存在");
    }

    // 更新个人资料
    @PostMapping("/update")
    public Result update(@RequestBody User user) {
        if (user.getId() == null) {
            return Result.error("更新失败：用户ID不能为空");
        }

        User dbUser = userService.getById(user.getId());
        if (dbUser == null) return Result.error("用户不存在");

        if (user.getNickname() != null) dbUser.setNickname(user.getNickname());
        if (user.getAvatar() != null) dbUser.setAvatar(user.getAvatar());
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

    // 头像上传
    @PostMapping("/upload")
    public Result upload(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) return Result.error("上传文件不能为空");

        try {
            String fileName = UUID.randomUUID() + "_" + file.getOriginalFilename();
            String uploadDir = System.getProperty("user.dir") + "/uploads/";

            File dest = new File(uploadDir + fileName);
            if (!dest.getParentFile().exists()) dest.getParentFile().mkdirs();

            file.transferTo(dest);

            // 返回路径前缀必须与 WebConfig 中的 addResourceHandler("/uploads/**") 一致
            String fileUrl = "/uploads/" + fileName;
            return Result.success(fileUrl);
        } catch (IOException e) {
            e.printStackTrace();
            return Result.error("文件上传失败");
        }
    }

    // 修改密码
    @PostMapping("/updatePwd")
    public Result updatePwd(@RequestBody Map<String, String> params) {
        String userId = params.get("userId");
        String oldPassword = params.get("oldPassword");
        String newPassword = params.get("newPassword");

        if (StrUtil.hasBlank(userId, oldPassword, newPassword)) {
            return Result.error("参数缺失");
        }

        User user = userService.getById(userId);
        if (user == null) return Result.error("用户不存在");

        // 比对旧密码密文
        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            return Result.error("旧密码错误");
        }

        // 设置新密码密文
        user.setPassword(passwordEncoder.encode(newPassword));
        return userService.updateById(user) ? Result.success("密码修改成功") : Result.error("更新失败");
    }

    /**
     * 获取用户列表（分页，用于管理员）
     */
    @GetMapping("/page")
    public Result getUserPage(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String role,
            @RequestParam(required = false) String keyword) {

        Page<User> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();

        // 角色筛选
        if (StrUtil.isNotBlank(role)) {
            wrapper.eq(User::getRole, role);
        }

        // 关键词搜索（用户名、昵称、目标院校）
        if (StrUtil.isNotBlank(keyword)) {
            wrapper.and(w -> w.like(User::getUsername, keyword)
                    .or().like(User::getNickname, keyword)
                    .or().like(User::getTargetSchool, keyword));
        }

        // 按创建时间倒序
        wrapper.orderByDesc(User::getCreateTime);

        // 清除密码字段
        Page<User> result = userService.page(page, wrapper);
        result.getRecords().forEach(user -> user.setPassword(null));

        return Result.success(result);
    }

    /**
     * 获取用户学习统计数据（用于管理员监控）
     */
    @GetMapping("/study-stats/{userId}")
    public Result getUserStudyStats(@PathVariable Long userId) {
        UserStudyStatsDTO stats = userService.getUserStudyStats(userId);
        if (stats == null) {
            return Result.error("用户不存在");
        }
        return Result.success(stats);
    }
}