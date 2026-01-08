package org.example.kaoyanplatform.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.example.kaoyanplatform.entity.ExamSession;
import org.example.kaoyanplatform.entity.dto.ExamStartDTO;

public interface ExamSessionService extends IService<ExamSession> {

    ExamStartDTO startExam(String userId, String paperId);

    boolean saveSnapshot(String sessionId, String snapshotJson);

    boolean recordSwitch(String sessionId);

    void submitExam(String sessionId);
}
