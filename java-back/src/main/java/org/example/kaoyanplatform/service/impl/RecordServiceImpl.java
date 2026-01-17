package org.example.kaoyanplatform.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.example.kaoyanplatform.entity.ExamRecord;
import org.example.kaoyanplatform.mapper.ExamRecordMapper;
import org.example.kaoyanplatform.service.RecordService;
import org.springframework.stereotype.Service;

/**
 * 答题记录服务实现类
 */
@Service
public class RecordServiceImpl extends ServiceImpl<ExamRecordMapper, ExamRecord> implements RecordService {
}