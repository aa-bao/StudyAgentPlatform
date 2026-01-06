package org.example.kaoyanplatform.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.example.kaoyanplatform.entity.Book;
import org.example.kaoyanplatform.entity.MapSubjectBook;
import org.example.kaoyanplatform.entity.dto.BookDTO;
import org.example.kaoyanplatform.mapper.BookMapper;
import org.example.kaoyanplatform.mapper.MapSubjectBookMapper;
import org.example.kaoyanplatform.service.BookService;
import org.example.kaoyanplatform.service.MapSubjectBookService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 习题册Service实现类
 * 使用映射表（map_subject_book）管理书本与科目的关系
 */
@Service
public class BookServiceImpl extends ServiceImpl<BookMapper, Book> implements BookService {

    @Autowired
    private MapSubjectBookMapper mapSubjectBookMapper;

    @Autowired
    private MapSubjectBookService mapSubjectBookService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean saveBookWithRelations(BookDTO bookDTO) {
        // 1. 保存书本基本信息
        Book book = new Book();
        BeanUtils.copyProperties(bookDTO, book);
        book.setCreateTime(LocalDateTime.now());
        boolean saved = save(book);

        if (!saved) {
            return false;
        }

        Integer bookId = book.getId();

        // 2. 保存书本-科目关联关系
        if (bookDTO.getSubjectIds() != null && !bookDTO.getSubjectIds().isEmpty()) {
            for (Integer subjectId : bookDTO.getSubjectIds()) {
                MapSubjectBook relation = new MapSubjectBook();
                relation.setBookId(bookId);
                relation.setSubjectId(subjectId);
                mapSubjectBookMapper.insert(relation);
            }
        }

        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateBookWithRelations(BookDTO bookDTO) {
        if (bookDTO.getId() == null) {
            return false;
        }

        // 1. 更新书本基本信息
        Book book = new Book();
        BeanUtils.copyProperties(bookDTO, book);
        boolean updated = updateById(book);

        if (!updated) {
            return false;
        }

        Integer bookId = bookDTO.getId();

        // 2. 删除旧的书本-科目关联关系，重新建立
        mapSubjectBookService.removeAllSubjectBookRelations(bookId);
        if (bookDTO.getSubjectIds() != null && !bookDTO.getSubjectIds().isEmpty()) {
            for (Integer subjectId : bookDTO.getSubjectIds()) {
                mapSubjectBookService.addSubjectBookRelation(bookId, subjectId);
            }
        }

        return true;
    }

    @Override
    public Page<Book> bookPage(Page<Book> page, Integer subjectId) {
        QueryWrapper<Book> queryWrapper = new QueryWrapper<>();

        // 如果提供了subjectId，通过映射表查询
        if (subjectId != null) {
            List<Integer> bookIds = mapSubjectBookMapper.getBookIdsBySubjectId(subjectId);
            if (bookIds != null && !bookIds.isEmpty()) {
                queryWrapper.in("id", bookIds);
            } else {
                // 没有找到书本，返回空页
                return new Page<>(page.getCurrent(), page.getSize());
            }
        }

        queryWrapper.orderByDesc("id");
        return page(page, queryWrapper);
    }
}
