# 数据库迁移记录 - 2026-01-12

## 迁移概述

本次迁移主要解决了科目权重在不同考试规格下的差异化问题。

## 迁移内容

### 1. 删除 tb_subject.weight 字段

**原因**:
- 原 `weight` 字段无法区分不同考试规格下的权重差异
- 例如：英语一和英语二的"大作文"权重不同（20% vs 15%），但只能存储一个值

**执行SQL**:
```sql
-- 备份原数据（如有需要）
-- CREATE TABLE tb_subject_backup_20260112 AS SELECT * FROM tb_subject;

-- 删除 weight 字段
ALTER TABLE `tb_subject` DROP COLUMN IF EXISTS `weight`;
```

### 2. 创建科目权重映射表

**表名**: `map_subject_weight`

**建表SQL**:
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

### 3. 初始化权重数据

#### 英语权重配置
```sql
-- 英语一
INSERT INTO `map_subject_weight` (`subject_id`, `exam_spec_id`, `weight`) VALUES
(13, 2, 10), (14, 2, 40), (15, 2, 10), (16, 2, 10), (17, 2, 10), (18, 2, 20);

-- 英语二
INSERT INTO `map_subject_weight` (`subject_id`, `exam_spec_id`, `weight`) VALUES
(13, 3, 10), (14, 3, 40), (15, 3, 10), (16, 3, 15), (17, 3, 10), (18, 3, 15);
```

#### 数学历权重配置
```sql
-- 数学一
INSERT INTO `map_subject_weight` (`subject_id`, `exam_spec_id`, `weight`) VALUES
(19, 4, 58), (20, 4, 21), (21, 4, 21);

-- 数学二
INSERT INTO `map_subject_weight` (`subject_id`, `exam_spec_id`, `weight`) VALUES
(19, 5, 78), (20, 5, 22);

-- 数学三
INSERT INTO `map_subject_weight` (`subject_id`, `exam_spec_id`, `weight`) VALUES
(19, 6, 58), (20, 6, 21), (21, 6, 21);
```

#### 408权重配置
```sql
INSERT INTO `map_subject_weight` (`subject_id`, `exam_spec_id`, `weight`) VALUES
(22, 7, 28), (23, 7, 28), (24, 7, 27), (25, 7, 17);
```

## 代码变更

### 后端变更

#### 1. 实体类更新
**文件**: `Subject.java`
- 删除 `weight` 字段
- 保留 `dynamicWeight` 字段（标记为 `@TableField(exist = false)`）

**文件**: `SubjectDTO.java`
- 删除 `weight` 字段
- 保留 `dynamicWeight` 字段

**文件**: `MapSubjectWeight.java` (新建)
- 新增权重映射表实体类

#### 2. Mapper层更新
**文件**: `MapSubjectWeightMapper.java` (新建)
```java
@Mapper
public interface MapSubjectWeightMapper extends BaseMapper<MapSubjectWeight> {
    @Select("SELECT * FROM map_subject_weight WHERE exam_spec_id = #{examSpecId}")
    List<MapSubjectWeight> listByExamSpecId(@Param("examSpecId") Integer examSpecId);
}
```

**文件**: `SubjectMapper.java`
- 添加 `getTreeByExamSpecId()` 方法
- 添加 `getAllExamSpecs()` 方法

#### 3. Service层更新
**文件**: `SubjectServiceImpl.java`
- 修改 `getTreeByExamSpec()` 方法，集成动态权重查询
- 移除对原 `weight` 字段的依赖

**核心逻辑**:
```java
// 1. 查询权重映射
List<MapSubjectWeight> weightMappings = mapSubjectWeightMapper.listByExamSpecId(examSpecId);
Map<Integer, Float> weightMap = new HashMap<>();
for (MapSubjectWeight mapping : weightMappings) {
    weightMap.put(mapping.getSubjectId(), mapping.getWeight());
}

// 2. 设置动态权重
if (weightMap.containsKey(s.getId())) {
    dto.setDynamicWeight(weightMap.get(s.getId()));
}
```

### 前端变更

**文件**: `TopicDrill.vue`
- 修改 `convertTreeData()` 方法
- 移除 `node.weight` 的降级逻辑，只使用 `node.dynamicWeight`

**变更前**:
```javascript
weight: node.dynamicWeight || node.weight
```

**变更后**:
```javascript
weight: node.dynamicWeight
```

## 回滚方案

如需回滚本次迁移，执行以下步骤：

### 1. 恢复 tb_subject.weight 字段
```sql
ALTER TABLE `tb_subject`
ADD COLUMN `weight` float NULL DEFAULT NULL COMMENT '分值占比（真题试卷中的权重）'
AFTER `sort`;
```

### 2. 恢复代码
- 使用 Git 回滚到迁移前的版本
- 或者手动恢复相关文件

### 3. 删除映射表
```sql
DROP TABLE IF EXISTS `map_subject_weight`;
```

## 验证步骤

迁移完成后，执行以下验证：

### 1. 数据库验证
```sql
-- 检查 weight 字段是否已删除
SHOW COLUMNS FROM tb_subject LIKE 'weight';

-- 检查映射表是否创建成功
SHOW CREATE TABLE map_subject_weight;

-- 验证权重数据
SELECT
  s.name as exam_spec_name,
  sub.name as subject_name,
  msw.weight
FROM map_subject_weight msw
LEFT JOIN tb_subject s ON msw.exam_spec_id = s.id
LEFT JOIN tb_subject sub ON msw.subject_id = sub.id
WHERE msw.exam_spec_id IN (2, 3)
ORDER BY msw.exam_spec_id, msw.subject_id;
```

### 2. 后端验证
- 启动后端服务
- 调用 `GET /api/subject/tree/2` (英语一)
- 调用 `GET /api/subject/tree/3` (英语二)
- 检查返回的科目树中 `dynamicWeight` 字段是否正确

### 3. 前端验证
- 启动前端服务
- 访问知识点专项训练页面
- 切换不同考试规格，检查权重显示是否正确

## 预期影响

### 正面影响
✅ 解决了不同考试规格下权重相同的问题
✅ 数据结构更灵活，易于扩展
✅ 支持未来更多考试规格的权重配置

### 潜在风险
⚠️ 需要确保所有依赖原 `weight` 字段的代码都已更新
⚠️ 需要重新初始化权重数据

## 后续优化建议

1. **政治权重配置**: 为政治科目添加权重映射
2. **专业课支持**: 扩展支持不同院校的专业课权重
3. **数据校验**: 添加权重总和校验（应等于100%）
4. **管理后台**: 开发权重配置管理界面

## 相关文档
- [科目权重映射设计](./map_subject_weight.md)
- [数据库设计总览](./database-schema.md)
- [项目结构文档](../project-structure.md)

## 执行记录

- **执行人**: [待填写]
- **执行时间**: 2026-01-12
- **验证状态**: ✅ 通过 / ⏳ 待验证 / ❌ 失败
- **备注**:
