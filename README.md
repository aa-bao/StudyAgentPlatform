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
12. [管理端待完成功能清单](#12-管理端待完成功能清单-admin-todo-list)
13. [已知问题与优化建议](#13-已知问题与优化建议)
14. [附录](#14-附录)
15. [首页数据接口](#15-首页数据接口-home-page-api)
16. [未完成考试强制提醒功能](#16-未完成考试强制提醒功能-incomplete-exam-reminder)

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

- **多层级结构**: 考试类别 → 具体考试科目 → 知识模块 → 知识点 → 题型方法
- **多对多关系**: 通过 `scope` 字段实现科目与具体考试科目的灵活映射
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

### 2.8 考试会话管理增强功能

- **倒计时持久化**: 基于固定时间戳的倒计时，支持页面刷新、标签页关闭后继续计时
- **浏览器返回阻止**: 双重保护机制防止考生通过返回按钮离开考试页面
- **答题快照保存**: 定时自动保存答题进度到 localStorage，防止数据丢失
- **切屏检测**: 记录考生切屏次数，防止作弊行为
- **异常恢复**: 支持超时自动提交和刷新后状态恢复

### 2.9 未完成考试强制提醒功能

- **自动检测**: 用户登录后自动检测未完成考试，提升考试完成率
- **强制弹窗**: 弹窗无法通过常规方式关闭，确保用户注意到未完成考试
- **智能避让**: 在考试页面不会重复弹窗，优化用户体验
- **二次确认**: 放弃考试需要二次确认，防止误操作
- **美观设计**: 渐变色卡片和按钮，符合平台整体设计风格

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
| type | tinyint | NOT NULL | 题目类型 |
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
- 题目类型的具体取值范围：
  - 1: 单选题
  - 2: 多选题
  - 3: 填空题
  - 4: 综合应用题（408的大题，数学的大题，政治的大题）
  - 5：完型填空
  - 6：阅读理解
  - 7：新题型
  - 8：翻译题
  - 9：小作文
  - 10：大作文

#### 科目分类表 (`tb_subject`)

| 字段 | 类型 | 约束 | 说明 |
|------|------|------|------|
| id | int | PK, AUTO | 主键 |
| name | varchar(50) | NOT NULL | 科目名称 |
| parent_id | int | | 父级 ID（0 表示根节点） |
| icon | varchar(100) | | 图标 |
| sort | int | | 排序号（值越小越靠前） |
| level | varchar(100) | | 层级：1-具体考试科目, 2-知识模块, 3-知识点, 4-题型 |
| question_count | int | | 题目数量 |
| scope | varchar(50) | | 适用范围：`"4,5,6"`（数一、数二、数三） |

**多层级结构说明**:

系统采用简化且清晰的多层级科目体系，支持基于 `scope` 字段的多对多关系映射：

- **Level 1 (具体考试科目)**: 具体考试科目/根节点
  - 具体数据示例：政治(id=1)、英语一(id=2)、英语二(id=3)、数学一(id=4)、数学二(id=5)、数学三(id=6)、408(id=7)
  - 本平台有且仅有以上的具体考试科目

- **Level 2 (知识模块)**: 知识模块（可跨具体考试科目）
  - 示例：高等数学、线性代数、概率论、完形填空、阅读理解、数据结构
  - 两种归属方式：
    - **传统父子关系**: `parent_id = 具体考试科目ID`（如：马原 parent_id=1（政治））
    - **Scope 多对多关系**: `parent_id = 0`, `scope = "2,3"`（如：完形填空属于英语一、二）

- **Level 3 (知识点)**: 知识点/章节
  - 示例：函数与极限、行列式、矩阵
  - `parent_id = 知识模块ID` 或使用 scope 字段

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
| exam_spec_id | varchar(36) | | 具体考试科目 ID |
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
| switch_count | int | DEFAULT 0 | 切换题目次数（切屏检测） |
| ai_summary | text | | AI 总结 |
| snapshot_answers | json | | 答题快照 JSON |
| create_time | datetime | | 创建时间 |

**特殊说明**:
- `switch_count`: 记录考生切换题目的次数，用于异常行为检测
- `snapshot_answers`: 存储答题快照，支持意外中断后恢复
- 前端使用 localStorage 存储额外的会话信息和考试结束时间戳，支持页面刷新后恢复状态

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

- ✅ **多层级结构**: 具体考试科目 → 知识模块 → 知识点 → 题型
- ✅ **虚拟分组**: 自动创建政治、英语、数学、408 虚拟分组节点
- ✅ **拖拽排序**: 支持拖拽调整科目顺序和层级
- ✅ **scope 配置**: 支持配置科目适用范围（数一/数二/数三）
- ✅ **多对多关系**: 通过 scope 字段实现科目与具体考试科目的多对多映射
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
| `/subject/by-exam-spec/{examSpecId}` | GET | 根据具体考试科目获取子树 |
| `/subject/batch-update-sort` | POST | 批量更新排序（拖拽后） |

#### 虚拟分组机制

为了优化前端展示，`getManageTree()` 方法在后端自动创建虚拟分组节点：

- **英语 (id=-2, level="0")**: 包含英语一(id=2)、英语二(id=3)
- **数学 (id=-3, level="0")**: 包含数学一(id=4)、数学二(id=5)、数学三(id=6)
- **政治 (id=1, level="1")**: 直接作为根节点
- **408 (id=7, level="1")**: 直接作为根节点

**特殊处理规则**:
1. Level 1 的具体考试科目（英语一/二、数学一/二/三）自动挂载到对应的虚拟分组下
2. `parent_id=0` 且有 `scope` 字段的节点（如高数、完形填空）通过 scope 字段动态挂载到多个具体考试科目下
3. 虚拟分组节点（id < 0 或 level="0"）不可编辑、删除、拖拽

#### Scope 多对多关系

通过 `scope` 字段实现科目与多个具体考试科目的关联：

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

#### 考试会话管理增强功能

**倒计时持久化机制**

系统实现了基于固定时间戳的倒计时持久化，确保考生在各种异常情况下都不会丢失考试进度：

- **时间戳存储**: 首次开始考试时计算并存储固定的结束时间戳 `examEndTime = Date.now() + duration * 1000`
- **localStorage 缓存**: 使用 `exam_session_${paperId}_${userId}` 键存储会话信息和结束时间
- **刷新恢复**: 页面刷新时从 localStorage 恢复结束时间戳，计算剩余时间
- **超时检测**: 重新打开页面时自动检测是否超时，超时则自动提交

**核心实现逻辑**:

```javascript
// 初始化考试时检测并恢复会话
const initExam = async () => {
  const storageKey = `exam_session_${paperId}_${userId}`;
  const savedSessionData = localStorage.getItem(storageKey);

  if (savedSessionData) {
    // 从本地恢复结束时间
    const parsed = JSON.parse(savedSessionData);
    examEndTime.value = parsed.examEndTime;

    // 计算剩余时间
    const remaining = Math.max(0, Math.floor((examEndTime.value - Date.now()) / 1000));

    if (remaining === 0) {
      // 超时，清除数据重新开始
      localStorage.removeItem(storageKey);
    } else {
      // 恢复状态，使用保存的时间
      secondsLeft.value = remaining;
    }
  } else {
    // 首次开始，设置新的结束时间
    examEndTime.value = Date.now() + timeLimit * 1000;
    localStorage.setItem(storageKey, JSON.stringify({
      sessionId: sessionId.value,
      examEndTime: examEndTime.value,
      paperId,
      userId
    }));
  }
}

// 基于时间戳的计时器
const initTimer = () => {
  const updateTimer = () => {
    const now = Date.now();
    const remaining = Math.max(0, Math.floor((examEndTime.value - now) / 1000));
    secondsLeft.value = remaining;

    if (remaining === 0) {
      clearInterval(timerInterval);
      handleSubmit(true); // 自动提交
    }
  };

  timerInterval = setInterval(updateTimer, 1000);
};
```

**浏览器返回按钮阻止机制**

采用双重保护防止考生通过浏览器返回按钮离开考试页面：

1. **历史记录堆栈管理** (PaperList.vue):
   - 使用 `router.replace` 替代 `router.push`，防止历史记录堆栈增加
   - 替换当前历史记录条目，不添加新条目

2. **popstate 事件监听** (MockExam.vue):
   - 考试页面添加 popstate 事件监听
   - 用户点击返回时重新推送历史记录状态
   - 显示警告提示并记录违规次数
   - 提交考试后自动移除保护

**核心实现代码**:

```javascript
// PaperList.vue - 使用 router.replace
router.replace({
  path: '/user/mock-exam',
  query: { paperId, userId }
})

// MockExam.vue - 历史记录保护
const addHistoryProtection = () => {
  window.history.pushState(null, '', window.location.href);
  window.addEventListener('popstate', handlePopState);
};

const handlePopState = (event) => {
  if (isSubmitted.value) return; // 已提交不再阻止

  // 重新推送状态，阻止返回
  window.history.pushState(null, '', window.location.href);
  exitAttemptCount.value++;

  ElMessage.warning(`考试期间请勿使用浏览器返回按钮（违规尝试 ${exitAttemptCount.value} 次）`);
};

const removeHistoryProtection = () => {
  window.removeEventListener('popstate', handlePopState);
};
```

**localStorage 数据结构**:

```javascript
// Session 信息（包含 sessionId 和结束时间）
`exam_session_${paperId}_${userId}` = {
  sessionId: string,
  examEndTime: number,  // 时间戳
  paperId: string,
  userId: string
}

// 考试结束时间（备用）
`exam_end_time_${sessionId}` = timestampString

// 用户答案状态
`mock_exam_state` = {
  sessionId: string,
  answers: object,
  strokes: array
}
```

**功能特性**:

| 功能 | 说明 | 实现方式 |
|------|------|----------|
| 刷新页面恢复 | F5 刷新后倒计时继续 | localStorage 恢复 examEndTime |
| 关闭重开恢复 | 关闭标签页后重新打开继续计时 | localStorage 持久化 |
| 超时自动提交 | 检测到时间已到自动提交考试 | initTimer 中检测 remaining === 0 |
| 返回按钮阻止 | 考试中点击返回不生效 | router.replace + popstate 监听 |
| 答案自动保存 | 定时保存答题进度 | localStorage 存储 mock_exam_state |
| 切屏检测 | 记录考生切屏次数 | visibilitychange 事件监听 |
| 提交清理 | 提交后清除所有本地缓存 | localStorage.removeItem() |

**注意事项**:

1. **时间精度**: examEndTime 使用毫秒级时间戳，确保倒计时准确
2. **数据清理**: 提交或超时后必须清理 localStorage，避免影响后续考试
3. **浏览器隔离**: localStorage 是浏览器隔离的，更换浏览器会丢失数据
4. **手动清除**: 用户可通过开发者工具手动清除 localStorage 数据
5. **安全限制**: 客户端存储可能被篡改，服务器端应记录 session 开始时间作为验证

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
8. **用户管理** - 登录注册、资料管理、首页数据
9. **试卷管理** - 试卷 CRUD、题目关联
10. **套卷刷题管理** - 考试会话、AI批改、未完成考试检测
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

科目体系支持 多层级结构（具体考试科目 → 知识模块 → 知识点 → 题型），并支持基于 scope 的多对多关系映射。

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

## 12. 管理端待完成功能清单 (Admin Todo List)

> **最后更新**: 2026-01-13
> **说明**: 本章节列出管理端尚未完成的功能模块，按优先级排序

### 12.1 完成情况概览

| 状态 | 数量 | 说明 |
|------|------|------|
| ✅ 已完成 | 4项 | 试卷管理、用户管理、题目管理、科目管理 |
| ⏳ 待完成 | 11项 | 从考试记录管理到数据备份等功能 |

---

### 12.2 ✅ 已完成功能

#### 12.2.1 试卷管理功能 ⭐⭐⭐
**状态**: ✅ 已完成 | **优先级**: 高 | **完成时间**: 2026-01-13

**已实现内容**:
- ✅ 试卷列表页面（分页、多条件搜索）
- ✅ 试卷编辑/创建功能
- ✅ 试卷组卷功能（从题库选题）
- ✅ 试卷题目管理（添加/移除题目、设置分值、调整顺序）

**相关文件**:
- 前端: `KaoYanPlatform-front/src/views/admin/PaperManage.vue`
- API: `KaoYanPlatform-front/src/api/paper.js`
- 后端: `KaoYanPlatform-back/src/main/java/org/example/kaoyanplatform/controller/PaperController.java`

**访问路径**: `/admin/papers-manage`

---

#### 12.2.2 用户管理功能 ⭐⭐⭐
**状态**: ✅ 已完成

**已实现内容**:
- ✅ 用户列表查看
- ✅ 用户搜索和筛选
- ✅ 用户信息编辑/删除/创建

---

#### 12.2.3 题目管理功能 ⭐⭐⭐
**状态**: ✅ 已完成

**已实现内容**:
- ✅ 题目创建/编辑/删除
- ✅ 题目列表查询
- ✅ 题目分类管理
- ✅ 题目与科目/习题册关联

---

#### 12.2.4 科目体系管理功能 ⭐⭐⭐
**状态**: ✅ 已完成

**已实现内容**:
- ✅ 科目层级管理
- ✅ 权重配置
- ✅ 科目关联管理

---

### 12.3 ⏳ 待完成功能（按优先级排序）

#### 🔴 高优先级 ⭐⭐⭐

##### 12.3.1 考试记录管理功能
**状态**: ❌ 未开始 | **优先级**: 高

**需要实现**:
- ❌ 查看所有用户的考试记录列表
- ❌ 考试记录详情查看（答题详情、用时、得分）
- ❌ 考试记录删除/归档
- ❌ 按用户、试卷、时间筛选
- ❌ 考试记录导出功能

**数据库表**: `exam_session`, `exam_record`, `exam_answer_detail`

---

##### 12.3.2 学习进度监控功能
**状态**: ❌ 未开始 | **优先级**: 高

**需要实现**:
- ❌ 查看所有用户的学习进度列表
- ❌ 学习时长统计
- ❌ 用户活跃度分析
- ❌ 学习进度可视化（图表展示）
- ❌ 按科目、时间段筛选

**数据库表**: `user_progress`

---

#### 🟡 中优先级 ⭐⭐

##### 12.3.3 数据导入导出功能
**状态**: ❌ 未开始 | **优先级**: 中

**需要实现**:
- ❌ 题目批量导入（Excel/CSV）
- ❌ 题目批量导出
- ❌ 用户数据导出
- ❌ 科目体系导出/导入
- ❌ 试卷导出（Word/PDF格式）

---

##### 12.3.4 权限管理系统
**状态**: ❌ 未开始 | **优先级**: 中

**需要实现**:
- ❌ 管理员角色管理
- ❌ 权限分配
- ❌ 操作日志记录
- ❌ 角色与菜单权限关联
- ❌ 数据权限控制

**数据库表（需新建）**: `tb_role`, `tb_permission`, `map_role_permission`, `tb_admin_log`

---

##### 12.3.5 系统配置管理功能
**状态**: ❌ 未开始 | **优先级**: 中

**需要实现**:
- ❌ 系统参数配置
- ❌ 考试时间配置
- ❌ 功能开关配置
- ❌ 邮件/短信配置
- ❌ 第三方服务配置

**数据库表（需新建）**: `tb_system_config`

---

#### 🟢 低优先级 ⭐

##### 12.3.6 通知公告管理功能
**状态**: ❌ 未开始 | **优先级**: 低

**需要实现**:
- ❌ 发布系统公告
- ❌ 向用户发送通知
- ❌ 公告历史管理
- ❌ 公告置顶/撤销

**数据库表（需新建）**: `tb_announcement`, `tb_notification`

---

##### 12.3.7 内容审核功能
**状态**: ❌ 未开始 | **优先级**: 低

**需要实现**:
- ❌ 用户提交的题目审核
- ❌ 用户评论审核
- ❌ 违规内容管理
- ❌ 敏感词过滤

**数据库表（需新建）**: `tb_content_audit`

---

##### 12.3.8 收藏管理功能（管理员视角）
**状态**: ❌ 未开始 | **优先级**: 低

**需要实现**:
- ❌ 查看用户收藏统计
- ❌ 热门收藏分析
- ❌ 收藏趋势图表

---

##### 12.3.9 数据备份与恢复功能
**状态**: ❌ 未开始 | **优先级**: 低

**需要实现**:
- ❌ 数据库手动备份
- ❌ 数据库恢复功能
- ❌ 定时备份任务配置
- ❌ 备份文件管理

---

### 12.4 开发优先级路线图

**第一阶段（高优先级）⭐⭐⭐**
1. ✅ 试卷管理功能 - **已完成**
2. ⏳ 考试记录管理功能 - **待开发**
3. ⏳ 学习进度监控功能 - **待开发**

**第二阶段（中优先级）⭐⭐**
4. ⏳ 数据导入导出功能 - **待开发**
5. ⏳ 权限管理系统 - **待开发**
6. ⏳ 系统配置管理功能 - **待开发**

**第三阶段（低优先级）⭐**
7. ⏳ 通知公告管理功能 - **待开发**
8. ⏳ 内容审核功能 - **待开发**
9. ⏳ 收藏管理功能 - **待开发**
10. ⏳ 数据备份与恢复功能 - **待开发**

---

## 13. 已知问题与优化建议

### 13.1 当前限制

1. **题目与科目关联**: 一道题只能关联一个科目（通过 `map_question_subject` 的 `LIMIT 1` 实现）
2. **文件存储**: 当前仅支持本地存储，未接入 OSS
3. **公告系统**: 未实现，需要新建 `tb_notice` 表
4. **资源管理**: 未实现，需要基于 `tb_resource` 表开发
5. **全局标签管理**: 收藏标签未规范化管理
6. **科目层级限制**: 当前固定为 4 层级，未来如需扩展需修改 `SubjectLevelConstants.java`
7. **AI 批改稳定性**: GLM API 调用可能受到网络影响，需要进一步优化重试机制和降级策略

### 13.2 性能优化建议

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
- 如果科目属于多个具体考试科目，设置 `parent_id=0` 并填写 `scope` 字段（如：`"2,3"` 表示属于英语一、二）
- 虚拟分组节点（英语、数学）由系统自动创建，无需手动添加

#### Q2: scope 字段如何使用？

A: scope 字段用于实现科目与具体考试科目的多对多关系：
- **适用场景**: 当一个科目属于多个具体考试科目时使用
- **格式**: 逗号分隔的具体考试科目 ID，如 `"4,5,6"` 表示属于数学一、二、三
- **空值**: scope 为空或 null 时表示"适用于所有具体考试科目"
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
- **BookManage**: 使用 `filterSubjectTreeForCascader()` 函数，根据 scope 字段动态过滤，确保习题册关联的科目符合所选具体考试科目
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

#### Q13: 考试倒计时持久化是如何实现的？

A: 系统采用基于固定时间戳的方案：
1. **首次开始**: 计算并存储结束时间戳 `examEndTime = Date.now() + duration * 1000`
2. **localStorage 存储**: 使用 `exam_session_${paperId}_${userId}` 键保存会话信息
3. **刷新恢复**: 页面加载时检测 localStorage，恢复结束时间戳并计算剩余时间
4. **超时处理**: 检测到时间已到自动提交考试

核心优势：
- 时间戳固定，不会因刷新重置
- 支持关闭标签页后重新打开继续计时
- 每秒根据当前时间计算剩余时间，确保准确性

#### Q14: 为什么每次刷新都调用 startExam API？

A: 这是为了确保能正确获取题目数据。虽然会创建新的 session，但系统使用保存的 `examEndTime`，所以倒计时不会重置。这种方式简化了实现逻辑，避免了复杂的 session 恢复机制。

#### Q15: 如何阻止用户使用浏览器返回按钮？

A: 采用双重保护机制：
1. **PaperList.vue**: 使用 `router.replace` 替代 `router.push`，防止历史记录堆栈增加
2. **MockExam.vue**: 监听 `popstate` 事件，用户点击返回时重新推送历史记录状态，阻止实际导航
3. **违规记录**: 记录用户尝试返回的次数，显示警告提示
4. **提交恢复**: 考试提交后移除保护，恢复正常导航

#### Q16: 如果用户关闭标签页超过考试时长会怎样？

A: 重新打开页面时，系统会检测到时间已到（remaining === 0），自动提交试卷。用户无需担心超时未提交的问题。

#### Q17: localStorage 数据会占用多少空间？

A: 单个考试会话的数据结构如下：
```javascript
{
  sessionId: "uuid-string",           // ~36 bytes
  examEndTime: 1234567890123,         // 8 bytes
  paperId: "uuid-string",             // ~36 bytes
  userId: "uuid-string"               // ~36 bytes
}
```
总计约 116 bytes，远低于 localStorage 5-10MB 的限制。即使存储 1000 个会话也只占约 116KB。

#### Q18: 更换浏览器或设备会怎样？

A: localStorage 是浏览器隔离的，更换浏览器或设备会丢失未保存的数据。这是客户端存储的限制，如需跨设备同步，需要实现服务器端 session 管理和账号系统。

#### Q19: 如何手动清除考试数据？

A:
1. 打开浏览器开发者工具（F12）
2. 进入 Application/Storage → Local Storage
3. 删除以 `exam_session_` 或 `exam_end_time_` 开头的键
4. 删除 `mock_exam_state` 键（答题状态）

#### Q20: 考试期间切屏会被记录吗？

A: 是的，系统通过 `visibilitychange` 事件监听页面可见性变化：
- 当用户切换标签页或最小化窗口时触发
- 调用 `/exam-session/switch` 接口记录切屏次数
- 切屏次数保存在 `tb_exam_session` 表的 `switch_count` 字段
- 可用于考后分析或异常行为检测

A:
1. 在 `views/admin/` 下创建 Vue 组件
2. 在 `router/index.js` 中添加路由
3. 在 `AdminLayout.vue` 中添加菜单项

---

## 14. 附录

### A. 常见问题

#### Q1: 如何添加新的科目？

A: 在管理后台的"科目体系管理"中，点击"新增科目"，填写科目信息并选择父节点。注意：
- Level 1 科目的 `parent_id` 必须为 0
- 如果科目属于多个具体考试科目，设置 `parent_id=0` 并填写 `scope` 字段（如：`"2,3"` 表示属于英语一、二）
- 虚拟分组节点（英语、数学）由系统自动创建，无需手动添加

#### Q2: scope 字段如何使用？

A: scope 字段用于实现科目与具体考试科目的多对多关系：
- **适用场景**: 当一个科目属于多个具体考试科目时使用
- **格式**: 逗号分隔的具体考试科目 ID，如 `"4,5,6"` 表示属于数学一、二、三
- **空值**: scope 为空或 null 时表示"适用于所有具体考试科目"
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
- **BookManage**: 使用 `filterSubjectTreeForCascader()` 函数，根据 scope 字段动态过滤，确保习题册关联的科目符合所选具体考试科目
- **SubjectManage**: 使用 `getManageTree()` 方法，显示完整的层级结构用于管理

#### Q7: 如何添加新的管理页面？

A:
1. 在 `views/admin/` 下创建 Vue 组件
2. 在 `router/index.js` 中添加路由
3. 在 `AdminLayout.vue` 中添加菜单项

---

**文档版本**: v1.6
**最后更新**: 2025-01-11
**维护者**: AI Assistant
**更新内容**:
- 新增考试会话管理增强功能（第2.8节）
- 完善考试会话表说明，增加 switch_count 和 snapshot_details 字段说明
- 新增考试倒计时持久化机制完整文档（第7.3节）
  - 基于时间戳的倒计时实现原理
  - localStorage 数据结构设计
  - 刷新恢复和超时自动提交机制
- 新增浏览器返回按钮阻止机制文档（第7.3节）
  - 历史记录堆栈管理（router.replace）
  - popstate 事件监听实现
  - 违规尝试次数记录
- 新增功能特性对比表，汇总所有考试增强功能
- 新增常见问题 Q13-Q20
  - 倒计时持久化实现原理
  - startExam API 调用说明
  - 浏览器返回按钮阻止机制
  - localStorage 空间占用分析
  - 切屏检测功能说明
- 更新项目亮点部分，增加考试会话管理增强功能
- 更新相关文件清单，包含 PaperList.vue 和 MockExam.vue 修改说明

**相关文件**:
- `kaoyan-frontend/src/views/PaperList.vue` - 修改路由跳转方式为 router.replace
- `kaoyan-frontend/src/views/quiz/MockExam.vue` - 新增历史记录保护和倒计时持久化功能
- `docs/exam-timer-persistence.md` - 考试倒计时持久化功能详细文档
- `docs/exam-persistence-changes.md` - 完整的改动总结文档（可选）

### B. 科目层级常量定义

参考 `SubjectLevelConstants.java`，项目中所有科目层级相关的常量定义如下：

```java
public final class SubjectLevelConstants {
    // 层级值定义
    public static final String LEVEL_EXAM_SPEC = "1";          // Level 1: 具体考试科目
    public static final String LEVEL_SUBJECT = "2";            // Level 2: 知识模块
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

当前数据库中的具体考试科目 ID 映射：

| 具体考试科目 | ID | Level | 说明 |
|---------|-----|-------|------|
| 政治 | 1 | 1 | 单独的具体考试科目 |
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

---

## 15. 首页数据接口 (Home Page API)

### 14.1 功能概述

用户首页 (`/user/home`) 需要展示多种数据，包括用户信息、学习统计、考试科目、每日励志语录和个性化推荐。为优化性能，提供统一的首页数据接口，一次性返回所有所需数据。

### 14.2 数据结构设计

#### HomePageDataDTO.java

```java
@Data
public class HomePageDataDTO {
    private UserInfo userInfo;           // 用户基本信息
    private StudyStats studyStats;       // 学习统计数据
    private List<Subject> subjects;      // 考试科目列表
    private DailyQuote dailyQuote;       // 每日励志语录
    private List<Recommendation> recommendations; // 个性化推荐
}
```

**内嵌类说明**:

1. **UserInfo** - 用户基本信息
   - id, username, nickname, avatar, role
   - targetSchool, targetTotalScore
   - examYear, examDate, examSubjects

2. **StudyStats** - 学习统计
   - questionsDone: 累计刷题数
   - todayStudyTime: 今日学习时长(小时)
   - accuracy: 总体正确率
   - mistakesCount: 错题本数量
   - totalStudyHours: 总学习时长(小时)
   - consecutiveDays: 连续打卡天数

3. **Subject** - 科目信息
   - id, name, level

4. **DailyQuote** - 每日励志语录
   - content: 语录内容
   - author: 作者

5. **Recommendation** - 个性化推荐
   - id, subjectName, subjectColor
   - difficulty, content, reason

### 14.3 API 接口

| 接口 | 方法 | 说明 |
|------|------|------|
| `/user/homeData/{userId}` | GET | 获取用户首页数据 |

#### 请求示例

```http
GET http://localhost:8081/user/homeData/1
```

#### 响应示例

```json
{
  "code": 200,
  "msg": "成功",
  "data": {
    "userInfo": {
      "id": 1,
      "username": "student",
      "nickname": "考研人_abc123",
      "avatar": "http://localhost:8081/uploads/avatar.jpg",
      "role": "student",
      "targetSchool": "清华大学",
      "targetTotalScore": 400,
      "examYear": "2026",
      "examDate": "2025-12-20",
      "examSubjects": "政治,英语一,数学一"
    },
    "studyStats": {
      "questionsDone": 128,
      "todayStudyTime": 2.5,
      "accuracy": 78.5,
      "mistakesCount": 26,
      "totalStudyHours": 42.0,
      "consecutiveDays": 7
    },
    "subjects": [
      {"id": 1, "name": "政治", "level": 1},
      {"id": 4, "name": "数学一", "level": 1},
      {"id": 2, "name": "英语一", "level": 1}
    ],
    "dailyQuote": {
      "content": "不积跬步，无以至千里；不积小流，无以成江海。",
      "author": "荀子"
    },
    "recommendations": []
  }
}
```

### 14.4 后端实现

#### UserService.java

```java
/**
 * 获取用户首页数据
 * @param userId 用户ID
 * @return 首页数据
 */
HomePageDataDTO getHomePageData(Long userId);
```

#### UserServiceImpl.java

```java
@Override
public HomePageDataDTO getHomePageData(Long userId) {
    HomePageDataDTO homeData = new HomePageDataDTO();
    
    // 1. 获取并组装用户基本信息
    User user = getById(userId);
    HomePageDataDTO.UserInfo userInfo = new HomePageDataDTO.UserInfo();
    // ... 设置用户信息
    homeData.setUserInfo(userInfo);
    
    // 2. 获取学习统计数据
    HomePageDataDTO.StudyStats stats = new HomePageDataDTO.StudyStats();
    // ... 从UserProgress、ExamRecord等表统计
    homeData.setStudyStats(stats);
    
    // 3. 获取考试科目列表（Level 1 - 具体考试科目）
    QueryWrapper<Subject> subjectWrapper = new QueryWrapper<>();
    subjectWrapper.eq("level", "1");
    List<Subject> subjects = subjectMapper.selectList(subjectWrapper);
    homeData.setSubjects(subjectList);
    
    // 4. 每日励志语录（可扩展为随机或按日期）
    HomePageDataDTO.DailyQuote quote = new HomePageDataDTO.DailyQuote();
    quote.setContent("不积跬步，无以至千里...");
    quote.setAuthor("荀子");
    homeData.setDailyQuote(quote);
    
    // 5. 个性化推荐（后续可基于算法实现）
    homeData.setRecommendations(new ArrayList<>());
    
    return homeData;
}
```

#### UserController.java

```java
@GetMapping("/homeData/{userId}")
@Operation(summary = "获取用户首页数据")
public Result getHomePageData(
        @Parameter(description = "用户ID", required = true) 
        @PathVariable Long userId) {
    var homeData = userService.getHomePageData(userId);
    if (homeData == null) {
        return Result.error("用户不存在");
    }
    return Result.success(homeData);
}
```

### 14.5 前端实现

#### API 调用 (user.js)

```javascript
// 获取用户首页数据
export function getHomePageDataApi(userId) {
    return request({
        url: `/user/homeData/${userId}`,
        method: 'get'
    })
}
```

#### Home.vue 数据获取

```javascript
import { getHomePageDataApi } from '@/api/user'
import { ElMessage } from 'element-plus'

// 获取首页数据
const fetchHomePageData = async () => {
    try {
        const userId = localStorage.getItem('userId')
        if (!userId) {
            ElMessage.error('未登录，请先登录')
            router.push('/login')
            return
        }

        const res = await getHomePageDataApi(userId)
        if (res.code === 200) {
            const data = res.data
            
            // 更新用户信息
            if (data.userInfo) {
                userInfo.value = data.userInfo
            }
            
            // 更新学习统计
            if (data.studyStats) {
                stats.value.questionsDone = data.studyStats.questionsDone || 0
                stats.value.studyTime = data.studyStats.todayStudyTime || 0
                stats.value.accuracy = data.studyStats.accuracy || 0
                stats.value.mistakes = data.studyStats.mistakesCount || 0
                
                userStats.value.questionsDone = data.studyStats.questionsDone || 0
                userStats.value.accuracy = data.studyStats.accuracy || 0
                userStats.value.studyHours = data.studyStats.totalStudyHours || 0
            }
            
            // 更新考试科目
            if (data.subjects && data.subjects.length > 0) {
                examSubjects.value = data.subjects
            }
            
            // 更新每日语录
            if (data.dailyQuote) {
                dailyQuote.value = data.dailyQuote
            }
        }
    } catch (error) {
        console.error('获取首页数据失败:', error)
        ElMessage.error('获取首页数据失败')
    }
}

// 组件挂载时获取数据
onMounted(() => {
    fetchHomePageData()
    // ... 其他初始化逻辑
})
```

### 14.6 数据初始化改进

**改进前** (静态硬编码):

```javascript
const stats = ref({
    questionsDone: 128,
    studyTime: 2.5,
    accuracy: 78,
    mistakes: 26
})
```

**改进后** (动态获取):

```javascript
const stats = ref({ 
    questionsDone: 0, 
    studyTime: 0, 
    accuracy: 0, 
    mistakes: 0 
})

// 通过 fetchHomePageData() 从后端获取真实数据
```

### 14.7 性能优化

1. **单次请求替代多次请求**
   - 改进前: 需要 5 次独立请求 (用户信息、统计、科目、语录、推荐)
   - 改进后: 1 次请求获取所有数据

2. **减少网络开销**
   - 降低 HTTP 请求头和数据传输开销
   - 减少前端等待时间

3. **数据一致性**
   - 确保所有数据来自同一时间点
   - 避免多次请求间的数据变化

### 14.8 扩展建议

1. **每日励志语录**
   - 创建 `tb_daily_quote` 表
   - 根据日期随机选择或按日期匹配
   - 支持管理员上传自定义语录

2. **个性化推荐**
   - 基于错题记录推荐薄弱知识点题目
   - 基于学习进度推荐下一步练习
   - 协同过滤推荐相似用户的学习路径

3. **学习时长统计**
   - 在 `UserProgress` 表添加 `study_duration` 字段
   - 记录每次学习的实际时长
   - 实现精确的学习时间统计

4. **连续打卡**
   - 创建 `tb_check_in` 表
   - 记录每日打卡记录
   - 计算连续打卡天数

5. **数据缓存**
   - 对首页数据进行 Redis 缓存
   - 设置合理的过期时间（如 5 分钟）
   - 用户提交答题后清除缓存

### 14.9 测试用例

```javascript
// 测试用例 1: 正常获取首页数据
test('should fetch home page data successfully', async () => {
    const userId = 1
    const res = await getHomePageDataApi(userId)
    expect(res.code).toBe(200)
    expect(res.data.userInfo).toBeDefined()
    expect(res.data.studyStats).toBeDefined()
    expect(res.data.subjects).toBeInstanceOf(Array)
})

// 测试用例 2: 用户不存在
test('should return error for non-existent user', async () => {
    const userId = 99999
    const res = await getHomePageDataApi(userId)
    expect(res.code).toBe(500)
    expect(res.msg).toContain('用户不存在')
})
```

---

## 16. 未完成考试强制提醒功能 (Incomplete Exam Reminder)

### 15.1 功能概述

为提升用户考试完成率，系统实现了**未完成考试强制提醒功能**。当用户登录后进入平台任何页面时，如果检测到用户有未完成的模拟考试（status=0），系统会强制弹窗提醒用户继续考试。

### 15.2 功能特性

- ✅ **自动检测**：用户登录后自动检测未完成考试
- ✅ **强制弹窗**：弹窗无法通过常规方式关闭（无法点击遮罩关闭、无法按ESC关闭、无关闭按钮）
- ✅ **智能避让**：在考试页面不会重复弹窗
- ✅ **二次确认**：放弃考试需要二次确认，防止误操作
- ✅ **美观设计**：渐变色卡片和按钮，符合平台整体设计风格
- ✅ **延迟显示**：延迟1秒显示，避免页面加载时突兀

### 15.3 后端实现

#### API 接口

**接口地址**: `GET /exam-session/user/{userId}/incomplete`

**功能说明**: 根据用户ID获取所有进行中的考试会话（status=0）

**请求参数**:
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| userId | String | 是 | 用户ID |

**响应示例**:
```json
{
  "code": 200,
  "msg": "成功",
  "data": [
    {
      "id": "uuid-string",
      "userId": "user-uuid",
      "paperId": "paper-uuid",
      "status": 0,
      "startTime": "2026-01-11 10:30:00",
      "createTime": "2026-01-11 10:30:00"
    }
  ]
}
```

#### ExamSessionController.java

```java
@GetMapping("/user/{userId}/incomplete")
@Operation(summary = "获取用户未完成考试", description = "根据用户ID获取所有进行中的考试会话（status=0）")
public Result<List<ExamSession>> getIncompleteSessions(
        @Parameter(description = "用户ID", required = true) @PathVariable String userId) {
    try {
        LambdaQueryWrapper<ExamSession> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ExamSession::getUserId, userId)
               .eq(ExamSession::getStatus, 0) // 0-进行中
               .orderByDesc(ExamSession::getCreateTime);
        List<ExamSession> sessions = examSessionService.list(wrapper);
        return Result.success(sessions);
    } catch (Exception e) {
        return Result.error(e.getMessage());
    }
}
```

### 15.4 前端实现

#### API 调用 (examSession.js)

```javascript
// 获取用户未完成考试列表
export function getIncompleteSessions(userId) {
    return request({
        url: `/exam-session/user/${userId}/incomplete`,
        method: 'get'
    })
}
```

#### UserLayout.vue 核心逻辑

**1. 状态管理**
```javascript
// 未完成考试弹窗相关
const showIncompleteExamDialog = ref(false)
const incompleteExamInfo = ref({
    paperTitle: '',
    startTime: '',
    sessionId: '',
    paperId: '',
    userId: ''
})
```

**2. 检测未完成考试**
```javascript
const checkIncompleteExam = async () => {
    try {
        const userId = localStorage.getItem('userId') || userStore.userInfo?.id
        if (!userId) return

        // 检查当前是否已经在考试页面，避免重复弹窗
        if (route.path === '/user/mock-exam') {
            return
        }

        const res = await getIncompleteSessions(userId)
        if (res.code === 200 && res.data && res.data.length > 0) {
            // 有未完成的考试
            const session = res.data[0] // 取最近的一个

            // 获取试卷详情
            const paperRes = await getPaperDetail(session.paperId)
            if (paperRes.code === 200 && paperRes.data) {
                incompleteExamInfo.value = {
                    paperTitle: paperRes.data.title,
                    startTime: formatTime(session.createTime),
                    sessionId: session.id,
                    paperId: session.paperId,
                    userId: userId
                }

                // 显示强制弹窗
                showIncompleteExamDialog.value = true
            }
        }
    } catch (error) {
        console.error('检测未完成考试失败:', error)
    }
}
```

**3. 时间格式化**
```javascript
const formatTime = (timeStr) => {
    if (!timeStr) return ''
    const date = new Date(timeStr)
    const now = new Date()
    const diff = Math.floor((now - date) / 1000 / 60) // 分钟差

    if (diff < 1) return '刚刚开始'
    if (diff < 60) return `${diff}分钟前开始`
    const hours = Math.floor(diff / 60)
    if (hours < 24) return `${hours}小时前开始`
    const days = Math.floor(hours / 24)
    return `${days}天前开始`
}
```

**4. 继续考试**
```javascript
const continueExam = () => {
    showIncompleteExamDialog.value = false
    router.replace({
        path: '/user/mock-exam',
        query: {
            paperId: incompleteExamInfo.value.paperId,
            userId: incompleteExamInfo.value.userId
        }
    })
}
```

**5. 放弃考试（带二次确认）**
```javascript
const abandonExam = () => {
    ElMessageBox.confirm(
        '确定要放弃当前考试吗？放弃后当前答题进度将不会保存，建议您继续完成考试。',
        '警告',
        {
            confirmButtonText: '仍要放弃',
            cancelButtonText: '继续考试',
            type: 'warning',
            distinguishCancelAndClose: true
        }
    ).then(() => {
        showIncompleteExamDialog.value = false
        ElMessage.warning('您已放弃考试，如需继续请从套卷列表重新进入')
    }).catch(() => {
        // 用户选择继续考试
    })
}
```

**6. 组件挂载时自动检测**
```javascript
onMounted(() => {
    // 延迟1秒检测，避免页面加载时立即弹窗影响体验
    setTimeout(() => {
        checkIncompleteExam()
    }, 1000)
})
```

**7. 弹窗模板**
```vue
<!-- 未完成考试强制弹窗 -->
<el-dialog
    v-model="showIncompleteExamDialog"
    title="⚠️ 检测到未完成的考试"
    width="500px"
    :close-on-click-modal="false"
    :close-on-press-escape="false"
    :show-close="false"
    class="incomplete-exam-dialog"
>
    <div class="dialog-content">
        <div class="exam-info-card">
            <div class="exam-icon">📝</div>
            <div class="exam-details">
                <h3 class="exam-title">{{ incompleteExamInfo.paperTitle }}</h3>
                <p class="exam-time">{{ incompleteExamInfo.startTime }}</p>
            </div>
        </div>
        <div class="warning-text">
            <p>您有一场考试尚未完成，为了您的考试成绩，请继续完成考试。</p>
        </div>
    </div>
    <template #footer>
        <div class="dialog-footer">
            <el-button @click="abandonExam" class="abandon-btn">放弃考试</el-button>
            <el-button type="primary" @click="continueExam" class="continue-btn">继续考试</el-button>
        </div>
    </template>
</el-dialog>
```

### 15.5 交互流程

```
用户登录 → 进入任意页面 → UserLayout 组件挂载
    ↓
延迟1秒后自动检测未完成考试
    ↓
有未完成考试？
    ├─ 是 → 显示强制弹窗
    │     ├─ 点击"继续考试" → 跳转到 MockExam 页面
    │     └─ 点击"放弃考试" → 二次确认弹窗
    │           ├─ 确认放弃 → 关闭弹窗，显示警告消息
    │           └─ 取消 → 返回强制弹窗
    └─ 否 → 正常使用平台
```

### 15.6 弹窗样式设计

**设计原则**:
- 使用渐变色背景突出重要性
- 警告色（橙色）标题引起注意
- 考试信息卡片使用蓝色主题
- "继续考试"按钮使用醒目的蓝色渐变
- "放弃考试"按钮使用灰色，降低视觉权重

**关键样式类**:
```css
.incomplete-exam-dialog :deep(.el-dialog__header) {
    background: linear-gradient(135deg, #fff7ed 0%, #ffedd5 100%);
    border-bottom: 2px solid #f97316;
}

.exam-info-card {
    background: linear-gradient(135deg, #eff6ff 0%, #dbeafe 100%);
    border: 2px solid #3b82f6;
    border-radius: 12px;
}

.continue-btn {
    background: linear-gradient(135deg, #2563eb 0%, #1d4ed8 100%);
}

.abandon-btn {
    background: #f3f4f6;
    color: #6b7280;
}
```

### 15.7 技术要点

**1. 路由状态判断**
- 检测 `route.path` 避免在考试页面重复弹窗
- 使用 `router.replace` 替代 `router.push`，避免历史记录堆栈问题

**2. 延迟检测**
- 使用 `setTimeout` 延迟1秒，避免页面加载时立即弹窗
- 提升用户体验，给用户一个缓冲时间

**3. 强制弹窗实现**
- `:close-on-click-modal="false"` - 禁止点击遮罩关闭
- `:close-on-press-escape="false"` - 禁止ESC键关闭
- `:show-close="false"` - 隐藏关闭按钮

**4. 二次确认机制**
- 使用 `ElMessageBox.confirm` 实现放弃考试的二次确认
- `distinguishCancelAndClose: true` 区分取消和关闭操作
- 防止用户误操作放弃考试

### 15.8 扩展建议

**1. 多考试提醒**
- 当前只显示最近的一个未完成考试
- 可扩展为列表展示所有未完成考试
- 支持用户选择继续哪一场考试

**2. 提醒频率控制**
- 添加"今日不再提醒"选项
- 使用 localStorage 记录用户选择
- 避免频繁弹窗打扰用户

**3. 智能提醒**
- 根据考试剩余时间调整提示语
- 临期考试显示更醒目的警告
- 超时考试自动标记为放弃

**4. 统计分析**
- 记录用户对提醒的响应情况
- 统计继续考试vs放弃考试的比例
- 分析弹窗对考试完成率的影响

**5. 通知增强**
- 支持浏览器桌面通知
- 邮件/短信提醒未完成考试
- 设置考试截止时间提醒

### 15.9 相关文件清单

**后端文件**:
- `KaoYanPlatform-back/src/main/java/org/example/kaoyanplatform/controller/ExamSessionController.java` - 新增获取未完成考试API

**前端文件**:
- `KaoYanPlatform-front/src/api/examSession.js` - 新增 `getIncompleteSessions` 方法
- `KaoYanPlatform-front/src/views/layout/UserLayout.vue` - 添加检测逻辑和弹窗组件

**测试建议**:
1. 创建一个进行中的考试会话（status=0）
2. 登录后进入任意页面，观察是否弹窗
3. 测试"继续考试"按钮是否正常跳转
4. 测试"放弃考试"按钮是否显示二次确认
5. 测试在考试页面是否不会重复弹窗

---

**文档更新日期**: 2026-01-11
**更新内容**: 新增第15章 - 未完成考试强制提醒功能文档，包含功能特性、API接口、前后端实现、交互流程和技术要点

---

## 16. 题目批量导入功能

### 16.1 功能概述

题目批量导入功能允许管理员通过上传 JSON 文件，快速将大量题目导入到题库中。该功能支持：

- **文件上传**: 支持 .json 格式文件，最大 10MB
- **实时解析**: 上传后立即解析 JSON 内容，预览所有题目
- **智能去重**: 自动检测并跳过与库中重复的题目
- **在线编辑**: 导入前可在线编辑题目内容
- **选择性导入**: 支持选择部分题目导入
- **灵活关联**: 支持导入到现有习题册或新建习题册/试卷

### 16.2 从PDF到JSON的转换流程

#### 16.2.1 推荐方案：使用LLM处理（MinerU + 智谱AI）

由于从PDF提取的Markdown格式往往混乱，使用LLM（大语言模型）处理比Python脚本更灵活准确。

**步骤1: 使用MinerU提取PDF为Markdown**

```bash
# 安装MinerU (参考官方文档)
# 提取PDF到Markdown
minerU_pdf_to_markdown input.pdf -o output_dir
```

**步骤2: 使用LLM转换为JSON**

项目提供了Python脚本 `ExtractQuestionFromPDF/process_md_with_llm.py`：

```bash
cd ExtractQuestionFromPDF

# 安装依赖
pip install zhipuai

# 配置API Key (编辑 config.py)
ZHIPU_API_KEY = "your_api_key_here"

# 单文件处理
python process_md_with_llm.py

# 批量处理整个目录
python process_md_with_llm.py --batch
```

**优势**：
- ✅ LLM能理解混乱的格式
- ✅ 自动提取题目、答案、解析
- ✅ 支持图片处理（转为Base64）
- ✅ 识别题目类型
- ✅ 比正则表达式更灵活

#### 16.2.2 JSON格式规范（支持图片）

**基本格式**:
```json
{
  "questions": [
    {
      "type": 1,
      "content": "题干内容，[图片:img_0]如适用",
      "options": ["A. 选项1", "B. 选项2", "C. 选项3", "D. 选项4"],
      "answer": "A",
      "analysis": "解析内容",
      "tags": ["标签1", "标签2"]
    }
  ],
  "images": [
    {
      "id": "img_0",
      "filename": "c5f6459ba2900a7f.jpg",
      "base64": "data:image/jpeg;base64,/9j/4AAQSkZJRgABAQAA..."
    }
  ]
}
```

**字段说明**:

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `questions` | Array | ✅ | 题目数组 |
| `type` | Integer | ✅ | 题目类型：1-单选, 2-多选, 3-填空, 4-简答 |
| `content` | String | ✅ | 题干内容，可用`[图片:img_x]`引用图片 |
| `options` | Array<String> | ⚠️ | 选项数组，选择题必填 |
| `answer` | String | ✅ | 正确答案 |
| `analysis` | String | ❌ | 题目解析 |
| `tags` | Array<String> | ❌ | 题目标签 |
| `images` | Array | ❌ | 图片数组（当题目包含图片时） |
| `images[].id` | String | ✅ | 图片ID，如`img_0` |
| `images[].filename` | String | ✅ | 原始文件名 |
| `images[].base64` | String | ✅ | Base64编码的图片数据 |

#### 16.2.3 图片处理方案

**方案对比**：

| 方案 | 优点 | 缺点 | 推荐度 |
|------|------|------|--------|
| Base64嵌入JSON | 简单，单文件 | JSON文件大 | ⭐⭐⭐ |
| 上传到服务器 | 节省空间 | 需额外接口 | ⭐⭐⭐⭐⭐ |
| 保持相对路径 | 文件小 | 需打包images文件夹 | ⭐⭐ |

**当前实现**：前端支持Base64嵌入方案，题目内容中使用`[图片:img_0]`标记，前端自动渲染为图片。

### 16.3 导入模式

#### 16.3.1 现有习题册模式
- 选择已存在的习题册
- 题目自动关联到该习题册

#### 16.3.2 新建习题册/试卷模式
- 输入新习题册或试卷名称
- 选择类型：习题册(1) 或 试卷(2)
- 系统自动创建

#### 16.3.3 暂不选择模式
- 题目先导入到题库，不关联习题册
- 后续可在题目管理中手动关联

### 16.4 API 接口

**导入题目**: `POST /question/import`

**请求体**:
```json
{
  "bookId": 1,
  "newBookName": "2024真题",
  "newBookType": 1,
  "subjectIds": [1, 2],
  "checkDuplicate": true,
  "questions": [...]
}
```

### 16.5 相关文件清单

**前端文件**:
- `KaoYanPlatform-front/src/views/admin/QuestionImport.vue`
- `KaoYanPlatform-front/src/api/questionImportExport.js`

**后端文件**:
- `KaoYanPlatform-back/src/main/java/org/example/kaoyanplatform/controller/QuestionController.java`
- `KaoYanPlatform-back/src/main/java/org/example/kaoyanplatform/service/QuestionService.java`
- `KaoYanPlatform-back/src/main/java/org/example/kaoyanplatform/entity/dto/QuestionImportDTO.java`

---

**文档更新日期**: 2026-01-17
**更新内容**: 新增第16章 - 题目批量导入功能文档
