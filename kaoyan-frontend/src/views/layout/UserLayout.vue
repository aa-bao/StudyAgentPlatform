<template>
    <div class="layout-wrapper">
        <el-container class="full-height">
            <el-aside :width="isCollapse ? '64px' : '240px'" class="aside-menu">
                <div class="logo-box">
                    <div class="sidebar-toggle" @click="isCollapse = !isCollapse">
                        <el-icon>
                            <Expand v-if="isCollapse" />
                            <Fold v-else />
                        </el-icon>
                    </div>
                    <transition name="fade">
                        <span v-show="!isCollapse" class="logo-text">考研全流程平台</span>
                    </transition>
                </div>

                <el-menu :default-active="activeMenu" router :collapse="isCollapse" class="el-menu-vertical custom-menu"
                    text-color="#606266" active-text-color="#409EFF">

                    <el-menu-item index="/user/dashboard">
                        <el-icon>
                            <img :src="dashboardIcon" class="menu-icon-svg" />
                        </el-icon>
                        <template #title>备考看板</template>
                    </el-menu-item>

                    <el-menu-item index="/user/subject">
                        <el-icon>
                            <img :src="singlePracticeIcon" class="menu-icon-svg" />
                        </el-icon>
                        <template #title>逐题精练</template>
                    </el-menu-item>

                    <el-menu-item index="/user/topic-drill">
                        <el-icon>
                            <img :src="topicDrillIcon" class="menu-icon-svg" />
                        </el-icon>
                        <template #title>专项突破</template>
                    </el-menu-item>

                    <el-menu-item index="/user/mock-exam">
                        <el-icon>
                            <img :src="mockExamIcon" class="menu-icon-svg" />
                        </el-icon>
                        <template #title>真题模考</template>
                    </el-menu-item>

                    <el-menu-item index="/user/correction-notebook">
                        <el-icon>
                            <img :src="correctionNotebookIcon" class="menu-icon-svg" />
                        </el-icon>
                        <template #title>错题本</template>
                    </el-menu-item>

                    <div class="menu-divider" v-if="userRole === 'admin'"></div>

                    <el-menu-item v-if="userRole === 'admin'" index="/admin/home" class="admin-entry">
                        <el-icon>
                            <Tools />
                        </el-icon>
                        <template #title>进入管理后台</template>
                    </el-menu-item>
                </el-menu>
            </el-aside>

            <el-container direction="vertical" class="main-container">
                <el-header class="header-bar" height="64px">
                    <div class="header-left">
                        <span class="breadcrumb-text">不要埋没你的梦想</span>
                    </div>

                    <div class="header-right">
                        <el-dropdown trigger="click">
                            <div class="user-info">
                                <el-avatar :size="36" :src="userAvatar" />
                                <span class="user-name">{{ userName }}</span>
                                <el-icon class="el-icon--right">
                                    <ArrowDown />
                                </el-icon>
                            </div>
                            <template #dropdown>
                                <el-dropdown-menu class="user-dropdown">
                                    <el-dropdown-item @click="router.push('/user/profile')" icon="UserFilled">
                                        个人主页
                                    </el-dropdown-item>
                                    <el-dropdown-item v-if="userRole === 'ADMIN'" @click="router.push('/admin/home')"
                                        icon="Management">
                                        进入后台管理系统
                                    </el-dropdown-item>
                                    <el-dropdown-item divided @click="handleLogout" icon="SwitchButton"
                                        style="color: #F56C6C">
                                        退出登录
                                    </el-dropdown-item>
                                </el-dropdown-menu>
                            </template>
                        </el-dropdown>
                    </div>
                </el-header>

                <el-main class="content-main">
                    <router-view v-slot="{ Component }">
                        <transition name="fade-transform" mode="out-in">
                            <component :is="Component" />
                        </transition>
                    </router-view>
                </el-main>
            </el-container>
        </el-container>
    </div>
</template>

<script setup>
import { ref, computed, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessageBox, ElMessage } from 'element-plus'
import { useUserStore } from '../../stores/user' 

// 导入自定义图标
import dashboardIcon from '@/assets/icons/dashboard.svg?url'
import singlePracticeIcon from '@/assets/icons/single-practice.svg?url'
import topicDrillIcon from '@/assets/icons/topic-drill.svg?url'
import mockExamIcon from '@/assets/icons/mock-exam.svg?url'
import correctionNotebookIcon from '@/assets/icons/correction-notebook.svg?url'

const router = useRouter()
const userStore = useUserStore() // 使用 store
const isCollapse = ref(true)
const activeMenu = computed(() => router.path)

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

.main-container {
    height: 100%;
    overflow: hidden;
    display: flex;
    flex-direction: column;
}

/* 侧边栏美化 */
.aside-menu {
    background-color: #fff;
    height: 100vh;
    border-right: 1px solid #e6e6e6 !important;
    box-shadow: 2px 0 8px rgba(0, 0, 0, 0.05);
    transition: width 0.3s cubic-bezier(0.2, 0, 0, 1);
    overflow: hidden;
    z-index: 10;
    display: flex;
    flex-direction: column;
}

.logo-box {
    height: 64px;
    display: flex;
    align-items: center;
    padding-left: 20px;
    background: #fff;
    border-bottom: 1px solid #e6e6e6;
    box-shadow: none;
    overflow: hidden;
    white-space: nowrap;
}

.sidebar-toggle {
    display: flex;
    align-items: center;
    justify-content: center;
    width: 24px;
    height: 24px;
    cursor: pointer;
    margin-right: 12px;
    color: #606266;
    transition: all 0.3s;
}

.sidebar-toggle:hover {
    color: #409EFF;
    transform: scale(1.1);
}

.logo-text {
    color: #303133;
    font-size: 18px;
    font-weight: 600;
    letter-spacing: 0.5px;
    white-space: nowrap;
}

/* 菜单项美化 */
.custom-menu {
    border-right: none;
    padding-top: 10px;
    background-color: transparent;
}

.el-menu-vertical:not(.el-menu--collapse) {
    width: 240px;
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
    box-shadow: none;
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

:deep(.el-menu-item .el-icon) {
    font-size: 18px;
    margin-right: 10px;
    color: inherit;
}

/* 菜单图标 SVG 样式 */
.menu-icon-svg {
    width: 1em;
    height: 1em;
    /* 
       利用 filter 实现颜色动态变化：
       初始状态：黑色图标 -> 转为深灰色 (#606266)
    */
    filter: invert(41%) sepia(5%) saturate(542%) hue-rotate(182deg) brightness(91%) contrast(87%);
    transition: all 0.2s;
}

/* 选中或悬停时：变为主题蓝 (#409EFF) */
:deep(.el-menu-item:hover) .menu-icon-svg,
:deep(.el-menu-item.is-active) .menu-icon-svg {
    filter: invert(53%) sepia(96%) saturate(3089%) hue-rotate(196deg) brightness(100%) contrast(101%);
}

/* 折叠状态下的特殊样式 */
:deep(.el-menu--collapse .el-menu-item) {
    justify-content: center;
    margin: 4px 0 !important;
    /* 移除水平 margin */
    padding: 0 !important;
    /* 移除默认 padding */
    width: 100%;
    /* 占满宽度 */
}

:deep(.el-menu--collapse .el-menu-item .el-icon) {
    margin-right: 0;
    text-align: center;
    /* 确保图标内部也居中 */
    vertical-align: middle;
}

:deep(.el-menu--collapse .el-tooltip__trigger) {
    justify-content: center;
    padding: 0 !important;
}

.menu-divider {
    height: 1px;
    background: rgba(255, 255, 255, 0.1);
    margin: 20px 20px;
}

.admin-entry {
    color: #e6a23c !important;
    border: 1px dashed rgba(230, 162, 60, 0.3);
}

.admin-entry:hover {
    background-color: rgba(230, 162, 60, 0.1) !important;
    border-color: #e6a23c;
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
    flex-shrink: 0;
}

.header-left {
    display: flex;
    align-items: center;
}

.breadcrumb-text {
    font-size: 16px;
    color: #303133;
    font-weight: 500;
    letter-spacing: 0.5px;
}

.header-right {
    display: flex;
    align-items: center;
}

.user-info {
    display: flex;
    align-items: center;
    cursor: pointer;
    padding: 4px 8px;
    border-radius: 40px;
    transition: background 0.3s;
    border: 1px solid transparent;
}

.user-info:hover {
    background: #f7f8fa;
    border-color: #e6e6e6;
}

.user-name {
    font-size: 14px;
    color: #606266;
    margin: 0 10px;
    font-weight: 500;
}

/* 内容区 */
.content-main {
    background-color: #f0f2f5;
    padding: 0;
    /* 移除默认内边距，由各个页面自己控制 */
    overflow-y: auto;
    flex: 1;
}

/* 路由切换动画 */
.fade-transform-enter-active,
.fade-transform-leave-active {
    transition: all 0.3s;
}

.fade-transform-enter-from {
    opacity: 0;
    transform: translateX(-20px);
}

.fade-transform-leave-to {
    opacity: 0;
    transform: translateX(20px);
}

/* 简单的淡入淡出 */
.fade-enter-active,
.fade-leave-active {
    transition: opacity 0.3s ease;
}

.fade-enter-from,
.fade-leave-to {
    opacity: 0;
}
</style>