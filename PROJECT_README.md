# 项目背景与技术文档

> **文档说明**: 本文档提供 **KaoYanPlatform** (考研刷题平台) 的完整技术架构说明，涵盖前端 (`kaoyan-frontend`) 和后端 (`KaoYanPlatform`) 的全栈上下文，供 AI 和开发者快速理解项目。

---

## 📋 目录

1. [快速开始](#1-快速开始)
2. [项目概览](#2-项目概览)
3. [技术栈](#3-技术栈)
4. [项目结构](#4-项目结构)
5. [数据模型设计](#5-数据模型设计)
6. [核心功能模块](#6-核心功能模块)
7. [API 接口文档](#7-api-接口文档)
8. [前端架构](#8-前端架构)
9. [核心实现细节](#9-核心实现细节)
10. [开发规范](#10-开发规范)
11. [已知问题与优化建议](#11-已知问题与优化建议)

---

## 1. 快速开始

### 1.1 环境要求

- **JDK**: 17+
- **Maven**: 3.6+
- **MySQL**: 8.0+
- **Node.js**: 16+
- **IDE**: IntelliJ IDEA / VS Code

### 1.2 数据库初始化

```sql
-- 创建数据库
CREATE DATABASE kaoyan_platform CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;

-- 导入表结构和初始数据
USE kaoyan_platform;
SOURCE /path/to/schema.sql;
```

### 1.3 后端启动

```bash
# 进入后端目录
cd KaoYanPlatform

# 修改配置文件
# 编辑 src/main/resources/application.yml，配置数据库连接

# 启动项目
mvn spring-boot:run

# 或者直接运行主类
# KaoYanPlatform/src/main/java/org/example/kaoyanplatform/KaoyanplatformApplication.java
```

后端服务启动后访问：
- **API 地址**: http://localhost:8081
- **Swagger 文档**: http://localhost:8081/doc.html

### 1.4 前端启动

```bash
# 进入前端目录
cd kaoyan-frontend

# 安装依赖
npm install

# 启动开发服务器
npm run dev

# 构建生产版本
npm run build
```

前端服务启动后访问：http://localhost:5173

### 1.5 默认账号

| 角色 | 用户名 | 密码 |
|------|--------|------|
| 管理员 | admin | admin123 |
| 普通用户 | student | 123456 |

---

## 2. 项目概览

### 2.1 项目定位

**KaoYanPlatform** 是一个面向考研学生的在线刷题与学习管理平台，支持：

- **多模式刷题**: 逐题精练、专项突破、真题模考
- **智能错题管理**: 自动记录错题、分类归档、统计分析
- **学习进度追踪**: 实时统计学习数据、可视化展示
- **后台管理系统**: 题目/科目/用户/资源的全面管理

### 2.2 核心业务流程

```
用户端流程:
登录 → 仪表盘 → 选择科目 → 选择刷题模式
 → 逐题精练/专项突破/真题模考
 → 提交答案 → 查看解析 → 加入错题本/收藏
 → 学习统计与进度追踪

管理员流程:
登录 → 管理后台 → 题目管理/科目管理/用户管理/错题监控
 → 数据统计与系统维护
```

### 2.3 产品特色

- **三种刷题模式**: 随心刷 | 精准练 | 整卷测
- **LaTeX 公式支持**: 完美支持数学公式渲染
- **画板草稿**: HTML5 Canvas 实现的在线草稿功能
- **多对多关系**: 灵活的题目-书本-科目关联体系
- **可视化监控**: ECharts 雷达图、热力图等数据可视化

### 2.4 更新历史

#### 2026-01-07: 完善 Swagger API 文档注解 📚

完成所有 Controller 接口的 Swagger 注解完善，提供详细的接口文档用于测试和调试。

**完善内容**:
- ✅ AdminController: 管理员统计、错题热力图、热门错题
- ✅ BookController: 习题册 CRUD、分页查询、科目关联
- ✅ CollectionController: 收藏管理、标签管理
- ✅ FileController: 文件上传
- ✅ QuestionController: 题目 CRUD、错题本、分页查询
- ✅ RecordController: 答题提交、统计查询、最近动态
- ✅ SubjectController: 科目树、层级管理、拖拽排序
- ✅ UserController: 用户认证、资料管理、头像上传、学习统计

**注解特性**:
- 详细的接口描述（`@Operation`）
- 参数说明（`@Parameter`）包含是否必填、示例值
- 请求体说明（`@RequestBody`）包含字段说明
- 返回数据结构说明
- 支持 Swagger UI 在线测试

访问地址: http://localhost:8081/doc.html

#### 2026-01-06: 科目层级结构重构为 4 级体系 🎯

完成科目层级体系的重大重构，简化层级结构并引入 scope 字段实现多对多关系映射。

**核心变更**:
- ✅ 简化为 4 层级结构：Level 1(考试规格) → Level 2(具体学科) → Level 3(知识点) → Level 4(题型)
- ✅ 移除原 Level 1(CATEGORY) 考试大类层级，避免层级混乱
- ✅ 引入 scope 字段实现科目与考试规格的多对多关系（如：线性代数属于数一、数三，但不属于数二）
- ✅ 创建虚拟分组节点（英语、数学）优化前端展示
- ✅ 更新 `SubjectLevelConstants.java` 常量定义
- ✅ 更新 `SubjectServiceImpl.java` 的 `getManageTree()` 方法
- ✅ 更新前端 `BookManage.vue` 的级联选择器过滤逻辑
- ✅ 更新前端 `SubjectManage.vue` 的层级选择器（4 级选项）

**技术亮点**:
- 后端自动创建虚拟分组节点（id=-2 英语、id=-3 数学）
- 前端动态过滤科目树，根据考试规格 ID 显示对应科目
- 支持 scope 为空时表示"适用于所有考试规格"

#### 2026-01-06: 核心管理功能实现完成 ✨

完成后台管理系统的核心功能模块，提供完整的题目、习题册、科目、用户、错题监控等管理能力。

**新增功能模块**:
- 题目管理模块（多对多关联、KaTeX 支持、批量操作）
- 习题册管理模块（多科目关联、sort 排序）
- 科目体系管理模块（树形结构、拖拽排序、scope 配置）
- 用户管理与学习监控模块（ECharts 雷达图、学习统计）
- 错题监控模块（热力统计、高频错题 TOP 20）

#### 2026-01-06: 数据库表命名规范统一

统一数据库表命名规范，所有表使用 `tb_` 前缀：
- ✅ 重命名 `sys_user` 表为 `tb_user`
- ✅ 更新 Java 实体类 `User.java` 的 `@TableName` 注解

#### 2026-01-06: 数据库映射表重构

将题目-书本-科目之间的直接外键关系改为使用映射表（`map_`前缀）管理：
- ✅ 从 `tb_question` 表删除 `book_id` 和 `subject_id` 外键字段
- ✅ 从 `tb_book` 表删除 `subject_id` 外键字段
- ✅ 新增映射表：`map_question_book`、`map_question_subject`、`map_subject_book`
- ✅ 支持多对多关系，提高数据灵活性

---

## 2. 技术栈

### 2.1 后端技术栈

| 技术 | 版本 | 用途 |
|------|------|------|
| Spring Boot | 3.5.9 | 核心框架 |
| MyBatis Plus | 3.5.5 | ORM 框架 |
| MySQL | 8.0 | 关系型数据库 |
| Knife4j | 4.5.0 | API 文档生成 (Swagger) |
| Spring Security | - | 安全框架 |
| Lombok | - | 简化 Java 代码 |
| Hutool | 5.8.25 | Java 工具库 |
| Jackson | - | JSON 处理 |

### 2.2 前端技术栈

| 技术 | 版本 | 用途 |
|------|------|------|
| Vue | 3.5 | 前端框架 (Composition API) |
| Vite | 7.3 | 构建工具 |
| Element Plus | 2.13 | UI 组件库 |
| Pinia | 3.0 | 状态管理 |
| Vue Router | 4.6 | 路由管理 |
| Axios | 1.13 | HTTP 客户端 |
| KaTeX | - | 数学公式渲染 |
| ECharts | 6.0 | 数据可视化 |

### 2.3 特色技术实现

- **HTML5 Canvas**: 画板草稿功能
- **Flex/Grid 布局**: 分屏刷题、错题本瀑布流
- **Glassmorphism**: 毛玻璃特效 UI
- **递归树处理**: 科目体系的层级管理
- **拖拽排序**: Element Plus el-tree 实现科目拖拽

---

## 3. 项目结构

### 3.1 后端结构

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
│   └── MybatisPlusConfig.java # MyBatis Plus 配置
│
├── controller/                # API 接口层
│   ├── AdminController.java   # 管理员后台（错题监控、标签统计）
│   ├── BookController.java    # 习题册管理（多科目关联）
│   ├── CollectionController.java # 收藏管理
│   ├── FileController.java    # 文件上传/下载
│   ├── QuestionController.java # 题目管理
│   ├── RecordController.java  # 考试记录
│   ├── SubjectController.java # 科目管理（树形、拖拽排序）
│   └── UserController.java    # 用户认证与信息管理
│
├── entity/                    # 数据库实体
│   ├── dto/                   # 数据传输对象
│   │   ├── SubjectDTO.java    # 科目树形结构 DTO
│   │   ├── BookDTO.java       # 习题册 DTO（含科目关联）
│   │   ├── UserStudyStatsDTO.java # 用户学习统计 DTO
│   │   └── MistakeHeatmapDTO.java # 错题热力统计 DTO
│   │
│   ├── Book.java              # tb_book 表实体
│   ├── ExamRecord.java        # tb_exam_record 表实体
│   ├── MapQuestionBook.java   # map_question_book 映射表实体
│   ├── MapQuestionSubject.java # map_question_subject 映射表实体
│   ├── MapSubjectBook.java    # map_subject_book 映射表实体
│   ├── Question.java          # tb_question 表实体
│   ├── Subject.java           # tb_subject 表实体
│   ├── User.java              # tb_user 表实体
│   └── MistakeRecord.java     # tb_mistake_record 表实体
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
│   └── MistakeRecordMapper.java
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
        └── MistakeRecordServiceImpl.java
```

### 3.2 前端结构

```
kaoyan-frontend/src/
│
├── api/                       # API 调用封装
│   └── user.js                # 用户相关 API
│
├── assets/                    # 静态资源
│   └── icons/                 # SVG 图标库
│
├── router/                    # 路由配置
│   └── index.js               # 路由定义与导航守卫
│
├── stores/                    # Pinia 状态管理
│   ├── user.js                # 用户会话状态
│   └── counter.js
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
│   │   └── MockExam.vue       # 真题模考
│   │
│   ├── layout/                # 布局容器
│   │   ├── AdminLayout.vue    # 管理员侧边栏 + 顶栏
│   │   └── UserLayout.vue     # 用户侧边栏（浅色主题）+ 顶栏
│   │
│   ├── Dashboard.vue          # 用户首页
│   ├── Login.vue              # 登录页
│   ├── CorrectionNotebook.vue # 错题本（瀑布流、分类导航）
│   ├── SubjectList.vue        # 科目选择列表
│   └── UserProfile.vue        # 个人资料
│
├── App.vue                    # 根组件
└── main.js                    # 入口文件（插件配置）
```

---

## 4. 数据模型设计

### 4.1 核心业务表

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

### 4.2 映射表（多对多关系）

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

### 4.3 映射表关系链

```
题目 → map_question_book → 习题册
题目 → map_question_subject → 科目/知识点
习题册 → map_subject_book → 科目/知识点
```

### 4.4 查询示例

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

## 5. 核心功能模块

### 5.1 题目管理模块 (Question Management)

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

### 5.4 用户管理与学习监控模块 (User Management)

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

## 7. API 接口文档

### 7.1 Swagger 文档访问

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

## 8. 前端架构

### 8.1 路由与权限

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

## 9. 核心实现细节

### 9.1 多对多关系管理

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

## 10. 开发规范

### 10.1 代码规范

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

## 11. 已知问题与优化建议

### 11.1 当前限制

1. **题目与科目关联**: 一道题只能关联一个科目（通过 `map_question_subject` 的 `LIMIT 1` 实现）
2. **文件存储**: 当前仅支持本地存储，未接入 OSS
3. **公告系统**: 未实现，需要新建 `tb_notice` 表
4. **资源管理**: 未实现，需要基于 `tb_resource` 表开发
5. **全局标签管理**: 收藏标签未规范化管理
6. **真题模考**: 套卷模式未完善
7. **科目层级限制**: 当前固定为 4 层级，未来如需扩展需修改 `SubjectLevelConstants.java`

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

### 11.3 扩展方向

1. **真题模考**: 实现套卷模式，倒计时、自动提交、成绩分析
2. **智能推荐**: 基于错题记录推荐薄弱知识点题目
3. **学习计划**: 自定义学习计划，每日打卡提醒
4. **社区讨论**: 题目评论区，支持提问和解答
5. **移动端适配**: 使用响应式布局或开发小程序版本
6. **数据导入导出**: Excel 批量导入题目、学习报告导出
7. **消息通知**: WebSocket 实时推送学习提醒

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

---

**文档版本**: v1.2
**最后更新**: 2026-01-07
**维护者**: AI Assistant

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
