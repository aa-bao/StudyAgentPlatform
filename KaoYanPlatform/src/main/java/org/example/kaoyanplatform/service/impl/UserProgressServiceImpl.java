package org.example.kaoyanplatform.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.example.kaoyanplatform.entity.QuestionSubject;
import org.example.kaoyanplatform.entity.UserProgress;
import org.example.kaoyanplatform.mapper.QuestionSubjectMapper;
import org.example.kaoyanplatform.mapper.UserProgressMapper;
import org.example.kaoyanplatform.service.UserProgressService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class UserProgressServiceImpl extends ServiceImpl<UserProgressMapper, UserProgress> implements UserProgressService {

    @Autowired
    private QuestionSubjectMapper questionSubjectMapper;

    @Autowired
    private UserProgressMapper userProgressMapper;

    @Override
    @Transactional
    public void updateProgress(Long userId, Long questionId, boolean isCorrect) {
        // 1. Find all subjects related to this question
        QueryWrapper<QuestionSubject> qsWrapper = new QueryWrapper<>();
        qsWrapper.eq("question_id", questionId);
        List<QuestionSubject> relations = questionSubjectMapper.selectList(qsWrapper);

        if (relations == null || relations.isEmpty()) {
            return;
        }

        // 2. Update progress for each subject
        for (QuestionSubject rel : relations) {
            Integer subjectId = rel.getSubjectId();
            
            QueryWrapper<UserProgress> upWrapper = new QueryWrapper<>();
            upWrapper.eq("user_id", userId);
            upWrapper.eq("subject_id", subjectId);
            UserProgress progress = userProgressMapper.selectOne(upWrapper);

            if (progress == null) {
                progress = new UserProgress();
                progress.setUserId(userId);
                progress.setSubjectId(subjectId);
                progress.setFinishedCount(1);
                progress.setCorrectCount(isCorrect ? 1 : 0);
                progress.setUpdateTime(LocalDateTime.now());
                userProgressMapper.insert(progress);
            } else {
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
