<template>
    <div class="user-manage-container">
        <div class="content-wrapper">
            <el-card shadow="never" class="main-card">
                <template #header>
                    <div class="card-header">
                        <span class="title-text">用户管理中心</span>
                        <div class="header-desc">查看并管理平台学生及管理员的学习数据</div>
                    </div>
                </template>

                <!-- 搜索表单 -->
                <div class="search-section">
                    <el-form :inline="true" :model="searchForm">
                        <el-form-item label="身份角色">
                            <el-select v-model="searchForm.role" placeholder="选择角色" clearable @change="loadData"
                                style="width: 130px">
                                <el-option label="全部" value="" />
                                <el-option label="学生" value="student" />
                                <el-option label="管理员" value="admin" />
                            </el-select>
                        </el-form-item>
                        <el-form-item label="关键词">
                            <el-input v-model="searchForm.keyword" placeholder="用户名 / 昵称 / 院校" clearable
                                style="width: 280px" @keyup.enter="loadData" prefix-icon="Search" />
                        </el-form-item>
                        <el-form-item>
                            <el-button type="primary" @click="loadData">查询</el-button>
                            <el-button @click="resetSearch" plain>重置</el-button>
                        </el-form-item>
                    </el-form>
                </div>
                
                <!-- 数据表格 -->
                <el-table :data="tableData" v-loading="loading" class="modern-table" stripe>
                    <!-- ID 列 -->
                    <el-table-column prop="id" label="ID" width="60" align="center" />
                    <!-- 用户信息列 -->
                    <el-table-column label="用户信息" min-width="180">
                        <template #default="scope">
                            <div class="user-info-cell">
                                <el-avatar :src="scope.row.avatar" :size="38">
                                    {{ scope.row.nickname?.charAt(0) || 'U' }}
                                </el-avatar>
                                <div class="name-box">
                                    <div class="nickname">{{ scope.row.nickname }}</div>
                                    <div class="username">@{{ scope.row.username }}</div>
                                </div>
                            </div>
                        </template>
                    </el-table-column>
                    <!-- 手机列 -->
                    <el-table-column prop="phone" label="手机号" width="120" />
                    <!-- 邮箱列 -->
                    <el-table-column prop="email" label="Email" min-width="180" show-overflow-tooltip />
                    <!-- 目标院校列 -->
                    <el-table-column prop="targetSchool" label="目标院校" min-width="140">
                        <template #default="scope">
                            <el-tag v-if="scope.row.targetSchool" type="info" effect="plain">{{ scope.row.targetSchool
                                }}</el-tag>
                            <span v-else class="text-muted">未设置</span>
                        </template>
                    </el-table-column>
                    <!-- 目标总分列 -->
                    <el-table-column prop="targetTotalScore" label="目标总分" width="90" align="center">
                        <template #default="scope">
                            <span class="score-text">{{ scope.row.targetTotalScore || '-' }}</span>
                        </template>
                    </el-table-column>
                    <!-- 考试科目列 -->
                    <el-table-column prop="examSubjects" label="考试科目" min-width="200" show-overflow-tooltip>
                        <template #default="scope">
                            <div style="display: flex; flex-wrap: wrap; gap: 4px;">
                                <template v-if="scope.row.examSubjects && scope.row.examSubjects.trim() !== ''">
                                    <el-tag v-for="(sub, index) in scope.row.examSubjects.split(',')" :key="index"
                                        :type="['primary', 'success', 'warning', 'danger', 'info'][index % 5]"
                                        size="small" effect="light">
                                        {{ sub }}
                                    </el-tag>
                                </template>
                                <span v-else class="text-muted">-</span>
                            </div>
                        </template>
                    </el-table-column>
                    <!-- 考试年份列 -->
                    <el-table-column prop="examYear" label="考试年份" width="90" align="center">
                        <template #default="scope">
                            <el-tag v-if="scope.row.examYear" size="small" effect="dark" color="#9b66f7"
                                style="border:none">
                                {{ scope.row.examYear }}
                            </el-tag>
                            <span v-else>-</span>
                        </template>
                    </el-table-column>
                    <!-- 角色列 -->
                    <el-table-column label="角色" width="90" align="center">
                        <template #default="scope">
                            <el-tag :type="scope.row.role === 'admin' ? 'danger' : 'success'" effect="light"
                                size="small">
                                {{ scope.row.role === 'admin' ? '管理员' : '学生' }}
                            </el-tag>
                        </template>
                    </el-table-column>
                    <!-- 注册日期列 -->
                    <el-table-column label="注册日期" width="170" align="center">
                        <template #default="scope">
                            <div class="full-time">
                                {{ formatDateTime(scope.row.createTime) }}
                            </div>
                        </template>
                    </el-table-column>
                    <!-- 操作面板列 -->
                    <el-table-column label="操作" width="220" align="center" fixed="right">
                        <template #default="scope">
                            <el-button size="small" type="primary" link @click="handleEdit(scope.row)">编辑</el-button>
                            <el-button size="small" type="warning" link
                                @click="handleResetPassword(scope.row)">重置密码</el-button>
                            <el-button size="small" type="info" link @click="handleViewStats(scope.row)"                                 style="color: #009688;">用户面板</el-button>
                        </template>
                    </el-table-column>

                    <!-- 编辑对话框 -->
                    <el-dialog v-model="editDialogVisible" title="编辑用户信息" width="500px" append-to-body>
                        <el-form :model="editForm" label-width="80px" style="padding: 0 20px">
                            <el-form-item label="用户名">
                                <el-input v-model="editForm.username" disabled />
                            </el-form-item>
                            <el-form-item label="昵称">
                                <el-input v-model="editForm.nickname" placeholder="请输入昵称" />
                            </el-form-item>
                            <el-form-item label="手机号">
                                <el-input v-model="editForm.phone" placeholder="请输入手机号" />
                            </el-form-item>
                            <el-form-item label="邮箱">
                                <el-input v-model="editForm.email" placeholder="请输入邮箱" />
                            </el-form-item>
                            <el-form-item label="身份角色">
                                <el-select v-model="editForm.role" style="width: 100%">
                                    <el-option label="学生" value="student" />
                                    <el-option label="管理员" value="admin" />
                                </el-select>
                            </el-form-item>
                        </el-form>
                        <template #footer>
                            <el-button @click="editDialogVisible = false">取消</el-button>
                            <el-button type="primary" @click="submitEdit" :loading="submitLoading">保存修改</el-button>
                        </template>
                    </el-dialog>

                    <!-- 重置密码对话框 -->
                    <el-dialog v-model="pwdDialogVisible" title="重置用户密码" width="400px" append-to-body>
                        <div style="margin-bottom: 20px; color: #606266;">
                            正在为用户 <b style="color: #409EFF">{{ currentUser?.nickname }}</b> 重置密码
                        </div>
                        <el-form :model="pwdForm" label-width="70px">
                            <el-form-item label="新密码">
                                <el-input v-model="pwdForm.newPassword" placeholder="请输入新密码" show-password />
                            </el-form-item>
                        </el-form>
                        <template #footer>
                            <el-button @click="pwdDialogVisible = false">取消</el-button>
                            <el-button type="danger" @click="submitResetPassword"
                                :loading="submitLoading">确认重置</el-button>
                        </template>
                    </el-dialog>
                </el-table>

                <!-- 分页 -->
                <div class="pagination-container">
                    <el-pagination background :current-page="pageNum" :page-size="pageSize" 
                        layout="total, prev, pager, next, jumper" :total="total" @current-change="handlePageChange" />
                </div>
            </el-card>
        </div>

        <!-- 用户画像 -->
        <el-drawer v-model="drawerVisible" :title="`${currentUser?.nickname} 的学习画像`" size="550px" destroy-on-close
            custom-class="stats-drawer">
            <div v-loading="statsLoading" class="drawer-inner">
                <div class="stats-grid">
                    <div class="mini-card">
                        <div class="label">完成题数</div>
                        <div class="value primary">{{ studyStats?.overallStats?.totalFinished || 0 }}</div>
                    </div>
                    <div class="mini-card">
                        <div class="label">正确率</div>
                        <div class="value success">{{ studyStats?.overallStats?.overallAccuracy?.toFixed(1) || 0 }}%
                        </div>
                    </div>
                </div>
                <div class="chart-box">
                    <div class="chart-header">
                        <span class="dot"></span> 知识点覆盖维度
                    </div>
                    <div ref="radarChartRef" class="radar-chart"></div>
                    <div v-if="!studyStats?.subjectStats?.length" class="empty-stats">暂无详细学科数据</div>
                </div>
                <div class="sub-list">
                    <div class="section-title">分科概览</div>
                    <el-table :data="studyStats?.subjectStats || []" size="small">
                        <el-table-column prop="subjectName" label="科目" />
                        <el-table-column label="正确率" align="right">
                            <template #default="scope">{{ scope.row.accuracy?.toFixed(1) }}%</template>
                        </el-table-column>
                    </el-table>
                </div>
            </div>
        </el-drawer>
    </div>
</template>


<script setup>
import { ref, onMounted, nextTick, onUnmounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus' 
import * as echarts from 'echarts'
import request from '@/utils/request'

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
let resizeObserver = null

const searchForm = ref({ role: '', keyword: '' })

// 状态控制
const editDialogVisible = ref(false)
const pwdDialogVisible = ref(false)
const submitLoading = ref(false)

// 表单数据
const editForm = ref({})
const pwdForm = ref({ newPassword: '' })

// 数据加载
const loadData = async () => {
    loading.value = true
    try {
        const params = { pageNum: pageNum.value, pageSize: pageSize.value, ...searchForm.value }
        const res = await request.get('/user/page', { params })
        const data = res.data || res
        console.log(data)
        // ID从小到大排序
        tableData.value = (data.records || [])
        total.value = data.total || 0
    } catch (e) {
        ElMessage.error('获取数据失败')
    } finally {
        loading.value = false
    }
}

// 打开编辑
const handleEdit = (row) => {
    editForm.value = { ...row }
    editDialogVisible.value = true
}

// 提交编辑
const submitEdit = async () => {
    submitLoading.value = true
    try {
        // 假设后端接口为 /user/update
        await request.post('/user/update', editForm.value)
        ElMessage.success('更新成功')
        editDialogVisible.value = false
        loadData() // 刷新列表
    } catch (e) {
        ElMessage.error('更新失败')
    } finally {
        submitLoading.value = false
    }
}

// 打开重置密码
const handleResetPassword = (row) => {
    currentUser.value = row
    pwdForm.value.newPassword = ''
    pwdDialogVisible.value = true
}

// 提交重置密码
const submitResetPassword = async () => {
    if (!pwdForm.value.newPassword) return ElMessage.warning('请输入新密码')

    submitLoading.value = true
    try {
        // 假设后端接口为 /user/resetPwd
        await request.post('/user/resetPwd', {
            id: currentUser.value.id,
            password: pwdForm.value.newPassword
        })
        ElMessage.success('密码重置成功')
        pwdDialogVisible.value = false
    } catch (e) {
        ElMessage.error('操作失败')
    } finally {
        submitLoading.value = false
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

// 时间格式化函数
const formatDateTime = (timeStr) => {
    if (!timeStr) return '-';
    // 处理 ISO 格式 (2026-01-01T01:18:43) -> 2026-01-01 01:18:43
    return timeStr.replace('T', ' ').split('.')[0];
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
.content-wrapper {
    width: 100%;
    transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

/* 卡片与头部：扁平化设计 */
.main-card {
    border: 1px solid #ebeef5;
    border-radius: 8px;
    background: #ffffff;
}

/* el-card 头部底部的分割线 */
:deep(.el-card__header) {
    padding-bottom: 15px;
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

.card-header {
    display: flex;
    flex-direction: column;
    gap: 4px;
}

.title-text {
    font-size: 18px;
    font-weight: 600;
    color: #303133;
}

.header-desc {
    font-size: 13px;
    color: #909399;
}

/* 搜索区域 */
.search-section {
    background: #fcfcfd;
    padding: 18px 20px 0;
    border-radius: 8px;
    margin-bottom: 20px;
    border: 1px solid #ebeef5;
}

:deep(.el-form--inline .el-form-item) {
    margin-right: 24px;
}

/* 表格 */
.modern-table {
    font-size: 14px;
}

/* 用户信息单元格 */
.user-info-cell {
    display: flex;
    align-items: center;
    gap: 12px;
}

.nickname {
    font-weight: 600;
    color: #303133;
    line-height: 1.2;
}

.username {
    font-size: 12px;
    color: #909399;
    margin-top: 2px;
}

/* 时间文本样式 */
.time-box {
    display: flex;
    flex-direction: column;
    align-items: center;
}

.time-date {
    color: #606266;
    font-weight: 500;
}

.time-hm {
    font-size: 11px;
    color: #a8abb2;
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

/* 抽屉内部样式 */
.drawer-inner {
    padding: 10px 20px;
}

.stats-grid {
    display: grid;
    grid-template-columns: repeat(2, 1fr);
    gap: 16px;
    margin-bottom: 24px;
}

.mini-card {
    background: #f8f9fa;
    border: 1px solid #f0f2f5;
    padding: 18px;
    border-radius: 8px;
    text-align: center;
}

.mini-card .label {
    font-size: 13px;
    color: #909399;
    margin-bottom: 6px;
}

.mini-card .value {
    font-size: 22px;
    font-weight: 700;
}

.mini-card .value.primary {
    color: #409EFF;
}

.mini-card .value.success {
    color: #67C23A;
}

.chart-box {
    border: 1px solid #f0f2f5;
    border-radius: 8px;
    padding: 20px;
    margin-bottom: 24px;
}

.radar-chart {
    width: 100%;
    height: 350px;
}

/* 隐藏空状态提示 */
.empty-stats {
    text-align: center;
    color: #c0c4cc;
    padding: 20px;
}

/* 用户信息单元格美化 */
.user-info-cell {
    display: flex;
    align-items: center;
    gap: 12px;
}

.nickname {
    font-weight: 600;
    color: #2c3e50;
    font-size: 14px;
}

.username {
    font-size: 12px;
    color: #94a3b8;
}

/* 考试科目小标签 */
.subjects-tags {
    display: flex;
    flex-wrap: wrap;
    gap: 4px;
}

.mini-sub-tag {
    background-color: #f1f5f9;
    color: #64748b;
    font-size: 11px;
    padding: 1px 6px;
    border-radius: 4px;
    border: 1px solid #e2e8f0;
}

/* 注册日期文字 */
.full-time {
    font-family: 'Monaco', 'Courier New', monospace;
    font-size: 13px;
    color: #475569;
}

/* 总分文字加粗 */
.score-text {
    font-weight: bold;
    color: #e67e22;
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

/* 操作列按钮间距优化 */
:deep(.el-table__fixed-right) {
    height: 100% !important;
}

.el-button+.el-button {
    margin-left: 12px;
}
</style>

