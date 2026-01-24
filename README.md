# KaoYanPlatform 考研刷题平台

一款面向考研学生的在线刷题与学习管理平台，采用现代化的前后端分离架构设计。

## 项目简介

KaoYanPlatform 提供完整的考研学习解决方案，包括：

- **多模式刷题**: 逐题精练、专项突破、真题模考、套卷刷题
- **AI 智能批改**: 集成智谱 GLM-4.7，主观题自动评分与反馈
- **智能题库管理**: 支持 PDF/Markdown 题目智能提取、批量导入导出
- **学习数据分析**: ECharts 雷达图可视化展示学习进度
- **错题本系统**: 自动收藏错题、标签分类、重做巩固

## 核心亮点

| 特性 | 说明 |
|------|------|
| **AI 智能批改** | 主观题使用 GLM-4.7 自动批改，提供详细反馈 |
| **AI 图片识别** | 使用 GLM-4.6V-Flash 自动识别图片中的题目，支持 LaTeX 数学公式 |
| **题目智能提取** | 基于 LLM 的 PDF 题目结构化提取，支持复杂格式 |
| **多层级科目体系** | 具体考试科目 → 知识模块 → 知识点 → 题型，支持 scope 多对多映射 |
| **JSON 字段优化** | 题目数据采用 JSON 字段存储，减少冗余，提高灵活性 |
| **LaTeX 公式支持** | 全面支持数学公式，前端 KaTeX 渲染，AI 自动识别 |
| **考试会话增强** | 倒计时持久化、浏览器返回阻止、切屏检测、答题快照恢复 |
| **未完成考试提醒** | 智能检测未完成考试，强制弹窗提醒继续 |
| **批量导入导出** | 支持 JSON 格式批量导入，PDF 格式导出（支持 LaTeX 公式） |

## 技术栈

### 后端

- **框架**: Spring Boot 3.3.5 + Spring Security
- **数据层**: MyBatis Plus 3.5.5 + MySQL 8.0
- **AI 集成**:
  - 智谱 AI SDK (GLM-4.7) - 智能批改
  - GLM-4.6V-Flash (完全免费) - 图片识别
- **API 文档**: Knife4j 4.5.0
- **PDF 生成**: Flying Saucer + Thymeleaf

### 前端

- **框架**: Vue.js 3.5.26 + Vite 7.3.0
- **状态管理**: Pinia 3.0.4
- **路由**: Vue Router 4.6.4
- **UI 组件**: Element Plus 2.13.0
- **图表**: ECharts 6.0.0
- **数学公式**: KaTeX 0.16.27

### Python 工具集

- **AI 模型**: DeepSeek-v1, 智谱 GLM-4
- **PDF 处理**: MinerU, PaddleOCR

## 快速启动

### 环境要求

- JDK 17+
- Maven 3.6+
- MySQL 8.0+
- Node.js 16+

### 后端启动

```bash
cd KaoYanPlatform
mvn spring-boot:run
```

访问: http://localhost:8081 | Swagger: http://localhost:8081/doc.html

### 前端启动

```bash
cd vue-front
npm install
npm run dev
```

访问: http://localhost:5173

### 默认账号

| 角色 | 用户名 | 密码 |
|------|--------|------|
| 管理员 | admin | 123123 |
| 普通用户 | student | 123123 |

## 文档导航

### 安装与部署

- [安装与部署指南](docs/guide/install.md) - 环境配置、数据库初始化、项目启动

### 架构设计

- [项目架构设计](docs/architecture/design.md) - 项目亮点、技术栈、数据模型、AI 集成

### 功能模块

- [核心功能模块](docs/features/modules.md) - 题目管理、套卷刷题、批量导入导出、未完成考试提醒

### 实现细节

- [前端架构与核心实现](docs/features/implementation-details.md) - 路由权限、状态管理、树形结构、拖拽排序

## 数据库设计

### tb_question 表结构（优化版）

采用 **JSON 字段** 设计，将题目内容、选项、答案、解析、标签、来源整合为单一 JSON 字段：

```sql
CREATE TABLE tb_question (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '题目ID',
    type TINYINT NOT NULL COMMENT '题目类型：1-单选, 2-多选, 3-填空, 4-简答',
    content_json JSON NOT NULL COMMENT '题目完整JSON数据',
    difficulty TINYINT DEFAULT 3 COMMENT '难度：1-5',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_type (type),
    INDEX idx_difficulty (difficulty)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='题目表';
```

### content_json 结构示例

**单选/多选题**：

```json
{
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
  "source": "高等数学习题集"
}
```

**填空/简答题**：

```json
{
  "content": "计算定积分：$\\int_0^1 x^2 dx = $ ______",
  "answer": "$$\\frac{1}{3}$$",
  "analysis": "根据定积分计算公式：$\\int_0^1 x^2 dx = [\\frac{x^3}{3}]_0^1 = \\frac{1}{3}$",
  "tags": ["积分", "填空题"],
  "source": "高等数学例题"
}
```

## AI 图片识别

### 使用 GLM-4.6V-Flash 模型

- **完全免费**：无调用次数限制
- **多模态**：支持图像识别 + 文本理解
- **LaTeX 支持**：自动识别并转换数学公式为 LaTeX 格式

### 识别能力

- ✅ 数学公式识别（LaTeX 格式）
- ✅ 手写/印刷体识别
- ✅ 自动判断题目类型（单选/多选/填空/简答）
- ✅ 提取选项、答案、解析
- ✅ 自动生成难度等级

### LaTeX 公式格式

- **行内公式**：使用 `$...$` 包裹

  ```text
  设函数 $f(x) = x^3 - 3x + 1$，求 $f'(x)$
  ```

- **块级公式**：使用 `$$...$$` 包裹

  ```text
  答案：$$3x^2-3$$
  ```

- **常见符号**：
  - 分数：`\frac{a}{b}`
  - 根号：`\sqrt{x}`
  - 积分：`\int_{a}^{b}`
  - 求和：`\sum_{i=1}^{n}`
  - 极限：`\lim_{x \to 0}`
  - 上下标：`x^2`, `x_1`

### API 配置

在 `java-back/src/main/resources/application.yml` 中配置：

```yaml
zhipu:
  api:
    key: your-zhipu-api-key-here
```

## JSON 批量导入

### 导入格式示例

```json
{
  "subjectIds": [1],
  "bookId": null,
  "newBookName": "高等数学测试题集",
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
      "tags": ["导数", "基础题"],
      "source": "高等数学习题集",
      "difficulty": 3
    }
  ]
}
```

### 选项格式兼容

系统支持两种选项格式（自动转换）：

**格式 1（旧格式，兼容）**：字符串数组

```json
"options": ["选项1", "选项2", "选项3", "选项4"]
```

**格式 2（新格式，推荐）**：对象数组

```json
"options": [
  {"label": "A", "text": "选项1"},
  {"label": "B", "text": "选项2"},
  {"label": "C", "text": "选项3"},
  {"label": "D", "text": "选项4"}
]
```

### 导入步骤

1. 在「题库管理」页面点击「批量导入」
2. 选择科目和习题册（或新建习题册）
3. 上传 JSON 文件
4. 预览题目内容
5. 点击导入，查看结果统计

## 项目结构

```text
KaoYanPlatform/
├── java-back/          # Java 后端服务 (端口: 8081)
├── vue-front/          # Vue 前端应用 (端口: 5173)
├── python-back/        # Python 数据处理工具集
├── docs/               # 项目文档
│   ├── guide/          # 安装指南
│   ├── architecture/   # 架构设计
│   └── features/       # 功能模块
└── README.md
```

## 技术亮点

### 1. JSON 字段优化设计

- **减少冗余**：将题目内容、选项、答案、解析、标签、来源整合为单一 JSON 字段
- **提高灵活性**：不同题型可以有差异化的 JSON 结构
- **自动序列化**：利用 MyBatis-Plus JacksonTypeHandler 自动处理

### 2. 分层架构重构

- **Controller 层**：仅负责接收请求和返回响应
- **Service 层**：封装业务逻辑（AI 调用、数据处理）
- **符合规范**：遵循 Spring Boot 最佳实践

### 3. AI 智能识别

- **GLM-4.6V-Flash**：完全免费的多模态大模型
- **LaTeX 支持**：自动识别数学公式并转换为 LaTeX 格式
- **智能转义**：自动处理 JSON 转义问题和控制字符

### 4. 前端公式渲染

- **KaTeX 集成**：快速渲染 LaTeX 数学公式
- **全面支持**：题目内容、选项、答案、解析均支持公式
- **兼容新旧格式**：自动识别字符串和对象格式选项

### 5. 选项格式升级

- **旧格式**：`["选项1", "选项2", "选项3", "选项4"]`
- **新格式**：`[{"label": "A", "text": "选项1"}, ...]`
- **自动转换**：后端自动识别并转换，无缝升级

## 更新日志

### v2.0.0 (2025-01)

#### 重大更新

- ✨ **数据库结构优化**：使用 JSON 字段替代多个冗余字段
- ✨ **选项格式升级**：支持对象数组格式 `[{label: "A", text: "选项内容"}]`
- ✨ **AI 识别升级**：改用 GLM-4.6V-Flash 模型（完全免费）
- ✨ **LaTeX 全面支持**：AI 识别 + 前端渲染 + PDF 导出
- ♻️ **架构重构**：AI 识别逻辑从 Controller 移到 Service 层

#### 功能改进

- 🎨 **前端渲染优化**：
  - 修复选项显示问题（对象格式渲染）
  - 添加 KaTeX 公式渲染支持
  - 优化导入预览页面
- 🐛 **JSON 解析修复**：
  - 自动修复 AI 返回 JSON 中的反斜杠转义问题
  - 自动处理控制字符（换行符、制表符等）
  - 确保 JSON 解析正确性
- 📦 **选项格式兼容**：同时支持新旧两种选项格式，自动转换

#### 文档更新

- 📝 添加数据库设计说明
- 📝 添加 AI 识别使用指南
- 📝 添加 LaTeX 公式格式说明
- 📝 完善 JSON 导入导出文档

### v1.0.0

- 🎉 基础题库管理功能
- 📦 JSON 批量导入
- 📄 PDF 导出
- 🤖 AI 智能批改（GLM-4.7）
- 📊 学习数据分析

## 许可证

MIT License

