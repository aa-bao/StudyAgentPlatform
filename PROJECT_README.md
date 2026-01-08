# KaoYanPlatform 考研刷题平台 - 技术文档

> **文档说明**: 本文挡提供 **KaoYanPlatform** (考研刷题平台) 的完整技术架构说明，涵盖前端 (`kaoyan-frontend`) 和后端 (`KaoYanPlatform`) 的全栈上下文，供 AI 和开发者快速理解项目。

---

## 📋 目录

1. [项目概述](#1-项目概述)
2. [项目亮点](#2-项目亮点)
3. [技术栈](#3-技术栈)
4. [快速开始](#4-快速开始)
5. [项目结构](#5-项目结构)
6. [数据模型设计](#6-数据模型设计)
7. [核心功能模块](#7-核心功能模块)
8. [API 接口文档](#8-api-接口文档)
9. [前端架构](#9-前端架构)
10. [核心实现细节](#10-核心实现细节)
11. [开发规范](#11-开发规范)
12. [已知问题与优化建议](#12-已知问题与优化建议)
13. [附录](#13-附录)

---

## 1. 项目概述

### 1.1 项目定位

**KaoYanPlatform** 是一款面向考研学生的在线刷题与学习管理平台，采用现代化的前后端分离架构设计。

### 1.2 核心业务流程

```
用户端流程:
登录 → 仪表盘 → 选择科目 → 选择刷题模式
  → 逐题精练/专项突破/真题模考/套卷刷题
  → 提交答案 → 查看解析 → 加入错题本/收藏
  → 学习统计与进度追踪

管理员流程:
登录 → 管理后台 → 题目管理/科目管理/用户管理/错题监控/试卷管理
  → 数据统计与系统维护
```

### 1.3 功能模块总览

| 模块 | 前端页面 | 后端控制器 | 主要功能 |
|------|---------|-----------|---------|
| 用户端 | Login.vue, Dashboard.vue, SubjectList.vue | UserController, RecordController | 登录注册、学习统计、刷题、错题本 |
| 刷题模块 | SinglePractice.vue, TopicDrill.vue, MockExam.vue | QuestionController, RecordController | 逐题精练、专项突破、真题模考 |
| 套卷刷题 | PaperExam.vue | ExamSessionController, PaperController | 试卷管理、考试会话、AI批改 |
| | 管理后台 | AdminHome.vue, UserManage.vue, QuestionManage.vue, BookManage.vue, SubjectManage.vue, MistakeMonitor.vue, PaperManage.vue | AdminController, UserController, QuestionController, BookController, SubjectController | 题目/习题册/科目/用户管理、错题监控 |

---

## 2. 项目亮点

### 2.1 前后端分离架构

- **后端**: Spring Boot 3.5.9 + MyBatis Plus 3.5.5，提供 RESTful API
- **前端**: Vue 3.5 + Vite 7.3，采用 Composition API 和 Pinia 状态管理
- **API 文档**: Knife4j (Swagger 增强版) 自动生成在线文档

### 2.2 灵活的科目体系设计

- **4 层级结构**: 考试规格 → 具体学科 → 知识点 → 题型方法
- **多对多关系**: 通过 `scope` 字段实现科目与考试规格的灵活映射
- **虚拟分组**: 自动创建"英语"、"数学"等虚拟分组，优化前端展示
- **拖拽排序**: 支持通过拖拽调整科目层级和顺序

### 2.3 完善的多对多关联体系

- **映射表设计**: 使用 `map_question_book`、`map_question_subject`、`map_subject_book` 三张映射表
- **灵活关联**: 一道题可关联多本书、多个科目；一本书可包含多个科目
- **级联操作**: 新增/更新时自动维护关联关系，删除时自动清理映射表

### 2.4 丰富的刷题体验

- **多模式刷题**: 支持逐题精练、专项突破、真题模考三种模式
- **LaTeX 公式渲染**: 完美支持数学公式（高等数学、线性代数、概率论等）
- **画板草稿功能**: HTML5 Canvas 实现在线草稿纸，方便演算
- **收藏与标记**: 支持收藏题目、自定义标签、错题本管理

### 2.5 可视化数据展示

- **学习统计**: 仪表盘展示累计刷题数、正确率、累计时长、倒计时
- **ECharts 雷达图**: 多维度展示用户学习状态（正确率、覆盖度、活跃度）
- **错题热力图**: 按科目展示错题分布，快速定位薄弱环节
- **高频错题 TOP 20**: 帮助管理员发现需要改进的题目

### 2.6 完善的权限管理

- **角色分离**: 区分 `admin` 管理员和 `student` 普通用户
- **路由守卫**: 前端路由拦截未授权访问
- **Spring Security**: 后端安全控制（待扩展 JWT 认证）

### 2.7 AI 智能批改系统

- **GLM-4.7 集成**: 智能批改主观题，按步骤给分
- **结构化反馈**: 返回得分、优点、不足、详细反馈
- **客观题自动判分**: 单选、多选题自动批改
- **考试统计**: 自动生成考试总结，包括正确率、时长、AI 反馈
- **指数退避重试**: 处理 API 超时，最大重试 3 次

---

## 3. 技术栈

### 3.1 后端技术栈

| 技术 | 版本 | 用途 |
|------|------|------|
| Spring Boot | 3.5.9 | 核心框架 |
| MyBatis Plus | 3.5.5 | ORM 框架，支持自动填充、分页插件 |
| MySQL | 8.0 | 关系型数据库 |
| Knife4j | 4.5.0 | API 文档生成 (Swagger 增强版) |
| Spring Security | - | 安全框架 |
| Lombok | - | 简化 POJO 代码 |
| Hutool | 5.8.25 | Java 工具库 |
| Jackson | - | JSON 处理（MyBatis Plus TypeHandler） |

### 3.2 前端技术栈

| 技术 | 版本 | 用途 |
|------|------|------|
| Vue | 3.5 | 前端框架（Composition API） |
| Vite | 7.3 | 构建工具（热更新、Rollup） |
| Element Plus | 2.13 | UI 组件库 |
| Pinia | 3.0 | 状态管理 |
| Vue Router | 4.6 | 路由管理 |
| Axios | 1.13 | HTTP 客户端 |
| KaTeX | - | 数学公式渲染 |
| ECharts | 6.0 | 数据可视化 |
| @element-plus/icons-vue | - | 图标组件库 |

### 3.3 数据库技术

| 技术 | 版本 | 用途 |
|------|------|------|
| MySQL | 8.0 | 主数据库 |
| MyBatis Plus TypeHandler | - | JSON 字段自动转换（JacksonTypeHandler） |

### 3.4 开发工具

| 工具 | 用途 |
|------|------|
| IntelliJ IDEA | 后端开发 IDE |
| VS Code | 前端开发 IDE |
| Maven | 项目构建、依赖管理 |
| npm/yarn | 前端包管理 |
| Git | 版本控制 |

---

## 4. 快速开始

### 4.1 环境要求

| 环境 | 要求版本 |
|------|---------|
| JDK | 17+ |
| Maven | 3.6+ |
| MySQL | 8.0+ |
| Node.js | 16+ |
| 磁盘空间 | ≥ 1GB |

### 4.2 数据库初始化

```sql
-- 1. 创建数据库
CREATE DATABASE kaoyan_platform CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;

-- 2. 导入表结构和初始数据
USE kaoyan_platform;
SOURCE sql/schema.sql;
```

**数据库表结构概览**:
- `tb_user` - 用户表
- `tb_question` - 题目表
- `tb_subject` - 科目表
- `tb_book` - 习题册表
- `tb_exam_record` - 答题记录表
- `tb_mistake_record` - 错题记录表
- `tb_collection` - 收藏表
- `tb_user_progress` - 用户进度表
- `map_question_book` - 题目-习题册映射表
- `map_question_subject` - 题目-科目映射表
- `map_subject_book` - 习题册-科目映射表

### 4.3 后端启动

```bash
# 1. 进入后端目录
cd KaoYanPlatform

# 2. 修改配置文件（可选）
# 编辑 src/main/resources/application.yml，配置数据库连接信息

# 3. 启动项目
mvn spring-boot:run

# 或者直接运行主类
# KaoYanPlatform/src/main/java/org/example/kaoyanplatform/KaoyanplatformApplication.java
```

**后端启动后访问**:
- API 地址: http://localhost:8081
- Swagger 文档: http://localhost:8081/doc.html

### 4.4 前端启动

```bash
# 1. 进入前端目录
cd kaoyan-frontend

# 2. 安装依赖
npm install

# 3. 启动开发服务器
npm run dev

# 4. 构建生产版本
npm run build
```

**前端启动后访问**: http://localhost:5173

### 4.5 默认测试账号

| 角色 | 用户名 | 密码 |
|------|--------|------|
| 管理员 | admin | admin123 |
| 普通用户 | student | 123456 |

---

## 5. 项目结构

### 5.1 后端结构

```
KaoYanPlatform/src/main/java/org/example/kaoyanplatform/
│
├── common/                    # 通用模块
│   └── Result.java            # 统一 API 响应封装
│
├── config/                    # 配置类
│   ├── CorsConfig.java        # CORS 跨域配置
│   ├── SecurityConfig.java    # Spring Security 配置
│   ├── WebConfig.java         # MVC 配置
│   ├── Knife4jConfig.java     # Swagger API 文档配置
│   ├── MybatisPlusConfig.java # MyBatis Plus 配置
│   └── RestTemplateConfig.java # RestTemplate 配置（用于 AI API 调用）
│
├── controller/                # API 接口层
│   ├── AdminController.java   # 管理员后台（错题监控、标签统计）
│   ├── BookController.java    # 习题册管理（多科目关联）
│   ├── CollectionController.java # 收藏管理
│   ├── FileController.java    # 文件上传/下载
│   ├── QuestionController.java # 题目管理
│   ├── RecordController.java  # 考试记录
│   ├── SubjectController.java # 科目管理（树形、拖拽排序）
│   ├── UserController.java    # 用户认证与信息管理
│   ├── PaperController.java   # 试卷管理
│   ├── ExamSessionController.java # 套卷刷题管理
│   └── ExamAnswerDetailController.java # 答题明细管理
│
├── entity/                    # 数据库实体
│   ├── dto/                   # 数据传输对象
│   │   ├── SubjectDTO.java    # 科目树形结构 DTO
│   │   ├── BookDTO.java       # 习题册 DTO（含科目关联）
│   │   ├── UserStudyStatsDTO.java # 用户学习统计 DTO
│   │   ├── MistakeHeatmapDTO.java # 错题热力统计 DTO
│   │   ├── QuestionDTO.java   # 题目 DTO（含关联关系）
│   │   ├── LLMQuestionOutputDTO.java # LLM 题目识别输出 DTO
│   │   ├── TagStatsDTO.java   # 标签统计 DTO
│   │   └── ExamStartDTO.java  # 考试开始 DTO
│   │
│   ├── Book.java              # tb_book 表实体
│   ├── ExamRecord.java        # tb_exam_record 表实体
│   ├── MapQuestionBook.java   # map_question_book 映射表实体
│   ├── MapQuestionSubject.java # map_question_subject 映射表实体
│   ├── MapSubjectBook.java    # map_subject_book 映射表实体
│   ├── Question.java          # tb_question 表实体
│   ├── Subject.java           # tb_subject 表实体
│   ├── User.java              # tb_user 表实体
│   ├── MistakeRecord.java     # tb_mistake_record 表实体
│   ├── Collection.java        # tb_collection 表实体
│   ├── UserProgress.java      # tb_user_progress 表实体
│   ├── Paper.java             # tb_paper 试卷表实体
│   ├── MapPaperQuestion.java  # map_paper_question 试卷-题目映射表实体
│   ├── ExamSession.java       # tb_exam_session 考试会话表实体
│   └── ExamAnswerDetail.java  # tb_exam_answer_detail 答题明细表实体
│
├── handler/                   # 处理器
│   ├── GlobalExceptionHandler.java # 全局异常处理
│   └── MyMetaObjectHandler.java    # MP 自动填充处理
│
├── mapper/                    # 数据访问层 (MyBatis Plus)
│   ├── BookMapper.java
│   ├── CollectionMapper.java
│   ├── ExamRecordMapper.java
│   ├── MapQuestionBookMapper.java
│   ├── MapQuestionSubjectMapper.java
│   ├── MapSubjectBookMapper.java
│   ├── QuestionMapper.java
│   ├── SubjectMapper.java
│   ├── UserMapper.java
│   ├── UserProgressMapper.java
│   ├── MistakeRecordMapper.java
│   ├── PaperMapper.java
│   ├── MapPaperQuestionMapper.java
│   ├── ExamSessionMapper.java
│   └── ExamAnswerDetailMapper.java
│
└── service/                   # 业务逻辑层
    ├── BookService.java
    ├── CollectionService.java
    ├── MapQuestionBookService.java
    ├── MapQuestionSubjectService.java
    ├── MapSubjectBookService.java
    ├── QuestionService.java
    ├── RecordService.java
    ├── SubjectService.java
    ├── UserService.java
    ├── UserProgressService.java
    ├── MistakeRecordService.java
    ├── PaperService.java
    ├── MapPaperQuestionService.java
    ├── ExamSessionService.java
    ├── ExamAnswerDetailService.java
    └── GLMService.java        # GLM-4.7 AI 批改服务
    │
    └── impl/                  # 服务实现类
        ├── BookServiceImpl.java
        ├── CollectionServiceImpl.java
        ├── MapQuestionBookServiceImpl.java
        ├── MapQuestionSubjectServiceImpl.java
        ├── MapSubjectBookServiceImpl.java
        ├── QuestionServiceImpl.java
        ├── RecordServiceImpl.java
        ├── SubjectServiceImpl.java
        ├── UserServiceImpl.java
        ├── UserProgressServiceImpl.java
        ├── MistakeRecordServiceImpl.java
        ├── PaperServiceImpl.java
        ├── MapPaperQuestionServiceImpl.java
        ├── ExamSessionServiceImpl.java
        ├── ExamAnswerDetailServiceImpl.java
        └── GLMServiceImpl.java
```

### 5.2 前端结构

```
kaoyan-frontend/src/
│
├── api/                       # API 调用封装
│   └── user.js                # 用户相关 API
│
├── assets/                    # 静态资源
│   └── icons/                 # SVG 图标库
│
├── components/                # 公共组件
│   └── exam/                  # 考试相关组件
│       ├── AnswerSheet.vue    # 答题卡组件
│       ├── ExamHeader.vue     # 考试头部组件
│       ├── QuestionItem.vue   # 题目展示组件
│       ├── ScoreReport.vue    # 成绩报告组件
│       └── ScratchPad.vue     # 草稿纸组件（Canvas）
│
├── router/                    # 路由配置
│   └── index.js               # 路由定义与导航守卫
│
├── stores/                    # Pinia 状态管理
│   ├── user.js                # 用户会话状态
│   ├── counter.js             # 计数器状态
│   └── exam.js                # 考试会话状态
│
├── utils/                     # 工具函数
│   └── request.js             # Axios 实例（带拦截器）
│
├── views/                     # 页面组件
│   │
│   ├── admin/                 # 管理员视图
│   │   ├── AdminHome.vue      # 数据看板
│   │   ├── UserManage.vue     # 用户管理（学习统计、雷达图）
│   │   ├── MistakeMonitor.vue # 错题监控（热力图、高频错题）
│   │   ├── SubjectManage.vue  # 科目体系管理（树形、拖拽排序）
│   │   ├── BookManage.vue     # 习题册管理（多科目关联）
│   │   └── QuestionManage.vue # 题库管理（KaTeX、多表关联）
│   │
│   ├── quiz/                  # 刷题模式
│   │   ├── SinglePractice.vue # 逐题精练
│   │   ├── TopicDrill.vue     # 专项突破
│   │   ├── MockExam.vue       # 真题模考
│   │   └── MockExamView.vue   # 真题模考视图
│   │
│   ├── layout/                # 布局容器
│   │   ├── AdminLayout.vue    # 管理员侧边栏 + 顶栏
│   │   ├── UserLayout.vue     # 用户侧边栏（浅色主题）+ 顶栏
│   │   └── Layout.vue         # 通用布局
│   │
│   ├── Dashboard.vue          # 用户首页
│   ├── Login.vue              # 登录页
│   ├── Exercise.vue           # 通用刷题页面
│   ├── CorrectionNotebook.vue # 错题本（瀑布流、分类导航）
│   ├── SubjectList.vue        # 科目选择列表
│   └── UserProfile.vue        # 个人资料
│
├── App.vue                    # 根组件
└── main.js                    # 入口文件（插件配置）
```

---

## 6. 数据模型设计

### 6.1 核心业务表

#### 用户表 (`tb_user`)

| 字段 | 类型 | 约束 | 说明 |
|------|------|------|------|
| id | bigint | PK, AUTO | 主键 |
| username | varchar(50) | UNIQUE | 用户名/账号 |
| password | varchar(100) | NOT NULL | 密码（加密） |
| phone | varchar(50) | | 手机号 |
| email | varchar(50) | | 邮箱 |
| nickname | varchar(50) | | 昵称 |
| role | varchar(20) | | `admin` 或 `student` |
| avatar | varchar(255) | | 头像 URL |
| target_school | varchar(100) | | 目标院校 |
| target_total_score | smallint | | 目标总分 |
| exam_year | varchar(50) | | 考研年份（如：2025） |
| exam_subjects | varchar(255) | | 公共课（如：政治、英语一） |
| create_time | datetime | | 创建时间 |
| update_time | datetime | | 更新时间 |

#### 题目表 (`tb_question`)

| 字段 | 类型 | 约束 | 说明 |
|------|------|------|------|
| id | bigint | PK, AUTO | 主键 |
| type | tinyint | NOT NULL | 题目类型：1-单选, 2-多选, 3-填空, 4-简答 |
| content | text | NOT NULL | 题干内容（支持 LaTeX） |
| options | json | | 选项：`["A.xx", "B.xx", ...]` |
| answer | text | NOT NULL | 正确答案 |
| analysis | text | | 解析（支持 LaTeX） |
| difficulty | tinyint | | 难度：1-5 |
| tags | json | | 题目标签：`["tag1", "tag2"]` |
| source | varchar(100) | | 题目来源（如：2020年真题） |
| create_time | datetime | | 创建时间 |

**特殊说明**:
- 题目与科目、书本的关系通过映射表管理，不存储外键字段
- `options` 和 `tags` 使用 `JacksonTypeHandler` 自动转换 JSON ↔ Java List

#### 科目分类表 (`tb_subject`)

| 字段 | 类型 | 约束 | 说明 |
|------|------|------|------|
| id | int | PK, AUTO | 主键 |
| name | varchar(50) | NOT NULL | 科目名称 |
| parent_id | int | | 父级 ID（0 表示根节点） |
| icon | varchar(100) | | 图标 |
| sort | int | | 排序号（值越小越靠前） |
| level | varchar(100) | | 层级：1-考试规格, 2-具体学科, 3-知识点, 4-题型 |
| question_count | int | | 题目数量 |
| scope | varchar(50) | | 适用范围：`"4,5,6"`（数一、数二、数三） |

**4 层级结构说明**:

系统采用简化且清晰的 4 层级科目体系，支持基于 `scope` 字段的多对多关系映射：

- **Level 1 (考试规格)**: 考试规格/根节点
  - 示例：政治(id=1)、英语一(id=2)、英语二(id=3)、数学一(id=4)、数学二(id=5)、数学三(id=6)、408(id=7)
  - `parent_id = 0`

- **Level 2 (具体学科)**: 具体学科（可跨考试规格）
  - 示例：高等数学、线性代数、概率论、完形填空、阅读理解、数据结构
  - 两种归属方式：
    - **传统父子关系**: `parent_id = 考试规格ID`（如：马原 parent_id=1（政治））
    - **Scope 多对多关系**: `parent_id = 0`, `scope = "2,3"`（如：完形填空属于英语一、二）

- **Level 3 (知识点)**: 知识点/章节
  - 示例：函数与极限、行列式、矩阵
  - `parent_id = 具体学科ID` 或使用 scope 字段

- **Level 4 (题型)**: 题型/解题方法
  - 示例：泰勒公式、洛必达法则、行列式计算
  - `parent_id = 知识点ID`

**scope 字段使用示例**:
- `scope = "4,5,6"` → 属于数学一、数学二、数学三
- `scope = "4,6"` → 属于数学一、数学三（**不包括数学二**）
- `scope = "2,3"` → 属于英语一、英语二


#### 习题册表 (`tb_book`)

| 字段 | 类型 | 约束 | 说明 |
|------|------|------|------|
| id | bigint | PK, AUTO | 主键 |
| name | varchar(50) | NOT NULL | 习题册名称 |
| description | varchar(255) | | 习题册描述 |
| create_time | datetime | | 创建时间 |

**特殊说明**:
- 习题册与科目的关系通过 `map_subject_book` 映射表管理
- 支持一本书包含多个科目（如《张宇1000题》包含高数、线代、概率）

#### 答题记录表 (`tb_exam_record`)

| 字段 | 类型 | 约束 | 说明 |
|------|------|------|------|
| id | bigint | PK, AUTO | 主键 |
| user_id | bigint | FK | 用户 ID |
| question_id | bigint | FK | 题目 ID |
| user_answer | text | | 用户提交的答案 |
| is_correct | tinyint(1) | | 是否正确：0-错, 1-对 |
| score | int | | 得分 |
| duration | int | | 答题耗时（秒） |
| create_time | datetime | | 创建时间 |

#### 错题本表 (`tb_mistake_record`)

| 字段 | 类型 | 约束 | 说明 |
|------|------|------|------|
| id | int | PK, AUTO | 主键 |
| user_id | int | FK | 用户 ID |
| question_id | int | FK | 题目 ID |
| create_time | datetime | | 首次加入错题本时间 |
| update_time | datetime | | 最近一次答错时间 |
| error_count | int | | 累计答错次数 |

#### 收藏夹表 (`tb_collection`)

| 字段 | 类型 | 约束 | 说明 |
|------|------|------|------|
| id | bigint | PK, AUTO | 主键 |
| user_id | bigint | FK | 用户 ID |
| question_id | bigint | FK | 题目 ID |
| tag | json | | 自定义标签：`["重点", "易错"]` |
| create_time | datetime | | 创建时间 |

#### 用户学习进度表 (`tb_user_progress`)

| 字段 | 类型 | 约束 | 说明 |
|------|------|------|------|
| id | bigint | PK, AUTO | 主键 |
| user_id | bigint | FK | 用户 ID |
| subject_id | int | FK | 科目或考点 ID |
| finished_count | int | | 该考点下已做题目数 |
| correct_count | int | | 该考点下做对题目数 |
| update_time | datetime | | 更新时间 |

### 6.2 套卷刷题相关表

#### 试卷主表 (`tb_paper`)

| 字段 | 类型 | 约束 | 说明 |
|------|------|------|------|
| id | varchar(36) | PK, UUID | 主键 |
| title | varchar(255) | NOT NULL | 试卷标题 |
| exam_spec_id | varchar(36) | | 考试规格 ID |
| total_score | int | DEFAULT 150 | 总分 |
| time_limit | int | DEFAULT 180 | 时间限制（分钟） |
| paper_type | tinyint | | 试卷类型：0-真题, 1-模拟 |
| structure_json | json | | 试卷结构：`[{"name":"一、选择题","start":1,"end":10}]` |
| create_time | datetime | | 创建时间 |

#### 试卷-题目关联表 (`map_paper_question`)

| 字段 | 类型 | 约束 | 说明 |
|------|------|------|------|
| id | varchar(36) | PK, UUID | 主键 |
| paper_id | varchar(36) | FK | 试卷 ID |
| question_id | varchar(36) | FK | 题目 ID |
| sort_order | int | | 题目顺序 |
| score_value | decimal(5,2) | | 题目分值 |
| parent_section_name | varchar(50) | | 所属大题名称 |

**索引**: `uk_paper_question` (paper_id, question_id) 唯一索引

#### 考试会话表 (`tb_exam_session`)

| 字段 | 类型 | 约束 | 说明 |
|------|------|------|------|
| id | varchar(36) | PK, UUID | 主键 |
| user_id | varchar(36) | FK, NOT NULL | 用户 ID |
| paper_id | varchar(36) | FK, NOT NULL | 试卷 ID |
| status | tinyint | DEFAULT 0 | 状态：0-进行中, 1-已完成 |
| start_time | datetime | | 开始时间 |
| submit_time | datetime | | 提交时间 |
| total_score | decimal(5,2) | | 总分 |
| switch_count | int | DEFAULT 0 | 切换题目次数 |
| ai_summary | text | | AI 总结 |
| snapshot_answers | json | | 答题快照 JSON |
| create_time | datetime | | 创建时间 |

#### 答题明细表 (`tb_exam_answer_detail`)

| 字段 | 类型 | 约束 | 说明 |
|------|------|------|------|
| id | varchar(36) | PK, UUID | 主键 |
| session_id | varchar(36) | FK | 考试会话 ID |
| question_id | varchar(36) | FK | 题目 ID |
| user_answer | text | | 用户答案 |
| is_correct | tinyint | | 是否正确：0-错, 1-对, 2-待定（主观题） |
| score_earned | decimal(5,2) | | 得分 |
| duration_seconds | int | | 答题时长（秒） |
| ai_feedback | text | | AI 反馈 |
| knowledge_point_id | varchar(36) | | 知识点 ID |
| create_time | datetime | | 创建时间 |

### 6.3 映射表（多对多关系）

#### 题目-书本关联表 (`map_question_book`)

| 字段 | 类型 | 约束 | 说明 |
|------|------|------|------|
| id | bigint | PK, AUTO | 主键 |
| question_id | bigint | FK | 题目 ID |
| book_id | int | FK | 习题册 ID |

**索引**: `uk_question_book` (question_id, book_id) 唯一索引, `idx_question_id`, `idx_book_id`

#### 题目-科目关联表 (`map_question_subject`)

| 字段 | 类型 | 约束 | 说明 |
|------|------|------|------|
| id | bigint | PK, AUTO | 主键 |
| question_id | bigint | FK | 题目 ID |
| subject_id | int | FK | 科目 ID 或知识点 ID |

**索引**: `uk_question_subject` (question_id, subject_id) 唯一索引, `idx_question_id`, `idx_subject_id`

#### 书本-科目关联表 (`map_subject_book`)

| 字段 | 类型 | 约束 | 说明 |
|------|------|------|------|
| id | int | PK, AUTO | 主键 |
| book_id | int | FK | 习题册 ID（如：4-数一, 5-数二, 6-数三） |
| subject_id | int | FK | 科目 ID（如：401-高数, 402-线代, 403-概率） |

**索引**: `uk_book_subject` (book_id, subject_id) 唯一索引, `idx_book_id`, `idx_subject_id`

### 6.4 映射表关系链

```
题目 → map_question_book → 习题册
题目 → map_question_subject → 科目/知识点
习题册 → map_subject_book → 科目/知识点
```

### 6.5 查询示例

```sql
-- 查询某本书的所有题目
SELECT q.* FROM tb_question q
INNER JOIN map_question_book mqb ON q.id = mqb.question_id
WHERE mqb.book_id = 1;

-- 查询某个科目的所有题目
SELECT q.* FROM tb_question q
INNER JOIN map_question_subject mqs ON q.id = mqs.question_id
WHERE mqs.subject_id = 401;

-- 查询题目所属的书本和科目
SELECT q.id, q.content, b.name as book_name, s.name as subject_name
FROM tb_question q
LEFT JOIN map_question_book mqb ON q.id = mqb.question_id
LEFT JOIN tb_book b ON mqb.book_id = b.id
LEFT JOIN map_question_subject mqs ON q.id = mqs.question_id
LEFT JOIN tb_subject s ON mqs.subject_id = s.id
WHERE q.id = 1000;
```

---

## 7. 核心功能模块

### 7.1 题目管理模块 (Question Management)

#### 功能特性

- ✅ **多对多关联**: 一道题可关联多本书、多个科目
- ✅ **完整 CRUD**: 新增、编辑、删除、分页查询
- ✅ **KaTeX 支持**: 题目内容和解析支持数学公式
- ✅ **动态选项**: 支持 2-8 个选项的灵活配置
- ✅ **多科目关联**: 级联选择器支持多选科目
- ✅ **批量操作**: 导出 CSV、批量删除

#### 核心 API

| 接口 | 方法 | 说明 |
|------|------|------|
| `/question/add` | POST | 新增题目（含关联） |
| `/question/update` | POST | 更新题目（含关联） |
| `/question/delete/{id}` | DELETE | 删除题目（级联删除映射表） |
| `/question/page` | GET | 分页查询（支持科目/书本筛选） |

### 5.2 习题册管理模块 (Book Management)

#### 功能特性

- ✅ **多科目关联**: 一本书可包含多个科目（如《张宇1000题》包含高数、线代、概率）
- ✅ **sort 排序**: 支持按 sort 字段排序（值越小越靠前）
- ✅ **完整 CRUD**: 新增、编辑、删除、分页查询
- ✅ **级联多选**: 使用 el-cascader 支持多科目关联

#### 核心 API

| 接口 | 方法 | 说明 |
|------|------|------|
| `/book/add` | POST | 新增习题册（含科目关联） |
| `/book/update` | POST | 更新习题册（含科目关联） |
| `/book/delete/{id}` | DELETE | 删除习题册（级联删除映射表） |
| `/book/page` | GET | 分页查询（支持科目筛选） |
| `/book/list` | GET | 获取所有习题册（无分页） |

### 5.3 科目体系管理模块 (Subject Management)

#### 功能特性

- ✅ **4 层级结构**: 考试规格 → 具体学科 → 知识点 → 题型
- ✅ **虚拟分组**: 自动创建政治、英语、数学、408 虚拟分组节点
- ✅ **拖拽排序**: 支持拖拽调整科目顺序和层级
- ✅ **scope 配置**: 支持配置科目适用范围（数一/数二/数三）
- ✅ **多对多关系**: 通过 scope 字段实现科目与考试规格的多对多映射
- ✅ **递归构建**: 自动将扁平数据转换为树形结构
- ✅ **安全删除**: 删除前检查是否有子节点或题目关联

#### 核心 API

| 接口 | 方法 | 说明 |
|------|------|------|
| `/subject/add` | POST | 新增科目 |
| `/subject/update` | POST | 更新科目 |
| `/subject/delete/{id}` | DELETE | 删除科目（带检查） |
| `/subject/manage-tree` | GET | 获取管理用科目树（含虚拟分组） |
| `/subject/tree` | GET | 获取用户端科目树 |
| `/subject/by-exam-spec/{examSpecId}` | GET | 根据考试规格获取子树 |
| `/subject/batch-update-sort` | POST | 批量更新排序（拖拽后） |

#### 虚拟分组机制

为了优化前端展示，`getManageTree()` 方法在后端自动创建虚拟分组节点：

- **英语 (id=-2, level="0")**: 包含英语一(id=2)、英语二(id=3)
- **数学 (id=-3, level="0")**: 包含数学一(id=4)、数学二(id=5)、数学三(id=6)
- **政治 (id=1, level="1")**: 直接作为根节点
- **408 (id=7, level="1")**: 直接作为根节点

**特殊处理规则**:
1. Level 1 的考试规格（英语一/二、数学一/二/三）自动挂载到对应的虚拟分组下
2. `parent_id=0` 且有 `scope` 字段的节点（如高数、完形填空）通过 scope 字段动态挂载到多个考试规格下
3. 虚拟分组节点（id < 0 或 level="0"）不可编辑、删除、拖拽

#### Scope 多对多关系

通过 `scope` 字段实现科目与多个考试规格的关联：

- **高等数学**: `scope="4,5,6"` → 属于数学一、二、三
- **线性代数**: `scope="4,6"` → 属于数学一、三（**数学二不考**）
- **概率论**: `scope="4,5,6"` → 属于数学一、二、三
- **完形填空**: `scope="2,3"` → 属于英语一、二

前端通过 `filterSubjectTreeForCascader()` 函数实现动态过滤，例如：
- 选择"数学二"时，只显示 scope 包含 "5" 的科目（高数、概率论，**无线代**）

### 7.6 套卷刷题模块 (Paper Exam)

#### 功能特性

- ✅ **试卷管理**: 创建、编辑、删除试卷，支持设置试卷结构和大题
- ✅ **题目关联**: 灵活的试卷-题目关联，支持设置分值和顺序
- ✅ **考试会话**: 完整的考试生命周期管理（开始、答题、提交）
- ✅ **自动存盘**: 实时保存答题进度，防止数据丢失
- ✅ **客观题自动判分**: 单选、多选题自动批改
- ✅ **AI 智能批改**: 主观题使用 GLM-4.7 进行智能批改
- ✅ **结构化反馈**: 返回得分、优点、不足、详细反馈
- ✅ **考试总结**: 自动生成详细的 AI 总结报告

#### 核心 API

| 接口 | 方法 | 说明 |
|------|------|------|
| `/paper/add` | POST | 新增试卷 |
| `/paper/update` | POST | 更新试卷 |
| `/paper/delete/{id}` | DELETE | 删除试卷 |
| `/paper/page` | GET | 分页查询试卷 |
| `/paper/{id}` | GET | 获取试卷详情 |
| `/paper/{paperId}/questions` | GET | 获取试卷题目列表 |
| `/paper/{paperId}/add-question` | POST | 添加题目到试卷 |
| `/exam-session/start` | POST | 开始考试 |
| `/exam-session/snapshot` | POST | 保存答题快照 |
| `/exam-session/switch` | POST | 记录题目切换 |
| `/exam-session/submit` | POST | 提交考试（触发AI批改） |
| `/exam-session/{sessionId}` | GET | 获取考试会话详情 |
| `/exam-session/{sessionId}/details` | GET | 获取答题明细 |
| `/exam-answer-detail/session/{sessionId}` | GET | 查询答题明细 |
| `/exam-answer-detail/session/{sessionId}/correct-rate` | GET | 获取正确率统计 |

#### 考试流程

```
1. 管理员创建试卷
   - 设置试卷基本信息（标题、总分、时限、类型）
   - 配置试卷结构（大题名称、题目范围、分值）
   - 关联题目（支持批量添加）

2. 用户开始考试
   - 初始化考试会话（记录开始时间）
   - 加载试卷内容和题目列表
   - 返回考试配置信息

3. 用户答题
   - 浏览题目（支持跳转）
   - 填写答案
   - 自动存盘（定时保存答题快照）
   - 记录切换次数

4. 提交考试
   - 客观题自动判分（单选、多选）
   - 主观题 AI 批改（调用 GLM-4.7）
   - 生成答题明细记录
   - 计算总分
   - 生成 AI 总结报告

5. 查看结果
   - 查看总成绩
   - 查看每题得分和反馈
   - 查看 AI 总结
   - 查看答题历史
```

#### AI 批改流程

```
1. 提取主观题列表（type = 3 或 4）
2. 构建批改 Prompt
   - 题目内容
   - 学生解答
   - 标准答案
   - 满分分值
   - 批改要求（按步骤给分）
3. 调用 GLM-4.7 API
   - 指数退避重试（最大 3 次）
   - 超时处理
4. 解析返回结果（JSON 格式）
   - score: 得分
   - feedback: 详细反馈
   - strengths: 优点列表
   - weaknesses: 不足列表
5. 保存批改结果
```

### 7.4 用户管理与学习监控模块 (User Management)

#### 功能特性

- ✅ **用户列表**: 分页展示所有用户，支持角色和关键词筛选
- ✅ **学习统计**: 查询用户在各个科目的学习进度
- ✅ **ECharts 雷达图**: 可视化展示正确率、覆盖度、活跃度
- ✅ **详细信息**: 目标院校、考研年份、完成题数、正确题数等

#### 核心 API

| 接口 | 方法 | 说明 |
|------|------|------|
| `/user/page` | GET | 获取用户列表（分页，支持筛选） |
| `/user/study-stats/{userId}` | GET | 获取用户学习统计数据 |

#### 统计指标

- **正确率**: (正确题数 / 完成题数) × 100%
- **覆盖度**: (完成题数 / 总题数) × 100%
- **活跃度**: 最近 7 天完成的题目数

### 5.5 错题监控模块 (Mistake Monitor)

#### 功能特性

- ✅ **热力统计**: 按科目统计错题高发区
- ✅ **高频错题**: TOP 20 高频错题列表
- ✅ **快速跳转**: 点击题目直接跳转到编辑页面
- ✅ **可视化展示**: 热力图直观展示错题分布

#### 核心 API

| 接口 | 方法 | 说明 |
|------|------|------|
| `/admin/mistake-heatmap` | GET | 获取错题热力统计 |
| `/admin/hot-mistakes` | GET | 获取高频错题 TOP N（参数：limit，默认 20） |

#### 统计维度

- **总错误次数**: 该科目下所有题目的 errorCount 总和
- **错题题目数**: 有错题记录的题目数量
- **涉及用户数**: 做错该科目题目的不同用户数

---

## 8. API 接口文档

### 8.1 Swagger 文档访问

项目使用 **Knife4j** (Swagger 的增强版) 自动生成 API 文档。

**访问地址**: http://localhost:8081/doc.html

**功能特性**:
- 在线调试接口（发送请求、查看响应）
- 查看接口参数说明和示例值
- 导出 API 文档（Markdown/HTML/Word）
- 接口分组管理（按 Controller 分组）

**接口分组**:
1. **管理员管理** - 统计数据、错题监控
2. **习题册管理** - 习题册 CRUD、科目关联
3. **收藏管理** - 收藏题目、标签管理
4. **文件管理** - 文件上传
5. **题目管理** - 题目 CRUD、错题本
6. **答题记录** - 提交答案、统计查询
7. **科目管理** - 科目树、层级管理
8. **用户管理** - 登录注册、资料管理
9. **试卷管理** - 试卷 CRUD、题目关联
10. **套卷刷题管理** - 考试会话、AI批改
11. **答题明细管理** - 答题详情、统计查询

### 7.2 统一响应格式

#### Result.java

```java
public class Result<T> {
    private Integer code; // 200 成功，500 失败
    private String msg;   // 提示信息
    private T data;       // 具体数据

    public static <T> Result<T> success(T data) {
        Result<T> result = new Result<>();
        result.setCode(200);
        result.setMsg("成功");
        result.setData(data);
        return result;
    }

    public static <T> Result<T> success() {
        Result<T> result = new Result<>();
        result.setCode(200);
        result.setMsg("成功");
        result.setData(null);
        return result;
    }

    public static <T> Result<T> error(String msg) {
        Result<T> result = new Result<>();
        result.setCode(500);
        result.setMsg(msg);
        return result;
    }
}
```

#### 响应示例

```json
// 成功响应
{
  "code": 200,
  "msg": "成功",
  "data": {
    "id": 1,
    "name": "高数"
  }
}

// 失败响应
{
  "code": 500,
  "msg": "题目不存在",
  "data": null
}
```

### 7.3 基础配置

- **Base URL**: `http://localhost:8081`
- **超时时间**: 10000ms
- **请求头**: `Content-Type: application/json`

### 7.4 前端拦截器

```javascript
// request.js
import axios from 'axios'
import { ElMessage } from 'element-plus'

const request = axios.create({
  baseURL: 'http://localhost:8081',
  timeout: 10000
})

// 响应拦截器
request.interceptors.response.use(
  response => {
    const res = response.data

    if (res.code === 200) {
      return res.data  // 自动解包 data
    } else {
      ElMessage.error(res.msg || '请求失败')
      return Promise.reject(new Error(res.msg || '请求失败'))
    }
  },
  error => {
    ElMessage.error(error.message || '网络错误')
    return Promise.reject(error)
  }
)

export default request
```

---

## 9. 前端架构

### 9.1 路由与权限

#### 路由结构

```javascript
const routes = [
  {
    path: '/login',
    component: () => import('@/views/Login.vue')
  },
  {
    path: '/user',
    component: () => import('@/views/layout/UserLayout.vue'),
    children: [
      { path: 'dashboard', component: () => import('@/views/Dashboard.vue') },
      { path: 'correction-notebook', component: () => import('@/views/CorrectionNotebook.vue') },
      { path: 'profile', component: () => import('@/views/UserProfile.vue') },
      { path: 'subject', component: () => import('@/views/SubjectList.vue') },
      { path: 'single-practice/:subjectId', component: () => import('@/views/quiz/SinglePractice.vue') },
      { path: 'topic-drill', component: () => import('@/views/quiz/TopicDrill.vue') },
      { path: 'mock-exam', component: () => import('@/views/quiz/MockExam.vue') }
    ]
  },
  {
    path: '/admin',
    component: () => import('@/views/layout/AdminLayout.vue'),
    children: [
      { path: 'home', component: () => import('@/views/admin/AdminHome.vue') },
      { path: 'users-manage', component: () => import('@/views/admin/UserManage.vue') },
      { path: 'mistake-monitor', component: () => import('@/views/admin/MistakeMonitor.vue') },
      { path: 'subjects-manage', component: () => import('@/views/admin/SubjectManage.vue') },
      { path: 'books-manage', component: () => import('@/views/admin/BookManage.vue') },
      { path: 'questions-manage', component: () => import('@/views/admin/QuestionManage.vue') }
    ]
  }
]
```

#### 导航守卫

```javascript
router.beforeEach((to, from, next) => {
  const role = localStorage.getItem('role')
  const userStr = localStorage.getItem('user')
  let userRole = null
  let isLogin = false

  if (role && userStr) {
    try {
      const userObj = JSON.parse(userStr)
      if (userObj.role === role) {
        userRole = role
        isLogin = true
      }
    } catch (e) {
      console.error('用户信息解析失败', e)
    }
  }

  // 路由逻辑判断
  if (to.path === '/login') {
    if (isLogin) {
      next(userRole === 'admin' ? '/admin/home' : '/user/dashboard')
    } else {
      next()
    }
  } else if (to.path.startsWith('/admin')) {
    if (!isLogin) {
      next('/login')
    } else if (userRole !== 'admin') {
      ElMessage.error("权限不足，无法进入管理后台")
      next('/user/dashboard')
    } else {
      next()
    }
  } else {
    if (!isLogin) {
      next('/login')
    } else {
      next()
    }
  }
})
```

### 8.2 布局策略

#### AdminLayout（管理员布局）

- **侧边栏**: 管理功能导航菜单
- **顶栏**: 欢迎语 + 用户头像下拉菜单（退出登录）
- **内容区**: 管理功能页面

#### UserLayout（用户布局）

- **侧边栏**: 浅色主题，学习功能导航菜单
- **顶栏**: 用户信息 + 消息通知
- **内容区**: 学习功能页面

### 8.3 状态管理

#### user.js (Pinia Store)

```javascript
import { defineStore } from 'pinia'

export const useUserStore = defineStore('user', {
  state: () => ({
    userInfo: null,
    token: null
  }),

  actions: {
    setUserInfo(userInfo) {
      this.userInfo = userInfo
      localStorage.setItem('userInfo', JSON.stringify(userInfo))
    },

    setToken(token) {
      this.token = token
      localStorage.setItem('token', token)
    },

    clearUserInfo() {
      this.userInfo = null
      this.token = null
      localStorage.removeItem('userInfo')
      localStorage.removeItem('token')
    }
  }
})
```

---

## 10. 核心实现细节

### 10.1 多对多关系管理

#### 设计理念

使用映射表（`map_` 前缀）实现灵活的多对多关系，避免外键锁死，支持一道题属于多本书、多个科目。

#### 实现要点

1. **原子性操作**: 使用 `@Transactional` 保证多表操作的原子性

2. **级联查询**: 通过 JOIN 实现高效查询

3. **前端级联选择**: 使用 `el-cascader` 的 `multiple` 属性支持多选

```vue
<el-cascader
    v-model="formData.subjectIds"
    :options="subjectTree"
    :props="{ multiple: true, emitPath: false }"
    placeholder="请选择关联科目"
/>
```

### 9.2 树形结构处理

#### 场景

科目体系支持 4 层级结构（考试规格 → 具体学科 → 知识点 → 题型），并支持基于 scope 的多对多关系映射。

#### 核心算法

##### 1. 递归构建树

从扁平数据转换为树形结构：

```java
public List<SubjectDTO> getTree(Long userId, Integer rootId) {
    // 1. 获取所有科目
    List<Subject> allSubjects = list();

    // 2. 创建 DTO 对象
    Map<Integer, SubjectDTO> dtoMap = new HashMap<>();
    for (Subject s : allSubjects) {
        SubjectDTO dto = new SubjectDTO();
        BeanUtils.copyProperties(s, dto);
        dto.setChildren(new ArrayList<>());
        dtoMap.put(s.getId(), dto);
    }

    // 3. 构建树结构
    List<SubjectDTO> resultRoots = new ArrayList<>();
    for (Subject s : allSubjects) {
        SubjectDTO dto = dtoMap.get(s.getId());

        // 关联到父节点
        if (s.getParentId() != null && s.getParentId() != 0) {
            SubjectDTO parent = dtoMap.get(s.getParentId());
            if (parent != null) {
                parent.getChildren().add(dto);
            }
        }

        // 检查是否为目标根节点
        if (targetRootIds.contains(s.getId())) {
            resultRoots.add(dto);
        }
    }

    // 4. 递归汇总统计数据
    for (SubjectDTO root : resultRoots) {
        aggregateCounts(root);
    }

    return resultRoots;
}
```

##### 2. Scope 多对多关系过滤

在 `BookManage.vue` 中实现动态过滤：

```javascript
const filterSubjectTreeForCascader = (tree) => {
    const nodeMap = new Map()

    // 扁平化所有节点
    const collectNodes = (node) => {
        nodeMap.set(node.id, { ...node, children: [] })
        if (node.children && node.children.length > 0) {
            node.children.forEach(child => collectNodes(child))
        }
    }
    tree.forEach(node => collectNodes(node))

    // 构建树：基于 scope 字段动态分配子节点
    const buildTree = (examSpecId) => {
        const children = []

        nodeMap.forEach(node => {
            // 情况1：基于 scope 的多对多关系（Level 2/3 学科）
            if ((node.level === '2' || node.level === '3') && node.parentId === 0) {
                const belongsToSpec = !node.scope || node.scope.split(',')
                    .map(s => s.trim()).includes(examSpecId.toString())

                if (belongsToSpec) {
                    children.push(childNode)
                }
            }
            // 情况2：传统的 parent_id 关系
            else if (node.parentId === examSpecId) {
                children.push(childNode)
            }
        })

        return children
    }
}
```

##### 3. 虚拟分组节点创建

在 `getManageTree()` 中实现：

```java
public List<SubjectDTO> getManageTree() {
    // 1. 创建虚拟分组
    SubjectDTO englishGroup = createVNode(-2, "英语");
    SubjectDTO mathGroup = createVNode(-3, "数学");

    Map<Integer, SubjectDTO> vMap = new HashMap<>();
    vMap.put(-2, englishGroup);
    vMap.put(-3, mathGroup);

    // 2. 处理 Level 1 节点
    for (SubjectDTO dto : allDTOs) {
        if ("1".equals(dto.getLevel())) {
            int vId = getMathOrEnglishVId(dto.getId());
            if (vId != 0) {
                // 挂载到虚拟分组下
                vMap.get(vId).getChildren().add(dto);
            }
        }
    }

    // 3. 处理 scope 节点（parent_id=0 且有 scope）
    for (SubjectDTO dto : allDTOs) {
        if ((dto.getParentId() == null || dto.getParentId() == 0)
            && StringUtils.hasText(dto.getScope())) {
            String[] scopeIds = dto.getScope().split(",");
            for (String scopeIdStr : scopeIds) {
                Integer scopeId = Integer.parseInt(scopeIdStr.trim());
                SubjectDTO parentSpec = dtoMap.get(scopeId);
                if (parentSpec != null) {
                    parentSpec.getChildren().add(cloneNode);
                }
            }
        }
    }
}
```

##### 4. 递归排序

每层按 sort 字段排序，递归处理子节点：

```java
private void sortTree(List<SubjectDTO> nodes) {
    if (nodes == null || nodes.isEmpty()) {
        return;
    }

    // 对当前层级的节点按 sort 排序（sort 为 null 的排在最后）
    nodes.sort((a, b) -> {
        Integer sortA = a.getSort() != null ? a.getSort() : 9999;
        Integer sortB = b.getSort() != null ? b.getSort() : 9999;
        return sortA.compareTo(sortB);
    });

    // 递归排序子节点
    for (SubjectDTO node : nodes) {
        if (node.getChildren() != null && !node.getChildren().isEmpty()) {
            sortTree(node.getChildren());
        }
    }
}
```

### 9.3 拖拽排序实现

#### 技术栈

Element Plus `el-tree` 组件的 `draggable` 属性。

#### 拖拽逻辑

```javascript
const allowDrop = (draggingNode, dropNode, type) => {
  if (type === 'prev' || type === 'next') {
    return draggingNode.data.parentId === dropNode.data.parentId
  }
  return true  // inner 类型允许
}

const handleNodeDrop = async (draggingNode, dropNode, dropType, event) => {
  const updates = []
  let newParentId, newSort

  if (dropType === 'inner') {
    newParentId = dropNode.data.id
    newSort = dropNode.data.children?.length || 0
  } else if (dropType === 'prev' || dropType === 'next') {
    newParentId = dropNode.data.parentId || 0
    const siblings = dropNode.parent.data.children || dropNode.parent.data
    const index = siblings.findIndex(node => node.id === dropNode.data.id)
    newSort = dropType === 'prev' ? index : index + 1
  }

  updates.push({
    id: draggingNode.data.id,
    parentId: newParentId,
    sort: newSort
  })

  await request.post('/subject/batch-update-sort', updates)
  await loadTree()
}
```

### 9.4 ECharts 雷达图实现

#### 场景

用户学习统计可视化，展示正确率、覆盖度、活跃度三个维度。

#### ECharts 配置

```javascript
const option = {
  radar: {
    indicator: [
      { name: '高数', max: 100 },
      { name: '线代', max: 100 },
      { name: '概率', max: 100 }
    ]
  },
  series: [{
    type: 'radar',
    data: [
      {
        value: [85, 70, 90],  // 正确率
        name: '正确率'
      },
      {
        value: [60, 50, 75],  // 覆盖度
        name: '覆盖度'
      },
      {
        value: [40, 30, 50],  // 活跃度
        name: '活跃度'
      }
    ]
  }]
}
```

### 9.5 KaTeX 数学公式渲染

#### 后端存储

题目内容和解析支持 LaTeX 语法，存储为纯文本。

#### 前端渲染

1. **安装依赖**: `npm install katex`
2. **导入样式**: `import 'katex/dist/katex.min.css'`
3. **渲染公式**:

```vue
<template>
    <div v-html="renderedContent" class="katex-content"></div>
</template>

<script setup>
import katex from 'katex'

const renderedContent = computed(() => {
  return question.content.replace(/\$\$(.*?)\$\$/g, (match, formula) => {
    return katex.renderToString(formula, {
      throwOnError: false,
      displayMode: true  // 块级公式
    })
  }).replace(/\$(.*?)\$/g, (match, formula) => {
    return katex.renderToString(formula, {
      throwOnError: false,
      displayMode: false  // 行内公式
    })
  })
})
</script>
```

#### 公式类型

- **块级公式**: `$$...$$` → 独立一行，居中显示
- **行内公式**: `$...$` → 嵌入文本中显示

---

## 11. 开发规范

### 11.1 代码规范

#### 后端

1. **命名规范**:
   - 类名：大驼峰（PascalCase）
   - 方法名、变量名：小驼峰（camelCase）
   - 常量：全大写下划线分隔（UPPER_SNAKE_CASE）

2. **注释规范**:
   - 类和方法添加 JavaDoc 注释
   - 复杂逻辑添加行内注释

3. **异常处理**:
   - 使用 `GlobalExceptionHandler` 统一处理异常
   - 业务异常抛出 `BusinessException`

#### 前端

1. **命名规范**:
   - 组件名：大驼峰（PascalCase）
   - 文件名：短横线分隔（kebab-case）
   - 变量名、函数名：小驼峰（camelCase）

2. **组件规范**:
   - 使用 Vue 3 Composition API
   - 单文件组件结构：`<template>` → `<script setup>` → `<style scoped>`

3. **样式规范**:
   - 使用 scoped 样式避免污染
   - 优先使用 Element Plus 主题变量

### 10.2 认证与授权

#### 前端

- 依赖 `localStorage` (`role`, `userInfo`) 进行路由权限检查
- 路由守卫拦截未授权访问

#### 后端

- 使用 Spring Security 进行安全控制
- JWT Token 机制（待实现）

### 10.3 环境配置

#### 开发环境

- API 代理: `request.js` 中硬编码 `baseURL` 为 `http://localhost:8081`
- 数据库: MySQL 8.0 本地实例

#### 生产环境

- 需配置环境变量
- API URL 替换为生产环境地址

### 10.4 JSON 处理

- 后端使用 `JacksonTypeHandler` 处理复杂字段（如 `Question` 实体中的 `options`, `tags`）
- 自动将 JSON 字符串映射为 Java List

### 10.5 图标系统

- 使用 SVG 图标 (`assets/icons/`)
- 通过 `vite-svg-loader` 加载，需在导入时添加 `?url` 后缀
- 利用 CSS `filter` 属性实现图标颜色动态切换

```javascript
import icon from '@/assets/icons/icon.svg?url'
```

---

## 12. 已知问题与优化建议

### 12.1 当前限制

1. **题目与科目关联**: 一道题只能关联一个科目（通过 `map_question_subject` 的 `LIMIT 1` 实现）
2. **文件存储**: 当前仅支持本地存储，未接入 OSS
3. **公告系统**: 未实现，需要新建 `tb_notice` 表
4. **资源管理**: 未实现，需要基于 `tb_resource` 表开发
5. **全局标签管理**: 收藏标签未规范化管理
6. **科目层级限制**: 当前固定为 4 层级，未来如需扩展需修改 `SubjectLevelConstants.java`
7. **AI 批改稳定性**: GLM API 调用可能受到网络影响，需要进一步优化重试机制和降级策略

### 11.2 性能优化建议

1. **索引优化**:
   - 为 `map_question_book`、`map_question_subject` 的 `question_id` 和 `book_id/subject_id` 建立复合索引

2. **缓存策略**:
   - 使用 Redis 缓存热门题目和科目树
   - 缓存用户学习统计数据

3. **分页优化**:
   - 使用 MyBatis Plus 的 `selectPage` 方法避免全表扫描

4. **统计预计算**:
   - 定时任务预计算错题热力图，减少实时查询压力

5. **前端优化**:
   - 路由懒加载
   - 组件按需引入
   - 图片懒加载

### 12.3 扩展方向

1. **智能推荐**: 基于错题记录推荐薄弱知识点题目
2. **学习计划**: 自定义学习计划，每日打卡提醒
3. **社区讨论**: 题目评论区，支持提问和解答
4. **移动端适配**: 使用响应式布局或开发小程序版本
5. **数据导入导出**: Excel 批量导入题目、学习报告导出
6. **消息通知**: WebSocket 实时推送学习提醒
7. **AI 能力增强**:
   - 支持更多 AI 模型（如 GPT-4、文心一言）
   - 增强主观题批改准确性
   - 添加错题 AI 解析和知识点推荐
8. **考试功能完善**:
   - 添加考试排名和竞赛模式
   - 支持试卷共享和模板复用
   - 添加考试历史对比分析

---

## 附录

### A. 常见问题

#### Q1: 如何添加新的科目？

A: 在管理后台的"科目体系管理"中，点击"新增科目"，填写科目信息并选择父节点。注意：
- Level 1 科目的 `parent_id` 必须为 0
- 如果科目属于多个考试规格，设置 `parent_id=0` 并填写 `scope` 字段（如：`"2,3"` 表示属于英语一、二）
- 虚拟分组节点（英语、数学）由系统自动创建，无需手动添加

#### Q2: scope 字段如何使用？

A: scope 字段用于实现科目与考试规格的多对多关系：
- **适用场景**: 当一个科目属于多个考试规格时使用
- **格式**: 逗号分隔的考试规格 ID，如 `"4,5,6"` 表示属于数学一、二、三
- **空值**: scope 为空或 null 时表示"适用于所有考试规格"
- **示例**:
  - 高等数学：`scope="4,5,6"` → 数一、数二、数三都要学
  - 线性代数：`scope="4,6"` → 只属于数一、数三（数二不考）
  - 马原：不填 scope → 通过 parent_id=1 绑定到政治

#### Q3: 如何实现一道题关联多个科目？

A: 当前一道题只能关联一个科目。如需扩展，需修改 `MistakeRecordServiceImpl.java` 中的 `getSubjectIdByQuestionId` 方法，移除 `LIMIT 1` 限制。

#### Q4: 错题监控报错 "element cannot be mapped to a null key"？

A: 这是因为部分题目没有关联科目。已在 `MistakeRecordServiceImpl.java` 中通过 `.filter()` 过滤掉没有科目关联的记录。

#### Q5: 为什么 SubjectManage 页面显示英语、数学虚拟分组？

A: 这是后端 `getManageTree()` 方法自动创建的虚拟节点，用于优化前端展示：
- 英语分组（id=-2）包含：英语一、英语二
- 数学分组（id=-3）包含：数学一、数学二、数学三
- 虚拟节点不可编辑、删除、拖拽（通过 `id < 0` 或 `level === '0'` 判断）

#### Q6: BookManage 页面的科目选择器为什么显示的科目树和 SubjectManage 不同？

A: 两个页面的用途不同：
- **BookManage**: 使用 `filterSubjectTreeForCascader()` 函数，根据 scope 字段动态过滤，确保习题册关联的科目符合所选考试规格
- **SubjectManage**: 使用 `getManageTree()` 方法，显示完整的层级结构用于管理

#### Q7: 如何添加新的管理页面？

A:
1. 在 `views/admin/` 下创建 Vue 组件
2. 在 `router/index.js` 中添加路由
3. 在 `AdminLayout.vue` 中添加菜单项

#### Q8: 套卷刷题的 AI 批改如何工作？

A: 套卷刷题的 AI 批改流程如下：
1. 提取所有主观题（解答题、简答题）
2. 为每道题构建批改 Prompt，包含题目内容、学生解答、标准答案、满分分值
3. 调用 GLM-4.7 API 进行批改，设置 temperature=0.3 确保稳定性
4. 使用指数退避重试机制处理 API 超时（最大重试 3 次）
5. 解析返回的 JSON 格式评分结果（score、feedback、strengths、weaknesses）
6. 保存批改结果到 `tb_exam_answer_detail` 表
7. 汇总所有题目得分，生成 AI 总结报告

#### Q9: 如何配置 GLM-4.7 API？

A: 在 `application.yml` 中配置 API Key：
```yaml
zhipu:
  api:
    key: your-api-key-here
```
系统会自动读取该配置用于 AI 批改功能。

#### Q10: 考试快照是如何保存的？

A:
1. 前端定时调用 `/exam-session/snapshot` 接口
2. 传入答题进度 JSON 数据（包含所有题目的答案）
3. 后端将 JSON 字符串保存到 `tb_exam_session` 表的 `snapshot_answers` 字段
4. 提交考试时，系统会从快照中提取答案进行批改
5. 快照存储格式：`{"questionId1": "answer1", "questionId2": "answer2", ...}`

#### Q11: 套卷刷题支持哪些题目类型？

A: 目前支持以下题目类型的批改：
- **单选题**: 自动判分，完全匹配得满分
- **多选题**: 自动判分，选项完全一致得满分
- **主观题**（填空、简答、解答）: 使用 GLM-4.7 AI 批改，按步骤给分

#### Q12: 如何创建新的试卷？

A:
1. 管理员在后台创建试卷基本信息（标题、总分、时限、类型）
2. 配置试卷结构（大题名称、题目范围、分值）
3. 通过 `/paper/{paperId}/add-question` 接口逐个添加题目
4. 设置每道题的顺序、分值和所属大题
5. 保存后试卷即可供用户开始考试

A:
1. 在 `views/admin/` 下创建 Vue 组件
2. 在 `router/index.js` 中添加路由
3. 在 `AdminLayout.vue` 中添加菜单项

---

## 13. 附录

### A. 常见问题

#### Q1: 如何添加新的科目？

A: 在管理后台的"科目体系管理"中，点击"新增科目"，填写科目信息并选择父节点。注意：
- Level 1 科目的 `parent_id` 必须为 0
- 如果科目属于多个考试规格，设置 `parent_id=0` 并填写 `scope` 字段（如：`"2,3"` 表示属于英语一、二）
- 虚拟分组节点（英语、数学）由系统自动创建，无需手动添加

#### Q2: scope 字段如何使用？

A: scope 字段用于实现科目与考试规格的多对多关系：
- **适用场景**: 当一个科目属于多个考试规格时使用
- **格式**: 逗号分隔的考试规格 ID，如 `"4,5,6"` 表示属于数学一、二、三
- **空值**: scope 为空或 null 时表示"适用于所有考试规格"
- **示例**:
  - 高等数学：`scope="4,5,6"` → 数一、数二、数三都要学
  - 线性代数：`scope="4,6"` → 只属于数一、数三（数二不考）
  - 马原：不填 scope → 通过 parent_id=1 绑定到政治

#### Q3: 如何实现一道题关联多个科目？

A: 当前一道题只能关联一个科目。如需扩展，需修改 `MistakeRecordServiceImpl.java` 中的 `getSubjectIdByQuestionId` 方法，移除 `LIMIT 1` 限制。

#### Q4: 错题监控报错 "element cannot be mapped to a null key"？

A: 这是因为部分题目没有关联科目。已在 `MistakeRecordServiceImpl.java` 中通过 `.filter()` 过滤掉没有科目关联的记录。

#### Q5: 为什么 SubjectManage 页面显示英语、数学虚拟分组？

A: 这是后端 `getManageTree()` 方法自动创建的虚拟节点，用于优化前端展示：
- 英语分组（id=-2）包含：英语一、英语二
- 数学分组（id=-3）包含：数学一、数学二、数学三
- 虚拟节点不可编辑、删除、拖拽（通过 `id < 0` 或 `level === '0'` 判断）

#### Q6: BookManage 页面的科目选择器为什么显示的科目树和 SubjectManage 不同？

A: 两个页面的用途不同：
- **BookManage**: 使用 `filterSubjectTreeForCascader()` 函数，根据 scope 字段动态过滤，确保习题册关联的科目符合所选考试规格
- **SubjectManage**: 使用 `getManageTree()` 方法，显示完整的层级结构用于管理

#### Q7: 如何添加新的管理页面？

A:
1. 在 `views/admin/` 下创建 Vue 组件
2. 在 `router/index.js` 中添加路由
3. 在 `AdminLayout.vue` 中添加菜单项

---

**文档版本**: v1.5
**最后更新**: 2026-01-09
**维护者**: AI Assistant
**更新内容**:
- 新增套卷刷题功能模块
- 集成 GLM-4.7 AI 批改系统
- 添加试卷管理和考试会话相关表
- 完善套卷刷题相关 API 接口文档
- 更新项目功能模块总览和亮点
- 修正项目结构部分，添加所有新增文件和组件

### B. 科目层级常量定义

参考 `SubjectLevelConstants.java`，项目中所有科目层级相关的常量定义如下：

```java
public final class SubjectLevelConstants {
    // 层级值定义
    public static final String LEVEL_EXAM_SPEC = "1";          // Level 1: 考试规格
    public static final String LEVEL_SUBJECT = "2";            // Level 2: 具体学科
    public static final String LEVEL_KNOWLEDGE_POINT = "3";    // Level 3: 知识点
    public static final String LEVEL_QUESTION_TYPE = "4";       // Level 4: 题型/方法

    // 层级标签定义
    public static final String LABEL_EXAM_SPEC = "规格";
    public static final String LABEL_SUBJECT = "学科";
    public static final String LABEL_KNOWLEDGE_POINT = "知识点";
    public static final String LABEL_QUESTION_TYPE = "题型";

    // 工具方法示例
    public static String getLevelLabel(String level) {
        switch (level) {
            case "1": return "规格";
            case "2": return "学科";
            case "3": return "知识点";
            case "4": return "题型";
            default: return "未知";
        }
    }

    public static boolean isValidLevel(String level) {
        return "1".equals(level) || "2".equals(level) || "3".equals(level) || "4".equals(level);
    }

    public static String getNextLevel(String currentLevel) {
        switch (currentLevel) {
            case "1": return "2";
            case "2": return "3";
            case "3": return "4";
            case "4": return null;  // 已是最高层级
            default: return null;
        }
    }
}
```

### C. 数据库 ID 映射表

当前数据库中的考试规格 ID 映射：

| 考试规格 | ID | Level | 说明 |
|---------|-----|-------|------|
| 政治 | 1 | 1 | 单独的考试规格 |
| 英语一 | 2 | 1 | 属于"英语"虚拟分组 |
| 英语二 | 3 | 1 | 属于"英语"虚拟分组 |
| 数学一 | 4 | 1 | 属于"数学"虚拟分组 |
| 数学二 | 5 | 1 | 属于"数学"虚拟分组 |
| 数学三 | 6 | 1 | 属于"数学"虚拟分组 |
| 408 | 7 | 1 | 计算机考研专业课 |

### C. 套卷刷题相关常量定义

参考实际代码，项目中套卷刷题相关的常量定义如下：

#### 试卷类型常量

```java
public final class PaperTypeConstants {
    public static final Integer TYPE_REAL_EXAM = 0;    // 真题
    public static final Integer TYPE_MOCK_EXAM = 1;    // 模拟题

    public static String getPaperTypeName(Integer type) {
        if (type == null) return "未知";
        switch (type) {
            case 0: return "真题";
            case 1: return "模拟题";
            default: return "未知";
        }
    }
}
```

#### 考试会话状态常量

```java
public final class ExamSessionStatusConstants {
    public static final Integer STATUS_IN_PROGRESS = 0;  // 进行中
    public static final Integer STATUS_COMPLETED = 1;    // 已完成

    public static String getStatusName(Integer status) {
        if (status == null) return "未知";
        switch (status) {
            case 0: return "进行中";
            case 1: return "已完成";
            default: return "未知";
        }
    }
}
```

#### 题目类型判分常量

```java
public final class QuestionTypeConstants {
    public static final Integer TYPE_SINGLE_CHOICE = 1;  // 单选题
    public static final Integer TYPE_MULTIPLE_CHOICE = 2; // 多选题
    public static final Integer TYPE_FILL_BLANK = 3;     // 填空题
    public static final Integer TYPE_SHORT_ANSWER = 4;   // 简答题/解答题

    public static boolean isObjectiveQuestion(Integer type) {
        return type != null && (type == TYPE_SINGLE_CHOICE || type == TYPE_MULTIPLE_CHOICE);
    }

    public static boolean isSubjectiveQuestion(Integer type) {
        return type != null && (type == TYPE_FILL_BLANK || type == TYPE_SHORT_ANSWER);
    }
}
```

#### 答题正误状态常量

```java
public final class AnswerStatusConstants {
    public static final Integer STATUS_INCORRECT = 0;  // 错误
    public static final Integer STATUS_CORRECT = 1;    // 正确
    public static final Integer STATUS_PENDING = 2;   // 待定（主观题）

    public static String getStatusName(Integer status) {
        if (status == null) return "未知";
        switch (status) {
            case 0: return "错误";
            case 1: return "正确";
            case 2: return "待定";
            default: return "未知";
        }
    }
}
```

#### AI 批改配置常量

```java
public final class GLMGradingConstants {
    // GLM-4.7 API 配置
    public static final String GLM_MODEL = "glm-4";
    public static final Double GLM_TEMPERATURE = 0.3;
    public static final Integer MAX_RETRY_COUNT = 3;
    public static final Long BASE_RETRY_DELAY = 1000L; // 1秒

    // 批改 Prompt 模板
    public static final String GRADING_PROMPT_TEMPLATE =
            "你是一位资深的考研数学阅卷组长。请按照以下标准对学生的解答进行评分：\n\n" +
            "【题目内容】\n%s\n\n" +
            "【标准答案】\n%s\n\n" +
            "【学生解答】\n%s\n\n" +
            "【评分要求】\n" +
            "1. 本题满分：%.1f分\n" +
            "2. 请按步骤给分，关注解题思路的正确性和完整性\n" +
            "3. 即使最终答案错误，也要考虑过程分\n" +
            "4. 对于计算错误、逻辑错误要明确指出\n" +
            "5. 给出详细的建设性反馈\n\n" +
            "【输出格式】\n" +
            "请以JSON格式返回评分结果，格式如下：\n" +
            "{\n" +
            "  \"score\": 得分(数字),\n" +
            "  \"feedback\": \"详细的评分反馈\",\n" +
            "  \"strengths\": [\"优点1\", \"优点2\"],\n" +
            "  \"weaknesses\": [\"不足点1\", \"不足点2\"]\n" +
            "}\n\n" +
            "请确保输出的是有效的JSON格式。";

    // 试卷默认配置
    public static final Integer DEFAULT_TOTAL_SCORE = 150;  // 默认总分
    public static final Integer DEFAULT_TIME_LIMIT = 180;    // 默认时间（分钟）
}
```
