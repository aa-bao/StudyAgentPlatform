<template>
    <div class="dashboard-container">
        <!-- 侧边栏 -->
        <div class="sidebar" :class="{ 'sidebar-exiting': isSidebarExiting }">
            <div class="logo-section">
                <div class="logo-icon">
                    <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                        <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"></path>
                        <polyline points="14 2 14 8 20 8"></polyline>
                        <line x1="16" y1="13" x2="8" y2="13"></line>
                        <line x1="16" y1="17" x2="8" y2="17"></line>
                        <polyline points="10 9 9 9 8 9"></polyline>
                    </svg>
                </div>
                <span class="logo-text">考研刷题平台</span>
            </div>

            <div class="user-profile-section">
                <el-avatar :size="70" :src="userInfo.avatar" class="user-avatar" />
                <h3>{{ userInfo.nickname }}</h3>
                <p class="user-role">{{ userInfo.role === 'admin' ? '管理员' : '考研学子' }}</p>

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
                    <el-menu-item index="/user/dashboard">
                        <el-icon>
                            <PieChart />
                        </el-icon>
                        <span>备考看板</span>
                    </el-menu-item>
                    <el-menu-item index="subject">
                        <el-icon>
                            <Reading />
                        </el-icon>
                        <span>科目练习</span>
                    </el-menu-item>
                    <el-menu-item index="/user/correction-notebook">
                        <el-icon>
                            <Failed />
                        </el-icon>
                        <span>错题本</span>
                    </el-menu-item>
                    <el-menu-item index="/user/paper-list">
                        <el-icon>
                            <Document />
                        </el-icon>
                        <span>真题模考</span>
                    </el-menu-item>
                    <el-menu-item index="/user/topic-drill?rootId=1">
                        <el-icon>
                            <svg class="icon" viewBox="0 0 1024 1024" version="1.1"
                                xmlns="http://www.w3.org/2000/svg" width="18" height="18">
                                <path
                                    d="M726.521506 0a35.929459 35.929459 0 0 1 35.677952 31.761642L762.450965 35.929459v74.841063h166.065959a35.929459 35.929459 0 0 1 4.203747 71.607412l-4.203747 0.251506h-193.695713l-167.036055 133.442011 31.114911 233.541483 128.016663-98.662295a35.139011 35.139011 0 0 1 17.21021-7.149962l4.70676-0.323365h131.861114a35.929459 35.929459 0 0 1 4.167817 71.607412l-4.167817 0.251506h-119.645098l-133.945023 103.297194a36.145036 36.145036 0 0 1-18.036589 7.257751l43.510575 326.239488H978.027718a35.929459 35.929459 0 0 1 4.203747 71.607411L978.027718 1023.98958H43.861785a35.929459 35.929459 0 0 1-4.203746-71.607411L43.861785 952.130663h324.910098l21.701393-222.51114-77.499843-59.714761-128.411886 0.03593a35.965388 35.965388 0 0 1-35.713883-31.761642l-0.215576-4.167817a35.929459 35.929459 0 0 1 31.725712-35.713882l4.203747-0.215577h140.699761a35.929459 35.929459 0 0 1 18.000659 4.814547l3.916311 2.65878 51.522844 39.702053 15.95268-163.622757-132.651563-53.031881a35.929459 35.929459 0 0 1-16.56348-13.509477l-2.227627-3.808522L201.268745 287.435672H79.791244a35.929459 35.929459 0 0 1-4.203746-71.607412L79.791244 215.576754h85.548042L119.493296 123.848845a35.929459 35.929459 0 0 1 62.193894-35.785741l2.119838 3.664805 137.573898 275.219655 100.530626 40.169136 16.276045-166.892337-134.879189-103.979855a35.929459 35.929459 0 0 1 40.133206-59.427325l3.736664 2.515062 99.237165 76.529748 12.036369-123.417692a35.929459 35.929459 0 0 1 70.529528-5.748713l0.826378 4.491182 26.875235 201.743912L690.592047 125.968683V35.929459a35.929459 35.929459 0 0 1 35.929459-35.929459"
                                    fill="currentColor"></path>
                            </svg>
                        </el-icon>
                        <span>知识体系树</span>
                    </el-menu-item>
                    <el-menu-item index="/rankings">
                        <el-icon>
                            <Trophy />
                        </el-icon>
                        <span>排行榜</span>
                    </el-menu-item>
                    <el-menu-item index="/user/profile">
                        <el-icon>
                            <User />
                        </el-icon>
                        <span>个人中心</span>
                    </el-menu-item>
                    <el-menu-item index="/user/community">
                        <el-icon>
                            <School />
                        </el-icon>
                        <span>学习社区</span>
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
                    <h1 class="welcome-title">你好，{{ userInfo.nickname }}！今天也要加油哦！</h1>
                    <p class="target-text">
                        🎯 考研目标：
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
                            <Reading />
                        </el-icon>
                        <span>刷书</span>
                    </el-button>

                    <el-button size="large" @click="startTopicDrill" class="action-button">
                        <el-icon :size="18">
                            <Failed />
                        </el-icon>
                        <span>刷题</span>
                    </el-button>

                    <el-button size="large" @click="startMockExam" class="action-button">
                        <el-icon :size="18">
                            <DocumentChecked />
                        </el-icon>
                        <span>刷卷</span>
                    </el-button>
                </div>
            </div>

            <!-- 数据统计卡片 -->
            <div class="stats-overview">
                <div class="stat-card primary-card" @click="ViewDashboard()">
                    <div class="stat-icon bg-primary">
                        <el-icon :size="24">
                            <Collection />
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
                            <Timer />
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
                            <Promotion />
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
                            <Failed />
                        </el-icon>
                    </div>
                    <div class="stat-content">
                        <span class="stat-value">{{ stats.mistakes }}</span>
                        <span class="stat-label">错题本数量</span>
                    </div>
                </div>
            </div>

            <!-- 图表区域 -->
            <div class="charts-section">
                <div class="chart-container">
                    <h3 class="chart-title">学科能力雷达图</h3>
                    <div ref="radarChart" class="chart-area" style="width: 100%; height: 320px;"></div>
                    <p class="chart-tip">💡 你的<strong>高等数学</strong>能力突出，<strong>线性代数</strong>需要加强练习</p>
                </div>

                <div class="chart-container">
                    <h3 class="chart-title">错题热力分布</h3>
                    <div ref="heatMap" class="chart-area" style="width: 100%; height: 320px;"></div>
                    <p class="chart-tip">🔥 <strong>泰勒公式</strong>相关题目错误率高达65%，建议优先复习</p>
                </div>
            </div>

            <!-- 个性化推荐和学习计划 -->
            <div class="recommendations-section">
                <div class="recommendations">
                    <div class="section-header">
                        <h2><el-icon name="Hot" /> 为你推荐</h2>
                        <el-button type="primary" text @click="refreshRecommendations">刷新推荐</el-button>
                    </div>

                    <div class="recommendation-cards">
                        <div class="recommendation-card" v-for="(recommendation, index) in recommendations"
                            :key="index">
                            <div class="card-header">
                                <span class="subject-tag" :class="recommendation.subjectColor">
                                    {{ recommendation.subjectName }}
                                </span>
                                <span class="difficulty-tag" :class="'difficulty-' + recommendation.difficulty">
                                    {{ getDifficultyText(recommendation.difficulty) }}
                                </span>
                            </div>
                            <div class="card-content">
                                <p class="question-preview">{{ recommendation.content }}</p>
                                <div class="card-footer">
                                    <el-button type="primary" size="small" @click="goToPractice(recommendation)">
                                        立即练习
                                    </el-button>
                                    <span class="recommendation-reason">
                                        {{ recommendation.reason }}
                                    </span>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>

                <div class="study-plan">
                    <div class="section-header">
                        <h2><el-icon name="Guide" /> 今日学习计划</h2>
                        <el-button type="primary" text @click="generateNewPlan">重新生成</el-button>
                    </div>

                    <div class="plan-items">
                        <div class="plan-item" v-for="(item, index) in studyPlan" :key="index"
                            :class="{ completed: item.completed }">
                            <div class="plan-checkbox">
                                <el-checkbox v-model="item.completed" @change="togglePlanItem(item)"></el-checkbox>
                            </div>
                            <div class="plan-content">
                                <h4>{{ item.title }}</h4>
                                <p class="plan-detail">{{ item.detail }}</p>
                                <div class="progress-bar">
                                    <el-progress :percentage="item.progress" :color="getProgressColor(item.progress)"
                                        :stroke-width="6" />
                                </div>
                            </div>
                        </div>
                    </div>

                    <div class="plan-actions">
                        <el-button type="primary" @click="customizePlan">自定义计划</el-button>
                        <el-button @click="generateNewPlan">智能生成新计划</el-button>
                    </div>
                </div>
            </div>
        </div>

        <!-- 首次用户引导层 -->
        <div v-if="isNewUser" class="onboarding-overlay" @click="finishOnboarding">
            <div class="onboarding-card" @click.stop>
                <div class="onboarding-header">
                    <h2>欢迎使用考研刷题平台</h2>
                    <p>让我们快速了解如何高效备考</p>
                </div>

                <div class="onboarding-steps">
                    <div class="step-item" v-for="(step, index) in onboardingSteps" :key="index"
                        :class="{ active: currentStep === index }">
                        <div class="step-number">{{ index + 1 }}</div>
                        <div class="step-content">
                            <h4>{{ step.title }}</h4>
                            <p>{{ step.description }}</p>
                        </div>
                    </div>
                </div>

                <div class="step-indicators">
                    <span v-for="(step, index) in onboardingSteps" :key="index" class="indicator"
                        :class="{ active: currentStep === index }"></span>
                </div>

                <div class="step-actions">
                    <el-button v-if="currentStep > 0" @click="prevStep">上一步</el-button>
                    <el-button v-if="currentStep < onboardingSteps.length - 1" type="primary" @click="nextStep">
                        {{ currentStep === onboardingSteps.length - 2 ? '完成引导' : '下一步' }}
                    </el-button>
                    <el-button v-else type="success" @click="finishOnboarding">
                        开始学习
                    </el-button>
                </div>
            </div>
        </div>

        <!-- 悬浮胶囊按钮组 -->
        <div class="floating-capsules">
            <!-- AI助手 -->
            <div class="floating-capsule ai-capsule" @click="handleFloatingAction">
                <div class="capsule-content">
                    <svg class="capsule-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                        <path d="M12 19V5M5 12l7-7 7 7"/>
                    </svg>
                    <span class="capsule-text">AI助手</span>
                </div>
            </div>

            <!-- 反馈 -->
            <div class="floating-capsule feedback-capsule" @click="handleFeedback">
                <div class="capsule-content">
                    <svg class="capsule-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                        <path d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z"/>
                    </svg>
                    <span class="capsule-text">反馈</span>
                </div>
            </div>

            <!-- 客服 -->
            <div class="floating-capsule service-capsule" @click="handleService">
                <div class="capsule-content">
                    <svg class="capsule-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                        <path d="M21 11.5a8.38 8.38 0 0 1-.9 3.8 8.5 8.5 0 0 1-7.6 4.7 8.38 8.38 0 0 1-3.8-.9L3 21l1.9-5.7a8.38 8.38 0 0 1-.9-3.8 8.5 8.5 0 0 1 4.7-7.6 8.38 8.38 0 0 1 3.8-.9h.5a8.48 8.48 0 0 1 8 8v.5z"/>
                    </svg>
                    <span class="capsule-text">客服</span>
                </div>
            </div>
        </div>
    </div>
</template>

<script setup>
// 导入图标
// import singlePracticeIcon from '@/assets/icons/single-practice.svg?url'
// import mockExamIcon from '@/assets/icons/mock-exam.svg?url'
// import correctionNotebookIcon from '@/assets/icons/correction-notebook.svg?url'
import {
    Reading,
    Failed,
    Document,
    Operation,
    Trophy,
    User,
    DocumentChecked,
    Collection,
    Timer,
    Promotion
} from '@element-plus/icons-vue'

import { ref, onMounted, onUnmounted, computed, onBeforeUnmount} from 'vue'
import * as echarts from 'echarts'
import { useRouter, useRoute } from 'vue-router'
import { getHomePageDataApi } from '@/api/user'
import { ElMessage } from 'element-plus'

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

// 个性化推荐
const recommendations = ref([
    {
        id: 1,
        subjectName: '高等数学',
        subjectColor: 'math',
        difficulty: 3,
        content: '求函数 f(x) = x³ - 3x² + 2 的极值点及极值',
        reason: '根据你最近的错题，推荐相似题目进行巩固'
    },
    {
        id: 2,
        subjectName: '线性代数',
        subjectColor: 'algebra',
        difficulty: 2,
        content: '计算行列式 | 1 2 3 | | 4 5 6 | | 7 8 9 | 的值',
        reason: '你尚未覆盖的知识点：特殊行列式计算'
    },
    {
        id: 3,
        subjectName: '概率论',
        subjectColor: 'probability',
        difficulty: 4,
        content: '设随机变量 X 服从参数为 λ 的泊松分布，求 E(X²)',
        reason: '系统预测这道题对你的考研目标很有帮助'
    }
])

// 学习计划
const studyPlan = ref([
    {
        id: 1,
        title: '高等数学基础练习',
        detail: '完成5道极限与连续相关题目',
        progress: 40,
        completed: false
    },
    {
        id: 2,
        title: '线性代数专项突破',
        detail: '复习矩阵运算，完成3道典型例题',
        progress: 0,
        completed: false
    },
    {
        id: 3,
        title: '错题本复习',
        detail: '复习10道之前做错的题目',
        progress: 100,
        completed: true
    }
])

// 首次用户引导
const isNewUser = ref(true)
const currentStep = ref(0)
const onboardingSteps = ref([
    {
        title: '设定考研目标',
        description: '完善你的考研目标院校、专业和考试时间，系统将为你定制专属学习计划。'
    },
    {
        title: '选择科目体系',
        description: '选择你需要备考的科目，系统会根据你的选择智能推荐学习内容。'
    },
    {
        title: '开始首次刷题',
        description: '完成第一套入门题目，系统将根据你的表现调整后续推荐策略。'
    },
    {
        title: '了解数据看板',
        description: '通过数据可视化图表追踪学习进度，找出薄弱知识点进行针对性突破。'
    },
    {
        title: '制定学习计划',
        description: '使用智能计划功能，为每天的学习设定明确目标，保持备考节奏。'
    }
])

// 图表引用
const radarChart = ref(null)
const heatMap = ref(null)

// 难度文本映射
const getDifficultyText = (level) => {
    const levels = ['简单', '中等', '困难', '挑战']
    return levels[level - 1] || '中等'
}

// 进度条颜色
const getProgressColor = (percentage) => {
    if (percentage < 30) return '#ff4d4f'
    if (percentage < 70) return '#faad14'
    return '#52c41a'
}

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

const ViewDashboard = () => {
    navigateWithAnimation('/user/dashboard')
}

const viewMistakes = () => {
    navigateWithAnimation('/user/correction-notebook')
}

const startMockExam = () => {
    navigateWithAnimation('/user/paper-list')
}

const goToPractice = (recommendation) => {
    if (recommendation) {
        navigateWithAnimation(`/user/practice/${recommendation.id}`)
    } else {
        navigateWithAnimation('/user/practice')
    }
}

// 学习计划操作
const togglePlanItem = (item) => {
    item.progress = item.completed ? 100 : 0
}
const customizePlan = () => {
    ElMessage.info('跳转到自定义计划页面')
}
const generateNewPlan = () => {
    ElMessage.success('已生成新的学习计划！')
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

// 推荐刷新
const refreshRecommendations = () => {
    ElMessage.success('已刷新推荐内容！')
}

// 首次用户引导
const nextStep = () => {
    if (currentStep.value < onboardingSteps.value.length - 1) {
        currentStep.value++
    } else {
        finishOnboarding()
    }
}
const prevStep = () => {
    if (currentStep.value > 0) {
        currentStep.value--
    }
}
const finishOnboarding = () => {
    isNewUser.value = false
    localStorage.setItem('onboardingCompleted', 'true')
}

// 初始化图表
const initCharts = () => {
    // 雷达图
    const radarChartInstance = echarts.init(radarChart.value)
    const radarOption = {
        backgroundColor: 'transparent',
        tooltip: {
            backgroundColor: 'rgba(255, 255, 255, 0.95)',
            borderColor: '#e5e7eb',
            textStyle: { color: '#1f2937' }
        },
        radar: {
            indicator: [
                { name: '高等数学', max: 100 },
                { name: '线性代数', max: 100 },
                { name: '概率论', max: 100 },
                { name: '英语', max: 100 },
                { name: '政治', max: 100 }
            ],
            radius: '65%',
            splitArea: {
                areaStyle: {
                    color: ['rgba(59, 130, 246, 0.08)', 'rgba(59, 130, 246, 0.15)']
                }
            },
            axisName: {
                color: '#4b5563',
                fontWeight: 500
            },
            axisLine: {
                lineStyle: {
                    color: 'rgba(59, 130, 246, 0.2)'
                }
            },
            splitLine: {
                lineStyle: {
                    color: 'rgba(59, 130, 246, 0.1)'
                }
            }
        },
        series: [{
            type: 'radar',
            data: [{
                value: [92, 65, 78, 85, 70],
                name: '能力分布',
                symbol: 'circle',
                symbolSize: 8,
                lineStyle: {
                    color: '#3b82f6',
                    width: 3
                },
                areaStyle: {
                    color: 'rgba(59, 130, 246, 0.25)'
                },
                itemStyle: {
                    color: '#3b82f6',
                    borderColor: '#fff',
                    borderWidth: 2
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
            const val = Math.floor(Math.random() * 100)
            heatMapData.push([j, i, val])
        }
    }

    const heatMapOption = {
        backgroundColor: 'transparent',
        tooltip: {
            backgroundColor: 'rgba(255, 255, 255, 0.95)',
            borderColor: '#e5e7eb',
            textStyle: { color: '#1f2937' }
        },
        grid: {
            left: '15%',
            right: '5%',
            top: '10%',
            bottom: '20%',
            containLabel: true
        },
        xAxis: {
            type: 'category',
            data: xAxisData,
            splitArea: {
                show: true,
                areaStyle: {
                    color: ['rgba(255,255,255,0.8)', 'rgba(249,250,251,0.8)']
                }
            },
            axisLabel: {
                rotate: 45,
                fontSize: 10,
                color: '#4b5563'
            }
        },
        yAxis: {
            type: 'category',
            data: yAxisData,
            splitArea: {
                show: true,
                areaStyle: {
                    color: ['rgba(255,255,255,0.8)', 'rgba(249,250,251,0.8)']
                }
            },
            axisLabel: {
                color: '#4b5563'
            }
        },
        visualMap: {
            min: 0,
            max: 100,
            calculable: true,
            orient: 'horizontal',
            left: 'center',
            bottom: '0%',
            text: ['高错误率', '低错误率'],
            inRange: {
                color: ['#dbeafe', '#bfdbfe', '#93c5fd', '#60a5fa', '#3b82f6']
            },
            textStyle: {
                color: '#4b5563'
            }
        },
        series: [{
            name: '错题分布',
            type: 'heatmap',
            data: heatMapData,
            label: {
                show: true,
                formatter: function (params) {
                    return params.value[2] > 50 ? params.value[2] : ''
                },
                color: '#fff',
                fontWeight: 500
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

    // 响应窗口大小变化
    const resizeCharts = () => {
        radarChartInstance.resize()
        heatMapInstance.resize()
    }

    window.addEventListener('resize', resizeCharts)

    return () => {
        window.removeEventListener('resize', resizeCharts)
        radarChartInstance.dispose()
        heatMapInstance.dispose()
    }
}



// 悬浮胶囊点击事件
const handleFloatingAction = () => {
    ElMessage.info('AI助手功能开发中...')
}

const handleFeedback = () => {
    ElMessage.info('反馈功能开发中...')
}

const handleService = () => {
    ElMessage.info('客服功能开发中...')
}

// 监听菜单点击事件(针对菜单项的导航)
const handleMenuNavigation = (path) => {
    navigateWithAnimation(path)
}

// 组件挂载
onMounted(() => {
    // 获取首页数据
    fetchHomePageData()

    // 检查是否已完成引导
    if (localStorage.getItem('onboardingCompleted') === 'true') {
        isNewUser.value = false
    }

    // 初始化图表
    const cleanup = initCharts()

    // 检测是否已完成引导
    if (localStorage.getItem('onboardingCompleted') === 'true') {
        isNewUser.value = false
    }

    // 在组件卸载时清理
    onUnmounted(cleanup)
})

</script>

<style scoped>
.dashboard-container {
    display: flex;
    min-height: 100vh;
    background: linear-gradient(135deg, #f0f9ff 0%, #e6f7ff 100%);
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

.user-role {
    color: #6b7280;
    font-size: 13px;
    margin-top: 4px;
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
    animation: pulseBounce 1.5s ease-in-out infinite;
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
}

.el-menu-item.is-active .el-icon {
    color: #3b82f6;
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
    gap: 15px;
    flex-wrap: wrap;
}

.action-button {
    height: 50px;
    padding: 0 25px;
    font-weight: 500;
    box-shadow: 0 4px 12px rgba(59, 130, 246, 0.15);
    transition: all 0.3s ease;
    background: white;
    border: 1px solid rgba(229, 231, 235, 0.5);
    border-radius: 16px;}

.action-button:hover {
    transform: translateY(-2px);
    box-shadow: 0 6px 15px rgba(59, 130, 246, 0.2);
}

.action-button:first-child {
    background: linear-gradient(45deg, #3b82f6, #60a5fa);
    color: white;
    border: none;
}

.action-button .el-icon {
    margin-right: 8px;
    font-size: 18px;
    vertical-align: middle;
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
    -webkit-background-clip: text;
    background-clip: text;
    -webkit-text-fill-color: transparent;
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
}

.chart-title {
    font-size: 18px;
    margin-bottom: 15px;
    display: flex;
    align-items: center;
    color: #1f2937;
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

/* 推荐和计划区域 */
.recommendations-section {
    display: grid;
    grid-template-columns: repeat(auto-fit, minmax(550px, 1fr));
    gap: 25px;
}

.section-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 20px;
    padding-bottom: 10px;
    border-bottom: 1px solid rgba(209, 213, 219, 0.5);
}

.section-header h2 {
    font-size: 18px;
    display: flex;
    align-items: center;
    color: #1f2937;
}

.section-header h2 .el-icon {
    margin-right: 8px;
    color: #3b82f6;
}

.recommendation-cards {
    display: flex;
    flex-direction: column;
    gap: 15px;
}

.recommendation-card {
    background: rgba(255, 255, 255, 0.7);
    border-radius: 12px;
    overflow: hidden;
    transition: all 0.3s ease;
    box-shadow: 0 4px 10px rgba(0, 0, 0, 0.05);
    border: 1px solid rgba(209, 213, 219, 0.3);
}

.recommendation-card:hover {
    transform: translateX(5px);
    box-shadow: 0 6px 15px rgba(59, 130, 246, 0.1);
}

.card-header {
    display: flex;
    justify-content: space-between;
    padding: 12px 15px;
    background: rgba(255, 255, 255, 0.5);
    border-bottom: 1px solid rgba(209, 213, 219, 0.3);
}

.subject-tag {
    padding: 3px 10px;
    border-radius: 15px;
    font-size: 12px;
    font-weight: 500;
}

.subject-tag.math {
    background: rgba(59, 130, 246, 0.15);
    color: #3b82f6;
}

.subject-tag.algebra {
    background: rgba(16, 185, 129, 0.15);
    color: #10b981;
}

.subject-tag.probability {
    background: rgba(245, 158, 11, 0.15);
    color: #f59e0b;
}

.difficulty-tag {
    padding: 3px 8px;
    border-radius: 12px;
    font-size: 12px;
    font-weight: 500;
}

.difficulty-1 {
    background: rgba(16, 185, 129, 0.15);
    color: #10b981;
}

.difficulty-2 {
    background: rgba(245, 158, 11, 0.15);
    color: #f59e0b;
}

.difficulty-3 {
    background: rgba(239, 68, 68, 0.15);
    color: #ef4444;
}

.difficulty-4 {
    background: rgba(139, 92, 246, 0.15);
    color: #8b5cf6;
}

.card-content {
    padding: 15px;
}

.question-preview {
    margin-bottom: 15px;
    line-height: 1.6;
    color: #4b5563;
    min-height: 60px;
}

.card-footer {
    display: flex;
    justify-content: space-between;
    align-items: center;
}

.recommendation-reason {
    color: #6b7280;
    font-size: 13px;
    max-width: 70%;
}

.plan-items {
    margin-bottom: 20px;
}

.plan-item {
    display: flex;
    padding: 15px;
    margin-bottom: 12px;
    background: rgba(255, 255, 255, 0.7);
    border-radius: 12px;
    transition: all 0.3s ease;
    border: 1px solid rgba(209, 213, 219, 0.3);
}

.plan-item:hover {
    background: rgba(255, 255, 255, 0.9);
}

.plan-item.completed {
    opacity: 0.8;
    background: rgba(16, 185, 129, 0.1);
    border-color: rgba(16, 185, 129, 0.3);
}

.plan-checkbox {
    margin-right: 15px;
    display: flex;
    align-items: center;
}

.plan-content {
    flex: 1;
}

.plan-content h4 {
    margin: 0 0 5px;
    font-size: 16px;
    color: #1f2937;
}

.plan-detail {
    color: #6b7280;
    font-size: 14px;
    margin-bottom: 10px;
}

.progress-bar {
    margin-top: 5px;
}

.plan-actions {
    display: flex;
    justify-content: center;
    gap: 15px;
    margin-top: 10px;
}

/* 新手引导层 */
.onboarding-overlay {
    position: fixed;
    top: 0;
    left: 0;
    right: 0;
    bottom: 0;
    background: rgba(0, 0, 0, 0.6);
    display: flex;
    justify-content: center;
    align-items: center;
    z-index: 1000;
    backdrop-filter: blur(8px);
}

.onboarding-card {
    background: linear-gradient(135deg, #ffffff 0%, #f0f9ff 100%);
    width: 90%;
    max-width: 650px;
    border-radius: 20px;
    padding: 30px;
    box-shadow: 0 25px 50px rgba(0, 0, 0, 0.15);
    border: 1px solid rgba(209, 213, 219, 0.5);
    position: relative;
    animation: fadeInUp 0.5s ease;
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

.onboarding-header {
    text-align: center;
    margin-bottom: 30px;
}

.onboarding-header h2 {
    font-size: 26px;
    margin-bottom: 10px;
    background: linear-gradient(to right, #1f2937, #3b82f6);
    background-clip: text;
    -webkit-background-clip: text;
    -webkit-text-fill-color: transparent;
}

.onboarding-header p {
    color: #6b7280;
    font-size: 16px;
}

.onboarding-steps {
    margin: 20px 0 30px;
}

.step-item {
    display: flex;
    padding: 15px;
    margin-bottom: 15px;
    background: rgba(255, 255, 255, 0.5);
    border-radius: 12px;
    opacity: 0.6;
    transition: all 0.3s ease;
    cursor: pointer;
}

.step-item.active {
    opacity: 1;
    background: rgba(59, 130, 246, 0.1);
    border-left: 4px solid #3b82f6;
    transform: translateX(5px);
}

.step-number {
    width: 30px;
    height: 30px;
    background: linear-gradient(45deg, #3b82f6, #60a5fa);
    border-radius: 50%;
    display: flex;
    align-items: center;
    justify-content: center;
    color: white;
    font-weight: bold;
    margin-right: 15px;
    flex-shrink: 0;
}

.step-content h4 {
    margin: 0 0 5px;
    color: #3b82f6;
}

.step-indicators {
    display: flex;
    justify-content: center;
    margin-bottom: 25px;
    gap: 8px;
}

.indicator {
    display: inline-block;
    width: 12px;
    height: 12px;
    border-radius: 50%;
    background: rgba(209, 213, 219, 0.5);
    transition: all 0.3s ease;
}

.indicator.active {
    background: #3b82f6;
    transform: scale(1.2);
}

.step-actions {
    display: flex;
    justify-content: space-between;
    gap: 15px;
}

/* 响应式调整 */
@media (max-width: 1200px) {

    .charts-section,
    .recommendations-section {
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
    .user-role,
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

/* AI助手胶囊 - 紫色渐变 */
.ai-capsule .capsule-content {
    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
    box-shadow: 0 8px 24px rgba(102, 126, 234, 0.4);
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
</style>
