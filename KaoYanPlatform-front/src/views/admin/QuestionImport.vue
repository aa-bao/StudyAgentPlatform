<template>
    <div class="admin-container">
        <el-card shadow="never" class="import-card">
            <template #header>
                <div class="card-header">
                    <div class="text-header">
                        <span class="title-text">JSON 批量导入题目</span>
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
                <el-step title="确认导入" />
                <el-step title="完成" />
            </el-steps>

            <div class="step-content">
                <!-- 步骤1: 选择习题册和科目 -->
                <div v-if="currentStep === 0" class="step-panel">
                    <el-form :model="importForm" label-width="120px" class="import-form">
                        <el-form-item label="选择习题册">
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

                        <el-form-item label="选择科目">
                            <el-tree-select
                                v-model="importForm.subjectIds"
                                :data="subjectTree"
                                :props="{ label: 'name', value: 'id', children: 'children' }"
                                multiple
                                collapse-tags
                                collapse-tags-tooltip
                                clearable
                                placeholder="请选择科目（可多选）"
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
                        </el-form-item>

                        <el-alert
                            title="提示"
                            type="info"
                            :closable="false"
                            show-icon
                            class="info-alert"
                        >
                            <template #default>
                                <div>• 题目将导入到指定的习题册和科目中</div>
                                <div>• 可以选择多个科目进行关联</div>
                                <div>• JSON 文件中的题目将自动关联到选定的习题册和科目</div>
                            </template>
                        </el-alert>
                    </el-form>
                </div>

                <!-- 步骤2: 上传JSON文件 -->
                <div v-if="currentStep === 1" class="step-panel">
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

                    <div v-if="jsonContent" class="json-preview">
                        <div class="preview-header">
                            <span>JSON 内容预览</span>
                            <el-button
                                type="primary"
                                size="small"
                                icon="Check"
                                @click="validateJSON"
                            >
                                验证格式
                            </el-button>
                        </div>
                        <el-input
                            v-model="jsonContent"
                            type="textarea"
                            :rows="10"
                            readonly
                            class="json-textarea"
                        />

                        <div v-if="validationResult.valid" class="validation-success">
                            <el-icon><SuccessFilled /></el-icon>
                            <span>格式验证通过！共 {{ validationResult.count }} 道题目</span>
                        </div>

                        <div v-if="validationResult.error" class="validation-error">
                            <el-icon><CircleCloseFilled /></el-icon>
                            <span>{{ validationResult.error }}</span>
                        </div>
                    </div>

                    <el-alert
                        title="JSON 格式要求"
                        type="warning"
                        :closable="false"
                        show-icon
                        class="format-alert"
                    >
                        <template #default>
                            <pre class="json-example">{{ jsonFormatExample }}</pre>
                        </template>
                    </el-alert>
                </div>

                <!-- 步骤3: 确认导入 -->
                <div v-if="currentStep === 2" class="step-panel">
                    <el-descriptions title="导入信息确认" :column="1" border class="confirm-info">
                        <el-descriptions-item label="习题册">
                            {{ getBookName(importForm.bookId) }}
                        </el-descriptions-item>
                        <el-descriptions-item label="科目">
                            {{ getSubjectNames(importForm.subjectIds) }}
                        </el-descriptions-item>
                        <el-descriptions-item label="题目数量">
                            {{ validationResult.count }} 道题
                        </el-descriptions-item>
                        <el-descriptions-item label="文件名">
                            {{ fileList[0]?.name }}
                        </el-descriptions-item>
                    </el-descriptions>

                    <el-divider />

                    <div class="question-preview">
                        <h4>题目预览（前5题）</h4>
                        <el-table :data="previewQuestions" stripe class="preview-table">
                            <el-table-column prop="type" label="类型" width="100">
                                <template #default="scope">
                                    {{ getQuestionType(scope.row.type) }}
                                </template>
                            </el-table-column>
                            <el-table-column prop="content" label="题目内容" show-overflow-tooltip />
                            <el-table-column prop="answer" label="答案" width="100" />
                        </el-table>
                    </div>
                </div>

                <!-- 步骤4: 完成 -->
                <div v-if="currentStep === 3" class="step-panel result-panel">
                    <el-result
                        :icon="importResult.success ? 'success' : 'error'"
                        :title="importResult.title"
                        :sub-title="importResult.message"
                    >
                        <template #extra>
                            <el-button type="primary" @click="resetImport">继续导入</el-button>
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
                    icon="Upload"
                    @click="startImport"
                >
                    开始导入
                </el-button>
            </div>
        </el-card>

        <!-- 教程对话框 -->
        <el-dialog v-model="showTutorial" title="JSON 导入教程" width="900px" destroy-on-close>
            <ImportTutorial @close="showTutorial = false" />
        </el-dialog>
    </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { UploadFilled, SuccessFilled, CircleCloseFilled, QuestionFilled, Collection, Document } from '@element-plus/icons-vue'
import { importQuestions, getSubjectTree, getAllBooks } from '@/api/questionImportExport'
import ImportTutorial from './ImportTutorial.vue'

// 步骤控制
const currentStep = ref(0)
const showTutorial = ref(false)

// 表单数据
const importForm = ref({
    bookId: null,
    subjectIds: []
})

// 数据
const allBooks = ref([])
const subjectTree = ref([])
const fileList = ref([])
const jsonContent = ref('')
const parsedQuestions = ref([])
const importing = ref(false)

// 验证结果
const validationResult = ref({
    valid: false,
    count: 0,
    error: ''
})

// 导入结果
const importResult = ref({
    success: false,
    title: '',
    message: ''
})

// JSON格式示例
const jsonFormatExample = `{
  "questions": [
    {
      "type": 1,
      "content": "设函数 f(x) = x^3 - 3x + 1，求 f'(x)",
      "options": [
        "A. 3x^2 - 3",
        "B. 3x^2 + 3",
        "C. x^2 - 3",
        "D. x^2 + 3"
      ],
      "answer": "A",
      "analysis": "根据求导法则，f'(x) = 3x^2 - 3",
      "tags": ["导数", "基础题"]
    }
  ]
}`

// 预览题目（前5题）
const previewQuestions = computed(() => {
    return parsedQuestions.value.slice(0, 5)
})

// 是否可以进入下一步
const canGoNext = computed(() => {
    if (currentStep.value === 0) {
        return importForm.value.bookId && importForm.value.subjectIds.length > 0
    }
    if (currentStep.value === 1) {
        return validationResult.value.valid
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
    } catch (e) {
        console.error('加载数据失败', e)
        ElMessage.error('加载数据失败')
    }
}

// 文件变更处理
const handleFileChange = (file) => {
    const reader = new FileReader()
    reader.onload = (e) => {
        jsonContent.value = e.target.result
    }
    reader.readAsText(file.raw)

    // 重置验证结果
    validationResult.value = {
        valid: false,
        count: 0,
        error: ''
    }
    parsedQuestions.value = []
}

// 验证JSON格式
const validateJSON = () => {
    try {
        const data = JSON.parse(jsonContent.value)

        if (!data.questions || !Array.isArray(data.questions)) {
            throw new Error('JSON 缺少 questions 数组')
        }

        if (data.questions.length === 0) {
            throw new Error('questions 数组为空')
        }

        // 验证每个题目字段
        for (let i = 0; i < data.questions.length; i++) {
            const q = data.questions[i]

            if (!q.type || q.type < 1 || q.type > 10) {
                throw new Error(`第 ${i + 1} 题的 type 字段无效（应为 1-10 的整数）`)
            }

            if (!q.content || typeof q.content !== 'string') {
                throw new Error(`第 ${i + 1} 题缺少 content 字段`)
            }

            // 选择题必须有 options
            if ((q.type === 1 || q.type === 2) && (!q.options || !Array.isArray(q.options))) {
                throw new Error(`第 ${i + 1} 题是选择题，必须包含 options 数组`)
            }
        }

        // 验证通过
        parsedQuestions.value = data.questions
        validationResult.value = {
            valid: true,
            count: data.questions.length,
            error: ''
        }

        ElMessage.success(`格式验证通过！共 ${data.questions.length} 道题目`)
    } catch (e) {
        validationResult.value = {
            valid: false,
            count: 0,
            error: e.message
        }
        ElMessage.error('JSON 格式错误：' + e.message)
    }
}

// 下一步
const nextStep = () => {
    if (currentStep.value === 1 && !validationResult.value.valid) {
        ElMessage.warning('请先验证 JSON 格式')
        return
    }
    currentStep.value++
}

// 上一步
const prevStep = () => {
    currentStep.value--
}

// 开始导入
const startImport = async () => {
    importing.value = true

    try {
        const res = await importQuestions({
            bookId: importForm.value.bookId,
            subjectIds: importForm.value.subjectIds,
            questions: parsedQuestions.value
        })

        if (res.code === 200) {
            importResult.value = {
                success: true,
                title: '导入成功',
                message: res.data
            }
            currentStep.value = 3
        } else {
            importResult.value = {
                success: false,
                title: '导入失败',
                message: res.msg || '未知错误'
            }
            currentStep.value = 3
        }
    } catch (e) {
        importResult.value = {
            success: false,
            title: '导入失败',
            message: e.message || '网络错误'
        }
        currentStep.value = 3
    } finally {
        importing.value = false
    }
}

// 重置导入
const resetImport = () => {
    currentStep.value = 0
    importForm.value = {
        bookId: null,
        subjectIds: []
    }
    fileList.value = []
    jsonContent.value = ''
    parsedQuestions.value = []
    validationResult.value = {
        valid: false,
        count: 0,
        error: ''
    }
}

// 获取习题册名称
const getBookName = (bookId) => {
    const book = allBooks.value.find(b => b.id === bookId)
    return book ? book.name : '未选择'
}

// 获取科目名称
const getSubjectNames = (subjectIds) => {
    if (!subjectIds || subjectIds.length === 0) return '未选择'

    const findSubjectName = (nodes, targetId) => {
        for (const node of nodes) {
            if (node.id === targetId) return node.name
            if (node.children) {
                const found = findSubjectName(node.children, targetId)
                if (found) return found
            }
        }
        return null
    }

    return subjectIds
        .map(id => findSubjectName(subjectTree.value, id) || `ID:${id}`)
        .join('、')
}

// 获取题目类型名称
const getQuestionType = (type) => {
    const types = {
        1: '单选题',
        2: '多选题',
        3: '填空题',
        4: '简答题'
    }
    return types[type] || '未知'
}

onMounted(() => {
    loadData()
})
</script>

<style scoped>
.import-card {
    border-radius: 12px;
    border: 1px solid #e8ecef;
}

.card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
}

.text-header {
    position: relative;
    padding-left: 14px;
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
    margin-top: 4px;
}

.steps-container {
    margin: 40px 0;
}

.step-content {
    min-height: 400px;
    padding: 20px 0;
}

.step-panel {
    padding: 20px;
}

.import-form {
    max-width: 600px;
    margin: 0 auto;
}

.info-alert {
    margin-top: 20px;
}

.upload-area {
    margin-bottom: 20px;
}

.json-preview {
    margin: 20px 0;
}

.preview-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 10px;
}

.json-textarea {
    font-family: 'Monaco', 'Courier New', monospace;
    font-size: 13px;
}

.validation-success {
    margin-top: 10px;
    padding: 10px;
    background: #f0f9ff;
    border-left: 3px solid #67c23a;
    border-radius: 4px;
    color: #67c23a;
    display: flex;
    align-items: center;
    gap: 8px;
}

.validation-error {
    margin-top: 10px;
    padding: 10px;
    background: #fef0f0;
    border-left: 3px solid #f56c6c;
    border-radius: 4px;
    color: #f56c6c;
    display: flex;
    align-items: center;
    gap: 8px;
}

.format-alert {
    margin-top: 20px;
}

.json-example {
    margin: 0;
    padding: 15px;
    background: #f5f7fa;
    border-radius: 4px;
    font-family: 'Monaco', 'Courier New', monospace;
    font-size: 12px;
    line-height: 1.6;
    overflow-x: auto;
}

.confirm-info {
    margin-bottom: 20px;
}

.question-preview h4 {
    margin-bottom: 15px;
    color: #303133;
}

.preview-table {
    margin-top: 10px;
}

.result-panel {
    display: flex;
    align-items: center;
    justify-content: center;
}

.step-actions {
    display: flex;
    justify-content: center;
    gap: 15px;
    padding: 20px 0;
    border-top: 1px solid #ebeef5;
}

/* 树节点样式 */
.custom-tree-node {
    flex: 1;
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding-right: 4px;
    font-size: 14px;
}

.node-label-wrapper {
    display: flex;
    align-items: center;
    gap: 6px;
}

.node-text {
    font-weight: 500;
}

.folder-icon {
    color: #e6a23c;
    font-size: 14px;
}

.leaf-icon {
    color: #909399;
    font-size: 13px;
}
</style>
