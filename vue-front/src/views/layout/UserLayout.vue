<template>
    <div class="layout-wrapper">
        <el-container class="full-height">
            <!-- 侧边栏 -->
            <el-aside v-if="!route.meta.hideLayout" width="70px" class="aside-menu">
                <div class="logo-box">
                    <span class="logo-icon">📚</span>
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

                    <router-link to="/user/dashboard" class="menu-item" :class="{ active: activeMenu === '/user/dashboard' }">
                        <el-tooltip content="备考看板" placement="right" :show-after="500">
                            <div class="menu-icon">
                                <img :src="dashboardIcon" class="menu-icon-svg" />
                            </div>
                        </el-tooltip>
                        <span class="menu-text">备考看板</span>
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
                                <div class="dropdown-header">
                                    <el-avatar :size="42" :src="userAvatar" />
                                    <div class="header-user-info">
                                        <div class="header-user-name">{{ userName }}</div>
                                        <div class="header-user-role">{{ userRole === 'ADMIN' ? '管理员' : '学生' }}</div>
                                    </div>
                                </div>
                                <el-dropdown-item divided @click="router.push('/user/profile')">
                                    <el-icon><UserFilled /></el-icon>
                                    <span>个人主页</span>
                                </el-dropdown-item>
                                <el-dropdown-item @click="router.push('/user/profile?tab=settings')">
                                    <el-icon><Setting /></el-icon>
                                    <span>账号设置</span>
                                </el-dropdown-item>
                                <el-dropdown-item v-if="userRole === 'ADMIN'" @click="router.push('/admin/home')">
                                    <el-icon><Management /></el-icon>
                                    <span>进入后台管理系统</span>
                                </el-dropdown-item>
                                <el-dropdown-item divided @click="handleLogout" class="logout-item">
                                    <el-icon><SwitchButton /></el-icon>
                                    <span>退出登录</span>
                                </el-dropdown-item>
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
            title="⚠️ 检测到未完成的考试"
            width="500px"
            :close-on-click-modal="false"
            :close-on-press-escape="false"
            :show-close="false"
            class="incomplete-exam-dialog"
        >
            <div class="dialog-content">
                <div class="exam-info-card">
                    <div class="exam-icon">📝</div>
                    <div class="exam-details">
                        <h3 class="exam-title">{{ incompleteExamInfo.paperTitle }}</h3>
                        <p class="exam-time">{{ incompleteExamInfo.startTime }}</p>
                    </div>
                </div>
                <div class="warning-text">
                    <p>
                        您有一场考试尚未完成，为了更好的评估您的各项能力，请
                        <strong class="highlight">认真作答</strong>完成考试。
                    </p>
                </div>
            </div>
            <template #footer>
                <div class="dialog-footer">
                    <el-button type="primary" @click="continueExam" class="continue-btn">回到考试</el-button>
                </div>
            </template>
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
import dashboardIcon from '@/assets/icons/dashboard.svg?url'
import singlePracticeIcon from '@/assets/icons/single-practice.svg?url'
import topicDrillIcon from '@/assets/icons/tree.svg?url'
import mockExamIcon from '@/assets/icons/mock-exam.svg?url'
import correctionNotebookIcon from '@/assets/icons/correction-notebook.svg?url'
import homeIcon from '@/assets/icons/home.svg?url'

const router = useRouter()
const route = useRoute();
const userStore = useUserStore() // 使用 store
const transitionStore = useTransitionStore()
const activeMenu = computed(() => router.path)

// 页面进入动画状态 - 从 store 获取
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
    return userStore.userInfo.avatar || 'https://cube.elemecdn.com/0/88/03b0d39583f48206768a7534e55bcpng.png'
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

// 组件挂载时检测未完成考试
onMounted(() => {
    // 延迟1秒检测，避免页面加载时立即弹窗影响体验
    setTimeout(() => {
        checkIncompleteExam()
    }, 1000)

    // 检测是否从 Home 页面进入，触发偏移补偿动画
    // 通过检查 referrer 或路由状态判断
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
    background: rgba(255, 255, 255, 0.85);
    backdrop-filter: blur(15px);
    height: 100vh;
    border-right: 1px solid rgba(219, 234, 254, 0.5) !important;
    box-shadow: 2px 0 20px rgba(59, 130, 246, 0.08);
    overflow: hidden;
    z-index: 100;
    display: flex;
    flex-direction: column;
}

/* Logo区域 */
.logo-box {
    height: 70px;
    display: flex;
    align-items: center;
    justify-content: center;
    background: linear-gradient(135deg, rgba(59, 130, 246, 0.08) 0%, rgba(96, 165, 250, 0.05) 100%);
    border-bottom: 1px solid rgba(219, 234, 254, 0.6);
    position: relative;
}

.logo-box::after {
    content: '';
    position: absolute;
    bottom: 0;
    left: 0;
    right: 0;
    height: 2px;
    background: linear-gradient(90deg, transparent 0%, #3b82f6 50%, transparent 100%);
    opacity: 0.6;
}

.logo-icon {
    font-size: 32px;
    display: flex;
    align-items: center;
    justify-content: center;
}

/* 菜单区域 */
.custom-menu {
    flex: 1;
    overflow-y: auto;
    overflow-x: hidden;
    padding: 12px 8px;
    display: flex;
    flex-direction: column;
    gap: 4px;
}

.custom-menu::-webkit-scrollbar {
    width: 4px;
}

.custom-menu::-webkit-scrollbar-thumb {
    background: rgba(59, 130, 246, 0.2);
    border-radius: 2px;
}

.custom-menu::-webkit-scrollbar-track {
    background: transparent;
}

/* 菜单项 */
.menu-item {
    display: flex;
    align-items: center;
    justify-content: center;
    height: 50px;
    padding: 0;
    text-decoration: none;
    border-radius: 10px;
    color: #64748b;
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
    background: linear-gradient(to bottom, #3b82f6, #60a5fa);
    transform: scaleY(0);
    transition: transform 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

.menu-item:hover {
    background: rgba(59, 130, 246, 0.1);
    color: #3b82f6;
}

.menu-item:hover::before {
    transform: scaleY(1);
}

.menu-item.active {
    background: linear-gradient(135deg, rgba(59, 130, 246, 0.15) 0%, rgba(96, 165, 250, 0.12) 100%);
    color: #3b82f6;
    font-weight: 600;
    box-shadow: 0 2px 8px rgba(59, 130, 246, 0.15);
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
    width: 24px;
    height: 24px;
    filter: invert(41%) sepia(5%) saturate(542%) hue-rotate(182deg) brightness(91%) contrast(87%);
    transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

.menu-item:hover .menu-icon-svg,
.menu-item.active .menu-icon-svg {
    filter: invert(53%) sepia(96%) saturate(3089%) hue-rotate(196deg) brightness(100%) contrast(101%);
    transform: scale(1.08);
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
    background: linear-gradient(90deg, transparent 0%, rgba(59, 130, 246, 0.2) 50%, transparent 100%);
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
    padding: 12px;
    background: linear-gradient(135deg, rgba(59, 130, 246, 0.06) 0%, rgba(96, 165, 250, 0.04) 100%);
    border-top: 1px solid rgba(219, 234, 254, 0.6);
    flex-shrink: 0;
    position: relative;
}

.user-center-section::before {
    content: '';
    position: absolute;
    top: 0;
    left: 0;
    right: 0;
    height: 1px;
    background: linear-gradient(90deg, transparent 0%, #3b82f6 50%, transparent 100%);
    opacity: 0.4;
}

.user-profile-card {
    display: flex;
    align-items: center;
    justify-content: center;
    padding: 8px;
    background: rgba(255, 255, 255, 0.75);
    backdrop-filter: blur(10px);
    border: 1px solid rgba(219, 234, 254, 0.6);
    border-radius: 14px;
    cursor: pointer;
    transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
    box-shadow: 0 2px 10px rgba(59, 130, 246, 0.08);
    position: relative;
    overflow: visible;
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

.online-indicator {
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
}

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
    border-radius: 14px;
    box-shadow: 0 10px 40px rgba(59, 130, 246, 0.15);
    overflow: hidden;
    min-width: 240px;
}

:deep(.user-dropdown-menu .el-dropdown-menu__item) {
    padding: 11px 18px;
    font-size: 14px;
    color: #4b5563;
    transition: all 0.2s;
}

:deep(.user-dropdown-menu .el-dropdown-menu__item:hover) {
    background: rgba(59, 130, 246, 0.1);
    color: #3b82f6;
}

:deep(.user-dropdown-menu .el-dropdown-menu__item .el-icon) {
    margin-right: 10px;
    font-size: 17px;
    color: inherit;
}

.dropdown-header {
    padding: 18px;
    background: linear-gradient(135deg, rgba(59, 130, 246, 0.1) 0%, rgba(96, 165, 250, 0.08) 100%);
    border-bottom: 1px solid rgba(219, 234, 254, 0.6);
    display: flex;
    align-items: center;
    gap: 12px;
}

.header-user-info {
    flex: 1;
    min-width: 0;
}

.header-user-name {
    font-size: 16px;
    font-weight: 600;
    color: #1f2937;
    margin-bottom: 3px;
    white-space: nowrap;
    overflow: hidden;
    text-overflow: ellipsis;
}

.header-user-role {
    font-size: 12px;
    color: #3b82f6;
    font-weight: 500;
}

.logout-item {
    color: #ef4444 !important;
}

.logout-item:hover {
    background: rgba(239, 68, 68, 0.1) !important;
    color: #dc2626 !important;
}

/* 内容区 */
.content-main {
    background: transparent;
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
.incomplete-exam-dialog :deep(.el-dialog__header) {
    background: linear-gradient(135deg, #fff7ed 0%, #ffedd5 100%);
    border-bottom: 2px solid #f97316;
    padding: 20px;
}

.incomplete-exam-dialog :deep(.el-dialog__title) {
    font-size: 18px;
    font-weight: 600;
    color: #c2410c;
}

.dialog-content {
    padding: 20px;
}

.exam-info-card {
    display: flex;
    align-items: center;
    gap: 16px;
    padding: 20px;
    background: linear-gradient(135deg, #eff6ff 0%, #dbeafe 100%);
    border-radius: 12px;
    border: 2px solid #3b82f6;
    margin-bottom: 20px;
}

.exam-icon {
    font-size: 48px;
    line-height: 1;
}

.exam-details {
    flex: 1;
}

.exam-title {
    margin: 0 0 8px 0;
    font-size: 18px;
    font-weight: 600;
    color: #1e40af;
}

.exam-time {
    margin: 0;
    font-size: 14px;
    color: #64748b;
}

.warning-text {
    padding: 16px;
    background: #fef3c7;
    border-radius: 6px;
}

.warning-text p {
    margin: 0;
    font-size: 15px;
    color: #92400e;
    line-height: 1.6;
}

.highlight {
    color: #d32f2f;
    font-weight: bold;
    font-size: 1.1em;
    text-shadow: 0 1px 1px rgba(0, 0, 0, 0.1);
}

.dialog-footer {
    display: flex;
    justify-content: flex-end;
    gap: 12px;
}

.continue-btn {
    background: linear-gradient(135deg, #2563eb 0%, #1d4ed8 100%);
    border: none;
    padding: 12px 24px;
    font-weight: 600;
    width: 100%;
    height: 50px;
}

.continue-btn:hover {
    background: linear-gradient(135deg, #1d4ed8 0%, #1e40af 100%);
    transform: translateY(-1px);
    box-shadow: 0 4px 12px rgba(37, 99, 235, 0.3);
}
</style>
