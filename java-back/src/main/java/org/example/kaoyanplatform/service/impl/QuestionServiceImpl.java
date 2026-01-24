package org.example.kaoyanplatform.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.example.kaoyanplatform.entity.MapQuestionBook;
import org.example.kaoyanplatform.entity.MapQuestionSubject;
import org.example.kaoyanplatform.entity.Question;
import org.example.kaoyanplatform.entity.Subject;
import org.example.kaoyanplatform.entity.Book;
import org.example.kaoyanplatform.entity.dto.QuestionDTO;
import org.example.kaoyanplatform.mapper.MapQuestionBookMapper;
import org.example.kaoyanplatform.mapper.MapQuestionSubjectMapper;
import org.example.kaoyanplatform.mapper.QuestionMapper;
import org.example.kaoyanplatform.service.MapQuestionBookService;
import org.example.kaoyanplatform.service.MapQuestionSubjectService;
import org.example.kaoyanplatform.service.QuestionService;
import org.example.kaoyanplatform.service.SubjectService;
import org.example.kaoyanplatform.service.BookService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 题目Service实现类（重构版：支持 content_json 结构）
 * 使用映射表（map_question_subject、map_question_book）管理题目与科目、书本的关系
 */
@Service
public class QuestionServiceImpl extends ServiceImpl<QuestionMapper, Question> implements QuestionService {

    @Autowired
    private MapQuestionSubjectMapper mapQuestionSubjectMapper;

    @Autowired
    private MapQuestionBookMapper mapQuestionBookMapper;

    @Autowired
    private MapQuestionSubjectService mapQuestionSubjectService;

    @Autowired
    private MapQuestionBookService mapQuestionBookService;

    @Autowired
    private SubjectService subjectService;

    @Autowired
    private BookService bookService;

    @Override
    public List<Question> getQuestionsBySubjectIds(List<Integer> subjectIds) {
        if (subjectIds == null || subjectIds.isEmpty()) {
            return Collections.emptyList();
        }

        // 使用映射表 map_question_subject
        QueryWrapper<MapQuestionSubject> qsWrapper = new QueryWrapper<>();
        qsWrapper.in("subject_id", subjectIds);
        qsWrapper.select("question_id");
        List<Object> qIds = mapQuestionSubjectMapper.selectObjs(qsWrapper);

        if (qIds == null || qIds.isEmpty()) {
            return Collections.emptyList();
        }

        List<Long> questionIds = qIds.stream().map(o -> Long.valueOf(o.toString())).collect(Collectors.toList());
        return listByIds(questionIds);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean saveQuestionWithRelations(QuestionDTO questionDTO) {
        // 1. 保存题目基本信息
        Question question = new Question();

        // 复制基本字段
        question.setId(questionDTO.getId());
        question.setType(questionDTO.getType());
        question.setDifficulty(questionDTO.getDifficulty());
        question.setCreateTime(LocalDateTime.now());

        // 合并 contentJson（兼容字段优先，然后是 contentJson 本身）
        question.setContentJson(questionDTO.getMergedContentJson());

        boolean saved = save(question);

        if (!saved) {
            return false;
        }

        Long questionId = question.getId();

        // 2. 保存题目-书本关联关系
        if (questionDTO.getBookIds() != null && !questionDTO.getBookIds().isEmpty()) {
            for (Integer bookId : questionDTO.getBookIds()) {
                MapQuestionBook relation = new MapQuestionBook();
                relation.setQuestionId(questionId);
                relation.setBookId(bookId);
                mapQuestionBookMapper.insert(relation);
            }
        }

        // 3. 保存题目-科目关联关系
        if (questionDTO.getSubjectIds() != null && !questionDTO.getSubjectIds().isEmpty()) {
            for (Integer subjectId : questionDTO.getSubjectIds()) {
                MapQuestionSubject relation = new MapQuestionSubject();
                relation.setQuestionId(questionId);
                relation.setSubjectId(subjectId);
                mapQuestionSubjectMapper.insert(relation);
            }
        }

        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateQuestionWithRelations(QuestionDTO questionDTO) {
        if (questionDTO.getId() == null) {
            return false;
        }

        // 1. 更新题目基本信息
        Question question = new Question();

        // 复制基本字段
        question.setId(questionDTO.getId());
        question.setType(questionDTO.getType());
        question.setDifficulty(questionDTO.getDifficulty());

        // 合并 contentJson（兼容字段优先，然后是 contentJson 本身）
        question.setContentJson(questionDTO.getMergedContentJson());

        boolean updated = updateById(question);

        if (!updated) {
            return false;
        }

        Long questionId = questionDTO.getId();

        // 2. 删除旧的题目-书本关联关系，重新建立
        mapQuestionBookService.removeAllQuestionBookRelations(questionId);
        if (questionDTO.getBookIds() != null && !questionDTO.getBookIds().isEmpty()) {
            for (Integer bookId : questionDTO.getBookIds()) {
                mapQuestionBookService.addQuestionBookRelation(questionId, bookId);
            }
        }

        // 3. 删除旧的题目-科目关联关系，重新建立
        mapQuestionSubjectService.removeAllQuestionSubjectRelations(questionId);
        if (questionDTO.getSubjectIds() != null && !questionDTO.getSubjectIds().isEmpty()) {
            for (Integer subjectId : questionDTO.getSubjectIds()) {
                mapQuestionSubjectService.addQuestionSubjectRelation(questionId, subjectId);
            }
        }

        return true;
    }

    @Override
    public List<Question> getQuestionsByBookId(Integer bookId) {
        List<Long> questionIds = mapQuestionBookMapper.getQuestionIdsByBookId(bookId);
        if (questionIds == null || questionIds.isEmpty()) {
            return Collections.emptyList();
        }
        return listByIds(questionIds);
    }

    @Override
    public Page<Question> questionPage(Page<Question> page, List<Integer> subjectIds, Integer bookId) {
        QueryWrapper<Question> queryWrapper = new QueryWrapper<>();

        // 如果提供了subjectIds（多个科目），通过映射表查询
        if (subjectIds != null && !subjectIds.isEmpty()) {
            // 查询与任一科目关联的题目
            Set<Long> questionIds = new HashSet<>();
            for (Integer subjectId : subjectIds) {
                List<Long> ids = mapQuestionSubjectMapper.getQuestionIdsBySubjectId(subjectId);
                if (ids != null && !ids.isEmpty()) {
                    questionIds.addAll(ids);
                }
            }
            if (!questionIds.isEmpty()) {
                queryWrapper.in("id", questionIds);
            } else {
                // 没有找到题目，返回空页
                return new Page<>(page.getCurrent(), page.getSize());
            }
        }

        // 如果提供了bookId，通过映射表查询
        if (bookId != null) {
            List<Long> questionIds = mapQuestionBookMapper.getQuestionIdsByBookId(bookId);
            if (questionIds != null && !questionIds.isEmpty()) {
                if (subjectIds != null && !subjectIds.isEmpty()) {
                    // 如果同时提供了subjectIds和bookId，取交集
                    Set<Long> finalQuestionIds = new HashSet<>(questionIds);
                    queryWrapper.in("id", finalQuestionIds);
                } else {
                    queryWrapper.in("id", questionIds);
                }
            } else {
                // 没有找到题目，返回空页
                return new Page<>(page.getCurrent(), page.getSize());
            }
        }

        queryWrapper.orderByDesc("id");
        Page<Question> resultPage = page(page, queryWrapper);

        // 填充 subjectName 和 bookName
        if (resultPage.getRecords() != null && !resultPage.getRecords().isEmpty()) {
            for (Question q : resultPage.getRecords()) {
                // 填充科目信息
                List<Integer> sIds = mapQuestionSubjectService.getSubjectIdsByQuestionId(q.getId());
                if (sIds != null && !sIds.isEmpty()) {
                    q.setSubjectIds(sIds);

                    List<String> subjectNames = new ArrayList<>();
                    for (Integer sId : sIds) {
                        Subject subject = subjectService.getById(sId);
                        if (subject != null) {
                            subjectNames.add(subject.getName());
                        }
                    }
                    q.setSubjectNames(subjectNames);
                    // 兼容性处理
                    q.setSubjectIds(sIds);
                    q.setSubjectName(sIds.isEmpty() ? null : subjectNames.get(0));
                }

                // 填充书本信息
                List<Integer> bIds = mapQuestionBookService.getBookIdsByQuestionId(q.getId());
                if (bIds != null && !bIds.isEmpty()) {
                    q.setBookIds(bIds);

                    List<String> bookNames = new ArrayList<>();
                    for (Integer bId : bIds) {
                        Book book = bookService.getById(bId);
                        if (book != null) {
                            bookNames.add(book.getName());
                        }
                    }
                    q.setBookNames(bookNames);
                    // 兼容性处理
                    q.setBookName(bIds.isEmpty() ? null : bookNames.get(0));
                }
            }
        }

        return resultPage;
    }

    @Override
    public boolean isQuestionExist(String content) {
        if (content == null || content.trim().isEmpty()) {
            return false;
        }

        // 由于 content 现在存储在 JSON 字段中，我们需要先查询所有题目然后在内存中比较
        // 或者使用原生 SQL 的 JSON_EXTRACT 函数（更高效）
        // 这里使用内存比较的方式（简单但性能稍差）
        List<Question> allQuestions = list();

        for (Question q : allQuestions) {
            String qContent = q.getContent();
            if (qContent != null && qContent.equals(content)) {
                return true;
            }
        }

        return false;

        // TODO: 如果数据量大，建议改用原生 SQL 查询:
        // SELECT COUNT(*) > 0 FROM tb_question
        // WHERE JSON_EXTRACT(content_json, '$.content') = #{content}
    }

    @Override
    public List<Question> findSimilarQuestions(String content) {
        if (content == null || content.trim().isEmpty()) {
            return Collections.emptyList();
        }

        // 提取关键词进行模糊匹配
        // 去除空格、换行等
        String normalizedContent = content.replaceAll("\\s+", "").trim();

        // 取前50个字符作为关键词
        String keyword = normalizedContent.length() > 50
                ? normalizedContent.substring(0, 50)
                : normalizedContent;

        // 由于 content 现在存储在 JSON 字段中，我们需要先查询所有题目然后在内存中比较
        List<Question> allQuestions = list();
        List<Question> similarQuestions = new ArrayList<>();

        for (Question q : allQuestions) {
            String qContent = q.getContent();
            if (qContent != null) {
                String normalizedQContent = qContent.replaceAll("\\s+", "").trim();
                if (normalizedQContent.contains(keyword) || keyword.contains(normalizedQContent.substring(0, Math.min(50, normalizedQContent.length())))) {
                    similarQuestions.add(q);
                    if (similarQuestions.size() >= 10) {
                        break;
                    }
                }
            }
        }

        return similarQuestions;

        // TODO: 如果数据量大，建议改用原生 SQL 查询:
        // SELECT * FROM tb_question
        // WHERE JSON_EXTRACT(content_json, '$.content') LIKE CONCAT('%', #{keyword}, '%')
        // LIMIT 10
    }
}
