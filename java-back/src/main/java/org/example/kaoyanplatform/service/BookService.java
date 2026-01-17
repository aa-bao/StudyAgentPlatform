package org.example.kaoyanplatform.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import org.example.kaoyanplatform.entity.Book;
import org.example.kaoyanplatform.entity.dto.BookDTO;

/**
 * 习题册Service接口
 * 使用映射表（map_subject_book）管理书本与科目的关系
 */
public interface BookService extends IService<Book> {

    /**
     * 新增习题册（包含关联关系）
     */
    boolean saveBookWithRelations(BookDTO bookDTO);

    /**
     * 更新习题册（包含关联关系）
     */
    boolean updateBookWithRelations(BookDTO bookDTO);

    /**
     * 分页查询习题册（支持科目筛选）
     */
    Page<Book> bookPage(Page<Book> page, java.util.List<Integer> subjectIds);

    /**
     * 获取所有书本列表（带关联信息）
     * @return 带关联信息的书本列表
     */
    java.util.List<Book> listWithRelations();
}
