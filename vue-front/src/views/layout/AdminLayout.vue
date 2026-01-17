<template>
    <div class="layout-wrapper">
        <el-container class="full-height">
            <el-aside width="240px" class="aside-menu">
                <div class="logo-box">
                    <el-icon :size="28" color="#409EFF" style="margin-right: 10px">
                        <Management />
                    </el-icon>
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

                    <div class="menu-divider"></div>

                    <el-menu-item index="/user/dashboard" class="preview-item">
                        <el-icon>
                            <img :src="homeIcon" class="menu-icon-img" />
                        </el-icon>
                        <span>学生端预览</span>
                    </el-menu-item>
                </el-menu>
            </el-aside>

            <el-container>
                <el-header class="header-bar" height="64px">
                    <div class="header-left">
                        <span class="welcome-text">欢迎回来，管理员</span>
                    </div>
                    <div class="header-right">
                        <el-dropdown>
                            <span class="el-dropdown-link user-profile">
                                <el-avatar :size="36" :src="userAvatar" />
                                <span class="username">Admin</span>
                                <el-icon class="el-icon--right"><ArrowDown /></el-icon>
                            </span>
                            <template #dropdown>
                                <el-dropdown-menu>
                                    <el-dropdown-item @click="handleLogout">
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
import { Management, TrendCharts, ArrowDown, SwitchButton, Notebook, Upload, Download } from '@element-plus/icons-vue'
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
    background-color: #fff;
    box-shadow: 2px 0 8px rgba(0, 0, 0, 0.05);
    z-index: 10;
    display: flex;
    flex-direction: column;
    border-right: 1px solid #f0f2f5;
}

.logo-box {
    height: 64px;
    display: flex;
    align-items: center;
    justify-content: center;
    background: #fff;
    border-bottom: 1px solid #f0f2f5;
}

.logo-text {
    color: #303133;
    font-size: 18px;
    font-weight: 600;
    letter-spacing: 1px;
}

/* 菜单项美化 */
.custom-menu {
    border-right: none;
    padding-top: 10px;
    background-color: transparent !important;
}

:deep(.el-menu-item) {
    margin: 4px 10px;
    height: 48px;
    line-height: 48px;
    border-radius: 6px;
    color: #606266 !important;
    transition: all 0.2s;
}

:deep(.el-menu-item:hover) {
    background-color: #ecf5ff !important;
    color: #409EFF !important;
}

:deep(.el-menu-item.is-active) {
    background-color: #e6f7ff !important;
    color: #1890ff !important;
    font-weight: 500;
    position: relative;
}

:deep(.el-menu-item.is-active)::after {
    content: '';
    position: absolute;
    right: -10px;
    top: 0;
    bottom: 0;
    width: 3px;
    background: #1890ff;
    border-radius: 2px 0 0 2px;
}

.menu-divider {
    height: 1px;
    background: #ebeef5;
    margin: 20px 20px;
}

.preview-item {
    color: #909399 !important;
    border: 1px dashed #dcdfe6;
}

.preview-item:hover {
    border-color: #409EFF;
    color: #409EFF !important;
    background: #f0f9eb !important;
}

/* 顶部栏美化 */
.header-bar {
    background: #fff;
    border-bottom: 1px solid #e6e6e6;
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding: 0 24px;
    box-shadow: 0 1px 4px rgba(0, 21, 41, 0.08);
    z-index: 9;
}

.welcome-text {
    font-size: 15px;
    color: #333;
    font-weight: 500;
}

.user-profile {
    display: flex;
    align-items: center;
    cursor: pointer;
    padding: 5px 10px;
    border-radius: 20px;
    transition: background 0.3s;
}

.user-profile:hover {
    background: #f5f5f5;
}

.username {
    margin: 0 8px;
    font-size: 14px;
    color: #606266;
}

/* 下拉菜单样式 */
:deep(.el-dropdown-menu__item) {
    display: flex;
    align-items: center;
    gap: 8px;
}

:deep(.el-dropdown-menu__item .el-icon) {
    font-size: 16px;
}

/* 内容区 */
.content-main {
    background-color: #f0f2f5;
    padding: 24px;
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
    filter: invert(41%) sepia(5%) saturate(542%) hue-rotate(182deg) brightness(91%) contrast(87%);
    transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

:deep(.el-menu-item:hover .menu-icon-img),
:deep(.el-menu-item.is-active .menu-icon-img) {
    filter: invert(53%) sepia(96%) saturate(3089%) hue-rotate(196deg) brightness(100%) contrast(101%);
    transform: scale(1.08);
}
</style>