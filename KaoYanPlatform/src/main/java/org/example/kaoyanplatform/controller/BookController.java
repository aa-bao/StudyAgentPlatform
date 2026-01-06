package org.example.kaoyanplatform.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.example.kaoyanplatform.common.Result;
import org.example.kaoyanplatform.entity.Book;
import org.example.kaoyanplatform.entity.dto.BookDTO;
import org.example.kaoyanplatform.service.BookService;
import org.example.kaoyanplatform.service.MapSubjectBookService;
import org.example.kaoyanplatform.service.SubjectService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

/**
 * 习题册Controller
 * 使用映射表（map_subject_book）管理书本与科目的关系
 */
@RestController
@RequestMapping("/book")
public class BookController {

    @Autowired
    private BookService bookService;

    @Autowired
    private MapSubjectBookService mapSubjectBookService;

    @Autowired
    private SubjectService subjectService;

    /**
     * 获取所有书本列表
     */
    @GetMapping("/list")
    public Result getAllBooks() {
        List<Book> books = bookService.list();
        return Result.success(books);
    }

    /**
     * 新增习题册（包含关联关系）
     */
    @PostMapping("/add")
    public Result addBook(@RequestBody BookDTO bookDTO) {
        boolean success = bookService.saveBookWithRelations(bookDTO);
        return success ? Result.success("添加成功") : Result.error("添加失败");
    }

    /**
     * 更新习题册（包含关联关系）
     */
    @PostMapping("/update")
    public Result updateBook(@RequestBody BookDTO bookDTO) {
        boolean success = bookService.updateBookWithRelations(bookDTO);
        return success ? Result.success("修改成功") : Result.error("修改失败");
    }

    /**
     * 删除习题册（同时删除关联关系）
     */
    @DeleteMapping("/delete/{id}")
    public Result deleteBook(@PathVariable Integer id) {
        // 删除书本-科目关联
        mapSubjectBookService.removeAllSubjectBookRelations(id);
        // 删除书本
        bookService.removeById(id);
        return Result.success("删除成功");
    }

    /**
     * 根据ID获取习题册详情（包含关联关系）
     */
    @GetMapping("/{id}")
    public Result getBookById(@PathVariable Integer id) {
        Book book = bookService.getById(id);
        if (book == null) {
            return Result.error("习题册不存在");
        }

        // 获取关联的科目ID列表
        List<Integer> subjectIds = mapSubjectBookService.getSubjectIdsByBookId(id);
        book.setSubjectIds(subjectIds);
        // 保留向后兼容性
        book.setSubjectId(subjectIds != null && !subjectIds.isEmpty() ? subjectIds.get(0) : null);

        return Result.success(book);
    }

    /**
     * 分页查询习题册（支持科目筛选）
     */
    @GetMapping("/page")
    public Result findPage(@RequestParam Integer pageNum,
                           @RequestParam Integer pageSize,
                           @RequestParam(required = false) Integer subjectId) {
        Page<Book> page = new Page<>(pageNum, pageSize);
        Page<Book> result = bookService.bookPage(page, subjectId);
        return Result.success(result);
    }

    @GetMapping("/list-by-subject")
    public Result getBooksBySubject(@RequestParam Integer subjectId) {
        // 通过映射表查询指定科目下的所有书本ID
        List<Integer> bookIds = mapSubjectBookService.getBookIdsBySubjectId(subjectId);

        if (bookIds == null || bookIds.isEmpty()) {
            return Result.success(new ArrayList<>());
        }

        // 根据书本ID列表查询书本信息
        List<Book> books = bookService.listByIds(bookIds);
        return Result.success(books);
    }
}
