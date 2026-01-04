package org.example.kaoyanplatform.service;

import com.baomidou.mybatisplus.extension.service.IService;

import org.example.kaoyanplatform.entity.Subject;
import org.example.kaoyanplatform.entity.dto.SubjectDTO;

import java.util.List;

public interface SubjectService extends IService<Subject> {
    List<SubjectDTO> getTree(Long userId);
    List<Integer> getDescendantIds(Integer subjectId);
}
