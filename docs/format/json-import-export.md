# JSON 导入导出格式文档

## 概述

本文档详细说明了题目批量导入的 JSON 格式规范，包括请求结构、字段说明、示例数据等。

## 导入格式

### 完整结构

```json
{
  "subjectIds": [1, 2],
  "bookId": 1,
  "newBookName": "新建习题册名称",
  "checkDuplicate": true,
  "questions": [
    {
      "type": 1,
      "content": "题目内容",
      "options": [...],
      "answer": "答案",
      "analysis": "解析",
      "tags": ["标签1"],
      "source": "来源",
      "difficulty": 3
    }
  ]
}
```

### 字段说明

#### 顶层字段

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| subjectIds | Number[] | 是 | 科目ID列表，支持多个科目 |
| bookId | Number | 条件 | 习题册ID（与 newBookName 二选一） |
| newBookName | String | 条件 | 新建习题册名称（与 bookId 二选一） |
| checkDuplicate | Boolean | 否 | 是否启用去重检查，默认 true |
| questions | Array | 是 | 题目列表 |

#### 题目字段（QuestionImportItem）

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| type | Number | 是 | 题目类型：1-单选, 2-多选, 3-填空, 4-简答 |
| content | String | 是 | 题干内容，支持 LaTeX 公式 |
| options | Array | 条件 | 选项数组（选择题必填） |
| answer | String | 是 | 正确答案 |
| analysis | String | 否 | 解析内容 |
| tags | Array | 否 | 标签数组 |
| source | String | 否 | 题目来源 |
| difficulty | Number | 否 | 难度 1-5，默认 3 |

## 题目类型示例

### 1. 单选题（type=1）

```json
{
  "type": 1,
  "content": "设函数 $f(x) = x^3 - 3x + 1$，求 $f'(x)$",
  "options": [
    {"label": "A", "text": "$$3x^2-3$$"},
    {"label": "B", "text": "$$3x^2+3$$"},
    {"label": "C", "text": "$$x^2-3$$"},
    {"label": "D", "text": "$$x^2+3$$"}
  ],
  "answer": "A",
  "analysis": "根据求导法则，$f'(x) = 3x^2-3$",
  "tags": ["导数", "基础题"],
  "source": "高等数学习题集",
  "difficulty": 3
}
```

**要求**：
- 必须包含 4 个选项
- 答案为单个字母（A/B/C/D）

### 2. 多选题（type=2）

```json
{
  "type": 2,
  "content": "下列哪些函数在区间 $(0, +\\infty)$ 上单调递增？",
  "options": [
    {"label": "A", "text": "$$f(x) = x^2$$"},
    {"label": "B", "text": "$$f(x) = e^x$$"},
    {"label": "C", "text": "$$f(x) = \\ln(x)$$"},
    {"label": "D", "text": "$$f(x) = \\frac{1}{x}$$"}
  ],
  "answer": "ABC",
  "analysis": "$x^2$在$x>0$时单调递增；$e^x$始终单调递增；$\\ln(x)$在定义域内单调递增；$\\frac{1}{x}$在$x>0$时单调递减",
  "tags": ["单调性", "多选题"],
  "difficulty": 4
}
```

**要求**：
- 必须包含 4 个选项
- 答案为字母组合（AB/ABC/ABCD）

### 3. 填空题（type=3）

```json
{
  "type": 3,
  "content": "计算定积分：$\\int_0^1 x^2 dx = $ ______",
  "answer": "$$\\frac{1}{3}$$",
  "analysis": "根据定积分计算公式：$\\int_0^1 x^2 dx = [\\frac{x^3}{3}]_0^1 = \\frac{1}{3}$",
  "tags": ["积分", "填空题"],
  "source": "高等数学例题",
  "difficulty": 2
}
```

**要求**：
- 不需要 `options` 字段
- 答案为具体内容

### 4. 简答题（type=4）

```json
{
  "type": 4,
  "content": "请论述函数 $f(x)$ 在点 $x_0$ 处可导的充分必要条件。",
  "answer": "函数 $f(x)$ 在点 $x_0$ 处可导的充分必要条件是：左导数 $f'_{-}(x_0)$ 和右导数 $f'_{+}(x_0)$ 都存在且相等。",
  "analysis": "本题考查导数存在的条件。根据导数定义，函数在某点可导要求左右导数都存在且相等。这是判断函数可导性的基本准则。",
  "tags": ["导数", "简答题", "理论"],
  "difficulty": 5
}
```

**要求**：
- 不需要 `options` 字段
- 答案可以是长文本

## 选项格式

### 格式 1（旧格式，兼容）

字符串数组格式：

```json
"options": [
  "3x^2 - 3",
  "3x^2 + 3",
  "x^2 - 3",
  "x^2 + 3"
]
```

### 格式 2（新格式，推荐）

对象数组格式：

```json
"options": [
  {"label": "A", "text": "3x^2 - 3"},
  {"label": "B", "text": "3x^2 + 3"},
  {"label": "C", "text": "x^2 - 3"},
  {"label": "D", "text": "x^2 + 3"}
]
```

### 格式对比

| 特性 | 格式 1（字符串） | 格式 2（对象） |
|------|-----------------|----------------|
| 兼容性 | ✅ 兼容旧版 | ✅ 新版标准 |
| 自定义标签 | ❌ 固定 A/B/C/D | ✅ 可自定义 |
| 扩展性 | ❌ 难扩展 | ✅ 易扩展 |
| LaTeX 支持 | ✅ 支持 | ✅ 支持 |

### 后端自动转换

无论使用哪种格式，后端都会自动转换为对象数组：

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

## LaTeX 公式支持

### 行内公式

使用 `$...$` 包裹，嵌入文本中：

```json
{
  "content": "设函数 $f(x) = x^3 - 3x + 1$，求导数"
}
```

### 块级公式

使用 `$$...$$` 包裹，独立成行：

```json
{
  "answer": "$$\\frac{1}{3}$$"
}
```

### 常用符号

| 符号 | 显示 | LaTeX |
|------|------|-------|
| 分数 | $\frac{a}{b}$ | `\frac{a}{b}` |
| 根号 | $\sqrt{x}$ | `\sqrt{x}` |
| 上标 | $x^2$ | `x^2` |
| 下标 | $x_1$ | `x_1` |
| 积分 | $\int$ | `\int` |
| 求和 | $\sum$ | `\sum` |
| 极限 | $\lim$ | `\lim` |
| 希腊字母 | $\alpha$, $\beta$ | `\alpha`, `\beta` |

## 完整示例

### 示例 1：单选题带公式

```json
{
  "subjectIds": [1],
  "bookId": null,
  "newBookName": "高等数学测试题集",
  "checkDuplicate": true,
  "questions": [
    {
      "type": 1,
      "content": "已知 $\\sin(\\alpha + \\beta) = \\frac{3}{5}$，$\\cos \\alpha = \\frac{4}{5}$，则 $\\sin \\beta = $",
      "options": [
        {"label": "A", "text": "$$\\frac{1}{5}$$"},
        {"label": "B", "text": "$$\\frac{2}{5}$$"},
        {"label": "C", "text": "$$\\frac{3}{5}$$"},
        {"label": "D", "text": "$$\\frac{4}{5}$$"}
      ],
      "answer": "A",
      "analysis": "利用三角恒等式：$\\sin(\\alpha + \\beta) = \\sin\\alpha\\cos\\beta + \\cos\\alpha\\sin\\beta$\n由 $\\cos\\alpha = \\frac{4}{5}$ 得 $\\sin\\alpha = \\frac{3}{5}$（假设 $\\alpha$ 为锐角）\n代入计算可得 $\\sin\\beta = \\frac{1}{5}$",
      "tags": ["三角函数", "计算题"],
      "source": "高等数学例题",
      "difficulty": 4
    }
  ]
}
```

### 示例 2：多题目批量导入

```json
{
  "subjectIds": [1],
  "bookId": 1,
  "checkDuplicate": true,
  "questions": [
    {
      "type": 1,
      "content": "设函数 $f(x) = x^3 - 3x + 1$，求 $f'(x)$",
      "options": [
        {"label": "A", "text": "$$3x^2-3$$"},
        {"label": "B", "text": "$$3x^2+3$$"},
        {"label": "C", "text": "$$x^2-3$$"},
        {"label": "D", "text": "$$x^2+3$$"}
      ],
      "answer": "A",
      "analysis": "根据求导法则，$f'(x) = 3x^2-3$",
      "tags": ["导数"],
      "difficulty": 3
    },
    {
      "type": 2,
      "content": "下列哪些函数在 $(0, +\\infty)$ 上单调递增？",
      "options": [
        {"label": "A", "text": "$$x^2$$"},
        {"label": "B", "text": "$$e^x$$"},
        {"label": "C", "text": "$$\\ln(x)$$"},
        {"label": "D", "text": "$$\\frac{1}{x}$$"}
      ],
      "answer": "ABC",
      "analysis": "A、B、C 正确",
      "tags": ["单调性"],
      "difficulty": 4
    },
    {
      "type": 3,
      "content": "$\\int_0^1 x^2 dx = $ ______",
      "answer": "$$\\frac{1}{3}$$",
      "analysis": "$\\int_0^1 x^2 dx = [\\frac{x^3}{3}]_0^1 = \\frac{1}{3}$",
      "tags": ["积分"],
      "difficulty": 2
    },
    {
      "type": 4,
      "content": "简述导数的几何意义",
      "answer": "导数 $f'(x_0)$ 表示曲线 $y=f(x)$ 在点 $(x_0, f(x_0))$ 处的切线斜率",
      "analysis": "导数的几何意义是切线斜率，反映函数在该点的瞬时变化率",
      "tags": ["导数", "概念"],
      "difficulty": 3
    }
  ]
}
```

## 导入流程

### 1. 准备 JSON 文件

创建一个 `.json` 文件，按照上述格式编写题目数据。

### 2. 前端操作

1. 进入「题库管理」页面
2. 点击「批量导入」按钮
3. 选择科目和习题册
   - 可选择现有习题册
   - 或新建习题册（填写名称）
4. 勾选「去重检查」（可选）
5. 上传 JSON 文件
6. 预览题目内容
7. 点击「导入」按钮

### 3. 后端处理

```java
@PostMapping("/import")
public Result importQuestions(@RequestBody QuestionImportDTO importDTO) {
    // 1. 验证科目
    // 2. 验证/创建习题册
    // 3. 遍历题目列表
    //    a. 去重检查（可选）
    //    b. 转换选项格式
    //    c. 保存题目
    // 4. 返回导入结果
}
```

### 4. 导入结果

成功响应：

```json
{
  "code": 200,
  "message": "导入完成！成功: 10, 跳过重复: 2, 失败: 0",
  "data": null
}
```

失败响应：

```json
{
  "code": 500,
  "message": "导入失败: 题目列表不能为空",
  "data": null
}
```

## 错误处理

### 常见错误

#### 1. 题目列表为空

```json
{
  "code": 500,
  "message": "题目列表不能为空"
}
```

**解决**：确保 `questions` 数组不为空

#### 2. 科目不存在

```json
{
  "code": 500,
  "message": "科目ID: 999 不存在"
}
```

**解决**：检查 `subjectIds` 是否正确

#### 3. JSON 格式错误

```json
{
  "code": 500,
  "message": "JSON 解析失败: Unexpected character..."
}
```

**解决**：检查 JSON 语法是否正确

#### 4. 题目内容为空

```json
{
  "code": 500,
  "message": "题目导入失败: 题目内容不能为空"
}
```

**解决**：确保 `content` 字段不为空

## 最佳实践

### 1. 文件命名

```
questions_高等数学_20250124.json
questions_线性代数_第一章.json
```

### 2. 数据验证

导入前验证：
- ✅ JSON 格式正确
- ✅ 科目 ID 存在
- ✅ 选项数量正确（选择题 4 个）
- ✅ 答案格式正确
- ✅ LaTeX 公式正确

### 3. 分批导入

- 建议每批不超过 100 题
- 大量题目分多个文件导入
- 先测试小批量，确认无误后再导入全部

### 4. 备份数据

导入前：
- ✅ 备份现有数据库
- ✅ 保留原始 JSON 文件
- ✅ 记录导入时间和数量

## API 接口

### POST /question/import

**请求头**：
```
Content-Type: application/json
```

**请求体**：见上述 JSON 格式

**响应**：
```json
{
  "code": 200,
  "message": "导入完成！成功: 10, 跳过重复: 2, 失败: 0"
}
```

## 相关文档

- [数据库设计文档](../database/question-table-design.md)
- [AI 图片识别文档](../ai/image-recognition.md)
- [LaTeX 公式使用指南](../guide/latex-syntax.md)
