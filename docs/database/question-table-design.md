# 题目表数据库设计文档

## 概述

本文档详细说明了 `tb_question` 表的数据库设计，包括字段说明、JSON 数据结构、迁移指南等。

## 表结构

### tb_question（优化版）

```sql
CREATE TABLE tb_question (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '题目ID',
    type TINYINT NOT NULL COMMENT '题目类型：1-单选, 2-多选, 3-填空, 4-简答',
    content_json JSON NOT NULL COMMENT '题目完整JSON数据',
    difficulty TINYINT DEFAULT 3 COMMENT '难度：1-5',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_type (type),
    INDEX idx_difficulty (difficulty)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='题目表';
```

### 字段说明

| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT | 主键，自增 |
| type | TINYINT | 题目类型：1-单选, 2-多选, 3-填空, 4-简答 |
| content_json | JSON | 题目完整数据（JSON 格式） |
| difficulty | TINYINT | 难度等级：1-5，默认 3 |
| create_time | DATETIME | 创建时间 |
| update_time | DATETIME | 更新时间，自动更新 |

## content_json 结构

### 单选/多选题结构

```json
{
  "content": "题目内容（支持 LaTeX 公式）",
  "options": [
    {"label": "A", "text": "选项内容（支持 LaTeX）"},
    {"label": "B", "text": "选项内容（支持 LaTeX）"},
    {"label": "C", "text": "选项内容（支持 LaTeX）"},
    {"label": "D", "text": "选项内容（支持 LaTeX）"}
  ],
  "answer": "A",
  "analysis": "解析内容（支持 LaTeX）",
  "tags": ["标签1", "标签2"],
  "source": "题目来源"
}
```

**字段说明**：
- `content`（必填）：题干内容，支持 LaTeX 公式
- `options`（必填）：选项数组，每个选项包含 label 和 text
- `answer`（必填）：正确答案
  - 单选题：A/B/C/D
  - 多选题：AB/ABC/ABCD
- `analysis`（可选）：解析内容
- `tags`（可选）：标签数组
- `source`（可选）：题目来源

### 填空/简答题结构

```json
{
  "content": "题目内容（支持 LaTeX 公式）",
  "answer": "答案内容（支持 LaTeX）",
  "analysis": "解析内容（支持 LaTeX）",
  "tags": ["标签1", "标签2"],
  "source": "题目来源"
}
```

**字段说明**：
- `content`（必填）：题干内容
- `answer`（必填）：答案内容
- `analysis`（可选）：解析内容
- `tags`（可选）：标签数组
- `source`（可选）：题目来源
- 注意：填空/简答题没有 `options` 字段

## 设计优势

### 1. 减少表字段冗余

**优化前**（多个字段）：
```sql
content VARCHAR(1000),
options JSON,
answer VARCHAR(500),
analysis TEXT,
tags JSON,
source VARCHAR(200)
```

**优化后**（单一 JSON 字段）：
```sql
content_json JSON
```

### 2. 提高灵活性

不同题型可以有差异化的 JSON 结构：
- 选择题：包含 `options` 字段
- 填空题：不需要 `options` 字段
- 简答题：答案可以是长文本

### 3. 便于扩展

未来如需添加新字段（如 `knowledge_points`），只需修改 JSON 结构，无需变更表结构。

## 后端实现

### Entity 实体类

**Question.java**:

```java
@Data
@TableName("tb_question")
public class Question {
    @TableId(type = IdType.AUTO)
    private Long id;

    private Integer type;

    @TableField(typeHandler = JacksonTypeHandler.class)
    private Map<String, Object> contentJson;

    private Integer difficulty;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
```

### DTO 数据传输对象

**QuestionDTO.java**:

```java
@Data
public class QuestionDTO {
    private Long id;
    private Integer type;
    private String content;
    private List<Map<String, String>> options;
    private String answer;
    private String analysis;
    private List<String> tags;
    private String source;
    private Integer difficulty;

    /**
     * 合并 contentJson（兼容新旧格式）
     */
    public Map<String, Object> getMergedContentJson() {
        Map<String, Object> json = new HashMap<>();
        json.put("content", content);
        if (options != null) json.put("options", options);
        if (answer != null) json.put("answer", answer);
        if (analysis != null) json.put("analysis", analysis);
        if (tags != null) json.put("tags", tags);
        if (source != null) json.put("source", source);
        return json;
    }
}
```

## 数据迁移

### 从旧表结构迁移

如果有旧的表结构需要迁移到新的 JSON 字段设计，可参考以下迁移脚本：

```sql
-- 1. 备份旧表
CREATE TABLE tb_question_backup AS SELECT * FROM tb_question;

-- 2. 添加新字段
ALTER TABLE tb_question ADD COLUMN content_json JSON;

-- 3. 迁移数据
UPDATE tb_question
SET content_json = JSON_OBJECT(
  'content', content,
  'options', IF(options IS NULL, JSON_ARRAY(), options),
  'answer', IFNULL(answer, ''),
  'analysis', IFNULL(analysis, ''),
  'tags', IF(tags IS NULL, JSON_ARRAY(), tags),
  'source', IFNULL(source, '')
);

-- 4. 验证数据
SELECT id, type, JSON_PRETTY(content_json) FROM tb_question LIMIT 10;

-- 5. 删除旧字段（确认无误后执行）
ALTER TABLE tb_question DROP COLUMN content;
ALTER TABLE tb_question DROP COLUMN options;
ALTER TABLE tb_question DROP COLUMN answer;
ALTER TABLE tb_question DROP COLUMN analysis;
ALTER TABLE tb_question DROP COLUMN tags;
ALTER TABLE tb_question DROP COLUMN source;
```

## 查询示例

### 基础查询

```sql
-- 查询所有题目
SELECT id, type, content_json, difficulty FROM tb_question;

-- 按题型查询
SELECT * FROM tb_question WHERE type = 1;

-- 按难度查询
SELECT * FROM tb_question WHERE difficulty >= 4;
```

### JSON 字段查询

```sql
-- 查询特定标签的题目
SELECT * FROM tb_question
WHERE JSON_CONTAINS(content_json->'$.tags', '"导数"');

-- 查询特定来源的题目
SELECT * FROM tb_question
WHERE content_json->'$.source' = '高等数学习题集';

-- 查询选项中包含特定内容的题目
SELECT * FROM tb_question
WHERE JSON_SEARCH(content_json->'$.options', 'one', '积分') IS NOT NULL;
```

### JSON 字段更新

```sql
-- 更新题目内容
UPDATE tb_question
SET content_json = JSON_SET(content_json, '$.content', '新的题目内容')
WHERE id = 1;

-- 添加标签
UPDATE tb_question
SET content_json = JSON_ARRAY_APPEND(content_json, '$.tags', '新标签')
WHERE id = 1;

-- 更新解析
UPDATE tb_question
SET content_json = JSON_SET(content_json, '$.analysis', '新的解析内容')
WHERE id = 1;
```

## 索引优化

### 当前索引

```sql
-- 题型索引
INDEX idx_type (type)

-- 难度索引
INDEX idx_difficulty (difficulty)
```

### 可选索引（根据查询需求）

```sql
-- 如果经常按标签查询，可考虑生成列+索引
ALTER TABLE tb_question
ADD COLUMN tags_extracted VARCHAR(255)
GENERATED ALWAYS AS (content_json->'$.tags') STORED;

CREATE INDEX idx_tags ON tb_question((CAST(tags_extracted AS CHAR(255))));
```

## 注意事项

### 1. JSON 字段验证

后端保存前应验证 JSON 数据的完整性：

```java
// QuestionServiceImpl.java
public void validateQuestionData(QuestionDTO dto) {
    if (dto.getContent() == null || dto.getContent().trim().isEmpty()) {
        throw new IllegalArgumentException("题目内容不能为空");
    }

    // 选择题必须有选项
    if ((dto.getType() == 1 || dto.getType() == 2)
        && (dto.getOptions() == null || dto.getOptions().isEmpty())) {
        throw new IllegalArgumentException("选择题必须包含选项");
    }

    // 答案不能为空
    if (dto.getAnswer() == null || dto.getAnswer().trim().isEmpty()) {
        throw new IllegalArgumentException("答案不能为空");
    }
}
```

### 2. LaTeX 公式转义

AI 识别返回的 JSON 可能包含未转义的反斜杠，需要处理：

```java
// GLMServiceImpl.java
private String fixLaTeXEscaping(String json) {
    // 处理 LaTeX 反斜杠和控制字符
    // 详细实现见 GLMServiceImpl.java
}
```

### 3. 选项格式兼容

系统同时支持两种选项格式：

**格式 1（旧）**：字符串数组
```json
"options": ["选项1", "选项2", "选项3", "选项4"]
```

**格式 2（新）**：对象数组
```json
"options": [
  {"label": "A", "text": "选项1"},
  {"label": "B", "text": "选项2"}
]
```

后端会自动识别并转换：

```java
// QuestionController.java:301-322
if (item.getOptions() instanceof List) {
    List<?> optionsList = (List<?>) item.getOptions();
    if (!optionsList.isEmpty() && optionsList.get(0) instanceof String) {
        // 旧格式：转换为对象数组
        List<Map<String, String>> formattedOptions = new ArrayList<>();
        String[] labels = {"A", "B", "C", "D", "E", "F"};
        for (int i = 0; i < optionsList.size() && i < labels.length; i++) {
            Map<String, String> option = new HashMap<>();
            option.put("label", labels[i]);
            option.put("text", (String) optionsList.get(i));
            formattedOptions.add(option);
        }
        questionDTO.setOptions(formattedOptions);
    }
}
```

## 相关文档

- [AI 图片识别文档](../ai/image-recognition.md)
- [JSON 导入导出格式](../format/json-import-export.md)
- [LaTeX 公式使用指南](../guide/latex-syntax.md)
