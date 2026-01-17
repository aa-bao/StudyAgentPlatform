package org.example.kaoyanplatform.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.example.kaoyanplatform.entity.ExamSession;
import org.example.kaoyanplatform.entity.dto.ExamStartDTO;

public interface ExamSessionService extends IService<ExamSession> {

    /**
     * 开始考试或恢复未完成的考试
     * 如果用户有该试卷的未完成会话（status=0），则恢复该会话
     * 否则创建新会话
     */
    ExamStartDTO startOrResumeExam(String userId, String paperId);

    boolean saveSnapshot(String sessionId, String snapshotJson);

    boolean recordSwitch(String sessionId);

    void submitExam(String sessionId);

    /**
     * 异步批改主观题
     * 在后台线程中执行，不会阻塞主流程
     */
    void gradeSubjectiveQuestionsAsync(String sessionId);
}
