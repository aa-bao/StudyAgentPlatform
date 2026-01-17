<template>
    <div class="layout-wrapper">
        <el-container class="full-height">
            <el-aside width="240px" class="aside-menu">
                <div class="logo-box">
                    <div class="logo-icon-wrapper">
                        <svg class="logo-icon" viewBox="0 0 48 48" fill="none" xmlns="http://www.w3.org/2000/svg">
                            <rect x="4" y="4" width="40" height="40" rx="8" fill="white" fill-opacity="0.2"/>
                            <path d="M14 16C14 14.8954 14.8954 14 16 14H20C21.1046 14 22 14.8954 22 16V32C22 33.1046 21.1046 34 20 34H16C14.8954 34 14 33.1046 14 32V16Z" fill="white" fill-opacity="0.9"/>
                            <path d="M26 20C26 18.8954 26.8954 18 28 18H32C33.1046 18 34 18.8954 34 20V32C34 33.1046 33.1046 34 32 34H28C26.8954 34 26 33.1046 26 32V20Z" fill="white" fill-opacity="0.9"/>
                            <circle cx="24" cy="38" r="2" fill="white" fill-opacity="0.6"/>
                        </svg>
                    </div>
                    <span class="logo-text">考研平台管理</span>
                </div>

                <el-menu :default-active="$route.path" router
                    text-color="#606266" active-text-color="#409EFF" class="custom-menu">
                    <el-menu-item index="/admin/home">
                        <el-icon>
                            <img :src="dashboardIcon" class="menu-icon-img" />
                        </el-icon>
                        <span>数据看板</span>
                    </el-menu-item>
                    <el-menu-item index="/admin/users-manage">
                        <el-icon>
                            <img :src="userIcon" class="menu-icon-img" />
                        </el-icon>
                        <span>用户管理</span>
                    </el-menu-item>
                    <el-menu-item index="/admin/mistake-monitor">
                        <el-icon>
                            <img :src="correctionNotebookIcon" class="menu-icon-img" />
                        </el-icon>
                        <span>错题监控</span>
                    </el-menu-item>
                    <el-menu-item index="/admin/subjects-manage">
                        <el-icon>
                            <img :src="treeIcon" class="menu-icon-img" />
                        </el-icon>
                        <span>科目体系管理</span>
                    </el-menu-item>
                    <el-menu-item index="/admin/books-manage">
                        <el-icon>
                            <Management />
                        </el-icon>
                        <span>习题册管理</span>
                    </el-menu-item>
                    <el-menu-item index="/admin/questions-manage">
                        <el-icon>
                            <img :src="testBaseIcon" class="menu-icon-img" />
                        </el-icon>
                        <span>题库管理</span>
                    </el-menu-item>
                    <el-menu-item index="/admin/papers-manage">
                        <el-icon>
                            <img :src="mockExamIcon" class="menu-icon-img" />
                        </el-icon>
                        <span>试卷管理</span>
                    </el-menu-item>
                    <el-menu-item index="/admin/exam-record-manage">
                        <el-icon>
                            <img :src="examinationRecordIcon" class="menu-icon-img" />
                        </el-icon>
                        <span>考试记录管理</span>
                    </el-menu-item>
                    <el-menu-item index="/admin/user-progress-monitor">
                        <el-icon>
                            <TrendCharts />
                        </el-icon>
                        <span>学习进度监控</span>
                    </el-menu-item>
                    <el-menu-item index="/admin/question-import">
                        <el-icon>
                            <Upload />
                        </el-icon>
                        <span>题目批量导入</span>
                    </el-menu-item>
                    <el-menu-item index="/admin/question-export">
                        <el-icon>
                            <Download />
                        </el-icon>
                        <span>题目导出PDF</span>
                    </el-menu-item>

                </el-menu>

                <div class="bottom-menu-item">
                    <el-menu :default-active="$route.path" router
                        text-color="#606266" active-text-color="#409EFF" class="custom-menu">
                        <el-menu-item index="/user/home" class="preview-item">
                            <el-icon>
                                <img :src="homeIcon" class="menu-icon-img" />
                            </el-icon>
                            <span>学生端预览</span>
                        </el-menu-item>
                    </el-menu>
                </div>
            </el-aside>

            <el-container>
                <el-header class="header-bar" height="64px">
                    <div class="header-left">
                        <div class="welcome-section">
                            <div class="welcome-icon">
                                <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                                    <path d="M12 2L13.09 8.26L19 7L14.74 11.74L21 14L14.74 15.5L19 20L13.09 18.26L12 24L10.91 18.26L5 20L9.26 15.5L3 14L9.26 11.74L5 7L10.91 8.26L12 2Z" fill="currentColor" fill-opacity="0.8"/>
                                </svg>
                            </div>
                            <div class="welcome-content">
                                <span class="welcome-label">{{ greetingLabel }}</span>
                                <span class="welcome-text">欢迎回来，管理员</span>
                            </div>
                        </div>
                    </div>
                    <div class="header-right">
                        <div class="header-actions">
                            <div class="action-item">
                                <el-tooltip content="通知" placement="bottom">
                                    <el-badge :value="3" :max="99" class="notification-badge">
                                        <el-icon :size="20">
                                            <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                                                <path d="M12 2C13.1 2 14 2.9 14 4V5H10V4C10 2.9 10.9 2 12 2ZM21 19V20H3V19L5 17V11C5 7.93 7.05 5.36 10 4.35V4C10 2.9 10.9 2 12 2C13.1 2 14 2.9 14 4V4.35C16.95 5.36 19 7.93 19 11V17L21 19ZM12 22C13.1 22 14 21.1 14 20H10C10 21.1 10.9 22 12 22Z" fill="currentColor"/>
                                            </svg>
                                        </el-icon>
                                    </el-badge>
                                </el-tooltip>
                            </div>
                            <div class="action-item">
                                <el-tooltip content="设置" placement="bottom">
                                    <el-icon :size="20">
                                        <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                                            <path d="M12 1L9 9H1L7.5 14L5 22L12 17L19 22L16.5 14L23 9H15L12 1Z" fill="currentColor"/>
                                        </svg>
                                    </el-icon>
                                </el-tooltip>
                            </div>
                        </div>
                        <el-dropdown class="user-dropdown">
                            <span class="el-dropdown-link user-profile">
                                <el-avatar :size="38" :src="userAvatar" />
                                <div class="user-info">
                                    <span class="username">管理员</span>
                                    <span class="user-role">系统管理员</span>
                                </div>
                                <el-icon class="el-icon--right dropdown-icon"><ArrowDown /></el-icon>
                            </span>
                            <template #dropdown>
                                <el-dropdown-menu>
                                    <el-dropdown-item>
                                        <el-icon><User /></el-icon>
                                        <span>个人中心</span>
                                    </el-dropdown-item>
                                    <el-dropdown-item>
                                        <el-icon><Setting /></el-icon>
                                        <span>账号设置</span>
                                    </el-dropdown-item>
                                    <el-dropdown-item divided @click="handleLogout">
                                        <el-icon><SwitchButton /></el-icon>
                                        <span>退出登录</span>
                                    </el-dropdown-item>
                                </el-dropdown-menu>
                            </template>
                        </el-dropdown>
                    </div>
                </el-header>
                <el-main class="content-main" :class="{ 'from-home-enter': isTransitioningFromHome }">
                    <router-view v-slot="{ Component }">
                        <transition name="fade" mode="out-in">
                            <component :is="Component" />
                        </transition>
                    </router-view>
                </el-main>
            </el-container>
        </el-container>
    </div>
</template>

<script setup>
import { useRouter } from 'vue-router'
import { Management, TrendCharts, ArrowDown, SwitchButton, Notebook, Upload, Download, User, Setting } from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'
import { useTransitionStore } from '@/stores/transition'
import { computed, onMounted } from 'vue'

// 导入自定义图标
import homeIcon from '@/assets/icons/home.svg?url'
import dashboardIcon from '@/assets/icons/dashboard.svg?url'
import treeIcon from '@/assets/icons/tree.svg?url'
import mockExamIcon from '@/assets/icons/mock-exam.svg?url'
import correctionNotebookIcon from '@/assets/icons/correction-notebook.svg?url'
import userIcon from '@/assets/icons/user-manage.svg?url'
import testBaseIcon from '@/assets/icons/test-base.svg?url'
import examinationRecordIcon from '@/assets/icons/examination-record.svg?url'

const router = useRouter()
const userStore = useUserStore()
const transitionStore = useTransitionStore()

// 动态问候语
const greetingLabel = computed(() => {
    const hour = new Date().getHours()
    if (hour >= 5 && hour < 9) {
        return '早上好'
    } else if (hour >= 9 && hour < 12) {
        return '上午好'
    } else if (hour >= 12 && hour < 14) {
        return '中午好'
    } else if (hour >= 14 && hour < 18) {
        return '下午好'
    } else if (hour >= 18 && hour < 22) {
        return '晚上好'
    } else {
        return '夜深了'
    }
})

// 页面进入动画状态 - 从 store 获取
const isTransitioningFromHome = computed(() => transitionStore.isEnteringLayout)

// 计算属性：用户头像
const userAvatar = computed(() => {
    return userStore.userInfo.avatar || 'https://cube.elemecdn.com/0/88/03b0d39583f48206768a7534e55bcpng.png'
})

const handleLogout = () => {
    // 1. 调用 Store 的清理方法
    userStore.clearUserInfo()

    // 2. 额外清理可能的残留
    localStorage.removeItem('user')
    localStorage.removeItem('userInfo')
    localStorage.removeItem('role')
    localStorage.removeItem('token')

    // 3. 跳转回登录页
    router.push('/login')
}

// 组件挂载时检测是否从 Home 进入
onMounted(() => {
    // 检测是否从 Home 页面进入，触发偏移补偿动画
    const fromHome = router.options.history.state?.back === '/user/home' ||
                      document.referrer.includes('/user/home')

    if (fromHome) {
        transitionStore.startEnteringLayout()
    }
})
</script>

<style scoped>
.layout-wrapper {
    height: 100vh;
    width: 100vw;
    overflow: hidden;
}

.full-height {
    height: 100%;
}

/* 侧边栏美化 */
.aside-menu {
    background: linear-gradient(180deg, #ffffff 0%, #f7f9fc 100%);
    box-shadow: 3px 0 16px rgba(64, 158, 255, 0.08);
    z-index: 10;
    display: flex;
    flex-direction: column;
    border-right: none;
    overflow: hidden;
    position: relative;
}

.logo-box {
    height: 64px;
    min-height: 64px;
    position: sticky;
    top: 0;
    display: flex;
    align-items: center;
    justify-content: center;
    background: linear-gradient(135deg, #409EFF 0%, #0066CC 100%);
    padding: 0 10px;
    z-index: 1;
    box-shadow: 0 4px 12px rgba(64, 158, 255, 0.2);
}

.logo-icon-wrapper {
    width: 32px;
    height: 32px;
    margin-right: 10px;
    display: flex;
    align-items: center;
    justify-content: center;
}

.logo-icon {
    width: 100%;
    height: 100%;
    animation: float 3s ease-in-out infinite;
}

@keyframes float {
    0%, 100% {
        transform: translateY(0);
    }
    50% {
        transform: translateY(-2px);
    }
}

.logo-text {
    color: #ffffff;
    font-size: 18px;
    font-weight: 700;
    letter-spacing: 1.5px;
    text-shadow: 0 2px 4px rgba(0, 0, 0, 0.15);
}

/* 菜单项美化 */
.custom-menu {
    border-right: none;
    padding: 16px 8px;
    background-color: transparent !important;
    flex: 1;
    overflow-y: auto;
    overflow-x: hidden;
}

/* 隐藏菜单滚动条但保留滚动功能 */
.custom-menu::-webkit-scrollbar {
    width: 0;
    display: none;
}

.custom-menu {
    -ms-overflow-style: none;
    scrollbar-width: none;
}

/* 底部菜单项容器 */
.bottom-menu-item {
    position: sticky;
    bottom: 0;
    border-top: 1px solid rgba(64, 158, 255, 0.1);
    background: linear-gradient(to top, #ffffff 80%, rgba(255, 255, 255, 0.95) 100%);
    z-index: 1;
    padding: 12px 8px;
}

.bottom-menu-item .custom-menu {
    padding: 0;
    flex: none;
}

:deep(.el-menu-item) {
    margin: 0 0 6px 0;
    height: 50px;
    line-height: 50px;
    border-radius: 12px;
    color: #5a5e66 !important;
    transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
    font-weight: 500;
    position: relative;
    overflow: hidden;
}

:deep(.el-menu-item::before) {
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

:deep(.el-menu-item:hover) {
    background: linear-gradient(90deg, rgba(64, 158, 255, 0.08) 0%, rgba(64, 158, 255, 0.04) 100%) !important;
    color: #409EFF !important;
    transform: translateX(2px);
}

:deep(.el-menu-item:hover::before) {
    transform: scaleY(0.5);
}

:deep(.el-menu-item.is-active) {
    background: linear-gradient(90deg, rgba(64, 158, 255, 0.15) 0%, rgba(0, 102, 204, 0.08) 100%) !important;
    color: #409EFF !important;
    font-weight: 600;
    box-shadow: 0 4px 12px rgba(64, 158, 255, 0.15);
}

:deep(.el-menu-item.is-active::before) {
    transform: scaleY(1);
}

:deep(.el-menu-item .el-icon) {
    transition: transform 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

:deep(.el-menu-item:hover .el-icon) {
    transform: scale(1.1);
}

:deep(.el-menu-item.is-active .el-icon) {
    transform: scale(1.15);
}

.menu-divider {
    height: 1px;
    background: linear-gradient(90deg, transparent 0%, rgba(64, 158, 255, 0.15) 50%, transparent 100%);
    margin: 16px 0;
}

.preview-item {
    color: #8a8e98 !important;
    border: 1.5px dashed rgba(64, 158, 255, 0.3);
    background: rgba(64, 158, 255, 0.03) !important;
}

.preview-item:hover {
    border-color: #409EFF;
    color: #409EFF !important;
    background: linear-gradient(90deg, rgba(64, 158, 255, 0.12) 0%, rgba(64, 158, 255, 0.06) 100%) !important;
    transform: translateX(2px);
    box-shadow: 0 2px 8px rgba(64, 158, 255, 0.15);
}

/* 顶部栏美化 */
.header-bar {
    background: linear-gradient(90deg, #ffffff 0%, #f7f9fc 100%);
    border-bottom: 1px solid rgba(64, 158, 255, 0.1);
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding: 0 32px;
    box-shadow: 0 2px 8px rgba(64, 158, 255, 0.06);
    z-index: 9;
}

/* 左侧欢迎区域 */
.welcome-section {
    display: flex;
    align-items: center;
    gap: 12px;
    padding: 8px 16px;
    border-radius: 12px;
    background: linear-gradient(135deg, rgba(64, 158, 255, 0.05) 0%, rgba(64, 158, 255, 0.02) 100%);
    transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

.welcome-section:hover {
    background: linear-gradient(135deg, rgba(64, 158, 255, 0.1) 0%, rgba(64, 158, 255, 0.05) 100%);
    transform: translateY(-1px);
    box-shadow: 0 4px 12px rgba(64, 158, 255, 0.1);
}

.welcome-icon {
    width: 36px;
    height: 36px;
    display: flex;
    align-items: center;
    justify-content: center;
    background: linear-gradient(135deg, #409EFF 0%, #0066CC 100%);
    border-radius: 10px;
    color: white;
    box-shadow: 0 4px 8px rgba(64, 158, 255, 0.2);
}

.welcome-icon svg {
    width: 20px;
    height: 20px;
    animation: rotate 20s linear infinite;
}

@keyframes rotate {
    from {
        transform: rotate(0deg);
    }
    to {
        transform: rotate(360deg);
    }
}

.welcome-content {
    display: flex;
    flex-direction: column;
    gap: 2px;
}

.welcome-label {
    font-size: 12px;
    color: #909399;
    font-weight: 500;
}

.welcome-text {
    font-size: 15px;
    color: #303133;
    font-weight: 600;
}

/* 右侧操作区域 */
.header-right {
    display: flex;
    align-items: center;
    gap: 16px;
}

.header-actions {
    display: flex;
    align-items: center;
    gap: 8px;
    padding-right: 16px;
    border-right: 1px solid rgba(64, 158, 255, 0.15);
}

.action-item {
    width: 40px;
    height: 40px;
    display: flex;
    align-items: center;
    justify-content: center;
    border-radius: 10px;
    color: #606266;
    cursor: pointer;
    transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
    background: transparent;
}

.action-item:hover {
    background: linear-gradient(135deg, rgba(64, 158, 255, 0.1) 0%, rgba(64, 158, 255, 0.05) 100%);
    color: #409EFF;
    transform: translateY(-2px);
    box-shadow: 0 4px 12px rgba(64, 158, 255, 0.15);
}

.notification-badge {
    display: flex;
    align-items: center;
}

.notification-badge :deep(.el-badge__content) {
    background: linear-gradient(135deg, #409EFF 0%, #0066CC 100%);
    border: 2px solid #fff;
    box-shadow: 0 2px 6px rgba(64, 158, 255, 0.3);
}

/* 用户下拉菜单 */
.user-dropdown {
    margin-left: 8px;
}

.user-profile {
    display: flex;
    align-items: center;
    gap: 10px;
    padding: 6px 12px;
    border-radius: 12px;
    cursor: pointer;
    transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
    background: linear-gradient(135deg, rgba(64, 158, 255, 0.05) 0%, rgba(64, 158, 255, 0.02) 100%);
}

.user-profile:hover {
    background: linear-gradient(135deg, rgba(64, 158, 255, 0.1) 0%, rgba(64, 158, 255, 0.05) 100%);
    box-shadow: 0 4px 12px rgba(64, 158, 255, 0.12);
}

.user-profile .el-avatar {
    border: 2px solid rgba(64, 158, 255, 0.2);
    box-shadow: 0 2px 8px rgba(64, 158, 255, 0.15);
}

.user-info {
    display: flex;
    flex-direction: column;
    gap: 2px;
    text-align: left;
}

.username {
    font-size: 14px;
    font-weight: 600;
    color: #303133;
    line-height: 1.2;
}

.user-role {
    font-size: 12px;
    color: #909399;
    font-weight: 500;
}

.dropdown-icon {
    color: #909399;
    transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

.user-profile:hover .dropdown-icon {
    color: #409EFF;
    transform: rotate(180deg);
}

/* 下拉菜单样式 */
:deep(.el-dropdown-menu) {
    padding: 8px 0;
    border: 1px solid rgba(64, 158, 255, 0.15);
    box-shadow: 0 8px 24px rgba(64, 158, 255, 0.12);
    border-radius: 12px;
}

:deep(.el-dropdown-menu__item) {
    padding: 10px 16px;
    display: flex;
    align-items: center;
    gap: 10px;
    font-weight: 500;
    transition: all 0.2s cubic-bezier(0.4, 0, 0.2, 1);
}

:deep(.el-dropdown-menu__item:hover) {
    background: linear-gradient(90deg, rgba(64, 158, 255, 0.08) 0%, rgba(64, 158, 255, 0.04) 100%);
    color: #409EFF;
}

:deep(.el-dropdown-menu__item .el-icon) {
    font-size: 16px;
    color: #606266;
}

:deep(.el-dropdown-menu__item:hover .el-icon) {
    color: #409EFF;
}

/* 内容区 */
.content-main {
    background-color: #f0f2f5;
    padding: 20px;
    /* 从 Home 进入时的偏移补偿动画 */
    transition: transform 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

.content-main.from-home-enter {
    transform: translateX(-240px);
}

/* 路由切换动画 */
.fade-enter-active,
.fade-leave-active {
    transition: opacity 0.2s ease;
}

.fade-enter-from,
.fade-leave-to {
    opacity: 0;
}

.icon {
    width: 20px;
    height: 20px;
    fill: currentColor;
    vertical-align: middle;
}

/* 自定义SVG图标样式 */
.menu-icon-img {
    width: 18px;
    height: 18px;
    filter: invert(45%) sepia(8%) saturate(1597%) hue-rotate(179deg) brightness(95%) contrast(85%);
    transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

:deep(.el-menu-item:hover .menu-icon-img),
:deep(.el-menu-item.is-active .menu-icon-img) {
    filter: invert(42%) sepia(98%) saturate(1814%) hue-rotate(189deg) brightness(99%) contrast(91%);
    transform: scale(1.15);
}
</style>