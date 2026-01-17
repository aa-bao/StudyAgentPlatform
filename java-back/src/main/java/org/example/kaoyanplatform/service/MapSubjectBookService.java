package org.example.kaoyanplatform.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.example.kaoyanplatform.entity.MapSubjectBook;

import java.util.List;

/**
 * 书本-科目映射表Service
 */
public interface MapSubjectBookService extends IService<MapSubjectBook> {

    /**
     * 根据书本ID获取科目ID列表
     * @param bookId 书本ID
     * @return 科目ID列表
     */
    List<Integer> getSubjectIdsByBookId(Integer bookId);

    /**
     * 根据科目ID获取书本ID列表
     * @param subjectId 科目ID
     * @return 书本ID列表
     */
    List<Integer> getBookIdsBySubjectId(Integer subjectId);

    /**
     * 为书本添加科目关联
     * @param bookId 书本ID
     * @param subjectId 科目ID
     * @return 是否成功
     */
    boolean addSubjectBookRelation(Integer bookId, Integer subjectId);

    /**
     * 批量为书本添加科目关联
     * @param bookId 书本ID
     * @param subjectIds 科目ID列表
     * @return 是否成功
     */
    boolean batchAddSubjectBookRelations(Integer bookId, List<Integer> subjectIds);

    /**
     * 删除书本的科目关联
     * @param bookId 书本ID
     * @param subjectId 科目ID
     * @return 是否成功
     */
    boolean removeSubjectBookRelation(Integer bookId, Integer subjectId);

    /**
     * 删除书本的所有科目关联
     * @param bookId 书本ID
     * @return 是否成功
     */
    boolean removeAllSubjectBookRelations(Integer bookId);
}
