<template>
    <div class="subject-manage-container">
        <el-card shadow="never" class="tree-card">
            <template #header>
                <div class="header-section">
                    <div class="card-header">
                        <span class="title-text">科目体系管理</span>
                        <div class="header-desc">统一配置管理考研体系大纲层级结构
                        </div>
                    </div>
                    <el-button type="primary" icon="Plus" @click="handleAddRoot">添加顶级科目</el-button>
                </div>
            </template>

            <div class="tree-container" v-loading="loading">
                <div class="tree-tip">
                    <el-icon>
                        <InfoFilled />
                    </el-icon>
                    <span>提示：支持拖拽节点调整顺序；虚拟分组节点不可操作。</span>
                </div>

                <el-tree ref="treeRef" :data="treeData" :props="treeProps" node-key="treeId" default-expand-all
                    :expand-on-click-node="false" draggable :allow-drop="allowDrop" :allow-drag="allowDrag"
                    @node-drop="handleNodeDrop">
                    <template #default="{ data }">
                        <div class="custom-tree-node">
                            <div class="node-content">
                                <span class="node-icon">
                                    <el-icon v-if="data.level === '0' || data.level === '1'">
                                        <Folder />
                                    </el-icon>
                                    <el-icon v-else-if="data.level === '2'">
                                        <Document />
                                    </el-icon>
                                    <el-icon v-else>
                                        <Tickets />
                                    </el-icon>
                                </span>
                                <span class="node-name" :class="{ 'virtual-node': data.id < 0 }">
                                    {{ data.name }}
                                    <el-tag v-if="data.scope" size="small" type="info" class="ml-2">
                                        Scope: {{ getScopeLabel(data.scope) }}
                                    </el-tag>
                                </span>
                            </div>

                            <div class="node-actions">
                                <el-button v-if="parseInt(data.level) < 4" link type="primary"
                                    @click.stop="handleAddChild(data)">添加</el-button>

                                <template v-if="data.id > 0">
                                    <el-button link type="warning" @click.stop="handleEdit(data)">编辑</el-button>
                                    <el-button link type="danger" @click.stop="handleDelete(data)">删除</el-button>
                                </template>
                            </div>
                        </div>
                    </template>
                </el-tree>
            </div>
        </el-card>

        <el-dialog v-model="dialogVisible" :title="dialogTitle" width="600px" destroy-on-close>
            <el-form :model="form" :rules="rules" ref="formRef" label-width="100px">
                <el-form-item label="科目名称" prop="name">
                    <el-input v-model="form.name" placeholder="请输入科目名称" />
                </el-form-item>

                <el-form-item label="层级" prop="level">
                    <el-radio-group v-model="form.level">
                        <el-radio value="1">Level 1 (考试规格)</el-radio>
                        <el-radio value="2">Level 2 (具体学科)</el-radio>
                        <el-radio value="3">Level 3 (知识点)</el-radio>
                        <el-radio value="4">Level 4 (题型方法)</el-radio>
                    </el-radio-group>
                </el-form-item>

                <el-form-item label="适用范围" v-if="form.level === '2'">
                    <el-checkbox-group v-model="scopeChecked">
                        <el-checkbox :label="4">数学一</el-checkbox>
                        <el-checkbox :label="5">数学二</el-checkbox>
                        <el-checkbox :label="6">数学三</el-checkbox>
                        <el-checkbox :label="2">英语一</el-checkbox>
                        <el-checkbox :label="3">英语二</el-checkbox>
                    </el-checkbox-group>
                    <div class="form-tip">提示：如果该科目是跨考试规格通用的（如高数），请勾选范围</div>
                </el-form-item>

                <el-form-item label="排序" prop="sort">
                    <el-input-number v-model="form.sort" :min="0" />
                </el-form-item>
            </el-form>
            <template #footer>
                <el-button @click="dialogVisible = false">取消</el-button>
                <el-button type="primary" :loading="saving" @click="saveSubject">确定保存</el-button>
            </template>
        </el-dialog>
    </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import request from '@/utils/request'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Folder, Document, Tickets, InfoFilled } from '@element-plus/icons-vue'

// --- 变量定义 ---
const loading = ref(false)
const saving = ref(false)
const treeData = ref([])
const treeRef = ref(null)
const formRef = ref(null)
const dialogVisible = ref(false)
const scopeChecked = ref([])

const form = ref({
    id: null,
    name: '',
    parentId: 0,
    level: '1',
    icon: '',
    sort: 0,
    scope: ''
})

const rules = {
    name: [{ required: true, message: '请输入科目名称', trigger: 'blur' }],
    level: [{ required: true, message: '请选择层级', trigger: 'change' }]
}

const treeProps = { label: 'name', children: 'children' }
const dialogTitle = computed(() => form.value.id ? '编辑科目' : '新增科目')

// --- 逻辑方法 ---

const loadTree = async () => {
    loading.value = true
    try {
        const res = await request.get('/subject/manage-tree')
        treeData.value = res.data || []
    } catch (e) {
        ElMessage.error('获取科目树失败')
    } finally {
        loading.value = false
    }
}

const getScopeLabel = (scope) => {
    if (!scope) return ''
    const map = { '4': '数一', '5': '数二', '6': '数三', '2': '英一', '3': '英二' }
    return scope.split(',').map(s => map[s.trim()] || s).join('/')
}

const handleAddRoot = () => {
    form.value = { id: null, name: '', parentId: 0, level: '1', sort: 0, scope: '' }
    scopeChecked.value = []
    dialogVisible.value = true
}

const handleAddChild = (data) => {
    form.value = {
        id: null,
        name: '',
        // 如果是虚拟节点(id < 0)，新科目的 parentId 必须是 0 (因为它是 Level 1)
        // 如果是普通节点，正常继承 id
        parentId: data.id < 0 ? 0 : data.id,
        level: String(Math.min(parseInt(data.level) + 1, 4)),
        sort: 0,
        scope: ''
    }

    // 自动化 Scope 勾选：
    // 如果当前点击的是“数学一/二/三”或“英语一/二”，自动把当前 ID 加入 scope
    if (data.level === '1' && data.id > 0) {
        scopeChecked.value = [data.id];
    } else {
        scopeChecked.value = [];
    }

    dialogVisible.value = true;
};

const handleEdit = async (data) => {
    try {
        const res = await request.get(`/subject/${data.id}`)
        const s = res.data
        form.value = { ...s }
        scopeChecked.value = s.scope ? s.scope.split(',').map(v => parseInt(v)) : []
        dialogVisible.value = true
    } catch (e) {
        ElMessage.error('获取详情失败')
    }
}

const saveSubject = async () => {
    await formRef.value.validate()
    saving.value = true
    try {
        form.value.scope = scopeChecked.value.join(',')
        const url = form.value.id ? '/subject/update' : '/subject/add'
        await request.post(url, form.value)
        ElMessage.success('操作成功')
        dialogVisible.value = false
        loadTree()
    } catch (e) {
        ElMessage.error('保存失败')
    } finally {
        saving.value = false
    }
}

const handleDelete = (data) => {
    ElMessageBox.confirm(`确定删除"${data.name}"吗？`, '提示', { type: 'warning' })
        .then(async () => {
            await request.delete(`/subject/delete/${data.id}`)
            ElMessage.success('删除成功')
            loadTree()
        }).catch(() => { })
}

// --- 拖拽逻辑 ---

const allowDrag = (node) => {
    return node.data.id > 0 // 虚拟节点禁止拖拽
}

const allowDrop = (draggingNode, dropNode, type) => {
    if (dropNode.data.id < 0) return false // 不允许拖入虚拟大类内部
    return type !== 'inner' || parseInt(dropNode.data.level) < 4
}

const handleNodeDrop = async (draggingNode, dropNode, dropType) => {
    const updates = []
    const draggedData = draggingNode.data
    let newParentId = draggedData.parentId

    if (dropType === 'inner') {
        newParentId = dropNode.data.id
    } else {
        newParentId = dropNode.data.parentId || 0
    }

    updates.push({ id: draggedData.id, parentId: newParentId, sort: 0 }) // 简化排序逻辑

    try {
        await request.post('/subject/batch-update-sort', updates)
        ElMessage.success('顺序已更新')
    } catch (e) {
        loadTree()
    }
}

onMounted(() => loadTree())
</script>

<style scoped>
.tree-card {
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

/* 
.tree-container {
    padding: 5px 0;
} */

.tree-tip {
    display: flex;
    align-items: center;
    gap: 8px;
    padding: 10px 16px;
    background-color: #f0f9ff;
    border-left: 3px solid #409EFF;
    border-radius: 4px;
    margin-bottom: 16px;
    color: #606266;
    font-size: 13px;
}

.tree-tip .el-icon {
    color: #409EFF;
    font-size: 16px;
}

.custom-tree-node {
    flex: 1;
    display: flex;
    align-items: center;
    justify-content: space-between;
    font-size: 14px;
    padding-right: 8px;
}

.node-content {
    display: flex;
    align-items: center;
    gap: 8px;
}

.node-icon {
    color: #409EFF;
    font-size: 16px;
}

.node-name {
    font-weight: 500;
    color: #303133;
}

.level-tag {
    margin-left: 4px;
}

.scope-tag {
    margin-left: 4px;
}

.node-actions {
    display: flex;
    gap: 4px;
    opacity: 0;
    transition: opacity 0.2s;
}

.custom-tree-node:hover .node-actions {
    opacity: 1;
}

.form-tip {
    font-size: 12px;
    color: #909399;
    margin-top: 4px;
}

:deep(.el-tree-node__content) {
    height: 40px;
}

:deep(.el-tree-node__content:hover) {
    background-color: #f5f7fa;
}

:deep(.el-form-item) {
    margin-bottom: 18px;
}

/* 拖拽相关样式 */
:deep(.el-tree-node.is-dropable > .el-tree-node__content) {
    background-color: #e6f7ff !important;
    border: 2px dashed #409EFF;
}

:deep(.el-tree-node__dragging) {
    opacity: 0.5;
}

:deep(.el-tree-node__content.dragging) {
    background-color: #f0f9ff;
}
</style>
