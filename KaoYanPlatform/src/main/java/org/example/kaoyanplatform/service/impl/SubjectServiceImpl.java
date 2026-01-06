package org.example.kaoyanplatform.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import org.example.kaoyanplatform.constant.SubjectLevelConstants;
import org.example.kaoyanplatform.entity.MapQuestionSubject;
import org.example.kaoyanplatform.entity.Subject;
import org.example.kaoyanplatform.entity.UserProgress;
import org.example.kaoyanplatform.entity.dto.SubjectDTO;
import org.example.kaoyanplatform.mapper.MapQuestionSubjectMapper;
import org.example.kaoyanplatform.mapper.SubjectMapper;
import org.example.kaoyanplatform.mapper.UserProgressMapper;
import org.example.kaoyanplatform.service.SubjectService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 科目Service实现类
 * 使用映射表（map_question_subject）管理题目与科目的关系
 * 使用映射表（map_subject_book）管理书本与科目的关系
 *
 * 层级结构说明：
 * - Level 1 (CATEGORY): 考试大类（如：公共课、专业课）
 * - Level 2 (EXAM_SPEC): 考试规格（如：数学一、数学二、英语一）
 * - Level 3 (SUBJECT): 具体学科（如：高等数学、线性代数、概率论）
 * - Level 4 (KNOWLEDGE_POINT): 知识点/章节（如：函数与极限、三重积分）
 * - Level 5 (QUESTION_TYPE): 题型/解题方法（如：泰勒公式）
 */
@Service
public class SubjectServiceImpl extends ServiceImpl<SubjectMapper, Subject> implements SubjectService {

    @Autowired
    private MapQuestionSubjectMapper mapQuestionSubjectMapper;

    @Autowired
    private UserProgressMapper userProgressMapper;

    @Override
    public List<SubjectDTO> getTree(Long userId, Integer rootId) {
        // 1. 获取所有科目
        List<Subject> allSubjects = list();

        // 2. 获取每个科目的题目数量（使用新的映射表）
        QueryWrapper<MapQuestionSubject> qsWrapper = new QueryWrapper<>();
        qsWrapper.select("subject_id", "count(*) as count");
        qsWrapper.groupBy("subject_id");
        List<Map<String, Object>> qsCounts = mapQuestionSubjectMapper.selectMaps(qsWrapper);
        Map<Integer, Integer> questionCountMap = new HashMap<>();
        for (Map<String, Object> map : qsCounts) {
            questionCountMap.put((Integer) map.get("subject_id"), ((Long) map.get("count")).intValue());
        }

        // 3. 获取用户进度
        Map<Integer, Integer> finishedCountMap = new HashMap<>();
        if (userId != null) {
            QueryWrapper<UserProgress> upWrapper = new QueryWrapper<>();
            upWrapper.eq("user_id", userId);
            List<UserProgress> progressList = userProgressMapper.selectList(upWrapper);
            for (UserProgress up : progressList) {
                finishedCountMap.put(up.getSubjectId(), up.getFinishedCount());
            }
        }

        // 4. 转换为 DTO 并构建树结构
        Map<Integer, SubjectDTO> dtoMap = new HashMap<>();
        List<Integer> targetRootIds = new ArrayList<>();

        // 确定目标根节点
        if (rootId != null) {
            // 检查是否为书籍
            List<Integer> mappedIds = baseMapper.getSubjectIdsByBookId(rootId);
            if (mappedIds != null && !mappedIds.isEmpty()) {
                targetRootIds.addAll(mappedIds);
            } else {
                // 如果不是书籍，使用 rootId 的子节点
                for (Subject s : allSubjects) {
                    if (s.getParentId() != null && s.getParentId().equals(rootId)) {
                        targetRootIds.add(s.getId());
                    }
                }
            }
        } else {
            // 如果 rootId 为空，返回一级科目以及 parentId=0 的特殊节点
            for (Subject s : allSubjects) {
                // level=1 的节点（正常的顶级科目）
                if ("1".equals(s.getLevel())) {
                    targetRootIds.add(s.getId());
                }
                // parentId=0 的节点（特殊的顶级节点，如高数、线代等）
                else if (s.getParentId() != null && s.getParentId() == 0) {
                    targetRootIds.add(s.getId());
                }
            }
        }

        // 创建 DTO 对象
        for (Subject s : allSubjects) {
            SubjectDTO dto = new SubjectDTO();
            BeanUtils.copyProperties(s, dto);
            dto.setChildren(new ArrayList<>());
            dto.setQuestionCount(questionCountMap.getOrDefault(s.getId(), 0));
            dto.setFinishedCount(finishedCountMap.getOrDefault(s.getId(), 0));
            dtoMap.put(s.getId(), dto);
        }

        // 构建树结构
        List<SubjectDTO> resultRoots = new ArrayList<>();
        for (Subject s : allSubjects) {
            SubjectDTO dto = dtoMap.get(s.getId());
            
            // 关联到父节点（如果父节点存在于 map 中）
            if (s.getParentId() != null && s.getParentId() != 0) {
                SubjectDTO parent = dtoMap.get(s.getParentId());
                if (parent != null) {
                    parent.getChildren().add(dto);
                }
            }

            // 检查当前节点是否为目标根节点
            if (targetRootIds.contains(s.getId())) {
                resultRoots.add(dto);
            }
        }

        // 5. 递归汇总统计数据
        for (SubjectDTO root : resultRoots) {
            aggregateCounts(root);
        }

        return resultRoots;
    }

    private void aggregateCounts(SubjectDTO node) {
        if (node.getChildren() == null || node.getChildren().isEmpty()) {
            return;
        }
        int totalQ = node.getQuestionCount();
        int totalF = node.getFinishedCount();

        for (SubjectDTO child : node.getChildren()) {
            aggregateCounts(child);
            totalQ += child.getQuestionCount();
            totalF += child.getFinishedCount();
        }
        node.setQuestionCount(totalQ);
        node.setFinishedCount(totalF);
    }

    @Override
    public List<Integer> getDescendantIds(Integer subjectId) {
        List<Subject> all = list();
        List<Integer> result = new ArrayList<>();
        result.add(subjectId);
        collectDescendants(subjectId, all, result);
        return result;
    }

    private void collectDescendants(Integer parentId, List<Subject> all, List<Integer> result) {
        for (Subject s : all) {
            if (s.getParentId() != null && s.getParentId().equals(parentId)) {
                result.add(s.getId());
                collectDescendants(s.getId(), all, result);
            }
        }
    }

    @Override
    public List<SubjectDTO> getManageTree() {
        // 1. 获取所有科目并转为 DTO
        List<Subject> allSubjects = list();
        List<SubjectDTO> allDTOs = allSubjects.stream()
                .map(s -> {
                    SubjectDTO dto = new SubjectDTO();
                    BeanUtils.copyProperties(s, dto);
                    dto.setChildren(new ArrayList<>());
                    dto.setTreeId(s.getParentId() + "-" + s.getId());
                    return dto;
                }).collect(Collectors.toList());

        Map<Integer, SubjectDTO> dtoMap = allDTOs.stream()
                .collect(Collectors.toMap(SubjectDTO::getId, d -> d));

        // 2. 只创建数学和英语的虚拟分组
        List<SubjectDTO> virtualGroups = new ArrayList<>();
        SubjectDTO englishGroup = createVNode(-2, "英语");
        SubjectDTO mathGroup = createVNode(-3, "数学");
        virtualGroups.add(englishGroup);
        virtualGroups.add(mathGroup);

        Map<Integer, SubjectDTO> vMap = new HashMap<>();
        vMap.put(-2, englishGroup);
        vMap.put(-3, mathGroup);

        List<SubjectDTO> roots = new ArrayList<>();

        for (SubjectDTO dto : allDTOs) {
            Integer pid = dto.getParentId();

            // 情况 A: Level 1 节点
            if ("1".equals(dto.getLevel())) {
                int vId = getMathOrEnglishVId(dto.getId());
                if (vId != 0) {
                    // 英语一/二、数学一/二/三 放入虚拟组
                    vMap.get(vId).getChildren().add(dto);
                    dto.setTreeId(vId + "-" + dto.getId());
                } else {
                    // 政治、408 等直接作为根节点
                    roots.add(dto);
                }
            }
            // 情况 B: 正常的父子级 (parentId > 0)
            else if (pid != null && pid > 0) {
                SubjectDTO parent = dtoMap.get(pid);
                if (parent != null) {
                    dto.setTreeId(parent.getId() + "-" + dto.getId());
                    parent.getChildren().add(dto);
                }
            }
            // 情况 C: parentId 为 0 且有 scope (如高数、完型) -> 挂载到数学/英语规格下
            else if ((pid == null || pid == 0) && StringUtils.hasText(dto.getScope())) {
                String[] scopeIds = dto.getScope().split(",");
                for (String scopeIdStr : scopeIds) {
                    try {
                        Integer scopeId = Integer.parseInt(scopeIdStr.trim());
                        SubjectDTO parentSpec = dtoMap.get(scopeId);
                        if (parentSpec != null) {
                            SubjectDTO cloneNode = new SubjectDTO();
                            BeanUtils.copyProperties(dto, cloneNode);
                            cloneNode.setChildren(new ArrayList<>());
                            cloneNode.setTreeId(scopeId + "-" + dto.getId());
                            parentSpec.getChildren().add(cloneNode);
                        }
                    } catch (NumberFormatException ignored) {}
                }
            }
        }

        // 将有内容的虚拟组加入根列表
        for (SubjectDTO g : virtualGroups) {
            if (!g.getChildren().isEmpty()) roots.add(g);
        }

        sortTree(roots);
        return roots;
    }

    // 仅区分数学和英语
    private int getMathOrEnglishVId(Integer id) {
        if (id == 2 || id == 3) return -2; // 英语
        if (id == 4 || id == 5 || id == 6) return -3; // 数学
        return 0;
    }

    // 辅助方法：定义虚拟大类
    private List<SubjectDTO> createVirtualGroups() {
        List<SubjectDTO> list = new ArrayList<>();
        list.add(createVNode(-1, "政治"));
        list.add(createVNode(-2, "英语"));
        list.add(createVNode(-3, "数学"));
        list.add(createVNode(-4, "408"));
        return list;
    }

    private SubjectDTO createVNode(Integer id, String name) {
        SubjectDTO dto = new SubjectDTO();
        dto.setId(id);
        dto.setName(name);
        dto.setLevel("0");
        dto.setTreeId("root-" + id);
        dto.setChildren(new ArrayList<>());
        return dto;
    }

    // 辅助方法：匹配 Level 1 归属于哪个虚拟大类
    private int getVirtualGroupId(Integer id, String name) {
        if (id == 1 || name.contains("政治")) return -1;
        if (id == 2 || id == 3 || name.contains("英语")) return -2;
        if (id == 4 || id == 5 || id == 6 || name.contains("数学")) return -3;
        if (id == 7 || name.contains("408")) return -4;
        return 0;
    }

    /**
     * 递归排序树形结构
     */
    private void sortTree(List<SubjectDTO> nodes) {
        if (nodes == null || nodes.isEmpty()) {
            return;
        }

        // 对当前层级的节点按 sort 排序（sort 为 null 的排在最后）
        nodes.sort((a, b) -> {
            Integer sortA = a.getSort() != null ? a.getSort() : 9999;
            Integer sortB = b.getSort() != null ? b.getSort() : 9999;
            return sortA.compareTo(sortB);
        });

        // 递归排序子节点
        for (SubjectDTO node : nodes) {
            if (node.getChildren() != null && !node.getChildren().isEmpty()) {
                sortTree(node.getChildren());
            }
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String deleteSubject(Integer subjectId) {
        // 1. 检查是否有子节点
        if (hasChildren(subjectId)) {
            return "该科目下存在子科目，请先删除子科目";
        }

        // 2. 检查是否有题目关联
        if (hasQuestionRelations(subjectId)) {
            return "该科目下存在关联题目，无法删除";
        }

        // 3. 执行删除
        boolean success = removeById(subjectId);
        return success ? "删除成功" : "删除失败";
    }

    @Override
    public boolean hasQuestionRelations(Integer subjectId) {
        // 检查 map_question_subject 表中是否存在该科目的关联
        QueryWrapper<MapQuestionSubject> wrapper = new QueryWrapper<>();
        wrapper.eq("subject_id", subjectId);
        long count = mapQuestionSubjectMapper.selectCount(wrapper);
        return count > 0;
    }

    @Override
    public boolean hasChildren(Integer subjectId) {
        // 检查是否存在子科目
        QueryWrapper<Subject> wrapper = new QueryWrapper<>();
        wrapper.eq("parent_id", subjectId);
        long count = count(wrapper);
        return count > 0;
    }

    @Override
    public boolean updateSubject(Subject subject) {
        return updateById(subject);
    }

    @Override
    public boolean addSubject(Subject subject) {
        return save(subject);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean batchUpdateSort(List<Subject> subjects) {
        if (subjects == null || subjects.isEmpty()) {
            return false;
        }

        // 批量更新每个科目的 parentId 和 sort
        for (Subject subject : subjects) {
            updateById(subject);
        }

        return true;
    }

    @Override
    public List<SubjectDTO> getTreeByLevel(String level, Long userId) {
        // 根据层级获取科目
        QueryWrapper<Subject> wrapper = new QueryWrapper<>();
        wrapper.eq("level", level);
        wrapper.orderByAsc("sort");
        List<Subject> subjects = list(wrapper);

        // 转换为 DTO
        List<SubjectDTO> result = new ArrayList<>();
        for (Subject s : subjects) {
            SubjectDTO dto = new SubjectDTO();
            BeanUtils.copyProperties(s, dto);
            dto.setChildren(new ArrayList<>());

            // 如果提供了 userId，获取统计数据
            if (userId != null) {
                // 获取题目数量
                QueryWrapper<MapQuestionSubject> qsWrapper = new QueryWrapper<>();
                qsWrapper.eq("subject_id", s.getId());
                qsWrapper.select("count(*) as count");
                List<Map<String, Object>> qsCounts = mapQuestionSubjectMapper.selectMaps(qsWrapper);
                if (!qsCounts.isEmpty()) {
                    dto.setQuestionCount(((Long) qsCounts.get(0).get("count")).intValue());
                }

                // 获取完成数量
                QueryWrapper<UserProgress> upWrapper = new QueryWrapper<>();
                upWrapper.eq("user_id", userId);
                upWrapper.eq("subject_id", s.getId());
                UserProgress progress = userProgressMapper.selectOne(upWrapper);
                if (progress != null) {
                    dto.setFinishedCount(progress.getFinishedCount());
                }
            }

            result.add(dto);
        }

        return result;
    }

    @Override
    public List<SubjectDTO> getExamSpecList() {
        // 获取所有 Level 2 的科目（考试规格）
        QueryWrapper<Subject> wrapper = new QueryWrapper<>();
        wrapper.eq("level", SubjectLevelConstants.LEVEL_EXAM_SPEC);
        wrapper.orderByAsc("sort");
        List<Subject> subjects = list(wrapper);

        // 转换为 DTO
        List<SubjectDTO> result = new ArrayList<>();
        for (Subject s : subjects) {
            SubjectDTO dto = new SubjectDTO();
            BeanUtils.copyProperties(s, dto);
            dto.setChildren(new ArrayList<>());
            result.add(dto);
        }

        return result;
    }

    @Override
    public List<SubjectDTO> getTreeByExamSpec(Integer examSpecId, Long userId) {
        // 获取指定考试规格下的所有子科目（Level 3 及以下）
        List<Integer> descendantIds = getDescendantIds(examSpecId);
        descendantIds.remove(examSpecId); // 移除考试规格节点本身

        if (descendantIds.isEmpty()) {
            return new ArrayList<>();
        }

        // 获取所有子科目
        List<Subject> allSubjects = listByIds(descendantIds);

        // 获取统计数据
        Map<Integer, Integer> questionCountMap = new HashMap<>();
        Map<Integer, Integer> finishedCountMap = new HashMap<>();

        // 获取题目数量
        QueryWrapper<MapQuestionSubject> qsWrapper = new QueryWrapper<>();
        qsWrapper.in("subject_id", descendantIds);
        qsWrapper.select("subject_id", "count(*) as count");
        qsWrapper.groupBy("subject_id");
        List<Map<String, Object>> qsCounts = mapQuestionSubjectMapper.selectMaps(qsWrapper);
        for (Map<String, Object> map : qsCounts) {
            questionCountMap.put((Integer) map.get("subject_id"), ((Long) map.get("count")).intValue());
        }

        // 获取用户进度
        if (userId != null) {
            QueryWrapper<UserProgress> upWrapper = new QueryWrapper<>();
            upWrapper.eq("user_id", userId);
            upWrapper.in("subject_id", descendantIds);
            List<UserProgress> progressList = userProgressMapper.selectList(upWrapper);
            for (UserProgress up : progressList) {
                finishedCountMap.put(up.getSubjectId(), up.getFinishedCount());
            }
        }

        // 转换为 DTO 并构建树结构
        Map<Integer, SubjectDTO> dtoMap = new HashMap<>();
        for (Subject s : allSubjects) {
            SubjectDTO dto = new SubjectDTO();
            BeanUtils.copyProperties(s, dto);
            dto.setChildren(new ArrayList<>());
            dto.setQuestionCount(questionCountMap.getOrDefault(s.getId(), 0));
            dto.setFinishedCount(finishedCountMap.getOrDefault(s.getId(), 0));
            dtoMap.put(s.getId(), dto);
        }

        // 构建树形结构（只返回子科目，不包括 examSpecId 节点本身）
        List<SubjectDTO> resultRoots = new ArrayList<>();
        for (Subject s : allSubjects) {
            SubjectDTO dto = dtoMap.get(s.getId());

            // 如果父节点是 examSpecId，则为根节点
            if (s.getParentId() != null && s.getParentId().equals(examSpecId)) {
                resultRoots.add(dto);
            } else if (s.getParentId() != null && s.getParentId() != 0) {
                // 否则添加到父节点的 children 中
                SubjectDTO parent = dtoMap.get(s.getParentId());
                if (parent != null) {
                    parent.getChildren().add(dto);
                }
            }
        }

        // 递归汇总统计数据
        for (SubjectDTO root : resultRoots) {
            aggregateCounts(root);
        }

        // 按照 sort 字段排序
        sortTree(resultRoots);

        return resultRoots;
    }
}
