package org.example.kaoyanplatform.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.example.kaoyanplatform.entity.MapQuestionBook;
import org.example.kaoyanplatform.mapper.MapQuestionBookMapper;
import org.example.kaoyanplatform.service.MapQuestionBookService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 题目-书本映射表Service实现类
 */
@Service
public class MapQuestionBookServiceImpl extends ServiceImpl<MapQuestionBookMapper, MapQuestionBook>
        implements MapQuestionBookService {

    @Autowired
    private MapQuestionBookMapper mapQuestionBookMapper;

    @Override
    public List<Integer> getBookIdsByQuestionId(Long questionId) {
        return mapQuestionBookMapper.getBookIdsByQuestionId(questionId);
    }

    @Override
    public List<Long> getQuestionIdsByBookId(Integer bookId) {
        return mapQuestionBookMapper.getQuestionIdsByBookId(bookId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean addQuestionBookRelation(Long questionId, Integer bookId) {
        // 检查是否已存在关联
        QueryWrapper<MapQuestionBook> wrapper = new QueryWrapper<>();
        wrapper.eq("question_id", questionId);
        wrapper.eq("book_id", bookId);
        if (count(wrapper) > 0) {
            return true; // 已存在，直接返回成功
        }

        MapQuestionBook relation = new MapQuestionBook();
        relation.setQuestionId(questionId);
        relation.setBookId(bookId);
        return save(relation);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean batchAddQuestionBookRelations(Long questionId, List<Integer> bookIds) {
        if (bookIds == null || bookIds.isEmpty()) {
            return true;
        }

        // 先删除旧的关联
        removeAllQuestionBookRelations(questionId);

        // 批量添加新关联
        for (Integer bookId : bookIds) {
            MapQuestionBook relation = new MapQuestionBook();
            relation.setQuestionId(questionId);
            relation.setBookId(bookId);
            if (!save(relation)) {
                return false;
            }
        }
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean removeQuestionBookRelation(Long questionId, Integer bookId) {
        QueryWrapper<MapQuestionBook> wrapper = new QueryWrapper<>();
        wrapper.eq("question_id", questionId);
        wrapper.eq("book_id", bookId);
        return remove(wrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean removeAllQuestionBookRelations(Long questionId) {
        QueryWrapper<MapQuestionBook> wrapper = new QueryWrapper<>();
        wrapper.eq("question_id", questionId);
        return remove(wrapper);
    }
}
