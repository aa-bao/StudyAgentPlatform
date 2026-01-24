# AI 图片识别详细文档

## 概述

本系统使用智谱 **GLM-4.6V-Flash** 多模态大模型实现题目图片识别功能。该模型完全免费，支持图像识别和文本理解，并能自动识别数学公式并转换为 LaTeX 格式。

## 模型介绍

### GLM-4.6V-Flash

- **价格**：完全免费，无调用次数限制
- **能力**：多模态理解（图像 + 文本）
- **优势**：
  - 支持数学公式识别
  - 支持手写和印刷体
  - 高准确率
  - 快速响应

### API 信息

- **接口地址**：`https://open.bigmodel.cn/api/paas/v4/chat/completions`
- **模型名称**：`glm-4.6v-flash`
- **认证方式**：Bearer Token

## 功能特性

### 1. 题目类型自动识别

系统能自动识别以下题型：
- 单选题（type=1）
- 多选题（type=2）
- 填空题（type=3）
- 简答题（type=4）

### 2. 数学公式识别

支持识别的数学符号和公式：

| 类型 | 示例 | LaTeX |
|------|------|-------|
| 分数 | $\frac{a}{b}$ | `\frac{a}{b}` |
| 根号 | $\sqrt{x}$ | `\sqrt{x}` |
| 积分 | $\int_{a}^{b} f(x)dx$ | `\int_{a}^{b} f(x)dx` |
| 求和 | $\sum_{i=1}^{n}$ | `\sum_{i=1}^{n}` |
| 极限 | $\lim_{x \to 0}$ | `\lim_{x \to 0}` |
| 上下标 | $x^2$, $x_1$ | `x^2`, `x_1` |
| 希腊字母 | $\alpha$, $\beta$, $\theta$ | `\alpha`, `\beta`, `\theta` |
| 矩阵 | $\begin{pmatrix} a & b \\ c & d \end{pmatrix}$ | `\begin{pmatrix} a & b \\ c & d \end{pmatrix}` |

### 3. 结构化数据提取

识别并提取以下信息：
- 题干内容（content）
- 选项（options）
- 正确答案（answer）
- 解析（analysis）
- 标签（tags）
- 难度等级（difficulty）

## 后端实现

### 服务层架构

```
Controller (QuestionController.java)
    ↓
Service (GLMService.java)
    ↓
Implementation (GLMServiceImpl.java)
    ↓
GLM API (https://open.bigmodel.cn)
```

### API 配置

**application.yml**:

```yaml
zhipu:
  api:
    key: your-zhipu-api-key-here
```

### 核心代码

#### 1. 服务接口

**GLMService.java**:

```java
public interface GLMService {
    /**
     * 使用GLM-4.6V-Flash识别图片中的题目内容
     * @param file 图片文件
     * @return 识别后的题目DTO
     */
    QuestionDTO recognizeQuestionFromImage(MultipartFile file);
}
```

#### 2. 服务实现

**GLMServiceImpl.java** 核心流程：

```java
@Override
public QuestionDTO recognizeQuestionFromImage(MultipartFile file) {
    // 1. 文件验证
    if (file.isEmpty()) {
        throw new IllegalArgumentException("请上传图片文件");
    }

    // 2. 图片转 Base64
    byte[] imageBytes = file.getBytes();
    String base64Image = Base64.getEncoder().encodeToString(imageBytes);

    // 3. 构造请求
    Map<String, Object> requestBody = new HashMap<>();
    requestBody.put("model", "glm-4.6v-flash");

    // 4. 构造消息（文本 + 图片）
    List<Map<String, Object>> messages = new ArrayList<>();
    Map<String, Object> userMessage = new HashMap<>();
    userMessage.put("role", "user");

    List<Map<String, Object>> messageContent = new ArrayList<>();

    // 文本提示词
    Map<String, Object> textContent = new HashMap<>();
    textContent.put("type", "text");
    textContent.put("text", buildPrompt()); // 构造详细的提示词
    messageContent.add(textContent);

    // 图片内容
    Map<String, Object> imageContent = new HashMap<>();
    imageContent.put("type", "image_url");
    imageContent.put("image_url", Map.of("url", base64Image));
    messageContent.add(imageContent);

    userMessage.put("content", messageContent);
    messages.add(userMessage);
    requestBody.put("messages", messages);

    // 5. 发送请求
    ResponseEntity<String> response = restTemplate.exchange(
        API_URL,
        HttpMethod.POST,
        new HttpEntity<>(requestBody, headers),
        String.class
    );

    // 6. 解析响应
    // ... JSON 解析和 QuestionDTO 构造
}
```

#### 3. 提示词构造

系统使用结构化提示词确保识别准确性：

```java
private String buildPrompt() {
    return "你是一个专业的数学题目识别助手。请识别图片中的题目内容，并以JSON格式返回。\n\n" +
           "【题目类型】1-单选, 2-多选, 3-填空, 4-简答\n\n" +
           "【LaTeX公式处理要求】\n" +
           "- 题目中的数学公式必须用LaTeX语法表示\n" +
           "- 行内公式用 $...$ 包裹\n" +
           "- 独立公式用 $$...$$ 包裹\n" +
           "- 支持常见LaTeX数学符号：分数\\frac{}{}、上下标_^{}、积分\\int、求和\\sum、根号\\sqrt{}、希腊字母等\n\n" +
           "【JSON格式要求】\n" +
           "{\n" +
           "  \"type\": 1,\n" +
           "  \"content\": \"题目内容（含LaTeX公式）\",\n" +
           "  \"options\": [\"选项A（含LaTeX公式）\", ...],\n" +
           "  \"answer\": \"答案（含LaTeX公式）\",\n" +
           "  \"analysis\": \"解析（含LaTeX公式）\",\n" +
           "  \"tags\": [\"标签1\", \"标签2\"],\n" +
           "  \"difficulty\": 3\n" +
           "}\n\n" +
           "【注意事项】\n" +
           "1. 所有数学符号、公式、表达式必须用LaTeX表示并用$或$$包裹\n" +
           "2. 纯粹返回JSON，不要有其他说明文字";
}
```

#### 4. JSON 转义处理

AI 返回的 JSON 可能包含未正确转义的反斜杠和控制字符，需要处理：

```java
private String fixLaTeXEscaping(String json) {
    StringBuilder result = new StringBuilder();
    boolean inString = false;
    boolean escapeNext = false;

    for (int i = 0; i < json.length(); i++) {
        char c = json.charAt(i);

        if (!inString) {
            if (c == '"') {
                inString = true;
                result.append(c);
            } else {
                result.append(c);
            }
        } else {
            if (escapeNext) {
                // 检查是否是有效的 JSON 转义序列
                if (c == '"' || c == '\\' || c == '/' || c == 'b' ||
                    c == 'f' || c == 'n' || c == 'r' || c == 't' || c == 'u') {
                    result.append('\\').append(c);
                } else {
                    // LaTeX 反斜杠，需要转义
                    result.append("\\\\").append(c);
                }
                escapeNext = false;
            } else if (c == '\\') {
                escapeNext = true;
            } else if (c == '"') {
                inString = false;
                result.append(c);
            } else {
                // 处理控制字符
                switch (c) {
                    case '\n': result.append("\\n"); break;
                    case '\r': result.append("\\r"); break;
                    case '\t': result.append("\\t"); break;
                    default:
                        if (c < 32) {
                            result.append(String.format("\\u%04x", (int) c));
                        } else {
                            result.append(c);
                        }
                }
            }
        }
    }

    return result.toString();
}
```

#### 5. 选项格式转换

将字符串数组转换为对象数组：

```java
List<Map<String, String>> formattedOptions = new ArrayList<>();
String[] labels = {"A", "B", "C", "D"};
for (int i = 0; i < optionsList.size() && i < labels.length; i++) {
    Map<String, String> option = new HashMap<>();
    option.put("label", labels[i]);
    option.put("text", optionsList.get(i));
    formattedOptions.add(option);
}
questionDTO.setOptions(formattedOptions);
```

## 前端实现

### API 调用

**questionImportExport.js**:

```javascript
export function recognizeQuestion(file) {
    const formData = new FormData()
    formData.append('file', file)

    return request({
        url: '/question/recognize',
        method: 'post',
        data: formData,
        headers: {
            'Content-Type': 'multipart/form-data'
        }
    })
}
```

### 组件使用

**QuestionManage.vue**:

```vue
<template>
    <el-upload
        :action="uploadUrl"
        :show-file-list="false"
        :before-upload="handleBeforeUpload"
        :on-success="handleSuccess"
        :on-error="handleError"
        accept="image/*"
    >
        <el-button type="primary">GLM AI 识别</el-button>
    </el-upload>
</template>

<script setup>
const handleSuccess = (response) => {
    if (response.code === 200) {
        // 填充表单
        form.value = {
            ...response.data,
            options: response.data.options || []
        }
        ElMessage.success('GLM AI 识别成功！请检查填充的题目内容和选项是否有误。')
    }
}
</script>
```

## 使用示例

### 识别流程

1. **上传图片**
   ```
   用户点击 "GLM AI 识别" 按钮
   → 选择题目图片
   → 前端上传到后端
   ```

2. **后端处理**
   ```
   接收图片文件
   → 转换为 Base64
   → 调用 GLM API
   → 解析 JSON 响应
   → 修复转义问题
   → 转换选项格式
   → 返回 QuestionDTO
   ```

3. **前端填充**
   ```
   接收 QuestionDTO
   → 自动填充表单
   → 用户可以手动调整
   → 保存题目
   ```

### 识别结果示例

**输入图片**：包含数学公式的题目

**输出 JSON**：

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
  "analysis": "根据求导法则，$f'(x) = \\lim_{h \\to 0} \\frac{f(x+h)-f(x)}{h} = 3x^2-3$",
  "tags": ["导数", "基础题"],
  "difficulty": 3
}
```

## 错误处理

### 常见错误及解决方案

#### 1. API 认证失败

**错误**：
```json
{
  "error": {
    "message": "Authentication Fails, Your api key: **** is invalid"
  }
}
```

**解决**：
- 检查 `application.yml` 中的 API Key 是否正确
- 确认 API Key 有效且未过期

#### 2. JSON 解析失败

**错误**：
```
Unrecognized character escape 'e' (code 101)
```

**原因**：AI 返回的 JSON 包含未正确转义的 LaTeX 反斜杠

**解决**：使用 `fixLaTeXEscaping` 方法预处理 JSON

#### 3. 图片格式不支持

**错误**：
```
只支持图片文件
```

**解决**：
- 确保上传的是图片文件（jpg、png等）
- 检查 `contentType` 是否以 `image/` 开头

## 性能优化

### 1. 超时设置

```java
// RestTemplate 配置
SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
factory.setConnectTimeout(10000); // 10秒连接超时
factory.setReadTimeout(30000);    // 30秒读取超时
```

### 2. 重试机制

```java
@Retryable(
    value = {RestClientException.class},
    maxAttempts = 3,
    backoff = @Backoff(delay = 1000)
)
public QuestionDTO recognizeQuestionFromImage(MultipartFile file) {
    // ...
}
```

### 3. 缓存（可选）

对已识别的图片进行缓存，避免重复调用 API：

```java
@Cacheable(value = "question-recognition", key = "#file.originalFilename")
public QuestionDTO recognizeQuestionFromImage(MultipartFile file) {
    // ...
}
```

## 最佳实践

### 1. 图片质量

- ✅ 使用高清图片（建议分辨率 ≥ 1280x720）
- ✅ 确保光线充足
- ✅ 避免模糊和倾斜
- ✅ 公式部分清晰可见

### 2. 提示词优化

- 明确指定返回格式（JSON）
- 提供示例和模板
- 强调 LaTeX 公式要求
- 说明不要有多余文字

### 3. 结果验证

AI 识别后，建议用户手动检查：
- 题目内容是否正确
- 选项是否完整
- 答案是否准确
- 公式是否正确渲染

## 相关文档

- [数据库设计文档](../database/question-table-design.md)
- [JSON 导入导出格式](../format/json-import-export.md)
- [LaTeX 公式使用指南](../guide/latex-syntax.md)
