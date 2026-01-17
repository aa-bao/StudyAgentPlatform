<template>
    <div class="error-book-container">
        <!-- 导航面包屑 (仅在非首页显示) -->
        <div class="nav-header-wrapper" v-if="currentLevel > 0">
            <el-breadcrumb separator-class="el-icon-arrow-right" class="custom-breadcrumb">
                <el-breadcrumb-item>
                    <a @click="handleBreadcrumbClick(0)" class="breadcrumb-link"
                        :class="{ 'is-active': currentLevel === 0 }">错题本首页</a>
                </el-breadcrumb-item>

                <!-- 动态生成中间层级 -->
                <el-breadcrumb-item v-for="(crumb, index) in breadcrumbs" :key="index">
                    <a v-if="index < breadcrumbs.length - 1" @click="handleBreadcrumbClick(crumb.level, crumb.data)"
                        class="breadcrumb-link">
                        {{ crumb.name }}
                    </a>
                    <span v-else class="current-crumb">{{ crumb.name }}</span>
                </el-breadcrumb-item>
            </el-breadcrumb>
        </div>

        <!-- Level 0: 四大一级分类 (全屏四分格布局) -->
        <div v-if="currentLevel === 0" class="fullscreen-categories">
            <div v-for="cat in mainCategories" :key="cat.name" class="hero-card" :class="cat.class"
                @click="handleCategorySelect(cat)">
                <div class="hero-content">
                    <div class="hero-icon-box">
                        <!-- 支持自定义图标：如果有 customIcon 则显示图片，否则显示 Element 图标 -->
                        <img v-if="cat.customIcon" :src="cat.customIcon" class="hero-main-icon custom-img" />
                        <component v-else :is="cat.icon" class="hero-main-icon" />
                    </div>
                    <div class="hero-text">
                        <h1 class="hero-title">{{ cat.name }}</h1>
                        <p class="hero-subtitle">
                            <span class="count">{{ getCategoryErrorCount(cat.name) }}</span> 题待复习
                        </p>
                    </div>
                </div>
                <!-- 背景装饰字 -->
                <div class="hero-bg-text">{{ cat.bgText }}</div>
                <!-- 装饰光效 -->
                <div class="hero-glow"></div>
            </div>
        </div>

        <!-- Level 1: 详细知识点 (Level 2) -->
        <div v-else-if="currentLevel === 1" class="level-container content-level">
            <div class="level-header">
                <el-button @click="resetLevel" circle icon="Back" class="back-btn" />
                <div class="section-title" style="margin-bottom: 0; border: none;">{{ selectedCategory.name }} - 知识点专项
                </div>
            </div>

            <el-empty v-if="topicList.length === 0" description="太棒了！该科目下暂无错题，继续保持！" />

            <div class="card-grid" v-else>
                <div v-for="topic in topicList" :key="topic.name" class="grid-card topic-card"
                    @click="handleTopicSelect(topic)">
                    <div class="topic-main">
                        <div class="topic-icon-small">
                            <el-icon>
                                <CollectionTag />
                            </el-icon>
                        </div>
                        <span class="topic-name">{{ topic.name }}</span>
                    </div>
                    <div class="topic-footer">
                        <span class="topic-count">{{ topic.count }} 道错题</span>
                        <el-icon class="arrow-icon">
                            <Right />
                        </el-icon>
                    </div>
                </div>
            </div>
        </div>

        <!-- Level 2: 错题列表 (Level 3) -->
        <div v-else-if="currentLevel === 2" class="content-level">
            <div class="level-header-row">
                <div class="header-title-group">
                    <el-button @click="currentLevel = 1" circle icon="Back" class="back-btn-modern" />
                    <span class="page-title">{{ selectedTopic.name }}</span>
                    <el-tag type="info" effect="plain" round size="small" class="count-tag">{{ filteredErrorList.length
                        }} 题</el-tag>
                </div>
                <el-button type="primary" plain round size="default" @click="exportErrorBook" class="export-btn">
                    <el-icon style="margin-right: 5px">
                        <Download />
                    </el-icon>导出本章错题
                </el-button>
            </div>

            <el-empty v-if="filteredErrorList.length === 0" description="恭喜！该知识点下的错题已全部攻克" />

            <div v-else class="error-items-wrapper">
                <div v-for="(item, index) in filteredErrorList" :key="item.id" class="error-item-card">
                    <div class="error-header">
                        <div class="error-badges">
                            <el-tag type="danger" effect="dark" class="error-tag">错题 {{ index + 1 }}</el-tag>
                            <el-tag type="info" effect="plain" size="small">{{ item.subjectName }}</el-tag>
                        </div>
                        <span class="error-date">最近错误: {{ formatDate(item.createTime) }}</span>
                    </div>

                    <div class="error-content" v-html="renderLatex(item.content)"></div>

                    <el-collapse class="minimal-collapse">
                        <el-collapse-item name="1">
                            <template #title>
                                <span class="collapse-title">
                                    <el-icon>
                                        <View />
                                    </el-icon> 查看解析与答案
                                </span>
                            </template>
                            <div class="detail-box">
                                <div class="detail-section options-section">
                                    <span class="section-label">选项：</span>
                                    <div class="options-grid">
                                        <div v-for="(opt, idx) in parseOptions(item.options)" :key="idx"
                                            class="option-item-row">
                                            <span class="opt-index">{{ String.fromCharCode(65 + idx) }}.</span>
                                            <div class="opt-content" v-html="renderLatex(formatOptionContent(opt))">
                                            </div>
                                        </div>
                                    </div>
                                </div>

                                <div class="detail-section answer-section">
                                    <span class="section-label">正确答案：</span>
                                    <span class="correct-badge">{{ item.answer }}</span>
                                </div>

                                <div class="detail-section analysis-section">
                                    <span class="section-label">解析：</span>
                                    <div class="analysis-content" v-html="renderLatex(item.analysis)"></div>
                                </div>
                            </div>
                        </el-collapse-item>
                    </el-collapse>
                </div>
            </div>
        </div>
    </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import request from '@/utils/request'
import * as XLSX from 'xlsx'
import { ElMessage } from 'element-plus'
import katex from 'katex'

// 导入自定义图标
import politicsIcon from '@/assets/icons/politics.svg?url'
import englishIcon from '@/assets/icons/english.svg?url'
import mathIcon from '@/assets/icons/math_1.svg?url'
import csIcon from '@/assets/icons/408.svg?url'

// 状态管理
const currentLevel = ref(0) // 0:首页分类, 1:知识点列表, 2:题目列表
const allErrors = ref([]) // 所有错题数据
const rawSubjects = ref([]) // 从后端获取的原始科目列表

// 选中项
const selectedCategory = ref(null)
const selectedTopic = ref(null)

// 动态计算科目元数据与映射
const subjectMeta = computed(() => {
    if (rawSubjects.value.length === 0) return []

    return rawSubjects.value.map(s => {
        let category = '其他'
        const name = s.name

        // 1. 基于名称的启发式分类
        if (name.includes('政治') || name.includes('马原') || name.includes('毛中特') || name.includes('史纲') || name.includes('思修')) {
            category = '政治'
        } else if (name.includes('英语')) {
            category = '英语'
        } else if (name.includes('数学') || name.includes('代数') || name.includes('概率') || name.includes('微积分')) {
            category = '数学'
        } else if (['操作系统', '数据结构', '计算机网络', '计算机组成原理', '408'].some(k => name.includes(k))) {
            category = '408专业课'
        } else if (s.parentId) {
            // 2. 基于父级科目的分类
            const parent = rawSubjects.value.find(p => p.id === s.parentId)
            if (parent) {
                const pName = parent.name
                if (pName.includes('政治')) category = '政治'
                else if (pName.includes('英语')) category = '英语'
                else if (pName.includes('数学')) category = '数学'
                else if (pName.includes('408') || pName.includes('计算机')) category = '408专业课'
            }
        }

        return {
            id: s.id,
            name: s.name,
            category: category
        }
    })
})

// Level 0: 固定四大分类
const mainCategories = [
    { name: '政治', icon: 'Flag', class: 'cat-politics', bgText: '⚖', customIcon: politicsIcon },
    { name: '英语', icon: 'Reading', class: 'cat-english', bgText: 'ENG', customIcon: englishIcon },
    { name: '数学', icon: 'Histogram', class: 'cat-math', bgText: '∑ ∞ π ∫', customIcon: mathIcon },
    { name: '408专业课', icon: 'Cpu', class: 'cat-408', bgText: 'C0DE', customIcon: csIcon }
]

// 获取某个一级分类下的总错题数
const getCategoryErrorCount = (categoryName) => {
    // 找到该分类下的所有 subjectIds
    const subjectIds = subjectMeta.value
        .filter(s => s.category === categoryName)
        .map(s => s.id)

    return allErrors.value.filter(q => subjectIds.includes(q.subjectId)).length
}

// Level 1: 计算当前分类下的知识点列表 (动态)
const topicList = computed(() => {
    if (!selectedCategory.value) return []

    // 1. 筛选出属于当前大类的错题
    const targetSubjectIds = subjectMeta.value
        .filter(s => s.category === selectedCategory.value.name)
        .map(s => s.id)

    const categoryErrors = allErrors.value.filter(q => targetSubjectIds.includes(q.subjectId))

    // 2. 按知识点分组
    const groups = {}

    categoryErrors.forEach(q => {
        let topicName = '综合知识点'

        // 策略：优先使用 tags[0]，如果没有 tags，则使用 '未分类知识点'
        if (q.tags && q.tags.length > 0) {
            topicName = q.tags[0]
        } else {
            // 尝试使用科目名称作为备选 topic
            const sub = subjectMeta.value.find(s => s.id === q.subjectId)
            topicName = sub ? sub.name : '未分类知识点'
        }

        if (!groups[topicName]) groups[topicName] = 0
        groups[topicName]++
    })

    return Object.keys(groups).map(name => ({
        name: name,
        count: groups[name]
    })).sort((a, b) => b.count - a.count) // 按错题数量降序
})

// Level 2: 过滤出错题列表
const filteredErrorList = computed(() => {
    if (!selectedCategory.value || !selectedTopic.value) return []

    const targetSubjectIds = subjectMeta.value
        .filter(s => s.category === selectedCategory.value.name)
        .map(s => s.id)

    return allErrors.value.filter(q => {
        // 1. 必须属于当前大类
        if (!targetSubjectIds.includes(q.subjectId)) return false

        // 2. 必须属于当前知识点 (逻辑同 topicList 构建)
        let topicName = '综合知识点'
        if (q.tags && q.tags.length > 0) {
            topicName = q.tags[0]
        } else {
            const sub = subjectMeta.value.find(s => s.id === q.subjectId)
            topicName = sub ? sub.name : '未分类知识点'
        }

        return topicName === selectedTopic.value.name
    }).map(item => {
        // 为 item 补充 subjectName 用于显示
        const sub = subjectMeta.value.find(s => s.id === item.subjectId)
        return {
            ...item,
            subjectName: sub ? sub.name : '未知科目'
        }
    })
})

const loadSubjects = async () => {
    try {
        const res = await request.get('/subject/list')
        if (res.code === 200) {
            rawSubjects.value = res.data
        }
    } catch (e) {
        console.error('Failed to load subjects', e)
    }
}

const loadErrorBook = async () => {
    const userStr = localStorage.getItem('user')
    if (!userStr) return
    const user = JSON.parse(userStr)

    const res = await request.get('/question/getErrorBook', {
        params: { userId: user.id }
    })
    allErrors.value = res.data.map(item => ({
        ...item,
        tags: typeof item.tags === 'string' ? JSON.parse(item.tags) : item.tags
    }))
}

const handleCategorySelect = (cat) => {
    selectedCategory.value = cat
    currentLevel.value = 1
}

const handleTopicSelect = (topic) => {
    selectedTopic.value = topic
    currentLevel.value = 2
}

const resetLevel = () => {
    currentLevel.value = 0
    selectedCategory.value = null
    selectedTopic.value = null
}

// 面包屑导航计算属性
const breadcrumbs = computed(() => {
    const crumbs = []
    if (currentLevel.value >= 1 && selectedCategory.value) {
        crumbs.push({
            name: selectedCategory.value.name,
            level: 1,
            data: selectedCategory.value
        })
    }
    if (currentLevel.value >= 2 && selectedTopic.value) {
        crumbs.push({
            name: selectedTopic.value.name,
            level: 2,
            data: selectedTopic.value
        })
    }
    // 如果后续有更深层级，可以在这里继续添加
    return crumbs
})

const handleBreadcrumbClick = (level, data) => {
    if (level === 0) {
        resetLevel()
    } else if (level === 1) {
        // 返回到知识点列表层级
        currentLevel.value = 1
        selectedCategory.value = data
        selectedTopic.value = null
    }
    // 可以扩展更多层级的跳转逻辑
}

// 解析选项字符串为数组
const parseOptions = (optionsStr) => {
    if (!optionsStr) return []
    try {
        if (Array.isArray(optionsStr)) return optionsStr
        return JSON.parse(optionsStr)
    } catch (e) {
        return []
    }
}

// 提取选项内容（去除 A. B. 等前缀，因为前端重新渲染了）
const formatOptionContent = (optStr) => {
    if (!optStr) return ''
    // 匹配并移除开头的 "A. " 或 "A " 等
    return optStr.replace(/^[A-Z][\.\s]\s*/, '')
}

const formatDate = (dateStr) => {
    if (!dateStr) return '未知时间'
    const date = new Date(dateStr)
    return date.toLocaleString()
}

const exportErrorBook = () => {
    if (filteredErrorList.value.length === 0) {
        ElMessage.warning("没有错题可以导出")
        return
    }

    const data = filteredErrorList.value.map((item, index) => ({
        "序号": index + 1,
        "科目": item.subjectName,
        "知识点": selectedTopic.value.name,
        "题目内容": item.content,
        "正确答案": item.answer,
        "题目解析": item.analysis || "暂无解析",
        "错题时间": formatDate(item.createTime)
    }))

    const ws = XLSX.utils.json_to_sheet(data)
    const wb = XLSX.utils.book_new()
    XLSX.utils.book_append_sheet(wb, ws, "错题本")
    XLSX.writeFile(wb, `错题集_${selectedTopic.value.name}.xlsx`)
}

// Latex 渲染函数
const renderLatex = (content) => {
    if (!content) return ''
    return content.replace(/\$([^$]+)\$/g, (match, tex) => {
        try {
            return katex.renderToString(tex, {
                throwOnError: false,
                displayMode: false
            })
        } catch (err) {
            return match
        }
    }).replace(/\$\$([^$]+)\$\$/g, (match, tex) => {
        try {
            return katex.renderToString(tex, {
                throwOnError: false,
                displayMode: true
            })
        } catch (err) {
            return match
        }
    })
}

onMounted(() => {
    loadSubjects()
    loadErrorBook()
})
</script>

<style scoped>
.error-book-container {
    height: 100%;
    display: flex;
    flex-direction: column;
}

/* 首页全屏四分格布局 */
.fullscreen-categories {
    flex: 1;
    display: grid;
    grid-template-columns: 1fr 1fr;
    grid-template-rows: 1fr 1fr;
    gap: 0;
    padding: 0;
    height: calc(100vh - 60px);
}

.hero-card {
    position: relative;
    border-radius: 0;
    overflow: hidden;
    cursor: pointer;
    transition: all 0.4s cubic-bezier(0.25, 0.8, 0.25, 1);
    display: flex;
    flex-direction: column;
    justify-content: center;
    align-items: center;
    color: #fff;
}

/* 动画定义 */
@keyframes sway {
    0% {
        transform: translateX(0) rotate(0);
    }

    25% {
        transform: translateX(-5px) rotate(-5deg);
    }

    50% {
        transform: translateX(5px) rotate(5deg);
    }

    75% {
        transform: translateX(-5px) rotate(-5deg);
    }

    100% {
        transform: translateX(0) rotate(0);
    }
}

.hero-card:hover {
    z-index: 10;
    box-shadow: 0 0 40px rgba(0, 0, 0, 0.3);
}

/* 装饰背景字 */
.hero-bg-text {
    position: absolute;
    bottom: 20%;
    left: 55%;
    transform: translateX(-50%) rotate(-15deg);
    width: 100%;
    text-align: center;
    font-size: 10rem;
    font-weight: 900;
    opacity: 0.15;
    font-family: 'Arial Black', sans-serif;
    line-height: 1;
    pointer-events: none;
    transition: all 0.5s ease;
    white-space: nowrap;
    overflow: visible;
}

.hero-card:hover .hero-bg-text {
    opacity: 0.25;
    animation: sway-center-tilted 1.2s ease-in-out infinite reverse;
}

/* 针对倾斜居中元素的晃动动画 */
@keyframes sway-center-tilted {
    0% {
        transform: translateX(-50%) rotate(-15deg);
    }

    25% {
        transform: translateX(calc(-50% - 5px)) rotate(-20deg);
    }

    50% {
        transform: translateX(calc(-50% + 5px)) rotate(-10deg);
    }

    75% {
        transform: translateX(calc(-50% - 5px)) rotate(-20deg);
    }

    100% {
        transform: translateX(-50%) rotate(-15deg);
    }
}

.hero-content {
    z-index: 2;
    text-align: center;
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: 20px;
}

.hero-icon-box {
    background: rgba(255, 255, 255, 0.2);
    width: 100px;
    height: 100px;
    border-radius: 50%;
    display: flex;
    align-items: center;
    justify-content: center;
    backdrop-filter: blur(5px);
    transition: transform 0.4s ease;
}

.hero-main-icon {
    width: 50px;
    height: 50px;
    color: #fff;
}

.hero-card:hover .hero-icon-box {
    /* 移除之前的 scale 和 rotate */
    /* transform: scale(1.1) rotate(10deg); */
    background: rgba(255, 255, 255, 0.3);
    animation: sway 0.8s ease-in-out infinite;
}

.hero-main-icon.custom-img {
    width: 60%;
    height: 60%;
    object-fit: contain;
    padding: 0;
    opacity: 0.85;
}

.hero-text {
    text-shadow: 0 2px 4px rgba(0, 0, 0, 0.2);
}

.hero-title {
    font-size: 3rem;
    margin: 0;
    font-weight: 800;
    letter-spacing: 2px;
}

.hero-subtitle {
    margin-top: 10px;
    font-size: 1.2rem;
    opacity: 0.9;
    font-weight: 500;
}

.count {
    font-size: 1.5em;
    font-weight: bold;
    color: #fff;
    text-decoration: underline;
    text-underline-offset: 4px;
}

/* 颜色定义 */
.cat-politics {
    background: linear-gradient(135deg, #ff4757 0%, #ff6b81 100%);
}

.cat-english {
    background: linear-gradient(135deg, #3742fa 0%, #5352ed 100%);
}

.cat-math {
    background: linear-gradient(135deg, #2ed573 0%, #7bed9f 100%);
}

.cat-408 {
    background: linear-gradient(135deg, #ffa502 0%, #eccc68 100%);
}

/* 导航条样式美化 */
.nav-header-wrapper {
    background: #fff;
    padding: 16px 32px;
    border-bottom: 1px solid #eef0f5;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.02);
    margin-bottom: 24px;
}

.custom-breadcrumb {
    font-size: 14px;
}

:deep(.el-breadcrumb__inner a),
:deep(.el-breadcrumb__inner.is-link) {
    font-weight: normal;
    color: #606266;
    transition: color 0.2s;
}

:deep(.el-breadcrumb__inner a:hover),
:deep(.el-breadcrumb__inner.is-link:hover) {
    color: #409EFF;
}

:deep(.el-breadcrumb__item:last-child .el-breadcrumb__inner) {
    font-weight: 600;
    color: #303133;
}

/* Level Header 美化 */
.level-header-row {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 24px;
    padding: 8px 0;
}

.header-title-group {
    display: flex;
    align-items: center;
    gap: 16px;
}

.page-title {
    font-size: 24px;
    font-weight: 800;
    color: #303133;
    letter-spacing: 0.5px;
}

.back-btn-modern {
    border: 1px solid #e4e7ed;
    color: #606266;
    transition: all 0.3s;
}

.back-btn-modern:hover {
    border-color: #409EFF;
    color: #409EFF;
    background-color: #ecf5ff;
}

.count-tag {
    font-weight: 600;
}

.export-btn {
    box-shadow: 0 4px 12px rgba(64, 158, 255, 0.2);
    transition: all 0.3s;
}

.export-btn:hover {
    transform: translateY(-2px);
    box-shadow: 0 6px 16px rgba(64, 158, 255, 0.3);
}

/* 内部内容页面的容器 */
.content-level {
    padding: 0 32px 40px;
    max-width: 1200px;
    margin: 0 auto;
    width: 100%;
    box-sizing: border-box;
}

/* Error Items 优化 */
.error-items-wrapper {
    display: flex;
    flex-direction: column;
    gap: 24px;
}

.error-item-card {
    border: none;
    border-radius: 16px;
    padding: 32px;
    transition: all 0.3s;
    background: #fff;
    box-shadow: 0 4px 20px rgba(0, 0, 0, 0.03);
}

.error-item-card:hover {
    box-shadow: 0 8px 30px rgba(0, 0, 0, 0.08);
    transform: translateY(-2px);
}

.card-grid {
    display: grid;
    grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
    gap: 20px;
    padding: 10px 0;
}

.topic-card {
    background: #fff;
    border-radius: 16px;
    padding: 24px;
    cursor: pointer;
    border: 1px solid #ebeef5;
    transition: all 0.3s;
    display: flex;
    flex-direction: column;
    justify-content: space-between;
    height: 120px;
    box-shadow: 0 4px 6px rgba(0, 0, 0, 0.02);
}

.topic-card:hover {
    border-color: #409EFF;
    transform: translateY(-5px);
    box-shadow: 0 10px 20px rgba(64, 158, 255, 0.1);
}

.topic-main {
    display: flex;
    align-items: center;
    gap: 12px;
}

.topic-icon-small {
    width: 40px;
    height: 40px;
    border-radius: 10px;
    background: #ecf5ff;
    color: #409EFF;
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 20px;
}

.topic-name {
    font-size: 16px;
    font-weight: bold;
    color: #303133;
    flex: 1;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
}

.topic-footer {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding-top: 16px;
    border-top: 1px dashed #ebeef5;
    margin-top: auto;
}

.topic-count {
    font-size: 13px;
    color: #909399;
}

.arrow-icon {
    color: #c0c4cc;
    transition: transform 0.3s;
}

.topic-card:hover .arrow-icon {
    transform: translateX(4px);
    color: #409EFF;
}

/* Headers & Utils */
.level-header {
    display: flex;
    align-items: center;
    gap: 16px;
    margin-bottom: 24px;
}

.back-btn {
    border: none;
    background-color: #f0f2f5;
    color: #606266;
    transition: all 0.3s;
}

.back-btn:hover {
    background-color: #e6e8eb;
    color: #409EFF;
}

.section-title {
    font-size: 20px;
    font-weight: 800;
    color: #303133;
}

/* 错题列表项 */
.error-items-wrapper {
    display: flex;
    flex-direction: column;
    gap: 24px;
}

.error-item-card {
    border: 1px solid #ebeef5;
    border-radius: 12px;
    padding: 24px;
    transition: all 0.3s;
    background: #fff;
}

.error-item-card:hover {
    box-shadow: 0 8px 24px rgba(0, 0, 0, 0.05);
    border-color: #c6e2ff;
}

.error-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 16px;
}

.error-badges {
    display: flex;
    align-items: center;
    gap: 12px;
}

.error-date {
    font-size: 12px;
    color: #909399;
}

.error-content {
    font-size: 16px;
    color: #303133;
    line-height: 1.7;
    margin-bottom: 20px;
    background: #f8f9fa;
    padding: 16px;
    border-radius: 8px;
}

/* 错题详情折叠面板样式 */
.minimal-collapse {
    margin-top: 16px;
    border: none;
    --el-collapse-header-bg-color: transparent;
    --el-collapse-content-bg-color: #fcfcfc;
}

:deep(.el-collapse-item__header) {
    border: none;
    height: 40px;
    line-height: 40px;
}

:deep(.el-collapse-item__wrap) {
    border: none;
    background-color: #fafafa;
    border-radius: 8px;
}

:deep(.el-collapse-item__content) {
    padding: 24px;
}

.collapse-title {
    color: #409EFF;
    font-size: 14px;
    display: flex;
    align-items: center;
    gap: 6px;
    cursor: pointer;
    font-weight: 500;
}

/* 详情盒子布局 */
.detail-box {
    display: flex;
    flex-direction: column;
    gap: 24px;
}

.detail-section {
    display: flex;
    flex-direction: column;
    gap: 12px;
}

.section-label {
    font-size: 14px;
    color: #909399;
    font-weight: 600;
    margin-bottom: 4px;
    display: block;
}

/* 选项网格布局 - 四行显示 */
.options-grid {
    display: flex;
    flex-direction: column;
    gap: 12px;
    width: 100%;
}

.option-item-row {
    background: #fff;
    border: 1px solid #eef0f5;
    border-radius: 8px;
    padding: 12px 16px;
    display: flex;
    align-items: flex-start;
    gap: 12px;
    transition: all 0.2s;
}

.option-item-row:hover {
    border-color: #c6e2ff;
    background: #ecf5ff;
}

.opt-index {
    font-weight: 700;
    color: #409EFF;
    font-family: 'Arial', sans-serif;
    margin-top: 2px;
    /* 微调对齐 */
}

.opt-content {
    color: #303133;
    font-size: 15px;
    line-height: 1.6;
    flex: 1;
}

/* 正确答案徽章 */
.correct-badge {
    display: inline-block;
    background-color: #f0f9eb;
    color: #67c23a;
    border: 1px solid #e1f3d8;
    padding: 4px 16px;
    border-radius: 4px;
    font-weight: 700;
    font-size: 16px;
    width: fit-content;
}

/* 解析内容 */
.analysis-content {
    color: #606266;
    line-height: 1.8;
    font-size: 15px;
    background: #fff;
    padding: 16px;
    border-radius: 8px;
    border: 1px dashed #dcdfe6;
}

/* 题目和选项中的 Latex 样式调整 */
:deep(.katex) {
    font-size: 1.15em;
    line-height: 1.2;
}

:deep(.katex-display) {
    margin: 0.8em 0;
    overflow-x: auto;
    overflow-y: hidden;
    padding: 4px 0;
}

/* 题干样式微调 */
.error-content,
.val {
    line-height: 1.8;
    font-size: 16px;
}
</style>