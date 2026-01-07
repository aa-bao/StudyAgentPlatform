<template>
    <div class="user-manage-container">
        <div class="content-wrapper">
            <el-card shadow="never" class="main-card">
                <div class="header-section">
                    <div class="title-info">
                        <div class="title-text">习题册管理中心</div>
                        <div class="header-desc">统一配置考研各科目所需的参考书目与习题资源（共 {{ total }} 本）</div>
                    </div>
                    <el-button type="primary" icon="Plus" @click="handleAdd">新增习题册</el-button>
                </div>
                <div class="search-section">
                    <el-form :inline="true" :model="searchForm">
                        <el-form-item label="所属科目">
                            <el-cascader v-model="searchForm.subjectId" :options="subjectTree" :props="{
                                value: 'id',
                                label: 'name',
                                children: 'children',
                                checkStrictly: true,
                                emitPath: false
                            }" placeholder="选择科目分类" clearable style="width: 240px" @change="loadData" />
                        </el-form-item>
                        <el-form-item>
                            <el-button type="primary" @click="loadData">查询</el-button>
                            <el-button @click="resetSearch" plain>重置</el-button>
                        </el-form-item>
                    </el-form>
                </div>

                <el-table :data="tableData" v-loading="loading" class="modern-table">
                    <!-- ID 列已移除 -->

                    <el-table-column label="习题册信息" min-width="250">
                        <template #default="scope">
                            <div class="book-info-cell">
                                <div class="nickname">
                                    <el-icon class="book-icon"><Notebook /></el-icon>
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
                            <span class="time-text">{{ scope.row.createTime ? scope.row.createTime.split('T')[0] : '-'
                                }}</span>
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

                <div class="pagination-container">
                    <el-pagination background :current-page="pageNum" :page-size="pageSize"
                        layout="total, prev, pager, next" :total="total" @current-change="handlePageChange" />
                </div>
            </el-card>
        </div>

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
                    <el-cascader v-model="form.subjectIds" :options="subjectTree" :props="{
                        value: 'id',
                        label: 'name',
                        children: 'children',
                        checkStrictly: true,
                        emitPath: false,
                        multiple: true
                    }" placeholder="可选择一个或多个科目" style="width: 100%" collapse-tags />
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

const searchForm = ref({ subjectId: null })
const subjectTree = ref([])

const form = ref({
    id: null,
    name: '',
    description: '',
    subjectIds: []
})

const rules = {
    name: [{ required: true, message: '请输入习题册名称', trigger: 'blur' }]
}

// 获取科目树
const loadSubjectTree = async () => {
    try {
        const res = await request.get('/subject/manage-tree')
        const fullTree = res.data || res || []

        // 过滤科目树：基于 scope 字段实现多对多关系
        subjectTree.value = filterSubjectTreeForCascader(fullTree)
    } catch (e) {
        ElMessage.error('获取科目树失败')
        console.error(e)
    }
}


// 基于 scope 字段实现多对多关系映射
const filterSubjectTreeForCascader = (tree) => {
    // 扁平化所有节点到一个 map 中
    const nodeMap = new Map()

    const collectNodes = (node) => {
        nodeMap.set(node.id, { ...node, children: [] })
        if (node.children && node.children.length > 0) {
            node.children.forEach(child => collectNodes(child))
        }
    }

    tree.forEach(node => collectNodes(node))

    // 定义考试规格根节点ID列表
    const rootIds = [1, 2, 3, 4, 5, 6, 7] // 政治(1)、英语一(2)、英语二(3)、数学一(4)、数学二(5)、数学三(6)、408(7)

    // 递归构建子节点（基于 parent_id 的传统关系）
    const buildChildren = (parentId) => {
        const children = []
        nodeMap.forEach(node => {
            if (node.parentId === parentId) {
                const childNode = {
                    id: node.id,
                    name: node.name,
                    level: node.level
                }

                const subChildren = buildChildren(node.id)
                if (subChildren.length > 0) {
                    childNode.children = subChildren
                }

                children.push(childNode)
            }
        })
        return children.sort((a, b) => {
            const nodeA = nodeMap.get(a.id)
            const nodeB = nodeMap.get(b.id)
            return (nodeA.sort || 9999) - (nodeB.sort || 9999)
        })
    }

    // 构建树：基于 scope 字段动态分配子节点
    const buildTree = (examSpecId) => {
        const children = []

        nodeMap.forEach(node => {
            // 情况1：基于 scope 的多对多关系（Level 2/3 学科）
            if ((node.level === '2' || node.level === '3') && node.parentId === 0) {
                // 检查 scope 字段是否包含当前考试规格ID
                const belongsToSpec = !node.scope || node.scope.split(',').map(s => s.trim()).includes(examSpecId.toString())

                if (belongsToSpec) {
                    const childNode = {
                        id: node.id,
                        name: node.name,
                        level: node.level
                    }

                    // 递归构建子节点（基于 parent_id）
                    const subChildren = buildChildren(node.id)
                    if (subChildren.length > 0) {
                        childNode.children = subChildren
                    }

                    children.push(childNode)
                }
            }
            // 情况2：传统的 parent_id 关系（如政治下的马原、408下的四门课）
            else if (node.parentId === examSpecId) {
                const childNode = {
                    id: node.id,
                    name: node.name,
                    level: node.level
                }

                const subChildren = buildChildren(node.id)
                if (subChildren.length > 0) {
                    childNode.children = subChildren
                }

                children.push(childNode)
            }
        })

        return children.sort((a, b) => {
            const nodeA = nodeMap.get(a.id)
            const nodeB = nodeMap.get(b.id)
            return (nodeA.sort || 9999) - (nodeB.sort || 9999)
        })
    }

    // 构建最终树
    const result = []
    rootIds.forEach(rootId => {
        const rootNode = nodeMap.get(rootId)
        if (!rootNode) return

        const newNode = {
            id: rootNode.id,
            name: rootNode.name,
            level: rootNode.level
        }

        const children = buildTree(rootId)
        if (children.length > 0) {
            newNode.children = children
        }

        result.push(newNode)
    })

    // 按 sort 排序根节点
    return result.sort((a, b) => {
        const nodeA = nodeMap.get(a.id)
        const nodeB = nodeMap.get(b.id)
        return (nodeA.sort || 9999) - (nodeB.sort || 9999)
    })
}

// 加载习题册数据
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
    loadSubjectTree()
    loadData()
})
</script>


<style scoped>
.user-manage-container {
    background-color: #f5f7f9;
    min-height: calc(100vh - 100px);
}

.main-card {
    border: none;
    border-radius: 8px;
}

/* 顶部标题样式 */
.header-section {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 24px;
    padding: 10px 5px;
    border-bottom: 1px solid #f0f2f5;
}

:deep(.el-card__header) {
    padding-bottom: 15px;
    border-bottom: 1px solid #f0f2f5 !important;
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
.search-section {
    background-color: #f8faff;
    padding: 18px 20px;
    border-radius: 4px;
    margin-bottom: 20px;
}

.search-section :deep(.el-form-item) {
    margin-bottom: 0;
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
    /* 描述太长时显示省略号 */
    display: -webkit-box;
    -webkit-line-clamp: 1;
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

.time-text {
    font-size: 13px;
    color: #606266;
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