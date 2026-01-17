<template>
    <div class="admin-container">
        <el-card shadow="never" class="export-card">
            <template #header>
                <div class="card-header">
                    <div class="text-header">
                        <span class="title-text">题目导出为 PDF</span>
                        <div class="header-desc">选择题目并导出为 PDF 文件</div>
                    </div>
                </div>
            </template>

            <!-- 选择条件 -->
            <div class="filter-section">
                <h3>第一步：选择导出条件</h3>

                <el-form :model="filterForm" label-width="100px" class="filter-form">
                    <el-form-item label="导出方式">
                        <el-radio-group v-model="exportMode" @change="handleModeChange">
                            <el-radio value="subject">按科目导出</el-radio>
                            <el-radio value="book">按习题册导出</el-radio>
                            <el-radio value="custom">自定义选择</el-radio>
                        </el-radio-group>
                    </el-form-item>

                    <!-- 按科目导出 -->
                    <el-form-item v-if="exportMode === 'subject'" label="选择科目">
                        <el-cascader
                            v-model="filterForm.subjectId"
                            :options="subjectTree"
                            :props="cascaderProps"
                            placeholder="请选择科目"
                            style="width: 100%"
                            clearable
                            filterable
                            @change="loadSubjectQuestions"
                        />
                    </el-form-item>

                    <!-- 按习题册导出 -->
                    <el-form-item v-if="exportMode === 'book'" label="选择习题册">
                        <el-select
                            v-model="filterForm.bookId"
                            placeholder="请选择习题册"
                            style="width: 100%"
                            filterable
                            @change="loadBookQuestions"
                        >
                            <el-option
                                v-for="book in allBooks"
                                :key="book.id"
                                :label="book.name"
                                :value="book.id"
                            />
                        </el-select>
                    </el-form-item>

                    <!-- 自定义选择 -->
                    <el-form-item v-if="exportMode === 'custom'" label="搜索题目">
                        <div class="custom-search">
                            <el-input
                                v-model="searchKeyword"
                                placeholder="输入题目关键词搜索..."
                                clearable
                                style="width: 300px; margin-right: 10px"
                                @keyup.enter="searchQuestions"
                            >
                                <template #append>
                                    <el-button icon="Search" @click="searchQuestions" />
                                </template>
                            </el-input>
                        </div>
                    </el-form-item>

                    <el-form-item label="包含答案">
                        <el-switch v-model="filterForm.includeAnswers" />
                        <span class="form-tip">⚠️ 开启后将在 PDF 中显示答案和解析</span>
                    </el-form-item>
                </el-form>
            </div>

            <el-divider />

            <!-- 题目列表 -->
            <div class="questions-section">
                <div class="section-header">
                    <h3>第二步：选择题目 ({{ selectedQuestions.length }} 道题)</h3>
                    <div class="header-actions">
                        <el-button size="small" @click="selectAll">全选</el-button>
                        <el-button size="small" @click="unselectAll">取消全选</el-button>
                        <el-tag type="info">已选 {{ selectedQuestions.length }} / {{ filteredQuestions.length }} 题</el-tag>
                    </div>
                </div>

                <div v-loading="loading" class="questions-list">
                    <el-checkbox-group v-model="selectedQuestions">
                        <div class="question-cards">
                            <el-card
                                v-for="question in paginatedQuestions"
                                :key="question.id"
                                shadow="hover"
                                class="question-card"
                                :class="{ 'is-selected': selectedQuestions.includes(question.id) }"
                            >
                                <div class="question-header">
                                    <el-checkbox :label="question.id" class="question-checkbox">
                                        <span class="question-number">题号: {{ question.id }}</span>
                                    </el-checkbox>
                                    <el-tag :type="getTypeColor(question.type)" size="small">
                                        {{ getTypeName(question.type) }}
                                    </el-tag>
                                </div>

                                <div class="question-content">
                                    <div class="question-text">{{ question.content }}</div>

                                    <div v-if="question.options && question.options.length > 0" class="question-options">
                                        <div v-for="(opt, idx) in question.options" :key="idx" class="option-item">
                                            {{ opt }}
                                        </div>
                                    </div>
                                </div>
                            </el-card>
                        </div>
                    </el-checkbox-group>

                    <!-- 分页 -->
                    <div v-if="filteredQuestions.length > pageSize" class="pagination-container">
                        <el-pagination
                            :current-page="currentPage"
                            :page-size="pageSize"
                            :total="filteredQuestions.length"
                            layout="prev, pager, next, jumper"
                            @current-change="handlePageChange"
                        />
                    </div>

                    <el-empty v-if="!loading && filteredQuestions.length === 0" description="暂无题目数据">
                        <el-button type="primary" @click="resetFilters">重置条件</el-button>
                    </el-empty>
                </div>
            </div>

            <el-divider />

            <!-- 预览和导出 -->
            <div class="preview-section">
                <h3>第三步：预览和导出</h3>

                <div class="preview-info">
                    <el-descriptions :column="3" border>
                        <el-descriptions-item label="导出题目数量">
                            <el-tag type="primary" size="large">{{ selectedQuestions.length }} 题</el-tag>
                        </el-descriptions-item>
                        <el-descriptions-item label="包含答案">
                            <el-tag :type="filterForm.includeAnswers ? 'success' : 'info'" size="large">
                                {{ filterForm.includeAnswers ? '是' : '否' }}
                            </el-tag>
                        </el-descriptions-item>
                        <el-descriptions-item label="预计页数">
                            约 {{ estimatedPages }} 页
                        </el-descriptions-item>
                    </el-descriptions>
                </div>

                <div class="export-actions">
                    <el-button
                        type="primary"
                        size="large"
                        icon="Download"
                        :disabled="selectedQuestions.length === 0"
                        :loading="exporting"
                        @click="handleExport"
                    >
                        导出 PDF
                    </el-button>
                    <el-button size="large" icon="Refresh" @click="resetFilters">
                        重置
                    </el-button>
                </div>
            </div>
        </el-card>
    </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { exportToPDF, getSubjectTree, getAllBooks, getQuestionsBySubject } from '@/api/questionImportExport'
import request from '@/utils/request'

// 数据
const loading = ref(false)
const exporting = ref(false)
const exportMode = ref('subject')
const subjectTree = ref([])
const allBooks = ref([])
const filteredQuestions = ref([])
const selectedQuestions = ref([])

// 表单
const filterForm = ref({
    subjectId: null,
    bookId: null,
    includeAnswers: false
})

// 搜索
const searchKeyword = ref('')

// 分页
const currentPage = ref(1)
const pageSize = ref(10)

// 级联选择器配置
const cascaderProps = {
    value: 'id',
    label: 'name',
    children: 'children',
    checkStrictly: false,
    emitPath: false
}

// 当前页的题目
const paginatedQuestions = computed(() => {
    const start = (currentPage.value - 1) * pageSize.value
    const end = start + pageSize.value
    return filteredQuestions.value.slice(start, end)
})

// 预计页数
const estimatedPages = computed(() => {
    return Math.ceil(selectedQuestions.value.length / 5)
})

// 加载初始数据
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

// 导出模式变更
const handleModeChange = () => {
    filteredQuestions.value = []
    selectedQuestions.value = []
    filterForm.value = {
        subjectId: null,
        bookId: null,
        includeAnswers: filterForm.value.includeAnswers
    }
    currentPage.value = 1
}

// 按科目加载题目
const loadSubjectQuestions = async () => {
    if (!filterForm.value.subjectId) {
        filteredQuestions.value = []
        return
    }

    loading.value = true
    try {
        const res = await getQuestionsBySubject(filterForm.value.subjectId, null)
        if (res.code === 200) {
            filteredQuestions.value = res.data || []
            selectedQuestions.value = []
        }
    } catch (e) {
        console.error('加载题目失败', e)
        ElMessage.error('加载题目失败')
    } finally {
        loading.value = false
    }
}

// 按习题册加载题目
const loadBookQuestions = async () => {
    if (!filterForm.value.bookId) {
        filteredQuestions.value = []
        return
    }

    loading.value = true
    try {
        const res = await getQuestionsBySubject(null, filterForm.value.bookId)
        if (res.code === 200) {
            filteredQuestions.value = res.data || []
            selectedQuestions.value = []
        }
    } catch (e) {
        console.error('加载题目失败', e)
        ElMessage.error('加载题目失败')
    } finally {
        loading.value = false
    }
}

// 搜索题目
const searchQuestions = async () => {
    if (!searchKeyword.value.trim()) {
        ElMessage.warning('请输入搜索关键词')
        return
    }

    loading.value = true
    try {
        const res = await request.get('/question/search', {
            params: { keyword: searchKeyword.value }
        })

        if (res.code === 200) {
            filteredQuestions.value = res.data || []
            selectedQuestions.value = []
        }
    } catch (e) {
        console.error('搜索失败', e)
        ElMessage.error('搜索失败')
    } finally {
        loading.value = false
    }
}

// 全选
const selectAll = () => {
    selectedQuestions.value = filteredQuestions.value.map(q => q.id)
}

// 取消全选
const unselectAll = () => {
    selectedQuestions.value = []
}

// 分页变更
const handlePageChange = (page) => {
    currentPage.value = page
}

// 重置条件
const resetFilters = () => {
    exportMode.value = 'subject'
    filterForm.value = {
        subjectId: null,
        bookId: null,
        includeAnswers: false
    }
    searchKeyword.value = ''
    filteredQuestions.value = []
    selectedQuestions.value = []
    currentPage.value = 1
}

// 导出 PDF
const handleExport = async () => {
    if (selectedQuestions.value.length === 0) {
        ElMessage.warning('请至少选择一道题目')
        return
    }

    await ElMessageBox.confirm(
        `确定要导出 ${selectedQuestions.value.length} 道题目为 PDF 吗？`,
        '确认导出',
        {
            confirmButtonText: '确定',
            cancelButtonText: '取消',
            type: 'warning'
        }
    )

    exporting.value = true
    try {
        const res = await exportToPDF({
            questionIds: selectedQuestions.value,
            includeAnswers: filterForm.value.includeAnswers
        })

        if (res.code === 200) {
            // 下载 PDF
            const link = document.createElement('a')
            link.href = `/${res.data}`
            link.download = `questions_${Date.now()}.pdf`
            link.click()
            ElMessage.success('PDF 导出成功')
        } else {
            ElMessage.error(res.msg || '导出失败')
        }
    } catch (e) {
        console.error('导出失败', e)
        ElMessage.error('导出失败：' + e.message)
    } finally {
        exporting.value = false
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

// 获取题目类型颜色
const getTypeColor = (type) => {
    const colors = {
        1: 'primary',
        2: 'success',
        3: 'warning',
        4: 'danger'
    }
    return colors[type] || 'info'
}

onMounted(() => {
    loadData()
})
</script>

<style scoped>
.export-card {
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

.filter-section,
.questions-section,
.preview-section {
    padding: 20px 0;
}

.filter-section h3,
.questions-section h3,
.preview-section h3 {
    font-size: 16px;
    font-weight: 600;
    color: #303133;
    margin-bottom: 20px;
}

.filter-form {
    max-width: 600px;
}

.form-tip {
    margin-left: 15px;
    font-size: 13px;
    color: #909399;
}

.section-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 20px;
}

.header-actions {
    display: flex;
    gap: 10px;
    align-items: center;
}

.questions-list {
    min-height: 300px;
}

.question-cards {
    display: grid;
    grid-template-columns: 1fr;
    gap: 15px;
    margin-bottom: 20px;
}

.question-card {
    border-radius: 8px;
    transition: all 0.3s;
}

.question-card.is-selected {
    border-color: #409eff;
    box-shadow: 0 0 0 1px #409eff;
}

.question-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 12px;
}

.question-checkbox {
    width: 100%;
}

.question-number {
    font-weight: 600;
    color: #303133;
}

.question-content {
    padding-left: 24px;
}

.question-text {
    color: #606266;
    line-height: 1.6;
    margin-bottom: 10px;
}

.question-options {
    padding-left: 15px;
}

.option-item {
    color: #909399;
    line-height: 1.8;
    font-size: 14px;
}

.pagination-container {
    display: flex;
    justify-content: center;
    padding: 20px 0;
}

.preview-info {
    margin-bottom: 20px;
}

.export-actions {
    display: flex;
    gap: 15px;
}
</style>
