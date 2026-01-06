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
                            <HomeFilled />
                        </el-icon>
                        <span>数据看板</span>
                    </el-menu-item>
                    <el-menu-item index="/admin/users-manage">
                        <el-icon>
                            <User />
                        </el-icon>
                        <span>用户管理</span>
                    </el-menu-item>
                    <el-menu-item index="/admin/mistake-monitor">
                        <el-icon>
                            <Warning />
                        </el-icon>
                        <span>错题监控</span>
                    </el-menu-item>
                    <el-menu-item index="/admin/subjects-manage">
                        <el-icon>
                            <Files />
                        </el-icon>
                        <span>科目体系管理</span>
                    </el-menu-item>
                    <el-menu-item index="/admin/books-manage">
                        <el-icon>
                            <Reading />
                        </el-icon>
                        <span>习题册管理</span>
                    </el-menu-item>
                    <el-menu-item index="/admin/questions-manage">
                        <el-icon>
                            <Document />
                        </el-icon>
                        <span>题库管理</span>
                    </el-menu-item>

                    <div class="menu-divider"></div>

                    <el-menu-item index="/user/dashboard" class="preview-item">
                        <el-icon>
                            <Monitor />
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
                                <el-avatar :size="32"
                                    src="https://cube.elemecdn.com/0/88/03b0d39583f48206768a7534e55bcpng.png" />
                                <span class="username">Admin</span>
                                <el-icon class="el-icon--right"><arrow-down /></el-icon>
                            </span>
                            <template #dropdown>
                                <el-dropdown-menu>
                                    <el-dropdown-item @click="handleLogout" icon="SwitchButton">退出登录</el-dropdown-item>
                                </el-dropdown-menu>
                            </template>
                        </el-dropdown>
                    </div>
                </el-header>
                <el-main class="content-main">
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
import { HomeFilled, Document, Monitor, Management, Reading, Files, User, Warning, ArrowDown, SwitchButton } from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const userStore = useUserStore()

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

/* 内容区 */
.content-main {
    background-color: #f0f2f5;
    padding: 24px;
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
</style>