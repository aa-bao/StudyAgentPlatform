package org.example.kaoyanplatform.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.example.kaoyanplatform.common.Result;
import org.example.kaoyanplatform.entity.Collection;
import org.example.kaoyanplatform.service.CollectionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Tag(name = "收藏管理", description = "题目收藏相关接口")
@RestController
@RequestMapping("/collection")
public class CollectionController {

    @Autowired
    private CollectionService collectionService;

    // 添加或更新收藏
    @PostMapping("/add")
    @Operation(summary = "添加或更新收藏", description = "收藏题目或更新收藏标签。如果已收藏则更新标签，如果未收藏则新增收藏记录。")
    public Result addCollection(@RequestBody Collection collection) {
        // 1. 基础校验
        if (collection.getUserId() == null || collection.getQuestionId() == null) {
            return Result.error("用户ID或题目ID不能为空");
        }

        // 2. 检查是否已收藏
        LambdaQueryWrapper<Collection> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Collection::getUserId, collection.getUserId())
                .eq(Collection::getQuestionId, collection.getQuestionId());

        Collection exist = collectionService.getOne(wrapper);
        if (exist != null) {
            // 更新标签
            exist.setTags(collection.getTags());
            collectionService.updateById(exist);
            return Result.success("收藏标签已更新");
        } else {
            // 新增
            collection.setCreateTime(new Date());
            boolean saved = collectionService.save(collection);
            return saved ? Result.success("收藏成功") : Result.error("收藏失败");
        }
    }

    // 取消收藏
    @PostMapping("/remove")
    @Operation(summary = "取消收藏", description = "取消收藏指定题目。需要传入 userId 和 questionId")
    public Result removeCollection(@RequestBody Collection collection) {
        if (collection.getUserId() == null || collection.getQuestionId() == null) {
            return Result.error("参数缺失");
        }

        LambdaQueryWrapper<Collection> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Collection::getUserId, collection.getUserId())
                .eq(Collection::getQuestionId, collection.getQuestionId());

        boolean removed = collectionService.remove(wrapper);
        return removed ? Result.success("已取消收藏") : Result.error("操作失败或记录不存在");
    }

    // 检查是否已收藏
    @GetMapping("/check")
    @Operation(summary = "检查收藏状态")
    public Result checkCollection(
            @Parameter(description = "用户ID", required = true) @RequestParam Long userId,
            @Parameter(description = "题目ID", required = true) @RequestParam Long questionId) {

        LambdaQueryWrapper<Collection> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Collection::getUserId, userId)
                .eq(Collection::getQuestionId, questionId);

        Collection exist = collectionService.getOne(wrapper);
        // 直接返回对象，前端根据对象是否为 null 判断状态即可
        return Result.success(exist);
    }

    // 获取用户的收藏列表
    @GetMapping("/list")
    @Operation(summary = "获取收藏列表", description = "获取用户的所有收藏记录，按创建时间倒序排列。")
    public Result getList(@Parameter(description = "用户ID", required = true) @RequestParam Long userId) {
        LambdaQueryWrapper<Collection> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Collection::getUserId, userId)
                .orderByDesc(Collection::getCreateTime);
        return Result.success(collectionService.list(wrapper));
    }

    // 获取用户所有收藏标签（去重）
    @GetMapping("/tags")
    @Operation(summary = "获取所有收藏标签", description = "获取用户使用过的所有标签（去重）。")
    public Result getTags(@Parameter(description = "用户ID", required = true) @RequestParam Long userId) {
        LambdaQueryWrapper<Collection> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Collection::getUserId, userId);
        List<Collection> list = collectionService.list(wrapper);

        // 使用 Stream 流进行扁平化和去重
        List<String> tags = list.stream()
                .filter(c -> c.getTags() != null)
                .flatMap(c -> c.getTags().stream())
                .distinct()
                .sorted() // 增加一个排序，让前端展示更整齐
                .collect(Collectors.toList());

        return Result.success(tags);
    }
}