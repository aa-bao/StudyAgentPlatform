package org.example.kaoyanplatform.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import org.example.kaoyanplatform.entity.Question;
import org.example.kaoyanplatform.entity.dto.LLMQuestionOutputDTO;
import org.example.kaoyanplatform.entity.dto.QuestionDTO;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * 题目Service接口
 */
public interface QuestionService extends IService<Question> {

    /**
     * 根据科目ID列表查询题目
     * @param subjectIds 科目ID列表
     * @return 题目列表
     */
    List<Question> getQuestionsBySubjectIds(List<Integer> subjectIds);

    /**
     * 新增题目（包含关联关系）
     * @param questionDTO 题目数据传输对象
     * @return 是否成功
     */
    boolean saveQuestionWithRelations(QuestionDTO questionDTO);

    /**
     * 更新题目（包含关联关系）
     * @param questionDTO 题目数据传输对象
     * @return 是否成功
     */
    boolean updateQuestionWithRelations(QuestionDTO questionDTO);

    /**
     * 根据书本ID查询题目列表
     * @param bookId 书本ID
     * @return 题目列表
     */
    List<Question> getQuestionsByBookId(Integer bookId);

    /**
     * 分页查询题目（支持关联查询）
     * @param page 分页对象
     * @param subjectId 科目ID（可选）
     * @param bookId 书本ID（可选）
     * @return 分页结果
     */
    Page<Question> questionPage(Page<Question> page, Integer subjectId, Integer bookId);

    LLMQuestionOutputDTO recognizeImageToText(MultipartFile file) throws Exception;


}