# 考研平台数据库设计文档

## 数据库概览

本项目采用关系型数据库 MySQL，存储考研学习平台的所有数据，包括用户、科目、题目、试卷、学习进度等信息。

## 核心表结构

### 1. 用户相关

#### tb_user (用户表)
存储用户基本信息和认证信息。

```sql
CREATE TABLE `tb_user` (
  `id` int NOT NULL AUTO_INCREMENT,
  `username` varchar(50) NOT NULL COMMENT '用户名',
  `password` varchar(100) NOT NULL COMMENT '密码（加密）',
  `nickname` varchar(50) NULL COMMENT '昵称',
  `email` varchar(100) NULL COMMENT '邮箱',
  `phone` varchar(20) NULL COMMENT '手机号',
  `avatar` varchar(255) NULL COMMENT '头像URL',
  `exam_subjects` varchar(200) NULL COMMENT '报考科目（逗号分隔）',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB COMMENT='用户表';
```

#### user_progress (用户学习进度表)
记录用户在各科目下的学习进度。

```sql
CREATE TABLE `user_progress` (
  `id` int NOT NULL AUTO_INCREMENT,
  `user_id` int NOT NULL COMMENT '用户ID',
  `subject_id` int NOT NULL COMMENT '科目ID',
  `finished_count` int NULL DEFAULT 0 COMMENT '已完成题目数',
  `total_count` int NULL DEFAULT 0 COMMENT '总题目数',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_user_subject` (`user_id`, `subject_id`)
) ENGINE=InnoDB COMMENT='用户学习进度表';
```

### 2. 科目相关

#### tb_subject (科目表)
存储科目的层级结构信息。

```sql
CREATE TABLE `tb_subject` (
  `id` int NOT NULL AUTO_INCREMENT,
  `name` varchar(50) NOT NULL COMMENT '科目名称',
  `parent_id` int NULL DEFAULT 0 COMMENT '父级ID（顶级为0）',
  `icon` varchar(100) NULL COMMENT '图标',
  `sort` tinyint NULL DEFAULT 0 COMMENT '排序号',
  `level` tinyint NULL DEFAULT 1 COMMENT '层级: 1-考试大类, 2-考试规格, 3-具体学科, 4-知识点, 5-题型',
  `question_count` int NULL DEFAULT 0 COMMENT '该分类下的题目总数',
  `scope` varchar(50) NULL DEFAULT '1,2,3' COMMENT '适用大纲: 1-数一, 2-数二, 3-数三',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB COMMENT='科目分类表';
```

**层级说明：**
- **Level 1**: 考试大类（CATEGORY）- 如：政治(id=1)、英语一(id=2)、英语二(id=3)、数学一(id=4)、数学二(id=5)、数学三(id=6)、408(id=7)
- **Level 2**: 考试规格（EXAM_SPEC）- 如：马原、毛中特、完形填空、高等数学
- **Level 3**: 具体学科（SUBJECT）- 如：函数与极限、行列式
- **Level 4**: 知识点/章节（KNOWLEDGE_POINT）- 如：数列敛散性判定
- **Level 5**: 题型/解题方法（QUESTION_TYPE）- 如：泰勒公式

**重要说明：**
- 原 `weight` 字段已删除，统一使用 `map_subject_weight` 表实现动态权重
- 权重数据通过查询 `map_subject_weight` 表动态获取，支持不同考试规格下的权重差异化

#### map_subject_weight (科目权重映射表)
解决不同考试规格下相同科目名称但权重不同的问题。

详见：[map_subject_weight.md](./map_subject_weight.md)

```sql
CREATE TABLE `map_subject_weight` (
  `id` int NOT NULL AUTO_INCREMENT,
  `subject_id` int NOT NULL COMMENT '科目ID',
  `exam_spec_id` int NOT NULL COMMENT '考试规格ID',
  `weight` float NULL DEFAULT NULL COMMENT '权重百分比',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_subject_exam` (`subject_id`, `exam_spec_id`)
) ENGINE=InnoDB COMMENT='科目权重映射表';
```

#### map_subject_book (科目-书籍关联表)
关联科目与参考书籍。

```sql
CREATE TABLE `map_subject_book` (
  `id` int NOT NULL AUTO_INCREMENT,
  `subject_id` int NOT NULL COMMENT '科目ID',
  `book_id` int NOT NULL COMMENT '书籍ID',
  PRIMARY KEY (`id`),
  KEY `idx_subject` (`subject_id`),
  KEY `idx_book` (`book_id`)
) ENGINE=InnoDB COMMENT='科目-书籍关联表';
```

### 3. 题目相关

#### tb_question (题目表)
存储题目信息。

```sql
CREATE TABLE `tb_question` (
  `id` int NOT NULL AUTO_INCREMENT,
  `content` text NOT NULL COMMENT '题目内容',
  `type` varchar(20) NULL COMMENT '题型：单选、多选、填空、解答',
  `difficulty` tinyint NULL DEFAULT 1 COMMENT '难度：1-简单, 2-中等, 3-困难',
  `answer` text NULL COMMENT '答案',
  `analysis` text NULL COMMENT '解析',
  `source` varchar(100) NULL COMMENT '来源',
  `year` int NULL COMMENT '年份',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB COMMENT='题目表';
```

#### map_question_subject (题目-科目关联表)
管理题目与科目的多对多关系。

```sql
CREATE TABLE `map_question_subject` (
  `id` int NOT NULL AUTO_INCREMENT,
  `question_id` int NOT NULL COMMENT '题目ID',
  `subject_id` int NOT NULL COMMENT '科目ID',
  PRIMARY KEY (`id`),
  KEY `idx_question` (`question_id`),
  KEY `idx_subject` (`subject_id`)
) ENGINE=InnoDB COMMENT='题目-科目关联表';
```

#### map_question_book (题目-书籍关联表)
关联题目与参考书籍。

```sql
CREATE TABLE `map_question_book` (
  `id` int NOT NULL AUTO_INCREMENT,
  `question_id` int NOT NULL COMMENT '题目ID',
  `book_id` int NOT NULL COMMENT '书籍ID',
  PRIMARY KEY (`id`),
  KEY `idx_question` (`question_id`),
  KEY `idx_book` (`book_id`)
) ENGINE=InnoDB COMMENT='题目-书籍关联表';
```

### 4. 试卷相关

#### tb_paper (试卷表)
存储试卷信息。

```sql
CREATE TABLE `tb_paper` (
  `id` int NOT NULL AUTO_INCREMENT,
  `name` varchar(100) NOT NULL COMMENT '试卷名称',
  `type` varchar(20) NULL COMMENT '试卷类型：真题、模拟题、练习',
  `year` int NULL COMMENT '年份',
  `exam_spec_id` int NULL COMMENT '考试规格ID',
  `duration` int NULL DEFAULT 120 COMMENT '考试时长（分钟）',
  `total_score` int NULL DEFAULT 100 COMMENT '总分',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB COMMENT='试卷表';
```

#### map_paper_question (试卷-题目关联表)
管理试卷包含的题目及分值。

```sql
CREATE TABLE `map_paper_question` (
  `id` int NOT NULL AUTO_INCREMENT,
  `paper_id` int NOT NULL COMMENT '试卷ID',
  `question_id` int NOT NULL COMMENT '题目ID',
  `score` float NULL DEFAULT 0 COMMENT '分值',
  `sort` int NULL DEFAULT 0 COMMENT '排序',
  PRIMARY KEY (`id`),
  KEY `idx_paper` (`paper_id`),
  KEY `idx_question` (`question_id`)
) ENGINE=InnoDB COMMENT='试卷-题目关联表';
```

### 5. 考试记录相关

#### exam_session (考试会话表)
记录用户的考试会话。

```sql
CREATE TABLE `exam_session` (
  `id` int NOT NULL AUTO_INCREMENT,
  `user_id` int NOT NULL COMMENT '用户ID',
  `paper_id` int NOT NULL COMMENT '试卷ID',
  `start_time` datetime NULL COMMENT '开始时间',
  `end_time` datetime NULL COMMENT '结束时间',
  `status` varchar(20) NULL COMMENT '状态：进行中、已完成、已放弃',
  `score` float NULL COMMENT '得分',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_user` (`user_id`),
  KEY `idx_paper` (`paper_id`)
) ENGINE=InnoDB COMMENT='考试会话表';
```

#### exam_record (考试记录表)
记录用户的考试历史。

```sql
CREATE TABLE `exam_record` (
  `id` int NOT NULL AUTO_INCREMENT,
  `user_id` int NOT NULL COMMENT '用户ID',
  `paper_id` int NOT NULL COMMENT '试卷ID',
  `score` float NULL COMMENT '得分',
  `total_score` float NULL COMMENT '总分',
  `duration` int NULL COMMENT '用时（秒）',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_user` (`user_id`)
) ENGINE=InnoDB COMMENT='考试记录表';
```

#### exam_answer_detail (答题详情表)
记录用户在考试中的答题详情。

```sql
CREATE TABLE `exam_answer_detail` (
  `id` int NOT NULL AUTO_INCREMENT,
  `exam_session_id` int NOT NULL COMMENT '考试会话ID',
  `question_id` int NOT NULL COMMENT '题目ID',
  `user_answer` text NULL COMMENT '用户答案',
  `is_correct` tinyint NULL COMMENT '是否正确',
  `score` float NULL COMMENT '得分',
  `time_spent` int NULL COMMENT '用时（秒）',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_session` (`exam_session_id`),
  KEY `idx_question` (`question_id`)
) ENGINE=InnoDB COMMENT='答题详情表';
```

### 6. 其他

#### tb_book (书籍表)
存储参考书籍信息。

```sql
CREATE TABLE `tb_book` (
  `id` int NOT NULL AUTO_INCREMENT,
  `name` varchar(100) NOT NULL COMMENT '书名',
  `author` varchar(50) NULL COMMENT '作者',
  `publisher` varchar(100) NULL COMMENT '出版社',
  `isbn` varchar(20) NULL COMMENT 'ISBN',
  `cover_url` varchar(255) NULL COMMENT '封面图片',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB COMMENT='书籍表';
```

#### tb_collection (收藏表)
记录用户收藏的内容。

```sql
CREATE TABLE `tb_collection` (
  `id` int NOT NULL AUTO_INCREMENT,
  `user_id` int NOT NULL COMMENT '用户ID',
  `item_type` varchar(20) NULL COMMENT '收藏类型：question、subject',
  `item_id` int NOT NULL COMMENT '收藏项ID',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_user` (`user_id`),
  KEY `idx_item` (`item_type`, `item_id`)
) ENGINE=InnoDB COMMENT='收藏表';
```

#### tb_mistake_record (错题本表)
记录用户的错题。

```sql
CREATE TABLE `tb_mistake_record` (
  `id` int NOT NULL AUTO_INCREMENT,
  `user_id` int NOT NULL COMMENT '用户ID',
  `question_id` int NOT NULL COMMENT '题目ID',
  `mistake_count` int NULL DEFAULT 1 COMMENT '错误次数',
  `last_mistake_time` datetime NULL COMMENT '最后一次错误时间',
  `is_mastered` tinyint NULL DEFAULT 0 COMMENT '是否已掌握',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_user_question` (`user_id`, `question_id`)
) ENGINE=InnoDB COMMENT='错题本表';
```

## 表关系图

```
tb_user (用户)
  ├── user_progress (学习进度)
  │   └── tb_subject (科目)
  ├── exam_session (考试会话)
  │   ├── tb_paper (试卷)
  │   │   ├── map_paper_question
  │   │   │   └── tb_question (题目)
  │   │   └── tb_subject (科目)
  │   └── exam_answer_detail (答题详情)
  │       └── tb_question (题目)
  ├── exam_record (考试记录)
  │   └── tb_paper (试卷)
  ├── tb_collection (收藏)
  └── tb_mistake_record (错题本)
      └── tb_question (题目)

tb_subject (科目)
  ├── map_subject_weight (权重映射)
  ├── map_subject_book (科目-书籍)
  │   └── tb_book (书籍)
  └── map_question_subject (题目-科目)
      └── tb_question (题目)
          ├── map_question_book (题目-书籍)
          │   └── tb_book (书籍)
          └── map_paper_question (试卷-题目)
              └── tb_paper (试卷)
```

## 索引设计

### 主要索引
1. **用户相关**: `user_id`, `username`, `email`
2. **科目相关**: `parent_id`, `level`, `scope`
3. **题目相关**: `type`, `difficulty`, `year`
4. **考试相关**: `user_id`, `paper_id`, `status`

### 联合索引
1. **学习进度**: `(user_id, subject_id)`
2. **题目-科目**: `(question_id, subject_id)`
3. **试卷-题目**: `(paper_id, question_id)`
4. **权重映射**: `(subject_id, exam_spec_id)` UNIQUE

## 数据字典

详见各个模块的详细文档：
- [科目权重映射](./map_subject_weight.md)
- [用户模块](./module-user.md) (待补充)
- [题目模块](./module-question.md) (待补充)
- [考试模块](./module-exam.md) (待补充)

## 维护建议

1. **备份策略**: 每日自动备份，保留30天
2. **索引优化**: 定期分析慢查询，优化索引
3. **数据归档**: 考试记录按年度归档
4. **监控告警**: 监控数据库连接数、慢查询、磁盘使用率

## 更新日志

- **2026-01-12**: 添加科目权重映射表 `map_subject_weight`
- **2026-01-10**: 初始版本，建立核心表结构
