# Python FastAPI 快速上手教程

> 目标读者：有Python脚本基础，但未接触过Web框架的开发者
> 预计学习时间：2-3小时
> 作者：AI Assistant
> 更新时间：2025-01

---

## 📚 目录

1. [FastAPI简介](#1-fastapi简介)
2. [核心概念](#2-核心概念)
3. [项目结构](#3-项目结构)
4. [实战案例](#4-实战案例)
5. [部署运行](#5-部署运行)
6. [调试技巧](#6-调试技巧)
7. [常见问题](#7-常见问题)

---

## 1. FastAPI简介

### 1.1 什么是FastAPI？

**FastAPI** 是一个现代、快速（高性能）的Web框架，用于基于标准Python类型提示使用Python 3.7+构建API。

**简单类比：**
```
FastAPI = Python版的Spring Boot + 自动API文档生成
```

### 1.2 为什么选择FastAPI？

| 特性 | 说明 |
|------|------|
| 🚀 **高性能** | 与NodeJS和Go相当 |
| 📝 **代码少** | 减少约40%的代码量 |
| 🔒 **类型安全** | 自动数据验证 |
| 📖 **自动文档** | Swagger UI和ReDoc |
| 🐍 **Python原生** | 无需学习新语言 |

### 1.3 与Java Spring Boot对比

| 概念 | Java (Spring Boot) | Python (FastAPI) |
|------|-------------------|------------------|
| 控制器 | `@RestController` | `@app.get()` / `@app.post()` |
| 路径参数 | `@PathVariable` | 函数参数直接定义 |
| 请求体 | `@RequestBody` | Pydantic模型 |
| 依赖注入 | `@Autowired` | 函数参数（Depends） |
| 异步 | `@Async` | `async def` / `await` |

---

## 2. 核心概念

### 2.1 第一个API

```python
from fastapi import FastAPI

# 创建应用实例
app = FastAPI()

# 定义路由（GET请求）
@app.get("/")
def read_root():
    return {"message": "Hello World"}

# 定义路由（带路径参数）
@app.get("/items/{item_id}")
def read_item(item_id: int):
    return {"item_id": item_id}
```

**运行：**
```bash
# 安装依赖
pip install fastapi uvicorn

# 启动服务
uvicorn main:app --reload

# 访问文档
# http://localhost:8000/docs
```

### 2.2 请求方法

```python
from fastapi import FastAPI

app = FastAPI()

# GET - 查询数据
@app.get("/users/{user_id}")
def get_user(user_id: int):
    return {"user_id": user_id, "name": "张三"}

# POST - 创建数据
@app.post("/users")
def create_user(user_data: dict):
    return {"status": "created", "user": user_data}

# PUT - 更新数据
@app.put("/users/{user_id}")
def update_user(user_id: int, user_data: dict):
    return {"status": "updated", "user_id": user_id}

# DELETE - 删除数据
@app.delete("/users/{user_id}")
def delete_user(user_id: int):
    return {"status": "deleted", "user_id": user_id}
```

### 2.3 请求体和Pydantic模型

**Pydantic** 类似Java的DTO/Entity，用于数据验证。

```python
from pydantic import BaseModel
from typing import List, Optional

# 定义数据模型
class User(BaseModel):
    id: int
    name: str
    email: str
    age: Optional[int] = None  # 可选字段，默认None

class QuestionItem(BaseModel):
    """题目模型"""
    questionNumber: int
    type: int  # 1=单选, 2=多选, 3=填空, 4=简答
    content: str
    options: List[str]
    answer: str
    analysis: str
    tags: List[str]

# 使用模型
@app.post("/questions")
def create_question(question: QuestionItem):
    """
    question参数会自动从JSON请求体中解析
    自动进行类型验证
    """
    return {
        "message": "创建成功",
        "question": question
    }
```

**请求示例：**
```json
POST /questions
Content-Type: application/json

{
  "questionNumber": 1,
  "type": 1,
  "content": "题干内容",
  "options": ["A. 选项1", "B. 选项2"],
  "answer": "A",
  "analysis": "解析内容",
  "tags": ["数据结构", "栈"]
}
```

### 2.4 查询参数和路径参数

```python
from typing import Optional

@app.get("/questions")
def list_questions(
    page: int = 1,           # 必填参数
    size: int = 10,          # 有默认值
    keyword: Optional[str] = None  # 可选参数
):
    """
    访问示例:
    /questions?page=2&size=20&keyword=数据结构
    """
    return {
        "page": page,
        "size": size,
        "keyword": keyword
    }

# 路径参数（自动类型转换）
@app.get("/questions/{question_id}")
def get_question(question_id: int):
    """
    访问: /questions/123
    question_id会自动转换为int类型
    如果传非数字会自动返回422错误
    """
    return {"id": question_id}
```

### 2.5 异步编程（重要！）

**什么时候用异步？**
- ✅ 调用外部API（如GLM、OpenAI）
- ✅ 数据库操作
- ✅ 文件读写
- ❌ CPU密集型计算

```python
import asyncio
from fastapi import FastAPI

app = FastAPI()

# 同步函数（简单场景）
@app.get("/sync")
def sync_endpoint():
    result = some_sync_function()
    return {"result": result}

# 异步函数（IO密集型场景）
@app.get("/async")
async def async_endpoint():
    # 调用GLM API是IO操作，应该用异步
    result = await call_glm_api()
    return {"result": result}

async def call_glm_api():
    """模拟异步IO操作"""
    await asyncio.sleep(1)  # 模拟网络请求
    return "GLM响应"

# 并行执行多个异步任务
@app.get("/parallel")
async def parallel_endpoint():
    # 同时执行多个任务
    results = await asyncio.gather(
        call_glm_api(),
        call_glm_api(),
        call_glm_api()
    )
    return {"results": results}
```

**Java对比：**
```java
// Java
@Async
public CompletableFuture<Result> asyncMethod() {
    return CompletableFuture.completedFuture(result);
}

// Python
async def async_method():
    result = await some_async_function()
    return result
```

### 2.6 依赖注入

```python
from fastapi import Depends, FastAPI

app = FastAPI()

# 依赖函数
def get_db():
    """模拟数据库连接"""
    db = "数据库连接"
    try:
        yield db
    finally:
        print("关闭数据库连接")

# 使用依赖
@app.get("/users")
def read_users(db: str = Depends(get_db)):
    return {"db": db}

# 多个依赖
@app.get("/items")
def read_items(
    db: str = Depends(get_db),
    token: str = Depends(get_token)
):
    return {"db": db, "token": token}
```

---

## 3. 项目结构

### 3.1 推荐的项目目录结构

```
ai_service/
├── main.py                    # 应用入口（类似SpringBoot的Application.java）
├── requirements.txt           # 依赖列表（类似pom.xml）
├── .env                       # 环境变量配置
├── .gitignore                 # Git忽略文件
├── Dockerfile                 # Docker配置
│
├── app/                       # 应用主目录
│   ├── __init__.py
│   ├── main.py               # FastAPI实例
│   │
│   ├── api/                  # 路由层（类似Controller）
│   │   ├── __init__.py
│   │   └── v1/
│   │       ├── __init__.py
│   │       ├── questions.py  # 题目相关接口
│   │       └── health.py     # 健康检查
│   │
│   ├── models/               # 数据模型（类似DTO/Entity）
│   │   ├── __init__.py
│   │   └── schemas.py        # Pydantic模型（请求/响应）
│   │
│   ├── services/             # 业务逻辑层（类似Service）
│   │   ├── __init__.py
│   │   ├── glm_service.py    # GLM调用服务
│   │   └── extract_service.py # 题目提取服务
│   │
│   ├── core/                 # 核心功能
│   │   ├── __init__.py
│   │   ├── config.py         # 配置加载
│   │   └── exceptions.py     # 自定义异常
│   │
│   └── utils/                # 工具类
│       ├── __init__.py
│       ├── file_utils.py     # 文件工具
│       └── logger.py         # 日志配置
│
├── tests/                    # 测试
│   ├── __init__.py
│   └── test_api.py
│
├── logs/                     # 日志文件
├── uploads/                  # 上传文件临时目录
└── config/                   # 配置文件
    └── prompts.yaml          # Prompt模板
```

### 3.2 模块示例代码

#### main.py（入口文件）

```python
from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware
from app.api.v1 import questions, health

# 创建应用实例
app = FastAPI(
    title="考研平台AI服务",
    description="提供题目提取、模型训练等AI功能",
    version="1.0.0",
    docs_url="/docs",      # Swagger UI地址
    redoc_url="/redoc"     # ReDoc地址
)

# CORS配置（允许前端跨域调用）
app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],  # 生产环境应该指定具体域名
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

# 注册路由
app.include_router(
    questions.router,
    prefix="/api/v1",
    tags=["题目管理"]
)

app.include_router(
    health.router,
    tags=["系统"]
)

@app.get("/")
def read_root():
    return {
        "message": "考研平台AI服务",
        "docs": "/docs",
        "version": "1.0.0"
    }
```

#### app/models/schemas.py（数据模型）

```python
from pydantic import BaseModel, Field
from typing import List, Optional

class QuestionItem(BaseModel):
    """单个题目"""
    questionNumber: int = Field(..., description="题号")
    type: int = Field(..., ge=1, le=4, description="题目类型：1=单选, 2=多选, 3=填空, 4=简答")
    content: str = Field(..., min_length=1, description="题干内容")
    options: List[str] = Field(default_factory=list, description="选项列表")
    answer: str = Field(..., description="答案")
    analysis: str = Field(default="", description="解析")
    tags: List[str] = Field(default_factory=list, description="标签列表")

    class Config:
        json_schema_extra = {
            "example": {
                "questionNumber": 1,
                "type": 1,
                "content": "题干内容",
                "options": ["A. 选项1", "B. 选项2"],
                "answer": "A",
                "analysis": "解析内容",
                "tags": ["数据结构", "栈"]
            }
        }

class QuestionImportDTO(BaseModel):
    """题目导入DTO"""
    questions: List[QuestionItem]

class ExtractRequest(BaseModel):
    """提取请求"""
    content: str = Field(..., description="Markdown内容")
    use_glm: bool = Field(default=True, description="是否使用GLM")

class ExtractResponse(BaseModel):
    """提取响应"""
    success: bool
    message: str
    data: Optional[QuestionImportDTO] = None
```

#### app/api/v1/questions.py（路由层）

```python
from fastapi import APIRouter, HTTPException
from app.models.schemas import ExtractRequest, ExtractResponse
from app.services.extract_service import ExtractService

router = APIRouter()
extract_service = ExtractService()

@router.post("/extract", response_model=ExtractResponse)
async def extract_questions(request: ExtractRequest):
    """
    从Markdown内容提取题目

    - **content**: Markdown格式的内容
    - **use_glm**: 是否使用GLM模型提取（默认True）
    """
    try:
        result = await extract_service.extract_questions(
            request.content,
            request.use_glm
        )

        return ExtractResponse(
            success=True,
            message="提取成功",
            data=result
        )

    except ValueError as e:
        raise HTTPException(status_code=400, detail=str(e))
    except Exception as e:
        raise HTTPException(status_code=500, detail=f"提取失败: {str(e)}")
```

#### app/services/extract_service.py（业务逻辑层）

```python
from app.models.schemas import QuestionImportDTO
from app.services.glm_service import GLMService
import logging

logger = logging.getLogger(__name__)

class ExtractService:
    def __init__(self):
        self.glm_service = GLMService()

    async def extract_questions(
        self,
        md_content: str,
        use_glm: bool = True
    ) -> QuestionImportDTO:
        """从Markdown提取题目"""
        if use_glm:
            logger.info("使用GLM提取题目")
            questions = await self.glm_service.extract(md_content)
        else:
            logger.info("使用正则表达式提取题目")
            questions = await self._extract_with_regex(md_content)

        return QuestionImportDTO(questions=questions)

    async def _extract_with_regex(self, content: str):
        """使用正则表达式提取"""
        # TODO: 实现正则提取逻辑
        pass
```

---

## 4. 实战案例

### 4.1 文件上传

```python
from fastapi import UploadFile, File

@app.post("/upload")
async def upload_file(file: UploadFile = File(...)):
    """
    上传Markdown文件

    自动解析:
    - 文件名: file.filename
    - 文件类型: file.content_type
    - 文件内容: await file.read()
    """
    # 验证文件类型
    if not file.filename.endswith('.md'):
        raise HTTPException(400, "只支持.md文件")

    # 读取内容
    content = await file.read()

    return {
        "filename": file.filename,
        "size": len(content),
        "message": "上传成功"
    }
```

### 4.2 错误处理

```python
from fastapi import HTTPException

# 内置异常
@app.get("/items/{item_id}")
def read_item(item_id: int):
    if item_id < 1:
        raise HTTPException(
            status_code=400,
            detail="ID必须大于0"
        )
    return {"item_id": item_id}

# 自定义异常
class GLMException(Exception):
    """GLM调用异常"""
    def __init__(self, message: str):
        self.message = message

# 异常处理器
@app.exception_handler(GLMException)
async def glm_exception_handler(request, exc: GLMException):
    return JSONResponse(
        status_code=500,
        content={"detail": f"GLM调用失败: {exc.message}"}
    )

# 使用自定义异常
@app.post("/extract")
async def extract(content: str):
    try:
        result = call_glm(content)
        return result
    except Exception:
        raise GLMException("GLM API调用失败")
```

### 4.3 日志记录

```python
import logging

# 配置日志
logging.basicConfig(
    level=logging.INFO,
    format='%(asctime)s - %(name)s - %(levelname)s - %(message)s',
    handlers=[
        logging.FileHandler('logs/app.log'),
        logging.StreamHandler()
    ]
)

logger = logging.getLogger(__name__)

# 使用日志
@app.get("/items/{item_id}")
def read_item(item_id: int):
    logger.info(f"查询商品: {item_id}")
    try:
        # 业务逻辑
        result = get_item(item_id)
        logger.debug(f"查询结果: {result}")
        return result
    except Exception as e:
        logger.error(f"查询失败: {e}", exc_info=True)
        raise
```

---

## 5. 部署运行

### 5.1 本地开发环境

```bash
# 1. 创建虚拟环境
python -m venv venv

# 2. 激活虚拟环境
# Windows
venv\Scripts\activate
# Linux/Mac
source venv/bin/activate

# 3. 安装依赖
pip install -r requirements.txt

# 4. 配置环境变量
cp .env.example .env
# 编辑.env文件，填入配置

# 5. 运行开发服务器
uvicorn main:app --reload --host 0.0.0.0 --port 8000

# 6. 访问API文档
# http://localhost:8000/docs
```

### 5.2 requirements.txt示例

```txt
fastapi==0.104.1
uvicorn[standard]==0.24.0
pydantic==2.5.0
pydantic-settings==2.1.0
python-multipart==0.0.6
python-dotenv==1.0.0
zhipuai==1.0.0
```

### 5.3 .env配置示例

```env
# GLM配置
GLM_API_KEY=your_api_key_here
GLM_MODEL=glm-4-flash
GLM_TEMPERATURE=0.1
GLM_MAX_TOKENS=8000

# 服务配置
SERVICE_PORT=8000
DEBUG=true

# 日志配置
LOG_LEVEL=INFO
LOG_FILE=logs/app.log
```

### 5.4 Docker部署

```dockerfile
FROM python:3.11-slim

WORKDIR /app

# 复制依赖文件
COPY requirements.txt .

# 安装依赖
RUN pip install --no-cache-dir -r requirements.txt

# 复制应用代码
COPY . .

# 暴露端口
EXPOSE 8000

# 启动命令
CMD ["uvicorn", "main:app", "--host", "0.0.0.0", "--port", "8000"]
```

---

## 6. 调试技巧

### 6.1 使用Swagger UI

访问 `http://localhost:8000/docs` 可以：
- 查看所有API接口
- 在线测试API
- 查看请求/响应模型
- 自动生成curl命令

### 6.2 print调试

```python
@app.get("/debug")
def debug_endpoint(value: int):
    print(f"接收到的值: {value}")
    print(f"值的类型: {type(value)}")
    return {"value": value}
```

### 6.3 日志调试

```python
import logging

logger = logging.getLogger(__name__)

@app.get("/debug")
def debug_endpoint(value: int):
    logger.debug(f"调试信息: {value}")
    logger.info(f"普通信息: {value}")
    logger.warning(f"警告信息: {value}")
    logger.error(f"错误信息: {value}")
    return {"value": value}
```

### 6.4 Python调试器

```python
# 在代码中设置断点
@app.get("/debug")
def debug_endpoint(value: int):
    breakpoint()  # 程序会在此处暂停
    result = value * 2
    return {"result": result}
```

### 6.5 VS Code调试

创建 `.vscode/launch.json`:

```json
{
    "version": "0.2.0",
    "configurations": [
        {
            "name": "Python: FastAPI",
            "type": "python",
            "request": "launch",
            "module": "uvicorn",
            "args": [
                "main:app",
                "--reload"
            ],
            "envFile": "${workspaceFolder}/.env"
        }
    ]
}
```

---

## 7. 常见问题

### Q1: 类型提示是什么？

```python
def func(name: str, age: int = 18) -> bool:
    """
    name: str  - 参数name必须是字符串
    age: int = 18 - 参数age必须是整数，默认18
    -> bool    - 返回值必须是布尔类型
    """
    return age >= 18

# FastAPI利用类型提示自动验证请求参数
# 如果传入错误的类型，自动返回422错误
```

### Q2: async和def的区别？

```python
# 同步函数（简单快速）
def sync_func():
    return "同步"

# 异步函数（IO密集型）
async def async_func():
    # 可以使用await调用其他异步函数
    result = await some_async_io()
    return "异步"
```

**使用建议：**
- 简单计算用 `def`
- API调用、数据库、文件操作用 `async def`

### Q3: 如何处理数据库？

```python
# 使用SQLAlchemy（异步）
from sqlalchemy.ext.asyncio import AsyncSession

@app.get("/users/{user_id}")
async def get_user(user_id: int, db: AsyncSession = Depends(get_db)):
    # 异步查询
    result = await db.execute(select(User).where(User.id == user_id))
    user = result.scalar_one_or_none()
    return user
```

### Q4: 如何验证请求？

```python
from pydantic import BaseModel, Field, validator

class UserCreate(BaseModel):
    name: str = Field(..., min_length=1, max_length=50)
    email: str
    age: int = Field(..., ge=0, le=150)

    @validator('email')
    def email_must_contain_at(cls, v):
        if '@' not in v:
            raise ValueError('邮箱必须包含@')
        return v

# 使用
@app.post("/users")
async def create_user(user: UserCreate):
    # 自动验证，失败返回422
    return user
```

---

## 📚 推荐学习资源

### 官方文档
- FastAPI官方文档（中文）：https://fastapi.tiangolo.com/zh/
- Pydantic文档：https://docs.pydantic.dev/

### 视频教程
- B站搜索"FastAPI教程"
- 推荐看1-2个快速入门视频

### 练习建议
1. 先跑通官方示例
2. 实现一个简单的CRUD接口
3. 再开始写实际业务功能

---

## 🎓 下一步

完成本教程后，你应该能够：
- ✅ 理解FastAPI的基本概念
- ✅ 创建简单的API接口
- ✅ 使用Pydantic进行数据验证
- ✅ 处理文件上传
- ✅ 实现异步调用
- ✅ 部署FastAPI应用

**建议实践项目：**
- 待办事项API（CRUD）
- 博客系统API
- 题目提取服务（本项目）

---

**祝学习愉快！** 🚀
