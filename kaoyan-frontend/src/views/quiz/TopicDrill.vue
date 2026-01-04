<template>
    <div class="knowledge-practice-container">
        <el-container style="height: calc(100vh - 80px);">
            <!-- Left Side: Knowledge Tree -->
            <el-aside width="320px" class="tree-aside">
                <div class="aside-header">
                    <h3><el-icon><Files /></el-icon> 知识点目录</h3>
                </div>
                <el-scrollbar>
                    <el-tree :data="treeData" :props="defaultProps" @node-click="handleNodeClick" node-key="id"
                        highlight-current :expand-on-click-node="false" default-expand-all :indent="16">
                        <template #default="{ node, data }">
                            <div class="custom-tree-node">
                                <span class="node-label" :title="node.label">{{ node.label }}</span>
                                <span class="node-stat" v-if="data.questionCount > 0">
                                    <el-tag size="small" :type="data.finishedCount > 0 ? 'success' : 'info'" effect="plain" round>
                                        {{ data.finishedCount }}/{{ data.questionCount }}
                                    </el-tag>
                                </span>
                            </div>
                        </template>
                    </el-tree>
                </el-scrollbar>
            </el-aside>

            <!-- Right Side: Question List -->
            <el-main class="questions-main" v-loading="loading">
                <div v-if="!currentSubject" class="empty-state">
                    <el-empty description="请在左侧选择一个知识点开始刷题" image-size="200">
                        <template #extra>
                            <p class="empty-tip">支持按考点、章节筛选题目，精准突破薄弱环节</p>
                        </template>
                    </el-empty>
                </div>
                <div v-else class="content-wrapper">
                    <div class="subject-header">
                        <div class="header-left">
                            <h2>{{ currentSubject.name }}</h2>
                            <el-tag effect="dark" type="primary" size="large">{{ questions.length }} 道题</el-tag>
                        </div>
                        <div class="header-right">
                            <el-progress :percentage="calculateProgress()" :stroke-width="10" :width="150" type="line" />
                            <span class="progress-text">本节进度</span>
                        </div>
                    </div>

                    <div v-if="questions.length === 0" class="empty-questions">
                        <el-empty description="该知识点下暂无题目" />
                    </div>

                    <div class="question-list">
                        <div v-for="(q, index) in questions" :key="q.id" class="question-card" :id="'q-' + q.id">
                            <div class="q-header-row">
                                <div class="q-tag-group">
                                    <span class="q-index">#{{ index + 1 }}</span>
                                    <el-tag size="small" :type="getTypeColor(q.type)">{{ getTypeName(q.type) }}</el-tag>
                                    <el-tag v-if="q.difficulty" size="small" type="warning" effect="plain">难度: {{ q.difficulty }}</el-tag>
                                </div>
                                <div class="q-actions">
                                     <!-- Placeholder for future actions like bookmark -->
                                </div>
                            </div>

                            <div class="q-content" v-html="renderLatex(q.content)"></div>

                            <div class="q-options">
                                <div v-for="(opt, oIndex) in parseOptions(q.options)" :key="oIndex"
                                    class="option-item"
                                    :class="getOptionClass(q, opt)"
                                    @click="selectOption(q, opt)">
                                    <span class="opt-prefix">{{ getOptionLetter(opt) }}</span>
                                    <span class="opt-text" v-html="renderLatex(getOptionContent(opt))"></span>
                                    
                                    <el-icon v-if="q.isSubmitted && getOptionLetter(opt) === q.answer" class="status-icon correct"><Select /></el-icon>
                                    <el-icon v-if="q.isSubmitted && getOptionLetter(opt) === q.userSelected && q.userSelected !== q.answer" class="status-icon wrong"><CloseBold /></el-icon>
                                </div>
                            </div>

                            <div class="q-footer">
                                <div class="footer-left">
                                    <el-button type="primary" 
                                        :disabled="q.isSubmitted || !q.userSelected"
                                        @click="submitAnswer(q)" round>
                                        提交答案
                                    </el-button>
                                    <el-button v-if="q.isSubmitted" @click="q.showAnalysis = !q.showAnalysis" plain round>
                                        {{ q.showAnalysis ? '收起解析' : '查看解析' }}
                                    </el-button>
                                </div>
                                
                                <div v-if="q.isSubmitted" class="result-box" :class="q.isCorrect ? 'correct' : 'wrong'">
                                    <el-icon><component :is="q.isCorrect ? 'Select' : 'CloseBold'" /></el-icon>
                                    <span v-if="q.isCorrect"> 回答正确</span>
                                    <span v-else> 回答错误，正确答案: <strong>{{ q.answer }}</strong></span>
                                </div>
                            </div>
                            
                            <el-collapse-transition>
                                <div v-if="q.isSubmitted && (q.showAnalysis || !q.isCorrect)" class="analysis-wrapper">
                                    <div class="analysis-box">
                                        <div class="analysis-title"><el-icon><Reading /></el-icon> 解析：</div>
                                        <div class="analysis-content" v-html="renderLatex(q.analysis || '暂无解析')"></div>
                                    </div>
                                </div>
                            </el-collapse-transition>
                        </div>
                    </div>
                </div>
            </el-main>
        </el-container>
    </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import request from '@/utils/request'
import { ElMessage } from 'element-plus'
import { Files, Select, CloseBold, Reading } from '@element-plus/icons-vue'
import katex from 'katex'
import 'katex/dist/katex.min.css'

const treeData = ref([])
const defaultProps = {
    children: 'children',
    label: 'name'
}
const loading = ref(false)
const currentSubject = ref(null)
const questions = ref([])
const userInfo = JSON.parse(localStorage.getItem('user') || '{}')

const loadTree = async () => {
    try {
        const res = await request.get('/subject/tree', {
            params: { userId: userInfo.id }
        })
        treeData.value = res.data
    } catch (error) {
        console.error(error)
    }
}

const handleNodeClick = async (data) => {
    currentSubject.value = data
    loading.value = true
    try {
        const res = await request.get('/question/list-by-knowledge-point', {
            params: { subjectId: data.id }
        })
        questions.value = res.data.map(q => ({
            ...q,
            userSelected: '',
            isSubmitted: false,
            isCorrect: false,
            showAnalysis: false
        }))
    } catch (error) {
        console.error(error)
    } finally {
        loading.value = false
    }
}

const calculateProgress = () => {
    if (!questions.value.length) return 0;
    const submitted = questions.value.filter(q => q.isSubmitted).length;
    return Math.round((submitted / questions.value.length) * 100);
}

const parseOptions = (opts) => {
    if (!opts) return []
    try {
        const parsed = typeof opts === 'string' ? JSON.parse(opts) : opts
        // Sometimes options might be ["A. xxx", "B. xxx"] or just ["xxx", "yyy"]
        // We handle standard format here
        return parsed
    } catch (e) {
        return []
    }
}

const getOptionLetter = (opt) => {
    // Check if opt starts with A., B., etc.
    if (/^[A-Z][.、]/.test(opt)) {
        return opt.charAt(0)
    }
    // Fallback logic if needed, but for now assume standard format or handle index in loop
    return opt.charAt(0) // Simplistic fallback
}

const getOptionContent = (opt) => {
    // Remove "A. " prefix
    return opt.replace(/^[A-Z][.、\s]\s*/, '')
}

const getTypeName = (t) => ({ 1: '单选', 2: '多选', 3: '填空', 4: '简答' }[t] || '题目')
const getTypeColor = (t) => ({ 1: '', 2: 'success', 3: 'warning', 4: 'info' }[t] || '')

const selectOption = (q, opt) => {
    if (q.isSubmitted) return
    const letter = getOptionLetter(opt)
    
    if (q.type === 2) { // 多选
        let current = q.userSelected ? q.userSelected.split('') : []
        if (current.includes(letter)) {
            current = current.filter(l => l !== letter)
        } else {
            current.push(letter)
        }
        q.userSelected = current.sort().join('')
    } else { // 单选/判断
        q.userSelected = letter
    }
}

const getOptionClass = (q, opt) => {
    const letter = getOptionLetter(opt)
    if (q.isSubmitted) {
        // 对于多选，判断是否包含
        const isAnswer = q.answer.includes(letter)
        const isSelected = q.userSelected.includes(letter)
        
        if (isAnswer) return 'correct-opt'
        if (isSelected && !isAnswer) return 'wrong-opt'
    } else {
        if (q.userSelected && q.userSelected.includes(letter)) return 'selected-opt'
    }
    return ''
}

const submitAnswer = async (q) => {
    if (!q.userSelected || (Array.isArray(q.userSelected) && q.userSelected.length === 0)) return
    
    // Format answer for backend
    let answerStr = q.userSelected
    if (Array.isArray(q.userSelected)) {
        answerStr = q.userSelected.sort().join('') // "AB"
    }
    
    // Optimistic update
    // Note: Backend stores answer usually as "A" or "A,B". Need to check backend expectation.
    // RecordController: String userAns = examRecord.getUserAnswer().replaceAll("[,\\s]", "").toUpperCase();
    // So "AB" is fine.
    
    const dbAns = q.answer.replaceAll(/[,\\s]/g, "").toUpperCase();
    const isRight = answerStr === dbAns
    
    q.isCorrect = isRight
    q.isSubmitted = true
    
    try {
        await request.post('/record/submit', {
            userId: userInfo.id,
            questionId: q.id,
            userAnswer: answerStr
        })
        
        // Refresh tree counts? 
        // We can manually update local tree data finishedCount
        if (currentSubject.value) {
            currentSubject.value.finishedCount = (currentSubject.value.finishedCount || 0) + 1
        }
    } catch (error) {
        ElMessage.error('提交失败')
        q.isSubmitted = false
    }
}

// Latex Render
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
    loadTree()
})
</script>

<style scoped>
.knowledge-practice-container {
    background: #f5f7fa;
    height: 100%;
}

.tree-aside {
    background: white;
    border-right: 1px solid #e6e6e6;
    display: flex;
    flex-direction: column;
}

.aside-header {
    padding: 16px 20px;
    border-bottom: 1px solid #eee;
    background: #fff;
}

.aside-header h3 {
    margin: 0;
    font-size: 16px;
    color: #303133;
    display: flex;
    align-items: center;
    gap: 8px;
}

.custom-tree-node {
    display: flex;
    justify-content: space-between;
    align-items: center;
    width: 100%;
    padding-right: 10px;
    font-size: 14px;
}

.node-label {
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
    margin-right: 8px;
    flex: 1;
}

.questions-main {
    padding: 20px 30px;
    overflow-y: auto;
    background: #f5f7fa;
}

.content-wrapper {
    max-width: 1000px;
    margin: 0 auto;
}

.subject-header {
    margin-bottom: 24px;
    display: flex;
    align-items: center;
    justify-content: space-between;
    background: #fff;
    padding: 20px;
    border-radius: 8px;
    box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.05);
}

.header-left {
    display: flex;
    align-items: center;
    gap: 16px;
}

.header-left h2 {
    margin: 0;
    font-size: 20px;
    color: #303133;
}

.header-right {
    display: flex;
    flex-direction: column;
    align-items: flex-end;
    gap: 4px;
}

.progress-text {
    font-size: 12px;
    color: #909399;
}

.question-list {
    display: flex;
    flex-direction: column;
    gap: 24px;
}

.question-card {
    background: #fff;
    border-radius: 8px;
    padding: 24px;
    box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.05);
    transition: all 0.3s;
    border: 1px solid transparent;
}

.question-card:hover {
    box-shadow: 0 4px 16px 0 rgba(0, 0, 0, 0.1);
}

.q-header-row {
    display: flex;
    justify-content: space-between;
    margin-bottom: 16px;
}

.q-tag-group {
    display: flex;
    align-items: center;
    gap: 12px;
}

.q-index {
    font-family: monospace;
    font-weight: bold;
    color: #909399;
    background: #f0f2f5;
    padding: 2px 8px;
    border-radius: 4px;
}

.q-content {
    font-size: 16px;
    margin-bottom: 24px;
    line-height: 1.8;
    color: #303133;
}

.q-options {
    display: flex;
    flex-direction: column;
    gap: 12px;
    margin-bottom: 24px;
}

.option-item {
    padding: 12px 20px;
    border: 1px solid #dcdfe6;
    border-radius: 8px;
    cursor: pointer;
    display: flex;
    align-items: center;
    transition: all 0.2s;
    background: #fff;
    position: relative;
}

.option-item:hover {
    background-color: #f5f7fa;
    border-color: #c0c4cc;
}

.selected-opt {
    border-color: #409eff;
    background-color: #ecf5ff !important;
    color: #409eff;
    font-weight: 500;
}

.correct-opt {
    border-color: #67c23a !important;
    background-color: #f0f9eb !important;
    color: #67c23a !important;
}

.wrong-opt {
    border-color: #f56c6c !important;
    background-color: #fef0f0 !important;
    color: #f56c6c !important;
}

.opt-prefix {
    font-weight: bold;
    margin-right: 12px;
    width: 24px;
}

.status-icon {
    position: absolute;
    right: 16px;
    font-size: 18px;
}

.q-footer {
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding-top: 16px;
    border-top: 1px dashed #eee;
}

.footer-left {
    display: flex;
    gap: 12px;
}

.result-box {
    display: flex;
    align-items: center;
    gap: 6px;
    font-weight: 600;
    font-size: 14px;
}

.result-box.correct { color: #67c23a; }
.result-box.wrong { color: #f56c6c; }

.analysis-wrapper {
    margin-top: 20px;
    padding-top: 20px;
    border-top: 1px solid #f0f2f5;
}

.analysis-box {
    background: #fdf6ec;
    padding: 20px;
    border-radius: 8px;
    border-left: 4px solid #e6a23c;
}

.analysis-title {
    font-weight: bold;
    color: #e6a23c;
    margin-bottom: 12px;
    display: flex;
    align-items: center;
    gap: 6px;
}

.analysis-content {
    font-size: 15px;
    color: #606266;
    line-height: 1.6;
}

.empty-state {
    height: 100%;
    display: flex;
    flex-direction: column;
    justify-content: center;
    align-items: center;
}

.empty-tip {
    color: #909399;
    font-size: 14px;
    margin-top: 10px;
}

:deep(.katex) {
    font-size: 1.1em;
}
</style>
