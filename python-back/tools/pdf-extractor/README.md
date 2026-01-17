# PDF题目提取与转换工具

## 📖 概述

本工具用于将考研真题PDF转换为平台可导入的JSON格式，支持题目、答案、解析、图片的自动提取。

**核心优势**：使用LLM（智谱AI）处理，比正则表达式更智能，能适应各种混乱的格式。

---

## 🚀 快速开始

### 1. 安装依赖

```bash
pip install zhipuai
```

### 2. 配置API Key

1. 访问 [智谱AI开放平台](https://open.bigmodel.cn/) 注册并获取API Key
2. 复制 `config.example.py` 为 `config.py`
3. 填入你的API Key：

```python
ZHIPU_API_KEY = "your_api_key_here"
```

### 3. 使用MinerU提取PDF为Markdown

```bash
# 安装MinerU（参考官方文档）
pip install minerU

# 提取PDF
minerU_pdf_to_markdown input.pdf -o output_dir
```

### 4. 转换为JSON

```bash
# 单文件处理
python process_md_with_llm.py

# 批量处理
python process_md_with_llm.py --batch
```

---

## 📂 文件说明

```
ExtractQuestionFromPDF/
├── process_md_with_llm.py    # 主脚本：LLM处理MD文件
├── config.example.py          # 配置示例
├── convert_md_to_json.py      # 旧版：正则表达式处理（不推荐）
└── README.md                  # 本文档
```

---

## 📝 输出格式

生成的JSON文件包含两部分：

### 1. questions数组

```json
{
  "type": 1,          // 题目类型：1-单选, 2-多选, 3-填空, 4-简答
  "content": "题干",   // 支持[图片:img_0]引用
  "options": [...],    // 选项数组
  "answer": "A",       // 答案
  "analysis": "...",   // 解析
  "tags": [...]        // 标签
}
```

### 2. images数组（可选）

```json
{
  "id": "img_0",                                    // 图片ID
  "filename": "c5f6459ba2900a7f.jpg",              // 原始文件名
  "base64": "data:image/jpeg;base64,/9j/4AAQ..."  // Base64数据
}
```

---

## ⚙️ 高级配置

编辑 `config.py` 调整参数：

```python
# 模型选择
MODEL_NAME = "glm-4-flash"  # 推荐：快速便宜
# MODEL_NAME = "glm-4-plus"  # 更强大但慢

# 温度参数（0-1，越低越稳定）
TEMPERATURE = 0.3

# 失败重试次数
MAX_RETRIES = 3
```

---

## 🎯 使用示例

### 示例1: 处理单个文件

```python
from process_md_with_llm import parse_questions_with_llm

result = parse_questions_with_llm("2009年计算机统考真题及解析.md")

print(f"提取了 {len(result['questions'])} 道题目")
print(f"包含 {len(result['images'])} 张图片")
```

### 示例2: 批量处理

```python
from process_md_with_llm import process_directory

process_directory(
    input_dir=r"F:\data\markdown",
    output_dir=r"F:\data\json"
)
```

---

## 🔧 常见问题

### Q1: API调用失败怎么办？

**A**: 检查以下几点：
1. API Key是否正确
2. 账户余额是否充足
3. 网络连接是否正常
4. 模型名称是否正确（推荐使用 `glm-4-flash`）

### Q2: 图片无法显示？

**A**: 确保：
1. MD文件旁边有 `images/` 文件夹
2. 图片文件名与MD中的引用一致
3. 图片格式支持（jpg, png等）

### Q3: JSON格式错误？

**A**:
1. 检查LLM返回的内容是否被markdown代码块包裹
2. 脚本已自动处理 ```json 标记
3. 如仍有问题，手动清理输出内容

### Q4: 题目解析不准确？

**A**:
1. 降低 `TEMPERATURE` 参数（如0.1）
2. 切换到更强大的模型（`glm-4-plus`）
3. 检查MD文件格式是否过于混乱

---

## 💡 最佳实践

1. **先测试单个文件**：确认效果后再批量处理
2. **检查输出结果**：人工抽查几道题目的准确性
3. **使用flash模型**：速度更快，成本更低
4. **保存原始MD**：便于重新处理

---

## 📊 成本估算

以 `glm-4-flash` 模型为例：

- 输入：¥0.1/千tokens
- 输出：¥0.1/千tokens
- 一套真题约2000tokens，成本约¥0.2

---

## 🤝 贡献

如有问题或建议，请联系项目维护者。

---

**最后更新**: 2026-01-17
