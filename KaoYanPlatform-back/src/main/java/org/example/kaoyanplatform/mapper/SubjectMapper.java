package org.example.kaoyanplatform.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.example.kaoyanplatform.entity.Subject;

import java.util.List;

/**
 * 科目表 Mapper
 */
@Mapper
public interface SubjectMapper extends BaseMapper<Subject> {

    /**
     * 根据书籍ID获取关联的科目ID列表
     *
     * @param bookId 书籍ID
     * @return 科目ID列表
     */
    @Select("SELECT subject_id FROM map_subject_book WHERE book_id = #{bookId}")
    List<Integer> getSubjectIdsByBookId(@Param("bookId") Integer bookId);

    /**
     * 根据考试规格ID获取该考试规格下的科目树（不包含顶级节点）
     *
     * @param examSpecId 考试规格ID（如：英语一=2，英语二=3）
     * @return 科目列表
     */
    @Select("SELECT * FROM tb_subject WHERE id = #{examSpecId} OR parent_id = #{examSpecId} OR scope LIKE CONCAT('%', #{examSpecId}, '%') ORDER BY sort ASC")
    List<Subject> getTreeByExamSpecId(@Param("examSpecId") Integer examSpecId);

    /**
     * 获取所有一级考试规格（用于顶部导航）
     *
     * @return 考试规格列表
     */
    @Select("SELECT * FROM tb_subject WHERE level = 1 ORDER BY sort ASC")
    List<Subject> getAllExamSpecs();
}
