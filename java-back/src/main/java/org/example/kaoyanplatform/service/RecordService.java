package org.example.kaoyanplatform.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.example.kaoyanplatform.entity.ExamRecord; // 注意检查你的实体类名是 Record 还是 ExamRecord

/**
 * 答题记录服务类
 */
public interface RecordService extends IService<ExamRecord> {
    // 如果有特殊的统计逻辑，可以在这里定义
}