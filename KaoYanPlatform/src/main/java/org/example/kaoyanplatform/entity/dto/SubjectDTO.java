package org.example.kaoyanplatform.entity.dto;

import lombok.Data;
import java.util.List;

@Data
public class SubjectDTO {
    private Integer id;
    private String name;
    private Integer parentId;
    private String icon;
    private Integer sort;
    private String level;
    private String scope;
    
    // 树形结构字段
    private List<SubjectDTO> children;
    
    // 统计数据
    private Integer questionCount;
    private Integer finishedCount;

    private String treeId; // 格式为 "父级ID-自身ID"
}
