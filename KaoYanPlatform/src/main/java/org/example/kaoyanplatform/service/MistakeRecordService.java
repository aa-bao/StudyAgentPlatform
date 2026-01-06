package org.example.kaoyanplatform.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.example.kaoyanplatform.entity.MistakeRecord;
import org.example.kaoyanplatform.entity.dto.MistakeHeatmapDTO;

import java.util.List;

/**
 * 错题记录Service接口
 */
public interface MistakeRecordService extends IService<MistakeRecord> {

    /**
     * 获取错题热力统计（按科目聚合）
     * @return 科目错题统计列表
     */
    List<MistakeHeatmapDTO> getMistakeHeatmap();

    /**
     * 获取全局错题 TOP N
     * @param limit 返回数量
     * @return 高频错题列表
     */
    List<MistakeHeatmapDTO.HotMistakeQuestion> getHotMistakeQuestions(Integer limit);
}
