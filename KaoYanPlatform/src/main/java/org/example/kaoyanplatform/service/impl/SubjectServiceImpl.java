package org.example.kaoyanplatform.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import org.example.kaoyanplatform.entity.QuestionSubject;
import org.example.kaoyanplatform.entity.Subject;
import org.example.kaoyanplatform.entity.UserProgress;
import org.example.kaoyanplatform.entity.dto.SubjectDTO;
import org.example.kaoyanplatform.mapper.QuestionSubjectMapper;
import org.example.kaoyanplatform.mapper.SubjectMapper;
import org.example.kaoyanplatform.mapper.UserProgressMapper;
import org.example.kaoyanplatform.service.SubjectService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class SubjectServiceImpl extends ServiceImpl<SubjectMapper, Subject> implements SubjectService {

    @Autowired
    private QuestionSubjectMapper questionSubjectMapper;

    @Autowired
    private UserProgressMapper userProgressMapper;

    @Override
    public List<SubjectDTO> getTree(Long userId) {
        // 1. Get all subjects
        List<Subject> allSubjects = list();

        // 2. Get question counts for each subject
        QueryWrapper<QuestionSubject> qsWrapper = new QueryWrapper<>();
        qsWrapper.select("subject_id", "count(*) as count");
        qsWrapper.groupBy("subject_id");
        List<Map<String, Object>> qsCounts = questionSubjectMapper.selectMaps(qsWrapper);
        Map<Integer, Integer> questionCountMap = new HashMap<>();
        for (Map<String, Object> map : qsCounts) {
            questionCountMap.put((Integer) map.get("subject_id"), ((Long) map.get("count")).intValue());
        }

        // 3. Get user progress
        Map<Integer, Integer> finishedCountMap = new HashMap<>();
        if (userId != null) {
            QueryWrapper<UserProgress> upWrapper = new QueryWrapper<>();
            upWrapper.eq("user_id", userId);
            List<UserProgress> progressList = userProgressMapper.selectList(upWrapper);
            for (UserProgress up : progressList) {
                finishedCountMap.put(up.getSubjectId(), up.getFinishedCount());
            }
        }

        // 4. Convert to DTOs and Build Tree
        Map<Integer, SubjectDTO> dtoMap = new HashMap<>();
        List<SubjectDTO> roots = new ArrayList<>();

        // First pass: create all DTOs
        for (Subject s : allSubjects) {
            SubjectDTO dto = new SubjectDTO();
            BeanUtils.copyProperties(s, dto);
            dto.setChildren(new ArrayList<>());
            // Set initial counts (direct counts)
            dto.setQuestionCount(questionCountMap.getOrDefault(s.getId(), 0));
            dto.setFinishedCount(finishedCountMap.getOrDefault(s.getId(), 0));
            dtoMap.put(s.getId(), dto);
        }

        // Second pass: build tree structure
        for (Subject s : allSubjects) {
            SubjectDTO dto = dtoMap.get(s.getId());
            if (s.getParentId() == null || s.getParentId() == 0) {
                roots.add(dto);
            } else {
                SubjectDTO parent = dtoMap.get(s.getParentId());
                if (parent != null) {
                    parent.getChildren().add(dto);
                }
            }
        }

        // 5. Aggregate counts recursively
        for (SubjectDTO root : roots) {
            aggregateCounts(root);
        }

        return roots;
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
}
