<template>
    <div class="admin-container">
        <el-card shadow="never" class="table-card">
            <template #header>
                <div class="card-header">
                    <span class="title-text">习题册管理中心</span>
                    <el-button type="primary" icon="Plus" @click="handleAdd">新增习题册</el-button>
                </div>
            </template>

            <!-- 搜索表单 -->
            <el-form :inline="true" :model="searchForm" class="search-form">
                <el-form-item label="所属科目">
                    <el-cascader
                        v-model="searchForm.subjectId"
                        :options="subjectTree"
                        :props="{
                            value: 'id',
                            label: 'name',
                            children: 'children',
                            checkStrictly: true,
                            emitPath: false
                        }"
                        placeholder="请选择科目"
                        clearable
                        style="width: 200px"
                        @change="loadData"
                    />
                </el-form-item>
                <el-form-item>
                    <el-button type="primary" icon="Search" @click="loadData">查询</el-button>
                    <el-button icon="Refresh" @click="resetSearch">重置</el-button>
                </el-form-item>
            </el-form>

            <!-- 数据表格 -->
            <el-table :data="tableData" border stripe v-loading="loading" class="custom-table">
                <el-table-column prop="id" label="ID" width="70" align="center" />
                <el-table-column prop="name" label="习题册名称" show-overflow-tooltip min-width="200" />
                <el-table-column prop="description" label="描述" show-overflow-tooltip min-width="250" />
                <el-table-column label="关联科目" width="200" align="center">
                    <template #default="scope">
                        <el-tag v-if="scope.row.subjectNames" size="small" type="success">
                            {{ scope.row.subjectNames.join(', ') }}
                        </el-tag>
                        <el-tag v-else size="small" type="info">未关联</el-tag>
                    </template>
                </el-table-column>
                <el-table-column prop="createTime" label="创建时间" width="180" align="center" />
                <el-table-column label="操作" width="200" align="center" fixed="right">
                    <template #default="scope">
                        <el-button size="small" type="primary" link @click="handleEdit(scope.row)">编辑</el-button>
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
        <el-dialog v-model="dialogVisible" :title="form.id ? '编辑习题册' : '新增习题册'" width="700px" destroy-on-close @close="resetForm">
            <el-form :model="form" :rules="rules" ref="formRef" label-width="100px">
                <el-form-item label="习题册名称" prop="name">
                    <el-input v-model="form.name" placeholder="请输入习题册名称，如：张宇1000题" />
                </el-form-item>

                <el-form-item label="描述" prop="description">
                    <el-input v-model="form.description" type="textarea" :rows="3" placeholder="请输入习题册描述" />
                </el-form-item>

                <el-form-item label="关联科目" prop="subjectIds">
                    <el-cascader
                        v-model="form.subjectIds"
                        :options="subjectTree"
                        :props="{
                            value: 'id',
                            label: 'name',
                            children: 'children',
                            checkStrictly: true,
                            emitPath: false,
                            multiple: true
                        }"
                        placeholder="请选择科目（可多选）"
                        collapse-tags
                        collapse-tags-tooltip
                        style="width: 100%"
                    />
                    <div class="form-tip">提示：一本书可以关联多个科目，如《张宇1000题》可同时关联高数、线代、概率</div>
                </el-form-item>
            </el-form>
            <template #footer>
                <el-button @click="dialogVisible = false">取消</el-button>
                <el-button type="primary" :loading="saving" @click="saveBook">确定保存</el-button>
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

// 过滤科目树，用于级联选择器
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

        // 为每本书获取关联的科目名称
        for (const book of books) {
            if (book.id) {
                const subjectNames = await getSubjectNamesByBookId(book.id)
                book.subjectNames = subjectNames
            }
        }

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

// 获取书籍关联的科目名称
const getSubjectNamesByBookId = async (bookId) => {
    try {
        const res = await request.get(`/book/${bookId}`)
        const book = res.data
        if (book && book.subjectId) {
            // 查找科目名称
            return findSubjectNames([book.subjectId])
        }
        return []
    } catch (e) {
        console.error('获取科目名称失败:', e)
        return []
    }
}

// 查找科目名称
const findSubjectNames = (subjectIds) => {
    if (!subjectIds || subjectIds.length === 0) return []

    const names = []
    const findInTree = (nodes, targetIds) => {
        for (const node of nodes) {
            if (targetIds.includes(node.id)) {
                names.push(node.name)
            }
            if (node.children) {
                findInTree(node.children, targetIds)
            }
        }
    }
    findInTree(subjectTree.value, subjectIds)
    return names
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

.form-tip {
    font-size: 12px;
    color: #909399;
    margin-top: 4px;
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
