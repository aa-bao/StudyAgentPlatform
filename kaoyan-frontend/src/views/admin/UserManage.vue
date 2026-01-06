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
                    <el-table-column prop="id" label="ID" width="70" align="center" />

                    <el-table-column label="习题册信息" min-width="250">
                        <template #default="scope">
                            <div class="book-info-cell">
                                <div class="nickname">{{ scope.row.name }}</div>
                                <div class="username">{{ scope.row.description || '暂无描述' }}</div>
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
import { ref, onMounted, nextTick } from 'vue'
import request from '@/utils/request'
import { ElMessage, ElMessageBox } from 'element-plus'

// 核心状态控制
const loading = ref(false)
const saving = ref(false)
const dialogVisible = ref(false) // 确保变量名正确
const tableData = ref([])
const total = ref(0)
const pageNum = ref(1)
const pageSize = ref(10)
const subjectTree = ref([])
const formRef = ref(null)

const searchForm = ref({ subjectId: null })
const form = ref({ id: null, name: '', description: '', subjectIds: [] })

const rules = {
    name: [{ required: true, message: '名称不能为空', trigger: 'blur' }],
    subjectIds: [{ required: true, message: '请至少关联一个科目', trigger: 'change' }]
}

// 修复打不开框的关键：handleAdd函数定义
const handleAdd = () => {
    resetForm()
    dialogVisible.value = true
}

const resetForm = () => {
    form.value = { id: null, name: '', description: '', subjectIds: [] }
    // 使用 nextTick 防止 formRef 为空时报错
    nextTick(() => {
        formRef.value?.clearValidate()
    })
}
// 加载数据
const loadData = async () => {
    loading.value = true
    try {
        const params = { pageNum: pageNum.value, pageSize: pageSize.value, ...searchForm.value }
        const res = await request.get('/book/page', { params })
        const data = res.data || res
        tableData.value = data.records || []
        total.value = data.total || 0
    } catch (e) {
        ElMessage.error('加载失败')
    } finally {
        loading.value = false
    }
}

const resetSearch = () => {
    searchForm.value = { role: '', keyword: '' }
    pageNum.value = 1
    loadData()
}

const handlePageChange = (page) => {
    pageNum.value = page
    loadData()
}

// 核心修复：雷达图渲染
const handleViewStats = async (user) => {
    currentUser.value = user
    drawerVisible.value = true
    statsLoading.value = true

    try {
        const res = await request.get(`/user/study-stats/${user.id}`)
        studyStats.value = res.data || res

        // 等待抽屉打开及DOM渲染
        await nextTick()
        setTimeout(() => {
            initRadarChart()
        }, 350)
    } catch (e) {
        ElMessage.error('获取详情失败')
    } finally {
        statsLoading.value = false
    }
}

const initRadarChart = () => {
    if (!radarChartRef.value) return
    if (radarChart) radarChart.dispose()

    radarChart = echarts.init(radarChartRef.value)
    const subjectStats = studyStats.value?.subjectStats || []

    const option = {
        color: ['#409EFF', '#67C23A'],
        tooltip: { trigger: 'item' },
        radar: {
            indicator: subjectStats.length ? subjectStats.map(s => ({ name: s.subjectName, max: 100 })) : [{ name: '暂无', max: 100 }],
            splitArea: { show: false },
            axisLine: { lineStyle: { color: '#E2E8F0' } },
            splitLine: { lineStyle: { color: '#E2E8F0' } }
        },
        series: [{
            type: 'radar',
            data: [
                { value: subjectStats.map(s => s.accuracy || 0), name: '正确率', areaStyle: { color: 'rgba(64, 158, 255, 0.2)' } },
                { value: subjectStats.map(s => s.coverage || 0), name: '覆盖度', areaStyle: { color: 'rgba(103, 194, 58, 0.2)' } }
            ]
        }]
    }
    radarChart.setOption(option)

    // 监听容器大小变化，防止雷达图缩成一团
    resizeObserver = new ResizeObserver(() => {
        radarChart?.resize()
    })
    resizeObserver.observe(radarChartRef.value)
}

onMounted(() => { loadData() })
onUnmounted(() => { resizeObserver?.disconnect() })
</script>

<style scoped>
/* 这里复用你用户管理页面的CSS逻辑 */
.user-manage-container {
    padding: 16px;
    background-color: #f5f7f9;
    min-height: calc(100vh - 100px);
}

.main-card {
    border: none;
    border-radius: 8px;
}

.header-section {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 24px;
    padding: 10px 5px;
}

.title-text {
    font-size: 18px;
    font-weight: 600;
    color: #303133;
}

.header-desc {
    font-size: 13px;
    color: #909399;
    margin-top: 5px;
}

.search-section {
    background-color: #f8faff;
    padding: 18px 20px;
    border-radius: 4px;
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
}

.username {
    font-size: 12px;
    color: #909399;
}

.tags-container {
    display: flex;
    flex-wrap: wrap;
    gap: 6px;
}

.subject-tag {
    border-radius: 4px;
}

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


