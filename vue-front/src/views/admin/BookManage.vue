<template>
    <div class="user-manage-container">
        <div class="content-wrapper">
            <el-card shadow="never" class="main-card">
                <template #header>
                    <div class="header-section">
                        <div class="card-header">
                            <span class="title-text">习题册管理中心</span>
                            <div class="header-desc">统一配置考研各科目所需的参考书目与习题资源（共 {{ total }} 本）
                            </div>
                        </div>
                        <el-button type="primary" icon="Plus" @click="handleAdd">新增习题册</el-button>
                    </div>
                </template>

                <!-- 搜索表单 -->
                <el-form :inline="true" :model="searchForm" class="search-form">
                    <el-form-item label="所属科目">
                        <el-tree-select v-model="searchForm.subjectIds" :data="subjects"
                        :props="{ label: 'name', value: 'id', children: 'children' }"
                        multiple collapse-tags
                        collapse-tags-tooltip clearable
                        placeholder="请选择科目" popper-class="subject-tree-popper"
                        check-strictly style="width: 260px">
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
                                            :indeterminate="isNodePartiallySelected(data)"
                                            @change="handleSelectAll(data)" @click.stop>
                                            <span class="all-text">全选</span>
                                        </el-checkbox>
                                    </div>
                                </div>
                            </template>
                        </el-tree-select>
                    </el-form-item>

                    <!-- 查询按钮 -->
                    <el-form-item>
                        <el-button type="primary" icon="Search" @click="loadData">查询</el-button>
                        <el-button icon="Refresh" @click="resetSearch">重置</el-button>
                    </el-form-item>
                </el-form>

                <!-- 数据表格 -->
                <el-table :data="tableData" v-loading="loading" class="modern-table" stripe>
                    <!-- ID 列 -->
                    <el-table-column prop="id" label="ID" width="60" align="center" />
                    <!-- 习题册信息列 -->
                    <el-table-column label="习题册信息" min-width="250">
                        <template #default="scope">
                            <div class="book-info-cell">
                                <div class="nickname">
                                    <el-icon class="book-icon">
                                        <Notebook />
                                    </el-icon>
                                    <span>{{ scope.row.name }}</span>
                                </div>
                                <div class="book-description">{{ scope.row.description || '暂无描述' }}</div>
                            </div>
                        </template>
                    </el-table-column>

                    <el-table-column label="关联科目" min-width="200">
                        <template #default="scope">
                            <div v-if="scope.row.subjectNames?.length" class="tags-container">
                                <el-tag v-for="name in scope.row.subjectNames" :key="name" size="small"
                                    class="subject-tag" effect="light">
                                    {{ name }}
                                </el-tag>
                            </div>
                            <span v-else class="text-muted">未关联</span>
                        </template>
                    </el-table-column>

                    <el-table-column label="录入时间" width="160" align="center">
                        <template #default="scope">
                            <div class="full-time">
                                {{ formatDateTime(scope.row.createTime) }}
                            </div>
                        </template>
                    </el-table-column>

                    <el-table-column label="操作" width="150" align="center" fixed="right">
                        <template #default="scope">
                            <el-button size="small" type="primary" link @click="handleEdit(scope.row)">
                                编辑
                            </el-button>
                            <el-button size="small" type="danger" link @click="handleDelete(scope.row.id)">
                                删除
                            </el-button>
                        </template>
                    </el-table-column>
                </el-table>

                <!-- 分页 -->
                <div class="pagination-container">
                    <el-pagination background :current-page="pageNum" :page-size="pageSize"
                        layout="total, prev, pager, next" :total="total" @current-change="handlePageChange" />
                </div>
            </el-card>
        </div>

        <!-- 编辑对话框 -->
        <el-dialog v-model="dialogVisible" :title="form.id ? '编辑习题册' : '新增习题册'" width="550px" destroy-on-close
            class="stats-drawer">
            <el-form :model="form" :rules="rules" ref="formRef" label-position="top">
                <el-form-item label="习题册名称" prop="name">
                    <el-input v-model="form.name" placeholder="例如：张宇1000题" />
                </el-form-item>

                <el-form-item label="详细描述" prop="description">
                    <el-input v-model="form.description" type="textarea" :rows="3" placeholder="习题册的版本或核心介绍" />
                </el-form-item>

                <el-form-item label="关联科目" prop="subjectIds">
                    <el-tree-select v-model="form.subjectIds" :data="subjectTreeForDialog"
                    :props="{ label: 'name', value: 'id', children: 'children' }"
                    multiple collapse-tags
                    collapse-tags-tooltip clearable
                    placeholder="请选择科目" popper-class="subject-tree-popper"
                    check-strictly style="width: 100%">
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
                                    <el-checkbox :model-value="isDialogNodeFullySelected(data)"
                                        :indeterminate="isDialogNodePartiallySelected(data)"
                                        @change="handleDialogSelectAll(data)" @click.stop>
                                        <span class="all-text">全选</span>
                                    </el-checkbox>
                                </div>
                            </div>
                        </template>
                    </el-tree-select>
                </el-form-item>
            </el-form>
            <template #footer>
                <div class="dialog-footer">
                    <el-button @click="dialogVisible = false">取消</el-button>
                    <el-button type="primary" :loading="saving" @click="saveBook">确认提交</el-button>
                </div>
            </template>
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
const formRef = ref(null)

const searchForm = ref({ subjectIds: [] })
const subjects = ref([])
const subjectTreeForDialog = ref([])

const form = ref({
    id: null,
    subjectIds: [],
    name: '',
    description: ''
})

const rules = {
    name: [{ required: true, message: '请输入习题册名称', trigger: 'blur' }]
}

// 时间格式化函数
const formatDateTime = (timeStr) => {
    if (!timeStr) return '-';
    // 处理 ISO 格式 (2026-01-01T01:18:43) -> 2026-01-01 01:18:43
    return timeStr.replace('T', ' ').split('.')[0];
}


// 获取科目树
const loadSubjects = async () => {
    try {
        const res = await request.get('/subject/manage-tree')
        subjects.value = res.data || res || []
        // 为对话框创建独立的数据源副本
        subjectTreeForDialog.value = JSON.parse(JSON.stringify(subjects.value))
        console.log('科目树:', subjects.value)
    } catch (e) {

        ElMessage.error('获取科目列表失败')
        console.error(e)
    }
}
// 全选/取消全选处理（搜索区域）
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

// 检测是否全选（搜索区域）
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

// 检测是否部分选中（搜索区域）
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

// 全选/取消全选处理（对话框）
const handleDialogSelectAll = (node) => {
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
    const selectedSet = new Set(form.value.subjectIds)

    if (selectedSet.has(node.id)) {
        // 取消全选：移除该节点及其所有子节点
        allIds.forEach(id => selectedSet.delete(id))
    } else {
        // 全选：添加该节点及其所有子节点
        allIds.forEach(id => selectedSet.add(id))
    }

    form.value.subjectIds = Array.from(selectedSet)
}

// 检测是否全选（对话框）
const isDialogNodeFullySelected = (node) => {
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
    const selectedSet = new Set(form.value.subjectIds)
    return allIds.every(id => selectedSet.has(id))
}

// 检测是否部分选中（对话框）
const isDialogNodePartiallySelected = (node) => {
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
    const selectedSet = new Set(form.value.subjectIds)
    const selectedCount = allIds.filter(id => selectedSet.has(id)).length
    return selectedCount > 0 && selectedCount < allIds.length
}

// 加载习题册数据
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

        console.log('请求参数:', params)
        const res = await request.get('/book/page', { params })
        const data = res.data || res
        const books = data.records || []
        console.log('原始习题册数据:', books)

        // 后端已经返回了subjectNames，无需再次请求
        tableData.value = books
        total.value = data.total || 0
        console.log('习题册数据:', tableData.value)
    } catch (e) {
        ElMessage.error('获取数据失败')
        console.error('错误详情:', e)
    } finally {
        loading.value = false
    }
}

// 重置搜索
const resetSearch = () => {
    searchForm.value = { subjectId: null }
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
        name: '',
        description: '',
        subjectIds: []
    }
    formRef.value?.clearValidate()
}

// 新增
const handleAdd = () => {
    resetForm()
    dialogVisible.value = true
}

// 编辑
const handleEdit = async (row) => {
    console.log('编辑习题册:', row)
    try {
        const res = await request.get(`/book/${row.id}`)
        const book = res.data

        form.value = {
            id: book.id,
            name: book.name || '',
            description: book.description || '',
            subjectIds: book.subjectIds || []
        }
        dialogVisible.value = true
        console.log('编辑表单数据:', form.value)
    } catch (e) {
        ElMessage.error('获取习题册详情失败')
        console.error(e)
    }
}

// 保存
const saveBook = async () => {
    await formRef.value?.validate()

    saving.value = true
    try {
        const url = form.value.id ? '/book/update' : '/book/add'
        console.log('保存习题册:', form.value)
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
    ElMessageBox.confirm('确认删除该习题册吗？删除后将同时删除所有关联关系。', '警告', {
        type: 'warning',
        confirmButtonText: '确定',
        cancelButtonText: '取消'
    }).then(async () => {
        try {
            await request.delete(`/book/delete/${id}`)
            ElMessage.success('删除成功')
            loadData()
        } catch (e) {
            ElMessage.error('删除失败')
            console.error(e)
        }
    })
}

onMounted(() => {
    console.log('页面已挂载')
    loadSubjects()
    loadData()
})
</script>


<style scoped>
.main-card {
    border: none;
    border-radius: 8px;
}

/* 顶部标题样式 */
.header-section {
    display: flex;
    justify-content: space-between;
    align-items: center;
    /* margin-bottom: 24px;
    padding: 10px 5px; */
}

:deep(.el-card__header) {
    padding-bottom: 15px;
}

.card-header {
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

/* 搜索区域 */
.search-form {
    background: #fcfcfd;
    padding: 18px 20px 0;
    border-radius: 8px;
    margin-bottom: 20px;
    border: 1px solid #ebeef5;
}

:deep(.el-form--inline .el-form-item) {
    margin-right: 24px;
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

/* 表格内部样式 */
.modern-table {
    margin-bottom: 20px;
}

.book-info-cell {
    display: flex;
    flex-direction: column;
    gap: 4px;
}

.nickname {
    font-weight: 600;
    color: #303133;
    display: flex;
    align-items: center;
    gap: 8px;
}

.book-icon {
    font-size: 16px;
    color: #409eff;
    background: #ecf5ff;
    padding: 6px;
    border-radius: 6px;
    box-sizing: content-box;
}

.book-description {
    font-size: 12px;
    color: #909399;
    display: -webkit-box;
    -webkit-line-clamp: 1;
    line-clamp: 1;
    -webkit-box-orient: vertical;
    overflow: hidden;
}

.tags-container {
    display: flex;
    flex-wrap: wrap;
    gap: 6px;
}

.subject-tag {
    border-radius: 4px;
}

/* 注册日期文字 */
.full-time {
    font-family: 'Monaco', 'Courier New', monospace;
    font-size: 13px;
    color: #475569;
}

.text-muted {
    color: #c0c4cc;
    font-size: 13px;
}

/* 分页居中或靠右 */
.pagination-container {
    display: flex;
    justify-content: flex-end;
    margin-top: 20px;
}

:deep(.el-table th) {
    background-color: #f8f9fb;
    color: #606266;
    font-weight: 600;
}
</style>