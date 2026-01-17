package org.example.kaoyanplatform.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.example.kaoyanplatform.entity.MapQuestionBook;

import java.util.List;

/**
 * 题目-书本映射表Mapper
 */
@Mapper
public interface MapQuestionBookMapper extends BaseMapper<MapQuestionBook> {

    /**
     * 根据题目ID查询书本ID列表
     * @param questionId 题目ID
     * @return 书本ID列表
     */
    @Select("SELECT book_id FROM map_question_book WHERE question_id = #{questionId}")
    List<Integer> getBookIdsByQuestionId(Long questionId);

    /**
     * 根据书本ID查询题目ID列表
     * @param bookId 书本ID
     * @return 题目ID列表
     */
    @Select("SELECT question_id FROM map_question_book WHERE book_id = #{bookId}")
    List<Long> getQuestionIdsByBookId(Integer bookId);
}
