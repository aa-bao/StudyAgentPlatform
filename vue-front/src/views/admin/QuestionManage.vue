<template>
    <div class="admin-container">
        <el-card shadow="never" class="table-card">
            <template #header>
                <div class="card-header">
                    <div class="text-header">
                        <span class="title-text">题目管理中心</span>
                        <div class="header-desc">查看并管理题目数据
                        </div>
                    </div>
                    <div class="header-btns">
                        <el-button type="success" icon="Download" @click="exportToExcel">导出 CSV</el-button>
                        <el-button type="primary" icon="Plus" @click="handleAdd">新增题目</el-button>
                    </div>
                </div>
            </template>

            <!-- 搜索表单 -->
            <el-form :inline="true" :model="searchForm" class="search-form">
                <el-form-item label="所属科目">
                    <el-tree-select v-model="searchForm.subjectIds" :data="subjects"
                        :props="{ label: 'name', value: 'id', children: 'children' }" multiple collapse-tags collapse-tags-tooltip clearable placeholder="请选择科目"
                        popper-class="subject-tree-popper" check-strictly
                        style="width: 260px">
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

                                <div v-if="data.children && data.children.length > 0" class="node-action">
                                    <el-checkbox :model-value="isNodeFullySelected(data)"
                                        :indeterminate="isNodePartiallySelected(data)" @change="handleSelectAll(data)"
                                        @click.stop>
                                        <span class="all-text">全选</span>
                                    </el-checkbox>
                                </div>
                            </div>
                        </template>
                    </el-tree-select>
                </el-form-item>

                <el-form-item label="习题册">
                    <el-select v-model="searchForm.bookId" placeholder="请选择习题册" clearable style="width: 200px"
                        @change="loadData">
                        <el-option v-for="book in books" :key="book.id" :label="book.name" :value="book.id" />
                    </el-select>
                </el-form-item>

                <!-- 查询按钮 -->
                <el-form-item>
                    <el-button type="primary" icon="Search" @click="loadData">查询</el-button>
                    <el-button icon="Refresh" @click="resetSearch">重置</el-button>
                </el-form-item>
            </el-form>

            <!-- 数据表格 -->
            <el-table :data="tableData" v-loading="loading" class="modern-table" stripe>
                <el-table-column prop="id" label="ID" width="70" align="center" />
                <el-table-column prop="content" label="题干内容" show-overflow-tooltip min-width="250" />
                <el-table-column prop="answer" label="答案" width="80" align="center" show-overflow-tooltip />
                <el-table-column prop="analysis" label="解析" show-overflow-tooltip min-width="150" />

                <el-table-column label="科目" width="150" align="center">
                    <template #default="scope">
                        <template v-if="scope.row.subjectNames && scope.row.subjectNames.length">
                            <el-tag v-for="(name, index) in scope.row.subjectNames" :key="index" size="small" style="margin: 2px;">
                                {{ name }}
                            </el-tag>
                        </template>
                        <el-tag v-else size="small" type="info" class="text-muted">未关联</el-tag>
                    </template>
                </el-table-column>
                <el-table-column label="书本" width="180" align="center">
                    <template #default="scope">
                        <template v-if="scope.row.bookNames && scope.row.bookNames.length">
                            <el-tag v-for="(name, index) in scope.row.bookNames" :key="index" size="small" type="success" style="margin: 2px;">
                                {{ name }}
                            </el-tag>
                        </template>
                        <el-tag v-else size="small" type="info" class="text-muted">未关联</el-tag>
                    </template>
                </el-table-column>

                <el-table-column prop="type" label="类型" width="80" align="center">
                    <template #default="scope">
                        <el-tag :type="String(scope.row.type) === '1' ? 'primary' : 'success'">
                            {{ getTypeLabel(scope.row.type) }}
                        </el-tag>
                    </template>
                </el-table-column>
                <el-table-column prop="difficulty" label="难度" width="100" align="center">
                    <template #default="scope">
                        <el-rate v-model="scope.row.difficulty" disabled show-score text-color="#ff9900" />
                    </template>
                </el-table-column>
                <el-table-column prop="createTime" label="导入时间" width="160" align="center">
                    <template #default="scope">
                        <div class="full-time">   
                        {{ scope.row.createTime ? scope.row.createTime.replace('T', ' ').substring(0, 19) : '-' }}
                        </div>
                    </template>
                </el-table-column>

                <el-table-column label="操作" width="150" align="center" fixed="right">
                    <template #default="scope">
                        <el-button size="small" type="primary" link @click="handleEdit(scope.row)">编辑</el-button>
                        <el-button size="small" type="info" link @click="handleView(scope.row)" style="color: #009688;">查看</el-button>
                        <el-button size="small" type="danger" link @click="handleDelete(scope.row.id)">删除</el-button>
                    </template>
                </el-table-column>
            </el-table>

            <!-- 分页 -->
            <div class="pagination-container">
                <el-pagination :current-page="pageNum" :page-size="pageSize" :page-sizes="[10, 20, 50, 100]"
                    layout="total, sizes, prev, pager, next, jumper" :total="total" @size-change="handleSizeChange"
                    @current-change="handlePageChange" />
            </div>
        </el-card>

        <!-- 编辑/新增对话框 -->
        <el-dialog v-model="dialogVisible" :title="form.id ? '编辑题目' : '新增题目'" width="1200px" destroy-on-close
            @close="resetForm" class="question-dialog" top="3vh">
            <el-form :model="form" :rules="rules" ref="formRef" label-position="top" class="question-form">
                <div class="form-scroll-area">
                    <!-- 第一行：基础属性配置 -->
                    <div class="config-panel">
                        <el-row :gutter="20">
                            <el-col :span="6">
                                <el-form-item label="所属科目" prop="subjectIds">
                                    <el-tree-select v-model="form.subjectIds" :data="subjects"
                                        :props="{ label: 'name', value: 'id', children: 'children' }"
                                        placeholder="请选择所属科目" check-strictly filterable multiple
                                        collapse-tags collapse-tags-tooltip style="width: 100%"
                                        :render-after-expand="false" default-expand-all
                                        @change="handleSubjectIdsChange">
                                        <template #default="{ node, data }">
                                            <span class="tree-node-label">
                                                <span>{{ node.label }}</span>
                                                <el-checkbox
                                                    v-if="data.children && data.children.length > 0"
                                                    :model-value="isNodeFullySelected(data)"
                                                    :indeterminate="isNodePartiallySelected(data)"
                                                    @change="handleSelectAll(data)"
                                                    @click.stop
                                                    class="select-all-checkbox"
                                                >全选</el-checkbox>
                                            </span>
                                        </template>
                                    </el-tree-select>
                                </el-form-item>
                            </el-col>
                            <el-col :span="6">
                                <el-form-item label="所属书本" prop="bookIds">
                                    <el-select v-model="form.bookIds" placeholder="选择习题册" style="width: 100%" filterable multiple collapse-tags collapse-tags-tooltip>
                                        <el-option v-for="book in books" :key="book.id" :label="book.name"
                                            :value="book.id" />
                                    </el-select>
                                </el-form-item>
                            </el-col>
                            <el-col :span="6">
                                <el-form-item label="题目类型" prop="type">
                                    <el-select v-model="form.type" placeholder="选择类型" style="width: 100%">
                                        <el-option v-for="type in questionTypesList" :key="type.code" :label="type.name" :value="type.code" />
                                    </el-select>
                                </el-form-item>
                            </el-col>
                            <el-col :span="6">
                                <el-form-item label="难度等级" prop="difficulty">
                                    <el-rate v-model="form.difficulty" show-score
                                        :colors="['#99A9BF', '#F7BA2A', '#FF9900']"
                                        style="height: 32px; display: flex; align-items: center;" />
                                </el-form-item>
                            </el-col>
                        </el-row>
                    </div>

                    <!-- 第二行：题目内容 -->
                    <div class="content-panel full-row">
                        <div class="section-title">
                            <span><el-icon><EditPen /></el-icon> 题目内容</span>
                            <el-upload ref="uploadRef" action="#" :auto-upload="false" :show-file-list="false"
                                :on-change="handleAiRecognize" accept="image/*" class="ai-uploader">
                                <el-button type="primary" size="small" :loading="recognizing" plain icon="Picture">
                                    AI 图片转文字
                                </el-button>
                            </el-upload>
                        </div>
                        <el-form-item prop="content">
                            <div class="content-editor-container">
                                <el-input v-model="form.content" type="textarea" :rows="5"
                                    placeholder="请输入题干内容，支持 LaTeX 公式..." resize="none" />
                                <div class="latex-preview-box" v-if="form.content">
                                    <div class="preview-label"><el-icon><View /></el-icon> 实时渲染预览：</div>
                                    <div class="preview-content" v-html="renderLatex(form.content)"></div>
                                </div>
                            </div>
                        </el-form-item>
                    </div>

                    <!-- 第三行：选项设置（简化为固定4个选项，不可删除） -->
                    <div class="options-panel" v-if="form.type === 1 || form.type === 2">
                        <div class="section-title">
                            <span>
                                <el-icon>
                                    <List />
                                </el-icon> 选项设置
                            </span>
                            <span class="text-info" style="font-size: 12px; font-weight: normal; color: #909399;">
                                请录入选项内容，支持 LaTeX 公式
                            </span>
                        </div>
                        <div class="options-grid">
                            <div v-for="(opt, index) in form.options" :key="index" class="option-row">
                                <div class="option-label">{{ String.fromCharCode(65 + index) }}</div>

                                <div class="option-edit-main">
                                    <el-input v-model="form.options[index]" placeholder="输入该选项内容..." type="textarea"
                                        :autosize="{ minRows: 1, maxRows: 3 }" class="option-input" />
                                    <div class="option-preview"
                                        v-html="renderLatex(form.options[index]) || '<span class=\'placeholder\'>公式预览...</span>'">
                                    </div>
                                </div>
                            </div>
                        </div>
                    </div>

                    <!-- 第四行：答案或解析 -->
                    <div class="content-panel">
                        <div class="section-title">
                            <span><el-icon><EditPen /></el-icon>
                            {{ form.type === 4 ? '答案解析' : '题目解析' }}
                            </span>
                        </div>
                        <!-- 修改第210行左右的代码 -->
                        <el-form-item :prop="form.type === 4 ? 'answer' : 'analysis'">
                            <div class="content-editor-container">
                                <el-input v-model="dynamicContent" type="textarea" :rows="4"
                                    :placeholder="form.type === 4 ? '请输入答案解析...' : '请输入详细解析，支持 LaTeX 公式...'"
                                    resize="none" />
                                <div class="latex-preview-box" v-if="dynamicContent">
                                    <div class="preview-label">
                                        <el-icon>
                                            <View />
                                        </el-icon> 实时渲染预览：
                                    </div>
                                    <div class="preview-content" v-html="renderLatex(dynamicContent)"></div>
                                </div>
                            </div>
                        </el-form-item>
                    </div>

                    <!-- 第五行：正确答案（非简答题显示）、标签 -->
                    <div class="meta-panel" v-if="form.type !== 4">
                        <el-row :gutter="20">
                            <el-col :span="8">
                                <el-form-item label="正确答案" prop="answer">
                                    <el-input v-model="form.answer"
                                        :placeholder="form.type === 1 ? '如：A' : form.type === 2 ? '如：AB' : '请输入答案'" />
                                </el-form-item>
                            </el-col>
                            <el-col :span="8">
                                <el-form-item label="题目标签" prop="tags">
                                    <el-select v-model="form.tags" multiple filterable allow-create default-first-option
                                        placeholder="输入标签回车添加" style="width: 100%">
                                        <el-option v-for="tag in tagOptions" :key="tag" :label="tag" :value="tag" />
                                    </el-select>
                                </el-form-item>
                            </el-col>
                        </el-row>
                    </div>

                    <!-- 简答题的标签 -->
                    <div class="meta-panel" v-else>
                        <el-form-item label="题目标签" prop="tags">
                            <el-select v-model="form.tags" multiple filterable allow-create default-first-option
                                placeholder="输入标签回车添加" style="width: 50%">
                                <el-option v-for="tag in tagOptions" :key="tag" :label="tag" :value="tag" />
                            </el-select>
                        </el-form-item>
                    </div>
                </div>
            </el-form>
            <template #footer>
                <div class="dialog-footer">
                    <el-button @click="dialogVisible = false">取 消</el-button>
                    <el-button type="primary" :loading="saving" @click="saveQuestion">保 存</el-button>
                </div>
            </template>
        </el-dialog>

        <!-- 查看详情对话框 -->
        <el-dialog v-model="viewDialogVisible" title="题目详情" width="700px">
            <el-descriptions :column="1" border v-if="viewQuestion">
                <el-descriptions-item label="题干内容">
                    <div v-html="renderLatex(viewQuestion.content)"></div>
                </el-descriptions-item>
                <el-descriptions-item v-if="viewQuestion.options && viewQuestion.options.length" label="选项">
                    <div v-for="(opt, index) in viewQuestion.options" :key="index">
                        {{ String.fromCharCode(65 + index) }}. <span v-html="renderLatex(opt)"></span>
                    </div>
                </el-descriptions-item>
                <el-descriptions-item label="正确答案">{{ viewQuestion.answer }}</el-descriptions-item>
                <el-descriptions-item label="解析">
                    <div v-html="renderLatex(viewQuestion.analysis || '暂无解析')"></div>
                </el-descriptions-item>
                <el-descriptions-item label="难度">L {{ viewQuestion.difficulty }}</el-descriptions-item>
                <el-descriptions-item label="科目">
                    <template v-if="viewQuestion.subjectNames && viewQuestion.subjectNames.length">
                        <el-tag v-for="(name, index) in viewQuestion.subjectNames" :key="index" size="small" style="margin: 2px;">
                            {{ name }}
                        </el-tag>
                    </template>
                    <span v-else class="text-muted">未关联</span>
                </el-descriptions-item>
                <el-descriptions-item label="书本">
                    <template v-if="viewQuestion.bookNames && viewQuestion.bookNames.length">
                        <el-tag v-for="(name, index) in viewQuestion.bookNames" :key="index" size="small" type="success" style="margin: 2px;">
                            {{ name }}
                        </el-tag>
                    </template>
                    <span v-else class="text-muted">未关联</span>
                </el-descriptions-item>
                <el-descriptions-item label="标签">{{ Array.isArray(viewQuestion.tags) ? viewQuestion.tags.join(', ') : '无'
                    }}</el-descriptions-item>
            </el-descriptions>
        </el-dialog>
    </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import request from '@/utils/request'
import { ElMessage, ElMessageBox } from 'element-plus'
import katex from 'katex'
import { getQuestionTypesBySubject } from '@/api/subject'
import { loadQuestionTypes } from '@/utils/questionTypes'

// 数据定义
const loading = ref(false)
const saving = ref(false)
const tableData = ref([])
const questionTypesList = ref([]) // 题型列表（动态）

const total = ref(0)
const pageNum = ref(1)
const pageSize = ref(10)

const dialogVisible = ref(false)
const viewDialogVisible = ref(false)
const formRef = ref(null)

const searchForm = ref({ subjectIds: [], bookId: null })
const subjects = ref([]) 
const books = ref([])
const viewQuestion = ref(null)
const recognizing = ref(false)
const uploadRef = ref(null)

const form = ref({
    id: null,
    subjectIds: [],
    bookIds: [],
    type: 1,
    content: '',
    options: ['', '', '', ''],
    answer: '',
    analysis: '',
    difficulty: 3,
    tags: []
})

const tagOptions = ref(['考研真题', '模拟题', '易错题', '重点', '基础', '进阶'])

// 计算属性处理动态绑定
const dynamicContent = computed({
    get() {
        return form.value.type === 4 ? form.value.answer : form.value.analysis
    },
    set(value) {
        if (form.value.type === 4) {
            form.value.answer = value
        } else {
            form.value.analysis = value
        }
    }
})

// Latex 渲染函数
const renderLatex = (content) => {
    if (!content) return ''
    // 确保在渲染前，先将 \\ 替换成 \，防止 KaTeX 错误解析转义字符
    let processedContent = content.replace(/\\\\/g, '\\')

    return processedContent.replace(/\$([^\$]+)\$/g, (match, tex) => {
        try {
            return katex.renderToString(tex, {
                throwOnError: false,
                displayMode: false
            })
        } catch (err) {
            return match
        }
    }).replace(/\$\$([^\$]+)\$\$/g, (match, tex) => { // 块级公式
        try {
            return katex.renderToString(tex, {
                throwOnError: false,
                displayMode: true
            })
        } catch (err) {
            return match
        }
    })
}

// 校验规则
const rules = {
    subjectIds: [{ required: true, message: '请选择所属科目', trigger: 'change' }],
    bookIds: [{ required: true, message: '请选择所属习题册', trigger: 'change' }],
    type: [{ required: true, message: '请选择题目类型', trigger: 'change' }],
    content: [{ required: true, message: '请输入题干内容', trigger: 'blur' }],
    answer: [{ required: true, message: '请输入正确答案', trigger: 'blur' }],
    difficulty: [{ required: true, message: '请选择难度', trigger: 'change' }]
}

// 获取科目列表
const loadSubjects = async () => {
    try {
        const res = await request.get('/subject/manage-tree')
        subjects.value = res.data || res || []
        console.log('科目树:', subjects.value)
    } catch (e) {
        ElMessage.error('获取科目列表失败')
        console.error(e)
    }
}

// 全选/取消全选处理
const handleSelectAll = (node) => {
    // 递归收集所有子节点 ID
    const collectIds = (n) => {
        const ids = [n.id]
        if (n.children && n.children.length > 0) {
            n.children.forEach(child => {
                ids.push(...collectIds(child))
            })
        }
        return ids
    }

    const allIds = collectIds(node)
    const selectedSet = new Set(searchForm.value.subjectIds)

    if (selectedSet.has(node.id)) {
        // 取消全选：移除该节点及其所有子节点
        allIds.forEach(id => selectedSet.delete(id))
    } else {
        // 全选：添加该节点及其所有子节点
        allIds.forEach(id => selectedSet.add(id))
    }

    searchForm.value.subjectIds = Array.from(selectedSet)
}

// 检测是否全选
const isNodeFullySelected = (node) => {
    const collectIds = (n) => {
        const ids = [n.id]
        if (n.children && n.children.length > 0) {
            n.children.forEach(child => {
                ids.push(...collectIds(child))
            })
        }
        return ids
    }
    const allIds = collectIds(node)
    const selectedSet = new Set(searchForm.value.subjectIds)
    return allIds.every(id => selectedSet.has(id))
}

// 检测是否部分选中
const isNodePartiallySelected = (node) => {
    const collectIds = (n) => {
        const ids = [n.id]
        if (n.children && n.children.length > 0) {
            n.children.forEach(child => {
                ids.push(...collectIds(child))
            })
        }
        return ids
    }
    const allIds = collectIds(node)
    const selectedSet = new Set(searchForm.value.subjectIds)
    const selectedCount = allIds.filter(id => selectedSet.has(id)).length
    return selectedCount > 0 && selectedCount < allIds.length
}

// 获取书本列表 (同时在搜索和新增中使用)
const loadBooks = async () => {
    try {
        const res = await request.get('/book/list')
        books.value = res.data || res || []
        // console.log('书本列表:', books.value)
    } catch (e) {
        ElMessage.error('获取书本列表失败')
        console.error(e)
    }
}

// 加载题目数据
const loadData = async () => {
    loading.value = true
    try {
        const params = {
            pageNum: pageNum.value,
            pageSize: pageSize.value
        }
        if (searchForm.value.subjectIds && searchForm.value.subjectIds.length > 0) {
            params.subjectIds = searchForm.value.subjectIds.join(',')
        }
        if (searchForm.value.bookId) {
            params.bookId = searchForm.value.bookId
        }

        console.log('请求参数:', params)
        const res = await request.get('/question/page', { params })
        const data = res.data || res
        tableData.value = data.records || []
        total.value = data.total || 0
        console.log('题目数据:', tableData.value)
    } catch (e) {
        ElMessage.error('获取数据失败')
        console.error('错误详情:', e)
    } finally {
        loading.value = false
    }
}

// 重置搜索
const resetSearch = () => {
    searchForm.value = { subjectIds: [], bookId: null }
    pageNum.value = 1
    loadData()
}

// 分页
const handlePageChange = (page) => {
    pageNum.value = page
    loadData()
}

const handleSizeChange = (size) => {
    pageSize.value = size
    pageNum.value = 1
    loadData()
}

// 类型标签
const getTypeLabel = (type) => {
    const map = { 1: '单选', 2: '多选', 3: '填空', 4: '简答' }
    return map[type] || '未知'
}

// 重置表单
const resetForm = () => {
    form.value = {
        id: null,
        subjectIds: [],
        bookIds: [],
        type: 1,
        content: '',
        options: ['', '', '', ''],
        answer: '',
        analysis: '',
        difficulty: 3,
        tags: []
    }
    formRef.value?.clearValidate()
}

// 新增
const handleAdd = () => {
    resetForm()
    dialogVisible.value = true
}

// 编辑
const handleEdit = (row) => {
    console.log('编辑题目:', row)
    form.value = {
        id: row.id,
        subjectIds: Array.isArray(row.subjectIds) ? [...row.subjectIds] : (row.subjectId ? [row.subjectId] : []),
        bookIds: Array.isArray(row.bookIds) ? [...row.bookIds] : (row.bookId ? [row.bookId] : []),
        type: row.type,
        content: row.content || '',
        options: Array.isArray(row.options) ? [...row.options] : ['', '', '', ''],
        answer: row.answer || '',
        analysis: row.analysis || '',
        difficulty: row.difficulty || 3,
        tags: Array.isArray(row.tags) ? [...row.tags] : []
    }
    dialogVisible.value = true

    // 加载该科目对应的题型
    if (form.value.subjectIds && form.value.subjectIds.length > 0) {
        loadQuestionTypesForSubjects(form.value.subjectIds)
    }
}

// 处理科目变化，动态加载题型
const handleSubjectIdsChange = async (subjectIds) => {
    if (!subjectIds || subjectIds.length === 0) {
        // 清空时，显示所有题型
        questionTypesList.value = await loadQuestionTypes()
        return
    }

    // 加载第一个选中科目对应的题型
    await loadQuestionTypesForSubjects(subjectIds)
}

// 根据科目加载题型
const loadQuestionTypesForSubjects = async (subjectIds) => {
    if (!subjectIds || subjectIds.length === 0) {
        questionTypesList.value = await loadQuestionTypes()
        return
    }

    // 只取第一个 Level 1 科目
    const firstSubjectId = subjectIds[0]
    const subject = findSubjectById(subjects.value, firstSubjectId)

    if (!subject) {
        questionTypesList.value = await loadQuestionTypes()
        return
    }

    // 如果是 Level 1 科目，直接加载其题型
    if (subject.level === '1' || subject.level === 1) {
        try {
            const res = await getQuestionTypesBySubject(firstSubjectId)
            if (res.code === 200 && res.data) {
                questionTypesList.value = res.data
                console.log(`科目 ${firstSubjectId} 支持的题型:`, res.data)
            } else {
                questionTypesList.value = await loadQuestionTypes()
            }
        } catch (e) {
            console.error('获取题型列表失败:', e)
            questionTypesList.value = await loadQuestionTypes()
        }
    } else {
        // 如果是下级科目，查找其父级科目
        const parentSubject = findParentLevel1Subject(subjects.value, firstSubjectId)
        if (parentSubject) {
            try {
                const res = await getQuestionTypesBySubject(parentSubject.id)
                if (res.code === 200 && res.data) {
                    questionTypesList.value = res.data
                    console.log(`父科目 ${parentSubject.id} 支持的题型:`, res.data)
                } else {
                    questionTypesList.value = await loadQuestionTypes()
                }
            } catch (e) {
                console.error('获取题型列表失败:', e)
                questionTypesList.value = await loadQuestionTypes()
            }
        } else {
            questionTypesList.value = await loadQuestionTypes()
        }
    }
}

// 在科目树中查找科目
const findSubjectById = (subjectList, id) => {
    for (const subject of subjectList) {
        if (subject.id === id) return subject
        if (subject.children && subject.children.length > 0) {
            const found = findSubjectById(subject.children, id)
            if (found) return found
        }
    }
    return null
}

// 查找 Level 1 父科目
const findParentLevel1Subject = (subjectList, id) => {
    for (const subject of subjectList) {
        if (subject.level === '1' || subject.level === 1) {
            // 检查是否是子孙节点
            if (hasDescendant(subject, id)) {
                return subject
            }
        }
        if (subject.children && subject.children.length > 0) {
            const found = findParentLevel1Subject(subject.children, id)
            if (found) return found
        }
    }
    return null
}

// 检查是否有某个子孙节点
const hasDescendant = (subject, targetId) => {
    if (subject.id === targetId) return true
    if (subject.children && subject.children.length > 0) {
        for (const child of subject.children) {
            if (hasDescendant(child, targetId)) return true
        }
    }
    return false
}

// 查看
const handleView = (row) => {
    viewQuestion.value = row
    viewDialogVisible.value = true
}

// 保存
const saveQuestion = async () => {
    await formRef.value?.validate()

    saving.value = true
    try {
        const url = form.value.id ? '/question/update' : '/question/add'
        
        // 构造提交数据
        const payload = {
            ...form.value
        }
        
        console.log('保存题目:', payload)
        await request.post(url, payload)
        ElMessage.success(form.value.id ? '修改成功' : '添加成功')
        dialogVisible.value = false
        loadData()
    } catch (e) {
        ElMessage.error('保存失败')
        console.error('错误详情:', e)
    } finally {
        saving.value = false
    }
}

// 删除
const handleDelete = (id) => {
    ElMessageBox.confirm('确认删除该题目吗？删除后将同时删除所有关联关系。', '警告', {
        type: 'warning',
        confirmButtonText: '确定',
        cancelButtonText: '取消'
    }).then(async () => {
        try {
            await request.delete(`/question/delete/${id}`)
            ElMessage.success('删除成功')
            loadData()
        } catch (e) {
            ElMessage.error('删除失败')
            console.error(e)
        }
    })
}

// 导出CSV
const exportToExcel = () => {
    let csv = '\uFEFFID,类型,科目,书本,题干,答案,难度\n'
    tableData.value.forEach(row => {
        const subjectNames = row.subjectNames && row.subjectNames.length ? row.subjectNames.join(';') : '未关联'
        const bookNames = row.bookNames && row.bookNames.length ? row.bookNames.join(';') : '未关联'
        csv += `${row.id},${getTypeLabel(row.type)},${subjectNames},${bookNames},${(row.content || '').replace(/,/g, '，')},${row.answer || ''},${row.difficulty || 0}\n`
    })
    const blob = new Blob([csv], { type: 'text/csv;charset=utf-8;' })
    const url = window.URL.createObjectURL(blob)
    const a = document.createElement('a')
    a.href = url
    a.download = `题目导出_${new Date().toISOString().slice(0, 10)}.csv`
    a.click()
    window.URL.revokeObjectURL(url)
}


// 识别处理函数
const handleAiRecognize = async (file) => {
    // console.log('触发识别流程, file.status:', file.status)
    // 仅处理新增的文件 (status === 'ready')
    if (file.status !== 'ready') return

    // 防止重复触发
    if (recognizing.value) return

    const formData = new FormData()
    formData.append('file', file.raw)

    recognizing.value = true
    try {
        // console.log('开始发送请求...')
        // 调用后端识别接口，设置 60s 超时
        const res = await request.post('/question/recognize', formData, {
            headers: { 'Content-Type': 'multipart/form-data' },
            timeout: 60000
        })
        // console.log('请求返回:', res)

        // 处理后端返回的结构化 JSON
        if (res.code === 200 && res.data) {
            const dto = res.data
            // console.log('识别结果 DTO:', dto)

            // 自动填充表单字段
            if (dto.content) {
                form.value.content = dto.content
            }
            if (Array.isArray(dto.options) && dto.options.length >= 4) {
                // 去除选项中的 ABCD 前缀，避免重复
                form.value.options = dto.options.slice(0, 4).map(opt => {
                    // 匹配 "A. "、"A)"、"A："、"A " 等格式并去除
                    return opt.replace(/^[A-Z][\.\) ：:]\s*/, '').replace(/^[A-Z]\s+/, '')
                })
            }
            if (dto.answer) {
                // 清理答案中的 ABCD 前缀
                form.value.answer = dto.answer.replace(/^[A-Z][\.\) ：:]\s*/, '').replace(/^[A-Z]\s+/, '')
            }
            if (dto.analysis) {
                form.value.analysis = dto.analysis
            }

            form.value.type = 1

            ElMessage.success('识别成功！请检查填充的题目内容和选项是否有误。')
        }
    } catch (e) {
        console.error('识别失败:', e)
        ElMessage.error('AI 识别服务暂时不可用: ' + (e.message || '未知错误'))
    } finally {
        setTimeout(() => {
            recognizing.value = false
            uploadRef.value?.clearFiles()
        }, 100)
    }
}

onMounted(async () => {
    console.log('页面已挂载')
    loadSubjects()
    loadBooks()
    loadData()
    // 初始化题型列表
    questionTypesList.value = await loadQuestionTypes()
})
</script>

<style scoped>
/* 容器与基础卡片 */
.admin-container {
    min-height: calc(100vh - 84px);
}

.table-card {
    border-radius: 12px;
    border: none;
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.05) !important;
}

/* 头部样式 */
.card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    /* padding: 4px 0; */
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

/* 搜索表单 */
.search-form {
    background: #fcfcfd;
    padding: 18px 20px 0;
    border-radius: 8px;
    margin-bottom: 20px;
    border: 1px solid #ebeef5;
}

/* 树节点布局 */
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
    /* 图标和文字的间距 */
}

.node-text{
    font-weight: 500;
}

/* 图标颜色区分 */
.folder-icon {
    color: #e6a23c;
    /* 文件夹用暖色 */
    font-size: 14px;
}

.leaf-icon {
    color: #909399;
    /* 叶子节点用中性色 */
    font-size: 13px;
}

.node-text {
    color: #303133;
}

/* 全选复选框美化 */
.node-action :deep(.el-checkbox) {
    height: auto;
    margin-right: 0;
}

.node-action :deep(.el-checkbox__label) {
    font-size: 12px;
    color: #409eff;
    padding-left: 4px;
}

.all-text {
    font-weight: 500;
}

/* 表格 */
.modern-table {
    font-size: 14px;
}

/* 注册日期文字 */
.full-time {
    font-family: 'Monaco', 'Courier New', monospace;
    font-size: 13px;
    color: #475569;
}

/* 暂无数据的文字颜色 */
.text-muted {
    color: #cbd5e1;
    font-style: italic;
    font-size: 13px;
}

/* 现代表格细节微调 */
:deep(.el-table__header th) {
    background-color: #f8fafc !important;
    color: #475569;
    font-weight: 600;
}

.modern-table {
    border-radius: 8px;
    overflow: hidden;
}

/* 翻页栏容器 */
.pagination-container {
    margin-top: 25px;
    padding: 15px 20px;
    display: flex;
    justify-content: center;
    background: #fdfdfd;
    border-radius: 0 0 8px 8px;
}

/* Element Plus 分页样式 */
:deep(.el-pagination.is-background .el-pager li) {
    background-color: #fff;
    border: 1px solid #dcdfe6;
    border-radius: 6px;
    font-weight: 500;
    transition: all 0.2s;
}

/* 激活状态的页码样式 */
:deep(.el-pagination.is-background .el-pager li:not(.is-disabled).is-active) {
    background-color: #409eff;
    color: #fff;
    border-color: #409eff;
    box-shadow: 0 2px 6px rgba(64, 158, 255, 0.3);
}

/* 悬停效果 */
:deep(.el-pagination.is-background .el-pager li:hover) {
    color: #409eff;
    border-color: #409eff;
}

/* 总条数和跳页文字颜色 */
:deep(.el-pagination__total),
:deep(.el-pagination__jump) {
    color: #606266;
    font-size: 13px;
}

/* 输入框圆角优化 */
:deep(.el-input__wrapper) {
    border-radius: 6px !important;
}



/* 题目内容容器 */
.content-editor-container {
    border: 1px solid #dcdfe6;
    border-radius: 8px;
    background: #fff;
    transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

.content-editor-container:focus-within {
    border-color: #409eff;
}

/* Latex 预览区域 */
.latex-preview-box {
    border-top: 1px solid #f0f0f2;
    padding: 12px;
}

.preview-label {
    font-size: 12px;
    color: #909399;
    margin-bottom: 6px;
    display: flex;
    align-items: center;
    gap: 4px;
}

.preview-content {
    min-height: 40px;
    font-size: 14px;
    line-height: 1.6;
    color: #303133;
}

/* 选项网格保持两列布局 */
.options-grid {
    display: grid;
    grid-template-columns: repeat(2, 1fr);
    gap: 20px;
    margin-bottom: 24px;
}

/* 题干与解析占满整行 */
.full-row {
    width: 100%;
    margin-bottom: 24px;
}

.content-editor-container {
    width: 100%;
    border: 1px solid #dcdfe6;
    border-radius: 8px;
    background: #fff;
    transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
    overflow: hidden;
}

/* 对话框内模块 */
.section-title {
    display: flex;
    justify-content: space-between;
    align-items: center;
    font-size: 15px;
    font-weight: 600;
    margin: 20px 0 12px;
    color: #303133;
}

/* 搜索与配置区域优化 */
.config-panel {
    background-color: #f8f9fb;
    padding: 24px;
    border-radius: 12px;
    margin-bottom: 24px;
    border: 1px solid #edf0f5;
}

.tree-node-label {
    display: flex;
    align-items: center;
    justify-content: space-between;
    width: 100%;
    box-sizing: border-box;
}

.select-all-checkbox {
    margin-left: 10px;
    font-weight: normal;
}

/* 编辑器全行容器 */
.full-width-panel {
    width: 100%;
    margin-bottom: 24px;
}



/* 选项网格布局 */
.options-grid {
    display: grid;
    grid-template-columns: repeat(2, 1fr);
    gap: 20px;
    margin-bottom: 24px;
}

/* 选项行 */
.option-row {
    display: flex;
    align-items: flex-start;
    /* 顶部对齐 */
    gap: 12px;
    border: 1px solid #e4e7ed;
    border-radius: 8px;
    padding: 16px;
    background-color: #fff;
    transition: all 0.2s ease;
}

.option-row:hover {
    border-color: #409eff;
    background-color: #fbfdff;
}

.option-header-area {
    display: flex;
    align-items: center;
    gap: 10px;
}

/* 左侧字母 */
.option-label {
    flex-shrink: 0;
    background: #409eff;
    color: white;
    width: 28px;
    height: 28px;
    border-radius: 50%;
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 14px;
    font-weight: bold;
    margin-top: 4px;
}

/* 右侧主容器：上下布局 */
.option-edit-main {
    flex-grow: 1;
    display: flex;
    flex-direction: column;
    gap: 8px;
    overflow: hidden;
    /* 防止内容溢出 */
}

/* 输入框外观 */
.option-input :deep(.el-textarea__inner) {
    box-shadow: none;
    background-color: #fcfcfd;
    border-color: #eef0f5;
}

.option-input :deep(.el-textarea__inner:focus) {
    background-color: #fff;
}

/* 下方预览框 */
.option-preview {
    font-size: 13px;
    color: #606266;
    padding: 10px;
    background: #f8f9fb;
    border-radius: 6px;
    border-left: 3px solid #e4e7ed;
    /* 加一个小边框增加区分度 */
    min-height: 24px;
    line-height: 1.5;
}

.placeholder {
    color: #c0c4cc;
    font-style: italic;
}

/* 滚动条美化 */
.form-scroll-area::-webkit-scrollbar {
    width: 6px;
}

.form-scroll-area::-webkit-scrollbar-thumb {
    background: #ddd;
    border-radius: 10px;
}

/* 分页 */
.pagination-container {
    padding: 20px 0 10px;
}
</style>


