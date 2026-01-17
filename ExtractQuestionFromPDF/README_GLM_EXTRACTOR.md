# GLM智能题目提取工具

## 📖 功能说明

使用智谱GLM-4大模型智能提取Markdown文件中的考试题目，并输出符合项目规范的JSON格式。

### ✨ 核心优势

1. **智能识别**：无需严格的正则表达式，GLM能理解各种混乱的题目格式
2. **准确提取**：自动识别题目类型（单选/多选/填空/简答）
3. **完整信息**：准确提取题干、选项、答案、解析
4. **图片支持**：自动提取并Base64编码图片
5. **标准化输出**：符合项目QuestionImportDTO规范的JSON格式

## 📋 JSON输出格式

```json
{
  "questions": [
    {
      "type": 1,                              // 题目类型：1=单选, 2=多选, 3=填空, 4=简答
      "content": "题干内容，可能包含[图片:img_0]等图片标记",
      "options": [                            // 选择题必填，其他题型为空数组
        "A. 选项1",
        "B. 选项2",
        "C. 选项3",
        "D. 选项4"
      ],
      "answer": "A",                          // 单选填字母，多选用逗号分隔如"A,B"
      "analysis": "解析内容，可能包含图片标记",
      "tags": [],                             // 可选字段
      "source": "题目来源"                    // 可选字段
    }
  ],
  "images": [                                // 可选，包含图片信息
    {
      "id": "img_0",
      "filename": "image1.jpg",
      "base64": "data:image/jpeg;base64,/9j/4AAQ..."
    }
  ]
}
```

## 🚀 使用方法

### 1. 安装依赖

```bash
pip install zhipuai
```

### 2. 配置API Key

编辑 `glm_extract_md_to_json.py` 文件，修改API Key：

```python
API_KEY = "your_api_key_here"  # 替换为你的智谱AI API Key
```

获取API Key：https://open.bigmodel.cn/

### 3. 准备数据

将MD文件放在 `data/` 目录下，文件结构示例：

```
data/
├── 2009年计算机统考真题及解析.md
├── 2010年计算机统考真题及解析.md
│
json_output/          # 输出目录（自动创建）
```

MD文件应该包含：
- 题目内容（选择题部分）
- 答案和解析（可选）
- 图片文件夹 `images/`（如果有图片）

### 4. 运行脚本

```bash
python glm_extract_md_to_json.py
```

### 5. 查看结果

生成的JSON文件保存在 `json_output/` 目录下：

```
json_output/
├── 2009年计算机统考真题及解析.json
├── 2010年计算机统考真题及解析.json
```

## ⚙️ 高级配置

### 单文件处理

如果只想处理单个文件，修改 `main()` 函数：

```python
# 单文件处理示例
single_file = r"F:\Coding\JavaProject\KaoYanPlatform\data\2009年计算机统考真题及解析.md"
result = extractor.parse_questions_with_llm(single_file, max_questions=30)
if result:
    extractor.save_result(result, "output.json")
```

### 调整处理参数

```python
# 批量处理，每个文件最多处理30道题目
extractor.process_directory(
    INPUT_DIR,
    OUTPUT_DIR,
    max_questions=30  # 调整这个值
)
```

### 切换GLM模型

编辑 `parse_questions_with_llm` 方法：

```python
response = self.client.chat.completions.create(
    model="glm-4-flash",     # 快速模型（推荐）
    # model="glm-4",         # 标准模型
    # model="glm-4-plus",    # 高级模型
    messages=[...],
    temperature=0.1,
    max_tokens=8000
)
```

## 📊 与项目集成

生成的JSON文件可以直接用于项目的批量导入接口：

### 后端导入接口

```java
POST /api/questions/import
Content-Type: application/json

{
  "bookId": 1,
  "subjectIds": [101, 102],
  "questions": [
    {
      "type": 1,
      "content": "题干内容",
      "options": ["A. xx", "B. xx", "C. xx", "D. xx"],
      "answer": "A",
      "analysis": "解析内容"
    }
  ]
}
```

### Python调用示例

```python
import requests
import json

# 读取生成的JSON文件
with open("json_output/2009年计算机统考真题及解析.json", "r", encoding="utf-8") as f:
    data = json.load(f)

# 构建导入请求
import_data = {
    "bookId": 1,
    "subjectIds": [101, 102],
    "questions": data["questions"]
}

# 调用后端接口
response = requests.post(
    "http://localhost:8080/api/questions/import",
    json=import_data,
    headers={"Authorization": "Bearer your_token"}
)

print(response.json())
```

## 🔧 故障排查

### 问题1：JSON解析失败

**现象**：提示"JSON解析错误"

**解决方法**：
1. 查看生成的 `debug_glm_response.txt` 文件
2. 检查GLM返回的原始内容
3. 可能需要调整提示词或限制题目数量

### 问题2：题目识别不准确

**现象**：提取的题目类型或答案错误

**解决方法**：
1. 降低temperature参数（已设置为0.1）
2. 换用更强大的模型（如glm-4-plus）
3. 在提示词中增加更多示例

### 问题3：Token超限

**现象**：提示"达到字符限制"

**解决方法**：
1. 减少 `max_questions` 参数（默认20）
2. 减少 `max_chars` 参数（默认12000）
3. 分批处理MD文件

### 问题4：图片未提取

**现象**：图片标记 `[图片:img_x]` 未出现

**解决方法**：
1. 确保MD文件中图片格式为 `![](images/xxx.jpg)`
2. 检查images文件夹是否与MD文件在同一目录
3. 确认图片文件存在且可读

## 📈 性能优化建议

1. **批量处理**：建议每次处理10-20个文件，避免API限流
2. **模型选择**：
   - `glm-4-flash`：速度快，成本低，适合简单题目
   - `glm-4`：平衡性能和成本
   - `glm-4-plus`：最强能力，适合复杂题目
3. **并行处理**：可以同时运行多个脚本实例处理不同文件

## 💡 最佳实践

1. **预处理MD文件**：
   - 确保题目编号清晰（如"1．"、"2．"）
   - 选项格式统一（A. B. C. D.）
   - 答案和解析部分清晰标注

2. **质量控制**：
   - 先用少量文件测试，验证提取效果
   - 检查生成的JSON文件，确认格式正确
   - 如有问题，调整提示词后重新处理

3. **数据备份**：
   - 保留原始MD文件
   - 备份生成的JSON文件
   - 记录每次处理的参数和结果

## 📝 与现有脚本对比

| 特性 | process_md_with_llm.py | glm_extract_md_to_json.py |
|------|----------------------|--------------------------|
| 代码结构 | 函数式 | 面向对象 |
| 错误处理 | 基础 | 完善 |
| 数据验证 | 无 | 有 |
| 文档说明 | 简单 | 详细 |
| JSON格式 | 基础 | 符合项目规范 |
| 可配置性 | 低 | 高 |
| 集成性 | 待完善 | 开箱即用 |

## 🤝 贡献

如有问题或建议，请提交Issue或Pull Request。

## 📄 许可

本项目遵循项目整体许可协议。
