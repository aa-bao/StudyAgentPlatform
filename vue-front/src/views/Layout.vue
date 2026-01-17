<template>
    <div class="layout-container">
        <el-container class="full-height">
            <el-aside :width="isCollapse ? '64px' : '240px'" class="aside-menu">
                <div class="logo-box">
                    <img src="https://img.icons8.com/fluency/48/graduation-cap.png" alt="logo">
                    <transition name="el-fade-in">
                        <span v-show="!isCollapse" class="logo-text">考研全流程</span>
                    </transition>
                </div>

                <el-menu :default-active="activeMenu" router :collapse="isCollapse" class="el-menu-vertical">
                    <el-menu-item index="/user/dashboard">
                        <el-icon>
                            <Odometer />
                        </el-icon>
                        <template #title>备考看板</template>
                    </el-menu-item>
                    <el-menu-item index="/user/questions">
                        <el-icon>
                            <Notebook />
                        </el-icon>
                        <template #title>在线刷题</template>
                    </el-menu-item>
                    <el-menu-item index="/user/error-book">
                        <el-icon>
                            <CollectionTag />
                        </el-icon>
                        <template #title>错题本</template>
                    </el-menu-item>
                </el-menu>
            </el-aside>

            <el-container class="is-vertical">
                <el-header class="header-bar">
                    <div class="header-left">
                        <el-icon class="collapse-btn" @click="isCollapse = !isCollapse">
                            <Expand v-if="isCollapse" />
                            <Fold v-else />
                        </el-icon>
                        <el-breadcrumb separator="/">
                            <el-breadcrumb-item>首页</el-breadcrumb-item>
                            <el-breadcrumb-item>{{ currentRouteName }}</el-breadcrumb-item>
                        </el-breadcrumb>
                    </div>
                    <div class="header-right">
                        <el-dropdown>
                            <div class="user-info">
                                <el-avatar :size="32"
                                    src="https://cube.elemecdn.com/0/88/03b0d39583f48206768a7534e55bcpng.png" />
                                <span class="user-name">研友，你好</span>
                            </div>
                            <template #dropdown>
                                <el-dropdown-menu>
                                    <el-dropdown-item @click="handleLogout">退出登录</el-dropdown-item>
                                </el-dropdown-menu>
                            </template>
                        </el-dropdown>
                    </div>
                </el-header>

                <el-main class="content-main">
                    <router-view />
                </el-main>
            </el-container>
        </el-container>
    </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { Odometer, Notebook, CollectionTag, Fold, Expand } from '@element-plus/icons-vue'

const route = useRoute()
const router = useRouter()
const isCollapse = ref(false)

const activeMenu = computed(() => route.path)
const currentRouteName = computed(() => {
    const map = { '/user/dashboard': '备考看板', '/user/questions': '在线刷题', '/user/error-book': '错题本' }
    return map[route.path] || '刷题中'
})

const handleLogout = () => {
    localStorage.removeItem('user')
    router.push('/')
}
</script>

<style scoped>
/* 1. 强制撑满屏幕，解决底部留白 */
.layout-container {
    height: 100vh;
    width: 100vw;
    overflow: hidden;
    display: flex;
}

.full-height {
    height: 100%;
}

/* 2. 侧边栏美化 */
.aside-menu {
    background-color: #001529;
    transition: width 0.3s cubic-bezier(0.2, 0, 0, 1);
    box-shadow: 2px 0 8px rgba(0, 21, 41, 0.15);
    z-index: 10;
    display: flex;
    flex-direction: column;
}

.logo-box {
    height: 64px;
    display: flex;
    align-items: center;
    padding: 0 20px;
    background: #002140;
}

.logo-box img {
    width: 28px;
}

.logo-text {
    color: white;
    font-weight: 600;
    font-size: 16px;
    margin-left: 12px;
    white-space: nowrap;
}

/* 菜单项美化 */
.el-menu-vertical {
    border-right: none !important;
    background-color: transparent;
    padding: 10px;
}

.el-menu-item {
    height: 50px;
    line-height: 50px;
    margin: 4px 0;
    border-radius: 8px;
    /* 圆角效果 */
    color: #a6adb4 !important;
}

.el-menu-item:hover {
    color: #fff !important;
    background-color: rgba(255, 255, 255, 0.1) !important;
}

.el-menu-item.is-active {
    background-color: #1890ff !important;
    color: #fff !important;
    box-shadow: 0 4px 12px rgba(24, 144, 255, 0.4);
}

/* 3. 顶栏美化 */
.header-bar {
    background: #fff;
    height: 64px;
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding: 0 24px;
    border-bottom: 1px solid #f0f0f0;
}

.collapse-btn {
    font-size: 20px;
    cursor: pointer;
    margin-right: 20px;
    color: #333;
}

.header-left {
    display: flex;
    align-items: center;
}

.content-main {
    background-color: #f5f7f9;
    padding: 24px;
    overflow-y: auto;
    /* 允许内部滚动 */
    flex: 1;
    /* 占据剩余全部高度 */
}

.user-info {
    display: flex;
    align-items: center;
    gap: 10px;
    padding: 5px 10px;
    border-radius: 20px;
    transition: background 0.3s;
}

.user-info:hover {
    background: #f6f6f6;
}

.user-name {
    font-size: 14px;
    color: #333;
}
</style>