<template>
    <div class="user-manage-container">
        <el-card shadow="never" class="table-card">
            <template #header>
                <div class="card-header">
                    <span class="title-text">用户管理中心</span>
                </div>
            </template>

            <!-- 搜索表单 -->
            <el-form :inline="true" :model="searchForm" class="search-form">
                <el-form-item label="角色">
                    <el-select v-model="searchForm.role" placeholder="请选择角色" clearable style="width: 150px"
                        @change="loadData">
                        <el-option label="全部" value="" />
                        <el-option label="学生" value="student" />
                        <el-option label="管理员" value="admin" />
                    </el-select>
                </el-form-item>
                <el-form-item label="关键词">
                    <el-input v-model="searchForm.keyword" placeholder="搜索用户名/昵称/目标院校" clearable style="width: 250px"
                        @keyup.enter="loadData" />
                </el-form-item>
                <el-form-item>
                    <el-button type="primary" icon="Search" @click="loadData">查询</el-button>
                    <el-button icon="Refresh" @click="resetSearch">重置</el-button>
                </el-form-item>
            </el-form>

            <!-- 数据表格 -->
            <el-table :data="tableData" border stripe v-loading="loading" class="custom-table">
                <el-table-column prop="id" label="ID" width="70" align="center" />
                <el-table-column prop="username" label="用户名" width="120" align="center" />
                <el-table-column prop="nickname" label="昵称" width="150" show-overflow-tooltip />
                <el-table-column label="头像" width="80" align="center">
                    <template #default="scope">
                        <el-avatar :src="scope.row.avatar" :size="40">
                            {{ scope.row.nickname?.charAt(0) || 'U' }}
                        </el-avatar>
                    </template>
                </el-table-column>
                <el-table-column prop="targetSchool" label="目标院校" width="180" show-overflow-tooltip />
                <el-table-column prop="examYear" label="考研年份" width="100" align="center" />
                <el-table-column prop="role" label="角色" width="100" align="center">
                    <template #default="scope">
                        <el-tag :type="scope.row.role === 'admin' ? 'danger' : 'primary'" size="small">
                            {{ scope.row.role === 'admin' ? '管理员' : '学生' }}
                        </el-tag>
                    </template>
                </el-table-column>
                <el-table-column prop="createTime" label="注册时间" width="180" align="center" />
                <el-table-column label="操作" width="150" align="center" fixed="right">
                    <template #default="scope">
                        <el-button size="small" type="primary" link @click="handleViewStats(scope.row)">
                            查看详情
                        </el-button>
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

        <!-- 学习进度抽屉 -->
        <el-drawer v-model="drawerVisible" :title="`学习进度 - ${currentUser?.nickname || currentUser?.username}`"
            size="50%" destroy-on-close>
            <div v-loading="statsLoading" class="drawer-content">
                <!-- 用户基本信息 -->
                <el-card shadow="never" class="user-info-card">
                    <template #header>
                        <span class="card-title">基本信息</span>
                    </template>
                    <el-descriptions :column="2" border>
                        <el-descriptions-item label="用户名">{{ currentUser?.username }}</el-descriptions-item>
                        <el-descriptions-item label="昵称">{{ currentUser?.nickname }}</el-descriptions-item>
                        <el-descriptions-item label="目标院校">
                            <el-tag type="success" v-if="currentUser?.targetSchool">{{ currentUser.targetSchool
                                }}</el-tag>
                            <span v-else class="text-muted">未设置</span>
                        </el-descriptions-item>
                        <el-descriptions-item label="考研年份">
                            <el-tag type="warning" v-if="currentUser?.examYear">{{ currentUser.examYear }}</el-tag>
                            <span v-else class="text-muted">未设置</span>
                        </el-descriptions-item>
                    </el-descriptions>
                </el-card>

                <!-- 学习雷达图 -->
                <el-card shadow="never" class="chart-card">
                    <template #header>
                        <span class="card-title">学习雷达图</span>
                    </template>
                    <div ref="radarChartRef" class="chart-container"></div>
                </el-card>

                <!-- 详细统计数据 -->
                <el-card shadow="never" class="stats-card">
                    <template #header>
                        <span class="card-title">详细统计</span>
                    </template>
                    <el-table :data="studyStats?.subjectStats || []" border stripe>
                        <el-table-column prop="subjectName" label="科目" width="120" />
                        <el-table-column prop="finishedCount" label="完成题数" width="100" align="center" />
                        <el-table-column prop="correctCount" label="正确题数" width="100" align="center" />
                        <el-table-column label="正确率" width="120" align="center">
                            <template #default="scope">
                                <el-tag :type="getAccuracyType(scope.row.accuracy)">
                                    {{ scope.row.accuracy?.toFixed(2) || 0 }}%
                                </el-tag>
                            </template>
                        </el-table-column>
                        <el-table-column label="覆盖度" width="120" align="center">
                            <template #default="scope">
                                <el-tag :type="getCoverageType(scope.row.coverage)">
                                    {{ scope.row.coverage?.toFixed(2) || 0 }}%
                                </el-tag>
                            </template>
                        </el-table-column>
                        <el-table-column prop="totalCount" label="总题数" width="100" align="center" />
                    </el-table>
                </el-card>

                <!-- 总体统计 -->
                <el-card shadow="never" class="overall-stats-card">
                    <template #header>
                        <span class="card-title">总体统计</span>
                    </template>
                    <el-row :gutter="20">
                        <el-col :span="8">
                            <div class="stat-item">
                                <div class="stat-value">{{ studyStats?.overallStats?.totalFinished || 0 }}</div>
                                <div class="stat-label">总完成题数</div>
                            </div>
                        </el-col>
                        <el-col :span="8">
                            <div class="stat-item">
                                <div class="stat-value">{{ studyStats?.overallStats?.totalCorrect || 0 }}</div>
                                <div class="stat-label">总正确题数</div>
                            </div>
                        </el-col>
                        <el-col :span="8">
                            <div class="stat-item">
                                <div class="stat-value">{{ studyStats?.overallStats?.overallAccuracy?.toFixed(2) || 0
                                    }}%</div>
                                <div class="stat-label">总体正确率</div>
                            </div>
                        </el-col>
                    </el-row>
                </el-card>
            </div>
        </el-drawer>
    </div>
</template>

<script setup>
import { ref, onMounted, nextTick } from 'vue'
import request from '@/utils/request'
import { ElMessage } from 'element-plus'
import * as echarts from 'echarts'

// 数据定义
const loading = ref(false)
const statsLoading = ref(false)
const tableData = ref([])
const total = ref(0)
const pageNum = ref(1)
const pageSize = ref(10)
const drawerVisible = ref(false)
const radarChartRef = ref(null)
const currentUser = ref(null)
const studyStats = ref(null)
let radarChart = null

const searchForm = ref({
    role: '',
    keyword: ''
})

// 加载用户数据
const loadData = async () => {
    loading.value = true
    try {
        const params = {
            pageNum: pageNum.value,
            pageSize: pageSize.value
        }
        if (searchForm.value.role) {
            params.role = searchForm.value.role
        }
        if (searchForm.value.keyword) {
            params.keyword = searchForm.value.keyword
        }

        console.log('请求参数:', params)
        const res = await request.get('/user/page', { params })
        const data = res.data || res
        tableData.value = data.records || []
        total.value = data.total || 0
        console.log('用户数据:', tableData.value)
    } catch (e) {
        ElMessage.error('获取数据失败')
        console.error('错误详情:', e)
    } finally {
        loading.value = false
    }
}

// 重置搜索
const resetSearch = () => {
    searchForm.value = { role: '', keyword: '' }
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

// 查看学习统计
const handleViewStats = async (user) => {
    currentUser.value = user
    drawerVisible.value = true
    statsLoading.value = true

    try {
        const res = await request.get(`/user/study-stats/${user.id}`)
        studyStats.value = res.data || res
        console.log('学习统计:', studyStats.value)

        // 等待 DOM 更新后渲染图表
        await nextTick()
        initRadarChart()
    } catch (e) {
        ElMessage.error('获取学习统计失败')
        console.error(e)
    } finally {
        statsLoading.value = false
    }
}

// 初始化雷达图
const initRadarChart = () => {
    if (!radarChartRef.value) return

    // 销毁旧图表
    if (radarChart) {
        radarChart.dispose()
    }

    // 创建新图表
    radarChart = echarts.init(radarChartRef.value)

    const stats = studyStats.value
    const subjectStats = stats?.subjectStats || []

    // 准备雷达图数据
    const indicators = subjectStats.map(s => ({
        name: s.subjectName,
        max: 100
    }))

    const accuracyData = subjectStats.map(s => s.accuracy || 0)
    const coverageData = subjectStats.map(s => s.coverage || 0)
    const activityData = subjectStats.map(s => {
        // 活跃度基于完成题数，归一化到 0-100
        const maxFinished = Math.max(...subjectStats.map(item => item.finishedCount || 0))
        return maxFinished > 0 ? ((s.finishedCount || 0) / maxFinished * 100) : 0
    })

    const option = {
        tooltip: {
            trigger: 'axis'
        },
        legend: {
            data: ['正确率', '覆盖度', '活跃度']
        },
        radar: {
            indicator: indicators
        },
        series: [
            {
                name: '学习数据',
                type: 'radar',
                data: [
                    {
                        value: accuracyData,
                        name: '正确率',
                        itemStyle: { color: '#67C23A' },
                        areaStyle: { opacity: 0.3 }
                    },
                    {
                        value: coverageData,
                        name: '覆盖度',
                        itemStyle: { color: '#409EFF' },
                        areaStyle: { opacity: 0.3 }
                    },
                    {
                        value: activityData,
                        name: '活跃度',
                        itemStyle: { color: '#E6A23C' },
                        areaStyle: { opacity: 0.3 }
                    }
                ]
            }
        ]
    }

    radarChart.setOption(option)

    // 响应式
    window.addEventListener('resize', () => {
        radarChart?.resize()
    })
}

// 获取正确率标签类型
const getAccuracyType = (accuracy) => {
    if (accuracy >= 80) return 'success'
    if (accuracy >= 60) return ''
    if (accuracy >= 40) return 'warning'
    return 'danger'
}

// 获取覆盖度标签类型
const getCoverageType = (coverage) => {
    if (coverage >= 80) return 'success'
    if (coverage >= 60) return ''
    if (coverage >= 40) return 'warning'
    return 'info'
}

onMounted(() => {
    console.log('用户管理页面已挂载')
    loadData()
})
</script>

<style scoped>
.user-manage-container {
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

.text-muted {
    color: #909399;
}

/* 抽屉样式 */
.drawer-content {
    padding: 0 16px;
}

.user-info-card,
.chart-card,
.stats-card,
.overall-stats-card {
    margin-bottom: 20px;
}

.card-title {
    font-weight: 600;
    color: #303133;
}

.chart-container {
    width: 100%;
    height: 400px;
}

.stat-item {
    text-align: center;
    padding: 20px 0;
}

.stat-value {
    font-size: 32px;
    font-weight: 600;
    color: #409EFF;
    margin-bottom: 8px;
}

.stat-label {
    font-size: 14px;
    color: #606266;
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