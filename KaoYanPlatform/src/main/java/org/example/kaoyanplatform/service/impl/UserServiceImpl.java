package org.example.kaoyanplatform.service.impl;

import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.digest.BCrypt;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.example.kaoyanplatform.entity.MapQuestionSubject;
import org.example.kaoyanplatform.entity.Subject;
import org.example.kaoyanplatform.entity.User;
import org.example.kaoyanplatform.entity.UserProgress;
import org.example.kaoyanplatform.entity.dto.UserStudyStatsDTO;
import org.example.kaoyanplatform.mapper.MapQuestionSubjectMapper;
import org.example.kaoyanplatform.mapper.SubjectMapper;
import org.example.kaoyanplatform.mapper.UserMapper;
import org.example.kaoyanplatform.mapper.UserProgressMapper;
import org.example.kaoyanplatform.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 用户服务实现类
 */
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    @Autowired
    private UserProgressMapper userProgressMapper;

    @Autowired
    private MapQuestionSubjectMapper mapQuestionSubjectMapper;

    @Autowired
    private SubjectMapper subjectMapper;

    /**
     * 实现登录逻辑
     */
    @Override
    public User login(String username, String password) {
        // 1. 根据用户名查询用户
        User user = getOne(new LambdaQueryWrapper<User>().eq(User::getUsername, username));

        // 2. 校验密码 (使用 BCrypt 校验)
        if (user != null && BCrypt.checkpw(password, user.getPassword())) {
            // 为了安全，返回前隐藏密码
            user.setPassword(null);
            return user;
        }
        return null;
    }

    /**
     * 实现注册逻辑
     */
    @Override
    public boolean register(User user) {
        // 1. 检查用户名是否存在
        User existUser = getOne(new LambdaQueryWrapper<User>().eq(User::getUsername, user.getUsername()));
        if (existUser != null) {
            return false;
        }

        // 2. 设置默认昵称
        if (StrUtil.isBlank(user.getNickname())) {
            user.setNickname("考研人_" + RandomUtil.randomString(6));
        }

        // 3. 密码加密 (BCrypt)
        String hashedPwd = BCrypt.hashpw(user.getPassword(), BCrypt.gensalt());
        user.setPassword(hashedPwd);

        // 4. 设置初始角色为学生
        user.setRole("student");

        // 5. 设置默认考研年份，确保 Dashboard 进度条有终点数据
        // 注意：请根据你 User 实体类中的实际字段名（exam_year 或 examYear）进行调整
        if (StrUtil.isBlank(user.getExamYear())) {
            user.setExamYear("2027");
        }

        // create_time 将由 MyMetaObjectHandler 自动填充
        return save(user);
    }

    @Override
    public UserStudyStatsDTO getUserStudyStats(Long userId) {
        // 1. 查询用户基本信息
        User user = getById(userId);
        if (user == null) {
            return null;
        }

        UserStudyStatsDTO statsDTO = new UserStudyStatsDTO();
        statsDTO.setUserId(user.getId());
        statsDTO.setUsername(user.getUsername());
        statsDTO.setNickname(user.getNickname());
        statsDTO.setTargetSchool(user.getTargetSchool());
        statsDTO.setExamYear(user.getExamYear());

        // 2. 查询用户的学习进度记录
        QueryWrapper<UserProgress> progressWrapper = new QueryWrapper<>();
        progressWrapper.eq("user_id", userId);
        List<UserProgress> progressList = userProgressMapper.selectList(progressWrapper);

        // 3. 获取所有顶级科目（level = 1）
        QueryWrapper<Subject> subjectWrapper = new QueryWrapper<>();
        subjectWrapper.eq("level", "1");
        List<Subject> topSubjects = subjectMapper.selectList(subjectWrapper);

        // 4. 构建科目统计数据
        List<UserStudyStatsDTO.SubjectStats> subjectStatsList = new ArrayList<>();

        // 获取每个科目的题目总数
        Map<Integer, Integer> subjectQuestionCount = getSubjectQuestionCount();

        // 处理用户进度数据
        Map<Integer, UserProgress> progressMap = progressList.stream()
                .collect(Collectors.toMap(UserProgress::getSubjectId, p -> p, (p1, p2) -> p1));

        for (Subject subject : topSubjects) {
            UserStudyStatsDTO.SubjectStats subjectStats = new UserStudyStatsDTO.SubjectStats();
            subjectStats.setSubjectId(subject.getId());
            subjectStats.setSubjectName(subject.getName());

            UserProgress progress = progressMap.get(subject.getId());
            if (progress != null) {
                subjectStats.setFinishedCount(progress.getFinishedCount());
                subjectStats.setCorrectCount(progress.getCorrectCount());

                // 计算正确率
                if (progress.getFinishedCount() != null && progress.getFinishedCount() > 0) {
                    double accuracy = (progress.getCorrectCount() * 100.0) / progress.getFinishedCount();
                    subjectStats.setAccuracy(Math.round(accuracy * 100.0) / 100.0); // 保留两位小数
                }
            } else {
                subjectStats.setFinishedCount(0);
                subjectStats.setCorrectCount(0);
                subjectStats.setAccuracy(0.0);
            }

            // 计算覆盖度
            Integer totalCount = subjectQuestionCount.get(subject.getId());
            subjectStats.setTotalCount(totalCount != null ? totalCount : 0);

            if (totalCount != null && totalCount > 0) {
                double coverage = (subjectStats.getFinishedCount() * 100.0) / totalCount;
                subjectStats.setCoverage(Math.round(coverage * 100.0) / 100.0);
            } else {
                subjectStats.setCoverage(0.0);
            }

            subjectStatsList.add(subjectStats);
        }

        statsDTO.setSubjectStats(subjectStatsList);

        // 5. 计算总体统计
        UserStudyStatsDTO.OverallStats overallStats = new UserStudyStatsDTO.OverallStats();
        int totalFinished = subjectStatsList.stream().mapToInt(UserStudyStatsDTO.SubjectStats::getFinishedCount).sum();
        int totalCorrect = subjectStatsList.stream().mapToInt(UserStudyStatsDTO.SubjectStats::getCorrectCount).sum();

        overallStats.setTotalFinished(totalFinished);
        overallStats.setTotalCorrect(totalCorrect);

        if (totalFinished > 0) {
            double overallAccuracy = (totalCorrect * 100.0) / totalFinished;
            overallStats.setOverallAccuracy(Math.round(overallAccuracy * 100.0) / 100.0);
        } else {
            overallStats.setOverallAccuracy(0.0);
        }

        // 活跃度：最近7天完成的题目数（简化处理，这里使用总完成数）
        overallStats.setActivityScore(totalFinished);

        statsDTO.setOverallStats(overallStats);

        return statsDTO;
    }

    /**
     * 获取每个科目的题目总数
     */
    private Map<Integer, Integer> getSubjectQuestionCount() {
        QueryWrapper<MapQuestionSubject> wrapper = new QueryWrapper<>();
        wrapper.select("subject_id", "count(*) as count");
        wrapper.groupBy("subject_id");
        List<Map<String, Object>> resultList = mapQuestionSubjectMapper.selectMaps(wrapper);

        return resultList.stream()
                .collect(Collectors.toMap(
                        map -> ((Number) map.get("subject_id")).intValue(),
                        map -> ((Number) map.get("count")).intValue()
                ));
    }
}
