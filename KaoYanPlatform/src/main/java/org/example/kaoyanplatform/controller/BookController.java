package org.example.kaoyanplatform.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.example.kaoyanplatform.common.Result;
import org.example.kaoyanplatform.entity.Book;
import org.example.kaoyanplatform.entity.dto.BookDTO;
import org.example.kaoyanplatform.service.BookService;
import org.example.kaoyanplatform.service.MapSubjectBookService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@Tag(name = "习题册管理", description = "习题册增删改查接口")
@RestController
@RequestMapping("/book")
public class BookController {

    @Autowired
    private BookService bookService;

    @Autowired
    private MapSubjectBookService mapSubjectBookService;

    // 1. 获取所有书本列表
    @GetMapping("/list")
    @Operation(summary = "获取所有习题册", description = "获取系统中所有习题册列表，无分页。")
    public Result getAllBooks() {
        return Result.success(bookService.list());
    }

    // 2. 新增习题册
    @PostMapping("/add")
    @Operation(summary = "新增习题册", description = "新增习题册并建立与科目的关联。要求传入 subjectIds 列表。")
    public Result addBook(@RequestBody BookDTO bookDTO) {
        if (bookDTO.getName() == null || bookDTO.getSubjectIds() == null) {
            return Result.error("名称或关联科目不能为空");
        }
        boolean success = bookService.saveBookWithRelations(bookDTO);
        return success ? Result.success("添加成功") : Result.error("添加失败");
    }

    // 3. 更新习题册
    @PostMapping("/update")
    @Operation(summary = "更新习题册", description = "更新信息及其关联科目。必须包含 id。")
    public Result updateBook(@RequestBody BookDTO bookDTO) {
        if (bookDTO.getId() == null) {
            return Result.error("习题册ID不能为空");
        }
        boolean success = bookService.updateBookWithRelations(bookDTO);
        return success ? Result.success("修改成功") : Result.error("修改失败");
    }

    // 4. 删除习题册
    @DeleteMapping("/delete/{id}")
    @Operation(summary = "删除习题册", description = "级联删除书本及其与科目的关联记录。")
    public Result deleteBook(@PathVariable Integer id) {
        // 级联删除关联关系（建议在 Service 层开启事务执行）
        mapSubjectBookService.removeAllSubjectBookRelations(id);
        bookService.removeById(id);
        return Result.success("删除成功");
    }

    // 5. 根据ID获取详情
    @GetMapping("/{id}")
    @Operation(summary = "获取习题册详情", description = "包含习题册信息及所有关联科目 ID。")
    public Result getBookById(@PathVariable Integer id) {
        Book book = bookService.getById(id);
        if (book == null) return Result.error("习题册不存在");

        // 注入关联科目列表
        List<Integer> subjectIds = mapSubjectBookService.getSubjectIdsByBookId(id);
        book.setSubjectIds(subjectIds);
        // 兼容性处理
        book.setSubjectId(subjectIds != null && !subjectIds.isEmpty() ? subjectIds.get(0) : null);

        return Result.success(book);
    }

    // 6. 分页查询（支持科目筛选）
    @GetMapping("/page")
    @Operation(summary = "分页查询习题册")
    public Result findPage(
            @RequestParam Integer pageNum,
            @RequestParam Integer pageSize,
            @RequestParam(required = false) Integer subjectId) {
        Page<Book> page = new Page<>(pageNum, pageSize);
        return Result.success(bookService.bookPage(page, subjectId));
    }

    // 7. 根据科目获取书本列表
    @GetMapping("/list-by-subject")
    @Operation(summary = "根据科目获取习题册")
    public Result getBooksBySubject(@RequestParam Integer subjectId) {
        List<Integer> bookIds = mapSubjectBookService.getBookIdsBySubjectId(subjectId);
        if (bookIds == null || bookIds.isEmpty()) {
            return Result.success(new ArrayList<>());
        }
        return Result.success(bookService.listByIds(bookIds));
    }
}