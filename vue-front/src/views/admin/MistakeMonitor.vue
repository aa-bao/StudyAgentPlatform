<template>
    <div class="mistake-monitor-container">
        <el-card shadow="never" class="header-card">
            <template #header>
                <div class="card-header">
                    <span class="title-text">错题监控中心</span>
                    <div class="header-desc">错题监控中心，用于查看用户错题的统计信息（如错题数、错题题目数、涉及用户数、涉及科目数等）
                    </div>
                </div>
            </template>

            <!-- 统计卡片 -->
            <el-row :gutter="20" class="stats-row">
                <el-col :span="6">
                    <div class="stat-card">
                        <div class="stat-value">{{ totalErrorCount }}</div>
                        <div class="stat-label">总错误次数</div>
                    </div>
                </el-col>
                <el-col :span="6">
                    <div class="stat-card">
                        <div class="stat-value">{{ totalMistakeQuestions }}</div>
                        <div class="stat-label">错题题目数</div>
                    </div>
                </el-col>
                <el-col :span="6">
                    <div class="stat-card">
                        <div class="stat-value">{{ totalAffectedUsers }}</div>
                        <div class="stat-label">涉及用户数</div>
                    </div>
                </el-col>
                <el-col :span="6">
                    <div class="stat-card">
                        <div class="stat-value">{{ subjectCount }}</div>
                        <div class="stat-label">涉及科目数</div>
                    </div>
                </el-col>
            </el-row>
        </el-card>

        <!-- 热力图 -->
        <el-card shadow="never" class="heatmap-card">
            <template #header>
                <span class="title-text">科目错题热力图</span>
            </template>
            <div v-loading="loading" class="heatmap-container">
                <div
                    v-for="item in heatmapData"
                    :key="item.subjectId"
                    class="heatmap-item"
                    :style="{
                        backgroundColor: getHeatmapColor(item.totalErrorCount),
                        flex: item.totalErrorCount
                    }"
                    @click="showSubjectDetail(item)"
                >
                    <div class="heatmap-subject">{{ item.subjectName }}</div>
                    <div class="heatmap-count">{{ item.totalErrorCount }}次</div>
                </div>
            </div>
        </el-card>

        <!-- 高频错题 TOP 20 -->
        <el-card shadow="never" class="hot-questions-card">
            <template #header>
                <span class="title-text">高频错题 TOP 20</span>
            </template>
            <el-table :data="hotMistakes" border stripe v-loading="loading" max-height="500">
                <el-table-column prop="questionId" label="题目ID" width="80" align="center" />
                <el-table-column prop="questionContent" label="题目内容" show-overflow-tooltip />
                <el-table-column prop="totalErrorCount" label="错误次数" width="100" align="center">
                    <template #default="scope">
                        <el-tag type="danger">{{ scope.row.totalErrorCount }}</el-tag>
                    </template>
                </el-table-column>
                <el-table-column prop="errorUserCount" label="错误人数" width="100" align="center" />
                <el-table-column label="操作" width="150" align="center">
                    <template #default="scope">
                        <el-button
                            size="small"
                            type="primary"
                            link
                            @click="goToQuestionEdit(scope.row.questionId)"
                        >
                            编辑题目
                        </el-button>
                    </template>
                </el-table-column>
            </el-table>
        </el-card>
    </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import request from '@/utils/request'
import { ElMessage } from 'element-plus'

const router = useRouter()
const loading = ref(false)
const heatmapData = ref([])
const hotMistakes = ref([])

// 计算统计数据
const totalErrorCount = computed(() => {
    return heatmapData.value.reduce((sum, item) => sum + item.totalErrorCount, 0)
})

const totalMistakeQuestions = computed(() => {
    return heatmapData.value.reduce((sum, item) => sum + item.mistakeQuestionCount, 0)
})

const totalAffectedUsers = computed(() => {
    return heatmapData.value.reduce((sum, item) => sum + item.affectedUserCount, 0)
})

const subjectCount = computed(() => heatmapData.value.length)

// 获取热力图颜色（基于错误次数）
const getHeatmapColor = (count) => {
    const maxCount = Math.max(...heatmapData.value.map(item => item.totalErrorCount))
    const ratio = count / maxCount

    // 从浅红到深红
    const r = 255
    const g = Math.round(255 * (1 - ratio * 0.8))
    const b = Math.round(255 * (1 - ratio * 0.8))

    return `rgb(${r}, ${g}, ${b})`
}

// 加载数据
const loadData = async () => {
    loading.value = true
    try {
        // 获取热力图数据
        const heatmapRes = await request.get('/admin/mistake-heatmap')
        heatmapData.value = heatmapRes.data || heatmapRes || []
        console.log('heatmapData：', heatmapData.value)

        // 获取高频错题
        const hotRes = await request.get('/admin/hot-mistakes?limit=20')
        hotMistakes.value = hotRes.data || hotRes || []

        console.log('热力图数据:', heatmapData.value)
        console.log('高频错题:', hotMistakes.value)
    } catch (e) {
        ElMessage.error('获取数据失败')
        console.error(e)
    } finally {
        loading.value = false
    }
}

// 显示科目详情
const showSubjectDetail = (item) => {
    ElMessage.info(`${item.subjectName}: ${item.totalErrorCount}次错误，${item.mistakeQuestionCount}道错题`)
}

// 跳转到题目编辑页面
const goToQuestionEdit = (questionId) => {
    router.push({
        path: '/admin/questions-manage',
        query: { editId: questionId }
    })
}

onMounted(() => {
    loadData()
})
</script>

<style scoped>
.header-card,
.heatmap-card,
.hot-questions-card {
    border-radius: 8px;
    border: none;
    margin-bottom: 20px;
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

.stats-row {
    margin-bottom: 10px;
}

.stat-card {
    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
    padding: 20px;
    border-radius: 8px;
    text-align: center;
    color: white;
}

.stat-value {
    font-size: 32px;
    font-weight: 600;
    margin-bottom: 8px;
}

.stat-label {
    font-size: 14px;
    opacity: 0.9;
}

.heatmap-container {
    display: flex;
    gap: 10px;
    padding: 20px 0;
    min-height: 100px;
}

.heatmap-item {
    padding: 15px;
    border-radius: 4px;
    color: white;
    text-align: center;
    cursor: pointer;
    transition: all 0.3s;
    display: flex;
    flex-direction: column;
    justify-content: center;
    min-width: 80px;
}

.heatmap-item:hover {
    transform: translateY(-5px);
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
}

.heatmap-subject {
    font-size: 14px;
    font-weight: 600;
    margin-bottom: 8px;
}

.heatmap-count {
    font-size: 12px;
    opacity: 0.9;
}
</style>
