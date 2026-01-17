<template>
    <div class="topic-drill-container" :style="{ background: currentTheme?.lightBg || '#f8fafc' }">
        <div class="header-glow" :style="{ background: currentTheme?.gradient }"></div>

        <div class="content-wrapper">
            <div class="navbar-container">
                <nav class="subject-navbar">
                    <div class="nav-slider" :style="navSliderStyle"></div>
                    <div v-for="(subject, index) in displaySubjects" :key="subject.id"
                        :ref="el => { if (el) navRefs[index] = el }"
                        class="subject-pill"
                        :class="{ active: activeSubject?.id === subject.id }"
                        @click="handleSubjectChange(subject, index)">
                        <span class="pill-label">{{ subject.name }}</span>
                    </div>
                </nav>
            </div>

            <div class="main-layout">
                <aside class="kb-sidebar" :style="{ borderColor: currentTheme?.borderColor }">
                    <div class="sidebar-header">
                        <h3>知识图谱</h3>
                        <div class="tree-actions">
                            <button class="text-btn" @click="expandAllNodes">全部展开</button>
                            <button class="text-btn" @click="collapseAllNodes">全部收起</button>
                        </div>
                    </div>
                    <div class="tree-viewport">
                        <ModuleNode v-for="node in moduleTree" :key="node.id" :node="node" :depth="0"
                            :selected-id="selectedNode?.id" :theme-color="currentTheme?.color"
                            @node-selected="handleNodeClick" />
                    </div>
                </aside>

                <main class="content-panel" :style="{ borderColor: currentTheme?.borderColor }">
                    <transition name="fade-slide" mode="out-in">
                        <div v-if="selectedNode" :key="selectedNode.id" class="detail-card">
                            <header class="detail-header">
                                <div class="title-section">
                                    <span class="type-tag" :style="{ background: currentTheme?.color }">
                                        {{ moduleTypeMap[selectedNode.type]?.label }}
                                    </span>
                                    <h1>{{ selectedNode.label }}</h1>
                                </div>

                                <div class="quick-stats">
                                    <div class="stat-item">
                                        <span class="label">掌握度</span>
                                        <div class="mini-progress-bar">
                                            <div class="fill"
                                                :style="{ width: (selectedNode.mastery || 0) + '%', background: currentTheme?.gradient }">
                                            </div>
                                        </div>
                                        <span class="value">{{ selectedNode.mastery || 0 }}%</span>
                                    </div>
                                    <div class="stat-item" v-if="selectedNode.examFrequency">
                                        <span class="label">考察热度</span>
                                        <span class="value">{{ selectedNode.examFrequency }}次 / 5年</span>
                                    </div>
                                </div>
                            </header>

                            <section class="info-grid">
                                <div class="info-block solution">
                                    <h3><i class="icon">🧠</i> 解题通法</h3>
                                    <div v-if="selectedNode.solutionPatterns?.length" class="pattern-steps">
                                        <div v-for="(p, i) in selectedNode.solutionPatterns" :key="i" class="step-item">
                                            <span class="step-num" :style="{ borderColor: currentTheme?.color, color: currentTheme?.color }">{{ i + 1 }}</span>
                                            <p>{{ p }}</p>
                                        </div>
                                    </div>
                                    <div v-else class="empty-mini">暂无录入解题模式</div>
                                </div>

                                <div class="info-block mistakes" v-if="selectedNode.commonMistakes?.length">
                                    <h3><i class="icon">⚠️</i> 高频误区</h3>
                                    <ul class="mistake-list">
                                        <li v-for="m in selectedNode.commonMistakes" :key="m">{{ m }}</li>
                                    </ul>
                                </div>
                            </section>
                        </div>

                        <div v-else class="empty-state">
                            <div class="floating-icons">📐 🧪 🖋️</div>
                            <h2>开始你的专项练习</h2>
                            <p>从左侧图谱中选择一个知识模块查看详情</p>
                        </div>
                    </transition>
                </main>
            </div>
        </div>
    </div>
</template>

<script setup>
import { ref, onMounted, defineComponent, h, computed, reactive, nextTick, watch } from 'vue'
import { getExamSpecListApi, getTreeByExamSpecApi } from '@/api/subject'
import { ElMessage } from 'element-plus'

const navRefs = ref([])
const navSliderStyle = reactive({
    width: '0px',
    left: '0px',
    background: ''
})

// 主科目配置（只显示4个主科目）
const MAIN_SUBJECTS = ['政治', '英语', '数学', '408']

// 科目名称映射：将后端返回的细分科目映射到主科目
const SUBJECT_NAME_MAP = {
    '英语一': '英语',
    '英语二': '英语',
    '数学一': '数学',
    '数学二': '数学',
    '数学三': '数学'
}

// 统一颜色配置（根据主科目设置颜色）
const SUBJECT_COLORS = {
    '政治': {
        color: '#ef4444',
        gradient: 'linear-gradient(135deg, #ef4444, #f87171)',
        bgGradient: 'rgba(239, 68, 68, 0.15)',
        lightBg: '#fef2f2',
        borderColor: '#fecaca'
    },
    '408': {
        color: '#3b82f6',
        gradient: 'linear-gradient(135deg, #3b82f6, #60a5fa)',
        bgGradient: 'rgba(59, 130, 246, 0.15)',
        lightBg: '#eff6ff',
        borderColor: '#bfdbfe'
    },
    '英语': {
        color: '#8b5cf6',
        gradient: 'linear-gradient(135deg, #8b5cf6, #a78bfa)',
        bgGradient: 'rgba(139, 92, 246, 0.15)',
        lightBg: '#f5f3ff',
        borderColor: '#ddd6fe'
    },
    '数学': {
        color: '#10b981',
        gradient: 'linear-gradient(135deg, #10b981, #34d399)',
        bgGradient: 'rgba(16, 185, 129, 0.15)',
        lightBg: '#ecfdf5',
        borderColor: '#a7f3d0'
    }
}

/**
 * 局部组件：ModuleNode
 * 负责递归显示知识树条目
 */
const ModuleNode = defineComponent({
    name: 'ModuleNode',
    props: ['node', 'depth', 'selectedId', 'themeColor'],
    emits: ['node-selected'],
    setup(props, { emit }) {
        const isExpanded = ref(props.node.expanded ?? props.depth < 1)

        // 监听外部数据变化（点击“全部展开”时生效）
        watch(() => props.node.expanded, (newVal) => {
            if (newVal !== undefined) isExpanded.value = newVal
        })
        const toggle = (e) => {
            e.stopPropagation()
            isExpanded.value = !isExpanded.value
            props.node.expanded = isExpanded.value
            emit('node-selected', props.node)
        }

        return () => h('div', { class: ['node-wrapper', `depth-${props.depth}`] }, [
            h('div', {
                class: ['node-item', {
                    'is-active': props.selectedId === props.node.id,
                    'has-children': props.node.children?.length > 0
                }],
                style: props.selectedId === props.node.id ? {
                    background: 'var(--node-active-bg, #eff6ff)',
                    color: props.themeColor || '#2563eb'
                } : {},
                onClick: toggle
            }, [
                props.node.children?.length ? h('span', { class: ['arrow', isExpanded.value ? 'down' : 'right'] }, '▶') : h('span', { class: 'dot' }),
                h('span', { class: 'label' }, props.node.label),
                props.node.weight ? h('span', { class: 'weight' }, `${props.node.weight}%`) : null
            ]),
            (isExpanded.value && props.node.children) ? h('div', { class: 'children-container' },
                props.node.children.map(child => h(ModuleNode, {
                    node: child,
                    depth: props.depth + 1,
                    selectedId: props.selectedId,
                    themeColor: props.themeColor,
                    onNodeSelected: (n) => emit('node-selected', n)
                }))
            ) : null
        ])
    }
})

// --- 状态管理 ---
const subjects = ref([]) // 后端返回的原始科目列表
const activeSubject = ref(null) // 当前选中的科目（后端原始数据）
const activeExamSpec = ref(null) // 当前选中的具体考试规格（如"数学一"）
const moduleTree = ref([])
const selectedNode = ref(null)

// 计算属性：处理后的显示科目列表（只显示4个主科目）
const displaySubjects = computed(() => {
    const mainSubjectMap = {}

    subjects.value.forEach(item => {
        // 映射到主科目名称
        const mainName = SUBJECT_NAME_MAP[item.name] || item.name

        // 如果该主科目还不存在，初始化数组
        if (!mainSubjectMap[mainName]) {
            mainSubjectMap[mainName] = {
                id: item.id, // 默认使用第一个的id
                name: mainName, // 使用主科目名称作为显示名称
                examSpecs: [] // 保存该主科目下的所有考试规格
            }
        }

        // 将当前考试规格添加到数组中
        mainSubjectMap[mainName].examSpecs.push(item)
    })

    // 按照 MAIN_SUBJECTS 的顺序返回
    return MAIN_SUBJECTS
        .filter(name => mainSubjectMap[name])
        .map(name => {
            const subject = mainSubjectMap[name]
            return {
                ...subject,
                color: SUBJECT_COLORS[name]?.color,
                gradient: SUBJECT_COLORS[name]?.gradient,
                bgGradient: SUBJECT_COLORS[name]?.bgGradient,
                lightBg: SUBJECT_COLORS[name]?.lightBg,
                borderColor: SUBJECT_COLORS[name]?.borderColor
            }
        })
})

// 计算当前科目的主题色
const currentTheme = computed(() => {
    if (!activeSubject.value) return SUBJECT_COLORS['政治']

    // 获取主科目名称
    const mainName = SUBJECT_NAME_MAP[activeSubject.value.name] || activeSubject.value.name
    return SUBJECT_COLORS[mainName] || SUBJECT_COLORS['政治']
})

const moduleTypeMap = {
    primary: { label: '核心模块', color: '#3b82f6' },
    secondary: { label: '题型群组', color: '#2563eb' },
    tertiary: { label: '知识考点', color: '#1d4ed8' },
    quaternary: { label: '原子考点', color: '#1e40af' }
}

// --- 逻辑处理 ---
// 获取用户选择的考试规格
const getUserExamSpec = (mainSubject, allExamSpecs) => {
    // 从localStorage获取用户信息（注意是'user'而不是'userInfo'）
    const userStr = localStorage.getItem('user') || '{}'
    const userInfo = JSON.parse(userStr)

    console.log('getUserExamSpec - mainSubject:', mainSubject)
    console.log('getUserExamSpec - userInfo:', userInfo)

    const examSubjectsStr = userInfo.examSubjects || ''
    const userSubjects = examSubjectsStr.split(',').map(s => s.trim()).filter(s => s)

    console.log('getUserExamSpec - examSubjectsStr:', examSubjectsStr)
    console.log('getUserExamSpec - userSubjects:', userSubjects)

    // 根据主科目找到用户选择的具体科目
    const userSpecificSubject = userSubjects.find(subject => {
        return SUBJECT_NAME_MAP[subject] === mainSubject
    })

    console.log('getUserExamSpec - userSpecificSubject:', userSpecificSubject)

    if (!userSpecificSubject) {
        return null
    }

    // 从后端返回的所有考试规格中找到匹配的
    const matchedSpec = allExamSpecs.find(spec => {
        return spec.name === userSpecificSubject
    })

    console.log('getUserExamSpec - matchedSpec:', matchedSpec)

    return matchedSpec
}

const fetchExamSpecs = async () => {
    try {
        const res = await getExamSpecListApi()
        if (res.code === 200) {
            subjects.value = res.data
            console.log('subjects:', res.data)
            // 加载完成后初始化第一个科目
            await nextTick()
            if (displaySubjects.value.length > 0) {
                handleSubjectChange(displaySubjects.value[0], 0)
            }
        }
    } catch (e) { ElMessage.error('加载科目失败') }
}

const handleSubjectChange = async (subject, index) => {
    // 保存主科目信息
    activeSubject.value = subject

    let targetExamSpec = null

    if (subject.name === '政治' || subject.name === '408') {
        targetExamSpec = subject.examSpecs[0]
    } else {
        targetExamSpec = getUserExamSpec(subject.name, subjects.value)

        if (!targetExamSpec) {
            ElMessage.warning(`请先在个人信息中选择${subject.name}的考试类型`)
            return
        }
    }

    // 保存当前选中的考试规格
    activeExamSpec.value = targetExamSpec

    try {
        const res = await getTreeByExamSpecApi(targetExamSpec.id)
        if (res.code === 200) {
            moduleTree.value = convertTreeData(res.data)
            selectedNode.value = moduleTree.value[0] || null
        }
    } catch (e) { ElMessage.error('加载图谱失败') }

    // 更新滑块位置
    await nextTick()
    const el = navRefs.value[index]
    if (el) {
        navSliderStyle.width = `${el.offsetWidth}px`
        navSliderStyle.left = `${el.offsetLeft}px`
        navSliderStyle.background = subject.gradient
        navSliderStyle.opacity = '1'
    }
}

const convertTreeData = (nodes) => {
    console.log('convertTreeData - nodes:', nodes)
    return nodes.map(node => ({
        id: node.id,
        label: node.name,
        type: node.level === 1 ? 'primary' : node.level === 2 ? 'secondary' : 'tertiary',
        weight: node.dynamicWeight, // 使用动态权重
        mastery: node.mastery || Math.floor(Math.random() * 100), // 演示用，后端有则取后端
        solutionPatterns: node.solutionPatterns,
        commonMistakes: node.commonMistakes,
        examFrequency: node.examFrequency,
        children: node.children?.length ? convertTreeData(node.children) : []
    }))
}

const handleNodeClick = (node) => { selectedNode.value = node }


// --- 逻辑处理 ---
// 递归处理节点的函数
const toggleTreeNodes = (nodes, status) => {
    nodes.forEach(node => {
        node.expanded = status
        if (node.children && node.children.length > 0) {
            toggleTreeNodes(node.children, status)
        }
    })
}

// 全部展开
const expandAllNodes = () => {
    toggleTreeNodes(moduleTree.value, true)
}

// 全部收起
const collapseAllNodes = () => {
    toggleTreeNodes(moduleTree.value, false)
}

onMounted(fetchExamSpecs)
</script>

<style scoped>
/* 容器与布局 */
.topic-drill-container {
    position: relative;
    height: 100vh;
    overflow: hidden;
    background: #f8fafc;
    padding: 24px;
    display: flex;
    overflow: hidden;
    transition: background 0.6s ease;
    box-sizing: border-box;
}

.header-glow {
    position: absolute;
    top: -100px;
    left: 0;
    width: 100%;
    height: 400px;
    opacity: 0.15;
    filter: blur(80px);
    pointer-events: none;
    transition: background 0.8s ease;
}

.content-wrapper {
    max-width: 1400px;
    width: 100%;
    height: 100%;
    min-height: 0;
    margin: 0 auto;
    display: flex;
    flex-direction: column;
    gap: 24px;
}

/* 外层居中容器 */
.navbar-container {
    display: flex;
    justify-content: center;
    width: 100%;
    flex-shrink: 0;
}

/* 胶囊导航 */
.subject-navbar {
    position: relative;
    display: flex;
    gap: 8px;
    background: rgba(255, 255, 255, 0.85);
    padding: 6px;
    border-radius: 100px;
    backdrop-filter: blur(12px);
    border: 1px solid rgba(255, 255, 255, 0.5);
    box-shadow:
        0 10px 25px -5px rgba(0, 0, 0, 0.05),
        0 8px 10px -6px rgba(0, 0, 0, 0.05);
    z-index: 10;
}

/* 滑块动画效果 */
.nav-slider {
    position: absolute;
    top: 6px;
    height: calc(100% - 12px);
    border-radius: 100px;
    transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
    opacity: 0;
    z-index: 0;
}

.subject-pill {
    position: relative;
    display: flex;
    align-items: center;
    justify-content: center;
    gap: 8px;
    padding: 0 36px;
    border-radius: 100px;
    cursor: pointer;
    transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
    color: #64748b;
    font-weight: 500;
    z-index: 1;
    height: 40px;
    font-size: 1rem;
}

.subject-pill .pill-label {
    line-height: 1;
}

.subject-pill.active {
    color: white;
    font-weight: 600;
}

.main-layout {
    display: grid;
    grid-template-columns: 340px 1fr;
    gap: 24px;
    flex: 1;
    min-height: 0;
}

/* 侧边栏：知识图谱条目 */
.kb-sidebar {
    background: white;
    border-radius: 24px;
    border: 1px solid #e2e8f0;
    display: flex;
    flex-direction: column;
    transition: border-color 0.6s ease;
    height: 100%;
    overflow: hidden;
    min-height: 0;
}

.sidebar-header {
    padding: 20px 24px;
    border-bottom: 1px solid #f1f5f9;
    display: flex;
    justify-content: space-between;
    align-items: center;
}

.sidebar-header h3 {
    font-size: 1.1rem;
    color: #1e293b;
    margin: 0;
}

.text-btn {
    background: none;
    border: none;
    color: #3b82f6;
    font-size: 0.85rem;
    cursor: pointer;
    padding: 4px 8px;
}

.tree-viewport {
    flex: 1;
    overflow-y: auto;
    padding: 16px;
}
/* 针对左侧知识图谱和右侧内容面板设置滚动条 */
.tree-viewport::-webkit-scrollbar,
.content-panel::-webkit-scrollbar {
    width: 6px;
    /* 纵向滚动条宽度 */
    height: 6px;
    /* 横向滚动条高度 */
}

/* 滚动条轨道 */
.tree-viewport::-webkit-scrollbar-track,
.content-panel::-webkit-scrollbar-track {
    background: transparent;
}

/* 滚动条滑块 */
.tree-viewport::-webkit-scrollbar-thumb,
.content-panel::-webkit-scrollbar-thumb {
    background-color: rgba(0, 0, 0, 0.1);
    border-radius: 10px;
    transition: background-color 0.3s;
}

/* 悬浮时滑块颜色 - 这里建议配合你的主题色 */
.tree-viewport::-webkit-scrollbar-thumb:hover,
.content-panel::-webkit-scrollbar-thumb:hover {
    background-color: rgba(37, 99, 235, 0.4);
}

/* 针对 Firefox 的兼容性写法 (可选) */
.tree-viewport,
.content-panel {
    scrollbar-width: thin;
    scrollbar-color: rgba(0, 0, 0, 0.1) transparent;
}

/* 知识树条目美化 */
:deep(.node-wrapper) {
    margin-bottom: 2px;
}

:deep(.node-item) {
    display: flex;
    align-items: center;
    gap: 8px;
    padding: 10px 12px;
    border-radius: 10px;
    cursor: pointer;
    transition: all 0.2s;
    color: #475569;
    font-size: 0.95rem;
}

:deep(.node-item:hover) {
    background: #f1f5f9;
}

:deep(.node-item.is-active) {
    background: #eff6ff;
    color: #2563eb;
    font-weight: 600;
}

:deep(.arrow) {
    font-size: 10px;
    transition: transform 0.2s;
    width: 14px;
}

:deep(.arrow.down) {
    transform: rotate(90deg);
}

:deep(.dot) {
    width: 4px;
    height: 4px;
    background: #cbd5e1;
    border-radius: 50%;
    margin: 0 5px;
}

:deep(.children-container) {
    padding-left: 20px;
    margin-top: 2px;
    border-left: 1px dashed #e2e8f0;
    margin-left: 18px;
}

/* 右侧详情面板 */
.content-panel {
    background: white;
    border-radius: 24px;
    border: 1px solid #e2e8f0;
    overflow-y: auto;
    height: 100%;
    min-height: 0;
    display: flex;
    flex-direction: column;
    transition: border-color 0.6s ease;
}

.detail-card {
    padding: 40px;
}

.detail-header {
    display: flex;
    justify-content: space-between;
    align-items: flex-start;
    margin-bottom: 40px;
}

.type-tag {
    font-size: 0.75rem;
    padding: 4px 12px;
    border-radius: 6px;
    color: white;
    margin-bottom: 12px;
    display: inline-block;
}

.quick-stats {
    display: flex;
    gap: 32px;
}

.stat-item {
    display: flex;
    flex-direction: column;
    gap: 6px;
}

.stat-item .label {
    font-size: 0.85rem;
    color: #94a3b8;
}

.stat-item .value {
    font-weight: 600;
    color: #1e293b;
}

.mini-progress-bar {
    width: 100px;
    height: 6px;
    background: #f1f5f9;
    border-radius: 10px;
    overflow: hidden;
}

.mini-progress-bar .fill {
    height: 100%;
    transition: width 0.6s ease;
}

/* 信息板块 */
.info-grid {
    display: grid;
    gap: 32px;
}

.info-block h3 {
    display: flex;
    align-items: center;
    gap: 10px;
    margin-bottom: 20px;
    font-size: 1.2rem;
}

.step-item {
    display: flex;
    gap: 16px;
    padding: 20px;
    background: #f8fafc;
    border-radius: 16px;
    margin-bottom: 12px;
}

.step-num {
    flex-shrink: 0;
    width: 28px;
    height: 28px;
    background: white;
    border: 2px solid #e2e8f0;
    border-radius: 50%;
    display: flex;
    align-items: center;
    justify-content: center;
    font-weight: bold;
    font-size: 0.9rem;
    transition: all 0.3s ease;
}

.mistake-list {
    padding-left: 20px;
    color: #e11d48;
}

.mistake-list li {
    margin-bottom: 10px;
    line-height: 1.6;
}

/* 状态切换动画 */
.fade-slide-enter-active,
.fade-slide-leave-active {
    transition: all 0.3s;
}

.fade-slide-enter-from {
    opacity: 0;
    transform: translateX(20px);
}

.fade-slide-leave-to {
    opacity: 0;
    transform: translateX(-20px);
}

.empty-state {
    height: 100%;
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    color: #94a3b8;
}

.floating-icons {
    font-size: 3rem;
    margin-bottom: 20px;
    opacity: 0.3;
    animation: float 3s infinite ease-in-out;
}

@keyframes float {

    0%,
    100% {
        transform: translateY(0);
    }

    50% {
        transform: translateY(-10px);
    }
}
</style>

