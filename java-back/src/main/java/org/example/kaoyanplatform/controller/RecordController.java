package org.example.kaoyanplatform.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.example.kaoyanplatform.common.Result;
import org.example.kaoyanplatform.entity.ExamRecord;
import org.example.kaoyanplatform.entity.Question;
import org.example.kaoyanplatform.entity.MistakeRecord;
import org.example.kaoyanplatform.service.QuestionService;
import org.example.kaoyanplatform.service.RecordService;
import org.example.kaoyanplatform.service.MistakeRecordService;
import org.example.kaoyanplatform.service.UserProgressService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Tag(name = "答题记录", description = "答题记录相关接口")
@RestController
@RequestMapping("/record")
public class RecordController {

    @Autowired
    private RecordService recordService;

    @Autowired
    private QuestionService questionService;

    @Autowired
    private MistakeRecordService mistakeRecordService;

    @Autowired
    private UserProgressService userProgressService;

    /**
     * 1. 提交答题并判分
     */
    @PostMapping("/submit")
    @Operation(summary = "提交答题", description = "提交答案并自动判分，自动记录错题，更新用户进度。")
    public Result submitRecord(@RequestBody ExamRecord examRecord) {
        if (examRecord.getUserId() == null || examRecord.getQuestionId() == null || examRecord.getUserAnswer() == null) {
            return Result.error("参数不完整（userId, questionId, userAnswer 必填）");
        }

        // 1. 查询题目
        Question question = questionService.getById(examRecord.getQuestionId());
        if (question == null) {
            return Result.error("题目不存在");
        }

        // 2. 判题逻辑：去除空格、转大写
        String dbAns = question.getAnswer().replaceAll("[,\\s]", "").toUpperCase();
        String userAns = examRecord.getUserAnswer().replaceAll("[,\\s]", "").toUpperCase();
        boolean isRight = dbAns.equals(userAns);

        examRecord.setIsCorrect(isRight ? 1 : 0);
        examRecord.setScore(isRight ? 5 : 0);
        examRecord.setCreateTime(LocalDateTime.now()); // 确保有记录时间

        // 3. 保存答题记录
        recordService.save(examRecord);

        // 4. 错题本逻辑
        if (!isRight) {
            updateMistakeBook(examRecord);
        }

        // 5. 更新用户学习进度
        userProgressService.updateProgress(examRecord.getUserId(), examRecord.getQuestionId(), isRight);

        // 6. 构造返回结果
        Map<String, Object> resMap = new HashMap<>();
        resMap.put("isCorrect", examRecord.getIsCorrect());
        resMap.put("correctAnswer", question.getAnswer());
        resMap.put("analysis", question.getAnalysis());

        return Result.success(resMap);
    }

    /**
     * 2. 获取个人统计数据
     */
    @GetMapping("/stats")
    @Operation(summary = "获取个人统计", description = "返回总题量、正确题量、正确率及总耗时。")
    public Result getStats(@Parameter(description = "用户ID") @RequestParam Integer userId) {
        if (userId == null) return Result.error("未传入用户ID");

        // 使用 Lambda 获取统计
        long total = recordService.count(new LambdaQueryWrapper<ExamRecord>().eq(ExamRecord::getUserId, userId));
        long correct = recordService.count(new LambdaQueryWrapper<ExamRecord>()
                .eq(ExamRecord::getUserId, userId)
                .eq(ExamRecord::getIsCorrect, 1));

        double accuracy = (total == 0) ? 0 : Math.round((double) correct / total * 100);

        // 计算总时长 (处理求和逻辑)
        QueryWrapper<ExamRecord> queryWrapper = new QueryWrapper<>();
        queryWrapper.select("IFNULL(sum(duration), 0) as totalDuration").eq("user_id", userId);
        Map<String, Object> sumMap = recordService.getMap(queryWrapper);
        Object totalTime = sumMap != null ? sumMap.get("totalDuration") : 0;

        Map<String, Object> map = new HashMap<>();
        map.put("total", total);
        map.put("correct", correct);
        map.put("accuracy", accuracy);
        map.put("totalTime", totalTime);

        return Result.success(map);
    }

    /**
     * 3. 获取最近动态
     */
    @GetMapping("/recent")
    @Operation(summary = "获取最近动态", description = "获取用户最近10条答题记录。")
    public Result getRecentRecords(@Parameter(description = "用户ID") @RequestParam Integer userId) {
        List<ExamRecord> list = recordService.list(new LambdaQueryWrapper<ExamRecord>()
                .eq(ExamRecord::getUserId, userId)
                .orderByDesc(ExamRecord::getCreateTime)
                .last("limit 10"));
        return Result.success(list);
    }

    /**
     * 4. 抽取私有方法：更新错题本
     */
    private void updateMistakeBook(ExamRecord examRecord) {
        LambdaQueryWrapper<MistakeRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(MistakeRecord::getUserId, examRecord.getUserId())
                .eq(MistakeRecord::getQuestionId, examRecord.getQuestionId());

        MistakeRecord exist = mistakeRecordService.getOne(wrapper);
        if (exist == null) {
            MistakeRecord wb = new MistakeRecord();
            wb.setUserId(examRecord.getUserId().intValue());
            wb.setQuestionId(examRecord.getQuestionId().intValue());
            wb.setErrorCount(1);
            wb.setUpdateTime(LocalDateTime.now());
            mistakeRecordService.save(wb);
        } else {
            exist.setErrorCount(exist.getErrorCount() + 1);
            exist.setUpdateTime(LocalDateTime.now());
            mistakeRecordService.updateById(exist);
        }
    }
}