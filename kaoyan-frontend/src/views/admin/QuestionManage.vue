<template>
    <div class="admin-container">
        <el-card shadow="never" class="table-card">
            <template #header>
                <div class="card-header">
                    <span class="title-text">题目管理中心</span>
                    <div class="header-btns">
                        <el-button type="success" icon="Download" @click="exportToExcel">导出 CSV</el-button>
                        <el-button type="primary" icon="Plus" @click="handleAdd">新增题目</el-button>
                    </div>
                </div>
            </template>

            <!-- 搜索表单 -->
            <el-form :inline="true" :model="searchForm" class="search-form">
                <el-form-item label="所属科目">
                    <el-select v-model="searchForm.subjectId" placeholder="请选择科目" clearable style="width: 200px" @change="loadData">
                        <el-option label="考研政治" :value="1" />
                        <el-option label="考研英语一" :value="2" />
                        <el-option label="考研数学一" :value="3" />
                        <el-option label="计算机 408" :value="4" />
                    </el-select>
                </el-form-item>
                <el-form-item label="习题册">
                    <el-select v-model="searchForm.bookId" placeholder="请选择习题册" clearable style="width: 200px" @change="loadData">
                        <el-option v-for="book in books" :key="book.id" :label="book.name" :value="book.id" />
                    </el-select>
                </el-form-item>
                <el-form-item>
                    <el-button type="primary" icon="Search" @click="loadData">查询</el-button>
                    <el-button icon="Refresh" @click="resetSearch">重置</el-button>
                </el-form-item>
            </el-form>

            <!-- 数据表格 -->
            <el-table :data="tableData" border stripe v-loading="loading" class="custom-table">
                <el-table-column prop="id" label="ID" width="70" align="center" />
                <el-table-column prop="content" label="题干内容" show-overflow-tooltip min-width="200" />
                <el-table-column label="科目" width="120" align="center">
                    <template #default="scope">
                        <el-tag v-if="scope.row.subjectName" size="small">{{ scope.row.subjectName }}</el-tag>
                        <el-tag v-else size="small" type="info">未关联</el-tag>
                    </template>
                </el-table-column>
                <el-table-column label="书本" width="120" align="center">
                    <template #default="scope">
                        <el-tag v-if="scope.row.bookName" size="small" type="success">{{ scope.row.bookName }}</el-tag>
                        <el-tag v-else size="small" type="info">未关联</el-tag>
                    </template>
                </el-table-column>
                <el-table-column prop="type" label="类型" width="80" align="center">
                    <template #default="scope">
                        <el-tag :type="scope.row.type === 1 ? '' : 'success'">
                            {{ scope.row.type === 1 ? '单选' : scope.row.type === 2 ? '多选' : '其他' }}
                        </el-tag>
                    </template>
                </el-table-column>
                <el-table-column prop="difficulty" label="难度" width="100" align="center">
                    <template #default="scope">
                        <el-rate v-model="scope.row.difficulty" disabled show-score />
                    </template>
                </el-table-column>
                <el-table-column prop="answer" label="答案" width="80" align="center" />
                <el-table-column label="操作" width="180" align="center" fixed="right">
                    <template #default="scope">
                        <el-button size="small" type="primary" link @click="handleEdit(scope.row)">编辑</el-button>
                        <el-button size="small" type="info" link @click="handleView(scope.row)">查看</el-button>
                        <el-button size="small" type="danger" link @click="handleDelete(scope.row.id)">删除</el-button>
                    </template>
                </el-table-column>
            </el-table>

            <!-- 分页 -->
            <div class="pagination-container">
                <el-pagination
                    :current-page="pageNum"
                    :page-size="pageSize"
                    :page-sizes="[10, 20, 50, 100]"
                    layout="total, sizes, prev, pager, next, jumper"
                    :total="total"
                    @size-change="handleSizeChange"
                    @current-change="handlePageChange"
                />
            </div>
        </el-card>

        <!-- 编辑/新增对话框 -->
        <el-dialog v-model="dialogVisible" :title="form.id ? '编辑题目' : '新增题目'" width="800px" destroy-on-close @close="resetForm">
            <el-form :model="form" :rules="rules" ref="formRef" label-width="100px">
                <el-row :gutter="20">
                    <el-col :span="12">
                        <el-form-item label="题目类型" prop="type">
                            <el-select v-model="form.type" placeholder="请选择题目类型" style="width: 100%">
                                <el-option label="单选题" :value="1" />
                                <el-option label="多选题" :value="2" />
                                <el-option label="填空题" :value="3" />
                                <el-option label="简答题" :value="4" />
                            </el-select>
                        </el-form-item>

                        <el-form-item label="难度" prop="difficulty">
                            <el-rate v-model="form.difficulty" show-score />
                        </el-form-item>

                        <el-form-item label="题目标签" prop="tags">
                            <el-select v-model="form.tags" multiple filterable allow-create placeholder="请输入标签" style="width: 100%">
                                <el-option v-for="tag in tagOptions" :key="tag" :label="tag" :value="tag" />
                            </el-select>
                        </el-form-item>

                        <el-form-item label="题目来源" prop="source">
                            <el-input v-model="form.source" placeholder="如：2018年考研真题" />
                        </el-form-item>
                    </el-col>

                    <el-col :span="12">
                        <el-form-item label="题干内容" prop="content">
                            <el-input v-model="form.content" type="textarea" :rows="4" placeholder="支持LaTeX公式" />
                        </el-form-item>

                        <template v-if="form.type === 1 || form.type === 2">
                            <el-form-item label="选项设置" prop="options">
                                <div v-for="(opt, index) in form.options" :key="index" class="option-item">
                                    <el-input v-model="form.options[index]" placeholder="请输入选项内容">
                                        <template #prepend>{{ String.fromCharCode(65 + index) }}</template>
                                        <template #append>
                                            <el-button icon="Delete" @click="removeOption(index)" :disabled="form.options.length <= 2" />
                                        </template>
                                    </el-input>
                                </div>
                                <el-button v-if="form.options.length < 8" type="success" size="small" icon="Plus" @click="addOption" style="margin-top: 8px">
                                    添加选项
                                </el-button>
                            </el-form-item>
                        </template>

                        <el-form-item label="正确答案" prop="answer">
                            <el-input v-model="form.answer" :placeholder="form.type === 1 ? '如：A' : form.type === 2 ? '如：AB' : '请输入答案'" />
                        </el-form-item>

                        <el-form-item label="解析" prop="analysis">
                            <el-input v-model="form.analysis" type="textarea" :rows="3" placeholder="支持LaTeX公式" />
                        </el-form-item>
                    </el-col>
                </el-row>
            </el-form>
            <template #footer>
                <el-button @click="dialogVisible = false">取消</el-button>
                <el-button type="primary" :loading="saving" @click="saveQuestion">确定保存</el-button>
            </template>
        </el-dialog>

        <!-- 查看详情对话框 -->
        <el-dialog v-model="viewDialogVisible" title="题目详情" width="700px">
            <el-descriptions :column="1" border v-if="viewQuestion">
                <el-descriptions-item label="题干内容">{{ viewQuestion.content }}</el-descriptions-item>
                <el-descriptions-item v-if="viewQuestion.options && viewQuestion.options.length" label="选项">
                    <div v-for="(opt, index) in viewQuestion.options" :key="index">
                        {{ String.fromCharCode(65 + index) }}. {{ opt }}
                    </div>
                </el-descriptions-item>
                <el-descriptions-item label="正确答案">{{ viewQuestion.answer }}</el-descriptions-item>
                <el-descriptions-item label="解析">{{ viewQuestion.analysis || '暂无解析' }}</el-descriptions-item>
                <el-descriptions-item label="难度">{{ viewQuestion.difficulty }}分</el-descriptions-item>
                <el-descriptions-item label="标签">{{ Array.isArray(viewQuestion.tags) ? viewQuestion.tags.join(', ') : '无' }}</el-descriptions-item>
                <el-descriptions-item label="来源">{{ viewQuestion.source || '未知' }}</el-descriptions-item>
            </el-descriptions>
        </el-dialog>
    </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import request from '@/utils/request'
import { ElMessage, ElMessageBox } from 'element-plus'

// 数据定义
const loading = ref(false)
const saving = ref(false)
const tableData = ref([])
const total = ref(0)
const pageNum = ref(1)
const pageSize = ref(10)
const dialogVisible = ref(false)
const viewDialogVisible = ref(false)
const formRef = ref(null)

const searchForm = ref({ subjectId: null, bookId: null })
const books = ref([])
const viewQuestion = ref(null)

const form = ref({
    id: null,
    type: 1,
    content: '',
    options: ['', '', '', ''],
    answer: '',
    analysis: '',
    difficulty: 3,
    tags: [],
    source: ''
})

const tagOptions = ref(['考研真题', '模拟题', '易错题', '重点', '基础', '进阶'])

const rules = {
    type: [{ required: true, message: '请选择题目类型', trigger: 'change' }],
    content: [{ required: true, message: '请输入题干内容', trigger: 'blur' }],
    answer: [{ required: true, message: '请输入正确答案', trigger: 'blur' }],
    difficulty: [{ required: true, message: '请选择难度', trigger: 'change' }]
}

// 获取书本列表
const loadBooks = async () => {
    try {
        const res = await request.get('/book/list')
        books.value = res.data || res || []
        console.log('书本列表:', books.value)
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
        if (searchForm.value.subjectId) {
            params.subjectId = searchForm.value.subjectId
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
    searchForm.value = { subjectId: null, bookId: null }
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

// 选项操作
const addOption = () => {
    if (form.value.options.length < 8) {
        form.value.options.push('')
    }
}

const removeOption = (index) => {
    if (form.value.options.length > 2) {
        form.value.options.splice(index, 1)
    }
}

// 重置表单
const resetForm = () => {
    form.value = {
        id: null,
        type: 1,
        content: '',
        options: ['', '', '', ''],
        answer: '',
        analysis: '',
        difficulty: 3,
        tags: [],
        source: ''
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
        type: row.type,
        content: row.content || '',
        options: Array.isArray(row.options) ? [...row.options] : ['', '', '', ''],
        answer: row.answer || '',
        analysis: row.analysis || '',
        difficulty: row.difficulty || 3,
        tags: Array.isArray(row.tags) ? [...row.tags] : [],
        source: row.source || ''
    }
    dialogVisible.value = true
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
        console.log('保存题目:', form.value)
        await request.post(url, form.value)
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
        csv += `${row.id},${getTypeLabel(row.type)},${row.subjectName || '未关联'},${row.bookName || '未关联'},${(row.content || '').replace(/,/g, '，')},${row.answer || ''},${row.difficulty || 0}\n`
    })
    const blob = new Blob([csv], { type: 'text/csv;charset=utf-8;' })
    const url = window.URL.createObjectURL(blob)
    const a = document.createElement('a')
    a.href = url
    a.download = `题目导出_${new Date().toISOString().slice(0, 10)}.csv`
    a.click()
    window.URL.revokeObjectURL(url)
}

onMounted(() => {
    console.log('页面已挂载')
    loadBooks()
    loadData()
})
</script>

<style scoped>
.admin-container {
    padding: 16px;
    background-color: #f5f7f9;
    min-height: calc(100vh - 120px);
}

.table-card {
    border-radius: 8px;
    border: none;
}

.card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
}

.title-text {
    font-size: 16px;
    font-weight: 600;
    color: #303133;
}

.search-form {
    background: #fff;
    padding: 12px 0;
    margin-bottom: 8px;
}

.custom-table {
    width: 100% !important;
    margin-top: 10px;
}

.pagination-container {
    margin-top: 20px;
    display: flex;
    justify-content: flex-end;
}

.option-item {
    margin-bottom: 12px;
}

:deep(.el-form-item) {
    margin-bottom: 18px;
}

:deep(.el-table) {
    font-size: 13px;
}

:deep(.el-table th) {
    background-color: #f5f7fa;
    font-weight: 600;
}
</style>
