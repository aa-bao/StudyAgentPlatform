package org.example.kaoyanplatform.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.example.kaoyanplatform.common.Result;
import org.example.kaoyanplatform.entity.ExamRecord;
import org.example.kaoyanplatform.entity.Question;
import org.example.kaoyanplatform.service.QuestionService;
import org.example.kaoyanplatform.service.RecordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.example.kaoyanplatform.entity.MistakeRecord;
import org.example.kaoyanplatform.service.MistakeRecordService;
import org.example.kaoyanplatform.service.UserProgressService;

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
     * 提交答题并判分
     */
    @PostMapping("/submit")
    public Result submitRecord(@RequestBody ExamRecord examRecord) {
        if (examRecord.getUserId() == null || examRecord.getQuestionId() == null) {
            return Result.error("参数不完整");
        }

        // 1. 查询题目获取正确答案
        Question question = questionService.getById(examRecord.getQuestionId());
        if (question == null) {
            return Result.error("题目不存在");
        }

        // 2. 判题逻辑
        String dbAns = question.getAnswer().replaceAll("[,\\s]", "").toUpperCase();
        String userAns = examRecord.getUserAnswer().replaceAll("[,\\s]", "").toUpperCase();
        boolean isRight = dbAns.equals(userAns);

        examRecord.setIsCorrect(isRight ? 1 : 0);
        examRecord.setScore(isRight ? 5 : 0);

        // 3. 保存答题记录
        recordService.save(examRecord);

        // 4. 处理错题本逻辑
        // 注意：isRight 为 false 时是错题
        if (!isRight) {
            LambdaQueryWrapper<MistakeRecord> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(MistakeRecord::getUserId, examRecord.getUserId())
                    .eq(MistakeRecord::getQuestionId, examRecord.getQuestionId());

            MistakeRecord exist = mistakeRecordService.getOne(wrapper);
            if (exist == null) {
                // 第一次做错，新增
                MistakeRecord wb = new MistakeRecord();
                wb.setUserId(examRecord.getUserId().intValue());
                wb.setQuestionId(examRecord.getQuestionId().intValue());
                wb.setErrorCount(1);
                wb.setUpdateTime(java.time.LocalDateTime.now());
                mistakeRecordService.save(wb);
            } else {
                // 已经存在，错误次数 +1
                exist.setErrorCount(exist.getErrorCount() + 1);
                exist.setUpdateTime(java.time.LocalDateTime.now());
                mistakeRecordService.updateById(exist);
            }
        }
        
        // 5. 更新用户学习进度
        userProgressService.updateProgress(examRecord.getUserId(), examRecord.getQuestionId(), isRight);
        
        // 6. 返回结果 (包含正确答案和解析，供前端展示)
        Map<String, Object> resMap = new HashMap<>();
        resMap.put("isCorrect", examRecord.getIsCorrect());
        resMap.put("correctAnswer", question.getAnswer());
        resMap.put("analysis", question.getAnalysis());
        // resMap.put("newDifficulty", question.getDifficulty()); // 难度评分功能已移除

        return Result.success(resMap);
    }

    /**
     * 保存答题记录 (保留旧接口兼容)
     */
    @PostMapping("/add")
    public Result addRecord(@RequestBody ExamRecord examRecord) {
        if (examRecord.getUserId() == null) {
            return Result.error("用户ID不能为空");
        }
        recordService.save(examRecord);
        return Result.success("记录保存成功");
    }

    /**
     * 获取个人统计数据
     */
    @GetMapping("/stats")
    public Result getStats(@RequestParam Integer userId) {
        if (userId == null)
            return Result.error("未传入用户ID");

        // 1. 总题量
        long total = recordService.count(new LambdaQueryWrapper<ExamRecord>()
                .eq(ExamRecord::getUserId, userId));

        // 2. 正确题量
        long correct = recordService.count(new LambdaQueryWrapper<ExamRecord>()
                .eq(ExamRecord::getUserId, userId)
                .eq(ExamRecord::getIsCorrect, 1));

        // 3. 计算正确率
        double accuracy = (total == 0) ? 0 : Math.round((double) correct / total * 100);

        // 4. 计算总时长
        QueryWrapper<ExamRecord> queryWrapper = new QueryWrapper<>();
        queryWrapper.select("sum(duration) as totalDuration").eq("user_id", userId);
        Map<String, Object> sumMap = recordService.getMap(queryWrapper);
        Object totalTime = (sumMap != null && sumMap.get("totalDuration") != null) ? sumMap.get("totalDuration") : 0;

        Map<String, Object> map = new HashMap<>();
        map.put("total", total);
        map.put("correct", correct);
        map.put("accuracy", accuracy);
        map.put("totalTime", totalTime);

        return Result.success(map);
    }

    /**
     * 获取最近动态
     */
    @GetMapping("/recent")
    public Result getRecentRecords(@RequestParam Integer userId) {
        List<ExamRecord> list = recordService.list(new LambdaQueryWrapper<ExamRecord>()
                .eq(ExamRecord::getUserId, userId)
                .orderByDesc(ExamRecord::getCreateTime)
                .last("limit 10"));
        return Result.success(list);
    }
}
