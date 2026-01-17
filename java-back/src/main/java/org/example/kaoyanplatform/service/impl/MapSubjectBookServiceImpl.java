package org.example.kaoyanplatform.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.example.kaoyanplatform.entity.MapSubjectBook;
import org.example.kaoyanplatform.mapper.MapSubjectBookMapper;
import org.example.kaoyanplatform.service.MapSubjectBookService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 书本-科目映射表Service实现类
 */
@Service
public class MapSubjectBookServiceImpl extends ServiceImpl<MapSubjectBookMapper, MapSubjectBook>
        implements MapSubjectBookService {

    @Autowired
    private MapSubjectBookMapper mapSubjectBookMapper;

    @Override
    public List<Integer> getSubjectIdsByBookId(Integer bookId) {
        return mapSubjectBookMapper.getSubjectIdsByBookId(bookId);
    }

    @Override
    public List<Integer> getBookIdsBySubjectId(Integer subjectId) {
        return mapSubjectBookMapper.getBookIdsBySubjectId(subjectId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean addSubjectBookRelation(Integer bookId, Integer subjectId) {
        // 检查是否已存在关联
        QueryWrapper<MapSubjectBook> wrapper = new QueryWrapper<>();
        wrapper.eq("book_id", bookId);
        wrapper.eq("subject_id", subjectId);
        if (count(wrapper) > 0) {
            return true; // 已存在，直接返回成功
        }

        MapSubjectBook relation = new MapSubjectBook();
        relation.setBookId(bookId);
        relation.setSubjectId(subjectId);
        return save(relation);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean batchAddSubjectBookRelations(Integer bookId, List<Integer> subjectIds) {
        if (subjectIds == null || subjectIds.isEmpty()) {
            return true;
        }

        // 先删除旧的关联
        removeAllSubjectBookRelations(bookId);

        // 批量添加新关联
        for (Integer subjectId : subjectIds) {
            MapSubjectBook relation = new MapSubjectBook();
            relation.setBookId(bookId);
            relation.setSubjectId(subjectId);
            if (!save(relation)) {
                return false;
            }
        }
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean removeSubjectBookRelation(Integer bookId, Integer subjectId) {
        QueryWrapper<MapSubjectBook> wrapper = new QueryWrapper<>();
        wrapper.eq("book_id", bookId);
        wrapper.eq("subject_id", subjectId);
        return remove(wrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean removeAllSubjectBookRelations(Integer bookId) {
        QueryWrapper<MapSubjectBook> wrapper = new QueryWrapper<>();
        wrapper.eq("book_id", bookId);
        return remove(wrapper);
    }
}
