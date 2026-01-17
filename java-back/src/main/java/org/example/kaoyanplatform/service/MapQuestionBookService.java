package org.example.kaoyanplatform.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.example.kaoyanplatform.entity.MapQuestionBook;

import java.util.List;

/**
 * 题目-书本映射表Service
 */
public interface MapQuestionBookService extends IService<MapQuestionBook> {

    /**
     * 根据题目ID获取书本ID列表
     * @param questionId 题目ID
     * @return 书本ID列表
     */
    List<Integer> getBookIdsByQuestionId(Long questionId);

    /**
     * 根据书本ID获取题目ID列表
     * @param bookId 书本ID
     * @return 题目ID列表
     */
    List<Long> getQuestionIdsByBookId(Integer bookId);

    /**
     * 为题目添加书本关联
     * @param questionId 题目ID
     * @param bookId 书本ID
     * @return 是否成功
     */
    boolean addQuestionBookRelation(Long questionId, Integer bookId);

    /**
     * 批量为题目添加书本关联
     * @param questionId 题目ID
     * @param bookIds 书本ID列表
     * @return 是否成功
     */
    boolean batchAddQuestionBookRelations(Long questionId, List<Integer> bookIds);

    /**
     * 删除题目的书本关联
     * @param questionId 题目ID
     * @param bookId 书本ID
     * @return 是否成功
     */
    boolean removeQuestionBookRelation(Long questionId, Integer bookId);

    /**
     * 删除题目的所有书本关联
     * @param questionId 题目ID
     * @return 是否成功
     */
    boolean removeAllQuestionBookRelations(Long questionId);
}
