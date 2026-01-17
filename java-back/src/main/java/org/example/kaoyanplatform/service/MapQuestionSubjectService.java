package org.example.kaoyanplatform.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.example.kaoyanplatform.entity.MapQuestionSubject;

import java.util.List;

/**
 * 题目-科目映射表Service
 */
public interface MapQuestionSubjectService extends IService<MapQuestionSubject> {

    /**
     * 根据题目ID获取科目ID列表
     * @param questionId 题目ID
     * @return 科目ID列表
     */
    List<Integer> getSubjectIdsByQuestionId(Long questionId);

    /**
     * 根据科目ID获取题目ID列表
     * @param subjectId 科目ID
     * @return 题目ID列表
     */
    List<Long> getQuestionIdsBySubjectId(Integer subjectId);

    /**
     * 为题目添加科目关联
     * @param questionId 题目ID
     * @param subjectId 科目ID
     * @return 是否成功
     */
    boolean addQuestionSubjectRelation(Long questionId, Integer subjectId);

    /**
     * 批量为题目添加科目关联
     * @param questionId 题目ID
     * @param subjectIds 科目ID列表
     * @return 是否成功
     */
    boolean batchAddQuestionSubjectRelations(Long questionId, List<Integer> subjectIds);

    /**
     * 删除题目的科目关联
     * @param questionId 题目ID
     * @param subjectId 科目ID
     * @return 是否成功
     */
    boolean removeQuestionSubjectRelation(Long questionId, Integer subjectId);

    /**
     * 删除题目的所有科目关联
     * @param questionId 题目ID
     * @return 是否成功
     */
    boolean removeAllQuestionSubjectRelations(Long questionId);
}
