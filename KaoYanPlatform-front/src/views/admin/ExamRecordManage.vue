<template>
    <div class="admin-container">
        <!-- 统计卡片 -->
        <el-row :gutter="20" class="stats-row">
            <el-col :span="6">
                <el-card shadow="hover" class="stats-card">
                    <div class="stats-content">
                        <div class="stats-icon" style="background: linear-gradient(135deg, #ecf5ff 0%, #d9ecff 100%)">
                            <el-icon :size="32" color="#409EFF">
                                <Document />
                            </el-icon>
                        </div>
                        <div class="stats-info">
                            <div class="stats-value">{{ stats.totalSessions || 0 }}</div>
                            <div class="stats-label">总考试次数</div>
                        </div>
                    </div>
                </el-card>
            </el-col>
            <el-col :span="6">
                <el-card shadow="hover" class="stats-card">
                    <div class="stats-content">
                        <div class="stats-icon" style="background: linear-gradient(135deg, #f0f9ff 0%, #e0f2ff 100%)">
                            <el-icon :size="32" color="#67C23A">
                                <CircleCheck />
                            </el-icon>
                        </div>
                        <div class="stats-info">
                            <div class="stats-value">{{ stats.completedSessions || 0 }}</div>
                            <div class="stats-label">已完成考试</div>
                        </div>
                    </div>
                </el-card>
            </el-col>
            <el-col :span="6">
                <el-card shadow="hover" class="stats-card">
                    <div class="stats-content">
                        <div class="stats-icon" style="background: linear-gradient(135deg, #fef0f0 0%, #fee2e2 100%)">
                            <el-icon :size="32" color="#F56C6C">
                                <Clock />
                            </el-icon>
                        </div>
                        <div class="stats-info">
                            <div class="stats-value">{{ stats.inProgressSessions || 0 }}</div>
                            <div class="stats-label">进行中考试</div>
                        </div>
                    </div>
                </el-card>
            </el-col>
            <el-col :span="6">
                <el-card shadow="hover" class="stats-card">
                    <div class="stats-content">
                        <div class="stats-icon" style="background: linear-gradient(135deg, #fdf6ec 0%, #fef0e6 100%)">
                            <el-icon :size="32" color="#E6A23C">
                                <TrendCharts />
                            </el-icon>
                        </div>
                        <div class="stats-info">
                            <div class="stats-value">{{ stats.avgScore ? stats.avgScore.toFixed(1) : '0.0' }}</div>
                            <div class="stats-label">平均分</div>
                        </div>
                    </div>
                </el-card>
            </el-col>
        </el-row>

        <!-- 考试记录卡片 -->
        <el-card shadow="never" class="table-card">
            <template #header>
                <div class="card-header">
                    <div class="text-header">
                        <span class="title-text">考试记录管理</span>
                        <div class="header-desc">查看和管理所有考试记录</div>
                    </div>
                </div>
            </template>

            <!-- 搜索表单 -->
            <el-form :inline="true" :model="searchForm" class="search-form">
                <el-form-item label="用户ID">
                    <el-input v-model="searchForm.userId" placeholder="请输入用户ID" clearable style="width: 200px" />
                </el-form-item>
                <el-form-item label="试卷ID">
                    <el-input v-model="searchForm.paperId" placeholder="请输入试卷ID" clearable style="width: 200px" />
                </el-form-item>
                <el-form-item label="考试状态">
                    <el-select v-model="searchForm.status" placeholder="全部" clearable style="width: 150px">
                        <el-option label="进行中" :value="0" />
                        <el-option label="已完成" :value="1" />
                    </el-select>
                </el-form-item>
                <el-form-item>
                    <el-button type="primary" icon="Search" @click="loadRecords">搜索</el-button>
                    <el-button icon="Refresh" @click="resetSearch">重置</el-button>
                </el-form-item>
            </el-form>

            <!-- 数据表格 -->
            <el-table :data="tableData" v-loading="loading" class="modern-table" stripe>
                <el-table-column prop="id" label="会话ID" width="200" show-overflow-tooltip />
                <el-table-column prop="userId" label="用户ID" width="120" align="center" />
                <el-table-column prop="paperId" label="试卷ID" width="200" show-overflow-tooltip />
                <el-table-column label="考试状态" width="100" align="center">
                    <template #default="scope">
                        <el-tag v-if="scope.row.status === 0" type="warning">进行中</el-tag>
                        <el-tag v-else-if="scope.row.status === 1" type="success">已完成</el-tag>
                        <el-tag v-else type="info">未知</el-tag>
                    </template>
                </el-table-column>
                <el-table-column prop="totalScore" label="得分" width="100" align="center">
                    <template #default="scope">
                        <span v-if="scope.row.totalScore !== null" class="score-value">{{ scope.row.totalScore }}</span>
                        <span v-else>-</span>
                    </template>
                </el-table-column>
                <el-table-column prop="switchCount" label="切屏次数" width="100" align="center" />
                <el-table-column prop="startTime" label="开始时间" width="160" align="center">
                    <template #default="scope">
                        <div class="full-time">{{ formatTime(scope.row.startTime) }}</div>
                    </template>
                </el-table-column>
                <el-table-column prop="submitTime" label="提交时间" width="160" align="center">
                    <template #default="scope">
                        <div class="full-time">{{ scope.row.submitTime ? formatTime(scope.row.submitTime) : '-' }}</div>
                    </template>
                </el-table-column>
                <el-table-column label="考试时长" width="120" align="center">
                    <template #default="scope">
                        {{ calculateDuration(scope.row) }}
                    </template>
                </el-table-column>
                <el-table-column label="操作" width="200" align="center" fixed="right">
                    <template #default="scope">
                        <el-button type="primary" link size="small" @click="viewDetails(scope.row)">
                            查看详情
                        </el-button>
                        <el-button v-if="scope.row.status === 1 && scope.row.aiSummary" type="success" link size="small" @click="viewAISummary(scope.row)">
                            AI总结
                        </el-button>
                    </template>
                </el-table-column>
            </el-table>

            <!-- 分页 -->
            <div class="pagination-container">
                <el-pagination
                    :current-page="pageNum"
                    :page-size="pageSize"
                    :page-sizes="[10, 20, 50, 100]"
                    :total="total"
                    layout="total, sizes, prev, pager, next, jumper"
                    @size-change="handleSizeChange"
                    @current-change="handlePageChange"
                />
            </div>
        </el-card>

        <!-- 详情对话框 -->
        <el-dialog v-model="detailDialogVisible" title="考试详情" width="900px" destroy-on-close>
            <div v-if="currentSession">
                <!-- 基本信息 -->
                <div class="detail-section">
                    <div class="section-title">
                        <el-icon><InfoFilled /></el-icon>
                        <span>基本信息</span>
                    </div>
                    <el-descriptions :column="2" border class="detail-descriptions">
                        <el-descriptions-item label="会话ID">{{ currentSession.id }}</el-descriptions-item>
                        <el-descriptions-item label="用户ID">{{ currentSession.userId }}</el-descriptions-item>
                        <el-descriptions-item label="试卷ID">{{ currentSession.paperId }}</el-descriptions-item>
                        <el-descriptions-item label="考试状态">
                            <el-tag v-if="currentSession.status === 0" type="warning">进行中</el-tag>
                            <el-tag v-else type="success">已完成</el-tag>
                        </el-descriptions-item>
                        <el-descriptions-item label="得分">
                            <span v-if="currentSession.totalScore !== null" class="score-value-large">{{ currentSession.totalScore }}</span>
                            <span v-else>-</span>
                        </el-descriptions-item>
                        <el-descriptions-item label="切屏次数">{{ currentSession.switchCount }}</el-descriptions-item>
                        <el-descriptions-item label="开始时间">{{ currentSession.startTime }}</el-descriptions-item>
                        <el-descriptions-item label="提交时间">{{ currentSession.submitTime || '-' }}</el-descriptions-item>
                    </el-descriptions>
                </div>

                <!-- AI总结 -->
                <div v-if="currentSession.aiSummary" class="detail-section">
                    <div class="section-title ai-section-title">
                        <el-icon><MagicStick /></el-icon>
                        <span>AI 智能总结</span>
                    </div>
                    <div class="ai-summary-content">{{ currentSession.aiSummary }}</div>
                </div>

                <!-- 答题明细 -->
                <div class="detail-section">
                    <div class="section-title">
                        <el-icon><List /></el-icon>
                        <span>答题明细</span>
                    </div>
                    <el-table :data="answerDetails" v-loading="loadingDetails" stripe max-height="400" class="modern-table">
                        <el-table-column prop="questionId" label="题目ID" width="100" align="center" />
                        <el-table-column prop="userAnswer" label="用户答案" width="150" show-overflow-tooltip />
                        <el-table-column label="是否正确" width="100" align="center">
                            <template #default="scope">
                                <el-tag v-if="scope.row.isCorrect === 1" type="success" size="small">正确</el-tag>
                                <el-tag v-else-if="scope.row.isCorrect === 0" type="danger" size="small">错误</el-tag>
                                <el-tag v-else type="info" size="small">未判分</el-tag>
                            </template>
                        </el-table-column>
                        <el-table-column prop="scoreEarned" label="得分" width="100" align="center" />
                        <el-table-column prop="durationSeconds" label="用时(秒)" width="100" align="center" />
                        <el-table-column prop="aiFeedback" label="AI反馈" min-width="200" show-overflow-tooltip />
                    </el-table>
                </div>
            </div>
        </el-dialog>

        <!-- AI总结对话框 -->
        <el-dialog v-model="aiSummaryDialogVisible" title="AI 智能总结" width="700px" destroy-on-close>
            <div v-if="currentSession" class="ai-summary-modal">
                {{ currentSession.aiSummary }}
            </div>
        </el-dialog>
    </div>
</template>


<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Search, Document, CircleCheck, Clock, TrendCharts, InfoFilled, MagicStick, List } from '@element-plus/icons-vue'
import { getAllExamSessions, getExamStats, getSessionDetails } from '@/api/examSession'

// 数据定义
const loading = ref(false)
const tableData = ref([])
const total = ref(0)
const pageNum = ref(1)
const pageSize = ref(10)

const stats = ref({
    totalSessions: 0,
    completedSessions: 0,
    inProgressSessions: 0,
    avgScore: 0
})

const searchForm = ref({
    userId: '',
    paperId: '',
    status: null
})

const detailDialogVisible = ref(false)
const aiSummaryDialogVisible = ref(false)
const currentSession = ref(null)
const answerDetails = ref([])
const loadingDetails = ref(false)

// 加载统计数据
const loadStats = async () => {
    try {
        const res = await getExamStats()
        if (res.code === 200) {
            stats.value = res.data || stats.value
        }
    } catch (e) {
        console.error('获取统计数据失败', e)
    }
}

// 加载考试记录
const loadRecords = async () => {
    loading.value = true
    try {
        const params = {
            pageNum: pageNum.value,
            pageSize: pageSize.value,
            ...searchForm.value
        }
        // 移除空字符串参数
        Object.keys(params).forEach(key => {
            if (params[key] === '' || params[key] === null || params[key] === undefined) {
                delete params[key]
            }
        })

        const res = await getAllExamSessions(params)
        if (res.code === 200) {
            tableData.value = res.data.records || []
            total.value = res.data.total || 0
        }
    } catch (e) {
        ElMessage.error('获取考试记录失败')
        console.error(e)
    } finally {
        loading.value = false
    }
}

// 重置搜索
const resetSearch = () => {
    searchForm.value = {
        userId: '',
        paperId: '',
        status: null
    }
    pageNum.value = 1
    loadRecords()
}

// 分页
const handlePageChange = (page) => {
    pageNum.value = page
    loadRecords()
}

const handleSizeChange = (size) => {
    pageSize.value = size
    pageNum.value = 1
    loadRecords()
}

// 格式化时间
const formatTime = (timeStr) => {
    if (!timeStr) return '-'
    return timeStr.replace('T', ' ').substring(0, 19)
}

// 计算考试时长
const calculateDuration = (row) => {
    if (!row.startTime) return '-'
    const start = new Date(row.startTime)
    const end = row.submitTime ? new Date(row.submitTime) : new Date()
    const diff = Math.floor((end - start) / 1000 / 60) // 分钟
    return `${diff} 分钟`
}

// 查看详情
const viewDetails = async (row) => {
    currentSession.value = row
    detailDialogVisible.value = true
    loadingDetails.value = true

    try {
        const res = await getSessionDetails(row.id)
        if (res.code === 200) {
            answerDetails.value = res.data || []
        }
    } catch (e) {
        ElMessage.error('获取答题明细失败')
        console.error(e)
    } finally {
        loadingDetails.value = false
    }
}

// 查看AI总结
const viewAISummary = (row) => {
    currentSession.value = row
    aiSummaryDialogVisible.value = true
}

// 初始化
onMounted(() => {
    loadStats()
    loadRecords()
})
</script>

<style scoped>
/* ==================== 统计卡片 ==================== */
.stats-row {
    margin-bottom: 20px;
}

.stats-card {
    border-radius: 12px;
    transition: all 0.3s ease;
    border: 1px solid #e8ecef;
}

.stats-card:hover {
    transform: translateY(-4px);
    box-shadow: 0 8px 24px rgba(0, 0, 0, 0.12);
}

.stats-content {
    display: flex;
    align-items: center;
    padding: 10px 0;
}

.stats-icon {
    width: 64px;
    height: 64px;
    border-radius: 16px;
    display: flex;
    align-items: center;
    justify-content: center;
    margin-right: 16px;
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
}

.stats-info {
    flex: 1;
}

.stats-value {
    font-size: 32px;
    font-weight: 700;
    color: #1f2937;
    line-height: 1.2;
    background: linear-gradient(135deg, #1f2937 0%, #4b5563 100%);
    -webkit-background-clip: text;
    -webkit-text-fill-color: transparent;
    background-clip: text;
}

.stats-label {
    font-size: 14px;
    color: #6b7280;
    margin-top: 6px;
    font-weight: 500;
}

/* ==================== 卡片头部 ==================== */
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
}

/* ==================== 搜索表单 ==================== */
.search-form {
    background: #fcfcfd;
    padding: 18px 20px 0;
    border-radius: 8px;
    margin-bottom: 20px;
    border: 1px solid #ebeef5;
}

/* ==================== 表格样式 ==================== */
.table-card {
    border-radius: 12px;
    border: 1px solid #e8ecef;
}

.modern-table {
    font-size: 14px;
    border-radius: 8px;
    overflow: hidden;
}

.modern-table :deep(.el-table__header th) {
    background-color: #f8fafc !important;
    color: #475569;
    font-weight: 600;
}

.score-value {
    font-weight: 600;
    color: #409eff;
    font-size: 15px;
}

.full-time {
    font-family: 'Monaco', 'Courier New', monospace;
    font-size: 13px;
    color: #475569;
}

/* ==================== 分页样式 ==================== */
.pagination-container {
    margin-top: 25px;
    padding: 15px 20px;
    display: flex;
    justify-content: center;
    background: #fdfdfd;
    border-radius: 0 0 8px 8px;
}

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

/* ==================== 详情对话框 ==================== */
.detail-section {
    margin-bottom: 24px;
}

.section-title {
    display: flex;
    align-items: center;
    gap: 8px;
    font-size: 16px;
    font-weight: 600;
    color: #303133;
    margin-bottom: 16px;
    padding-bottom: 12px;
    border-bottom: 2px solid #e8ecef;
}

.section-title .el-icon {
    color: #409eff;
    font-size: 18px;
}

.ai-section-title {
    color: #409eff;
    border-bottom-color: #409eff;
}

.ai-section-title .el-icon {
    color: #409eff;
}

.detail-descriptions {
    border-radius: 8px;
    overflow: hidden;
}

.score-value-large {
    font-weight: 700;
    color: #409eff;
    font-size: 18px;
}

/* ==================== AI总结样式 ==================== */
.ai-summary-content {
    padding: 16px;
    background: linear-gradient(135deg, #f0f9ff 0%, #e0f2fe 100%);
    border-left: 4px solid #409eff;
    border-radius: 8px;
    color: #606266;
    line-height: 1.8;
    white-space: pre-wrap;
}

.ai-summary-modal {
    padding: 20px;
    background: linear-gradient(135deg, #f8f9fa 0%, #ffffff 100%);
    border-radius: 8px;
    line-height: 1.8;
    color: #606266;
    white-space: pre-wrap;
    max-height: 500px;
    overflow-y: auto;
}

/* ==================== 输入框圆角 ==================== */
:deep(.el-input__wrapper) {
    border-radius: 6px !important;
}
</style>
