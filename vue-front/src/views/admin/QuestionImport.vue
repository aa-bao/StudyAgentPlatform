<template>
    <div class="admin-container">
        <el-card shadow="never" class="import-card">
            <template #header>
                <div class="card-header">
                    <div class="text-header">
                        <span class="title-text">JSON 文件批量导入题目</span>
                        <div class="header-desc">上传 JSON 文件批量导入题目到题库</div>
                    </div>
                    <el-button type="primary" icon="QuestionFilled" @click="showTutorial = true">
                        查看教程
                    </el-button>
                </div>
            </template>

            <el-steps :active="currentStep" finish-status="success" align-center class="steps-container">
                <el-step title="选择习题册和科目" />
                <el-step title="上传JSON文件" />
                <el-step title="预览题目" />
                <el-step title="确认导入" />
            </el-steps>

            <div class="step-content">
                <!-- 步骤1: 选择习题册和科目 -->
                <div v-if="currentStep === 0" class="step-panel">
                    <el-form :model="importForm" label-width="140px" class="import-form">
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

                        <el-form-item label="去重检查">
                            <el-switch
                                v-model="importForm.checkDuplicate"
                                active-text="启用"
                                inactive-text="禁用"
                            />
                            <span class="form-item-tip">启用后将跳过与库中重复的题目</span>
                        </el-form-item>

                        <el-alert
                            title="提示"
                            type="info"
                            :closable="false"
                            show-icon
                            class="info-alert"
                        >
                            <template #default>
                                <div>• 可以选择导入到现有习题册或新建习题册</div>
                                <div>• 可以选择多个科目进行关联</div>
                                <div>• 暂不选择习题册时，题目会先导入到题库，后续可以手动关联</div>
                            </template>
                        </el-alert>
                    </el-form>
                </div>

                <!-- 步骤2: 上传JSON文件 -->
                <div v-if="currentStep === 1" class="step-panel">
                    <div v-if="!jsonFile" class="upload-container">
                        <el-upload
                            ref="uploadRef"
                            class="upload-area"
                            drag
                            :auto-upload="false"
                            :on-change="handleFileChange"
                            :limit="1"
                            accept=".json"
                            :file-list="fileList"
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

                        <el-divider />

                        <el-alert
                            title="JSON 格式要求"
                            type="warning"
                            :closable="false"
                            show-icon
                            class="format-alert"
                        >
                            <template #default>
                                <pre class="json-example">{
  "questions": [
    {
      "type": 1,
      "content": "题干内容",
      "options": ["A. 选项1", "B. 选项2", "C. 选项3", "D. 选项4"],
      "answer": "A",
      "analysis": "解析内容",
      "tags": ["标签1", "标签2"]
    }
  ]
}</pre>
                            </template>
                        </el-alert>
                    </div>

                    <!-- 已上传文件 -->
                    <div v-else class="file-uploaded">
                        <el-card>
                            <template #header>
                                <div class="file-header">
                                    <span>已上传文件</span>
                                    <el-button type="danger" size="small" icon="Delete" @click="removeFile">
                                        删除文件
                                    </el-button>
                                </div>
                            </template>
                            <div class="file-info">
                                <el-icon><Document /></el-icon>
                                <span class="file-name">{{ jsonFile.name }}</span>
                                <el-tag type="success" size="small">已解析 {{ parsedQuestions.length }} 道题目</el-tag>
                            </div>
                        </el-card>
                    </div>
                </div>

                <!-- 步骤3: 预览题目 -->
                <div v-if="currentStep === 2" class="step-panel">
                    <div v-if="loading" class="loading-container">
                        <el-icon class="is-loading"><Loading /></el-icon>
                        <span>正在解析 JSON 文件，请稍候...</span>
                    </div>

                    <div v-else-if="parsedQuestions.length === 0" class="empty-container">
                        <el-empty description="暂无题目数据">
                            <el-button type="primary" @click="currentStep = 1">重新上传文件</el-button>
                        </el-empty>
                    </div>

                    <div v-else>
                        <div class="preview-header">
                            <span>题目预览 (共 {{ parsedQuestions.length }} 道题)</span>
                            <div class="header-actions">
                                <el-button
                                    type="warning"
                                    size="small"
                                    icon="Edit"
                                    @click="enableEditMode"
                                    v-if="!editMode"
                                >
                                    编辑题目
                                </el-button>
                                <el-button
                                    type="success"
                                    size="small"
                                    icon="Check"
                                    @click="confirmImport"
                                >
                                    确认导入
                                </el-button>
                            </div>
                        </div>

                        <div class="questions-list">
                            <el-card
                                v-for="(question, index) in previewQuestions"
                                :key="index"
                                class="question-card"
                                shadow="hover"
                            >
                                <template #header>
                                    <div class="question-header">
                                        <span class="question-number">第 {{ (currentPage - 1) * pageSize + index + 1 }} 题</span>
                                        <div class="header-controls">
                                            <el-tag :type="getTypeTagType(question.type)" size="small">
                                                {{ getTypeName(question.type) }}
                                            </el-tag>
                                            <el-checkbox
                                                v-model="question.selected"
                                                :checked="selectedAll"
                                                @change="handleSelectChange"
                                            >
                                                选中
                                            </el-checkbox>
                                        </div>
                                    </div>
                                </template>

                                <div class="question-content">
                                    <!-- 编辑模式 -->
                                    <div v-if="editMode" class="edit-mode">
                                        <el-form :model="question" label-width="80px" size="small">
                                            <el-form-item label="题目类型">
                                                <el-select v-model="question.type">
                                                    <el-option label="单选题" :value="1" />
                                                    <el-option label="多选题" :value="2" />
                                                    <el-option label="填空题" :value="3" />
                                                    <el-option label="简答题" :value="4" />
                                                </el-select>
                                            </el-form-item>

                                            <el-form-item label="题干">
                                                <el-input
                                                    v-model="question.content"
                                                    type="textarea"
                                                    :rows="3"
                                                    placeholder="请输入题干"
                                                />
                                            </el-form-item>

                                            <el-form-item label="选项" v-if="question.type <= 2">
                                                <div v-for="(opt, optIdx) in question.options" :key="optIdx" class="option-edit">
                                                    <el-input
                                                        v-model="question.options[optIdx]"
                                                        :placeholder="`选项 ${String.fromCharCode(65 + optIdx)}`"
                                                    />
                                                    <el-button
                                                        type="danger"
                                                        size="small"
                                                        icon="Delete"
                                                        @click="removeOption(question, optIdx)"
                                                        circle
                                                    />
                                                </div>
                                                <el-button
                                                    type="primary"
                                                    size="small"
                                                    icon="Plus"
                                                    @click="addOption(question)"
                                                    v-if="question.options.length < 8"
                                                >
                                                    添加选项
                                                </el-button>
                                            </el-form-item>

                                            <el-form-item label="答案">
                                                <el-input
                                                    v-model="question.answer"
                                                    placeholder="单选填A/B/C/D，多选填AB/ABC等"
                                                />
                                            </el-form-item>

                                            <el-form-item label="解析">
                                                <el-input
                                                    v-model="question.analysis"
                                                    type="textarea"
                                                    :rows="2"
                                                    placeholder="请输入解析"
                                                />
                                            </el-form-item>

                                            <el-form-item label="标签">
                                                <el-input
                                                    v-model="question.tagsStr"
                                                    placeholder="用逗号分隔，如：数据结构,栈"
                                                />
                                            </el-form-item>
                                        </el-form>
                                    </div>

                                    <!-- 预览模式 -->
                                    <div v-else class="preview-mode">
                                        <div class="content-text">
                                            <strong>题干：</strong>
                                            <div v-html="renderMarkdown(question.content)"></div>
                                        </div>

                                        <div v-if="question.options && question.options.length > 0" class="options-list">
                                            <strong>选项：</strong>
                                            <ul>
                                                <li v-for="(option, optIndex) in question.options" :key="optIndex">
                                                    {{ option }}
                                                </li>
                                            </ul>
                                        </div>

                                        <div class="answer-analysis">
                                            <el-row :gutter="20">
                                                <el-col :span="12">
                                                    <div><strong>答案：</strong>{{ question.answer || '未填写' }}</div>
                                                </el-col>
                                                <el-col :span="12">
                                                    <div><strong>解析：</strong>{{ question.analysis || '未填写' }}</div>
                                                </el-col>
                                            </el-row>
                                        </div>

                                        <div v-if="question.tags && question.tags.length > 0" class="tags-list">
                                            <strong>标签：</strong>
                                            <el-tag
                                                v-for="(tag, tagIdx) in question.tags"
                                                :key="tagIdx"
                                                size="small"
                                                style="margin-right: 5px;"
                                            >
                                                {{ tag }}
                                            </el-tag>
                                        </div>
                                    </div>
                                </div>
                            </el-card>
                        </div>

                        <!-- 工具栏 -->
                        <div class="toolbar">
                            <el-checkbox v-model="selectedAll" @change="handleSelectAll">
                                全选当前页
                            </el-checkbox>
                            <el-tag type="info">已选 {{ selectedCount }} 题</el-tag>
                        </div>

                        <el-pagination
                            v-if="parsedQuestions.length > pageSize"
                            :current-page="currentPage"
                            :page-size="pageSize"
                            :total="parsedQuestions.length"
                            layout="prev, pager, next"
                            class="pagination"
                            @current-change="handlePageChange"
                        />
                    </div>
                </div>

                <!-- 步骤4: 完成导入 -->
                <div v-if="currentStep === 3" class="step-panel">
                    <el-result
                        :icon="importResult.success ? 'success' : 'error'"
                        :title="importResult.success ? '导入完成！' : '导入失败'"
                    >
                        <template #sub-title>
                            <div class="result-summary">
                                <p v-if="importResult.summary">{{ importResult.summary }}</p>
                                <div v-if="importResult.details" class="result-details">
                                    <el-descriptions :column="1" border>
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
                        <template #extra>
                            <el-button type="primary" @click="resetImport">继续导入</el-button>
                            <el-button @click="$router.push('/admin/questions-manage')">查看题目列表</el-button>
                        </template>
                    </el-result>
                </div>
            </div>

            <div class="step-actions">
                <el-button v-if="currentStep > 0 && currentStep < 3" @click="prevStep">
                    上一步
                </el-button>
                <el-button
                    v-if="currentStep < 2"
                    type="primary"
                    :disabled="!canGoNext"
                    @click="nextStep"
                >
                    下一步
                </el-button>
                <el-button
                    v-if="currentStep === 2"
                    type="primary"
                    :loading="importing"
                    @click="confirmImport"
                >
                    确认导入 ({{ selectedCount }} 题)
                </el-button>
            </div>
        </el-card>

        <!-- 教程弹窗 -->
        <el-dialog
            v-model="showTutorial"
            title="JSON 导入教程"
            width="800px"
            :close-on-click-modal="false"
        >
            <ImportTutorial @close="showTutorial = false" />
        </el-dialog>
    </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
    UploadFilled,
    Collection,
    Document,
    Loading,
    Edit,
    Delete,
    Plus
} from '@element-plus/icons-vue'
import { getAllBooks, getSubjectTree, importQuestions } from '@/api/questionImportExport'
import ImportTutorial from './ImportTutorial.vue'

// 数据
const currentStep = ref(0)
const showTutorial = ref(false)
const loading = ref(false)
const importing = ref(false)
const currentPage = ref(1)
const pageSize = ref(10)
const editMode = ref(false)
const selectedAll = ref(false)

const importForm = ref({
    bookMode: 'existing', // existing, new, skip
    bookId: null,
    newBookName: '',
    newBookType: 1, // 1=习题册, 2=试卷
    subjectIds: [],
    checkDuplicate: true
})

const allBooks = ref([])
const subjectTree = ref([])
const fileList = ref([])
const jsonFile = ref(null)
const parsedQuestions = ref([])

const importResult = ref({
    success: false,
    summary: '',
    details: null
})

// 预览题目（分页）
const previewQuestions = computed(() => {
    const start = (currentPage.value - 1) * pageSize.value
    const end = start + pageSize.value
    return parsedQuestions.value.slice(start, end)
})

// 已选题目数量
const selectedCount = computed(() => {
    return parsedQuestions.value.filter(q => q.selected).length
})

// 是否可以进入下一步
const canGoNext = computed(() => {
    if (currentStep.value === 0) {
        // 验证第一步：习题册和科目
        // 科目必填
        if (!importForm.value.subjectIds || importForm.value.subjectIds.length === 0) {
            return false
        }

        // 习题册验证
        if (importForm.value.bookMode === 'existing') {
            return importForm.value.bookId !== null
        } else if (importForm.value.bookMode === 'new') {
            return importForm.value.newBookName.trim() !== ''
        }
        return true // skip 模式
    }
    if (currentStep.value === 1) {
        return jsonFile.value !== null && parsedQuestions.value.length > 0
    }
    if (currentStep.value === 2) {
        return selectedCount.value > 0
    }
    return true
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
        return false
    }

    if (file.size > 10 * 1024 * 1024) {
        ElMessage.error('文件大小不能超过 10MB')
        return false
    }

    jsonFile.value = file.raw
    fileList.value = [file]

    // 立即解析 JSON
    await parseJSONFile(file.raw)
}

// 删除文件
const removeFile = () => {
    ElMessageBox.confirm('确定要删除已上传的文件吗？', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
    }).then(() => {
        jsonFile.value = null
        fileList.value = []
        parsedQuestions.value = []
        ElMessage.success('文件已删除')
    }).catch(() => {})
}

// 解析 JSON 文件
const parseJSONFile = async (file) => {
    loading.value = true
    try {
        const text = await file.text()
        const json = JSON.parse(text)

        if (!json.questions || !Array.isArray(json.questions)) {
            throw new Error('JSON 格式错误：缺少 questions 数组')
        }

        // 处理题目数据 - 修复解析问题
        parsedQuestions.value = json.questions.map((q, index) => {
            // 兼容两种字段名: content 和 question
            let content = q.content || q.question || ''

            // 只移除明显的格式错误：题干开头如果是单个选项前缀（A. B. C. D.）
            // 使用更精确的正则，只匹配单个字母加点
            content = content.replace(/^[A-D]\.\s*/, '')

            return {
                ...q,
                content: content.trim(),
                selected: true,
                tagsStr: q.tags ? q.tags.join(', ') : '',
                options: q.options || [],
                answer: q.answer || '',
                analysis: q.analysis || ''
            }
        }).filter(q => {
            // 过滤掉明显无效的题目
            return q.content && q.content.length > 0
        })

        // 保存图片信息（如果有的话）
        if (json.images && Array.isArray(json.images)) {
            parsedQuestions.value.images = json.images
        } else {
            parsedQuestions.value.images = []
        }

        ElMessage.success(`成功解析 ${parsedQuestions.value.length} 道题目`)
    } catch (error) {
        ElMessage.error('解析 JSON 失败：' + error.message)
        parsedQuestions.value = []
        jsonFile.value = null
        fileList.value = []
    } finally {
        loading.value = false
    }
}

// 下一步
const nextStep = () => {
    if (currentStep.value === 2 && editMode.value) {
        saveEdits()
    }
    currentStep.value++
}

// 上一步
const prevStep = () => {
    currentStep.value--
}

// 确认导入
const confirmImport = async () => {
    // 验证科目ID
    if (!importForm.value.subjectIds || importForm.value.subjectIds.length === 0) {
        ElMessage.warning('请至少选择一个科目')
        currentStep.value = 0
        return
    }

    if (selectedCount.value === 0) {
        ElMessage.warning('请至少选择一道题目')
        return
    }

    importing.value = true
    try {
        // 过滤选中的题目
        const selectedQuestions = parsedQuestions.value
            .filter(q => q.selected)
            .map(q => ({
                ...q,
                tags: q.tagsStr ? q.tagsStr.split(',').map(t => t.trim()).filter(t => t) : q.tags || []
            }))

        const importData = {
            bookId: importForm.value.bookMode === 'existing' ? importForm.value.bookId : null,
            newBookName: importForm.value.bookMode === 'new' ? importForm.value.newBookName : null,
            newBookType: importForm.value.bookMode === 'new' ? importForm.value.newBookType : null,
            subjectIds: importForm.value.subjectIds,
            checkDuplicate: importForm.value.checkDuplicate,
            questions: selectedQuestions
        }

        const res = await importQuestions(importData)

        if (res.code === 200) {
            importResult.value = {
                success: true,
                summary: res.data,
                details: parseImportResult(res.data)
            }
            currentStep.value = 3
            ElMessage.success('导入成功！')
        } else {
            ElMessage.error(res.message || '导入失败')
        }
    } catch (error) {
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

// 重置导入
const resetImport = () => {
    currentStep.value = 0
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
    importResult.value = {
        success: false,
        summary: '',
        details: null
    }
    currentPage.value = 1
    editMode.value = false
    selectedAll.value = false
}

// 分页变化
const handlePageChange = (page) => {
    currentPage.value = page
    selectedAll.value = false
}

// 全选/取消全选当前页
const handleSelectAll = (checked) => {
    const start = (currentPage.value - 1) * pageSize.value
    const end = start + pageSize.value
    for (let i = start; i < end && i < parsedQuestions.value.length; i++) {
        parsedQuestions.value[i].selected = checked
    }
}

// 单个题目选择变化
const handleSelectChange = () => {
    const start = (currentPage.value - 1) * pageSize.value
    const end = start + pageSize.value
    selectedAll.value = true
    for (let i = start; i < end && i < parsedQuestions.value.length; i++) {
        if (!parsedQuestions.value[i].selected) {
            selectedAll.value = false
            break
        }
    }
}

// 启用编辑模式
const enableEditMode = () => {
    editMode.value = true
    ElMessage.info('进入编辑模式，修改后点击"确认导入"保存')
}

// 保存编辑
const saveEdits = () => {
    parsedQuestions.value.forEach(q => {
        if (q.tagsStr) {
            q.tags = q.tagsStr.split(',').map(t => t.trim()).filter(t => t)
        }
    })
    editMode.value = false
    ElMessage.success('修改已保存')
}

// 添加选项
const addOption = (question) => {
    if (question.options.length < 8) {
        question.options.push('')
    }
}

// 删除选项
const removeOption = (question, index) => {
    if (question.options.length > 2) {
        question.options.splice(index, 1)
    } else {
        ElMessage.warning('至少保留2个选项')
    }
}

// 获取题目类型名称
const getTypeName = (type) => {
    const types = {
        1: '单选题',
        2: '多选题',
        3: '填空题',
        4: '简答题'
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

// 渲染 Markdown，支持图片显示
const renderMarkdown = (content) => {
    if (!content) return ''

    // 处理图片标记 [图片:img_0] -> <img src="...">
    let rendered = content.replace(/\[图片:(\w+)\]/g, (match, imageId) => {
        // 查找对应的图片数据
        const image = parsedQuestions.value.images?.find(img => img.id === imageId)
        if (image && image.base64) {
            return `<img src="${image.base64}" alt="${image.filename}" style="max-width: 100%; margin: 10px 0; border-radius: 4px;" />`
        }
        return match // 保留原标记
    })

    // 处理已有的markdown图片语法
    rendered = rendered.replace(/!\[\]\(([^)]+)\)/g, (match, path) => {
        // 如果是相对路径，尝试从images数组中查找
        const filename = path.split('/').pop()
        const image = parsedQuestions.value.images?.find(img => img.filename === filename)
        if (image && image.base64) {
            return `<img src="${image.base64}" alt="${filename}" style="max-width: 100%; margin: 10px 0; border-radius: 4px;" />`
        }
        return match
    })

    // 加粗
    rendered = rendered.replace(/\*\*(.*?)\*\*/g, '<strong>$1</strong>')

    // 换行
    rendered = rendered.replace(/\n/g, '<br>')

    return rendered
}

onMounted(() => {
    loadData()
})
</script>

<style scoped lang="scss">
.admin-container {
    padding: 20px;
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

.header-desc {
    font-size: 13px;
    color: #909399;
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

.steps-container {
    margin: 30px 0;
}

.step-content {
    min-height: 400px;
    margin-top: 30px;
}

.step-panel {
    padding: 20px;
}

.import-form {
    .info-alert {
        margin-top: 20px;
    }

    .form-item-tip {
        margin-left: 10px;
        font-size: 12px;
        color: #909399;
    }
}

.upload-container {
    margin: 20px 0;
}

.upload-area {
    margin: 20px 0;
}

.file-uploaded {
    .file-header {
        display: flex;
        justify-content: space-between;
        align-items: center;
    }

    .file-info {
        display: flex;
        align-items: center;
        gap: 15px;
        font-size: 16px;

        .file-name {
            flex: 1;
            font-weight: 500;
        }
    }
}

.format-alert {
    pre {
        margin: 10px 0 0 0;
        padding: 15px;
        background-color: #f5f7fa;
        border-radius: 4px;
        font-size: 13px;
        line-height: 1.6;
        color: #606266;
        font-family: 'Courier New', monospace;
    }
}

.json-example {
    margin: 10px 0 0 0;
    padding: 15px;
    background-color: #f5f7fa;
    border-radius: 4px;
    font-size: 13px;
    line-height: 1.6;
    color: #606266;
    font-family: 'Courier New', monospace;
}

.loading-container,
.empty-container {
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    padding: 60px 0;
    font-size: 16px;
    color: #606266;

    .el-icon {
        font-size: 48px;
        margin-bottom: 20px;
        color: #409eff;
    }
}

.preview-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 20px;
    padding: 15px;
    background-color: #f5f7fa;
    border-radius: 4px;
    font-weight: 600;

    .header-actions {
        display: flex;
        gap: 10px;
    }
}

.questions-list {
    .question-card {
        margin-bottom: 15px;

        .question-header {
            display: flex;
            justify-content: space-between;
            align-items: center;

            .question-number {
                font-weight: 600;
                color: #303133;
            }

            .header-controls {
                display: flex;
                gap: 15px;
                align-items: center;
            }
        }

        .question-content {
            .content-text {
                margin-bottom: 15px;
                line-height: 1.8;
            }

            .options-list {
                margin-bottom: 15px;

                ul {
                    margin: 10px 0 0 20px;
                    padding: 0;

                    li {
                        margin: 5px 0;
                        line-height: 1.6;
                    }
                }
            }

            .answer-analysis {
                padding: 15px;
                background-color: #f5f7fa;
                border-radius: 4px;

                div {
                    margin: 5px 0;
                    line-height: 1.6;
                }
            }

            .tags-list {
                margin-top: 10px;
                padding: 10px;
                background-color: #ecf5ff;
                border-radius: 4px;
            }
        }

        .edit-mode {
            .option-edit {
                display: flex;
                gap: 10px;
                margin-bottom: 10px;

                .el-input {
                    flex: 1;
                }
            }
        }
    }
}

.toolbar {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 15px;
    background-color: #f5f7fa;
    border-radius: 4px;
    margin-bottom: 15px;
}

.pagination {
    display: flex;
    justify-content: center;
    margin-top: 20px;
}

.step-actions {
    display: flex;
    justify-content: center;
    gap: 15px;
    margin-top: 30px;
    padding-top: 20px;
    border-top: 1px solid #ebeef5;
}

.result-summary {
    p {
        font-size: 16px;
        color: #606266;
        margin-bottom: 20px;
    }
}

.result-details {
    margin-top: 20px;
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
