package org.example.kaoyanplatform.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.example.kaoyanplatform.entity.MapQuestionSubject;
import org.example.kaoyanplatform.entity.UserProgress;
import org.example.kaoyanplatform.mapper.MapQuestionSubjectMapper;
import org.example.kaoyanplatform.mapper.UserProgressMapper;
import org.example.kaoyanplatform.service.UserProgressService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 用户学习进度Service实现类
 * 使用映射表（map_question_subject）管理题目与科目的关系
 */
@Service
public class UserProgressServiceImpl extends ServiceImpl<UserProgressMapper, UserProgress> implements UserProgressService {

    @Autowired
    private MapQuestionSubjectMapper mapQuestionSubjectMapper;

    @Autowired
    private UserProgressMapper userProgressMapper;

    @Override
    @Transactional
    public void updateProgress(Long userId, Long questionId, boolean isCorrect) {
        // 1. 使用新的映射表查找与该题目相关的所有科目
        QueryWrapper<MapQuestionSubject> qsWrapper = new QueryWrapper<>();
        qsWrapper.eq("question_id", questionId);
        List<MapQuestionSubject> relations = mapQuestionSubjectMapper.selectList(qsWrapper);

        if (relations == null || relations.isEmpty()) {
            return;
        }

        // 2. 更新每个科目的学习进度
        for (MapQuestionSubject rel : relations) {
            Integer subjectId = rel.getSubjectId();

            QueryWrapper<UserProgress> upWrapper = new QueryWrapper<>();
            upWrapper.eq("user_id", userId);
            upWrapper.eq("subject_id", subjectId);
            UserProgress progress = userProgressMapper.selectOne(upWrapper);

            if (progress == null) {
                // 首次在该科目下做题，创建进度记录
                progress = new UserProgress();
                progress.setUserId(userId);
                progress.setSubjectId(subjectId);
                progress.setFinishedCount(1);
                progress.setCorrectCount(isCorrect ? 1 : 0);
                progress.setUpdateTime(LocalDateTime.now());
                userProgressMapper.insert(progress);
            } else {
                // 更新已有进度
                progress.setFinishedCount(progress.getFinishedCount() + 1);
                if (isCorrect) {
                    progress.setCorrectCount(progress.getCorrectCount() + 1);
                }
                progress.setUpdateTime(LocalDateTime.now());
                userProgressMapper.updateById(progress);
            }
        }
    }
}
