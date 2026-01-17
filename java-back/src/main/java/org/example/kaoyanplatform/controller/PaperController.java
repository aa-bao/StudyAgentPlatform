package org.example.kaoyanplatform.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.example.kaoyanplatform.common.Result;
import org.example.kaoyanplatform.entity.Paper;
import org.example.kaoyanplatform.entity.MapPaperQuestion;
import org.example.kaoyanplatform.entity.Question;
import org.example.kaoyanplatform.service.PaperService;
import org.example.kaoyanplatform.service.MapPaperQuestionService;
import org.example.kaoyanplatform.service.QuestionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Tag(name = "试卷管理", description = "试卷增删改查接口，包括试卷基本信息和题目关联管理")
@RestController
@RequestMapping("/paper")
public class PaperController {

    @Autowired
    private PaperService paperService;

    @Autowired
    private MapPaperQuestionService mapPaperQuestionService;

    @Autowired
    private QuestionService questionService;

    @PostMapping("/add")
    @Operation(summary = "新增试卷", description = "创建新试卷，支持设置试卷结构和题目关联")
    public Result<String> addPaper(@RequestBody Paper paper) {
        try {
            boolean success = paperService.save(paper);
            return success ? Result.success("试卷创建成功") : Result.error("试卷创建失败");
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @PostMapping("/update")
    @Operation(summary = "更新试卷", description = "更新试卷基本信息和结构配置")
    public Result<String> updatePaper(@RequestBody Paper paper) {
        try {
            if (paper.getId() == null) {
                return Result.error("试卷ID不能为空");
            }
            boolean success = paperService.updateById(paper);
            return success ? Result.success("试卷更新成功") : Result.error("试卷更新失败");
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @DeleteMapping("/delete/{id}")
    @Operation(summary = "删除试卷", description = "删除试卷及其所有题目关联关系")
    public Result<String> deletePaper(
            @Parameter(description = "试卷ID", required = true) @PathVariable String id) {
        try {
            List<MapPaperQuestion> mappings = mapPaperQuestionService.list(
                    new LambdaQueryWrapper<MapPaperQuestion>().eq(MapPaperQuestion::getPaperId, id)
            );
            
            if (!mappings.isEmpty()) {
                mapPaperQuestionService.removeByIds(
                        mappings.stream().map(MapPaperQuestion::getId).toList()
                );
            }
            
            boolean success = paperService.removeById(id);
            return success ? Result.success("试卷删除成功") : Result.error("试卷删除失败");
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @GetMapping("/{id}")
    @Operation(summary = "获取试卷详情", description = "根据ID获取试卷的完整信息，包括结构配置")
    public Result<Paper> getPaper(
            @Parameter(description = "试卷ID", required = true) @PathVariable String id) {
        try {
            Paper paper = paperService.getById(id);
            if (paper == null) {
                return Result.error("试卷不存在");
            }
            return Result.success(paper);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @GetMapping("/list")
    @Operation(summary = "获取试卷列表", description = "查询所有试卷，可按试卷类型、年份筛选")
    public Result<List<Paper>> getPapers(
            @Parameter(description = "试卷类型：0-真题，1-模拟") @RequestParam(required = false) Integer paperType,
            @Parameter(description = "试卷年份") @RequestParam(required = false) Integer year) {
        try {
            LambdaQueryWrapper<Paper> wrapper = new LambdaQueryWrapper<>();
            if (paperType != null) {
                wrapper.eq(Paper::getPaperType, paperType);
            }
            if (year != null) {
                wrapper.eq(Paper::getYear, year);
            }
            wrapper.orderByDesc(Paper::getCreateTime);
            List<Paper> papers = paperService.list(wrapper);
            return Result.success(papers);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @GetMapping("/page")
    @Operation(summary = "分页查询试卷", description = "支持分页和多条件筛选的试卷查询")
    public Result<Page<Paper>> getPaperPage(
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Integer pageNum,
            @Parameter(description = "每页数量") @RequestParam(defaultValue = "10") Integer pageSize,
            @Parameter(description = "试卷类型：0-真题，1-模拟") @RequestParam(required = false) Integer paperType,
            @Parameter(description = "考试规格ID") @RequestParam(required = false) String examSpecId,
            @Parameter(description = "试卷标题关键词") @RequestParam(required = false) String keyword,
            @Parameter(description = "试卷年份") @RequestParam(required = false) Integer year) {
        try {
            Page<Paper> page = new Page<>(pageNum, pageSize);
            LambdaQueryWrapper<Paper> wrapper = new LambdaQueryWrapper<>();

            if (paperType != null) {
                wrapper.eq(Paper::getPaperType, paperType);
            }
            if (examSpecId != null) {
                wrapper.eq(Paper::getExamSpecId, examSpecId);
            }
            if (keyword != null && !keyword.isEmpty()) {
                wrapper.like(Paper::getTitle, keyword);
            }
            if (year != null) {
                wrapper.eq(Paper::getYear, year);
            }

            wrapper.orderByDesc(Paper::getCreateTime);
            Page<Paper> result = paperService.page(page, wrapper);
            return Result.success(result);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @GetMapping("/{paperId}/questions")
    @Operation(summary = "获取试卷题目列表", description = "根据试卷ID获取关联的所有题目及详细配置")
    public Result<List<Question>> getPaperQuestions(
            @Parameter(description = "试卷ID", required = true) @PathVariable String paperId) {
        try {
            List<Question> questions = mapPaperQuestionService.getQuestionsWithDetails(paperId);
            return Result.success(questions);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @PostMapping("/{paperId}/add-question")
    @Operation(summary = "添加题目到试卷", description = "将指定题目添加到试卷中，设置题目顺序、分值和类型")
    public Result<String> addQuestionToPaper(
            @Parameter(description = "试卷ID", required = true) @PathVariable String paperId,
            @Parameter(description = "题目ID", required = true) @RequestParam String questionId,
            @Parameter(description = "题目顺序") @RequestParam Integer sortOrder,
            @Parameter(description = "题目分值") @RequestParam java.math.BigDecimal scoreValue,
            @Parameter(description = "题目类型（可选，用于覆盖原题目类型）") @RequestParam(required = false) Integer type) {
        try {
            MapPaperQuestion mapping = new MapPaperQuestion();
            mapping.setPaperId(paperId);
            mapping.setQuestionId(questionId);
            mapping.setSortOrder(sortOrder);
            mapping.setScoreValue(scoreValue);
            mapping.setType(type);

            boolean success = mapPaperQuestionService.save(mapping);
            return success ? Result.success("题目添加成功") : Result.error("题目添加失败");
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @DeleteMapping("/{paperId}/remove-question/{questionId}")
    @Operation(summary = "从试卷中移除题目", description = "从试卷中移除指定题目")
    public Result<String> removeQuestionFromPaper(
            @Parameter(description = "试卷ID", required = true) @PathVariable String paperId,
            @Parameter(description = "题目ID", required = true) @PathVariable String questionId) {
        try {
            LambdaQueryWrapper<MapPaperQuestion> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(MapPaperQuestion::getPaperId, paperId)
                   .eq(MapPaperQuestion::getQuestionId, questionId);

            boolean success = mapPaperQuestionService.remove(wrapper);
            return success ? Result.success("题目移除成功") : Result.error("题目移除失败");
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @PostMapping("/{paperId}/update-question-order")
    @Operation(summary = "更新试卷中题目的顺序", description = "批量更新试卷中题目的顺序")
    public Result<String> updateQuestionOrder(
            @Parameter(description = "试卷ID", required = true) @PathVariable String paperId,
            @RequestBody Map<String, Integer> orderMap) {
        try {
            // orderMap: {questionId: sortOrder}
            for (Map.Entry<String, Integer> entry : orderMap.entrySet()) {
                String questionId = entry.getKey();
                Integer sortOrder = entry.getValue();

                LambdaQueryWrapper<MapPaperQuestion> wrapper = new LambdaQueryWrapper<>();
                wrapper.eq(MapPaperQuestion::getPaperId, paperId)
                       .eq(MapPaperQuestion::getQuestionId, questionId);

                MapPaperQuestion mapping = mapPaperQuestionService.getOne(wrapper);
                if (mapping != null) {
                    mapping.setSortOrder(sortOrder);
                    mapPaperQuestionService.updateById(mapping);
                }
            }

            return Result.success("题目顺序更新成功");
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
}
