package org.example.kaoyanplatform.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.example.kaoyanplatform.entity.UserProgress;

public interface UserProgressService extends IService<UserProgress> {
    void updateProgress(Long userId, Long questionId, boolean isCorrect);
}
