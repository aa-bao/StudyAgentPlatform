<template>
    <div class="admin-container">
        <el-card shadow="never" class="import-card">
            <template #header>
                <div class="card-header">
                    <div class="text-header">
                        <span class="title-text">JSON 文件批量导入题目</span>
                        <div class="header-desc">上传 JSON 文件批量导入题目到题库</div>
                    </div>
                </div>
            </template>

            <!-- 导入表单 -->
            <el-form :model="importForm" label-width="120px" class="import-form" v-loading="importing">
                <!-- 习题册选择 -->
                <el-form-item label="习题册/试卷">
                    <el-radio-group v-model="importForm.bookMode" @change="handleBookModeChange">
                        <el-radio value="existing">选择现有习题册</el-radio>
                        <el-radio value="new">新建习题册/试卷</el-radio>
                        <el-radio value="skip">暂不选择</el-radio>
                    </el-radio-group>
                </el-form-item>

                <!-- 现有习题册 -->
                <el-form-item v-if="importForm.bookMode === 'existing'" label="选择习题册">
                    <el-select
                        v-model="importForm.bookId"
                        placeholder="请选择习题册"
                        style="width: 100%"
                        filterable
                    >
                        <el-option
                            v-for="book in allBooks"
                            :key="book.id"
                            :label="book.name"
                            :value="book.id"
                        />
                    </el-select>
                </el-form-item>

                <!-- 新建习题册 -->
                <el-form-item v-if="importForm.bookMode === 'new'" label="习题册/试卷名称">
                    <el-input
                        v-model="importForm.newBookName"
                        placeholder="请输入习题册或试卷名称"
                        clearable
                    />
                </el-form-item>

                <el-form-item v-if="importForm.bookMode === 'new'" label="类型">
                    <el-radio-group v-model="importForm.newBookType">
                        <el-radio :value="1">习题册</el-radio>
                        <el-radio :value="2">试卷</el-radio>
                    </el-radio-group>
                </el-form-item>

                <!-- 科目选择 -->
                <el-form-item label="选择科目" required>
                    <el-tree-select
                        v-model="importForm.subjectIds"
                        :data="subjectTree"
                        :props="{ label: 'name', value: 'id', children: 'children' }"
                        multiple
                        collapse-tags
                        collapse-tags-tooltip
                        clearable
                        placeholder="请选择科目（至少选择一个，可多选）"
                        check-strictly
                        filterable
                        style="width: 100%"
                        :render-after-expand="false"
                    >
                        <template #default="{ node, data }">
                            <div class="custom-tree-node">
                                <div class="node-label-wrapper">
                                    <el-icon v-if="data.children && data.children.length > 0" class="folder-icon">
                                        <Collection />
                                    </el-icon>
                                    <el-icon v-else class="leaf-icon">
                                        <Document />
                                    </el-icon>
                                    <span class="node-text">{{ node.label }}</span>
                                </div>
                            </div>
                        </template>
                    </el-tree-select>
                    <span class="form-item-tip">必填项：请至少选择一个科目用于题目分类</span>
                </el-form-item>

                <!-- 去重检查 -->
                <el-form-item label="去重检查">
                    <el-switch
                        v-model="importForm.checkDuplicate"
                        active-text="启用"
                        inactive-text="禁用"
                    />
                    <span class="form-item-tip">启用后将跳过与库中重复的题目</span>
                </el-form-item>

                <!-- 文件上传 -->
                <el-form-item label="上传JSON文件" required>
                    <el-upload
                        ref="uploadRef"
                        class="upload-area"
                        drag
                        :auto-upload="false"
                        :on-change="handleFileChange"
                        :limit="1"
                        accept=".json"
                        :file-list="fileList"
                        :disabled="importing"
                    >
                        <el-icon class="el-icon--upload"><UploadFilled /></el-icon>
                        <div class="el-upload__text">
                            将 JSON 文件拖到此处，或<em>点击上传</em>
                        </div>
                        <template #tip>
                            <div class="el-upload__tip">
                                只能上传 JSON 格式文件，且不超过 10MB
                            </div>
                        </template>
                    </el-upload>
                </el-form-item>

                <!-- JSON格式说明 -->
                <el-form-item label="JSON格式">
                    <el-alert type="info" :closable="false" show-icon>
                        <template #default>
                            <div class="format-info">
                                <p>JSON文件必须包含 <code>questions</code> 数组，每个题目包含以下字段：</p>
                                <ul>
                                    <li><code>type</code> - 题目类型（1=单选, 2=多选, 3=填空, 4=简答）</li>
                                    <li><code>content</code> - 题干内容</li>
                                    <li><code>options</code> - 选项数组（选择题必填）</li>
                                    <li><code>answer</code> - 正确答案</li>
                                    <li><code>analysis</code> - 解析（可选）</li>
                                    <li><code>tags</code> - 标签数组（可选）</li>
                                </ul>
                                <el-button type="primary" link @click="downloadTemplate">下载JSON模板</el-button>
                            </div>
                        </template>
                    </el-alert>
                </el-form-item>

                <!-- 题目预览 -->
                <el-form-item v-if="parsedQuestions.length > 0" label="题目预览">
                    <div class="preview-container">
                        <div class="preview-header">
                            <span class="preview-count">共 {{ parsedQuestions.length }} 道题目</span>
                            <el-button text type="primary" @click="toggleExpandAll">
                                {{ allExpanded ? '收起全部' : '展开全部' }}
                            </el-button>
                        </div>
                        <el-collapse v-model="activeQuestions" accordion>
                            <el-collapse-item
                                v-for="(question, index) in currentPageQuestions"
                                :key="index"
                                :name="index"
                            >
                                <template #title>
                                    <div class="question-title">
                                        <el-tag :type="getTypeTagType(question.type)" size="small" effect="dark">
                                            {{ getTypeName(question.type) }}
                                        </el-tag>
                                        <span class="question-number">NO.{{ (previewCurrentPage - 1) * pageSize + index + 1 }}</span>
                                    </div>
                                </template>
                                <div class="question-detail">
                                    <div class="detail-row">
                                        <span class="detail-label">题干：</span>
                                        <span class="detail-content" v-html="renderLatex(question.content || '(无)')"></span>
                                    </div>

                                    <div v-if="question.options && question.options.length > 0" class="detail-row">
                                        <span class="detail-label">选项：</span>
                                        <div class="detail-content options-list">
                                            <div
                                                v-for="(option, optIdx) in question.options"
                                                :key="optIdx"
                                                class="option-item"
                                                v-html="formatOption(option, optIdx)"
                                            >
                                            </div>
                                        </div>
                                    </div>

                                    <div class="detail-row">
                                        <span class="detail-label">答案：</span>
                                        <span class="detail-content answer" v-html="renderLatex(question.answer || '(未填写)')"></span>
                                    </div>

                                    <div v-if="question.analysis" class="detail-row">
                                        <span class="detail-label">解析：</span>
                                        <span class="detail-content analysis" v-html="renderLatex(question.analysis)"></span>
                                    </div>

                                    <div v-if="question.tags && question.tags.length > 0" class="detail-row tags-row">
                                        <span class="detail-label">标签：</span>
                                        <span class="detail-content">
                                            <el-tag
                                                v-for="(tag, tagIdx) in question.tags"
                                                :key="tagIdx"
                                                size="small"
                                                effect="plain"
                                                style="margin-right: 8px; margin-bottom: 5px"
                                            >
                                                {{ tag }}
                                            </el-tag>
                                        </span>
                                    </div>

                                    <div v-if="question.source" class="detail-row">
                                        <span class="detail-label">来源：</span>
                                        <span class="detail-content source">{{ question.source }}</span>
                                    </div>
                                </div>
                            </el-collapse-item>
                        </el-collapse>

                        <!-- 分页 -->
                        <div v-if="parsedQuestions.length > pageSize" class="preview-pagination">
                            <el-pagination
                                :current-page="previewCurrentPage"
                                :page-size="pageSize"
                                :total="parsedQuestions.length"
                                layout="prev, pager, next"
                                small
                            />
                        </div>
                    </div>
                </el-form-item>

                <!-- 导入结果 -->
                <el-form-item v-if="importResult" label="导入结果">
                    <el-alert
                        :type="importResult.success ? 'success' : 'error'"
                        :closable="false"
                        show-icon
                    >
                        <template #default>
                            <div class="result-info">
                                <p>{{ importResult.summary }}</p>
                                <div v-if="importResult.details" class="result-details">
                                    <el-descriptions :column="1" border size="small">
                                        <el-descriptions-item label="成功导入">
                                            {{ importResult.details.success }} 题
                                        </el-descriptions-item>
                                        <el-descriptions-item label="跳过重复">
                                            {{ importResult.details.duplicate }} 题
                                        </el-descriptions-item>
                                        <el-descriptions-item label="导入失败">
                                            {{ importResult.details.failed }} 题
                                        </el-descriptions-item>
                                    </el-descriptions>
                                </div>
                            </div>
                        </template>
                    </el-alert>
                </el-form-item>

                <!-- 操作按钮 -->
                <el-form-item>
                    <el-button
                        type="primary"
                        size="large"
                        :loading="importing"
                        :disabled="!canImport"
                        @click="handleImport"
                    >
                        {{ importing ? '导入中...' : '开始导入' }}
                    </el-button>
                    <el-button size="large" @click="resetForm" :disabled="importing">
                        重置
                    </el-button>
                </el-form-item>
            </el-form>
        </el-card>
    </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getAllBooks, getSubjectTree, importQuestions } from '@/api/questionImportExport'
import katex from 'katex'

// 数据
const importing = ref(false)
const allBooks = ref([])
const subjectTree = ref([])
const fileList = ref([])
const jsonFile = ref(null)
const parsedQuestions = ref([])

const importForm = ref({
    bookMode: 'existing', // existing, new, skip
    bookId: null,
    newBookName: '',
    newBookType: 1, // 1=习题册, 2=试卷
    subjectIds: [],
    checkDuplicate: true
})

const importResult = ref(null)

// 预览相关
const activeQuestions = ref([])
const previewCurrentPage = ref(1)
const pageSize = ref(5)
const allExpanded = ref(false)

// 当前页题目
const currentPageQuestions = computed(() => {
    const start = (previewCurrentPage.value - 1) * pageSize.value
    const end = start + pageSize.value
    return parsedQuestions.value.slice(start, end)
})

// 是否可以导入
const canImport = computed(() => {
    // 必须选择科目
    if (!importForm.value.subjectIds || importForm.value.subjectIds.length === 0) {
        return false
    }

    // 习题册验证
    if (importForm.value.bookMode === 'existing') {
        if (!importForm.value.bookId) return false
    } else if (importForm.value.bookMode === 'new') {
        if (!importForm.value.newBookName || !importForm.value.newBookName.trim()) return false
    }

    // 必须上传文件
    return jsonFile.value !== null && parsedQuestions.value.length > 0
})

// 加载数据
const loadData = async () => {
    try {
        const [booksRes, subjectsRes] = await Promise.all([
            getAllBooks(),
            getSubjectTree()
        ])

        if (booksRes.code === 200) {
            allBooks.value = booksRes.data || []
        }

        if (subjectsRes.code === 200) {
            subjectTree.value = subjectsRes.data || []
        }
    } catch (error) {
        ElMessage.error('加载数据失败')
    }
}

// 习题册模式变化
const handleBookModeChange = (mode) => {
    importForm.value.bookId = null
    importForm.value.newBookName = ''
    importForm.value.newBookType = 1
}

// 处理文件变化
const handleFileChange = async (file) => {
    if (!file.raw.name.endsWith('.json')) {
        ElMessage.error('只能上传 JSON 格式文件')
        fileList.value = []
        return false
    }

    if (file.size > 10 * 1024 * 1024) {
        ElMessage.error('文件大小不能超过 10MB')
        fileList.value = []
        return false
    }

    jsonFile.value = file.raw
    fileList.value = [file]

    // 解析 JSON
    await parseJSONFile(file.raw)
}

// 解析 JSON 文件
const parseJSONFile = async (file) => {
    try {
        const text = await file.text()
        const json = JSON.parse(text)

        if (!json.questions || !Array.isArray(json.questions)) {
            throw new Error('JSON 格式错误：缺少 questions 数组')
        }

        parsedQuestions.value = json.questions
        importResult.value = null
        ElMessage.success(`成功解析 ${json.questions.length} 道题目`)
    } catch (error) {
        ElMessage.error('解析 JSON 失败：' + error.message)
        parsedQuestions.value = []
        jsonFile.value = null
        fileList.value = []
    }
}

// 执行导入
const handleImport = async () => {
    if (importing.value) return

    importing.value = true
    importResult.value = null

    try {
        const importData = {
            bookId: importForm.value.bookMode === 'existing' ? importForm.value.bookId : null,
            newBookName: importForm.value.bookMode === 'new' ? importForm.value.newBookName : null,
            newBookType: importForm.value.bookMode === 'new' ? importForm.value.newBookType : null,
            subjectIds: importForm.value.subjectIds,
            checkDuplicate: importForm.value.checkDuplicate,
            questions: parsedQuestions.value
        }

        const res = await importQuestions(importData)

        if (res.code === 200) {
            importResult.value = {
                success: true,
                summary: res.data,
                details: parseImportResult(res.data)
            }
            ElMessage.success('导入成功！')
        } else {
            importResult.value = {
                success: false,
                summary: res.message || '导入失败',
                details: null
            }
            ElMessage.error(res.message || '导入失败')
        }
    } catch (error) {
        importResult.value = {
            success: false,
            summary: '导入失败：' + error.message,
            details: null
        }
        ElMessage.error('导入失败：' + error.message)
    } finally {
        importing.value = false
    }
}

// 解析导入结果
const parseImportResult = (message) => {
    const successMatch = message.match(/成功[：:]\s*(\d+)/)
    const duplicateMatch = message.match(/跳过重复[：:]\s*(\d+)/)
    const failedMatch = message.match(/失败[：:]\s*(\d+)/)

    return {
        success: successMatch ? parseInt(successMatch[1]) : 0,
        duplicate: duplicateMatch ? parseInt(duplicateMatch[1]) : 0,
        failed: failedMatch ? parseInt(failedMatch[1]) : 0
    }
}

// 重置表单
const resetForm = () => {
    importForm.value = {
        bookMode: 'existing',
        bookId: null,
        newBookName: '',
        newBookType: 1,
        subjectIds: [],
        checkDuplicate: true
    }
    fileList.value = []
    jsonFile.value = null
    parsedQuestions.value = []
    importResult.value = null
}

// 获取题目类型名称
const getTypeName = (type) => {
    const types = {
        1: '单选题',
        2: '多选题',
        3: '填空题',
        4: '简答/问答'
    }
    return types[type] || '未知'
}

// 获取题目类型标签颜色
const getTypeTagType = (type) => {
    const types = {
        1: 'success',
        2: 'warning',
        3: 'info',
        4: 'danger'
    }
    return types[type] || ''
}

// 展开/收起全部
const toggleExpandAll = () => {
    allExpanded.value = !allExpanded.value
    if (allExpanded.value) {
        // 展开当前页所有题目
        activeQuestions.value = currentPageQuestions.value.map((_, index) => index)
    } else {
        activeQuestions.value = []
    }
}

// 渲染 LaTeX 公式
const renderLatex = (text) => {
    if (!text) return ''

    // 匹配 $$...$$ (块级公式)
    const blockRegex = /\$\$([^$]+)\$\$/g
    // 匹配 $...$ (行内公式)
    const inlineRegex = /\$([^$]+)\$/g

    let result = text

    // 替换所有 LaTeX 公式
    const replacements = []

    // 先收集所有块级公式
    result = result.replace(blockRegex, (_, tex) => {
        const placeholder = `___LATEX_BLOCK_${replacements.length}___`
        replacements.push({
            type: 'block',
            tex: tex
        })
        return placeholder
    })

    // 再收集所有行内公式
    result = result.replace(inlineRegex, (_, tex) => {
        const placeholder = `___LATEX_INLINE_${replacements.length}___`
        replacements.push({
            type: 'inline',
            tex: tex
        })
        return placeholder
    })

    // 替换占位符为实际的 KaTeX HTML
    replacements.forEach((item, idx) => {
        const placeholder = item.type === 'block' ? `___LATEX_BLOCK_${idx}___` : `___LATEX_INLINE_${idx}___`
        try {
            // 确保在渲染前，先将 \\ 替换成 \，防止 KaTeX 错误解析转义字符
            const unescapedTex = item.tex.replace(/\\\\/g, '\\')
            const html = katex.renderToString(unescapedTex, {
                throwOnError: false,
                displayMode: item.type === 'block'
            })
            result = result.replace(placeholder, html)
        } catch (e) {
            console.error('KaTeX render error:', e)
            result = result.replace(placeholder, item.tex)
        }
    })

    return result
}

// 格式化选项显示
const formatOption = (option, index) => {
    if (typeof option === 'string') {
        const label = String.fromCharCode(65 + index)
        const text = renderLatex(option)
        return `${label}. ${text}`
    } else if (typeof option === 'object' && option !== null) {
        const label = option.label || String.fromCharCode(65 + index)
        const text = renderLatex(option.text || '')
        return `${label}. ${text}`
    }
    return ''
}

// 下载模板
const downloadTemplate = () => {
    const template = {
        "questions": [
            {
                "type": 1,
                "content": "设函数 f(x) = x³ - 3x + 1，求 f'(x)",
                // 选项格式1（旧格式，兼容）：字符串数组
                // "options": ["3x² - 3", "3x² + 3", "x² - 3", "x² + 3"],
                // 选项格式2（新格式，推荐）：对象数组
                "options": [
                    {"label": "A", "text": "3x² - 3"},
                    {"label": "B", "text": "3x² + 3"},
                    {"label": "C", "text": "x² - 3"},
                    {"label": "D", "text": "x² + 3"}
                ],
                "answer": "A",
                "analysis": "根据求导法则，f'(x) = 3x² - 3",
                "tags": ["导数", "基础题"],
                "source": "高等数学例题"
            },
            {
                "type": 2,
                "content": "下列哪些函数在区间 (0, +∞) 上单调递增？",
                "options": [
                    {"label": "A", "text": "f(x) = x²"},
                    {"label": "B", "text": "f(x) = eˣ"},
                    {"label": "C", "text": "f(x) = ln(x)"},
                    {"label": "D", "text": "f(x) = 1/x"}
                ],
                "answer": "ABC",
                "analysis": "x²在x>0时单调递增；eˣ始终单调递增；ln(x)在定义域内单调递增；1/x在x>0时单调递减",
                "tags": ["单调性", "多选题"]
            },
            {
                "type": 4,
                "content": "请论述马克思主义哲学中的质变与量变关系。",
                "answer": "量变是质变的必要准备，质变是量变的必然结果",
                "analysis": "本题考查唯物辩证法核心原理...",
                "tags": ["马克思主义哲学", "简答题"]
            }
        ]
    }

    const blob = new Blob([JSON.stringify(template, null, 2)], { type: 'application/json' })
    const url = URL.createObjectURL(blob)
    const a = document.createElement('a')
    a.href = url
    a.download = 'questions_template.json'
    a.click()
    URL.revokeObjectURL(url)
    ElMessage.success('模板已下载')
}

onMounted(() => {
    loadData()
})
</script>

<style scoped lang="scss">
.import-card {
    border-radius: 12px;
}

.card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
}

.text-header {
    display: flex;
    flex-direction: column;
    gap: 4px;
}

.title-text {
    font-size: 18px;
    font-weight: 600;
    color: #1f2f3d;
    position: relative;
    padding-left: 12px;
}

.title-text::before {
    content: "";
    position: absolute;
    left: 0;
    top: 50%;
    transform: translateY(-50%);
    width: 4px;
    height: 18px;
    background: #409eff;
    border-radius: 2px;
}

.header-desc {
    font-size: 13px;
    color: #909399;
}

.import-form {
    max-width: 800px;

    .form-item-tip {
        margin-left: 10px;
        font-size: 12px;
        color: #909399;
    }
}

.upload-area {
    width: 100%;
    margin: 10px 0;
}

.preview-container {
    width: 100%;
    border-radius: 8px;
    padding: 20px;
    background: linear-gradient(135deg, #f5f7fa 0%, #e8eef5 100%);
    box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);

    .preview-header {
        display: flex;
        justify-content: space-between;
        align-items: center;
        margin-bottom: 20px;
        padding: 15px 20px;
        background: white;
        border-radius: 8px;
        box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);

        .preview-count {
            font-size: 15px;
            font-weight: 600;
            color: #303133;
            display: flex;
            align-items: center;
            gap: 8px;

            &::before {
                content: '';
                display: inline-block;
                width: 4px;
                height: 16px;
                background: linear-gradient(180deg, #409eff 0%, #66b1ff 100%);
                border-radius: 2px;
            }
        }
    }

    :deep(.el-collapse) {
        border: none;
        background: transparent;
    }

    :deep(.el-collapse-item) {
        margin-bottom: 12px;
        border-radius: 8px;
        overflow: hidden;
        box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
        transition: all 0.3s ease;
        background: white;

        &:hover {
            box-shadow: 0 4px 16px rgba(64, 158, 255, 0.15);
            transform: translateY(-2px);
        }

        &.is-active {
            box-shadow: 0 4px 20px rgba(64, 158, 255, 0.2);
        }
    }

    :deep(.el-collapse-item__header) {
        background: white;
        border: none;
        padding: 15px 20px;
        font-size: 14px;
        height: auto;
        line-height: 1.5;

        &.is-active {
            border-bottom: 1px solid #ebeef5;
        }
    }

    :deep(.el-collapse-item__wrap) {
        border: none;
        background: transparent;
    }

    :deep(.el-collapse-item__content) {
        padding: 0 20px 20px;
    }

    .question-title {
        display: flex;
        align-items: center;
        gap: 12px;

        .question-number {
            font-size: 16px;
            font-weight: 700;
            padding: 6px 16px;
            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
            -webkit-background-clip: text;
            -webkit-text-fill-color: transparent;
            background-clip: text;
            letter-spacing: 1px;
            position: relative;

            &::after {
                content: '';
                position: absolute;
                bottom: 0;
                left: 0;
                right: 0;
                height: 2px;
                background: linear-gradient(90deg, transparent 0%, rgba(102, 126, 234, 0.3) 50%, transparent 100%);
            }
        }
    }

    .question-detail {
        padding: 20px 0;

        .detail-row {
            display: flex;
            margin-bottom: 16px;
            line-height: 1.8;
            padding: 12px;
            border-radius: 6px;
            background: #fafbfc;
            transition: all 0.2s ease;

            &:hover {
                background: #f5f7fa;
            }

            &:last-child {
                margin-bottom: 0;
            }

            &.tags-row {
                flex-wrap: wrap;

                .detail-content {
                    display: flex;
                    flex-wrap: wrap;
                    gap: 8px;
                }
            }

            .detail-label {
                min-width: 70px;
                font-weight: 600;
                color: #606266;
                flex-shrink: 0;
                font-size: 13px;
                text-transform: uppercase;
                letter-spacing: 0.5px;
            }

            .detail-content {
                flex: 1;
                color: #303133;
                word-break: break-word;
                font-size: 14px;

                &.answer {
                    color: #67c23a;
                    font-weight: 600;
                    font-size: 15px;
                    padding: 4px 12px;
                    background: linear-gradient(135deg, #e7f9e7 0%, #d4f0d4 100%);
                    border-radius: 4px;
                    display: inline-block;
                }

                &.analysis {
                    color: #909399;
                    font-style: italic;
                    line-height: 1.6;
                    padding: 12px;
                    background: #fef9f0;
                    border-left: 3px solid #e6a23c;
                    border-radius: 4px;
                }

                &.source {
                    color: #909399;
                    font-size: 13px;
                    font-style: italic;
                }
            }

            .options-list {
                display: flex;
                flex-direction: column;
                gap: 8px;
                width: 100%;

                .option-item {
                    padding: 10px 15px;
                    background: white;
                    border: 1px solid #e4e7ed;
                    border-radius: 6px;
                    font-family: 'Consolas', 'Monaco', 'Courier New', monospace;
                    font-size: 13px;
                    color: #303133;
                    transition: all 0.2s ease;
                    position: relative;
                    overflow: hidden;

                    &:hover {
                        border-color: #409eff;
                        background: linear-gradient(135deg, #ecf5ff 0%, #e1f0ff 100%);
                        transform: translateX(4px);
                    }

                    &::before {
                        content: attr(data-index);
                        position: absolute;
                        left: 0;
                        top: 0;
                        bottom: 0;
                        width: 30px;
                        background: linear-gradient(135deg, #409eff 0%, #66b1ff 100%);
                        color: white;
                        display: flex;
                        align-items: center;
                        justify-content: center;
                        font-size: 12px;
                        font-weight: 600;
                        border-radius: 6px 0 0 6px;
                    }

                    padding-left: 40px;
                }
            }
        }
    }

    .preview-pagination {
        display: flex;
        justify-content: center;
        margin-top: 20px;
        padding: 20px;
        background: white;
        border-radius: 8px;
        box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);

        :deep(.el-pagination) {
            .el-pager li {
                border-radius: 6px;
                margin: 0 2px;
                transition: all 0.2s ease;
                font-weight: 500;

                &.is-active {
                    background: linear-gradient(135deg, #409eff 0%, #66b1ff 100%);
                    color: white;
                }

                &:hover:not(.is-active) {
                    color: #409eff;
                }
            }

            .btn-prev,
            .btn-next {
                border-radius: 6px;
                padding: 0 12px;
                transition: all 0.2s ease;

                &:hover {
                    color: #409eff;
                }
            }
        }
    }
}

.format-info {
    p {
        margin-bottom: 10px;
        color: #606266;
    }

    ul {
        margin: 10px 0;
        padding-left: 20px;

        li {
            margin: 5px 0;
            color: #606266;

            code {
                background: #f5f7fa;
                padding: 2px 6px;
                border-radius: 3px;
                font-family: 'Courier New', monospace;
                color: #e6a23c;
            }
        }
    }
}

.result-info {
    p {
        margin-bottom: 15px;
        font-size: 15px;
        font-weight: 500;
    }

    .result-details {
        margin-top: 10px;
    }
}

.custom-tree-node {
    display: flex;
    align-items: center;
    width: 100%;

    .node-label-wrapper {
        display: flex;
        align-items: center;
        gap: 8px;

        .folder-icon,
        .leaf-icon {
            color: #909399;
        }

        .node-text {
            flex: 1;
        }
    }
}
</style>
