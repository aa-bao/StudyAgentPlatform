package org.example.kaoyanplatform.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.example.kaoyanplatform.entity.MapQuestionSubject;

import java.util.List;

/**
 * 题目-科目映射表Mapper
 */
@Mapper
public interface MapQuestionSubjectMapper extends BaseMapper<MapQuestionSubject> {

    /**
     * 根据题目ID查询科目ID列表
     * @param questionId 题目ID
     * @return 科目ID列表
     */
    @Select("SELECT subject_id FROM map_question_subject WHERE question_id = #{questionId}")
    List<Integer> getSubjectIdsByQuestionId(Long questionId);

    /**
     * 根据科目ID查询题目ID列表
     * @param subjectId 科目ID
     * @return 题目ID列表
     */
    @Select("SELECT question_id FROM map_question_subject WHERE subject_id = #{subjectId}")
    List<Long> getQuestionIdsBySubjectId(Integer subjectId);
}
