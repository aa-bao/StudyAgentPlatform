# 考研平台项目结构文档

## 项目概览

考研学习平台是一个基于 Spring Boot + Vue 3 的在线学习系统，为考研学生提供科目管理、题目练习、模拟考试等功能。

### 技术栈

#### 后端
- **框架**: Spring Boot 3.x
- **数据库**: MySQL 8.0
- **ORM**: MyBatis-Plus 3.x
- **构建工具**: Maven
- **JDK**: JDK 17+

#### 前端
- **框架**: Vue 3 + Composition API
- **UI组件**: Element Plus
- **状态管理**: Pinia
- **构建工具**: Vite
- **语言**: JavaScript

## 项目结构

```
KaoYanPlatform/
├── KaoYanPlatform-back/          # 后端项目
│   ├── src/
│   │   └── main/
│   │       ├── java/org/example/kaoyanplatform/
│   │       │   ├── controller/         # 控制器层
│   │       │   │   ├── AuthController.java
│   │       │   │   ├── PaperController.java
│   │       │   │   ├── QuestionController.java
│   │       │   │   ├── SubjectController.java
│   │       │   │   └── UserController.java
│   │       │   ├── service/            # 业务逻辑层
│   │       │   │   ├── SubjectService.java
│   │       │   │   └── impl/
│   │       │   │       └── SubjectServiceImpl.java
│   │       │   ├── mapper/             # 数据访问层
│   │       │   │   ├── MapSubjectWeightMapper.java
│   │       │   │   ├── SubjectMapper.java
│   │       │   │   └── ...
│   │       │   ├── entity/             # 实体类
│   │       │   │   ├── MapSubjectWeight.java
│   │       │   │   ├── Subject.java
│   │       │   │   ├── User.java
│   │       │   │   ├── Question.java
│   │       │   │   ├── Paper.java
│   │       │   │   └── dto/
│   │       │   │       └── SubjectDTO.java
│   │       │   ├── config/             # 配置类
│   │       │   │   └── MybatisPlusConfig.java
│   │       │   ├── common/             # 公共类
│   │       │   │   ├── Result.java      # 统一响应结果
│   │       │   │   └── ResultCode.java  # 响应码枚举
│   │       │   ├── constant/           # 常量定义
│   │       │   │   └── SubjectLevelConstants.java
│   │       │   └── KaoYanPlatformApplication.java
│   │       └── resources/
│   │           ├── application.yml     # 配置文件
│   │           └── mapper/             # MyBatis XML映射文件
│   └── pom.xml
│
├── KaoYanPlatform-front/         # 前端项目
│   ├── public/
│   ├── src/
│   │   ├── api/                    # API接口
│   │   │   ├── subject.js
│   │   │   ├── auth.js
│   │   │   └── ...
│   │   ├── assets/                 # 静态资源
│   │   │   ├── images/
│   │   │   └── styles/
│   │   ├── components/             # 公共组件
│   │   ├── views/                  # 页面组件
│   │   │   ├── Home.vue
│   │   │   ├── Login.vue
│   │   │   ├── layout/
│   │   │   │   ├── UserLayout.vue
│   │   │   │   └── ...
│   │   │   └── quiz/
│   │   │       └── TopicDrill.vue
│   │   ├── router/                 # 路由配置
│   │   ├── store/                  # 状态管理
│   │   ├── utils/                  # 工具函数
│   │   ├── App.vue
│   │   └── main.js
│   ├── package.json
│   └── vite.config.js
│
├── docs/                          # 项目文档
│   └── database/
│       ├── database-schema.md      # 数据库设计总览
│       └── map_subject_weight.md   # 科目权重映射设计
│
├── map_subject_weight.sql         # 权重映射表SQL（完整）
├── insert_math_weights.sql        # 数学权重插入SQL
├── insert_408_weights.sql         # 408权重插入SQL
├── tb_subject.sql                 # 科目表结构和数据
└── PROJECT_README.md              # 项目说明文档
```

## 核心模块说明

### 1. 科目管理模块

#### 数据库表
- `tb_subject`: 科目主表，存储科目的层级结构
- `map_subject_weight`: 科目权重映射表，实现不同考试规格下的权重差异化

#### 层级结构
```
Level 1 (考试大类)
├── 政治 (id=1)
├── 英语一 (id=2)
├── 英语二 (id=3)
├── 数学一 (id=4)
├── 数学二 (id=5)
├── 数学三 (id=6)
└── 408 (id=7)

Level 2 (考试规格/题型)
├── 英语题型：完形填空、阅读理解、新题型、翻译、小作文、大作文
├── 数学科目：高等数学、线性代数、概率论与数理统计
├── 408科目：数据结构、计算机组成原理、操作系统、计算机网络
└── 政治科目：马原、毛中特、史纲、思修、时政

Level 3+ (知识点/章节)
└── 具体的知识点和题型细分
```

#### 后端实现
- **Entity**: `Subject.java`, `MapSubjectWeight.java`
- **DTO**: `SubjectDTO.java`
- **Mapper**: `SubjectMapper.java`, `MapSubjectWeightMapper.java`
- **Service**: `SubjectService.java`, `SubjectServiceImpl.java`
- **Controller**: `SubjectController.java`

#### 前端实现
- **页面**: `TopicDrill.vue` (知识点专项训练)
- **API**: `api/subject.js`
- **路由**: `/quiz/topic-drill`

### 2. 用户管理模块

#### 数据库表
- `tb_user`: 用户信息表
- `user_progress`: 用户学习进度表

#### 功能
- 用户注册/登录
- 个人信息管理
- 选择报考科目
- 记录学习进度

### 3. 题目管理模块

#### 数据库表
- `tb_question`: 题目主表
- `map_question_subject`: 题目-科目关联表
- `map_question_book`: 题目-书籍关联表

#### 功能
- 题目录入
- 题目分类
- 题目搜索
- 题目收藏

### 4. 试卷与考试模块

#### 数据库表
- `tb_paper`: 试卷表
- `map_paper_question`: 试卷-题目关联表
- `exam_session`: 考试会话表
- `exam_record`: 考试记录表
- `exam_answer_detail`: 答题详情表

#### 功能
- 试卷组卷
- 模拟考试
- 倒计时功能
- 自动判分
- 答题记录

### 5. 错题本模块

#### 数据库表
- `tb_mistake_record`: 错题记录表

#### 功能
- 自动记录错题
- 错题分类
- 错题重做
- 掌握度标记

## 数据库设计要点

### 映射表命名规范
所有映射表使用 `map_` 前缀，格式为 `map_源_目标`：

- `map_subject_weight`: 科目-考试规格权重映射
- `map_question_subject`: 题目-科目关联
- `map_question_book`: 题目-书籍关联
- `map_paper_question`: 试卷-题目关联
- `map_subject_book`: 科目-书籍关联

### 科目权重设计

#### 问题背景
不同考试规格下，相同科目名称但权重不同：
- 英语一的大作文权重为 20%
- 英语二的大作文权重为 15%

#### 解决方案
使用 `map_subject_weight` 表建立科目与考试规格的多对多权重关系，实现动态权重查询。

#### 实现逻辑
1. 后端查询考试规格时，关联查询 `map_subject_weight` 表
2. 将权重数据填充到 `dynamicWeight` 字段（非数据库字段）
3. 前端通过 `dynamicWeight` 显示权重

### scope 字段设计
`scope` 字段用于标识科目适用的考试规格，格式为逗号分隔的ID列表：
- `"2,3"` 表示适用于英语一(id=2)和英语二(id=3)
- `"4,5,6"` 表示适用于数学一、二、三
- `"4,6"` 表示适用于数学一和数学三（数学二不考概率论）

## API 设计规范

### 统一响应格式
```json
{
  "code": 200,
  "message": "success",
  "data": {...}
}
```

### 科目相关 API
- `GET /api/subject/exam-specs`: 获取所有考试规格
- `GET /api/subject/tree/{examSpecId}`: 获取指定考试规格的科目树
- `GET /api/subject/weight/{examSpecId}`: 获取指定考试规格的权重配置

## 前端路由设计

### 主要路由
- `/login`: 登录页
- `/register`: 注册页
- `/home`: 首页
- `/quiz/topic-drill`: 知识点专项训练
- `/exam/mock`: 模拟考试
- `/mistake-book`: 错题本
- `/profile`: 个人中心

## 开发规范

### 代码规范
- 后端遵循阿里巴巴Java开发规范
- 前端遵循Vue 3官方风格指南
- 所有公共方法添加JSDoc注释
- 数据库表和字段添加注释

### Git提交规范
- `feat`: 新功能
- `fix`: 修复bug
- `docs`: 文档更新
- `style`: 代码格式调整
- `refactor`: 重构
- `test`: 测试相关
- `chore`: 构建/工具链更新

## 部署说明

### 后端部署
```bash
cd KaoYanPlatform-back
mvn clean package
java -jar target/KaoYanPlatform-back.jar
```

### 前端部署
```bash
cd KaoYanPlatform-front
npm install
npm run build
# 部署 dist/ 目录到静态服务器
```

## 更新日志

### 2026-01-12
- ✅ 实现科目权重映射功能
- ✅ 支持英语一/二、数学一/二/三、408的动态权重
- ✅ 删除 `tb_subject.weight` 字段，统一使用 `dynamicWeight`
- ✅ 完善项目文档和数据库设计文档

### 待完成
- [ ] 政治科目权重配置
- [ ] 专业课权重支持
- [ ] 用户学习进度统计优化
- [ ] 题目推荐算法

## 相关文档
- [数据库设计总览](./database/database-schema.md)
- [科目权重映射设计](./database/map_subject_weight.md)
- [项目README](../PROJECT_README.md)
