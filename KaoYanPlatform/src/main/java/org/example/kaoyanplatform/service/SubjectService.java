package org.example.kaoyanplatform.service;

import com.baomidou.mybatisplus.extension.service.IService;

import org.example.kaoyanplatform.entity.Subject;
import org.example.kaoyanplatform.entity.dto.SubjectDTO;

import java.util.List;

/**
 * 科目Service接口
 *
 * 层级结构说明：
 * - Level 1 (CATEGORY): 考试大类（如：公共课、专业课）
 * - Level 2 (EXAM_SPEC): 考试规格（如：数学一、数学二、英语一）
 * - Level 3 (SUBJECT): 具体学科（如：高等数学、线性代数、概率论）
 * - Level 4 (KNOWLEDGE_POINT): 知识点/章节（如：函数与极限、三重积分）
 * - Level 5 (QUESTION_TYPE): 题型/解题方法（如：泰勒公式）
 *
 * @see org.example.kaoyanplatform.constant.SubjectLevelConstants
 */
public interface SubjectService extends IService<Subject> {
    /**
     * 获取科目树（带统计数据）
     *
     * @param userId 用户ID，用于统计学习进度
     * @param rootId 根节点ID，可选。如果提供，则返回该节点下的子树
     * @return 科目树结构
     */
    List<SubjectDTO> getTree(Long userId, Integer rootId);

    /**
     * 获取科目树（按层级过滤）
     * 用于前端动态加载特定层级的科目
     *
     * @param level 层级值（1-5）
     * @param userId 用户ID，可选
     * @return 该层级的科目列表
     */
    List<SubjectDTO> getTreeByLevel(String level, Long userId);

    /**
     * 获取考试规格列表（Level 2）
     * 用于 SubjectList 页面展示考试规格选择
     *
     * @return 考试规格列表
     */
    List<SubjectDTO> getExamSpecList();

    /**
     * 根据考试规格获取科目树
     * 返回指定考试规格下的所有科目（Level 3 及以下）
     *
     * @param examSpecId 考试规格ID（Level 2 的 ID）
     * @param userId 用户ID，可选
     * @return 科目树
     */
    List<SubjectDTO> getTreeByExamSpec(Integer examSpecId, Long userId);

    /**
     * 获取所有子孙节点ID
     */
    List<Integer> getDescendantIds(Integer subjectId);

    /**
     * 获取扁平化的科目树（用于管理界面，不带统计数据）
     */
    List<SubjectDTO> getManageTree();

    /**
     * 删除科目（检查关联）
     * @param subjectId 科目ID
     * @return 删除结果消息
     */
    String deleteSubject(Integer subjectId);

    /**
     * 更新科目信息
     */
    boolean updateSubject(Subject subject);

    /**
     * 新增科目
     */
    boolean addSubject(Subject subject);

    /**
     * 检查科目是否有题目关联
     */
    boolean hasQuestionRelations(Integer subjectId);

    /**
     * 检查科目是否有子节点
     */
    boolean hasChildren(Integer subjectId);

    /**
     * 批量更新科目排序
     */
    boolean batchUpdateSort(List<Subject> subjects);
}
