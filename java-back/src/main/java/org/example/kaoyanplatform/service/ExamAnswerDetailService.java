package org.example.kaoyanplatform.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.example.kaoyanplatform.entity.ExamAnswerDetail;

import java.util.List;

public interface ExamAnswerDetailService extends IService<ExamAnswerDetail> {
    
    List<ExamAnswerDetail> getDetailsBySessionId(String sessionId);
}
