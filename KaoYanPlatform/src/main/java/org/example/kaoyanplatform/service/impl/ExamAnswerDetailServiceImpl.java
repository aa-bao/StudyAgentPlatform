package org.example.kaoyanplatform.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.example.kaoyanplatform.entity.ExamAnswerDetail;
import org.example.kaoyanplatform.mapper.ExamAnswerDetailMapper;
import org.example.kaoyanplatform.service.ExamAnswerDetailService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ExamAnswerDetailServiceImpl extends ServiceImpl<ExamAnswerDetailMapper, ExamAnswerDetail> implements ExamAnswerDetailService {

    @Override
    public List<ExamAnswerDetail> getDetailsBySessionId(String sessionId) {
        LambdaQueryWrapper<ExamAnswerDetail> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ExamAnswerDetail::getSessionId, sessionId);
        return list(wrapper);
    }
}
