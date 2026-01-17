<template>
    <div class="admin-home">
        <el-row :gutter="20">
            <el-col :span="6" v-for="item in statsCards" :key="item.title">
                <el-card shadow="hover" class="stat-card">
                    <div class="stat-content">
                        <el-icon class="stat-icon" :style="{ color: item.color }">
                            <component :is="item.icon" />
                        </el-icon>
                        <div class="stat-info">
                            <div class="stat-title">{{ item.title }}</div>
                            <div class="stat-number">{{ item.value }}</div>
                        </div>
                    </div>
                </el-card>
            </el-col>
        </el-row>

        <el-row :gutter="20" style="margin-top: 20px;">
            <el-col :span="12">
                <el-card header="题目科目分布" shadow="never">
                    <div id="pieChart" style="height: 350px;"></div>
                </el-card>
            </el-col>
            <el-col :span="12">
                <el-card header="系统活跃趋势" shadow="never">
                    <div id="lineChart" style="height: 350px;"></div>
                </el-card>
            </el-col>
        </el-row>
    </div>
</template>

<script setup>
import { ref, onMounted, nextTick } from 'vue'
import request from '@/utils/request'
import * as echarts from 'echarts'
import { Memo, User, Reading, TrendCharts } from '@element-plus/icons-vue'

const statsCards = ref([
    { title: '题目总数', value: 0, icon: 'Memo', color: '#409EFF' },
    { title: '注册用户', value: 0, icon: 'User', color: '#67C23A' },
    { title: '练习总量', value: 0, icon: 'Reading', color: '#E6A23C' },
    { title: '今日活跃', value: 0, icon: 'TrendCharts', color: '#F56C6C' }
])

const loadStats = async () => {
    const userStr = localStorage.getItem("userInfo")
    const user = userStr ? JSON.parse(userStr) : null

    // 如果是普通用户且不是 admin，请求时带上 userId
    const params = {}
    if (user && user.role !== 'admin') {
        params.userId = user.id
    }

    try {
        // --- 核心修改点：发起请求并获取返回数据 ---
        const res = await request.get('/admin/statistics', { params })
        const data = res.data || res  // 适配不同的 Result 封装格式

        if (data) {
            // 更新卡片数据
            statsCards.value[0].value = data.questionCount || 0
            statsCards.value[1].value = data.userCount || 0
            statsCards.value[2].value = data.exerciseCount || 0
            statsCards.value[3].value = data.todayActive || 0

            // 初始化图表
            if (data.subjectData) {
                initPieChart(data.subjectData)
            }
            initLineChart()
        }
    } catch (error) {
        console.error("加载统计数据失败", error)
    }
}
const initPieChart = (data) => {
    const chart = echarts.init(document.getElementById('pieChart'))
    chart.setOption({
        tooltip: { trigger: 'item' },
        legend: { bottom: '5%', left: 'center' },
        series: [{
            name: '科目占比',
            type: 'pie',
            radius: ['40%', '70%'],
            avoidLabelOverlap: false,
            itemStyle: { borderRadius: 10, borderColor: '#fff', borderWidth: 2 },
            label: { show: false },
            data: data
        }]
    })
}

const initLineChart = () => {
    const chart = echarts.init(document.getElementById('lineChart'))
    chart.setOption({
        xAxis: { type: 'category', data: ['Mon', 'Tue', 'Wed', 'Thu', 'Fri', 'Sat', 'Sun'] },
        yAxis: { type: 'value' },
        tooltip: { trigger: 'axis' },
        series: [{
            data: [150, 230, 224, 218, 135, 147, 260],
            type: 'line',
            smooth: true,
            color: '#409EFF',
            areaStyle: { opacity: 0.1 }
        }]
    })
}

onMounted(() => {
    loadStats()
})
</script>

<style scoped>
.admin-home {
    min-height: 100vh;
}

.stat-card {
    border: none;
    border-radius: 12px;
}

.stat-content {
    display: flex;
    align-items: center;
    padding: 10px;
}

.stat-icon {
    font-size: 40px;
    margin-right: 20px;
    opacity: 0.8;
}

.stat-title {
    font-size: 14px;
    color: #909399;
    margin-bottom: 4px;
}

.stat-number {
    font-size: 24px;
    font-weight: bold;
    color: #303133;
}
</style>