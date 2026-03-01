<template>
    <div class="dashboard-container">
        <!-- 侧边栏 -->
        <div class="sidebar" :class="{ 'sidebar-exiting': isSidebarExiting }">
            <div class="logo-section">
                <div class="logo-icon">
                    <span class="logo-letter">Y</span>
                </div>
                <span class="logo-text">考研全流程学习平台</span>
            </div>

            <div class="user-profile-section">
                <el-avatar :size="70" :src="userInfo.avatar" class="user-avatar" />
                <h3>{{ userInfo.nickname }}</h3>
                <div class="quick-stats">
                    <div class="stat-item">
                        <span class="stat-number">{{ userStats.questionsDone }}</span>
                        <span class="stat-label">已刷题</span>
                    </div>
                    <div class="stat-item">
                        <span class="stat-number">{{ userStats.accuracy }}%</span>
                        <span class="stat-label">正确率</span>
                    </div>
                    <div class="stat-item">
                        <span class="stat-number">{{ userStats.studyHours }}h</span>
                        <span class="stat-label">已学习</span>
                    </div>
                </div>
            </div>

            <div class="nav-menu">
                <h4>功能导航</h4>
                <el-menu :default-active="$route.path" class="el-menu-vertical" @select="handleMenuNavigation">
                    <el-menu-item index="subject">
                        <el-icon>
                            <img :src="singlePracticeIcon" style="width: 18px; height: 18px;">
                        </el-icon>
                        <span>科目练习</span>
                    </el-menu-item>
                    <el-menu-item index="/user/correction-notebook">
                        <el-icon>
                            <img :src="correctionNotebookIcon" style="width: 18px; height: 18px;">
                        </el-icon>
                        <span>错题本</span>
                    </el-menu-item>
                    <el-menu-item index="/user/paper-list">
                        <el-icon>
                            <img :src="mockExamIcon" style="width: 18px; height: 18px;">
                        </el-icon>
                        <span>真题模考</span>
                    </el-menu-item>
                    <el-menu-item index="/user/topic-drill?rootId=1">
                        <el-icon>
                            <img :src="treeIcon" style="width: 18px; height: 18px;">
                        </el-icon>
                        <span>知识体系树</span>
                    </el-menu-item>
                    <el-menu-item index="/user/profile">
                        <el-icon>
                            <img :src="userSettingIcon" style="width: 18px; height: 18px;">
                        </el-icon>
                        <span>个人中心</span>
                    </el-menu-item>
                </el-menu>
            </div>

            <div v-if="currentQuote" class="motivation-quote">
                <el-icon name="ChatDotRound" class="quote-icon" />
                <p>"{{ currentQuote.content }}"</p>
                <span class="quote-author">— {{ currentQuote.author }}</span>
            </div>
            <div v-else class="motivation-quote">
                <p>加载中...</p>
            </div>
        </div>

        <!-- 主内容区 -->
        <div class="main-content" :class="{ 'sidebar-exiting-content': isSidebarExiting }">
            <!-- 顶部欢迎区域 -->
            <div class="dashboard-header">
                <div class="welcome-section">
                    <h1 class="welcome-title">你好，{{ userInfo.nickname }}！今天也要加油哦~</h1>
                    <p class="target-text">
                        目标：
                        <span class="target-school">{{ userInfo.targetSchool }}</span> 
                        <span class="custom-tag">{{ userInfo.examYear }}</span>
                    </p>
                    <div class="countdown-badge" v-if="daysLeft >= 0">
                        <span class="countdown-number">{{ daysLeft }}</span> 天后考研
                    </div>
                </div>

                <div class="quick-actions">
                    <el-button type="primary" size="large" @click="startPractice" class="action-button">
                        <el-icon :size="18">
                            <img :src="singlePracticeIcon" style="width: 18px; height: 18px;">
                        </el-icon>
                        <span>刷书</span>
                    </el-button>

                    <el-button size="large" @click="startTopicDrill" class="action-button">
                        <el-icon :size="18">
                            <img :src="testBaseIcon" style="width: 18px; height: 18px;">
                        </el-icon>
                        <span>刷题</span>
                    </el-button>

                    <el-button size="large" @click="startMockExam" class="action-button">
                        <el-icon :size="18">
                            <img :src="mockExamIcon" style="width: 18px; height: 18px;">
                        </el-icon>
                        <span>刷卷</span>
                    </el-button>

                    <el-button
                        size="large"
                        @click="startDailyTest"
                        class="action-button"
                        :class="{ 'is-daily-active': remainingCount > 0 }"
                    >
                        <el-icon :size="18">
                            <img :src="dailyTestIcon" style="width: 18px; height: 18px;">
                        </el-icon>
                        <span>每日测试</span>
                    </el-button>
                </div>
            </div>

            <!-- 数据统计卡片 -->
            <div class="stats-overview">
                <div class="stat-card primary-card" @click="ViewDashboard()">
                    <div class="stat-icon bg-primary">
                        <el-icon :size="24">
                            <img :src="testBaseIcon" style="width: 24px; height: 24px;">
                        </el-icon>
                    </div>
                    <div class="stat-content">
                        <span class="stat-value">{{ stats.questionsDone }}</span>
                        <span class="stat-label">累计刷题</span>
                    </div>
                </div>

                <div class="stat-card success-card" @click="ViewDashboard()">
                    <div class="stat-icon bg-success">
                        <el-icon :size="24">
                            <img :src="clockIcon" style="width: 24px; height: 24px;">
                        </el-icon>
                    </div>
                    <div class="stat-content">
                        <span class="stat-value">{{ stats.studyTime }}h</span>
                        <span class="stat-label">今日学习</span>
                    </div>
                </div>

                <div class="stat-card warning-card" @click="ViewDashboard()">
                    <div class="stat-icon bg-warning">
                        <el-icon :size="24">
                            <img :src="lineChartIcon" style="width: 24px; height: 24px;">
                        </el-icon>
                    </div>
                    <div class="stat-content">
                        <span class="stat-value">{{ stats.accuracy }}%</span>
                        <span class="stat-label">总体正确率</span>
                    </div>
                </div>

                <div class="stat-card danger-card" @click="viewMistakes()">
                    <div class="stat-icon bg-danger">
                        <el-icon :size="24">
                            <img :src="correctionNotebookIcon" style="width: 24px; height: 24px;">
                        </el-icon>
                    </div>
                    <div class="stat-content">
                        <span class="stat-value">{{ stats.mistakes }}</span>
                        <span class="stat-label">错题本数量</span>
                    </div>
                </div>
            </div>

            <!-- 数据展示区域 -->
            <div class="charts-section">
                <!-- 最近错题模块 -->
                <div class="chart-container feature-card">
                    <div class="feature-header">
                        <h3>最近错题</h3>
                        <el-button type="primary" text @click="viewMistakes()">查看全部</el-button>
                    </div>
                    <div class="mistakes-list">
                        <div class="mistake-item" v-for="(mistake, index) in recentMistakes" :key="index"
                             @click="goToQuestion(mistake)">
                            <div class="mistake-index">{{ index + 1 }}</div>
                            <div class="mistake-content">
                                <div class="mistake-subject">
                                    <span class="subject-badge" :class="'subject-' + mistake.subjectType">
                                        {{ mistake.subjectName }}
                                    </span>
                                    <span class="mistake-time">{{ mistake.timeAgo }}</span>
                                </div>
                                <div class="mistake-question" v-html="renderLatex(mistake.question)"></div>
                            </div>
                        </div>
                        <div v-if="recentMistakes.length === 0" class="empty-state">
                            <p>🎉 太棒了！最近没有错题</p>
                        </div>
                    </div>
                </div>

                <!-- 各科刷题数 -->
                <div class="chart-container feature-card">
                    <div class="feature-header">
                        <h3>各科刷题数</h3>
                    </div>
                    <div ref="radarChart" class="chart-area" style="width: 100%; height: 280px;"></div>
                    <p class="chart-tip" v-html="subjectTip"></p>
                </div>
            </div>

            <!-- 数据展示区域2 -->
            <div class="charts-section">
                <!-- 每日测试模块 -->
                <div class="chart-container feature-card">
                    <div class="feature-header">
                        <h3>每日测试正确率</h3>
                        <span class="test-period">最近7天</span>
                    </div>
                    <div ref="dailyTestChart" class="daily-test-chart" style="width: 100%; height: 280px;"></div>
                    <div class="test-summary">
                        <div class="summary-item">
                            <span class="summary-label">平均正确率</span>
                            <span class="summary-value">{{ dailyTestStats.averageAccuracy }}%</span>
                        </div>
                        <div class="summary-item">
                            <span class="summary-label">最高正确率</span>
                            <span class="summary-value">{{ dailyTestStats.maxAccuracy }}%</span>
                        </div>
                        <div class="summary-item">
                            <span class="summary-label">测试次数</span>
                            <span class="summary-value">{{ dailyTestStats.totalTests }}次</span>
                        </div>
                    </div>
                </div>

                <!-- 错题分布 -->
                <div class="chart-container feature-card">
                    <div class="feature-header">
                        <h3>错题分布</h3>
                    </div>
                    <div ref="heatMap" class="chart-area" style="width: 100%; height: 280px;"></div>
                    <p class="chart-tip" v-html="mistakeTip"></p>
                </div>
            </div>

        </div>


        <!-- 错题详情对话框 -->
        <el-dialog
            v-model="questionDetailDialogVisible"
            title="错题详情"
            width="800px"
            :close-on-click-modal="false"
            class="question-detail-dialog"
        >
            <div v-if="selectedQuestion" class="question-detail-content">
                <!-- 题目基本信息 -->
                <div class="detail-section">
                    <div class="detail-header">
                        <h4>题目信息</h4>
                        <div class="question-meta">
                            <el-tag v-if="selectedQuestion.subjectNames && selectedQuestion.subjectNames.length > 0"
                                    type="primary" size="small">
                                {{ selectedQuestion.subjectNames.join('、') }}
                            </el-tag>
                            <el-tag v-if="selectedQuestion.bookNames && selectedQuestion.bookNames.length > 0"
                                    type="info" size="small" style="margin-left: 8px;">
                                {{ selectedQuestion.bookNames.join('、') }}
                            </el-tag>
                        </div>
                    </div>
                    <div class="question-body">
                        <div class="question-text" v-html="renderLatex(selectedQuestion.content)"></div>

                        <!-- 选项（如果有） -->
                        <div v-if="selectedQuestion.options && selectedQuestion.options.length > 0" class="options-list">
                            <div v-for="(option, index) in selectedQuestion.options" :key="index" class="option-item">
                                <span class="option-label">{{ option.label || ['A', 'B', 'C', 'D', 'E', 'F'][index] }}.</span>
                                <span class="option-text" v-html="renderLatex(option.text)"></span>
                            </div>
                        </div>
                    </div>
                </div>

                <!-- 答案与解析 -->
                <div class="detail-section">
                    <div class="detail-header">
                        <h4>答案与解析</h4>
                    </div>
                    <div class="answer-section">
                        <div class="answer-row">
                            <span class="answer-label">正确答案：</span>
                            <span class="answer-value primary latex-answer" v-html="renderLatex(selectedQuestion.answer)"></span>
                        </div>
                        <div v-if="selectedQuestion.analysis" class="analysis-row">
                            <span class="analysis-label">题目解析：</span>
                            <div class="analysis-content latex-content" v-html="renderLatex(selectedQuestion.analysis)"></div>
                        </div>
                    </div>
                </div>

                <!-- 标签（如果有） -->
                <div v-if="selectedQuestion.tags && selectedQuestion.tags.length > 0" class="detail-section">
                    <div class="detail-header">
                        <h4>知识点标签</h4>
                    </div>
                    <div class="tags-list">
                        <el-tag v-for="(tag, index) in selectedQuestion.tags" :key="index" size="small" style="margin-right: 8px; margin-bottom: 8px;">
                            {{ tag }}
                        </el-tag>
                    </div>
                </div>
            </div>

            <template #footer>
                <el-button @click="questionDetailDialogVisible = false">关闭</el-button>
            </template>
        </el-dialog>

        <!-- 悬浮胶囊按钮组 -->
        <div class="floating-capsules">
            <!-- 客服 -->
            <div class="floating-capsule service-capsule" @click="handleService">
                <div class="capsule-content">
                    <img :src="serviceIcon" class="capsule-icon" style="width: 24px; height: 24px;">
                    <span class="capsule-text">客服</span>
                </div>
            </div>
        </div>
    </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted, computed, onBeforeUnmount} from 'vue'
import * as echarts from 'echarts'
import { useRouter, useRoute } from 'vue-router'
import { getHomePageDataApi, getUserStudyStatsApi, getErrorBookApi, getDailyTestStatusApi, getDailyTestAccuracyStatsApi, getMistakeDistributionStatsApi, getSubjectQuestionCountStatsApi } from '@/api/user'
import { ElMessage } from 'element-plus'
import { renderLatex } from '@/utils/latex'

import singlePracticeIcon from '@/assets/icons/single-practice.svg?url'
import correctionNotebookIcon from '@/assets/icons/correction-notebook.svg?url'
import mockExamIcon from '@/assets/icons/mock-exam.svg?url'
import treeIcon from '@/assets/icons/tree.svg?url'
import userSettingIcon from '@/assets/icons/user-setting.svg?url'
import testBaseIcon from '@/assets/icons/do-exercise.svg?url'
import lineChartIcon from '@/assets/icons/line-chart.svg?url'
import clockIcon from '@/assets/icons/clock.svg?url'
import chartNoAxisIcon from '@/assets/icons/chart-no-axis.svg?url'
import serviceIcon from '@/assets/icons/service.svg?url'
import dailyTestIcon from '@/assets/icons/daily-test.svg?url'

const router = useRouter()
const route = useRoute()

// 侧边栏动画状态
const isSidebarExiting = ref(false)

const userInfo = ref({})

// 计算考研倒计时
const daysLeft = computed(() => {
    const today = new Date()
    const examDate = new Date(userInfo.value.exam_date)
    const diffTime = examDate - today
    const diffDays = Math.ceil(diffTime / (1000 * 60 * 60 * 24))
    return diffDays
})

// 用户统计数据
const userStats = ref({ questionsDone: 0, accuracy: 0, studyHours: 0 })

// 首页统计数据
const stats = ref({ questionsDone: 0, studyTime: 0, accuracy: 0, mistakes: 0 })
// 考试科目
const examSubjects = ref([])
// 每日测试剩余次数
const remainingCount = ref(0)

// 所有励志语录
const quotes = [
    { content: "天行健，君子以自强不息。", author: "《周易》" },
    { content: "路漫漫其修远兮，吾将上下而求索。", author: "屈原" },
    { content: "不积跬步，无以至千里；不积小流，无以成江海。", author: "荀子" },
    { content: "天生我材必有用，千金散尽还复来。", author: "李白" },
    { content: "长风破浪会有时，直挂云帆济沧海。", author: "李白" }
]

// 每日语录（从后端获取）
const dailyQuote = ref(null)

// 确保初始值有效
const currentQuote = ref(quotes.length > 0 ? quotes[0] : null)
let currentIndex = 0
let intervalId = null
// 自动轮播函数
const nextQuote = () => {
    if (quotes.length === 0) return
    currentIndex = (currentIndex + 1) % quotes.length
    currentQuote.value = quotes[currentIndex]
}

// 组件挂载后启动轮播
onMounted(() => {
    intervalId = setInterval(nextQuote, 100000) // 每100秒切换一次
})

// 组件卸载前清除定时器
onBeforeUnmount(() => {
    if (intervalId) {
        clearInterval(intervalId)
    }
})



// 图表引用
const radarChart = ref(null)
const heatMap = ref(null)
const dailyTestChart = ref(null)

// 动态提示文字
const subjectTip = ref('')
const mistakeTip = ref('')

// 最近错题数据
const recentMistakes = ref([])

// 错题详情对话框
const questionDetailDialogVisible = ref(false)
const selectedQuestion = ref(null)

// 每日测试统计数据（模拟数据）
const dailyTestStats = ref({
    averageAccuracy: 72.4,
    maxAccuracy: 85,
    totalTests: 23
})


// 快捷操作 - 包装导航函数,添加退出动画
const navigateWithAnimation = (path) => {
    // 如果不是 Home 页面,直接跳转
    if (route.path !== '/user/home') {
        router.push(path)
        return
    }

    // 触发侧边栏退出动画
    isSidebarExiting.value = true

    // 等待动画完成后再跳转 (0.3s，与退出动画时长一致)
    setTimeout(() => {
        router.push(path)
    }, 300)
}

const startPractice = () => {
    navigateWithAnimation('/user/subject')
}

const startTopicDrill = () => {
    navigateWithAnimation('/user/topic-drill')
}

const viewMistakes = () => {
    navigateWithAnimation('/user/correction-notebook')
}

const startMockExam = () => {
    navigateWithAnimation('/user/paper-list')
}

const startDailyTest = () => {
    if (remainingCount.value === 0) {
        ElMessage.info('今日测试已完成，继续保持')
        return
    }
    navigateWithAnimation('/user/daily-test')
}

const fetchDailyTestStatus = async () => {
    try {
        const userId = JSON.parse(localStorage.getItem('user') || '{}').id;
        if (!userId) {
            console.log('未登录，跳过获取每日测试状态')
            remainingCount.value = 0
            return
        }
        
        const res = await getDailyTestStatusApi(userId)
        if (res.code === 200 && res.data) {
            remainingCount.value = res.data.remainingCount || 0
        } else {
            remainingCount.value = 0
        }
    } catch (error) {
        console.error('获取每日测试状态失败:', error)
        remainingCount.value = 0
    }
}



// 获取首页数据
const fetchHomePageData = async () => {
    try {
        const userId = JSON.parse(localStorage.getItem('user') || '{}').id;
        console.log('userId:', userId)
        if (!userId) {
            ElMessage.error('未登录，请先登录')
            router.push('/login')
            return
        }

        const res = await getHomePageDataApi(userId)
        if (res.code === 200) {
            const data = res.data
            console.log('首页数据:', data)

            if (data.userInfo) {
                userInfo.value = data.userInfo
            }

            if (data.studyStats) {
                stats.value.questionsDone = data.studyStats.questionsDone || 0
                stats.value.studyTime = data.studyStats.todayStudyTime || 0
                stats.value.accuracy = data.studyStats.accuracy || 0
                stats.value.mistakes = data.studyStats.mistakesCount || 0
                userStats.value.questionsDone = data.studyStats.questionsDone || 0
                userStats.value.accuracy = data.studyStats.accuracy || 0
                userStats.value.studyHours = data.studyStats.totalStudyHours || 0
            }

            if (data.subjects && data.subjects.length > 0) {
                examSubjects.value = data.subjects
                console.log('examSubjects:', examSubjects)
            }

            if (data.dailyQuote) {
                dailyQuote.value = data.dailyQuote
            }
        }
    } catch (error) {
        console.error('获取首页数据失败:', error)
        ElMessage.error('获取首页数据失败')
    }
}

// 获取用户学习统计数据
const fetchUserStudyStats = async () => {
    try {
        const userId = JSON.parse(localStorage.getItem('user') || '{}').id;
        console.log('userId:', userId)
        if (!userId) {
            ElMessage.error('未登录，请先登录')
            router.push('/login')
            return
        }

        const res = await getUserStudyStatsApi(userId)
        if (res.code === 200) {
            const data = res.data
            console.log('学习统计数据:', data)

            if (data) {
                userStats.value.questionsDone = data.questionsDone || 0
                userStats.value.accuracy = data.accuracy || 0
                userStats.value.studyHours = data.studyHours || 0
            }
        }
    } catch (error) {
        console.error('获取学习统计数据失败:', error)
        ElMessage.error('获取学习统计数据失败')
    }
}



// 初始化图表
const initCharts = async () => {
    // 各科目刷题数量柱状图
    const subjectQuestionCountChartInstance = echarts.init(radarChart.value)
    let subjectNames = []
    let questionCountData = []

    try {
        const userId = JSON.parse(localStorage.getItem('user') || '{}').id
        if (userId) {
            const res = await getSubjectQuestionCountStatsApi(userId)
            if (res.code === 200 && res.data) {
                subjectNames = res.data.map(item => item.subjectName)
                questionCountData = res.data.map(item => item.questionCount)
            }
        }
    } catch (error) {
        console.error('获取各科目刷题数量失败:', error)
        // 使用默认数据
        subjectNames = ['政治', '英语', '数学', '专业课']
        questionCountData = [150, 200, 300, 100]
    }

    // 计算各科刷题数的动态提示
    if (subjectNames.length > 0 && questionCountData.length > 0) {
        const maxCount = Math.max(...questionCountData)
        const maxSubject = subjectNames[questionCountData.indexOf(maxCount)]
        const minCount = Math.min(...questionCountData)
        const minSubject = subjectNames[questionCountData.indexOf(minCount)]

        if (maxSubject === minSubject) {
            subjectTip.value = `💡 各科刷题数量比较均衡，继续保持！`
        } else {
            subjectTip.value = `💡 你的<strong>${maxSubject}</strong>刷题数量最多，<strong>${minSubject}</strong>需要加强练习`
        }
    }

    const barChartOption = {
        backgroundColor: 'transparent',
        tooltip: {
            trigger: 'axis',
            backgroundColor: 'rgba(255, 255, 255, 0.95)',
            borderColor: '#e5e7eb',
            textStyle: { color: '#1f2937' },
            formatter: function (params) {
                return `${params[0].name}<br/>刷题数量：<strong>${params[0].value}题</strong>`
            }
        },
        grid: {
            left: '8%',
            right: '5%',
            top: '15%',
            bottom: '20%',
            containLabel: true
        },
        xAxis: {
            type: 'category',
            data: subjectNames,
            axisLine: {
                lineStyle: { color: '#e5e7eb' }
            },
            axisLabel: {
                color: '#6b7280',
                fontSize: 12,
                rotate: 15
            }
        },
        yAxis: {
            type: 'value',
            min: 0,
            axisLine: {
                lineStyle: { color: '#e5e7eb' }
            },
            axisLabel: {
                color: '#6b7280',
                fontSize: 12
            },
            splitLine: {
                lineStyle: {
                    color: '#f3f4f6',
                    type: 'dashed'
                }
            }
        },
        series: [{
            name: '刷题数量',
            type: 'bar',
            data: questionCountData,
            itemStyle: {
                color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
                    { offset: 0, color: '#10b981' },
                    { offset: 1, color: '#34d399' }
                ]),
                borderRadius: [8, 8, 0, 0]
            },
            emphasis: {
                itemStyle: {
                    color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
                        { offset: 0, color: '#059669' },
                        { offset: 1, color: '#10b981' }
                    ])
                }
            },
            barWidth: '60%'
        }]
    }
    subjectQuestionCountChartInstance.setOption(barChartOption)

    // 错题分布柱状图
    const mistakeDistributionChartInstance = echarts.init(heatMap.value)
    let distributionData = []
    let mistakeSubjectNames = []

    try {
        const userId = JSON.parse(localStorage.getItem('user') || '{}').id
        if (userId) {
            const res = await getMistakeDistributionStatsApi(userId)
            if (res.code === 200 && res.data) {
                distributionData = res.data.map(item => item.mistakeCount)
                mistakeSubjectNames = res.data.map(item => item.subjectName)
            }
        }
    } catch (error) {
        console.error('获取错题分布数据失败:', error)
        //  fallback 数据
        mistakeSubjectNames = ['高等数学', '线性代数', '概率论', '英语', '政治']
        distributionData = [12, 8, 6, 15, 5]
    }

    // 计算错题分布的动态提示
    if (mistakeSubjectNames.length > 0 && distributionData.length > 0) {
        const maxMistakes = Math.max(...distributionData)
        const maxSubject = mistakeSubjectNames[distributionData.indexOf(maxMistakes)]

        // 计算错误率（这里简单计算为错题数占总错题数的百分比）
        const totalMistakes = distributionData.reduce((sum, count) => sum + count, 0)
        const errorRate = totalMistakes > 0 ? Math.round((maxMistakes / totalMistakes) * 100) : 0

        mistakeTip.value = `🔥 <strong>${maxSubject}</strong>相关题目错误率较高，占总错题数的${errorRate}%，建议优先复习`
    }

    const mistakeBarChartOption = {
        backgroundColor: 'transparent',
        tooltip: {
            trigger: 'axis',
            backgroundColor: 'rgba(255, 255, 255, 0.95)',
            borderColor: '#e5e7eb',
            textStyle: { color: '#1f2937' },
            formatter: function (params) {
                return `${params[0].name}<br/>错题数：<strong>${params[0].value}题</strong>`
            }
        },
        grid: {
            left: '8%',
            right: '5%',
            top: '15%',
            bottom: '20%',
            containLabel: true
        },
        xAxis: {
            type: 'category',
            data: mistakeSubjectNames,
            axisLine: {
                lineStyle: { color: '#e5e7eb' }
            },
            axisLabel: {
                color: '#6b7280',
                fontSize: 12,
                rotate: 15
            }
        },
        yAxis: {
            type: 'value',
            min: 0,
            axisLine: {
                lineStyle: { color: '#e5e7eb' }
            },
            axisLabel: {
                color: '#6b7280',
                fontSize: 12
            },
            splitLine: {
                lineStyle: {
                    color: '#f3f4f6',
                    type: 'dashed'
                }
            }
        },
        series: [{
            name: '错题数',
            type: 'bar',
            data: distributionData,
            itemStyle: {
                color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
                    { offset: 0, color: '#3b82f6' },
                    { offset: 1, color: '#60a5fa' }
                ]),
                borderRadius: [8, 8, 0, 0]
            },
            emphasis: {
                itemStyle: {
                    color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
                        { offset: 0, color: '#2563eb' },
                        { offset: 1, color: '#3b82f6' }
                    ])
                }
            },
            barWidth: '60%'
        }]
    }
    mistakeDistributionChartInstance.setOption(mistakeBarChartOption)

    // 响应窗口大小变化
    const resizeCharts = () => {
        subjectQuestionCountChartInstance.resize()
        mistakeDistributionChartInstance.resize()
    }

    window.addEventListener('resize', resizeCharts)

    return () => {
        window.removeEventListener('resize', resizeCharts)
        subjectQuestionCountChartInstance.dispose()
        mistakeDistributionChartInstance.dispose()
    }
}

// 初始化每日测试折线图
const initDailyTestChart = async () => {
    if (!dailyTestChart.value) return

    const userId = JSON.parse(localStorage.getItem('user') || '{}').id;
    if (!userId) return

    const dailyTestChartInstance = echarts.init(dailyTestChart.value)
    let last7Days = []
    let accuracyData = []

    try {
        const res = await getDailyTestAccuracyStatsApi(userId)
        console.log('每日测试接口原始数据:', res) // 输出原始数据到控制台
        if (res.code === 200 && res.data) {
            console.log('接口返回的有效数据:', res.data) // 输出有效数据到控制台
            last7Days = res.data.map(item => item.date)
            accuracyData = res.data.map(item => item.accuracy)

            // 计算每日测试统计数据
            const validAccuracyData = accuracyData.filter(accuracy => accuracy > 0)
            if (validAccuracyData.length > 0) {
                const sum = validAccuracyData.reduce((a, b) => a + b, 0)
                const average = sum / validAccuracyData.length
                const max = Math.max(...validAccuracyData)

                dailyTestStats.value.averageAccuracy = parseFloat(average.toFixed(1))
                dailyTestStats.value.maxAccuracy = max
                dailyTestStats.value.totalTests = validAccuracyData.length
            }
        }
    } catch (error) {
        console.error('获取正确率统计失败:', error)
    }

    // 如果没拿到数据，显示空的（或者保留原来的逻辑，但我这里选择直接用真实数据）
    if (last7Days.length === 0) {
        for (let i = 6; i >= 0; i--) {
            const date = new Date()
            date.setDate(date.getDate() - i)
            last7Days.push(`${date.getMonth() + 1}/${date.getDate()}`)
            accuracyData.push(0)
        }
    } else {
        // 打印最终使用的数据
        console.log('准备绘制图表的日期数据:', last7Days)
        console.log('准备绘制图表的正确率数据:', accuracyData)
    }

    const option = {
        backgroundColor: 'transparent',
        tooltip: {
            trigger: 'axis',
            backgroundColor: 'rgba(255, 255, 255, 0.95)',
            borderColor: '#e5e7eb',
            textStyle: { color: '#1f2937' },
            formatter: function (params) {
                return `${params[0].name}<br/>正确率：<strong>${params[0].value}%</strong>`
            }
        },
        grid: {
            left: '8%',
            right: '5%',
            top: '15%',
            bottom: '12%',
            containLabel: true
        },
        xAxis: {
            type: 'category',
            data: last7Days,
            boundaryGap: false,
            axisLine: {
                lineStyle: { color: '#e5e7eb' }
            },
            axisLabel: {
                color: '#6b7280',
                fontSize: 12
            }
        },
        yAxis: {
            type: 'value',
            min: 0,
            max: 100,
            axisLine: {
                lineStyle: { color: '#e5e7eb' }
            },
            axisLabel: {
                color: '#6b7280',
                fontSize: 12,
                formatter: '{value}%'
            },
            splitLine: {
                lineStyle: {
                    color: '#f3f4f6',
                    type: 'dashed'
                }
            }
        },
        series: [{
            name: '正确率',
            type: 'line',
            smooth: true,
            data: accuracyData,
            symbol: 'circle',
            symbolSize: 8,
            lineStyle: {
                color: '#3b82f6',
                width: 3
            },
            itemStyle: {
                color: '#3b82f6',
                borderColor: '#fff',
                borderWidth: 2
            },
            areaStyle: {
                color: {
                    type: 'linear',
                    x: 0,
                    y: 0,
                    x2: 0,
                    y2: 1,
                    colorStops: [{
                        offset: 0,
                        color: 'rgba(59, 130, 246, 0.3)'
                    }, {
                        offset: 1,
                        color: 'rgba(59, 130, 246, 0.05)'
                    }]
                }
            }
        }]
    }

    // 使用 notMerge: false 强制重绘图表，确保数据正确更新
    dailyTestChartInstance.setOption(option, {
        notMerge: false,
        lazyUpdate: false
    })
    console.log('图表已更新')

    // 响应窗口大小变化
    const handleResize = () => {
        dailyTestChartInstance.resize()
    }
    window.addEventListener('resize', handleResize)

    return () => {
        window.removeEventListener('resize', handleResize)
        dailyTestChartInstance.dispose()
    }
}

// 获取最近错题数据
const fetchRecentMistakes = async () => {
    try {
        const userId = JSON.parse(localStorage.getItem('user') || '{}').id
        if (!userId) {
            console.log('未登录，跳过获取错题数据')
            return
        }

        const res = await getErrorBookApi(userId)
        if (res.code === 200 && res.data) {
            // 后端已经按update_time降序排列并只返回5条数据
            const mistakes = res.data.map((item) => {
                // 计算时间差
                const mistakeTime = item.mistakeTime ? new Date(item.mistakeTime) : new Date()
                const now = new Date()
                const diffMs = now - mistakeTime
                const diffHours = Math.floor(diffMs / (1000 * 60 * 60))
                const diffDays = Math.floor(diffHours / 24)

                let timeAgo = ''
                if (diffHours < 1) {
                    timeAgo = '刚刚'
                } else if (diffHours < 24) {
                    timeAgo = `${diffHours}小时前`
                } else if (diffDays < 7) {
                    timeAgo = `${diffDays}天前`
                } else {
                    timeAgo = mistakeTime.toLocaleDateString('zh-CN')
                }

                // 获取科目名称
                const subjectName = item.subjectNames && item.subjectNames.length > 0
                    ? item.subjectNames[0]
                    : '未分类'

                // 获取科目类型（用于样式）
                let subjectType = 'math'
                if (subjectName.includes('代数') || subjectName.includes('线代')) {
                    subjectType = 'algebra'
                } else if (subjectName.includes('概率') || subjectName.includes('统计')) {
                    subjectType = 'probability'
                }

                // 获取题目内容
                const questionText = item.content || '题目内容加载失败'

                // 限制题目文本长度
                const truncatedQuestion = questionText.length > 100
                    ? questionText.substring(0, 100) + '...'
                    : questionText

                return {
                    id: item.id,
                    question: truncatedQuestion,
                    subjectName: subjectName,
                    subjectType: subjectType,
                    timeAgo: timeAgo,
                    fullData: item // 保存完整数据以便点击时查看详情
                }
            })

            recentMistakes.value = mistakes
            console.log('最近错题数据:', recentMistakes.value)
        }
    } catch (error) {
        console.error('获取最近错题失败:', error)
    }
}

// 跳转到题目详情
const goToQuestion = (mistake) => {
    // 显示错题详情对话框
    if (mistake.fullData) {
        selectedQuestion.value = mistake.fullData
        questionDetailDialogVisible.value = true
    } else {
        ElMessage.warning('无法查看题目详情')
    }
}

// 监听菜单点击事件(针对菜单项的导航)
const handleMenuNavigation = (path) => {
    navigateWithAnimation(path)
}

let cleanupFunctions = []

onMounted(async () => {
    fetchHomePageData()
    fetchUserStudyStats()
    fetchRecentMistakes()
    fetchDailyTestStatus()


    // 初始化通用图表
    const cleanup = await initCharts()
    if (typeof cleanup === 'function') cleanupFunctions.push(cleanup)

    // 初始化每日测试折线图
    initDailyTestChart().then(cleanup => {
        if (typeof cleanup === 'function') cleanupFunctions.push(cleanup)
    })
})

onUnmounted(() => {
    cleanupFunctions.forEach(fn => fn())
})

</script>

<style scoped>
.dashboard-container {
    display: flex;
    min-height: 100vh;
    background: linear-gradient(135deg, #cfecff 0%, #00aeff 100%);
    color: #1f2937;
    font-family: 'Segoe UI', 'PingFang SC', 'Microsoft YaHei', sans-serif;
    padding: 20px;
}

/* 侧边栏样式 */
.sidebar {
    width: 250px;
    background: rgba(255, 255, 255, 0.8);
    backdrop-filter: blur(10px);
    border-right: 1px solid rgba(229, 231, 235, 0.5);
    display: flex;
    flex-direction: column;
    height: 100vh;
    position: fixed;
    left: 0;
    top: 0;
    z-index: 100;
    box-shadow: 2px 0 15px rgba(0, 0, 0, 0.05);
    transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

.sidebar-exiting {
    opacity: 0;
    transform: scale(0.95) translateX(-10px);
    filter: blur(2px);
    transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

.logo-section {
    display: flex;
    align-items: center;
    padding: 20px;
    border-bottom: 1px solid rgba(229, 231, 235, 0.5);
    margin-bottom: 20px;
    height: 64px;
    box-sizing: border-box;
}

.logo-icon {
    width: 36px;
    height: 36px;
    background: linear-gradient(45deg, #3b82f6, #60a5fa);
    border-radius: 8px;
    display: flex;
    align-items: center;
    justify-content: center;
    margin-right: 12px;
    animation: logoBounce 2s ease-in-out infinite;
}

.logo-letter {
    font-size: 1.25rem;
    font-weight: 600;
    color: white;
    line-height: 1;
}

@keyframes logoBounce {
    0%, 100% {
        transform: scale(1);
        box-shadow: 0 0 10px rgba(59, 130, 246, 0.3);
    }
    50% {
        transform: scale(1.1);
        box-shadow: 0 0 20px rgba(59, 130, 246, 0.6);
    }
}

.logo-icon svg {
    width: 20px;
    height: 20px;
    color: white;
}

.logo-text {
    font-size: 18px;
    font-weight: 700;
    background: linear-gradient(to right, #3b82f6, #60a5fa);
    -webkit-background-clip: text;
    background-clip: text;
    -webkit-text-fill-color: transparent;
    color: transparent;
}

.user-profile-section {
    padding: 0 20px 20px;
    border-bottom: 1px solid rgba(229, 231, 235, 0.5);
    text-align: center;
}

.user-avatar {
    margin: 10px auto;
    border: 2px solid #3b82f6;
    box-shadow: 0 0 10px rgba(59, 130, 246, 0.3);
}

.quick-stats {
    display: flex;
    justify-content: space-around;
    margin-top: 15px;
    padding-top: 10px;
    border-top: 1px dashed rgba(229, 231, 235, 0.5);
}

.stat-item {
    text-align: center;
}

.stat-number {
    font-size: 20px;
    font-weight: 700;
    color: #3b82f6;
    display: block;
}

@keyframes pulseBounce {
    0%, 100% {
        opacity: 1;
        transform: scale(1);
    }
    25% {
        opacity: 0.9;
        transform: scale(1.05);
    }
    50% {
        opacity: 1;
        transform: scale(1.15);
    }
    75% {
        opacity: 0.9;
        transform: scale(1.05);
    }
}

.stat-label {
    font-size: 12px;
    color: #6b7280;
}

.nav-menu {
    padding: 0 15px;
    flex: 1;
    overflow-y: auto;
}

.nav-menu h4 {
    color: #4677da;
    font-size: 20px;
    margin: 15px 0 10px;
    padding-left: 10px;
    text-transform: uppercase;
    letter-spacing: 1px;
    animation: fadeInDown 0.6s ease-out;
}

@keyframes fadeInDown {
    from {
        opacity: 0;
        transform: translateY(-10px);
    }
    to {
        opacity: 1;
        transform: translateY(0);
    }
}

.el-menu-vertical {
    background: transparent;
    border-right: none;
}

/* 为菜单项添加交错淡入动画 */
.el-menu-item {
    animation: slideInLeft 0.4s ease-out backwards;
}

.el-menu-item:nth-child(1) { animation-delay: 0.05s; }
.el-menu-item:nth-child(2) { animation-delay: 0.1s; }
.el-menu-item:nth-child(3) { animation-delay: 0.15s; }
.el-menu-item:nth-child(4) { animation-delay: 0.2s; }
.el-menu-item:nth-child(5) { animation-delay: 0.25s; }
.el-menu-item:nth-child(6) { animation-delay: 0.3s; }
.el-menu-item:nth-child(7) { animation-delay: 0.35s; }
.el-menu-item:nth-child(8) { animation-delay: 0.4s; }

@keyframes slideInLeft {
    from {
        opacity: 0;
        transform: translateX(-20px);
    }
    to {
        opacity: 1;
        transform: translateX(0);
    }
}

.el-menu-item,
.el-sub-menu__title {
    color: #1f2937 !important;
    height: 48px;
    line-height: 48px;
    margin: 5px 0;
    border-radius: 8px;
    transition: all 0.3s ease;
    position: relative;
    overflow: hidden;
}

.el-menu-item::before {
    content: '';
    position: absolute;
    left: 0;
    top: 0;
    width: 3px;
    height: 100%;
    background: linear-gradient(to bottom, #3b82f6, #60a5fa);
    transform: scaleY(0);
    transition: transform 0.3s ease;
}

.el-menu-item.is-active::before {
    transform: scaleY(1);
}

.el-menu-item:hover,
.el-sub-menu__title:hover {
    background: rgba(59, 130, 246, 0.1) !important;
    transform: translateX(5px);
}

.el-menu-item:active {
    transform: translateX(8px) scale(0.98);
    transition: all 0.1s ease;
}

.el-menu-item.is-active {
    background: rgba(59, 130, 246, 0.15) !important;
    color: #3b82f6 !important;
}

.el-menu-item .el-icon,
.el-sub-menu__title .el-icon {
    margin-right: 10px;
    width: 20px;
    font-size: 18px;
    color: #6b7280;
    display: flex;
    align-items: center;
    justify-content: center;
}

.el-menu-item .el-icon img,
.el-sub-menu__title .el-icon img {
    filter: brightness(0) saturate(100%) invert(41%) sepia(5%) saturate(542%) hue-rotate(182deg) brightness(91%) contrast(87%);
    transition: all 0.2s;
}

.el-menu-item.is-active .el-icon {
    color: #3b82f6;
}

.el-menu-item.is-active .el-icon img {
    filter: brightness(0) saturate(100%) invert(53%) sepia(96%) saturate(3089%) hue-rotate(196deg) brightness(100%) contrast(101%);
}

.el-menu-item:hover .el-icon img {
    filter: brightness(0) saturate(100%) invert(53%) sepia(96%) saturate(3089%) hue-rotate(196deg) brightness(100%) contrast(101%);
}

.motivation-quote {
    padding: 20px 20px 30px;
    text-align: center;
    border-top: 1px solid rgba(229, 231, 235, 0.5);
    margin-top: auto;
}

.quote-icon {
    color: #3b82f6;
    font-size: 20px;
    margin-bottom: 8px;
    display: block;
}

.motivation-quote p {
    font-size: 15px;
    font-style: italic;
    margin: 8px 0;
    line-height: 1.5;
    color: #1f2937;
}

.quote-author {
    color: #3b82f6;
    font-weight: 500;
    display: block;
    margin-top: 5px;
}

/* 主内容区样式 */
.main-content {
    flex: 1;
    margin-left: 260px;
    transition: transform 0.3s cubic-bezier(0.4, 0, 0.2, 1), opacity 0.3s ease;
    /* padding: 20px; */
}

.main-content.sidebar-exiting-content {
    transform: scale(1.02) translateX(20px);
    opacity: 0;
    transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

.dashboard-header {
    display: flex;
    justify-content: space-between;
    align-items: flex-start;
    margin-bottom: 25px;
    flex-wrap: wrap;
    gap: 20px;
}

.welcome-section {
    border-radius: 16px;
    margin-bottom: 24px;
    animation: fadeInUp 0.6s ease-out;
}

.welcome-section h1 {
    font-size: 28px;
    margin-bottom: 8px;
    background: linear-gradient(to right, #1f2937, #3b82f6);
    background-clip: text; /* 添加标准属性 */
    -webkit-background-clip: text; /* WebKit兼容性属性 */
    -webkit-text-fill-color: transparent;
}

/* 目标院校 */
.target-school {
    font-weight: 600;
    color: #2b6cb0;
}

/* 考研年份tag */
.custom-tag {
    display: inline-block;
    padding: 2px 10px;
    font-size: 12px;
    font-weight: 600;
    color: #1890ff;
    background-color: #e6f4ff;
    border: 1px solid #bae7ff;
    border-radius: 12px;
    margin-left: 8px;
    line-height: 1.4;
}

/* 倒计时badge */
.countdown-badge {
    display: inline-flex;
    align-items: center;
    gap: 8px;
    background: linear-gradient(135deg, #ff6b35, #f7931e);
    color: white;
    padding: 8px 16px;
    border-radius: 20px;
    font-weight: 600;
    font-size: 16px;
    box-shadow: 0 4px 8px rgba(247, 147, 30, 0.3);
    animation: float 3s ease-in-out infinite;
}

.countdown-number {
    font-size: 20px;
    font-weight: 800;
    min-width: 28px;
    text-align: center;
}

/* 微动效 */
@keyframes float {

    0%,
    100% {
        transform: translateY(0);
    }

    50% {
        transform: translateY(-4px);
    }
}

@keyframes fadeInUp {
    from {
        opacity: 0;
        transform: translateY(20px);
    }

    to {
        opacity: 1;
        transform: translateY(0);
    }
}

/* 响应式：小屏优化 */
@media (max-width: 768px) {
    .welcome-section {
        padding: 16px;
    }

    .welcome-title {
        font-size: 22px;
    }

    .countdown-badge {
        font-size: 14px;
        padding: 6px 12px;
    }

    .countdown-number {
        font-size: 18px;
    }
}

/* .target-text {
    color: #6b7280;
    font-size: 16px;
    margin-bottom: 10px;
} */

/* .countdown-badge {
    display: inline-block;
    background: linear-gradient(45deg, #ef4444, #f87171);
    color: white;
    padding: 4px 12px;
    border-radius: 20px;
    font-weight: 600;
    margin-top: 5px;
    box-shadow: 0 4px 10px rgba(239, 68, 68, 0.2);
}

.countdown-number {
    font-size: 20px;
    font-weight: 700;
    margin-right: 4px;
} */

.quick-actions {
    display: flex;
    gap: 12px;
    flex-wrap: wrap;
}

.action-button {
    height: 50px;
    padding: 0 20px;
    font-weight: 600;
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
    transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
    border-radius: 14px;
    display: inline-flex;
    align-items: center;
    justify-content: center;
}

.action-button:hover {
    transform: translateY(-3px);
    box-shadow: 0 8px 20px rgba(0, 0, 0, 0.12);
}

/* 蓝色渐变 - 第一个按钮 */
.action-button:nth-child(1) {
    background: linear-gradient(135deg, #3b82f6 0%, #2563eb 100%) !important;
    color: white !important;
    border: none !important;
}

/* 红色渐变 - 每日测试且有题时 */
.action-button.is-daily-active {
    background: linear-gradient(135deg, #f87171 0%, #ef4444 100%) !important;
    color: white !important;
    border: none !important;
}

/* 白色背景 - 普通状态 */
.action-button:not(.is-daily-active):not(:nth-child(1)) {
    background: white;
    color: #4b5563;
    border: 1px solid #e5e7eb;
}

.action-button .el-icon {
    margin-right: 8px;
    font-size: 18px;
}

/* 白色背景按钮的图标颜色转换 */
.action-button:not(.is-daily-active):not(:nth-child(1)) .el-icon img {
    filter: brightness(0) saturate(100%) invert(30%) sepia(10%) saturate(1000%) hue-rotate(200deg);
}

/* 彩色背景按钮的图标转为白色 */
.action-button.is-daily-active .el-icon img,
.action-button:nth-child(1) .el-icon img {
    filter: brightness(0) invert(1);
}

/* 菜单图标 SVG 样式 */
.menu-icon-svg {
    width: 1em;
    height: 1em;
    filter: invert(41%) sepia(5%) saturate(542%) hue-rotate(182deg) brightness(91%) contrast(87%);
    transition: all 0.2s;
}

:deep(.el-menu-item:hover) .menu-icon-svg,
:deep(.el-menu-item.is-active) .menu-icon-svg {
    filter: invert(53%) sepia(96%) saturate(3089%) hue-rotate(196deg) brightness(100%) contrast(101%);
}


/* 数据统计卡片 */
.stats-overview {
    display: grid;
    grid-template-columns: repeat(auto-fit, minmax(240px, 1fr));
    gap: 20px;
    margin-bottom: 30px;
}

.stat-card {
    background: rgba(255, 255, 255, 0.7);
    border-radius: 16px;
    padding: 20px;
    display: flex;
    align-items: center;
    cursor: pointer;
    transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
    box-shadow: 0 6px 15px rgba(0, 0, 0, 0.05);
    backdrop-filter: blur(5px);
    border: 1px solid rgba(209, 213, 219, 0.3);
    position: relative;
    overflow: hidden;
}

/* 卡片进入动画 */
.stat-card {
    animation: slideInUp 0.6s ease-out backwards;
}

.stat-card:nth-child(1) { animation-delay: 0.1s; }
.stat-card:nth-child(2) { animation-delay: 0.2s; }
.stat-card:nth-child(3) { animation-delay: 0.3s; }
.stat-card:nth-child(4) { animation-delay: 0.4s; }

@keyframes slideInUp {
    from {
        opacity: 0;
        transform: translateY(30px);
    }
    to {
        opacity: 1;
        transform: translateY(0);
    }
}

/* 卡片悬停效果 */
.stat-card::before {
    content: '';
    position: absolute;
    top: 0;
    left: 0;
    right: 0;
    bottom: 0;
    background: linear-gradient(135deg, rgba(255, 255, 255, 0.3) 0%, transparent 100%);
    opacity: 0;
    transition: opacity 0.3s ease;
}

.stat-card:hover::before {
    opacity: 1;
}

.stat-card:hover {
    transform: translateY(-8px) scale(1.02);
    box-shadow: 0 15px 35px rgba(59, 130, 246, 0.2);
    border-color: rgba(59, 130, 246, 0.4);
}

/* 不同卡片的悬停阴影颜色 */
.primary-card:hover {
    box-shadow: 0 15px 35px rgba(59, 130, 246, 0.25);
}

.success-card:hover {
    box-shadow: 0 15px 35px rgba(16, 185, 129, 0.25);
}

.warning-card:hover {
    box-shadow: 0 15px 35px rgba(245, 158, 11, 0.25);
}

.danger-card:hover {
    box-shadow: 0 15px 35px rgba(239, 68, 68, 0.25);
}

/* 图标容器样式 */
.stat-icon {
    width: 60px;
    height: 60px;
    border-radius: 16px;
    display: flex;
    align-items: center;
    justify-content: center;
    margin-right: 15px;
    flex-shrink: 0;
    position: relative;
    transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
}

/* 图标背景渐变动画 */
.bg-primary {
    background: linear-gradient(135deg, #3b82f6 0%, #60a5fa 100%);
    animation: gradientShift1 3s ease infinite;
    background-size: 200% 200%;
}

.bg-success {
    background: linear-gradient(135deg, #10b981 0%, #34d399 100%);
    animation: gradientShift2 3s ease infinite;
    background-size: 200% 200%;
}

.bg-warning {
    background: linear-gradient(135deg, #f59e0b 0%, #fbbf24 100%);
    animation: gradientShift3 3s ease infinite;
    background-size: 200% 200%;
}

.bg-danger {
    background: linear-gradient(135deg, #ef4444 0%, #f87171 100%);
    animation: gradientShift4 3s ease infinite;
    background-size: 200% 200%;
}

@keyframes gradientShift1 {
    0%, 100% { background-position: 0% 50%; }
    50% { background-position: 100% 50%; }
}

@keyframes gradientShift2 {
    0%, 100% { background-position: 0% 50%; }
    50% { background-position: 100% 50%; }
}

@keyframes gradientShift3 {
    0%, 100% { background-position: 0% 50%; }
    50% { background-position: 100% 50%; }
}

@keyframes gradientShift4 {
    0%, 100% { background-position: 0% 50%; }
    50% { background-position: 100% 50%; }
}

/* 图标脉冲动画 */
.stat-icon::after {
    content: '';
    position: absolute;
    top: -4px;
    left: -4px;
    right: -4px;
    bottom: -4px;
    border-radius: 18px;
    background: inherit;
    opacity: 0;
    z-index: -1;
    animation: pulse 2s ease-in-out infinite;
}

@keyframes pulse {
    0%, 100% {
        transform: scale(1);
        opacity: 0;
    }
    50% {
        transform: scale(1.1);
        opacity: 0.3;
    }
}

/* 图标悬停效果 */
.stat-card:hover .stat-icon {
    transform: rotate(10deg) scale(1.1);
}

.stat-icon .el-icon {
    color: white;
    font-size: 28px;
    transition: all 0.3s ease;
    animation: iconFloat 3s ease-in-out infinite;
    display: flex;
    align-items: center;
    justify-content: center;
}

.stat-icon .el-icon img {
    filter: brightness(0) saturate(100%) invert(100%);
}

@keyframes iconFloat {
    0%, 100% {
        transform: translateY(0);
    }
    50% {
        transform: translateY(-3px);
    }
}

.stat-card:hover .stat-icon .el-icon {
    transform: scale(1.1);
    animation: none;
}

/* 数字动画 */
.stat-content .stat-value {
    font-size: 28px;
    font-weight: 700;
    display: block;
    margin-bottom: 4px;
    color: #1f2937;
    background: linear-gradient(135deg, #1f2937 0%, #3b82f6 100%);
    background-clip: text;
    transition: all 0.3s ease;
}

.stat-card:hover .stat-content .stat-value {
    transform: scale(1.05);
    filter: brightness(1.1);
}

/* 标签样式 */
.stat-content .stat-label {
    color: #6b7280;
    font-size: 14px;
    font-weight: 500;
    transition: all 0.3s ease;
    position: relative;
}

.stat-card:hover .stat-content .stat-label {
    color: #3b82f6;
    transform: translateX(3px);
}

/* 图表区域 */
.charts-section {
    display: grid;
    grid-template-columns: repeat(auto-fit, minmax(550px, 1fr));
    gap: 25px;
    margin-bottom: 30px;
}

.chart-container {
    background: rgba(255, 255, 255, 0.7);
    border-radius: 16px;
    padding: 20px;
    box-shadow: 0 6px 15px rgba(0, 0, 0, 0.05);
    backdrop-filter: blur(5px);
    border: 1px solid rgba(209, 213, 219, 0.3);
    transition: all 0.3s ease;
}

.chart-container:hover {
    transform: translateY(-5px);
    box-shadow: 0 12px 25px rgba(59, 130, 246, 0.15);
}

.chart-title {
    font-size: 18px;
    display: flex;
    align-items: center;
    color: #1f2937;
    margin-bottom: 15px;
}

.chart-title .el-icon {
    margin-right: 8px;
    color: #3b82f6;
}

.chart-tip {
    margin-top: 10px;
    padding: 10px;
    background: rgba(59, 130, 246, 0.1);
    border-radius: 8px;
    font-size: 14px;
    color: #4b5563;
}

.chart-tip strong {
    color: #3b82f6;
    font-weight: 600;
}



/* 卡片头部样式 */
.feature-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 20px;
    padding-bottom: 12px;
    border-bottom: 2px solid rgba(209, 213, 219, 0.3);
}

.feature-header h3 {
    font-size: 18px;
    color: #1f2937;
    margin: 0;
    display: flex;
    align-items: center;
    gap: 8px;
}

.feature-header h3 .el-icon {
    color: #3b82f6;
}

.test-period {
    font-size: 13px;
    color: #6b7280;
    background: rgba(59, 130, 246, 0.1);
    padding: 4px 12px;
    border-radius: 12px;
}

/* 最近错题模块 */
.mistakes-list {
    max-height: 400px;
    overflow-y: auto;
    padding-right: 8px;
}

.mistakes-list::-webkit-scrollbar {
    width: 6px;
}

.mistakes-list::-webkit-scrollbar-thumb {
    background: rgba(59, 130, 246, 0.3);
    border-radius: 3px;
}

.mistakes-list::-webkit-scrollbar-track {
    background: rgba(209, 213, 219, 0.2);
}

.mistake-item {
    display: flex;
    gap: 12px;
    padding: 15px;
    margin-bottom: 12px;
    background: rgba(255, 255, 255, 0.6);
    border-radius: 12px;
    border: 1px solid rgba(209, 213, 219, 0.3);
    transition: all 0.3s ease;
    cursor: pointer;
}

.mistake-item:hover {
    background: rgba(255, 255, 255, 0.9);
    border-color: rgba(59, 130, 246, 0.4);
    transform: translateX(5px);
}

.mistake-index {
    width: 28px;
    height: 28px;
    background: linear-gradient(135deg, #3b82f6, #60a5fa);
    color: white;
    border-radius: 50%;
    display: flex;
    align-items: center;
    justify-content: center;
    font-weight: 600;
    font-size: 14px;
    flex-shrink: 0;
}

.mistake-content {
    flex: 1;
}

.mistake-subject {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 8px;
}

.subject-badge {
    padding: 3px 10px;
    border-radius: 12px;
    font-size: 12px;
    font-weight: 500;
}

.subject-badge.subject-math {
    background: rgba(59, 130, 246, 0.15);
    color: #3b82f6;
}

.subject-badge.subject-algebra {
    background: rgba(16, 185, 129, 0.15);
    color: #10b981;
}

.subject-badge.subject-probability {
    background: rgba(245, 158, 11, 0.15);
    color: #f59e0b;
}

.mistake-time {
    font-size: 12px;
    color: #9ca3af;
}

.mistake-question {
    color: #374151;
    line-height: 1.6;
    margin: 8px 0;
    font-size: 14px;
}

.empty-state {
    text-align: center;
    padding: 40px 20px;
    color: #10b981;
    font-size: 16px;
}

/* 每日测试模块 */
.daily-test-chart {
    margin-bottom: 15px;
}

.test-summary {
    display: flex;
    justify-content: space-around;
    padding: 15px;
    background: rgba(59, 130, 246, 0.05);
    border-radius: 12px;
}

.summary-item {
    text-align: center;
}

.summary-label {
    display: block;
    font-size: 13px;
    color: #6b7280;
    margin-bottom: 5px;
}

.summary-value {
    display: block;
    font-size: 20px;
    font-weight: 700;
    color: #3b82f6;
}

/* 响应式调整 */
@media (max-width: 1200px) {

    .charts-section {
        grid-template-columns: 1fr;
    }

    .dashboard-header {
        flex-direction: column;
        align-items: center;
    }

    .quick-actions {
        width: 100%;
        justify-content: center;
    }
}

@media (max-width: 900px) {
    .sidebar {
        width: 70px;
        overflow: hidden;
    }

    .logo-text,
    .user-profile-section h3,
    .stat-label,
    .nav-menu h4,
    .quote-author,
    .motivation-quote p,
    .el-sub-menu__title span,
    .el-menu-item span {
        display: none;
    }

    .logo-section,
    .user-profile-section,
    .nav-menu,
    .motivation-quote {
        text-align: center;
    }

    .el-sub-menu__title i,
    .el-menu-item i {
        margin: 0;
    }

    .main-content {
        margin-left: 70px;
    }
}

@media (max-width: 768px) {
    .stats-overview {
        grid-template-columns: 1fr;
    }

    .quick-actions {
        flex-direction: column;
        width: 100%;
    }

    .action-button {
        width: 100%;
    }
}

/* 悬浮胶囊样式 */
.floating-capsules {
    position: fixed;
    right: 30px;
    bottom: 30px;
    z-index: 999;
    display: flex;
    flex-direction: column;
    gap: 15px;
}

.floating-capsule {
    cursor: pointer;
    animation: slideInRight 0.5s ease-out;
}

.floating-capsule:nth-child(1) {
    animation-delay: 0s;
}

.floating-capsule:nth-child(2) {
    animation-delay: 0.1s;
}

.floating-capsule:nth-child(3) {
    animation-delay: 0.2s;
}

@keyframes slideInRight {
    from {
        opacity: 0;
        transform: translateX(100px);
    }
    to {
        opacity: 1;
        transform: translateX(0);
    }
}

.capsule-content {
    display: flex;
    align-items: center;
    gap: 10px;
    padding: 12px 24px;
    border-radius: 50px;
    box-shadow: 0 8px 24px rgba(0, 0, 0, 0.15);
    transition: all 0.3s ease;
    backdrop-filter: blur(10px);
}

/* 反馈胶囊 - 橙色渐变 */
.feedback-capsule .capsule-content {
    background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
    box-shadow: 0 8px 24px rgba(245, 87, 108, 0.4);
}

/* 客服胶囊 - 绿色渐变 */
.service-capsule .capsule-content {
    background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%);
    box-shadow: 0 8px 24px rgba(79, 172, 254, 0.4);
}

.capsule-content:hover {
    transform: translateY(-3px) scale(1.05);
}

.ai-capsule .capsule-content:hover {
    box-shadow: 0 12px 32px rgba(102, 126, 234, 0.5);
}

.feedback-capsule .capsule-content:hover {
    box-shadow: 0 12px 32px rgba(245, 87, 108, 0.5);
}

.service-capsule .capsule-content:hover {
    box-shadow: 0 12px 32px rgba(79, 172, 254, 0.5);
}

.capsule-icon {
    width: 24px;
    height: 24px;
    color: white;
    animation: bounce 2s infinite;
}

.feedback-capsule .capsule-icon,
.service-capsule .capsule-icon {
    animation: bounce 2s infinite 0.5s;
}

@keyframes bounce {
    0%, 100% {
        transform: translateY(0);
    }
    50% {
        transform: translateY(-5px);
    }
}

.capsule-text {
    color: white;
    font-size: 15px;
    font-weight: 600;
    letter-spacing: 0.5px;
}

/* 响应式调整 */
@media (max-width: 768px) {
    .floating-capsules {
        right: 20px;
        bottom: 20px;
        gap: 12px;
    }

    .capsule-content {
        padding: 10px 18px;
    }

    .capsule-icon {
        width: 20px;
        height: 20px;
    }

    .capsule-text {
        font-size: 14px;
    }
}

/* 错题详情对话框样式 */
.question-detail-dialog .el-dialog__body {
    padding: 20px;
    max-height: 600px;
    overflow-y: auto;
}

.question-detail-content {
    color: #1f2937;
}

.detail-section {
    margin-bottom: 24px;
    padding-bottom: 20px;
    border-bottom: 1px solid #e5e7eb;
}

.detail-section:last-child {
    border-bottom: none;
}

.detail-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 12px;
}

.detail-header h4 {
    font-size: 16px;
    font-weight: 600;
    color: #3b82f6;
    margin: 0;
}

.question-meta {
    display: flex;
    align-items: center;
}

.question-body {
    line-height: 1.8;
}

.question-text {
    font-size: 15px;
    color: #374151;
    margin-bottom: 16px;
    padding: 12px;
    background: #f9fafb;
    border-radius: 8px;
}

.options-list {
    margin-top: 12px;
}

.option-item {
    display: flex;
    padding: 8px 12px;
    margin-bottom: 8px;
    background: white;
    border: 1px solid #e5e7eb;
    border-radius: 6px;
    transition: all 0.2s;
}

.option-item:hover {
    border-color: #3b82f6;
    background: #f0f9ff;
}

.option-label {
    font-weight: 600;
    color: #3b82f6;
    min-width: 24px;
}

.option-text {
    flex: 1;
    color: #4b5563;
}

.answer-section {
    background: #f0f9ff;
    padding: 16px;
    border-radius: 8px;
}

.answer-row,
.analysis-row {
    margin-bottom: 12px;
}

.answer-row:last-child,
.analysis-row:last-child {
    margin-bottom: 0;
}

.answer-label,
.analysis-label {
    font-weight: 600;
    color: #1f2937;
    margin-right: 8px;
}

.answer-value {
    font-weight: 600;
    font-size: 16px;
}

.answer-value.primary {
    color: #10b981;
}

.analysis-content {
    display: inline-block;
    color: #4b5563;
    line-height: 1.8;
    padding: 8px 0;
}

.tags-list {
    display: flex;
    flex-wrap: wrap;
}

/* KaTeX样式优化 */
.latex-answer :deep(.katex),
.latex-content :deep(.katex) {
    font-size: 1.1em;
}

.latex-content :deep(.katex-display) {
    margin: 12px 0;
}

.question-text :deep(.katex) {
    font-size: 1.05em;
}

.option-text :deep(.katex) {
    font-size: 1em;
}
</style>
