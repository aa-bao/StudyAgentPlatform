<template>
    <div class="subject-list-container">
        <!-- 背景装饰 -->
        <div class="bg-shape shape-1"></div>
        <div class="bg-shape shape-2"></div>

        <div class="content-container">
            <!-- 导航面包屑 (仅在非首页显示) -->
            <div class="nav-header" v-if="currentLevel > 0">
                <div class="breadcrumb-wrapper">
                    <el-icon class="home-icon" @click="resetLevel">
                        <HomeFilled />
                    </el-icon>
                    <span class="separator">/</span>
                    <span class="current-crumb">{{ selectedSubject?.name }}</span>
                </div>
            </div>


            <!-- Level 0: 四大公共课/专业课选择 -->
            <transition name="fade-slide" mode="out-in">
                <div v-if="currentLevel === 0" class="level-wrapper home-view">
                    <!-- 头部 标题区域 -->
                    <div class="main-header">
                        <div class="header-icon-box">
                            <el-icon :size="48">
                                <Trophy />
                            </el-icon>
                        </div>
                        <h1 class="main-title">全真模拟练习</h1>
                        <p class="main-subtitle">沉浸式刷题体验 · 智能评估 · 专项突破</p>
                    </div>

                    <!-- 科目选择网格 -->
                    <div class="subject-grid-level0">
                        <div v-for="(item, index) in mainSubjects" :key="item.name" class="subject-card-glass"
                            :class="`delay-${index}`" @click="handleSubjectSelect(item)">

                            <div class="card-inner">
                                <div class="icon-wrapper" :class="item.type">
                                    <img v-if="item.customIcon" :src="item.customIcon" class="custom-icon-img" />
                                    <el-icon v-else :size="40">
                                        <component :is="item.icon" />
                                    </el-icon>
                                </div>
                                <div class="card-info">
                                    <h3 class="card-title">{{ item.name }}</h3>
                                    <p class="card-desc">点击进入专项练习</p>
                                </div>
                                <div class="hover-arrow">
                                    <el-icon>
                                        <Right />
                                    </el-icon>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>

                <!-- Level 1: 详细习题集选择 -->
                <div v-else-if="currentLevel === 1" class="level-wrapper book-view">
                    <div class="sub-header">
                        <h2 class="sub-title">{{ selectedSubject.name }}习题库</h2>
                        <p class="sub-subtitle">共找到 {{ bookList.length }} 本相关习题集</p>
                    </div>

                    <el-empty v-if="bookList.length === 0" description="该科目下暂无习题集" :image-size="200"
                        class="custom-empty" />

                    <div class="book-grid" v-else>
                        <div v-for="(book, index) in bookList" :key="book.id" class="book-card"
                            :class="`delay-${index % 4}`" @click="goToPractice(book)">
                            <div class="book-cover-section">
                                <div class="book-cover-icon">
                                    <el-icon>
                                        <Notebook />
                                    </el-icon>
                                </div>
                                <div class="book-tag">专项</div>
                            </div>
                            <div class="book-content-section">
                                <h3 class="book-name" :title="book.name">{{ book.name }}</h3>
                                <div class="book-footer">
                                    <span class="start-text">开始刷题</span>
                                    <el-icon class="action-icon">
                                        <Right />
                                    </el-icon>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
            </transition>
        </div>
    </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import request from '@/utils/request'
import {
    Notebook, Right, Flag, Reading, Histogram, Cpu,
    HomeFilled, Trophy
} from '@element-plus/icons-vue'

// 导入自定义图标
import politicsIcon from '@/assets/icons/politics.svg?url'
import englishIcon from '@/assets/icons/english.svg?url'
import mathIcon from '@/assets/icons/math_1.svg?url'
import csIcon from '@/assets/icons/408.svg?url'

const router = useRouter()
const route = useRoute()

// 状态管理
const currentLevel = ref(0) // 0:科目选择, 1:习题集选择
const selectedSubject = ref(null)
const bookList = ref([])

// Level 0: 固定四大科目
const mainSubjects = [
    { name: '考研政治', icon: 'Flag', type: 'politics', customIcon: politicsIcon },
    { name: '考研英语', icon: 'Reading', type: 'english', customIcon: englishIcon },
    { name: '考研数学', icon: 'Histogram', type: 'math', customIcon: mathIcon },
    { name: '408专业课', icon: 'Cpu', type: 'cs', customIcon: csIcon }
]

const handleSubjectSelect = async (subject) => {
    selectedSubject.value = subject
    const res = await request.get('/book/list-by-subject', {
        params: { subjectName: subject.name }
    })
    if (res.code === 200) {
        bookList.value = res.data
        currentLevel.value = 1
    }
}

const goToPractice = (book) => {
    router.push(`/user/single-practice/${book.id}?name=${book.name}&subject=${selectedSubject.value.name}`)
}

const resetLevel = () => {
    currentLevel.value = 0
    selectedSubject.value = null
}

onMounted(() => {
    // 检查路由参数，如果有 subject，则自动进入该科目的 Level 1
    const subjectName = route.query.subject
    if (subjectName) {
        const subject = mainSubjects.find(s => s.name === subjectName)
        if (subject) {
            handleSubjectSelect(subject)
        }
    }
})
</script>

<style scoped>
.subject-list-container {
    position: relative;
    width: 100%;
    height: 100%;
    background: linear-gradient(135deg, #409EFF 0%, #36D1DC 100%);
    overflow: hidden;
    color: #fff;
    box-sizing: border-box;
    display: flex;
    flex-direction: column;
    align-items: center;
}

/* 背景装饰图形 */
.bg-shape {
    position: absolute;
    border-radius: 50%;
    background: rgba(255, 255, 255, 0.1);
    filter: blur(80px);
    z-index: 0;
    pointer-events: none;
}

.shape-1 {
    width: 500px;
    height: 500px;
    top: -100px;
    left: -100px;
}

.shape-2 {
    width: 600px;
    height: 600px;
    bottom: -150px;
    right: -150px;
    background: rgba(255, 255, 255, 0.08);
}

/* 内容限制容器 */
.content-container {
    position: relative;
    z-index: 1;
    max-width: 1600px;
    /* Increased width to 1600px */
    width: 100%;
    margin: 0 auto;
    padding: 20px 40px 40px;
    display: flex;
    flex-direction: column;
    height: 100%;
    overflow-y: auto;
    box-sizing: border-box;
    -ms-overflow-style: none;
    scrollbar-width: none;
}

.content-container::-webkit-scrollbar {
    display: none;
}

/* 导航头 */
.nav-header {
    margin-bottom: 30px;
    display: flex;
    align-items: center;
    min-height: 40px;
    width: 100%;
}

.breadcrumb-wrapper {
    display: inline-flex;
    align-items: center;
    background: rgba(255, 255, 255, 0.2);
    padding: 8px 16px;
    border-radius: 20px;
    backdrop-filter: blur(10px);
    font-size: 14px;
}

.home-icon {
    cursor: pointer;
    font-size: 18px;
    margin-right: 8px;
    opacity: 0.8;
    transition: opacity 0.2s;
}

.home-icon:hover {
    opacity: 1;
}

.separator {
    margin: 0 8px;
    opacity: 0.6;
}

.current-crumb {
    font-weight: 600;
}

/* 首页 Level 0 样式 */
.home-view {
    flex: 1;
    display: flex;
    flex-direction: column;
    justify-content: center;
    align-items: center;
    /* Center children horizontally */
    padding-bottom: 60px;
    min-height: 600px;
    width: 100%;
}

.main-header {
    text-align: center;
    margin-bottom: 42px;
    flex-shrink: 0;
    width: 100%;
}

.header-icon-box {
    width: 80px;
    height: 80px;
    background: rgba(255, 255, 255, 0.2);
    border-radius: 50%;
    display: flex;
    align-items: center;
    justify-content: center;
    margin: 0 auto 24px;
    box-shadow: 0 8px 20px rgba(0, 0, 0, 0.1);
}

.main-title {
    font-size: 48px;
    font-weight: 800;
    margin: 0 0 16px 0;
    letter-spacing: 2px;
    text-shadow: 0 4px 10px rgba(0, 0, 0, 0.1);
}

.main-subtitle {
    font-size: 18px;
    opacity: 0.9;
    font-weight: 300;
    margin: 0;
}

/* Grid Layout */
.subject-grid-level0 {
    display: grid;
    grid-template-columns: repeat(4, 1fr);
    gap: 30px;
}

/* 响应式调整 */
@media (max-width: 1024px) {
    .subject-grid-level0 {
        grid-template-columns: repeat(2, 1fr);
    }
}

@media (max-width: 600px) {
    .subject-grid-level0 {
        grid-template-columns: 1fr;
    }
}

/* 卡片样式 */
.subject-card-glass {
    background: rgba(255, 255, 255, 0.95);
    border-radius: 24px;
    cursor: pointer;
    transition: all 0.4s cubic-bezier(0.34, 1.56, 0.64, 1);
    position: relative;
    overflow: hidden;
    height: 300px;
    width: 100%;
    box-shadow: 0 10px 30px rgba(0, 0, 0, 0.15);
    border: 1px solid rgba(255, 255, 255, 0.4);
}

.subject-card-glass:hover {
    transform: translateY(-10px) scale(1.02);
    box-shadow: 0 20px 40px rgba(0, 0, 0, 0.25);
    background: #fff;
    border-color: #fff;
}

.card-inner {
    padding: 70px;
    padding-bottom: 50px;
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    text-align: center;
}

.icon-wrapper {
    width: 100px;
    height: 100px;
    border-radius: 50%;
    display: flex;
    align-items: center;
    justify-content: center;
    margin-bottom: 30px;
    transition: transform 0.4s;
}

/* 图标背景色 - 使用 !important 强制生效 */
.icon-wrapper.politics {
    background: linear-gradient(135deg, #ff7875 0%, #ff4d4f 100%) !important;
    color: #fff !important;
    box-shadow: 0 4px 10px rgba(255, 77, 79, 0.3);
}

.icon-wrapper.english {
    background: linear-gradient(135deg, #69c0ff 0%, #1890ff 100%) !important;
    color: #fff !important;
    box-shadow: 0 4px 10px rgba(24, 144, 255, 0.3);
}

.icon-wrapper.math {
    background: linear-gradient(135deg, #95de64 0%, #52c41a 100%) !important;
    color: #fff !important;
    box-shadow: 0 4px 10px rgba(82, 196, 26, 0.3);
}

.icon-wrapper.cs {
    background: linear-gradient(135deg, #ffc069 0%, #fa8c16 100%) !important;
    color: #fff !important;
    box-shadow: 0 4px 10px rgba(250, 140, 22, 0.3);
}

/* 强制让内部的 el-icon 也是白色 */
.icon-wrapper :deep(.el-icon) {
    color: #fff !important;
}

.custom-icon-img {
    width: 60%;
    height: 60%;
    object-fit: contain;
    /* 强制将 SVG 图片反白，适配深色背景 */
    filter: brightness(0) invert(1);
}

.subject-card-glass:hover .icon-wrapper {
    transform: scale(1.1) rotate(5deg);
}

.card-title {
    color: #333;
    font-size: 24px;
    font-weight: 700;
    margin: 0 0 12px 0;
}

.card-desc {
    color: #999;
    font-size: 14px;
    margin: 0;
}

.hover-arrow {
    position: absolute;
    bottom: 20px;
    width: 40px;
    height: 40px;
    border-radius: 50%;
    background: #409EFF;
    color: #fff;
    display: flex;
    align-items: center;
    justify-content: center;
    opacity: 0;
    transform: translateY(20px);
    transition: all 0.3s;
}

.subject-card-glass:hover .hover-arrow {
    opacity: 1;
    transform: translateY(0);
}

/* Level 1 Book View */
.level-wrapper.book-view {
    margin-top: 20px;
    width: 100%;
}

.sub-header {
    text-align: center;
    margin-bottom: 10px;
}

.sub-title {
    font-size: 36px;
    font-weight: 800;
    margin: 0 0 12px 0;
    text-shadow: 0 4px 10px rgba(0, 0, 0, 0.1);
}

.sub-subtitle {
    font-size: 16px;
    opacity: 0.9;
    margin: 0;
    font-weight: 300;
}

.book-grid {
    display: grid;
    grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
    gap: 30px;
    padding: 10px;
    max-width: 1200px;
    margin: 0 auto;
}

.book-card {
    background: rgba(255, 255, 255, 0.9);
    backdrop-filter: blur(10px);
    border-radius: 24px;
    overflow: hidden;
    cursor: pointer;
    transition: all 0.4s cubic-bezier(0.34, 1.56, 0.64, 1);
    display: flex;
    flex-direction: column;
    box-shadow: 0 10px 30px rgba(0, 0, 0, 0.1);
    height: 320px;
    /* Increased height */
    border: 1px solid rgba(255, 255, 255, 0.5);
    position: relative;
}

.book-card:hover {
    transform: translateY(-10px) scale(1.02);
    box-shadow: 0 20px 40px rgba(0, 0, 0, 0.2);
    background: #fff;
    z-index: 2;
}

.book-cover-section {
    height: 180px;
    /* Increased cover height */
    background: linear-gradient(135deg, #f5f7fa 0%, #e4e7ed 100%);
    display: flex;
    align-items: center;
    justify-content: center;
    position: relative;
    color: #909399;
    transition: all 0.4s;
    overflow: hidden;
}

/* Add a decorative circle behind the icon */
.book-cover-section::before {
    content: '';
    position: absolute;
    width: 120px;
    height: 120px;
    background: rgba(255, 255, 255, 0.5);
    border-radius: 50%;
    transition: transform 0.4s;
}

.book-card:hover .book-cover-section::before {
    transform: scale(1.2);
}

.book-card:hover .book-cover-section {
    background: linear-gradient(135deg, #ecf5ff 0%, #d9ecff 100%);
    color: #409EFF;
}

.book-cover-icon {
    font-size: 64px;
    /* Larger icon */
    position: relative;
    z-index: 1;
    transition: transform 0.4s;
}

.book-card:hover .book-cover-icon {
    transform: scale(1.1) rotate(-5deg);
}

.book-tag {
    position: absolute;
    top: 15px;
    right: 15px;
    background: rgba(255, 255, 255, 0.9);
    color: #409EFF;
    font-size: 12px;
    padding: 4px 10px;
    border-radius: 12px;
    font-weight: 700;
    box-shadow: 0 4px 10px rgba(0, 0, 0, 0.05);
    z-index: 2;
}

.book-content-section {
    flex: 1;
    padding: 24px;
    display: flex;
    flex-direction: column;
    justify-content: space-between;
    background: transparent;
}

.book-name {
    color: #333;
    font-size: 20px;
    font-weight: 700;
    margin: 0 0 8px 0;
    line-height: 1.4;
    display: -webkit-box;
    -webkit-line-clamp: 2;
    line-clamp: 2;
    -webkit-box-orient: vertical;
    overflow: hidden;
    text-align: center;
}

.book-footer {
    display: flex;
    justify-content: center;
    align-items: center;
    margin-top: auto;
    background: #f0f9eb;
    padding: 8px 16px;
    border-radius: 20px;
    color: #67c23a;
    width: fit-content;
    align-self: center;
    transition: all 0.3s;
}

.book-card:hover .book-footer {
    background: #409EFF;
    color: #fff;
    padding: 8px 24px;
}

.start-text {
    font-size: 14px;
    font-weight: 600;
    margin-right: 6px;
}

.action-icon {
    transition: transform 0.2s;
}

.book-card:hover .action-icon {
    transform: translateX(4px);
}

.custom-empty :deep(.el-empty__description p) {
    color: #fff;
    opacity: 0.8;
}

/* 动画效果 */
.fade-slide-enter-active,
.fade-slide-leave-active {
    transition: all 0.5s ease;
}

.fade-slide-enter-from {
    opacity: 0;
    transform: translateY(20px);
}

.fade-slide-leave-to {
    opacity: 0;
    transform: translateY(-20px);
}

/* 依次延迟动画 */
.delay-0 {
    animation: fadeInUp 0.6s ease 0s backwards;
}

.delay-1 {
    animation: fadeInUp 0.6s ease 0.1s backwards;
}

.delay-2 {
    animation: fadeInUp 0.6s ease 0.2s backwards;
}

.delay-3 {
    animation: fadeInUp 0.6s ease 0.3s backwards;
}

@keyframes fadeInUp {
    from {
        opacity: 0;
        transform: translateY(30px);
    }

    to {
        opacity: 1;
        transform: translateY(0);
    }
}
</style>