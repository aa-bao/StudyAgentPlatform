package org.example.kaoyanplatform.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.example.kaoyanplatform.entity.MapSubjectBook;

import java.util.List;

/**
 * 书本-科目映射表Mapper
 */
@Mapper
public interface MapSubjectBookMapper extends BaseMapper<MapSubjectBook> {

    /**
     * 根据书本ID查询科目ID列表
     * @param bookId 书本ID
     * @return 科目ID列表
     */
    @Select("SELECT subject_id FROM map_subject_book WHERE book_id = #{bookId}")
    List<Integer> getSubjectIdsByBookId(Integer bookId);

    /**
     * 根据科目ID查询书本ID列表
     * @param subjectId 科目ID
     * @return 书本ID列表
     */
    @Select("SELECT book_id FROM map_subject_book WHERE subject_id = #{subjectId}")
    List<Integer> getBookIdsBySubjectId(Integer subjectId);
}
