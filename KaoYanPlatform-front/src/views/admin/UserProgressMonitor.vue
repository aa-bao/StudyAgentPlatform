<template>
    <div class="admin-container">
        <!-- 统计卡片 -->
        <el-row :gutter="20" class="stats-row">
            <el-col :span="6">
                <el-card shadow="hover" class="stats-card">
                    <div class="stats-content">
                        <div class="stats-icon" style="background: linear-gradient(135deg, #ecf5ff 0%, #d9ecff 100%)">
                            <el-icon :size="32" color="#409EFF">
                                <User />
                            </el-icon>
                        </div>
                        <div class="stats-info">
                            <div class="stats-value">{{ stats.totalUsers || 0 }}</div>
                            <div class="stats-label">学习用户数</div>
                        </div>
                    </div>
                </el-card>
            </el-col>
            <el-col :span="6">
                <el-card shadow="hover" class="stats-card">
                    <div class="stats-content">
                        <div class="stats-icon" style="background: linear-gradient(135deg, #f0f9ff 0%, #e0f2ff 100%)">
                            <el-icon :size="32" color="#67C23A">
                                <DocumentChecked />
                            </el-icon>
                        </div>
                        <div class="stats-info">
                            <div class="stats-value">{{ stats.totalFinished || 0 }}</div>
                            <div class="stats-label">总完成题目数</div>
                        </div>
                    </div>
                </el-card>
            </el-col>
            <el-col :span="6">
                <el-card shadow="hover" class="stats-card">
                    <div class="stats-content">
                        <div class="stats-icon" style="background: linear-gradient(135deg, #fdf6ec 0%, #fef0e6 100%)">
                            <el-icon :size="32" color="#E6A23C">
                                <CircleCheck />
                            </el-icon>
                        </div>
                        <div class="stats-info">
                            <div class="stats-value">{{ stats.totalCorrect || 0 }}</div>
                            <div class="stats-label">总正确题目数</div>
                        </div>
                    </div>
                </el-card>
            </el-col>
            <el-col :span="6">
                <el-card shadow="hover" class="stats-card">
                    <div class="stats-content">
                        <div class="stats-icon" style="background: linear-gradient(135deg, #fef0f0 0%, #fee2e2 100%)">
                            <el-icon :size="32" color="#F56C6C">
                                <TrendCharts />
                            </el-icon>
                        </div>
                        <div class="stats-info">
                            <div class="stats-value">{{ stats.avgAccuracy ? stats.avgAccuracy.toFixed(1) : '0.0' }}%</div>
                            <div class="stats-label">平均正确率</div>
                        </div>
                    </div>
                </el-card>
            </el-col>
        </el-row>

        <!-- 学习排行榜 -->
        <el-card shadow="never" class="ranking-card table-card">
            <template #header>
                <div class="card-header">
                    <div class="text-header">
                        <span class="title-text">🏆 学习排行榜</span>
                        <div class="header-desc">Top 10 学习之星</div>
                    </div>
                    <div class="header-btns">
                        <el-button type="primary" icon="Refresh" @click="loadRanking">刷新</el-button>
                    </div>
                </div>
            </template>

            <el-table :data="rankingData" v-loading="loadingRanking" class="modern-table" stripe>
                <el-table-column label="排名" width="100" align="center">
                    <template #default="scope">
                        <div v-if="scope.$index < 3" class="rank-badge">
                            <el-tag v-if="scope.$index === 0" type="danger" effect="dark" size="large">🥇 1</el-tag>
                            <el-tag v-else-if="scope.$index === 1" type="warning" effect="dark" size="large">🥈 2</el-tag>
                            <el-tag v-else-if="scope.$index === 2" type="success" effect="dark" size="large">🥉 3</el-tag>
                        </div>
                        <div v-else class="rank-number">{{ scope.$index + 1 }}</div>
                    </template>
                </el-table-column>
                <el-table-column prop="user_id" label="用户ID" width="150" align="center" />
                <el-table-column label="用户昵称" width="180" align="center">
                    <template #default="scope">
                        <span class="user-name">{{ getUserName(scope.row.user_id) }}</span>
                    </template>
                </el-table-column>
                <el-table-column prop="total_finished" label="完成题目数" width="150" align="center">
                    <template #default="scope">
                        <span class="stat-number">{{ scope.row.total_finished }}</span>
                    </template>
                </el-table-column>
                <el-table-column prop="total_correct" label="正确题目数" width="150" align="center">
                    <template #default="scope">
                        <span class="stat-number correct">{{ scope.row.total_correct }}</span>
                    </template>
                </el-table-column>
                <el-table-column label="正确率" width="200" align="center">
                    <template #default="scope">
                        <div class="accuracy-wrapper">
                            <el-progress
                                :percentage="calculateAccuracy(scope.row.total_correct, scope.row.total_finished)"
                                :color="getProgressColor(calculateAccuracy(scope.row.total_correct, scope.row.total_finished))"
                                :stroke-width="12"
                            />
                        </div>
                    </template>
                </el-table-column>
            </el-table>
        </el-card>

        <!-- 学习进度详情 -->
        <el-card shadow="never" class="table-card">
            <template #header>
                <div class="card-header">
                    <div class="text-header">
                        <span class="title-text">学习进度详情</span>
                        <div class="header-desc">查看和管理用户学习进度</div>
                    </div>
                </div>
            </template>

            <!-- 搜索表单 -->
            <el-form :inline="true" :model="searchForm" class="search-form">
                <el-form-item label="用户ID">
                    <el-input v-model="searchForm.userId" placeholder="请输入用户ID" clearable style="width: 200px" />
                </el-form-item>
                <el-form-item label="科目">
                    <el-select v-model="searchForm.subjectId" placeholder="全部科目" clearable style="width: 200px">
                        <el-option
                            v-for="subject in allSubjects"
                            :key="subject.id"
                            :label="subject.name"
                            :value="subject.id"
                        />
                    </el-select>
                </el-form-item>
                <el-form-item>
                    <el-button type="primary" icon="Search" @click="loadProgress">搜索</el-button>
                    <el-button icon="Refresh" @click="resetSearch">重置</el-button>
                </el-form-item>
            </el-form>

            <!-- 数据表格 -->
            <el-table :data="tableData" v-loading="loading" class="modern-table" stripe>
                <el-table-column prop="id" label="记录ID" width="120" align="center" show-overflow-tooltip />
                <el-table-column prop="userId" label="用户ID" width="150" align="center" />
                <el-table-column label="用户昵称" width="180" align="center">
                    <template #default="scope">
                        <span class="user-name">{{ getUserName(scope.row.userId) }}</span>
                    </template>
                </el-table-column>
                <el-table-column prop="subjectId" label="科目ID" width="120" align="center" />
                <el-table-column label="科目名称" min-width="200" show-overflow-tooltip>
                    <template #default="scope">
                        {{ getSubjectName(scope.row.subjectId) }}
                    </template>
                </el-table-column>
                <el-table-column prop="finishedCount" label="完成题目数" width="140" align="center">
                    <template #default="scope">
                        <el-tag type="primary" effect="plain">{{ scope.row.finishedCount || 0 }}</el-tag>
                    </template>
                </el-table-column>
                <el-table-column prop="correctCount" label="正确题目数" width="140" align="center">
                    <template #default="scope">
                        <el-tag type="success" effect="plain">{{ scope.row.correctCount || 0 }}</el-tag>
                    </template>
                </el-table-column>
                <el-table-column label="正确率" width="220" align="center">
                    <template #default="scope">
                        <div class="accuracy-wrapper">
                            <el-progress
                                :percentage="calculateAccuracy(scope.row.correctCount, scope.row.finishedCount)"
                                :color="getProgressColor(calculateAccuracy(scope.row.correctCount, scope.row.finishedCount))"
                                :stroke-width="12"
                            />
                        </div>
                    </template>
                </el-table-column>
                <el-table-column prop="updateTime" label="更新时间" width="180" align="center">
                    <template #default="scope">
                        <div class="full-time">{{ formatTime(scope.row.updateTime) }}</div>
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
    </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Search, Refresh, User, DocumentChecked, CircleCheck, TrendCharts } from '@element-plus/icons-vue'
import { getAllUserProgress, getProgressStats, getLearningRanking } from '@/api/userProgress'
import request from '@/utils/request'

// 数据定义
const loading = ref(false)
const tableData = ref([])
const total = ref(0)
const pageNum = ref(1)
const pageSize = ref(10)

const loadingRanking = ref(false)
const rankingData = ref([])

const stats = ref({
    totalUsers: 0,
    totalRecords: 0,
    totalFinished: 0,
    totalCorrect: 0,
    avgAccuracy: 0
})

const searchForm = ref({
    userId: null,
    subjectId: null
})

// 用户缓存
const userCache = ref(new Map())
// 科目缓存
const subjectCache = ref(new Map())
const allSubjects = ref([])
const examSpecs = ref([])

// 加载统计数据
const loadStats = async () => {
    try {
        const res = await getProgressStats()
        if (res.code === 200) {
            stats.value = res.data || stats.value
        }
    } catch (e) {
        console.error('获取统计数据失败', e)
    }
}

// 加载排行榜
const loadRanking = async () => {
    loadingRanking.value = true
    try {
        const res = await getLearningRanking(10)
        if (res.code === 200) {
            rankingData.value = res.data || []
            // 预加载用户信息
            rankingData.value.forEach(item => {
                loadUserInfo(item.user_id)
            })
        }
    } catch (e) {
        ElMessage.error('获取排行榜失败')
        console.error(e)
    } finally {
        loadingRanking.value = false
    }
}

// 加载学习进度
const loadProgress = async () => {
    loading.value = true
    try {
        const params = {
            pageNum: pageNum.value,
            pageSize: pageSize.value,
            ...searchForm.value
        }
        // 移除空值参数
        Object.keys(params).forEach(key => {
            if (params[key] === null || params[key] === undefined || params[key] === '') {
                delete params[key]
            }
        })

        const res = await getAllUserProgress(params)
        if (res.code === 200) {
            tableData.value = res.data.records || []
            total.value = res.data.total || 0

            // 预加载用户信息
            tableData.value.forEach(item => {
                if (item.userId) {
                    loadUserInfo(item.userId)
                }
            })
        }
    } catch (e) {
        ElMessage.error('获取学习进度失败')
        console.error(e)
    } finally {
        loading.value = false
    }
}

// 加载科目树
const loadSubjects = async () => {
    try {
        const res = await request.get('/subject/manage-tree')
        const treeData = res.data || res || []
        examSpecs.value = treeData

        // 提取所有科目（扁平化）
        const flattenSubjects = (nodes) => {
            let result = []
            nodes.forEach(node => {
                result.push(node)
                if (node.children && node.children.length > 0) {
                    result = result.concat(flattenSubjects(node.children))
                }
            })
            return result
        }

        allSubjects.value = flattenSubjects(treeData)
    } catch (e) {
        console.error('获取科目列表失败', e)
    }
}

// 加载用户信息
const loadUserInfo = async (userId) => {
    if (userCache.value.has(userId)) {
        return userCache.value.get(userId)
    }

    try {
        const res = await request.get('/user/userInfo', { params: { userId } })
        if (res.code === 200 && res.data) {
            const userInfo = res.data
            userCache.value.set(userId, userInfo)
            return userInfo
        }
    } catch (e) {
        console.error(`获取用户${userId}信息失败`, e)
    }
    return null
}

// 获取用户昵称
const getUserName = (userId) => {
    const user = userCache.value.get(userId)
    return user ? user.nickname || user.username : `用户${userId}`
}

// 获取科目名称
const getSubjectName = (subjectId) => {
    if (!subjectId) return '-'

    // 先从缓存查找
    if (subjectCache.value.has(subjectId)) {
        return subjectCache.value.get(subjectId)
    }

    // 递归查找科目树
    const findSubject = (nodes, targetId) => {
        for (const node of nodes) {
            if (String(node.id) === String(targetId)) {
                return node
            }
            if (node.children && node.children.length > 0) {
                const found = findSubject(node.children, targetId)
                if (found) return found
            }
        }
        return null
    }

    const subject = findSubject(examSpecs.value, subjectId)
    if (subject) {
        subjectCache.value.set(subjectId, subject.name)
        return subject.name
    }

    return `科目${subjectId}`
}

// 计算正确率
const calculateAccuracy = (correct, finished) => {
    if (!finished || finished === 0) return 0
    return Math.round((correct / finished) * 100)
}

// 获取进度条颜色
const getProgressColor = (percentage) => {
    if (percentage >= 80) return '#67C23A'
    if (percentage >= 60) return '#E6A23C'
    return '#F56C6C'
}

// 格式化时间
const formatTime = (timeStr) => {
    if (!timeStr) return '-'
    return timeStr.replace('T', ' ').substring(0, 19)
}

// 分页
const handlePageChange = (page) => {
    pageNum.value = page
    loadProgress()
}

const handleSizeChange = (size) => {
    pageSize.value = size
    pageNum.value = 1
    loadProgress()
}

// 重置搜索
const resetSearch = () => {
    searchForm.value = {
        userId: null,
        subjectId: null
    }
    pageNum.value = 1
    loadProgress()
}

// 初始化
onMounted(() => {
    loadStats()
    loadRanking()
    loadSubjects()
    loadProgress()
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

.header-btns {
    display: flex;
    gap: 10px;
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
    margin-bottom: 20px;
}

.ranking-card {
    margin-bottom: 20px;
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

/* ==================== 排行榜样式 ==================== */
.rank-badge {
    font-size: 20px;
    font-weight: bold;
}

.rank-number {
    font-size: 18px;
    font-weight: 700;
    color: #409eff;
    background: linear-gradient(135deg, #e0f2fe 0%, #bae6fd 100%);
    width: 40px;
    height: 40px;
    line-height: 40px;
    border-radius: 50%;
    margin: 0 auto;
}

.user-name {
    font-weight: 500;
    color: #475569;
}

.stat-number {
    font-weight: 600;
    font-size: 16px;
    color: #409eff;
}

.stat-number.correct {
    color: #67c23a;
}

.accuracy-wrapper {
    padding: 0 10px;
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

/* ==================== 进度条美化 ==================== */
:deep(.el-progress__text) {
    font-weight: 600;
    font-size: 14px !important;
}

:deep(.el-progress-bar__outer) {
    border-radius: 6px;
}

:deep(.el-progress-bar__inner) {
    border-radius: 6px;
}

/* ==================== 输入框圆角 ==================== */
:deep(.el-input__wrapper) {
    border-radius: 6px !important;
}

/* ==================== Tag美化 ==================== */
:deep(.el-tag--dark) {
    border-radius: 6px;
    font-weight: 600;
}
</style>
