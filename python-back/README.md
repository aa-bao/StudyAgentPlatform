# 考研平台AI服务 (ai_service)

> 提供题目智能提取、模型训练、数据处理等AI功能的Python微服务

[![Python](https://img.shields.io/badge/Python-3.11+-blue.svg)](https://www.python.org/)
[![FastAPI](https://img.shields.io/badge/FastAPI-0.104.1-green.svg)](https://fastapi.tiangolo.com/)
[![License](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)

---

## 📖 项目简介

考研平台AI服务是一个独立的Python微服务，负责处理所有AI相关功能。

### 核心功能

- 🚀 **题目智能提取** - 使用智谱GLM大模型从Markdown中智能提取题目
- 🤖 **模型训练** - 支持题目推荐、难度评估等模型训练（开发中）
- 📊 **数据处理** - 数据清洗、特征工程、数据转换（开发中）
- 🎯 **推荐系统** - 基于AI的个性化题目推荐（开发中）

### 系统定位

```
┌─────────────────────────────────────────┐
│          Java主系统                       │
│  (用户管理、权限控制、文件处理)            │
└──────────────┬──────────────────────────┘
               │ HTTP REST API
               ↓
┌─────────────────────────────────────────┐
│       Python AI服务 (本项目)             │
│  (题目提取、模型训练、数据处理)           │
└─────────────────────────────────────────┘
```

---

## 🚀 快速开始

### 环境要求

- Python 3.11+
- pip

### 安装步骤

```bash
# 1. 克隆项目
cd ai_service

# 2. 创建虚拟环境
python -m venv venv

# 3. 激活虚拟环境
# Windows
venv\Scripts\activate
# Linux/Mac
source venv/bin/activate

# 4. 安装依赖
pip install -r requirements.txt

# 5. 配置环境变量
cp .env.example .env
# 编辑.env文件，填入GLM_API_KEY

# 6. 运行开发服务器
uvicorn main:app --reload --host 0.0.0.0 --port 8000

# 7. 访问API文档
# http://localhost:8000/docs
```

### 快速测试

```bash
# 健康检查
curl http://localhost:8000/api/v1/health

# 提取题目
curl -X POST http://localhost:8000/api/v1/questions/extract \
  -H "Content-Type: application/json" \
  -d '{
    "content": "1. 题目内容...\n2. 题目内容...",
    "use_glm": true
  }'
```

---

## 📚 文档

- [FastAPI快速上手教程](docs/FastAPI快速上手教程.md) - FastAPI入门指南
- [开发文档](docs/开发文档.md) - 详细的开发文档
- [项目结构说明](docs/项目结构说明.md) - 目录结构和模块说明

---

## 🏗️ 项目结构

```
ai_service/
├── app/                    # 应用主目录
│   ├── api/               # API路由层
│   ├── models/            # 数据模型
│   ├── services/          # 业务逻辑层
│   ├── core/              # 核心功能
│   └── utils/             # 工具类
├── config/                # 配置文件
├── tests/                 # 测试
├── logs/                  # 日志
└── docs/                  # 文档
```

详细说明请查看 [项目结构说明](docs/项目结构说明.md)

---

## 🔧 配置说明

### 环境变量

主要配置项（.env文件）：

```env
# GLM配置
GLM_API_KEY=your_api_key_here
GLM_MODEL=glm-4-flash
GLM_TEMPERATURE=0.1
GLM_MAX_TOKENS=8000

# 服务配置
SERVICE_PORT=8000
DEBUG=false

# 日志配置
LOG_LEVEL=INFO
LOG_FILE=logs/app.log

# 限制配置
MAX_CONTENT_LENGTH=10485760  # 10MB
MAX_QUESTIONS_PER_REQUEST=50
```

### Prompt模板

Prompt模板存储在 `config/prompts.yaml`，支持热更新（重启服务生效）。

---

## 📡 API接口

### 基础信息

- **基础URL**: `http://localhost:8000`
- **API文档**: `http://localhost:8000/docs` (Swagger UI)
- **备用文档**: `http://localhost:8000/redoc` (ReDoc)

### 主要接口

#### 1. 提取题目

```http
POST /api/v1/questions/extract
Content-Type: application/json

{
  "content": "Markdown格式的内容",
  "use_glm": true
}
```

**响应：**
```json
{
  "success": true,
  "message": "提取成功",
  "data": {
    "questions": [
      {
        "questionNumber": 1,
        "type": 1,
        "content": "题干内容",
        "options": ["A. 选项1", "B. 选项2"],
        "answer": "A",
        "analysis": "解析内容",
        "tags": ["数据结构", "栈"]
      }
    ]
  }
}
```

#### 2. 健康检查

```http
GET /api/v1/health
```

更多接口详情请查看 [开发文档](docs/开发文档.md)

---

## 🧪 测试

```bash
# 运行所有测试
pytest

# 运行指定文件
pytest tests/test_api.py

# 显示详细输出
pytest -v

# 显示覆盖率
pytest --cov=app tests/
```

---

## 🐳 Docker部署

```bash
# 构建镜像
docker build -t kao-yan-ai-service:latest .

# 运行容器
docker run -d \
  --name ai-service \
  -p 8000:8000 \
  --env-file .env \
  kao-yan-ai-service:latest

# 查看日志
docker logs -f ai-service
```

---

## 🤝 贡献指南

1. Fork 项目
2. 创建特性分支 (`git checkout -b feature/AmazingFeature`)
3. 提交更改 (`git commit -m 'feat: 添加某个功能'`)
4. 推送到分支 (`git push origin feature/AmazingFeature`)
5. 开启 Pull Request

### 代码规范

- 遵循 PEP 8 规范
- 使用类型提示
- 编写文档字符串
- 添加单元测试

---

## 📝 开发路线图

### v1.0.0 (当前版本)
- [x] 题目智能提取功能
- [x] GLM集成
- [x] Prompt模板化管理
- [x] 基础API接口
- [x] 错误处理和日志

### v1.1.0 (计划中)
- [ ] 批量题目提取
- [ ] 提取结果导出
- [ ] 性能优化
- [ ] 完善测试覆盖

### v2.0.0 (未来版本)
- [ ] 模型训练功能
- [ ] 数据处理模块
- [ ] 推荐系统
- [ ] WebSocket支持

---

## 📄 许可证

本项目采用 MIT 许可证 - 查看 [LICENSE](LICENSE) 文件了解详情

---

## 📞 联系方式

- 项目负责人：[Your Name]
- 技术支持：[Email]
- 问题反馈：[GitHub Issues]

---

## 🙏 致谢

- [FastAPI](https://fastapi.tiangolo.com/) - 现代化的Python Web框架
- [智谱AI](https://open.bigmodel.cn/) - 提供大模型API服务
- [Pydantic](https://docs.pydantic.dev/) - 数据验证库

---

**Made with ❤️ by [Your Team]**
