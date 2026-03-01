<template>
    <div class="layout-wrapper">
        <el-container class="full-height">
            <!-- 侧边栏 -->
            <el-aside v-if="!route.meta.hideLayout" width="70px" class="aside-menu">
                <div class="logo-box">
                    <div class="logo-icon-wrapper">
                        <span class="logo-letter">Y</span>
                    </div>
                </div>

                <div class="custom-menu">
                    <router-link to="/user/home" class="menu-item" :class="{ active: activeMenu === '/user/home' }">
                        <el-tooltip content="主页" placement="right" :show-after="500">
                            <div class="menu-icon">
                                <img :src="homeIcon" class="menu-icon-svg" />
                            </div>
                        </el-tooltip>
                        <span class="menu-text">主页</span>
                    </router-link>

                    <router-link to="/user/subject" class="menu-item" :class="{ active: activeMenu === '/user/subject' }">
                        <el-tooltip content="开始学习" placement="right" :show-after="500">
                            <div class="menu-icon">
                                <img :src="singlePracticeIcon" class="menu-icon-svg" />
                            </div>
                        </el-tooltip>
                        <span class="menu-text">开始学习</span>
                    </router-link>

                    <router-link to="/user/topic-drill" class="menu-item" :class="{ active: activeMenu === '/user/topic-drill' }">
                        <el-tooltip content="知识体系树" placement="right" :show-after="500">
                            <div class="menu-icon">
                                <img :src="topicDrillIcon" class="menu-icon-svg" />
                            </div>
                        </el-tooltip>
                        <span class="menu-text">知识体系树</span>
                    </router-link>

                    <router-link to="/user/paper-list" class="menu-item" :class="{ active: activeMenu === '/user/paper-list' }">
                        <el-tooltip content="套卷模考" placement="right" :show-after="500">
                            <div class="menu-icon">
                                <img :src="mockExamIcon" class="menu-icon-svg" />
                            </div>
                        </el-tooltip>
                        <span class="menu-text">套卷模考</span>
                    </router-link>

                    <router-link to="/user/correction-notebook" class="menu-item" :class="{ active: activeMenu === '/user/correction-notebook' }">
                        <el-tooltip content="错题本" placement="right" :show-after="500">
                            <div class="menu-icon">
                                <img :src="correctionNotebookIcon" class="menu-icon-svg" />
                            </div>
                        </el-tooltip>
                        <span class="menu-text">错题本</span>
                    </router-link>

                    <div class="menu-divider" v-if="userRole === 'admin'"></div>

                    <router-link v-if="userRole === 'admin'" to="/admin/home" class="menu-item admin-entry">
                        <el-tooltip content="进入管理后台" placement="right" :show-after="500">
                            <div class="menu-icon">
                                <el-icon><Tools /></el-icon>
                            </div>
                        </el-tooltip>
                        <span class="menu-text">进入管理后台</span>
                    </router-link>
                </div>

                <!-- 用户中心入口 - 侧边栏底部 -->
                <div class="user-center-section">
                    <el-dropdown trigger="click" placement="right" @command="handleUserCommand">
                        <div class="user-profile-card">
                            <div class="user-avatar-wrapper">
                                <el-avatar :size="36" :src="userAvatar" class="user-avatar" />
                                <div class="online-indicator"></div>
                            </div>
                            <span class="menu-text">个人中心</span>
                        </div>
                        <template #dropdown>
                            <el-dropdown-menu class="user-dropdown-menu">
                                <!-- 用户信息头部 -->
                                <div class="dropdown-header">
                                    <div class="header-avatar-wrapper">
                                        <el-avatar :size="48" :src="userAvatar" class="header-avatar" />
                                    </div>
                                    <div class="header-user-info">
                                        <div class="header-user-name">{{ userName }}</div>
                                        <div class="header-user-role-badge">
                                            <svg class="role-icon" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                                                <path d="M12 2L15.09 8.26L22 9.27L17 14.14L18.18 21.02L12 17.77L5.82 21.02L7 14.14L2 9.27L8.91 8.26L12 2Z" fill="currentColor"/>
                                            </svg>
                                            <span>{{ userRole === 'ADMIN' ? '管理员' : '学生' }}</span>
                                        </div>
                                    </div>
                                </div>

                                <!-- 菜单项 -->
                                <div class="dropdown-menu-items">
                                    <div class="menu-item" @click="router.push('/user/profile')">
                                        <div class="menu-item-icon personal-icon">
                                            <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                                                <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                                                <circle cx="12" cy="7" r="4" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                                            </svg>
                                        </div>
                                        <div class="menu-item-content">
                                            <span class="menu-item-title">个人主页</span>
                                        </div>
                                    </div>

                                    <div class="menu-item" @click="router.push('/user/messages')">
                                        <div class="menu-item-icon message-icon">
                                            <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                                                <path d="M3 8l7.89 5.26a2 2 0 002.22 0L21 8M5 19h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v10a2 2 0 002 2z" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                                            </svg>
                                        </div>
                                        <div class="menu-item-content">
                                            <span class="menu-item-title">消息</span>
                                        </div>
                                        <el-badge :value="5" :max="99" class="menu-item-badge" />
                                    </div>

                                    <div class="menu-divider"></div>

                                    <div v-if="userRole === 'ADMIN'" class="menu-item" @click="router.push('/admin/home')">
                                        <div class="menu-item-icon admin-icon">
                                            <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                                                <rect x="3" y="3" width="7" height="7" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                                                <rect x="14" y="3" width="7" height="7" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                                                <rect x="14" y="14" width="7" height="7" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                                                <rect x="3" y="14" width="7" height="7" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                                            </svg>
                                        </div>
                                        <div class="menu-item-content">
                                            <span class="menu-item-title">后台管理</span>
                                        </div>
                                    </div>

                                    <div class="menu-item logout-menu-item" @click="handleLogout">
                                        <div class="menu-item-icon logout-icon">
                                            <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                                                <path d="M9 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h4" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                                                <polyline points="16 17 21 12 16 7" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                                                <line x1="21" y1="12" x2="9" y2="12" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                                            </svg>
                                        </div>
                                        <div class="menu-item-content">
                                            <span class="menu-item-title">退出登录</span>
                                        </div>
                                    </div>
                                </div>
                            </el-dropdown-menu>
                        </template>
                    </el-dropdown>
                </div>
            </el-aside>

            <el-container direction="vertical" class="main-container">
                <el-main class="content-main" :class="{ 'no-padding': route.meta.hideLayout, 'from-home-enter': isTransitioningFromHome }">
                    <router-view v-slot="{ Component }">
                        <transition name="fade-transform" mode="out-in">
                            <component :is="Component" />
                        </transition>
                    </router-view>
                </el-main>
            </el-container>
        </el-container>

        <!-- 未完成考试强制弹窗 -->
        <el-dialog
            v-model="showIncompleteExamDialog"
            width="480px"
            :close-on-click-modal="false"
            :close-on-press-escape="false"
            :show-close="false"
            class="incomplete-exam-dialog"
            align-center
        >
            <div class="dialog-wrapper">
                <!-- 顶部图标区 -->
                <div class="dialog-top">
                    <div class="top-icon-container">
                        <svg class="top-icon" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                            <circle cx="12" cy="12" r="10" fill="#f59e0b"/>
                            <path d="M12 8v4m0 4h.01" stroke="white" stroke-width="2.5" stroke-linecap="round"/>
                        </svg>
                    </div>
                    <h2 class="dialog-title">发现未完成的考试</h2>
                    <p class="dialog-desc">检测到您有正在进行的考试</p>
                </div>

                <!-- 内容区 -->
                <div class="dialog-body">
                    <!-- 试卷信息卡片 -->
                    <div class="exam-card">
                        <div class="exam-card-header">
                            <svg class="exam-icon" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                                <path d="M9 12h6m-6 4h6m2 5H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                            </svg>
                            <span class="exam-label">试卷名称</span>
                        </div>
                        <div class="exam-card-content">
                            <h3 class="exam-name">{{ incompleteExamInfo.paperTitle }}</h3>
                            <div class="exam-meta">
                                <div class="meta-item">
                                    <svg class="meta-icon" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                                        <circle cx="12" cy="12" r="9" stroke="currentColor" stroke-width="2"/>
                                        <path d="M12 6v6l4 2" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
                                    </svg>
                                    <span>{{ incompleteExamInfo.startTime }}</span>
                                </div>
                            </div>
                        </div>
                    </div>

                    <!-- 提示信息 -->
                    <div class="tips-box">
                        <svg class="tips-icon" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                            <circle cx="12" cy="12" r="10" stroke="#f59e0b" stroke-width="2"/>
                            <path d="M12 8v4m0 4h.01" stroke="#f59e0b" stroke-width="2.5" stroke-linecap="round"/>
                        </svg>
                        <p class="tips-text">继续完成考试以获得<span class="highlight">更准确</span>的能力评估！</p>
                    </div>
                </div>

                <!-- 底部按钮区 -->
                <div class="dialog-footer">
                    <button @click="abandonExam" class="btn-abandon">
                        放弃考试
                    </button>
                    <button @click="continueExam" class="btn-continue">
                        <svg class="btn-icon" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                            <path d="M5 12h14m-7-7 7 7-7 7" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                        </svg>
                        继续考试
                    </button>
                </div>
            </div>
        </el-dialog>
    </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessageBox, ElMessage } from 'element-plus'
import { useUserStore } from '../../stores/user'
import { getIncompleteSessions } from '@/api/examSession'
import { getPaperDetail } from '@/api/paper'
import { useTransitionStore } from '@/stores/transition'

// 导入自定义图标
import singlePracticeIcon from '@/assets/icons/single-practice.svg?url'
import topicDrillIcon from '@/assets/icons/tree.svg?url'
import mockExamIcon from '@/assets/icons/mock-exam.svg?url'
import correctionNotebookIcon from '@/assets/icons/correction-notebook.svg?url'
import homeIcon from '@/assets/icons/home.svg?url'

const router = useRouter()
const route = useRoute();
const userStore = useUserStore() // 使用 store
const transitionStore = useTransitionStore()
const activeMenu = computed(() => route.path)

// 从 store 获取页面进入动画状态
const isTransitioningFromHome = computed(() => transitionStore.isEnteringLayout)

// 监听 store 中的 userInfo 变化
const userName = computed(() => {
    const user = userStore.userInfo
    if (user && (user.nickname || user.username)) {
        return user.nickname || user.username
    }
    return '研友'
})

const userAvatar = computed(() => {
    return userStore.userInfo.avatar
})

const userRole = computed(() => {
    return userStore.userInfo.role || localStorage.getItem('role') || 'USER'
})

// 未完成考试弹窗相关
const showIncompleteExamDialog = ref(false)
const incompleteExamInfo = ref({
    paperTitle: '',
    startTime: '',
    sessionId: '',
    paperId: '',
    userId: ''
})

const handleLogout = () => {
    ElMessageBox.confirm('确定要退出登录吗？', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
    }).then(() => {
        // 清除所有用户信息和 Token
        localStorage.removeItem('user')
        localStorage.removeItem('userInfo')
        localStorage.removeItem('role')
        localStorage.removeItem('token')

        ElMessage.success('已安全退出')

        // 跳转到登录页
        router.push('/login')
    }).catch(() => { })
}

const handleUserCommand = (command) => {
    // 处理下拉菜单命令
    console.log('User command:', command)
}

// 检测未完成考试
const checkIncompleteExam = async () => {
    try {
        const userId = localStorage.getItem('userId') || userStore.userInfo?.id
        if (!userId) return

        // 检查当前是否已经在考试页面，避免重复弹窗
        if (route.path === '/user/mock-exam') {
            return
        }

        const res = await getIncompleteSessions(userId)
        if (res.code === 200 && res.data && res.data.length > 0) {
            // 有未完成的考试
            const session = res.data[0] // 取最近的一个

            // 获取试卷详情
            const paperRes = await getPaperDetail(session.paperId)
            if (paperRes.code === 200 && paperRes.data) {
                incompleteExamInfo.value = {
                    paperTitle: paperRes.data.title,
                    startTime: formatTime(session.createTime),
                    sessionId: session.id,
                    paperId: session.paperId,
                    userId: userId
                }

                // 显示强制弹窗
                showIncompleteExamDialog.value = true
            }
        }
    } catch (error) {
        console.error('检测未完成考试失败:', error)
    }
}

// 格式化时间
const formatTime = (timeStr) => {
    if (!timeStr) return ''
    const date = new Date(timeStr)
    const now = new Date()
    const diff = Math.floor((now - date) / 1000 / 60) // 分钟差

    if (diff < 1) return '刚刚开始'
    if (diff < 60) return `${diff}分钟前开始`
    const hours = Math.floor(diff / 60)
    if (hours < 24) return `${hours}小时前开始`
    const days = Math.floor(hours / 24)
    return `${days}天前开始`
}

// 继续考试
const continueExam = () => {
    showIncompleteExamDialog.value = false
    router.replace({
        path: '/user/mock-exam',
        query: {
            paperId: incompleteExamInfo.value.paperId,
            userId: incompleteExamInfo.value.userId
        }
    })
}

// 放弃考试
const abandonExam = () => {
    ElMessageBox.confirm(
        '确定要放弃当前考试吗？放弃后当前答题进度将不会保存，建议您继续完成考试。',
        '警告',
        {
            confirmButtonText: '仍要放弃',
            cancelButtonText: '继续考试',
            type: 'warning',
            distinguishCancelAndClose: true
        }
    ).then(() => {
        showIncompleteExamDialog.value = false
        ElMessage.warning('您已放弃考试，如需继续请从套卷列表重新进入')
    }).catch(() => {
        // 用户选择继续考试
    })
}

onMounted(() => {
    setTimeout(() => {
        checkIncompleteExam()
    }, 1000)

    const fromHome = router.options.history.state?.back === '/user/home' ||
                      document.referrer.includes('/user/home')

    if (fromHome && !route.meta.hideLayout) {
        transitionStore.startEnteringLayout()
    }
})

</script>

<style scoped>
.layout-wrapper {
    height: 100vh;
    width: 100vw;
    overflow: hidden;
    background: linear-gradient(135deg, #f0f9ff 0%, #e6f7ff 100%);
}

.full-height {
    height: 100%;
}

.main-container {
    height: 100%;
    overflow: hidden;
    display: flex;
    flex-direction: column;
}

/* 侧边栏 */
.aside-menu {
    background: linear-gradient(180deg, #ffffff 0%, #f7f9fc 100%);
    backdrop-filter: blur(15px);
    height: 100vh;
    border-right: 1px solid rgba(64, 158, 255, 0.1) !important;
    box-shadow: 3px 0 16px rgba(64, 158, 255, 0.08);
    overflow: hidden;
    z-index: 100;
    display: flex;
    flex-direction: column;
    border: none !important;
    padding: 0 !important;
}

/* Logo区域 */
.logo-box {
    height: 70px;
    width: 100%;
    display: flex;
    align-items: center;
    justify-content: center;
    background: linear-gradient(135deg, #409EFF 0%, #0066CC 100%);
    position: relative;
    overflow: hidden;
    border: none !important;
    margin: 0 !important;
    flex-shrink: 0;
}

.logo-box::after {
    display: none;
}

.logo-icon-wrapper {
    width: 40px;
    height: 40px;
    display: flex;
    align-items: center;
    justify-content: center;
}

.logo-letter {
    font-size: 1.5rem;
    font-weight: 600;
    color: white;
    line-height: 1;
    animation: logoFloat 3s ease-in-out infinite;
}

@keyframes logoFloat {
    0%, 100% {
        transform: translateY(0);
    }
    50% {
        transform: translateY(-3px);
    }
}

/* 菜单区域 */
.custom-menu {
    flex: 1;
    overflow-y: auto;
    overflow-x: hidden;
    padding: 16px 8px 12px;
    display: flex;
    flex-direction: column;
    gap: 1px;
}

.custom-menu::-webkit-scrollbar {
    width: 0;
    display: none;
}

.custom-menu {
    -ms-overflow-style: none;
    scrollbar-width: none;
}

/* 菜单项 */
.menu-item {
    display: flex;
    align-items: center;
    justify-content: center;
    height: 25px;
    padding: 0;
    text-decoration: none;
    border-radius: 12px;
    color: #5a5e66;
    transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
    position: relative;
    overflow: hidden;
}

.menu-item::before {
    content: '';
    position: absolute;
    left: 0;
    top: 0;
    bottom: 0;
    width: 3px;
    background: linear-gradient(180deg, #409EFF 0%, #0066CC 100%);
    border-radius: 0 12px 12px 0;
    transform: scaleY(0);
    transition: transform 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

.menu-item:hover {
    background: linear-gradient(90deg, rgba(64, 158, 255, 0.08) 0%, rgba(64, 158, 255, 0.04) 100%);
    color: #409EFF;
    transform: translateX(2px);
}

.menu-item:hover::before {
    transform: scaleY(0.5);
}

.menu-item.active {
    background: linear-gradient(90deg, rgba(64, 158, 255, 0.15) 0%, rgba(0, 102, 204, 0.08) 100%);
    color: #409EFF;
    font-weight: 600;
    box-shadow: 0 4px 12px rgba(64, 158, 255, 0.15);
}

.menu-item.active::before {
    transform: scaleY(1);
}

/* 菜单图标 */
.menu-icon {
    display: flex;
    align-items: center;
    justify-content: center;
    width: 100%;
    height: 100%;
}

.menu-icon-svg {
    width: 22px;
    height: 22px;
    filter: invert(45%) sepia(8%) saturate(1597%) hue-rotate(179deg) brightness(95%) contrast(85%);
    transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

.menu-item:hover .menu-icon-svg {
    filter: invert(42%) sepia(98%) saturate(1814%) hue-rotate(189deg) brightness(99%) contrast(91%);
    transform: scale(1.15);
}

.menu-item.active .menu-icon-svg {
    filter: invert(42%) sepia(98%) saturate(1814%) hue-rotate(189deg) brightness(99%) contrast(91%);
    transform: scale(1.15);
}

/* 菜单文字 - 默认隐藏 */
.menu-text {
    position: absolute;
    left: 100%;
    top: 50%;
    transform: translateY(-50%);
    margin-left: 10px;
    padding: 6px 12px;
    background: rgba(59, 130, 246, 0.95);
    color: white;
    font-size: 13px;
    font-weight: 500;
    border-radius: 6px;
    white-space: nowrap;
    opacity: 0;
    pointer-events: none;
    transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
    box-shadow: 0 4px 12px rgba(59, 130, 246, 0.3);
    z-index: 1000;
}

.menu-text::before {
    content: '';
    position: absolute;
    left: -6px;
    top: 50%;
    transform: translateY(-50%);
    border: 6px solid transparent;
    border-right-color: rgba(59, 130, 246, 0.95);
    border-left: none;
}

.menu-item:hover .menu-text {
    opacity: 1;
    transform: translateY(-50%) translateX(5px);
}

/* El-icon 样式 */
.menu-item .el-icon {
    font-size: 24px;
    transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

.menu-item:hover .el-icon,
.menu-item.active .el-icon {
    transform: scale(1.08);
}

/* 分割线 */
.menu-divider {
    height: 1px;
    background: linear-gradient(90deg, transparent 0%, rgba(64, 158, 255, 0.15) 50%, transparent 100%);
    margin: 16px 12px;
}

/* 管理员入口 */
.admin-entry {
    color: #f59e0b !important;
    border: 1px dashed rgba(245, 158, 11, 0.4);
    background: rgba(245, 158, 11, 0.05) !important;
}

.admin-entry:hover {
    background: rgba(245, 158, 11, 0.12) !important;
    border-color: #f59e0b;
    box-shadow: 0 2px 8px rgba(245, 158, 11, 0.15);
}

/* 用户中心区域 - 底部固定 */
.user-center-section {
    margin-top: auto;
    padding: 5px 0;
    flex-shrink: 0;
    position: relative;
    display: flex;
    align-items: center;
    justify-content: center;
}

.user-profile-card {
    display: flex;
    align-items: center;
    justify-content: center;
    padding: 0px 0;
    background: rgba(255, 255, 255, 0.75);
    backdrop-filter: blur(10px);
    border: 1px solid rgba(219, 234, 254, 0.6);
    border-radius: 14px;
    cursor: pointer;
    transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
    box-shadow: 0 2px 10px rgba(59, 130, 246, 0.08);
    position: relative;
    overflow: visible;
    width: 54px;
    height: 54px;
}

.user-profile-card::before {
    content: '';
    position: absolute;
    top: 0;
    left: 0;
    right: 0;
    bottom: 0;
    background: linear-gradient(135deg, rgba(59, 130, 246, 0.08) 0%, rgba(96, 165, 250, 0.05) 100%);
    opacity: 0;
    transition: opacity 0.3s;
    border-radius: 14px;
}

.user-profile-card:hover {
    transform: translateY(-2px);
    box-shadow: 0 6px 20px rgba(59, 130, 246, 0.18);
    border-color: rgba(59, 130, 246, 0.4);
}

.user-profile-card:hover::before {
    opacity: 1;
}

.user-avatar-wrapper {
    position: relative;
    flex-shrink: 0;
    z-index: 1;
    display: flex;
    align-items: center;
    justify-content: center;
}

.user-avatar {
    border: 2.5px solid rgba(59, 130, 246, 0.25);
    transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

.user-profile-card:hover .user-avatar {
    border-color: #3b82f6;
    box-shadow: 0 0 16px rgba(59, 130, 246, 0.4);
    transform: scale(1.05);
}

/* .online-indicator {
    position: absolute;
    bottom: 0;
    right: 0;
    width: 9px;
    height: 9px;
    background: #10b981;
    border: 2px solid #fff;
    border-radius: 50%;
    box-shadow: 0 0 0 3px rgba(16, 185, 129, 0.15);
    animation: pulse 2s cubic-bezier(0.4, 0, 0.6, 1) infinite;
} */

@keyframes pulse {
    0%, 100% {
        box-shadow: 0 0 0 0 rgba(16, 185, 129, 0.4);
    }
    50% {
        box-shadow: 0 0 0 6px rgba(16, 185, 129, 0);
    }
}

/* 用户下拉菜单样式 */
.user-dropdown-menu {
    padding: 0;
    border: none;
    border-radius: 16px;
    box-shadow: 0 12px 48px rgba(0, 0, 0, 0.12);
    overflow: hidden;
    min-width: 260px;
    background: white;
}

/* 下拉菜单头部 */
.dropdown-header {
    padding: 18px 20px;
    background: linear-gradient(135deg, #f8fafc 0%, #f1f5f9 100%);
    border-bottom: 1px solid #e2e8f0;
}

.header-avatar-wrapper {
    position: relative;
    display: flex;
    justify-content: center;
    margin-bottom: 12px;
}

.header-avatar {
    border: 3px solid white;
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
}

.header-user-info {
    text-align: center;
}

.header-user-name {
    font-size: 15px;
    font-weight: 600;
    color: #1e293b;
    margin-bottom: 6px;
}

.header-user-role-badge {
    display: inline-flex;
    align-items: center;
    gap: 4px;
    padding: 3px 10px;
    background: #dbeafe;
    border-radius: 12px;
    font-size: 11px;
    color: #1e40af;
    font-weight: 600;
}

.role-icon {
    width: 11px;
    height: 11px;
    color: #3b82f6;
}

/* 菜单项容器 */
.dropdown-menu-items {
    padding: 12px 16px;
}

.menu-item-group {
    display: flex;
    flex-direction: column;
}

.menu-divider {
    height: 1px;
    background: #e2e8f0;
    margin: 8px 0;
}

/* 菜单项 */
.menu-item {
    display: flex;
    align-items: center;
    gap: 12px;
    padding: 10px 12px;
    border-radius: 8px;
    cursor: pointer;
    transition: all 0.2s ease;
    position: relative;
    margin-bottom: 4px;
}

.menu-item:last-child {
    margin-bottom: 0;
}

.menu-item:hover {
    background: #f1f5f9;
}

/* 图标容器 */
.menu-item-icon {
    width: 36px;
    height: 36px;
    display: flex;
    align-items: center;
    justify-content: center;
    border-radius: 8px;
    flex-shrink: 0;
    padding: 0;
}

/* 统一容器大小 */
.user-dropdown-menu .menu-item-icon {
    width: 36px !important;
    height: 36px !important;
    display: flex !important;
    align-items: center;
    justify-content: center;
    border-radius: 10px;
    /* 稍微圆润一点更协调 */
    flex-shrink: 0;
}

/* 统一内部图标的基础大小 */
.user-dropdown-menu .menu-item-icon svg,
.user-dropdown-menu .menu-item-icon img {
    width: 18px !important;
    /* 统一减小到 18px，给背景留白 */
    height: 18px !important;
    display: block;
    transition: transform 0.2s;
}

/* 个人主页图标 */
.personal-icon {
    background: linear-gradient(135deg, #3b82f6 0%, #2563eb 100%);
    color: white;
}

.personal-icon svg {
    color: white;
}

/* 消息图标 */
.message-icon {
    background: linear-gradient(135deg, #8b5cf6 0%, #7c3aed 100%);
    color: white;
}

.message-icon svg {
    color: white;
}

/* 后台管理图标 */
.admin-icon {
    background: linear-gradient(135deg, #f59e0b 0%, #d97706 100%);
    color: white;
}

.admin-icon svg {
    color: white;
}

/* 退出登录图标 */
.logout-icon {
    background: linear-gradient(135deg, #ef4444 0%, #dc2626 100%);
    color: white;
}

.logout-icon svg {
    color: white;
}

/* 悬停效果 */
.menu-item:hover .personal-icon {
    background: linear-gradient(135deg, #2563eb 0%, #1d4ed8 100%);
}

.menu-item:hover .message-icon {
    background: linear-gradient(135deg, #7c3aed 0%, #6d28d9 100%);
}

.menu-item:hover .admin-icon {
    background: linear-gradient(135deg, #d97706 0%, #b45309 100%);
}

.menu-item:hover .logout-icon {
    background: linear-gradient(135deg, #dc2626 0%, #b91c1c 100%);
}

.menu-item-content {
    flex: 1;
    min-width: 0;
}

.menu-item-title {
    font-size: 14px;
    font-weight: 500;
    color: #334155;
    display: block;
}

.menu-item-desc {
    font-size: 12px;
    color: #94a3b8;
    display: none;
}

.menu-item-badge {
    margin-left: auto;
}

:deep(.menu-item-badge .el-badge__content) {
    background: #ef4444;
    border: 2px solid #fff;
    font-weight: 600;
    font-size: 11px;
    height: 18px;
    line-height: 14px;
    padding: 0 5px;
}

.admin-menu-item:hover .menu-item-title {
    color: #f59e0b;
}

.logout-menu-item:hover {
    background: #fef2f2;
}

.logout-menu-item:hover .menu-item-title {
    color: #ef4444;
}

/* 内容区 */
.content-main {
    /* background: transparent; */
    background: linear-gradient(135deg, #cfecff 0%, #00aeff 100%);
    padding: 0;
    overflow-y: auto;
    overflow-x: hidden;
    flex: 1;
    transition: transform 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

.content-main::-webkit-scrollbar {
    width: 8px;
}

.content-main::-webkit-scrollbar-track {
    background: rgba(255, 255, 255, 0.1);
    border-radius: 4px;
}

.content-main::-webkit-scrollbar-thumb {
    background: rgba(255, 255, 255, 0.3);
    border-radius: 4px;
}

.content-main::-webkit-scrollbar-thumb:hover {
    background: rgba(255, 255, 255, 0.5);
}

.content-main.from-home-enter {
    transform: translateX(-70px);
}

.no-padding {
    padding: 0 !important;
}

/* 路由切换动画 */
.fade-transform-enter-active {
    transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
}

.fade-transform-leave-active {
    transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

.fade-transform-enter-from {
    opacity: 0;
    transform: scale(0.98) translateY(10px);
}

.fade-transform-leave-to {
    opacity: 0;
    transform: scale(1.02) translateY(-10px);
}

.fade-enter-active,
.fade-leave-active {
    transition: opacity 0.3s ease;
}

.fade-enter-from,
.fade-leave-to {
    opacity: 0;
}

/* 未完成考试弹窗样式 */
.incomplete-exam-dialog {
    border-radius: 20px;
    overflow: visible;
}

.incomplete-exam-dialog :deep(.el-dialog__header) {
    display: none;
}

.incomplete-exam-dialog :deep(.el-dialog__body) {
    padding: 0;
    background: transparent;
}

.incomplete-exam-dialog :deep(.el-dialog__footer) {
    display: none;
}

.incomplete-exam-dialog :deep(.el-dialog) {
    border-radius: 20px;
    box-shadow: 0 20px 60px rgba(0, 0, 0, 0.15);
}

/* 弹窗包装器 */
.dialog-wrapper {
    background: white;
    border-radius: 20px;
    overflow: hidden;
}

/* 顶部区域 */
.dialog-top {
    background: linear-gradient(135deg, #fef9e7 0%, #fef3c7 100%);
    padding: 32px 28px 28px;
    text-align: center;
    position: relative;
}

.dialog-top::after {
    content: '';
    position: absolute;
    bottom: 0;
    left: 0;
    right: 0;
    height: 1px;
    background: linear-gradient(90deg, transparent, rgba(245, 158, 11, 0.3), transparent);
}

.top-icon-container {
    width: 72px;
    height: 72px;
    margin: 0 auto 20px;
    background: white;
    border-radius: 50%;
    display: flex;
    align-items: center;
    justify-content: center;
    box-shadow: 0 8px 24px rgba(245, 158, 11, 0.2);
    position: relative;
}

.top-icon-container::before {
    content: '';
    position: absolute;
    inset: -4px;
    border-radius: 50%;
    border: 2px dashed rgba(245, 158, 11, 0.3);
    animation: spin 20s linear infinite;
}

@keyframes spin {
    from { transform: rotate(0deg); }
    to { transform: rotate(360deg); }
}

.top-icon {
    width: 36px;
    height: 36px;
}

.dialog-title {
    margin: 0 0 8px 0;
    font-size: 22px;
    font-weight: 700;
    color: #78350f;
    letter-spacing: -0.3px;
}

.dialog-desc {
    margin: 0;
    font-size: 14px;
    color: #92400e;
    font-weight: 500;
}

/* 主体内容区 */
.dialog-body {
    padding: 28px;
}

/* 试卷信息卡片 */
.exam-card {
    background: linear-gradient(135deg, #f0f9ff 0%, #e0f2fe 100%);
    border-radius: 16px;
    overflow: hidden;
    margin-bottom: 16px;
    border: 1px solid rgba(59, 130, 246, 0.15);
}

.exam-card-header {
    display: flex;
    align-items: center;
    gap: 8px;
    padding: 12px 16px;
    background: rgba(59, 130, 246, 0.08);
    border-bottom: 1px solid rgba(59, 130, 246, 0.1);
}

.exam-icon {
    width: 18px;
    height: 18px;
    color: #3b82f6;
}

.exam-label {
    font-size: 12px;
    font-weight: 600;
    color: #1e40af;
    text-transform: uppercase;
    letter-spacing: 0.5px;
}

.exam-card-content {
    padding: 16px;
}

.exam-name {
    margin: 0 0 12px 0;
    font-size: 16px;
    font-weight: 700;
    color: #1e3a8a;
    line-height: 1.5;
}

.exam-meta {
    display: flex;
    align-items: center;
}

.meta-item {
    display: flex;
    align-items: center;
    gap: 6px;
    font-size: 13px;
    color: #64748b;
}

.meta-icon {
    width: 14px;
    height: 14px;
}

/* 提示框 */
.tips-box {
    display: flex;
    align-items: center;
    gap: 12px;
    padding: 14px 16px;
    background: linear-gradient(135deg, #fffbeb 0%, #fef3c7 100%);
    border-radius: 12px;
    border: 1px solid rgba(245, 158, 11, 0.2);
}

.tips-icon {
    width: 20px;
    height: 20px;
    flex-shrink: 0;
}

.tips-text {
    margin: 0;
    font-size: 13px;
    font-weight: 500;
    color: #92400e;
    line-height: 1.6;
}

.tips-text .highlight {
    color: #d97706;
    font-weight: 700;
    background: linear-gradient(135deg, rgba(245, 158, 11, 0.2) 0%, rgba(217, 119, 6, 0.15) 100%);
    padding: 2px 6px;
    border-radius: 4px;
    position: relative;
    display: inline-block;
}

/* 底部按钮区 */
.dialog-footer {
    display: flex;
    gap: 12px;
    padding: 0 28px 28px 28px;
}

.btn-abandon,
.btn-continue {
    flex: 1;
    height: 48px;
    border-radius: 12px;
    font-size: 15px;
    font-weight: 600;
    cursor: pointer;
    transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
    display: flex;
    align-items: center;
    justify-content: center;
    gap: 8px;
    border: none;
}

.btn-abandon {
    background: white;
    color: #64748b;
    border: 2px solid #e2e8f0;
}

.btn-abandon:hover {
    background: #f8fafc;
    border-color: #ef4444;
    color: #ef4444;
    transform: translateY(-2px);
    box-shadow: 0 4px 12px rgba(239, 68, 68, 0.15);
}

.btn-continue {
    background: linear-gradient(135deg, #3b82f6 0%, #2563eb 100%);
    color: white;
    box-shadow: 0 4px 14px rgba(59, 130, 246, 0.3);
}

.btn-continue:hover {
    background: linear-gradient(135deg, #2563eb 0%, #1d4ed8 100%);
    transform: translateY(-2px);
    box-shadow: 0 6px 20px rgba(59, 130, 246, 0.4);
}

.btn-continue .btn-icon {
    width: 18px;
    height: 18px;
}

/* 弹窗进入动画 */
.incomplete-exam-dialog :deep(.el-dialog) {
    animation: dialogSlideIn 0.4s cubic-bezier(0.34, 1.56, 0.64, 1);
}

@keyframes dialogSlideIn {
    from {
        opacity: 0;
        transform: translateY(20px) scale(0.95);
    }
    to {
        opacity: 1;
        transform: translateY(0) scale(1);
    }
}
</style>
