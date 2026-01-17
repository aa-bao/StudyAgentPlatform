package org.example.kaoyanplatform.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.example.kaoyanplatform.entity.Book;
import org.example.kaoyanplatform.entity.MapSubjectBook;
import org.example.kaoyanplatform.entity.Subject;
import org.example.kaoyanplatform.entity.dto.BookDTO;
import org.example.kaoyanplatform.mapper.BookMapper;
import org.example.kaoyanplatform.mapper.MapSubjectBookMapper;
import org.example.kaoyanplatform.mapper.SubjectMapper;
import org.example.kaoyanplatform.service.BookService;
import org.example.kaoyanplatform.service.MapSubjectBookService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 习题册Service实现类
 * 使用映射表（map_subject_book）管理书本与科目的关系
 */
@Service
public class BookServiceImpl extends ServiceImpl<BookMapper, Book> implements BookService {

    private static final Logger log = LoggerFactory.getLogger(BookServiceImpl.class);

    @Autowired
    private MapSubjectBookMapper mapSubjectBookMapper;

    @Autowired
    private MapSubjectBookService mapSubjectBookService;

    @Autowired
    private SubjectMapper subjectMapper;

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
    public Page<Book> bookPage(Page<Book> page, List<Integer> subjectIds) {
        QueryWrapper<Book> queryWrapper = new QueryWrapper<>();

        // 如果提供了subjectIds（多个科目），通过映射表查询
        if (subjectIds != null && !subjectIds.isEmpty()) {
            // 查询与任一科目关联的书本
            Set<Integer> bookIds = new HashSet<>();
            for (Integer subjectId : subjectIds) {
                List<Integer> ids = mapSubjectBookMapper.getBookIdsBySubjectId(subjectId);
                if (ids != null && !ids.isEmpty()) {
                    bookIds.addAll(ids);
                }
            }
            if (!bookIds.isEmpty()) {
                queryWrapper.in("id", bookIds);
            } else {
                // 没有找到书本，返回空页
                return new Page<>(page.getCurrent(), page.getSize());
            }
        }

        queryWrapper.orderByDesc("id");
        Page<Book> resultPage = page(page, queryWrapper);
        // 填充每条记录的关联信息
        fillBookRelationsBatch(resultPage.getRecords());
        return resultPage;
    }

    /**
     * 填充单本书本的关联信息（subjectIds, subjectId, subjectName, subjectNames）
     * @param book 书本对象
     */
    private void fillBookRelations(Book book) {
        if (book == null) return;

        log.debug("开始填充书本关联信息，bookId={}", book.getId());

        // 1. 获取关联的科目ID列表
        List<Integer> subjectIds = mapSubjectBookService.getSubjectIdsByBookId(book.getId());
        book.setSubjectIds(subjectIds);
        log.debug("bookId={} 的 subjectIds={}", book.getId(), subjectIds);

        // 2. 设置主科目ID（兼容旧字段）
        if (subjectIds != null && !subjectIds.isEmpty()) {
            book.setSubjectId(subjectIds.get(0));

            // 3. 获取所有科目名称
            List<String> subjectNames = new ArrayList<>();
            for (Integer subjectId : subjectIds) {
                Subject subject = subjectMapper.selectById(subjectId);
                if (subject != null) {
                    subjectNames.add(subject.getName());
                    log.debug("subjectId={} 对应的 subjectName={}", subjectId, subject.getName());
                }
            }
            book.setSubjectNames(subjectNames);
            log.debug("bookId={} 的 subjectNames={}", book.getId(), subjectNames);

            // 4. 设置主科目名称（兼容旧字段，取第一个）
            if (!subjectNames.isEmpty()) {
                book.setSubjectName(subjectNames.get(0));
            }
        }
    }

    /**
     * 批量填充书本列表的关联信息
     * @param books 书本列表
     */
    private void fillBookRelationsBatch(List<Book> books) {
        if (books == null || books.isEmpty()) return;

        for (Book book : books) {
            fillBookRelations(book);
        }
    }

    /**
     * 获取所有书本列表（带关联信息）
     * @return 带关联信息的书本列表
     */
    public List<Book> listWithRelations() {
        List<Book> books = list();
        fillBookRelationsBatch(books);
        return books;
    }
}
