# 📋 项目结构重构 - 执行总结

> ✅ 准备工作已完成，等待执行

---

## 🎯 重构目标

| 原目录名 | 新目录名 |
|---------|---------|
| `KaoYanPlatform-back` | `java-back` |
| `KaoYanPlatform-front` | `vue-front` |
| `ai_service` | `python-back` |
| `ExtractQuestionFromPDF` | `python-back/tools/pdf-extractor` |

---

## ✅ 已完成的工作

### 1. Git备份 ✅

已创建备份提交：`backup: 重构前完整备份`
- 包含所有当前修改
- 包含所有新增文件
- 可随时恢复

### 2. 重构脚本 ✅

已创建自动化脚本：
- 📄 `scripts/refactor-structure.bat` - Windows批处理脚本
- 📄 `scripts/refactor-structure.sh` - Linux/Mac Bash脚本
- 📄 `scripts/重构操作指南.md` - 详细操作指南
- 📄 `scripts/配置更新清单.md` - 配置更新说明

---

## 🚀 执行步骤

### 方式A：自动化脚本（推荐）

#### Windows用户

```cmd
# 1. 关闭所有IDE（VSCode、IntelliJ等）

# 2. 打开CMD或PowerShell，进入项目根目录
cd F:\Coding\JavaProject\KaoYanPlatform

# 3. 运行重构脚本
scripts\refactor-structure.bat
```

#### Linux/Mac用户

```bash
# 1. 关闭所有IDE

# 2. 进入项目根目录
cd /path/to/KaoYanPlatform

# 3. 添加执行权限并运行
chmod +x scripts/refactor-structure.sh
./scripts/refactor-structure.sh
```

---

### 方式B：手动执行

如果脚本无法运行，按照以下步骤手动操作：

#### 步骤1：关闭所有IDE

**必须关闭：**
- VSCode
- IntelliJ IDEA
- PyCharm
- 任何终端/命令行窗口

#### 步骤2：重命名目录

```cmd
# 在文件资源管理器中：
# 1. 找到 KaoYanPlatform-back，重命名为 java-back
# 2. 找到 KaoYanPlatform-front，重命名为 vue-front
# 3. 找到 ai_service，重命名为 python-back
```

#### 步骤3：移动PDF工具

```cmd
# 1. 在 python-back 中创建 tools 文件夹
# 2. 将 ExtractQuestionFromPDF 移动到 python-back/tools/
# 3. 重命名为 pdf-extractor
```

详细步骤请查看：[重构操作指南.md](重构操作指南.md)

---

## 📝 重构后需要做的事

### 1. 验证目录结构

```bash
git status
```

应该看到：
```
renamed: KaoYanPlatform-back -> java-back
renamed: KaoYanPlatform-front -> vue-front
renamed: ai_service -> python-back
renamed: ExtractQuestionFromPDF -> python-back/tools/pdf-extractor
```

### 2. 提交Git更改

```bash
git add -A
git commit -m "refactor: 重构项目目录结构

- KaoYanPlatform-back → java-back
- KaoYanPlatform-front → vue-front
- ai_service → python-back
- ExtractQuestionFromPDF → python-back/tools/pdf-extractor
- 统一使用 kebab-case 命名风格"
```

### 3. 更新配置文件（可选）

参考：[配置更新清单.md](配置更新清单.md)

需要更新的文件：
- `vue-front/package.json`
- `java-back/pom.xml`
- 文档文件

### 4. 测试服务启动

```bash
# 测试Java后端
cd java-back
mvn clean install
mvn spring-boot:run

# 测试Vue前端
cd vue-front
npm install
npm run dev

# 测试Python后端
cd python-back
pip install -r requirements.txt
uvicorn main:app --reload
```

---

## 🎯 重构后的目录结构

```
KaoYanPlatform/                          # 项目根目录
│
├── java-back/                           # Java后端服务
│   ├── src/
│   ├── pom.xml
│   └── uploads/
│
├── vue-front/                           # Vue前端服务
│   ├── src/
│   ├── public/
│   └── package.json
│
├── python-back/                         # Python AI服务
│   ├── app/
│   ├── tools/
│   │   └── pdf-extractor/              # PDF提取工具（离线）
│   │       ├── extract_json.py
│   │       ├── glm_basic_extract.py
│   │       └── README.md
│   ├── config/
│   ├── docs/
│   ├── requirements.txt
│   └── main.py
│
├── data/                                # 数据目录
│   ├── raw/                            # 原始数据
│   ├── processed/                      # 处理后数据
│   └── temp/                           # 临时数据
│
├── docs/                                # 项目文档
│   ├── PROJECT_README.md
│   ├── 项目结构重构方案讨论.md
│   └── ...
│
└── scripts/                             # 脚本集合
    ├── refactor-structure.bat          # 重构脚本
    ├── refactor-structure.sh
    ├── 重构操作指南.md
    └── 配置更新清单.md
```

---

## ⚠️ 注意事项

### 1. IDE配置

重构后需要重新导入项目：

**IntelliJ IDEA (Java)**
- File → Open → 选择 `java-back` 目录

**VSCode (Vue/Python)**
- File → Open Folder → 选择 `vue-front` 或 `python-back`

**PyCharm (Python)**
- File → Open → 选择 `python-back` 目录

### 2. Git历史

Git会自动识别重命名操作，历史记录会保留。

### 3. 相对路径

如果项目中有硬编码的相对路径，需要手动更新。

---

## 🔄 回滚方案

如果重构出现问题，可以随时恢复：

```bash
# 查看提交历史
git log --oneline

# 恢复到重构前状态
git reset --hard HEAD~1

# 或恢复到指定提交
git reset --hard <commit-hash>
```

---

## 📞 遇到问题？

### 问题1：权限拒绝

**解决：** 关闭所有IDE和编辑器，确保文件未被占用

### 问题2：Git未识别重命名

**解决：** 执行 `git add -A` 让Git自动识别

### 问题3：配置文件路径错误

**解决：** 参考 `配置更新清单.md` 更新配置

---

## ✅ 准备就绪！

现在你可以：

1. **立即执行重构** - 运行 `scripts/refactor-structure.bat`
2. **查看详细指南** - 阅读 `scripts/重构操作指南.md`
3. **手动执行** - 按照指南逐步操作

**选择你喜欢的方式开始重构吧！** 🚀
