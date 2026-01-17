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
import org.example.kaoyanplatform.entity.dto.HomePageDataDTO;
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


    @Override
    public HomePageDataDTO getHomePageData(Long userId) {
        HomePageDataDTO homeData = new HomePageDataDTO();

        // 1. 获取用户基本信息
        User user = getById(userId);
        if (user == null) {
            return null;
        }

        HomePageDataDTO.UserInfo userInfo = new HomePageDataDTO.UserInfo();
        userInfo.setId(user.getId());
        userInfo.setUsername(user.getUsername());
        userInfo.setNickname(user.getNickname());
        userInfo.setAvatar(user.getAvatar());
        userInfo.setRole(user.getRole());
        userInfo.setTargetSchool(user.getTargetSchool());
        userInfo.setTargetTotalScore(user.getTargetTotalScore());
        userInfo.setExamYear(user.getExamYear());
        userInfo.setExamDate("2025-12-20"); // 默认考试日期
        userInfo.setExamSubjects(user.getExamSubjects());
        homeData.setUserInfo(userInfo);

        // 2. 获取学习统计数据
        HomePageDataDTO.StudyStats stats = new HomePageDataDTO.StudyStats();

        // 获取总刷题数
        QueryWrapper<UserProgress> progressWrapper = new QueryWrapper<>();
        progressWrapper.eq("user_id", userId);
        List<UserProgress> progressList = userProgressMapper.selectList(progressWrapper);

        int totalQuestions = progressList.stream()
                .mapToInt(p -> p.getFinishedCount() != null ? p.getFinishedCount() : 0)
                .sum();
        stats.setQuestionsDone(totalQuestions);

        // 获取总正确数，计算正确率
        int totalCorrect = progressList.stream()
                .mapToInt(p -> p.getCorrectCount() != null ? p.getCorrectCount() : 0)
                .sum();
        double accuracy = totalQuestions > 0 ? (totalCorrect * 100.0 / totalQuestions) : 0.0;
        stats.setAccuracy(Math.round(accuracy * 10.0) / 10.0);

        // 获取错题本数量
        // 这里需要注入MistakeRecordMapper，暂时用模拟数据
        stats.setMistakesCount(0);

        // 学习时长（暂时用模拟数据，后续可以添加字段）
        stats.setTodayStudyTime(0.0);
        stats.setTotalStudyHours(0.0);
        stats.setConsecutiveDays(0);

        homeData.setStudyStats(stats);

        // 3. 获取科目列表（Level 1 - 考试规格）
        QueryWrapper<Subject> subjectWrapper = new QueryWrapper<>();
        subjectWrapper.eq("level", "1");
        subjectWrapper.orderByAsc("sort");
        List<Subject> subjects = subjectMapper.selectList(subjectWrapper);

        List<HomePageDataDTO.Subject> subjectList = subjects.stream()
                .map(s -> {
                    HomePageDataDTO.Subject dto = new HomePageDataDTO.Subject();
                    dto.setId(s.getId());
                    dto.setName(s.getName());
                    dto.setLevel(Integer.parseInt(s.getLevel()));
                    return dto;
                })
                .collect(Collectors.toList());
        homeData.setSubjects(subjectList);

        // 4. 每日励志语录（暂时硬编码，后续可以做成随机或按日期获取）
        HomePageDataDTO.DailyQuote quote = new HomePageDataDTO.DailyQuote();
        quote.setContent("不积跬步，无以至千里；不积小流，无以成江海。");
        quote.setAuthor("荀子");
        homeData.setDailyQuote(quote);

        // 5. 个性化推荐（暂时返回空列表，后续可以根据算法实现）
        homeData.setRecommendations(new ArrayList<>());

        return homeData;
    }

}