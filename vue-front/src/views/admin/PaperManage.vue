<template>
    <div class="admin-container">
        <el-card shadow="never" class="table-card">
            <template #header>
                <div class="card-header">
                    <div class="text-header">
                        <span class="title-text">试卷管理中心</span>
                        <div class="header-desc">管理模拟试卷和真题试卷</div>
                    </div>
                    <div class="header-btns">
                        <el-button type="primary" icon="Plus" @click="handleAdd">新增试卷</el-button>
                    </div>
                </div>
            </template>

            <!-- 搜索表单 -->
            <el-form :inline="true" :model="searchForm" class="search-form">
                <el-form-item label="试卷类型">
                    <el-select v-model="searchForm.paperType" placeholder="请选择试卷类型" clearable style="width: 150px">
                        <el-option label="真题" :value="0" />
                        <el-option label="模拟题" :value="1" />
                    </el-select>
                </el-form-item>

                <el-form-item label="考试规格">
                    <el-select v-model="searchForm.examSpecId" placeholder="请选择考试规格" clearable style="width: 200px">
                        <el-option v-for="spec in examSpecs" :key="spec.id" :label="spec.name" :value="spec.id" />
                    </el-select>
                </el-form-item>

                <el-form-item label="年份">
                    <el-input-number v-model="searchForm.year" placeholder="年份" :min="2000" :max="2035"
                        style="width: 150px" />
                </el-form-item>

                <el-form-item label="关键词">
                    <el-input v-model="searchForm.keyword" placeholder="试卷名称关键词" clearable style="width: 200px" />
                </el-form-item>

                <el-form-item>
                    <el-button type="primary" icon="Search" @click="loadData">查询</el-button>
                    <el-button icon="Refresh" @click="resetSearch">重置</el-button>
                </el-form-item>
            </el-form>

            <!-- 数据表格 -->
            <el-table :data="tableData" v-loading="loading" class="modern-table" stripe>
                <el-table-column prop="id" label="ID" width="100" align="center" show-overflow-tooltip />
                <el-table-column prop="title" label="试卷名称" min-width="250" show-overflow-tooltip />
                <el-table-column prop="paperType" label="类型" width="100" align="center">
                    <template #default="scope">
                        <el-tag :type="scope.row.paperType === 0 ? 'warning' : 'success'">
                            {{ scope.row.paperType === 0 ? '真题' : '模拟题' }}
                        </el-tag>
                    </template>
                </el-table-column>
                <el-table-column label="考试科目" width="150" align="center">
                    <template #default="scope">
                        {{ scope.row.subjectName || getSubjectName(scope.row.examSpecId) }}
                    </template>
                </el-table-column>
                <el-table-column prop="totalScore" label="总分" width="80" align="center" />
                <el-table-column prop="timeLimit" label="时长(分钟)" width="100" align="center" />
                <el-table-column prop="year" label="年份" width="80" align="center" />
                <el-table-column prop="createTime" label="创建时间" width="160" align="center">
                    <template #default="scope">
                        <div class="full-time">
                            {{ scope.row.createTime ? scope.row.createTime.replace('T', ' ').substring(0, 19) : '-' }}
                        </div>
                    </template>
                </el-table-column>

                <el-table-column label="操作" width="280" align="center" fixed="right">
                    <template #default="scope">
                        <el-button size="small" type="primary" link @click="handleEdit(scope.row)">编辑</el-button>
                        <el-button size="small" type="info" link
                            @click="handleManageQuestions(scope.row)">题目管理</el-button>
                        <el-button size="small" type="success" link
                            @click="handleViewQuestions(scope.row)">查看题目</el-button>
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
        <el-dialog v-model="dialogVisible" :title="form.id ? '编辑试卷' : '新增试卷'" width="700px" destroy-on-close
            @close="resetForm">
            <el-form :model="form" :rules="rules" ref="formRef" label-width="100px">
                <el-form-item label="试卷名称" prop="title">
                    <el-input v-model="form.title" placeholder="请输入试卷名称，如：2024年考研数学一真题" />
                </el-form-item>

                <el-form-item label="试卷类型" prop="paperType">
                    <el-radio-group v-model="form.paperType">
                        <el-radio :value="0">真题</el-radio>
                        <el-radio :value="1">模拟题</el-radio>
                    </el-radio-group>
                </el-form-item>

                <el-form-item label="考试科目" prop="examSpecId">
                    <el-select v-model="form.examSpecId" placeholder="请选择考试科目" style="width: 100%">
                        <el-option v-for="spec in paperLevel1Subjects" :key="spec.id" :label="spec.name" :value="spec.id" />
                    </el-select>
                </el-form-item>

                <el-form-item label="总分" prop="totalScore">
                    <el-input-number v-model="form.totalScore" :min="0" :max="200" style="width: 100%" />
                </el-form-item>

                <el-form-item label="考试时长" prop="timeLimit">
                    <el-input-number v-model="form.timeLimit" :min="0" :max="300" :step="10" style="width: 100%" />
                    <span style="margin-left: 10px; color: #909399">分钟</span>
                </el-form-item>

                <el-form-item label="年份" prop="year">
                    <el-input-number v-model="form.year" :min="2000" :max="2030" style="width: 100%" />
                </el-form-item>
            </el-form>
            <template #footer>
                <el-button @click="dialogVisible = false">取消</el-button>
                <el-button type="primary" :loading="saving" @click="savePaper">保存</el-button>
            </template>
        </el-dialog>

        <!-- 题目管理对话框 -->
        <el-dialog v-model="questionDialogVisible" :title="`试卷题目管理 - ${currentPaper?.title}`" width="1200px"
            destroy-on-close class="question-manage-dialog">
            <div class="question-manage-content">
                <div class="left-panel">
                    <div class="panel-header">
                        <span class="panel-title">试卷中的题目</span>
                        <div class="header-actions">
                            <el-button type="danger" size="small" :disabled="selectedPaperQuestions.length === 0"
                                @click="batchRemoveQuestions">
                                批量移除 ({{ selectedPaperQuestions.length }})
                            </el-button>
                            <el-button type="primary" size="small" icon="Plus"
                                @click="showAddQuestionDialog">添加题目</el-button>
                        </div>
                    </div>
                    <el-table
                        ref="questionTableRef"
                        :data="paperQuestions"
                        v-loading="loadingQuestions"
                        stripe
                        max-height="500"
                        row-key="id"
                        @selection-change="handlePaperQuestionSelectionChange">
                        <el-table-column type="selection" width="55" align="center" />
                        <el-table-column label="拖拽" width="60" align="center">
                            <template #default>
                                <el-icon style="cursor: move;"><Rank /></el-icon>
                            </template>
                        </el-table-column>
                        <el-table-column prop="sortOrder" label="题号" width="80" align="center" />
                        <el-table-column label="题目类型" width="120" align="center">
                            <template #default="scope">
                                <el-tag size="small" type="primary">{{ getQuestionTypeName(scope.row.paperType || scope.row.type) }}</el-tag>
                            </template>
                        </el-table-column>
                        <el-table-column label="题目内容" min-width="300" show-overflow-tooltip>
                            <template #default="scope">
                                <div v-html="renderLatex(scope.row.content)"></div>
                            </template>
                        </el-table-column>
                        <el-table-column prop="scoreValue" label="分值" width="80" align="center">
                            <template #default="scope">
                                <span class="score-value">{{ scope.row.scoreValue }}</span>
                            </template>
                        </el-table-column>
                        <el-table-column label="操作" width="100" align="center" fixed="right">
                            <template #default="scope">
                                <el-button size="small" type="danger" link
                                    @click="removeQuestionDirect(scope.row)">移除</el-button>
                            </template>
                        </el-table-column>
                    </el-table>
                </div>
            </div>
        </el-dialog>

        <!-- 添加题目对话框 -->
        <el-dialog
            v-model="addQuestionDialogVisible"
            title="添加题目到试卷"
            width="1400px"
            destroy-on-close
            :top="'3vh'"
            class="add-question-dialog"
        >
            <div class="add-question-layout">
                <!-- 左侧：搜索条件 -->
                <div class="search-panel">
                    <div class="panel-title">
                        <el-icon><SearchIcon /></el-icon>
                        <span>搜索条件</span>
                    </div>
                    <el-form :model="questionSearchForm" label-width="80px" size="small">
                        <el-form-item label="考试科目">
                            <el-select
                                v-model="questionSearchForm.subjectId"
                                placeholder="请选择科目"
                                clearable
                                style="width: 100%"
                                @change="handleSubjectChange"
                            >
                                <el-option
                                    v-for="subject in level1Subjects"
                                    :key="subject.id"
                                    :label="subject.name"
                                    :value="subject.id"
                                />
                            </el-select>
                        </el-form-item>

                        <el-form-item label="知识模块">
                            <el-select
                                v-model="questionSearchForm.knowledgeModuleId"
                                placeholder="请先选择考试科目"
                                clearable
                                style="width: 100%"
                                :disabled="!questionSearchForm.subjectId"
                                @change="handleKnowledgeModuleChange"
                            >
                                <el-option
                                    v-for="module in level2Subjects"
                                    :key="module.id"
                                    :label="module.name"
                                    :value="module.id"
                                />
                            </el-select>
                        </el-form-item>

                        <el-form-item label="知识点">
                            <el-select
                                v-model="questionSearchForm.knowledgePointId"
                                placeholder="请先选择知识模块"
                                clearable
                                style="width: 100%"
                                :disabled="!questionSearchForm.knowledgeModuleId"
                            >
                                <el-option
                                    v-for="point in level3Subjects"
                                    :key="point.id"
                                    :label="point.name"
                                    :value="point.id"
                                />
                            </el-select>
                        </el-form-item>

                        <el-form-item label="题目类型">
                            <el-select v-model="questionSearchForm.type" placeholder="请选择题目类型" clearable style="width: 100%">
                                <el-option
                                    v-for="type in questionTypesList"
                                    :key="type.code"
                                    :label="type.name"
                                    :value="type.code"
                                />
                            </el-select>
                        </el-form-item>

                        <el-form-item label="关键词">
                            <el-input v-model="questionSearchForm.keyword" placeholder="支持LaTeX公式搜索" clearable />
                        </el-form-item>

                        <el-form-item label="标签">
                            <el-input v-model="questionSearchForm.tags" placeholder="多个标签用逗号分隔" clearable />
                        </el-form-item>

                        <el-form-item>
                            <el-button type="primary" icon="Search" @click="searchQuestions" style="width: 100%">搜索</el-button>
                        </el-form-item>
                        <el-form-item>
                            <el-button @click="resetQuestionSearch" style="width: 100%">重置</el-button>
                        </el-form-item>
                    </el-form>
                </div>

                <!-- 右侧：题目列表 -->
                <div class="questions-panel">
                    <div class="panel-header">
                        <div class="panel-title">
                            <el-icon><ListIcon /></el-icon>
                            <span>可选题目（已选中 {{ selectedQuestions.length }} 道）</span>
                        </div>
                    </div>

                    <el-table :data="availableQuestions" v-loading="loadingAvailableQuestions" stripe
                        @selection-change="handleQuestionSelectionChange" max-height="450">
                        <el-table-column type="selection" width="55" align="center" />
                        <el-table-column label="题目内容" min-width="250" show-overflow-tooltip>
                            <template #default="scope">
                                <div class="question-content" v-html="renderLatex(scope.row.content)"></div>
                            </template>
                        </el-table-column>
                        <el-table-column label="分值" width="100" align="center">
                            <template #default="scope">
                                <el-input-number
                                    v-model="scope.row._scoreValue"
                                    :controls="false"
                                    :min="0"
                                    :max="50"
                                    size="small"
                                    :precision="1"
                                    
                                />
                            </template>
                        </el-table-column>
                        <el-table-column label="题号" width="100" align="center">
                            <template #default="scope">
                                <el-input-number
                                    v-model="scope.row._sortOrder"
                                    :controls="false"
                                    :min="1"
                                    size="small"
                                    
                                />
                            </template>
                        </el-table-column>
                        <el-table-column label="题型" width="130" align="center">
                            <template #default="scope">
                                <el-select v-model="scope.row._type" placeholder="选择" size="small" >
                                    <el-option
                                        v-for="type in questionTypesList"
                                        :key="type.code"
                                        :label="type.name"
                                        :value="type.code"
                                    />
                                </el-select>
                            </template>
                        </el-table-column>
                    </el-table>

                    <div class="pagination-container">
                        <el-pagination :current-page="questionPageNum" :page-size="questionPageSize"
                            :page-sizes="[20, 50, 100]" :total="questionTotal" layout="total, sizes, prev, pager, next"
                            @size-change="handleQuestionPageSizeChange" @current-change="handleQuestionPageChange" />
                    </div>
                </div>
            </div>

            <template #footer>
                <el-button @click="addQuestionDialogVisible = false">取消</el-button>
                <el-button type="primary" :loading="addingQuestions" :disabled="selectedQuestions.length === 0"
                    @click="addQuestionsToPaper">
                    添加选中题目 ({{ selectedQuestions.length }})
                </el-button>
            </template>
        </el-dialog>

        <!-- 查看题目对话框 -->
        <el-dialog v-model="viewQuestionsDialogVisible" :title="`试卷题目列表 - ${currentPaper?.title}`" width="1000px"
            destroy-on-close>
            <el-table :data="paperQuestions" v-loading="loadingQuestions" stripe max-height="500">
                <el-table-column prop="id" label="题目ID" width="100" align="center" />
                <el-table-column prop="sortOrder" label="题号" width="80" align="center" />
                <el-table-column prop="scoreValue" label="分值" width="80" align="center" />
                <el-table-column label="题型" width="120" align="center">
                    <template #default="scope">
                        {{ getQuestionTypeName(scope.row.paperType || scope.row.type) }}
                    </template>
                </el-table-column>
                <el-table-column label="题目内容" min-width="300" show-overflow-tooltip>
                    <template #default="scope">
                        <div v-html="renderLatex(scope.row.content)"></div>
                    </template>
                </el-table-column>
                <el-table-column prop="answer" label="答案" width="100" align="center" show-overflow-tooltip />
            </el-table>
        </el-dialog>
    </div>
</template>

<script setup>
import { ref, onMounted, computed, nextTick } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search as SearchIcon, List as ListIcon, Rank } from '@element-plus/icons-vue'
import Sortable from 'sortablejs'
import { getPaperPage, getPaperDetail, addPaper as addPaperApi, updatePaper as updatePaperApi, deletePaper, getPaperQuestions, addQuestionToPaper, removeQuestionFromPaper, updateQuestionOrder } from '@/api/paper'
import { getQuestionTypesBySubject } from '@/api/subject'
import request from '@/utils/request'
import { loadQuestionTypes, getQuestionTypeName } from '@/utils/questionTypes'
import katex from 'katex'

// 数据定义
const loading = ref(false)
const saving = ref(false)
const tableData = ref([])
const total = ref(0)
const pageNum = ref(1)
const pageSize = ref(10)

const dialogVisible = ref(false)
const questionDialogVisible = ref(false)
const addQuestionDialogVisible = ref(false)
const viewQuestionsDialogVisible = ref(false)
const formRef = ref(null)

const searchForm = ref({
    paperType: null,
    examSpecId: null,
    year: null,
    keyword: ''
})

const examSpecs = ref([]) // 用于显示科目名称的完整树
const paperLevel1Subjects = ref([]) // 用于新增试卷选择的Level 1科目

// 添加题目搜索用的科目数据
const level1Subjects = ref([]) // Level 1 考试科目（政治、408等）
const level2Subjects = ref([]) // Level 2 知识模块（马原、毛中特等）
const level3Subjects = ref([]) // Level 3 知识点
const questionTypesList = ref([]) // 题目类型列表
const form = ref({
    id: null,
    title: '',
    paperType: 0,
    examSpecId: null,
    totalScore: 150,
    timeLimit: 180,
    year: new Date().getFullYear()
})

const rules = {
    title: [{ required: true, message: '请输入试卷名称', trigger: 'blur' }],
    paperType: [{ required: true, message: '请选择试卷类型', trigger: 'change' }],
    examSpecId: [{ required: true, message: '请选择考试规格', trigger: 'change' }],
    totalScore: [{ required: true, message: '请输入总分', trigger: 'blur' }],
    timeLimit: [{ required: true, message: '请输入考试时长', trigger: 'blur' }]
}

// 题目管理相关
const currentPaper = ref(null)
const paperQuestions = ref([])
const questionTableRef = ref(null)
const loadingQuestions = ref(false)
const availableQuestions = ref([])
const loadingAvailableQuestions = ref(false)
const addingQuestions = ref(false)
const selectedQuestions = ref([])
const questionSearchForm = ref({
    keyword: '',
    subjectId: null, // Level 1 考试科目
    knowledgeModuleId: null, // Level 2 知识模块
    knowledgePointId: null, // Level 3 知识点
    type: null,
    tags: ''
})

const batchQuestionSettings = ref({
    scoreValue: 5,
    type: '',
    startOrder: 1,
    overrideType: false,
    overrideTypeValue: null
})

const questionPageNum = ref(1)
const questionPageSize = ref(20)
const questionTotal = ref(0)
const allSubjectsFlat = ref([]) // 扁平化的科目列表
const selectedPaperQuestions = ref([]) // 选中的试卷题目

// 获取考试规格列表
const loadExamSpecs = async () => {
    try {
        // 使用 manage-tree 接口获取完整科目树
        const res = await request.get('/subject/manage-tree')
        const treeData = res.data || res || []

        // 保存完整树用于显示科目名称
        examSpecs.value = treeData

        // 提取 level=1 的科目用于新增试卷下拉选择
        // 需要处理两种情况：
        // 1. 直接的 level=1 节点（如政治、408）
        // 2. 虚拟分组（英语、数学）下的 level=1 子节点
        const level1List = []

        treeData.forEach(item => {
            // 情况1：直接是 level=1 的节点
            if (item.level === '1' || item.level === 1) {
                level1List.push(item)
            }
            // 情况2：虚拟分组（id 为负数，如 -2 英语, -3 数学）
            // 提取其 children 中的 level=1 科目
            else if (item.id < 0 && item.children && item.children.length > 0) {
                item.children.forEach(child => {
                    if (child.level === '1' || child.level === 1) {
                        level1List.push(child)
                    }
                })
            }
        })

        paperLevel1Subjects.value = level1List
        level1Subjects.value = level1List // 同时用于搜索

        console.log('加载的科目树:', examSpecs.value)
        console.log('level=1 的科目:', level1Subjects.value)
    } catch (e) {
        ElMessage.error('获取考试规格失败')
        console.error(e)
    }
}

// 根据科目ID获取科目名称（递归查找）
const getSubjectName = (subjectId) => {
    if (!subjectId) return '-'

    // 递归查找函数
    const findSubjectInTree = (nodes, targetId) => {
        if (!nodes || nodes.length === 0) return null

        for (const node of nodes) {
            // 检查当前节点
            if (String(node.id) === String(targetId)) {
                return node
            }

            // 递归检查子节点
            if (node.children && node.children.length > 0) {
                const found = findSubjectInTree(node.children, targetId)
                if (found) return found
            }
        }

        return null
    }

    const subject = findSubjectInTree(examSpecs.value, subjectId)
    return subject ? subject.name : subjectId
}

// LaTeX 公式渲染函数
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

// 加载试卷数据
const loadData = async () => {
    loading.value = true
    try {
        const params = {
            pageNum: pageNum.value,
            pageSize: pageSize.value
        }
        if (searchForm.value.paperType !== null) {
            params.paperType = searchForm.value.paperType
        }
        if (searchForm.value.examSpecId) {
            params.examSpecId = searchForm.value.examSpecId
        }
        if (searchForm.value.year) {
            params.year = searchForm.value.year
        }
        if (searchForm.value.keyword) {
            params.keyword = searchForm.value.keyword
        }

        const res = await getPaperPage(params)
        const data = res.data || res
        tableData.value = data.records || []
        total.value = data.total || 0
    } catch (e) {
        ElMessage.error('获取数据失败')
        console.error(e)
    } finally {
        loading.value = false
    }
}

// 重置搜索
const resetSearch = () => {
    searchForm.value = {
        paperType: null,
        examSpecId: null,
        year: null,
        keyword: ''
    }
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

// 重置表单
const resetForm = () => {
    form.value = {
        id: null,
        title: '',
        paperType: 0,
        examSpecId: null,
        totalScore: 150,
        timeLimit: 180,
        year: new Date().getFullYear()
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
    form.value = {
        id: row.id,
        title: row.title,
        paperType: row.paperType,
        examSpecId: row.examSpecId,
        totalScore: row.totalScore,
        timeLimit: row.timeLimit,
        year: row.year
    }
    dialogVisible.value = true
}

// 保存
const savePaper = async () => {
    await formRef.value?.validate()

    saving.value = true
    try {
        if (form.value.id) {
            await updatePaperApi(form.value)
            ElMessage.success('修改成功')
        } else {
            await addPaperApi(form.value)
            ElMessage.success('添加成功')
        }
        dialogVisible.value = false
        loadData()
    } catch (e) {
        ElMessage.error('保存失败')
        console.error(e)
    } finally {
        saving.value = false
    }
}

// 删除
const handleDelete = (id) => {
    ElMessageBox.confirm('确认删除该试卷吗？删除后将同时删除所有题目关联关系。', '警告', {
        type: 'warning',
        confirmButtonText: '确定',
        cancelButtonText: '取消'
    }).then(async () => {
        try {
            await deletePaper(id)
            ElMessage.success('删除成功')
            loadData()
        } catch (e) {
            ElMessage.error('删除失败')
            console.error(e)
        }
    })
}

// 题目管理
const handleManageQuestions = async (row) => {
    currentPaper.value = row
    questionDialogVisible.value = true
    await loadPaperQuestions(row.id)
}

const loadPaperQuestions = async (paperId) => {
    loadingQuestions.value = true
    try {
        const res = await getPaperQuestions(paperId)
        console.log(res)
        paperQuestions.value = res.data || res || []

        // 等待 DOM 更新后初始化拖拽功能
        await nextTick()
        initSortable()
    } catch (e) {
        ElMessage.error('获取试卷题目失败')
        console.error(e)
    } finally {
        loadingQuestions.value = false
    }
}

// 初始化拖拽排序
let sortableInstance = null

const initSortable = () => {
    if (sortableInstance) {
        sortableInstance.destroy()
    }

    const el = questionTableRef.value?.$el.querySelector('.el-table__body-wrapper tbody')
    if (!el) return

    sortableInstance = Sortable.create(el, {
        animation: 150,
        handle: '.el-table__row', // 整行可拖拽
        ghostClass: 'sortable-ghost',
        chosenClass: 'sortable-chosen',
        dragClass: 'sortable-drag',
        onEnd: async (evt) => {
            const { oldIndex, newIndex } = evt

            if (oldIndex === newIndex) return

            // 更新本地数据
            const movedItem = paperQuestions.value.splice(oldIndex, 1)[0]
            paperQuestions.value.splice(newIndex, 0, movedItem)

            // 更新题号
            paperQuestions.value.forEach((q, index) => {
                q.sortOrder = index + 1
            })

            // 构建更新数据
            const orderMap = {}
            paperQuestions.value.forEach((q, index) => {
                orderMap[q.id] = index + 1
            })

            // 调用后端 API 更新
            try {
                await updateQuestionOrder(currentPaper.value.id, orderMap)
                ElMessage.success('题号调整成功')
            } catch (e) {
                ElMessage.error('题号调整失败')
                console.error(e)
                // 失败后重新加载数据
                await loadPaperQuestions(currentPaper.value.id)
            }
        }
    })
}

const showAddQuestionDialog = () => {
    addQuestionDialogVisible.value = true
    questionSearchForm.value.keyword = ''
    availableQuestions.value = []
    selectedQuestions.value = []
    // 自动加载题目列表
    searchQuestions()
}

const searchQuestions = async () => {
    loadingAvailableQuestions.value = true
    try {
        const params = {
            pageNum: questionPageNum.value,
            pageSize: questionPageSize.value
        }

        // 后端支持的参数：subjectIds (需要转换为字符串)
        // 优先使用最具体的科目筛选
        let subjectId = questionSearchForm.value.knowledgePointId ||
                      questionSearchForm.value.knowledgeModuleId ||
                      questionSearchForm.value.subjectId

        if (subjectId) {
            params.subjectIds = String(subjectId)
        }

        console.log('后端搜索参数:', params)

        const res = await request.get('/question/page', { params })
        const data = res.data || res
        let questions = data.records || []

        // 前端进一步过滤：根据 keyword、type、tags
        if (questionSearchForm.value.keyword && questionSearchForm.value.keyword.trim()) {
            const keyword = questionSearchForm.value.keyword.trim().toLowerCase()
            questions = questions.filter(q => {
                // 搜索题目内容（支持 LaTeX 公式文本）
                const content = (q.content || '').toLowerCase()
                // 搜索标签
                const tags = (q.tags || []).join(' ').toLowerCase()
                // 搜索来源
                const source = (q.source || '').toLowerCase()

                return content.includes(keyword) ||
                       tags.includes(keyword) ||
                       source.includes(keyword)
            })
        }

        if (questionSearchForm.value.type) {
            questions = questions.filter(q => q.type === questionSearchForm.value.type)
        }

        if (questionSearchForm.value.tags && questionSearchForm.value.tags.trim()) {
            const tag = questionSearchForm.value.tags.trim().toLowerCase()
            questions = questions.filter(q => {
                const tags = (q.tags || []).join(' ').toLowerCase()
                return tags.includes(tag)
            })
        }

        // 过滤掉已经在试卷中的题目
        const paperQuestionIds = paperQuestions.value.map(q => q.id)
        availableQuestions.value = questions.filter(q => !paperQuestionIds.includes(q.id))

        questionTotal.value = availableQuestions.value.length // 更新总数

        // 为每道题初始化临时字段（分值、题号、题型）
        availableQuestions.value.forEach((q, index) => {
            if (q._scoreValue === undefined) {
                q._scoreValue = batchQuestionSettings.value.scoreValue
            }
            if (q._sortOrder === undefined || q._sortOrder === null) {
                q._sortOrder = batchQuestionSettings.value.startOrder + index
            }
            if (q._type === undefined) {
                q._type = q.type // 默认使用原题型
            }
        })
    } catch (e) {
        ElMessage.error('搜索题目失败')
        console.error(e)
    } finally {
        loadingAvailableQuestions.value = false
    }
}

// 处理考试科目变化，加载对应的知识模块和题型
const handleSubjectChange = async (subjectId) => {
    // 清空下级选择
    questionSearchForm.value.knowledgeModuleId = null
    questionSearchForm.value.knowledgePointId = null
    questionSearchForm.value.type = null // 清空题型
    level2Subjects.value = []
    level3Subjects.value = []

    if (!subjectId) {
        // 如果清空了科目，恢复显示所有题型
        questionTypesList.value = await loadQuestionTypes()
        return
    }

    // 从科目树中找到该科目，获取其 level=2 的子节点
    const subject = findSubjectInTree(examSpecs.value, subjectId)
    if (subject && subject.children && subject.children.length > 0) {
        level2Subjects.value = subject.children.filter(child =>
            child.level === '2' || child.level === 2
        )
    }

    // 加载该科目支持的题型
    try {
        const res = await getQuestionTypesBySubject(subjectId)
        if (res.code === 200 && res.data) {
            questionTypesList.value = res.data
            console.log(`科目 ${subjectId} 支持的题型:`, res.data)
        }
    } catch (e) {
        console.error('获取题型列表失败:', e)
        // 失败时使用所有题型
        questionTypesList.value = await loadQuestionTypes()
    }
}

// 处理知识模块变化，加载对应的知识点
const handleKnowledgeModuleChange = (moduleId) => {
    // 清空下级选择
    questionSearchForm.value.knowledgePointId = null
    level3Subjects.value = []

    if (!moduleId) return

    // 从科目树中找到该模块，获取其 level=3 的子节点
    const module = findSubjectInTree(examSpecs.value, moduleId)
    if (module && module.children && module.children.length > 0) {
        level3Subjects.value = module.children.filter(child =>
            child.level === '3' || child.level === 3
        )
    }
}

// 在科目树中递归查找科目
const findSubjectInTree = (nodes, targetId) => {
    if (!nodes || nodes.length === 0) return null

    for (const node of nodes) {
        if (String(node.id) === String(targetId)) return node
        if (node.children && node.children.length > 0) {
            const found = findSubjectInTree(node.children, targetId)
            if (found) return found
        }
    }
    return null
}

// 重置搜索条件
const resetQuestionSearch = () => {
    questionSearchForm.value = {
        keyword: '',
        subjectId: null,
        knowledgeModuleId: null,
        knowledgePointId: null,
        type: null,
        tags: ''
    }
    level2Subjects.value = []
    level3Subjects.value = []
    questionPageNum.value = 1
    searchQuestions()
}

// 处理分页变化
const handleQuestionPageChange = (page) => {
    questionPageNum.value = page
    searchQuestions()
}

const handleQuestionPageSizeChange = (size) => {
    questionPageSize.value = size
    questionPageNum.value = 1
    searchQuestions()
}

const handleQuestionSelectionChange = (selection) => {
    selectedQuestions.value = selection
}

const addQuestionsToPaper = async () => {
    if (selectedQuestions.value.length === 0) {
        ElMessage.warning('请选择要添加的题目')
        return
    }

    addingQuestions.value = true
    try {
        const settings = batchQuestionSettings.value

        // 为每道题添加，使用各自的设置或默认设置
        for (let i = 0; i < selectedQuestions.value.length; i++) {
            const question = selectedQuestions.value[i]

            // 使用题目单独设置，如果没有则使用默认设置
            const scoreValue = question._scoreValue !== undefined ? question._scoreValue : settings.scoreValue
            const sortOrder = question._sortOrder !== undefined ? question._sortOrder : (settings.startOrder + i)
            const type = question._type !== undefined ? question._type : (
                settings.overrideType && settings.overrideTypeValue ? settings.overrideTypeValue : question.type
            )

            await addQuestionToPaper(currentPaper.value.id, {
                questionId: question.id,
                sortOrder: sortOrder,
                scoreValue: scoreValue,
                type: type
            })

            console.log(`题目 ${question.id}: 题号=${sortOrder}, 分值=${scoreValue}, 题型=${type}`)
        }

        ElMessage.success(`成功添加 ${selectedQuestions.value.length} 道题目`)
        addQuestionDialogVisible.value = false
        await loadPaperQuestions(currentPaper.value.id)
    } catch (e) {
        ElMessage.error('添加题目失败')
        console.error(e)
    } finally {
        addingQuestions.value = false
    }
}

// 单个移除题目（无确认弹窗）
const removeQuestionDirect = async (row) => {
    try {
        await removeQuestionFromPaper(currentPaper.value.id, row.id)
        ElMessage.success('移除成功')
        await loadPaperQuestions(currentPaper.value.id)
    } catch (e) {
        ElMessage.error('移除失败')
        console.error(e)
    }
}

// 批量移除题目
const batchRemoveQuestions = async () => {
    if (selectedPaperQuestions.value.length === 0) {
        ElMessage.warning('请选择要移除的题目')
        return
    }

    try {
        // 并发删除所有选中的题目
        const removePromises = selectedPaperQuestions.value.map(q =>
            removeQuestionFromPaper(currentPaper.value.id, q.id)
        )
        await Promise.all(removePromises)

        ElMessage.success(`成功移除 ${selectedPaperQuestions.value.length} 道题目`)
        selectedPaperQuestions.value = []
        await loadPaperQuestions(currentPaper.value.id)
    } catch (e) {
        ElMessage.error('批量移除失败')
        console.error(e)
    }
}

// 处理试卷题目选择变化
const handlePaperQuestionSelectionChange = (selection) => {
    selectedPaperQuestions.value = selection
}

const handleViewQuestions = async (row) => {
    currentPaper.value = row
    viewQuestionsDialogVisible.value = true
    await loadPaperQuestions(row.id)
}

const getQuestionTypeLabel = (type) => {
    return getQuestionTypeName(type)
}

onMounted(async () => {
    await loadExamSpecs()
    loadData()
    questionTypesList.value = await loadQuestionTypes() // 加载题目类型列表
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

.header-btns {
    display: flex;
    gap: 10px;
}

/* 搜索表单 */
.search-form {
    background: #fcfcfd;
    padding: 18px 20px 0;
    border-radius: 8px;
    margin-bottom: 20px;
    border: 1px solid #ebeef5;
}

/* 表格 */
.modern-table {
    font-size: 14px;
}

.full-time {
    font-family: 'Monaco', 'Courier New', monospace;
    font-size: 13px;
    color: #475569;
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

/* 题目管理面板 */
.question-manage-content {
    display: flex;
}

.left-panel {
    flex: 1;
}

.panel-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 15px;
    font-size: 16px;
    font-weight: 600;
}

.header-actions {
    display: flex;
    gap: 10px;
}

/* 分页样式 */
:deep(.el-pagination.is-background .el-pager li) {
    background-color: #fff;
    border: 1px solid #dcdfe6;
    border-radius: 6px;
    font-weight: 500;
    transition: all 0.2s;
}

:deep(.el-pagination.is-background .el-pager li:not(.is-disabled).is-active) {
    background-color: #409eff;
    color: #fff;
    border-color: #409eff;
    box-shadow: 0 2px 6px rgba(64, 158, 255, 0.3);
}

:deep(.el-pagination.is-background .el-pager li:hover) {
    color: #409eff;
    border-color: #409eff;
}

:deep(.el-pagination__total),
:deep(.el-pagination__jump) {
    color: #606266;
    font-size: 13px;
}

:deep(.el-input__wrapper) {
    border-radius: 6px !important;
}

/* KaTeX 公式样式优化 */
:deep(.katex) {
    font-size: 1.1em;
}

/* ==================== 试卷题目管理弹窗样式 ==================== */
.question-manage-dialog :deep(.el-dialog__body) {
    padding: 20px;
}

.question-manage-content {
    display: flex;
    gap: 0;
}

.left-panel {
    flex: 1;
}

.panel-title {
    font-size: 16px;
    font-weight: 600;
    color: #1f2f3d;
    display: flex;
    align-items: center;
    gap: 8px;
}

.panel-title .el-icon {
    color: #409eff;
    font-size: 18px;
}

.score-value {
    font-weight: 600;
    color: #409eff;
    font-size: 15px;
}

/* ==================== 添加题目弹窗样式 ==================== */
.add-question-dialog {
    border-radius: 12px;
    overflow: hidden;
}

.add-question-dialog :deep(.el-dialog__header) {
    background: linear-gradient(135deg, #409eff 0%, #53a8ff 100%);
    padding: 20px 24px;
    margin: 0;
}

.add-question-dialog :deep(.el-dialog__title) {
    color: #fff;
    font-size: 18px;
    font-weight: 600;
    letter-spacing: 0.5px;
}

.add-question-dialog :deep(.el-dialog__headerbtn .el-dialog__close) {
    color: #fff;
    font-size: 20px;
}

.add-question-dialog :deep(.el-dialog__headerbtn .el-dialog__close:hover) {
    color: rgba(255, 255, 255, 0.8);
}

.add-question-dialog :deep(.el-dialog__body) {
    padding: 0;
    height: 72vh;
}

.add-question-layout {
    display: flex;
    height: 100%;
    gap: 0;
}

/* ==================== 左侧搜索面板 ==================== */
.search-panel {
    width: 340px;
    background: linear-gradient(180deg, #f8f9fa 0%, #ffffff 100%);
    border-right: 1px solid #e8ecef;
    padding: 24px;
    overflow-y: auto;
    box-shadow: 2px 0 8px rgba(0, 0, 0, 0.04);
}

.search-panel .panel-title {
    display: flex;
    align-items: center;
    gap: 10px;
    font-size: 16px;
    font-weight: 700;
    color: #2c3e50;
    margin-bottom: 24px;
    padding-bottom: 16px;
    border-bottom: 3px solid #409eff;
    position: relative;
}

.search-panel .panel-title::after {
    content: '';
    position: absolute;
    bottom: -3px;
    left: 0;
    width: 60px;
    height: 3px;
    background: linear-gradient(90deg, #409eff 0%, #53a8ff 100%);
    border-radius: 2px;
}

.search-panel .panel-title .el-icon {
    color: #409eff;
    font-size: 22px;
    animation: pulse 2s ease-in-out infinite;
}

@keyframes pulse {
    0%, 100% {
        transform: scale(1);
    }
    50% {
        transform: scale(1.1);
    }
}

.search-panel :deep(.el-form-item) {
    margin-bottom: 24px;
}

.search-panel :deep(.el-form-item__label) {
    font-size: 13px;
    color: #606266;
    font-weight: 600;
    padding: 0 0 12px 0;
    line-height: 1.5;
    display: block;
}

.search-panel :deep(.el-input__wrapper),
.search-panel :deep(.el-select .el-input__wrapper) {
    border-radius: 8px;
    box-shadow: 0 2px 6px rgba(0, 0, 0, 0.04);
    transition: all 0.3s ease;
    border: 1px solid #e0e0e0;
}

.search-panel :deep(.el-input__wrapper:hover),
.search-panel :deep(.el-select .el-input__wrapper:hover) {
    box-shadow: 0 4px 12px rgba(102, 126, 234, 0.15);
    border-color: #409eff;
}

.search-panel :deep(.el-input__wrapper.is-focus),
.search-panel :deep(.el-select .el-input__wrapper.is-focus) {
    box-shadow: 0 4px 16px rgba(102, 126, 234, 0.25);
    border-color: #409eff;
}

.search-panel :deep(.el-button) {
    border-radius: 8px;
    font-weight: 600;
    letter-spacing: 0.5px;
    transition: all 0.3s ease;
}

.search-panel :deep(.el-button--primary) {
    background: linear-gradient(135deg, #409eff 0%, #53a8ff 100%);
    border: none;
    box-shadow: 0 4px 12px rgba(102, 126, 234, 0.3);
}

.search-panel :deep(.el-button--primary:hover) {
    transform: translateY(-2px);
    box-shadow: 0 6px 16px rgba(102, 126, 234, 0.4);
}

.search-panel :deep(.el-button--primary:active) {
    transform: translateY(0);
}

.search-panel :deep(.el-button--default) {
    border-color: #d0d7de;
    color: #57606a;
}

.search-panel :deep(.el-button--default:hover) {
    background: #f3f4f6;
    border-color: #409eff;
    color: #409eff;
}

/* ==================== 右侧题目面板 ==================== */
.questions-panel {
    flex: 1;
    display: flex;
    flex-direction: column;
    padding: 24px;
    background: #ffffff;
}

.questions-panel .panel-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 20px;
    padding-bottom: 16px;
    border-bottom: 2px solid #f0f0f0;
}

.questions-panel .panel-title {
    display: flex;
    align-items: center;
    gap: 10px;
    font-size: 16px;
    font-weight: 700;
    color: #2c3e50;
}

.questions-panel .panel-title .el-icon {
    color: #409eff;
    font-size: 22px;
}

.questions-panel .panel-title span {
    background: linear-gradient(135deg, #409eff 0%, #53a8ff 100%);
    -webkit-background-clip: text;
    -webkit-text-fill-color: transparent;
    background-clip: text;
}

/* ==================== 表格美化 ==================== */
.questions-panel :deep(.el-table) {
    border-radius: 12px;
    overflow: hidden;
    box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
    font-size: 14px;
}

.questions-panel :deep(.el-table__header-wrapper) {
    border-radius: 12px 12px 0 0;
}

.questions-panel :deep(.el-table th.el-table__cell) {
    background: linear-gradient(135deg, #409eff15 0%, #53a8ff15 100%);
    color: #2c3e50;
    font-weight: 700;
    padding: 16px 0;
    border-bottom: 2px solid #e8ecef;
}

.questions-panel :deep(.el-table .el-table__row) {
    transition: all 0.3s ease;
}

.questions-panel :deep(.el-table .el-table__row:hover) {
    background: linear-gradient(90deg, #409eff08 0%, #53a8ff08 100%) !important;
    transform: scale(1.01);
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
}

.questions-panel :deep(.el-table .el-table__cell) {
    padding: 14px 0;
    border-bottom: 1px solid #f0f0f0;
}

.questions-panel :deep(.el-table__row.el-table__row--striped) {
    background: #fafbfc;
}

.questions-panel :deep(.el-checkbox__input.is-checked .el-checkbox__inner) {
    background: linear-gradient(135deg, #409eff 0%, #53a8ff 100%);
    border-color: #409eff;
}

/* ==================== 输入框和选择器美化 ==================== */
:deep(.el-input-number) {
    width: 100%;
}

:deep(.el-input-number .el-input__inner) {
    text-align: center;
    font-weight: 600;
    border-radius: 6px;
}

:deep(.el-select) {
    width: 100%;
}

:deep(.el-select .el-input__inner) {
    font-size: 13px;
    font-weight: 500;
}

:deep(.el-input-number:hover .el-input__inner),
:deep(.el-input-number.is-focus .el-input__inner) {
    border-color: #409eff;
}

/* ==================== 题目内容样式 ==================== */
.question-content {
    font-size: 14px;
    line-height: 1.8;
    color: #2c3e50;
    padding: 4px 0;
}

/* ==================== 分页美化 ==================== */
.pagination-container {
    margin-top: 20px;
    padding: 16px;
    display: flex;
    justify-content: center;
    background: linear-gradient(135deg, #f8f9fa 0%, #ffffff 100%);
    border-radius: 12px;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
}

:deep(.el-pagination.is-background .el-pager li) {
    background-color: #fff;
    border: 1px solid #d0d7de;
    border-radius: 8px;
    font-weight: 600;
    transition: all 0.3s ease;
    margin: 0 4px;
}

:deep(.el-pagination.is-background .el-pager li:not(.is-disabled).is-active) {
    background: linear-gradient(135deg, #409eff 0%, #53a8ff 100%);
    color: #fff;
    border-color: #409eff;
    box-shadow: 0 4px 12px rgba(102, 126, 234, 0.4);
}

:deep(.el-pagination.is-background .el-pager li:hover) {
    color: #409eff;
    border-color: #409eff;
    transform: translateY(-2px);
}

/* ==================== 滚动条美化 ==================== */
.search-panel::-webkit-scrollbar,
.questions-panel::-webkit-scrollbar {
    width: 6px;
}

.search-panel::-webkit-scrollbar-track,
.questions-panel::-webkit-scrollbar-track {
    background: #f1f3f5;
    border-radius: 3px;
}

.search-panel::-webkit-scrollbar-thumb,
.questions-panel::-webkit-scrollbar-thumb {
    background: linear-gradient(180deg, #409eff 0%, #53a8ff 100%);
    border-radius: 3px;
}

.search-panel::-webkit-scrollbar-thumb:hover,
.questions-panel::-webkit-scrollbar-thumb:hover {
    background: linear-gradient(180deg, #53a8ff 0%, #409eff 100%);
}

/* ==================== 底部按钮栏 ==================== */
.add-question-dialog :deep(.el-dialog__footer) {
    background: linear-gradient(180deg, #f8f9fa 0%, #ffffff 100%);
    padding: 16px 24px;
    border-top: 1px solid #e8ecef;
}

.add-question-dialog :deep(.el-dialog__footer .el-button--primary) {
    background: linear-gradient(135deg, #409eff 0%, #53a8ff 100%);
    border: none;
    border-radius: 8px;
    font-weight: 600;
    padding: 12px 32px;
    box-shadow: 0 4px 12px rgba(102, 126, 234, 0.3);
    transition: all 0.3s ease;
}

.add-question-dialog :deep(.el-dialog__footer .el-button--primary:hover) {
    transform: translateY(-2px);
    box-shadow: 0 6px 20px rgba(102, 126, 234, 0.45);
}

.add-question-dialog :deep(.el-dialog__footer .el-button--default) {
    border-radius: 8px;
    font-weight: 600;
    padding: 12px 24px;
}

/* ==================== 响应式调整 ==================== */
@media (max-width: 1400px) {
    .search-panel {
        width: 300px;
        padding: 20px;
    }

    .questions-panel {
        padding: 20px;
    }
}

/* ==================== 加载动画 ==================== */
@keyframes fadeIn {
    from {
        opacity: 0;
        transform: translateY(10px);
    }
    to {
        opacity: 1;
        transform: translateY(0);
    }
}

.questions-panel :deep(.el-table__row) {
    animation: fadeIn 0.3s ease-out;
}


:deep(.katex-display) {
    margin: 10px 0;
    overflow-x: auto;
    overflow-y: hidden;
}

/* 添加题目对话框样式 */
.add-question-container {
    display: flex;
    flex-direction: column;
    gap: 16px;
}

.add-question-container .search-card,
.add-question-container .batch-setting-card,
.add-question-container .questions-list-card {
    /* margin-bottom: 0; */
}

.add-question-container .search-card :deep(.el-card__body),
.add-question-container .batch-setting-card :deep(.el-card__body),
.add-question-container .questions-list-card :deep(.el-card__body) {
    padding: 20px;
}

.add-question-container .questions-list-card .pagination-container {
    margin-top: 20px;
    display: flex;
    justify-content: flex-end;
}

/* ==================== 拖拽排序样式 ==================== */
.sortable-ghost {
    opacity: 0.4;
    background-color: #f0f9ff !important;
}

.sortable-chosen {
    background-color: #e3f2fd !important;
}

.sortable-drag {
    opacity: 0.8;
    background-color: #fff !important;
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15) !important;
}

/* 题目表格的行可拖拽样式 */
.question-manage-content :deep(.el-table__row) {
    cursor: move;
    transition: background-color 0.2s;
}

.question-manage-content :deep(.el-table__row:hover) {
    background-color: #f5f7fa !important;
}

/* 拖拽图标样式 */
.question-manage-content :deep(.el-icon) {
    font-size: 18px;
    color: #909399;
    transition: color 0.2s;
}

.question-manage-content :deep(.el-table__row:hover .el-icon) {
    color: #409eff;
}
</style>
