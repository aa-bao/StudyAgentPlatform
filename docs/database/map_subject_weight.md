# 科目权重映射表设计文档

## 概述

`map_subject_weight` 表用于解决不同考试规格下，相同科目名称但权重不同的问题。

## 问题描述

在考研系统中，英语一和英语二的题型分类（如大作文、小作文等）名称相同，但在真题中的分值占比不同：

- **英语一**：大作文占 **20%**，翻译占 10%
- **英语二**：大作文占 **15%**，翻译占 15%

原有的 `tb_subject` 表中 `weight` 字段只能存储一个值，无法区分不同考试规格下的权重差异。

## 解决方案

### 1. 数据库设计

创建映射表 `map_subject_weight`，建立科目与考试规格的多对多权重关系：

```sql
CREATE TABLE `map_subject_weight` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `subject_id` int NOT NULL COMMENT '科目ID（关联tb_subject.id）',
  `exam_spec_id` int NOT NULL COMMENT '考试规格ID（关联tb_subject.id，如：英语一=2，英语二=3）',
  `weight` float NULL DEFAULT NULL COMMENT '该科目在该考试规格下的权重（百分比）',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `uk_subject_exam` (`subject_id`, `exam_spec_id`) USING BTREE,
  KEY `idx_exam_spec` (`exam_spec_id`) USING BTREE,
  KEY `idx_subject` (`subject_id`) USING BTREE
) ENGINE = InnoDB COMMENT = '科目权重映射表';
```

### 2. 示例数据

#### 英语一的权重配置
```sql
INSERT INTO `map_subject_weight` (`subject_id`, `exam_spec_id`, `weight`) VALUES
(13, 2, 10),  -- 完形填空: 10%
(14, 2, 40),  -- 阅读理解: 40%
(15, 2, 10),  -- 新题型: 10%
(16, 2, 10),  -- 翻译: 10%
(17, 2, 10),  -- 小作文: 10%
(18, 2, 20);  -- 大作文: 20%
```

#### 英语二的权重配置
```sql
INSERT INTO `map_subject_weight` (`subject_id`, `exam_spec_id`, `weight`) VALUES
(13, 3, 10),  -- 完形填空: 10%
(14, 3, 40),  -- 阅读理解: 40%
(15, 3, 10),  -- 新题型: 10%
(16, 3, 15),  -- 翻译: 15%
(17, 3, 10),  -- 小作文: 10%
(18, 3, 15);  -- 大作文: 15%
```

#### 数学一的权重配置
```sql
INSERT INTO `map_subject_weight` (`subject_id`, `exam_spec_id`, `weight`) VALUES
(19, 4, 58),  -- 高等数学: 58%
(20, 4, 21),  -- 线性代数: 21%
(21, 4, 21);  -- 概率论与数理统计: 21%
```

#### 数学二的权重配置
```sql
INSERT INTO `map_subject_weight` (`subject_id`, `exam_spec_id`, `weight`) VALUES
(19, 5, 78),  -- 高等数学: 78%
(20, 5, 22);  -- 线性代数: 22%
```

#### 数学三的权重配置
```sql
INSERT INTO `map_subject_weight` (`subject_id`, `exam_spec_id`, `weight`) VALUES
(19, 6, 58),  -- 高等数学: 58%
(20, 6, 21),  -- 线性代数: 21%
(21, 6, 21);  -- 概率论与数理统计: 21%
```

#### 408的权重配置
```sql
INSERT INTO `map_subject_weight` (`subject_id`, `exam_spec_id`, `weight`) VALUES
(22, 7, 28),  -- 数据结构: 28%
(23, 7, 28),  -- 计算机组成原理: 28%
(24, 7, 27),  -- 操作系统: 27%
(25, 7, 17);  -- 计算机网络: 17%
```

**408分值说明**：
- 总分：150分
- 数据结构：42分（28%）
- 计算机组成原理：42分（28%）
- 操作系统：40分（27%）
- 计算机网络：26分（17%）

## 技术实现

### 1. 后端实现

#### 实体类
- `MapSubjectWeight.java`: 映射表实体类
- `Subject.java`: 添加 `dynamicWeight` 字段用于存储动态权重
- `SubjectDTO.java`: 同步添加 `dynamicWeight` 字段

#### Mapper 层
```java
@Mapper
public interface MapSubjectWeightMapper extends BaseMapper<MapSubjectWeight> {
    @Select("SELECT * FROM map_subject_weight WHERE exam_spec_id = #{examSpecId}")
    List<MapSubjectWeight> listByExamSpecId(@Param("examSpecId") Integer examSpecId);
}
```

#### Service 层
在 `SubjectServiceImpl.getTreeByExamSpec()` 方法中：

1. 查询考试规格对应的所有权重映射
2. 构建 `weightMap` 缓存
3. 转换 DTO 时优先使用映射表中的权重

```java
// 获取该考试规格下的权重映射
List<MapSubjectWeight> weightMappings = mapSubjectWeightMapper.listByExamSpecId(examSpecId);
Map<Integer, Float> weightMap = new HashMap<>();
for (MapSubjectWeight mapping : weightMappings) {
    weightMap.put(mapping.getSubjectId(), mapping.getWeight());
}

// 设置动态权重
if (weightMap.containsKey(s.getId())) {
    dto.setDynamicWeight(weightMap.get(s.getId()));
} else if (s.getWeight() != null) {
    dto.setDynamicWeight(s.getWeight().floatValue());
}
```

### 2. 前端实现

在 `TopicDrill.vue` 的 `convertTreeData` 方法中：

```javascript
weight: node.dynamicWeight || node.weight, // 优先使用动态权重
```

## 使用说明

### 1. 初始化数据库

执行 SQL 文件：
```bash
mysql -u root -p kaoyan_platform < map_subject_weight.sql
```

### 2. 查询权重

#### 查询某个考试规格下所有科目的权重
```sql
SELECT s.*, m.weight
FROM tb_subject s
LEFT JOIN map_subject_weight m ON s.id = m.subject_id AND m.exam_spec_id = 2
WHERE s.level = 2 AND s.scope LIKE '%2%';
```

#### 查询某个科目在所有考试规格下的权重
```sql
SELECT s.name as subject_name, es.name as exam_spec_name, m.weight
FROM map_subject_weight m
LEFT JOIN tb_subject s ON m.subject_id = s.id
LEFT JOIN tb_subject es ON m.exam_spec_id = es.id
WHERE m.subject_id = 18;
```

### 3. 更新权重

```sql
INSERT INTO map_subject_weight (subject_id, exam_spec_id, weight)
VALUES (18, 2, 20)
ON DUPLICATE KEY UPDATE weight = 20;
```

## 扩展性

该方案同样适用于其他需要区分权重的场景：

- ✅ **英语一/二**：各题型在完形填空、阅读、新题型、翻译、作文上的权重差异
- ✅ **数学一/二/三**：各科目在高数、线代、概率论上的权重差异
- ✅ **408**：各科目在数据结构、计组、操作系统、计算机网络上的权重差异
- **专业课**：不同院校的专业课科目权重差异
- **政治**：各科目在马原、毛中特、史纲、思修、时政上的权重差异

## 维护建议

1. **数据一致性**：添加新科目时，同时更新映射表
2. **数据校验**：确保同一考试规格下所有科目权重之和为 100%
3. **版本控制**：SQL 文件纳入版本控制，记录每次权重调整

## 相关文件

- 数据库脚本：`map_subject_weight.sql`
- 后端实体：`KaoYanPlatform-back/src/main/java/org/example/kaoyanplatform/entity/MapSubjectWeight.java`
- 后端 Mapper：`KaoYanPlatform-back/src/main/java/org/example/kaoyanplatform/mapper/MapSubjectWeightMapper.java`
- 后端 Service：`KaoYanPlatform-back/src/main/java/org/example/kaoyanplatform/service/impl/SubjectServiceImpl.java`
- 前端页面：`KaoYanPlatform-front/src/views/quiz/TopicDrill.vue`

## 更新日志

- **2026-01-12**: 初始版本，支持英语一/英语二的权重区分
