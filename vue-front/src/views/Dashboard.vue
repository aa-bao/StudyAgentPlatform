<template>
    <div class="dashboard-container">
        <!-- 顶部筛选区域 -->
        <div class="dashboard-header">
            <div class="welcome-section">
                <h1>数据看板 </h1>
                <span class="subtitle">学习数据全景分析</span>
                <p class="last-update">最后更新: {{ lastUpdateTime }}</p>
            </div>

            <div class="header-actions">
                <el-date-picker v-model="dateRange" type="daterange" range-separator="至" start-placeholder="开始日期"
                    end-placeholder="结束日期" :shortcuts="shortcuts" @change="handleDateChange" />
                <el-select v-model="subjectFilter" placeholder="选择科目" @change="handleSubjectChange">
                    <el-option label="全部科目" value="all" />
                    <el-option v-for="subject in allSubjects" :key="subject.id" :label="subject.name"
                        :value="subject.id" />
                </el-select>
                <el-button type="primary" @click="exportReport">
                    <el-icon :size="24">
                        <Download />
                    </el-icon>
                    导出学习报告
                </el-button>
            </div>
        </div>

        <!-- 第一行：核心指标卡片 -->
        <div class="stats-grid">
            <div class="stat-card" @click="goToPractice()">
                <div class="stat-header">
                    <h3>累计刷题</h3>
                    <el-icon name="Collection" class="stat-icon" />
                </div>
                <div class="stat-value">{{ coreStats.totalQuestions }}</div>
                <div class="stat-trend positive">
                    <el-icon name="Top" /> +{{ coreStats.todayQuestions }} 今日
                </div>
            </div>

            <div class="stat-card" @click="viewMistakeAnalysis()">
                <div class="stat-header">
                    <h3>总体正确率</h3>
                    <el-icon name="Promotion" class="stat-icon" />
                </div>
                <div class="stat-value">{{ coreStats.accuracy }}%</div>
                <div class="stat-trend" :class="coreStats.accuracyTrend > 0 ? 'positive' : 'negative'">
                    <el-icon :name="coreStats.accuracyTrend > 0 ? 'Top' : 'Bottom'" />
                    {{ Math.abs(coreStats.accuracyTrend) }}% {{ coreStats.accuracyTrend > 0 ? '上升' : '下降' }}
                </div>
            </div>

            <div class="stat-card" @click="viewStudyTime()">
                <div class="stat-header">
                    <h3>累计学习时长</h3>
                    <el-icon name="Timer" class="stat-icon" />
                </div>
                <div class="stat-value">{{ coreStats.totalStudyHours }}h</div>
                <div class="stat-trend positive">
                    <el-icon name="Top" /> +{{ coreStats.todayStudyHours }}h 今日
                </div>
            </div>

            <div class="stat-card" @click="viewMistakes()">
                <div class="stat-header">
                    <h3>错题本数量</h3>
                    <el-icon name="Failed" class="stat-icon" />
                </div>
                <div class="stat-value">{{ coreStats.mistakeCount }}</div>
                <div class="stat-trend negative">
                    <el-icon name="Bottom" /> +{{ coreStats.newMistakes }} 今日新增
                </div>
            </div>
        </div>

        <!-- 第二行：主要图表 -->
        <div class="charts-row">
            <div class="chart-container">
                <div class="chart-header">
                    <h2>学科能力雷达图</h2>
                    <el-dropdown @command="handleRadarCommand">
                        <el-button type="primary" text>
                            操作 <el-icon name="ArrowDownBold" />
                        </el-button>
                        <template #dropdown>
                            <el-dropdown-menu>
                                <el-dropdown-item command="change-metrics">更改指标</el-dropdown-item>
                                <el-dropdown-item command="compare-with-avg">与平均值对比</el-dropdown-item>
                                <el-dropdown-item command="export-data">导出数据</el-dropdown-item>
                            </el-dropdown-menu>
                        </template>
                    </el-dropdown>
                </div>
                <div ref="radarChart" class="chart-area" style="width: 100%; height: 400px;"></div>
            </div>

            <div class="chart-container">
                <div class="chart-header">
                    <h2>错题热力分布</h2>
                    <el-dropdown @command="handleHeatmapCommand">
                        <el-button type="primary" text>
                            操作 <el-icon name="ArrowDownBold" />
                        </el-button>
                        <template #dropdown>
                            <el-dropdown-menu>
                                <el-dropdown-item command="filter-subject">按科目筛选</el-dropdown-item>
                                <el-dropdown-item command="sort-by-errors">按错误率排序</el-dropdown-item>
                                <el-dropdown-item command="export-data">导出数据</el-dropdown-item>
                            </el-dropdown-menu>
                        </template>
                    </el-dropdown>
                </div>
                <div ref="heatMap" class="chart-area" style="width: 100%; height: 400px;"></div>
            </div>
        </div>

        <!-- 第三行：学习趋势和错题分析 -->
        <div class="analysis-row">
            <div class="chart-container">
                <div class="chart-header">
                    <h2>学习趋势</h2>
                    <el-radio-group v-model="trendPeriod" @change="updateTrendChart">
                        <el-radio-button label="daily">日</el-radio-button>
                        <el-radio-button label="weekly">周</el-radio-button>
                        <el-radio-button label="monthly">月</el-radio-button>
                    </el-radio-group>
                </div>
                <div ref="trendChart" class="chart-area" style="width: 100%; height: 350px;"></div>
            </div>

            <div class="chart-container">
                <div class="chart-header">
                    <h2>高频错题分析</h2>
                    <el-button type="primary" text @click="generateWeaknessReport">
                        <el-icon name="Document" /> 生成弱点报告
                    </el-button>
                </div>
                <div class="hot-mistakes-container">
                    <el-table :data="hotMistakes" height="280">
                        <el-table-column prop="rank" label="排名" width="60" align="center" />
                        <el-table-column prop="content" label="题目" show-overflow-tooltip>
                            <template #default="{ row }">
                                <span class="question-content" @click="goToQuestion(row.id)">
                                    {{ row.content.substring(0, 30) }}...
                                </span>
                            </template>
                        </el-table-column>
                        <el-table-column prop="subjectName" label="科目" width="100" />
                        <el-table-column prop="errorRate" label="错误率" width="100">
                            <template #default="{ row }">
                                <el-tag :type="getErrorTagType(row.errorRate)">
                                    {{ row.errorRate }}%
                                </el-tag>
                            </template>
                        </el-table-column>
                        <el-table-column prop="errorCount" label="错误次数" width="100" />
                    </el-table>
                </div>
            </div>
        </div>

        <!-- 第四行：目标进度和排行榜 -->
        <div class="progress-row">
            <div class="chart-container">
                <div class="chart-header">
                    <h2>考研目标进度</h2>
                    <el-button type="primary" text @click="editTarget">
                        <el-icon name="Edit" /> 编辑目标
                    </el-button>
                </div>
                <div class="target-progress">
                    <div class="target-info">
                        <p class="target-school">目标院校: <span>{{ userInfo.target_school }}</span></p>
                        <p class="target-exam">考试类型: <span>{{ examType }}</span></p>
                        <p class="target-date">考试日期: <span>{{ userInfo.exam_date }}</span></p>
                    </div>

                    <el-progress :text-inside="true" :stroke-width="25" :percentage="targetProgress.percentage"
                        :color="getProgressColor(targetProgress.percentage)" status="success">
                        <span class="progress-text">{{ targetProgress.percentage }}%</span>
                    </el-progress>

                    <div class="progress-details">
                        <p>已完成 <span class="completed">{{ targetProgress.completed }}</span> /
                            计划 <span class="total">{{ targetProgress.total }}</span> 题目</p>
                        <p>预计达成目标日期: <span class="estimate-date">{{ targetProgress.estimateDate }}</span></p>
                    </div>
                </div>
            </div>

            <div class="chart-container">
                <div class="chart-header">
                    <h2>排行榜</h2>
                    <el-dropdown @command="handleRankingCommand">
                        <el-button type="primary" text>
                            排序 <el-icon name="Sort" />
                        </el-button>
                        <template #dropdown>
                            <el-dropdown-menu>
                                <el-dropdown-item command="by-correct-rate">按正确率</el-dropdown-item>
                                <el-dropdown-item command="by-total-questions">按刷题量</el-dropdown-item>
                                <el-dropdown-item command="by-study-time">按学习时长</el-dropdown-item>
                            </el-dropdown-menu>
                        </template>
                    </el-dropdown>
                </div>
                <div class="ranking-container">
                    <div class="ranking-item self-item">
                        <div class="ranking-position user-position">
                            {{ userRanking.position }}
                        </div>
                        <el-avatar :size="40" :src="userInfo.avatar" class="ranking-avatar" />
                        <div class="ranking-info">
                            <p class="ranking-nickname">{{ userInfo.nickname }}</p>
                            <p class="ranking-stats">
                                正确率: {{ userRanking.accuracy }}% |
                                刷题数: {{ userRanking.questions }} 题
                            </p>
                        </div>
                    </div>

                    <div class="ranking-list">
                        <div v-for="(ranker, index) in topRankers" :key="index" class="ranking-item"
                            :class="{ 'highlight-item': index < 3 }">
                            <div class="ranking-position" :class="getPositionClass(index + 1)">
                                {{ index + 1 }}
                            </div>
                            <el-avatar :size="40" :src="ranker.avatar" class="ranking-avatar" />
                            <div class="ranking-info">
                                <p class="ranking-nickname">{{ ranker.nickname }}</p>
                                <p class="ranking-stats">
                                    正确率: {{ ranker.accuracy }}% |
                                    刷题数: {{ ranker.questions }} 题
                                </p>
                            </div>
                        </div>
                    </div>

                    <div class="ranking-footer">
                        <el-button type="primary" @click="viewFullRanking">查看完整排行榜</el-button>
                    </div>
                </div>
            </div>
        </div>

        <!-- 学习建议区域 -->
        <div class="suggestions-section">
            <div class="chart-container">
                <div class="chart-header">
                    <h2><el-icon name="Lightning" /> 智能学习建议</h2>
                    <el-button type="primary" text @click="refreshSuggestions">
                        <el-icon name="Refresh" /> 刷新建议
                    </el-button>
                </div>

                <div class="suggestions-container">
                    <div v-for="(suggestion, index) in suggestions" :key="index" class="suggestion-card">
                        <div class="suggestion-header">
                            <el-tag
                                :type="suggestion.type === 'urgent' ? 'danger' : suggestion.type === 'important' ? 'warning' : 'success'">
                                {{ getSuggestionTagText(suggestion.type) }}
                            </el-tag>
                            <el-icon :name="suggestion.icon" class="suggestion-icon" />
                        </div>
                        <h3 class="suggestion-title">{{ suggestion.title }}</h3>
                        <p class="suggestion-content">{{ suggestion.content }}</p>
                        <div class="suggestion-actions">
                            <el-button v-if="suggestion.actionType === 'practice'" type="primary" size="small"
                                @click="startPractice(suggestion.subjectId)">
                                立即练习
                            </el-button>
                            <el-button v-if="suggestion.actionType === 'review'" type="warning" size="small"
                                @click="reviewMistakes(suggestion.subjectId)">
                                复习错题
                            </el-button>
                            <el-button v-if="suggestion.actionType === 'exam'" type="success" size="small"
                                @click="startMockExam">
                                模拟考试
                            </el-button>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted, computed } from 'vue'
import * as echarts from 'echarts'
import { useRouter } from 'vue-router'
import { ElMessage, ElNotification } from 'element-plus'

const router = useRouter()

// 用户信息
const userInfo = ref({
    nickname: '考研学子',
    avatar: 'https://cube.elemecdn.com/0/88/03b0d39583f48206768a7534e55bcpng.png',
    role: 'student',
    target_school: '清华大学',
    exam_year: 2026,
    exam_date: '2025-12-20',
    target_total_score: 400,
    exam_subjects: '政治、英语一、数学一、408'
})

// 考试类型计算
const examType = computed(() => {
    return userInfo.value.exam_subjects.replace(/、/g, ' + ')
})

// 最后更新时间
const lastUpdateTime = ref(new Date().toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
}))

// 日期范围筛选
const dateRange = ref([
    new Date(new Date().setDate(new Date().getDate() - 6)),
    new Date()
])

// 快捷日期选项
const shortcuts = ref([
    {
        text: '最近7天',
        value: () => {
            const end = new Date()
            const start = new Date()
            start.setDate(start.getDate() - 6)
            return [start, end]
        }
    },
    {
        text: '最近30天',
        value: () => {
            const end = new Date()
            const start = new Date()
            start.setDate(start.getDate() - 29)
            return [start, end]
        }
    },
    {
        text: '本月',
        value: () => {
            const end = new Date()
            const start = new Date()
            start.setDate(1)
            return [start, end]
        }
    }
])

// 科目筛选
const subjectFilter = ref('all')
const allSubjects = ref([
    { id: 1, name: '高等数学' },
    { id: 2, name: '线性代数' },
    { id: 3, name: '概率论' },
    { id: 4, name: '英语' },
    { id: 5, name: '政治' }
])

// 核心统计数据
const coreStats = ref({
    totalQuestions: 128,
    todayQuestions: 15,
    accuracy: 78,
    accuracyTrend: 2.5,
    totalStudyHours: 42,
    todayStudyHours: 2.5,
    mistakeCount: 26,
    newMistakes: 3
})

// 目标进度
const targetProgress = ref({
    percentage: 65,
    completed: 1280,
    total: 2000,
    estimateDate: '2025-11-30'
})

// 排行榜数据
const userRanking = ref({
    position: 42,
    accuracy: 78,
    questions: 128
})

const topRankers = ref([
    {
        nickname: '数学大神',
        avatar: 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png',
        accuracy: 95,
        questions: 3200
    },
    {
        nickname: '考研先锋',
        avatar: 'https://cube.elemecdn.com/e/fd/0fc7d20532fdaf769a25683617711png.png',
        accuracy: 92,
        questions: 2850
    },
    {
        nickname: '高分学霸',
        avatar: 'https://cube.elemecdn.com/9/c2/f0ee8a3c7c9638a54940382568c9dpng.png',
        accuracy: 90,
        questions: 2500
    },
    {
        nickname: '努力奋斗',
        avatar: 'https://cube.elemecdn.com/2/19/a649d2a1e79a99b365f71af988dpng.png',
        accuracy: 88,
        questions: 2200
    },
    {
        nickname: '每日一练',
        avatar: 'https://cube.elemecdn.com/d/5c/39332e6a2c8c6c2f9c863dpng.png',
        accuracy: 85,
        questions: 1950
    }
])

// 高频错题
const hotMistakes = ref([
    {
        id: 1001,
        rank: 1,
        content: '设函数 f(x) = x³ - 3x² + 2，求函数的极值点及极值',
        subjectName: '高数',
        errorRate: 85,
        errorCount: 17
    },
    {
        id: 1002,
        rank: 2,
        content: '求矩阵 A = |1 2 3| |0 1 2| |0 0 1| 的特征值',
        subjectName: '线代',
        errorRate: 78,
        errorCount: 15
    },
    {
        id: 1003,
        rank: 3,
        content: '设随机变量 X 服从参数为 λ 的泊松分布，求 E(X²)',
        subjectName: '概率',
        errorRate: 75,
        errorCount: 14
    },
    {
        id: 1004,
        rank: 4,
        content: '求极限 lim(x→0) (e^x - 1 - x)/x²',
        subjectName: '高数',
        errorRate: 70,
        errorCount: 13
    },
    {
        id: 1005,
        rank: 5,
        content: '设 A 为 n 阶矩阵，若 A² = A，则 A 的特征值为',
        subjectName: '线代',
        errorRate: 68,
        errorCount: 12
    }
])

// 学习趋势周期
const trendPeriod = ref('daily')

// 智能学习建议
const suggestions = ref([
    {
        id: 1,
        type: 'urgent',
        icon: 'Warning',
        title: '线性代数基础薄弱',
        content: '您在线性代数的正确率仅为65%，特别是矩阵特征值相关题目错误率高达78%。建议优先复习特征值、特征向量基础概念。',
        actionType: 'practice',
        subjectId: 2
    },
    {
        id: 2,
        type: 'important',
        icon: 'Reading',
        title: '泰勒公式专项突破',
        content: '最近3次练习中，泰勒公式相关题目全部答错。系统建议进行5道泰勒公式专项练习，巩固这一重要知识点。',
        actionType: 'practice',
        subjectId: 1
    },
    {
        id: 3,
        type: 'normal',
        icon: 'Document',
        title: '全真模拟考试',
        content: '您已覆盖80%的考点，建议进行一次2小时的全真模拟考试，检验整体掌握情况并适应考试节奏。',
        actionType: 'exam'
    }
])

// 图表引用
const radarChart = ref(null)
const heatMap = ref(null)
const trendChart = ref(null)

// 进度条颜色
const getProgressColor = (percentage) => {
    if (percentage < 30) return '#ff4d4f'
    if (percentage < 70) return '#faad14'
    return '#52c41a'
}

// 获取错误标签类型
const getErrorTagType = (rate) => {
    if (rate > 70) return 'danger'
    if (rate > 50) return 'warning'
    return 'success'
}

// 获取建议标签文本
const getSuggestionTagText = (type) => {
    switch (type) {
        case 'urgent': return '紧急'
        case 'important': return '重要'
        case 'normal': return '建议'
        default: return '提示'
    }
}

// 获取排名位置样式
const getPositionClass = (position) => {
    if (position === 1) return 'gold'
    if (position === 2) return 'silver'
    if (position === 3) return 'bronze'
    return ''
}

// 导航操作
const goToPractice = () => {
    router.push('/practice')
}
const viewMistakes = () => {
    router.push('/correction-notebook')
}
const startMockExam = () => {
    router.push('/mock-exam')
}
const goToQuestion = (id) => {
    router.push(`/question/${id}`)
}
const viewFullRanking = () => {
    router.push('/rankings')
}
const editTarget = () => {
    ElMessage.success('跳转到目标设置页面')
}
const generateWeaknessReport = () => {
    ElNotification({
        title: '报告生成中',
        message: '正在生成您的弱点分析报告，请稍候...',
        type: 'info',
        duration: 2000
    })
    setTimeout(() => {
        ElNotification({
            title: '报告生成成功',
            message: '弱点分析报告已生成，点击此处查看',
            type: 'success',
        })
    }, 1500)
}
const startPractice = (subjectId) => {
    router.push(`/practice?subject=${subjectId}`)
}
const reviewMistakes = (subjectId) => {
    router.push(`/correction-notebook?subject=${subjectId}`)
}
const refreshSuggestions = () => {
    ElMessage.success('已刷新学习建议')
}

// 图表操作
const handleRadarCommand = (command) => {
    switch (command) {
        case 'change-metrics':
            ElMessage.info('切换雷达图指标')
            break
        case 'compare-with-avg':
            ElMessage.info('与平均值对比')
            break
        case 'export-data':
            ElMessage.success('雷达图数据已导出')
            break
    }
}

const handleHeatmapCommand = (command) => {
    switch (command) {
        case 'filter-subject':
            ElMessage.info('按科目筛选错题分布')
            break
        case 'sort-by-errors':
            ElMessage.info('按错误率排序')
            hotMistakes.value.sort((a, b) => b.errorRate - a.errorRate)
            break
        case 'export-data':
            ElMessage.success('热力图数据已导出')
            break
    }
}

const handleRankingCommand = (command) => {
    switch (command) {
        case 'by-correct-rate':
            ElMessage.info('按正确率排序')
            break
        case 'by-total-questions':
            ElMessage.info('按刷题量排序')
            break
        case 'by-study-time':
            ElMessage.info('按学习时长排序')
            break
    }
}

const handleDateChange = () => {
    ElMessage.success('已更新数据范围')
    // 这里应该重新获取数据
}

const handleSubjectChange = () => {
    ElMessage.success('已切换科目筛选')
    // 这里应该重新获取数据
}

const updateTrendChart = () => {
    ElMessage.info(`已切换到${trendPeriod.value === 'daily' ? '日' : trendPeriod.value === 'weekly' ? '周' : '月'}视图`)
    // 这里应该更新趋势图
}

// 导出报告
const exportReport = () => {
    ElNotification({
        title: '导出成功',
        message: '学习报告已导出，可在下载文件夹中查看',
        type: 'success',
        duration: 3000
    })
}

// 视图操作
const viewMistakeAnalysis = () => {
    router.push('/analysis/mistakes')
}
const viewStudyTime = () => {
    router.push('/analysis/study-time')
}

// 错题分析
const viewMistakesAnalysis = () => {
    router.push('/analysis/mistakes')
}

// 初始化图表
const initCharts = () => {
    // 雷达图
    const radarChartInstance = echarts.init(radarChart.value)
    const radarOption = {
        tooltip: {
            trigger: 'axis'
        },
        radar: {
            radius: '65%',
            indicator: [
                { name: '高等数学', max: 100 },
                { name: '线性代数', max: 100 },
                { name: '概率论', max: 100 },
                { name: '英语', max: 100 },
                { name: '政治', max: 100 }
            ],
            axisName: {
                color: '#666',
                fontSize: 14
            },
            splitLine: {
                lineStyle: {
                    color: 'rgba(130, 179, 255, 0.3)'
                }
            },
            axisLine: {
                lineStyle: {
                    color: 'rgba(130, 179, 255, 0.8)'
                }
            },
            splitArea: {
                areaStyle: {
                    color: ['rgba(197, 225, 255, 0.2)', 'rgba(158, 195, 255, 0.1)'],
                    shadowColor: 'rgba(0, 0, 0, 0.1)',
                    shadowBlur: 5
                }
            }
        },
        series: [{
            name: '能力分布',
            type: 'radar',
            data: [{
                value: [92, 65, 78, 85, 70],
                name: '你的能力',
                symbol: 'circle',
                symbolSize: 8,
                lineStyle: {
                    width: 3,
                    color: '#409EFF'
                },
                areaStyle: {
                    color: 'rgba(64, 158, 255, 0.2)'
                },
                itemStyle: {
                    color: '#409EFF',
                    borderColor: '#fff',
                    borderWidth: 2
                }
            }, {
                value: [80, 75, 80, 78, 75],
                name: '平均能力',
                symbol: 'rect',
                symbolSize: 6,
                lineStyle: {
                    width: 2,
                    color: '#909399',
                    type: 'dashed'
                },
                areaStyle: {
                    color: 'rgba(144, 147, 153, 0.1)'
                },
                itemStyle: {
                    color: '#909399',
                    borderColor: '#fff',
                    borderWidth: 1
                }
            }]
        }]
    }
    radarChartInstance.setOption(radarOption)

    // 热力图
    const heatMapInstance = echarts.init(heatMap.value)
    const xAxisData = ['极限', '导数', '积分', '级数', '微分方程', '矩阵', '特征值', '概率分布', '统计推断']
    const yAxisData = ['基础', '提高', '强化', '冲刺']

    const heatMapData = []
    for (let i = 0; i < yAxisData.length; i++) {
        for (let j = 0; j < xAxisData.length; j++) {
            let val
            // 模拟一些高错误率的数据
            if ((j === 5 || j === 6) && i >= 1) { // 矩阵、特征值在提高+级别
                val = 70 + Math.floor(Math.random() * 20)
            } else if (j === 2 && i === 2) { // 积分在强化级别
                val = 65 + Math.floor(Math.random() * 15)
            } else {
                val = 20 + Math.floor(Math.random() * 50)
            }
            heatMapData.push([j, i, val])
        }
    }

    const heatMapOption = {
        tooltip: {
            position: 'top',
            formatter: function (params) {
                return `${xAxisData[params.value[0]]} - ${yAxisData[params.value[1]]}<br/>错误率: ${params.value[2]}%`
            }
        },
        grid: {
            height: '70%',
            y: '15%'
        },
        xAxis: {
            type: 'category',
            data: xAxisData,
            axisLabel: {
                rotate: 45,
                fontSize: 12
            },
            splitArea: {
                show: true
            }
        },
        yAxis: {
            type: 'category',
            data: yAxisData,
            axisLabel: {
                fontSize: 12
            },
            splitArea: {
                show: true
            }
        },
        visualMap: {
            min: 0,
            max: 100,
            calculable: true,
            orient: 'horizontal',
            left: 'center',
            bottom: '0%',
            text: ['错误率低', '错误率高'],
            inRange: {
                color: ['#e6f7ff', '#bae7ff', '#69c0ff', '#3385ff', '#0b3488']
            },
            textStyle: {
                color: '#666'
            },
            controller: {
                inRange: {
                    color: '#409EFF'
                }
            }
        },
        series: [{
            name: '错题分布',
            type: 'heatmap',
            data: heatMapData,
            label: {
                show: true,
                formatter: function (params) {
                    return params.value[2] > 50 ? params.value[2] + '%' : ''
                },
                color: '#333',
                fontSize: 10
            },
            emphasis: {
                itemStyle: {
                    shadowBlur: 10,
                    shadowColor: 'rgba(0, 0, 0, 0.2)'
                }
            }
        }]
    }
    heatMapInstance.setOption(heatMapOption)

    // 学习趋势图
    const trendChartInstance = echarts.init(trendChart.value)

    // 生成最近7天的日期
    const dates = []
    const questionsData = []
    const accuracyData = []
    const studyTimeData = []

    for (let i = 6; i >= 0; i--) {
        const date = new Date()
        date.setDate(date.getDate() - i)
        dates.push(date.toLocaleDateString('zh-CN', { month: 'numeric', day: 'numeric' }))

        // 模拟数据，最近表现更好
        const baseQuestions = 10 + Math.floor(Math.random() * 5)
        const baseAccuracy = 70 + Math.floor(Math.random() * 10)
        const baseTime = 1.5 + Math.random() * 1.5

        questionsData.push(baseQuestions + (i > 3 ? Math.floor(Math.random() * 5) : Math.floor(Math.random() * 8)))
        accuracyData.push(baseAccuracy + (i > 3 ? Math.floor(Math.random() * 3) : Math.floor(Math.random() * 7)))
        studyTimeData.push(parseFloat((baseTime + (i > 3 ? Math.random() * 0.5 : Math.random() * 1.5)).toFixed(1)))
    }

    const trendOption = {
        tooltip: {
            trigger: 'axis',
            axisPointer: {
                type: 'cross'
            }
        },
        legend: {
            data: ['刷题数量', '正确率', '学习时长(h)'],
            bottom: 0
        },
        grid: {
            top: 40,
            left: 50,
            right: 50,
            bottom: 60
        },
        xAxis: {
            type: 'category',
            data: dates,
            axisLabel: {
                fontSize: 12
            }
        },
        yAxis: [{
            type: 'value',
            name: '数量',
            min: 0,
            axisLabel: {
                fontSize: 12,
                formatter: '{value}'
            },
            splitLine: {
                lineStyle: {
                    type: 'dashed',
                    color: 'rgba(0, 0, 0, 0.05)'
                }
            }
        }, {
            type: 'value',
            name: '正确率(%)',
            min: 0,
            max: 100,
            axisLabel: {
                fontSize: 12,
                formatter: '{value}%'
            }
        }, {
            type: 'value',
            name: '时长(h)',
            min: 0,
            axisLabel: {
                fontSize: 12,
                formatter: '{value}h'
            }
        }],
        series: [
            {
                name: '刷题数量',
                type: 'bar',
                data: questionsData,
                itemStyle: {
                    color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
                        { offset: 0, color: '#66b1ff' },
                        { offset: 1, color: '#409eff' }
                    ])
                },
                emphasis: {
                    itemStyle: {
                        color: '#409eff'
                    }
                }
            },
            {
                name: '正确率',
                type: 'line',
                yAxisIndex: 1,
                data: accuracyData,
                symbol: 'circle',
                symbolSize: 8,
                lineStyle: {
                    width: 3,
                    color: '#67c23a'
                },
                itemStyle: {
                    color: '#67c23a',
                    borderColor: '#fff',
                    borderWidth: 2
                },
                areaStyle: {
                    color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
                        { offset: 0, color: 'rgba(103, 194, 58, 0.5)' },
                        { offset: 1, color: 'rgba(103, 194, 58, 0.1)' }
                    ])
                }
            },
            {
                name: '学习时长(h)',
                type: 'line',
                yAxisIndex: 2,
                data: studyTimeData,
                symbol: 'rect',
                symbolSize: 6,
                lineStyle: {
                    width: 2,
                    type: 'dashed',
                    color: '#e6a23c'
                },
                itemStyle: {
                    color: '#e6a23c',
                    borderColor: '#fff',
                    borderWidth: 1
                }
            }
        ]
    }
    trendChartInstance.setOption(trendOption)

    // 响应窗口大小变化
    const resizeCharts = () => {
        radarChartInstance.resize()
        heatMapInstance.resize()
        trendChartInstance.resize()
    }

    window.addEventListener('resize', resizeCharts)

    return () => {
        window.removeEventListener('resize', resizeCharts)
        radarChartInstance.dispose()
        heatMapInstance.dispose()
        trendChartInstance.dispose()
    }
}

// 组件挂载
onMounted(async () => {
    // 初始化图表
    const cleanup = initCharts()

    // 模拟数据加载
    await new Promise(resolve => setTimeout(resolve, 500))

    // 在组件卸载时清理
    onUnmounted(cleanup)
})
</script>

<style scoped>
/* 整体布局 */
.dashboard-container {
    padding: 20px;
    background-color: #f8fafc;
    min-height: 100vh;
}

/* 顶部筛选区域 */
.dashboard-header {
    display: flex;
    justify-content: space-between;
    align-items: flex-start;
    margin-bottom: 32px;
    flex-wrap: wrap;
    gap: 20px;
    background: linear-gradient(135deg, #ffffff 0%, #f8fbff 100%);
    border-radius: 16px;
    padding: 24px;
    box-shadow: 0 4px 20px rgba(0, 0, 0, 0.06);
    border: 1px solid #ebeef5;
    position: relative;
    overflow: hidden;
}

.dashboard-header::before {
    content: '';
    position: absolute;
    top: -50%;
    left: -50%;
    width: 200%;
    height: 200%;
    background: radial-gradient(circle, rgba(64, 158, 255, 0.1) 0%, transparent 70%);
    z-index: 0;
}

.welcome-section {
    display: flex;
    flex-direction: column;
    position: relative;
    z-index: 1;
    max-width: 650px;
    gap: 8px;
}

.welcome-section h1 {
    font-size: 45px;
    font-weight: 800;
    margin: 0 0 10px;
    background: linear-gradient(to right, #3a6197, #4a92da);
    background-clip: text;
    -webkit-background-clip: text;
    -webkit-text-fill-color: transparent;
    position: relative;
    display: inline-block;
}

.subtitle {
    font-size: 18px;
    color: #4a5568;
    font-weight: 500;
}

.last-update {
    color: #718096;
    font-size: 14px;
    margin: 8px 0 0;
    display: flex;
    align-items: center;
    gap: 6px;
}

.last-update::before {
    content: '';
    display: inline-block;
    width: 8px;
    height: 8px;
    background: #4ade80;
    border-radius: 50%;
    box-shadow: 0 0 6px #4ade80;
}

.header-actions {
    display: flex;
    flex-direction: column;
    gap: 16px;
    flex-wrap: wrap;
    align-items: center;
    position: relative;
    z-index: 1;
    min-width: 420px;
    justify-content: flex-end;
}

:deep(.el-date-editor) {
    width: 320px;
    height: 44px;
}

:deep(.el-input__wrapper) {
    border-radius: 12px !important;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05) !important;
    border: 1px solid #dcdfe6 !important;
    background-color: white !important;
    transition: all 0.3s ease !important;
}

:deep(.el-input__wrapper:hover) {
    box-shadow: 0 4px 12px rgba(64, 158, 255, 0.15) !important;
    border-color: #409eff !important;
}

:deep(.el-select) {
    width: 160px;
    height: 44px;
}

:deep(.el-button) {
    height: 44px;
    border-radius: 12px;
    font-weight: 500;
    padding: 0 24px;
    box-shadow: 0 3px 10px rgba(64, 158, 255, 0.25);
    transition: all 0.3s ease;
}

:deep(.el-button:hover) {
    transform: translateY(-2px);
    box-shadow: 0 5px 15px rgba(64, 158, 255, 0.35);
}

:deep(.el-button:active) {
    transform: translateY(0);
}

:deep(.el-icon) {
    font-size: 18px;
    margin-right: 6px;
    vertical-align: middle;
}

/* 响应式调整 */
@media (max-width: 1100px) {
  .dashboard-header {
    flex-direction: column;
    align-items: stretch;
  }
  
  .header-actions {
    width: 100%;
    justify-content: space-between;
    min-width: auto;
  }
  
  :deep(.el-date-editor) {
    width: 200px;
  }
  
  :deep(.el-select) {
    width: 140px;
  }
}

@media (max-width: 768px) {
  .dashboard-header {
    padding: 20px;
  }
  
  .welcome-section h1 {
    font-size: 28px;
  }
  
  .subtitle {
    font-size: 16px;
  }
  
  :deep(.el-date-editor) {
    width: 180px;
  }
  
  :deep(.el-select) {
    width: 120px;
  }
  
  :deep(.el-button) {
    height: 40px;
    font-size: 14px;
    padding: 0 16px;
  }
  
  :deep(.el-icon) {
    font-size: 16px;
  }
}

@media (max-width: 480px) {
  :deep(.el-date-editor) {
    width: 100%;
  }
  
  .header-actions {
    flex-direction: column;
    gap: 12px;
  }
  
  :deep(.el-select) {
    width: 100%;
  }
  
  :deep(.el-button) {
    width: 100%;
  }
}

/* 统计卡片 */
.stats-grid {
    display: grid;
    grid-template-columns: repeat(auto-fit, minmax(240px, 1fr));
    gap: 20px;
    margin-bottom: 25px;
}

.stat-card {
    background: white;
    border-radius: 12px;
    padding: 20px;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
    border: 1px solid #ebeef5;
    cursor: pointer;
    transition: all 0.3s ease;
}

.stat-card:hover {
    transform: translateY(-3px);
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.12);
    border-color: #dcdfe6;
}

.stat-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 15px;
}

.stat-header h3 {
    font-size: 16px;
    color: #606266;
    margin: 0;
}

.stat-icon {
    color: #409eff;
    font-size: 20px;
}

.stat-value {
    font-size: 32px;
    font-weight: 700;
    color: #303133;
    margin-bottom: 8px;
}

.stat-trend {
    font-size: 14px;
    display: flex;
    align-items: center;
    gap: 4px;
}

.stat-trend.positive {
    color: #67c23a;
}

.stat-trend.negative {
    color: #f56c6c;
}

/* 图表区域 */
.charts-row,
.analysis-row,
.progress-row {
    display: grid;
    grid-template-columns: repeat(auto-fit, minmax(550px, 1fr));
    gap: 25px;
    margin-bottom: 30px;
}

.chart-container {
    background: white;
    border-radius: 12px;
    padding: 20px;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
    border: 1px solid #ebeef5;
    transition: all 0.3s ease;
}

.chart-container:hover {
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.chart-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 20px;
}

.chart-header h2 {
    font-size: 18px;
    color: #303133;
    margin: 0;
    display: flex;
    align-items: center;
    gap: 8px;
}

.chart-header h2 .el-icon {
    color: #409eff;
    font-size: 20px;
}

.chart-area {
    width: 100%;
    height: 350px;
}

/* 错题表格 */
.hot-mistakes-container {
    height: 350px;
}

.question-content {
    color: #409eff;
    cursor: pointer;
    text-decoration: underline;
}

.question-content:hover {
    color: #66b1ff;
}

/* 目标进度 */
.target-progress {
    padding: 15px 0;
}

.target-info {
    margin-bottom: 20px;
    padding-bottom: 15px;
    border-bottom: 1px solid #ebeef5;
}

.target-school,
.target-exam,
.target-date {
    font-size: 15px;
    margin: 5px 0;
    color: #606266;
}

.target-school span,
.target-exam span,
.target-date span {
    font-weight: 600;
    color: #303133;
}

.el-progress {
    margin: 15px 0;
}

.progress-text {
    font-size: 18px;
    font-weight: 600;
    color: white;
    text-shadow: 0 1px 2px rgba(0, 0, 0, 0.3);
}

.progress-details {
    margin-top: 15px;
    font-size: 14px;
    color: #606266;
}

.completed {
    color: #67c23a;
    font-weight: 600;
}

.total {
    color: #e6a23c;
    font-weight: 600;
}

.estimate-date {
    color: #409eff;
    font-weight: 600;
}

/* 排行榜 */
.ranking-container {
    padding: 10px 0;
}

.ranking-item {
    display: flex;
    align-items: center;
    padding: 12px 0;
    border-bottom: 1px solid #f2f6fc;
}

.ranking-item:last-child {
    border-bottom: none;
}

.ranking-item.highlight-item {
    background-color: rgba(64, 158, 255, 0.05);
    border-radius: 6px;
    padding: 12px 15px;
    margin: 5px 0;
}

.self-item {
    background-color: rgba(103, 194, 58, 0.1);
    border-radius: 8px;
    padding: 15px;
    margin-bottom: 20px;
    border: 1px solid rgba(103, 194, 58, 0.2);
}

.ranking-position {
    width: 30px;
    height: 30px;
    border-radius: 50%;
    display: flex;
    align-items: center;
    justify-content: center;
    margin-right: 15px;
    font-weight: 600;
    font-size: 14px;
    flex-shrink: 0;
}

.user-position {
    background: linear-gradient(45deg, #409eff, #66b1ff);
    color: white;
}

.gold {
    background: linear-gradient(45deg, #ffd700, #ffcc00);
    color: #333;
    box-shadow: 0 0 6px rgba(255, 215, 0, 0.5);
}

.silver {
    background: linear-gradient(45deg, #c0c0c0, #a9a9a9);
    color: #333;
    box-shadow: 0 0 6px rgba(192, 192, 192, 0.5);
}

.bronze {
    background: linear-gradient(45deg, #cd7f32, #b87333);
    color: #fff;
    box-shadow: 0 0 6px rgba(205, 127, 50, 0.5);
}

.ranking-avatar {
    margin-right: 15px;
}

.ranking-info {
    flex: 1;
}

.ranking-nickname {
    font-size: 16px;
    font-weight: 600;
    color: #303133;
    margin: 0;
}

.ranking-stats {
    font-size: 13px;
    color: #909399;
    margin: 3px 0 0;
}

.ranking-footer {
    text-align: center;
    margin-top: 20px;
    padding-top: 15px;
    border-top: 1px solid #ebeef5;
}

/* 学习建议 */
.suggestions-section {
    margin-top: 20px;
}

.suggestions-container {
    display: grid;
    grid-template-columns: repeat(auto-fit, minmax(350px, 1fr));
    gap: 20px;
}

.suggestion-card {
    background: white;
    border-radius: 12px;
    padding: 20px;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
    border: 1px solid #ebeef5;
    transition: all 0.3s ease;
}

.suggestion-card:hover {
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
    transform: translateY(-2px);
}

.suggestion-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 15px;
}

.suggestion-icon {
    font-size: 20px;
    color: #409eff;
}

.suggestion-title {
    font-size: 18px;
    color: #303133;
    margin: 0 0 10px;
}

.suggestion-content {
    color: #606266;
    line-height: 1.6;
    margin-bottom: 15px;
    font-size: 14px;
}

.suggestion-actions {
    display: flex;
    justify-content: flex-end;
    gap: 10px;
}

/* 响应式调整 */
@media (max-width: 1200px) {

    .charts-row,
    .analysis-row,
    .progress-row {
        grid-template-columns: 1fr;
    }
}

@media (max-width: 980px) {
    .dashboard-header {
        flex-direction: column;
        align-items: stretch;
    }

    .header-actions {
        width: 100%;
        justify-content: space-between;
    }
}

@media (max-width: 768px) {
    .stats-grid {
        grid-template-columns: repeat(auto-fit, minmax(180px, 1fr));
    }

    .suggestions-container {
        grid-template-columns: 1fr;
    }

    .chart-area {
        height: 300px;
    }
}
</style>