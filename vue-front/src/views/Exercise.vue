<template>
    <div class="exercise-container">
        <el-card class="box-card" v-loading="loading">
            <template #header v-if="questionList && questionList.length > 0 && !showResult">
                <div class="card-header">
                    <div class="header-left">
                        <el-button icon="ArrowLeft" circle @click="$router.back()" size="small" />
                        <span class="subject-title">在线练习</span>
                    </div>
                    <div class="header-right">
                        <span class="timer-text"><el-icon>
                                <Timer />
                            </el-icon> {{ formatTime(seconds) }}</span>
                    </div>
                </div>
                <el-progress :percentage="progress" :show-text="false" :stroke-width="4" style="margin-top: 10px" />
            </template>

            <div v-if="questionList && questionList.length > 0 && !showResult">
                <div class="info-bar">
                    <el-tag type="info" effect="plain">第 {{ currentIndex + 1 }} / {{ questionList.length }} 题</el-tag>
                    <el-tag :type="getTypeTag(currentQuestion.type)" style="margin-left: 10px">{{
                        getTypeName(currentQuestion.type)
                        }}</el-tag>
                </div>

                <div class="question-content">
                    <span class="content-text">{{ currentQuestion.content }}</span>
                </div>

                <div class="options-list">
                    <div v-for="(opt, index) in parsedOptions" :key="index" class="option-item"
                        :class="getOptionClass(opt)" @click="handleSelect(opt)">
                        <div class="option-prefix">{{ getOptionLetter(opt) }}</div>
                        <div class="option-text">{{ getOptionContent(opt) }}</div>
                    </div>
                </div>

                <div class="action-bar">
                    <el-button-group>
                        <el-button @click="prevQuestion" :disabled="currentIndex === 0">上一题</el-button>
                        <el-button type="primary" :disabled="!currentAnswer || currentStatus.isSubmitted"
                            @click="submitAnswer">提交答案</el-button>
                        <el-button @click="nextQuestion" :disabled="!currentStatus.isSubmitted">
                            {{ currentIndex === questionList.length - 1 ? '查看报告' : '下一题' }}
                        </el-button>
                    </el-button-group>
                </div>

                <el-collapse-transition>
                    <div class="analysis-panel" v-if="currentStatus.isSubmitted">
                        <div class="res-banner" :class="currentStatus.isCorrect ? 'correct' : 'wrong'">
                            <el-icon>
                                <CircleCheck v-if="currentStatus.isCorrect" />
                                <CircleClose v-else />
                            </el-icon>
                            {{ currentStatus.isCorrect ? '回答正确！' : '回答错误' }}
                        </div>
                        <div class="analysis-detail">
                            <p><strong>正确答案：</strong><span class="ans">{{ currentQuestion.answer }}</span></p>
                            <p><strong>题目解析：</strong>{{ currentQuestion.analysis || '暂无详细解析' }}</p>
                        </div>
                    </div>
                </el-collapse-transition>
            </div>

            <div v-else-if="showResult" class="result-container">
                <el-result :icon="accuracy >= 60 ? 'success' : 'warning'" :title="'练习完成！正确率 ' + accuracy + '%'"
                    :sub-title="'本次共练习 ' + questionList.length + ' 道题，用时 ' + formatTime(seconds)">
                    <template #extra>
                        <div class="stat-grid">
                            <div v-for="(status, index) in questionStats" :key="index" class="stat-node"
                                :class="status.isCorrect ? 'node-correct' : 'node-wrong'"
                                @click="jumpToQuestion(index)">
                                {{ index + 1 }}
                            </div>
                        </div>
                        <div class="result-btns">
                            <el-button type="primary" @click="restart">重新练习</el-button>
                            <el-button @click="$router.push('/user/questions')">返回列表</el-button>
                        </div>
                    </template>
                </el-result>
            </div>

            <el-empty v-else-if="!loading" description="该科目下暂无题目" />
        </el-card>
    </div>
</template>

<script setup>
import { ref, onMounted, computed, onUnmounted } from 'vue'
import { useRoute } from 'vue-router'
import request from '@/utils/request'
import { Timer, ArrowLeft, CircleCheck, CircleClose } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus' // 确保引入了消息提示

const route = useRoute()
const loading = ref(false)
const questionList = ref([])
const currentIndex = ref(0)
const showResult = ref(false)
const seconds = ref(0)
let timerInterval = null

// 核心状态数组
const questionStats = ref([])

const currentQuestion = computed(() => questionList.value[currentIndex.value] || {})
const currentStatus = computed(() => questionStats.value[currentIndex.value] || { selected: '', isSubmitted: false, isCorrect: false })
const currentAnswer = computed(() => currentStatus.value.selected)
const progress = computed(() => {
    if (!questionList.value.length) return 0
    return Math.round(((currentIndex.value + 1) / questionList.value.length) * 100)
})

const accuracy = computed(() => {
    if (!questionList.value.length) return 0
    const correctCount = questionStats.value.filter(s => s.isCorrect).length
    return Math.round((correctCount / questionList.value.length) * 100)
})

const parsedOptions = computed(() => {
    const opts = currentQuestion.value.options
    if (!opts) return []
    try {
        return typeof opts === 'string' ? JSON.parse(opts) : opts
    } catch (e) {
        return []
    }
})

const loadData = async () => {
    loading.value = true
    const id = route.params.id
    try {
        const res = await request.get('/question/list-by-subject', { params: { subjectId: id } })
        if (res.data && res.data.length > 0) {
            questionList.value = res.data
            // 初始化状态数组，长度必须与题目列表一致
            questionStats.value = res.data.map(() => ({
                selected: '',
                isSubmitted: false,
                isCorrect: false
            }))
            startTimer()
        }
    } catch (error) {
        console.error("加载数据失败", error)
    } finally {
        loading.value = false
    }
}

const startTimer = () => {
    if (timerInterval) clearInterval(timerInterval)
    timerInterval = setInterval(() => { seconds.value++ }, 1000)
}

const formatTime = (s) => {
    const m = Math.floor(s / 60)
    const sec = s % 60
    return `${m.toString().padStart(2, '0')}:${sec.toString().padStart(2, '0')}`
}

const handleSelect = (opt) => {
    if (currentStatus.value.isSubmitted) return
    questionStats.value[currentIndex.value].selected = opt.charAt(0)
}

const getOptionClass = (opt) => {
    const letter = opt.charAt(0)
    const stat = currentStatus.value
    if (stat.isSubmitted) {
        if (letter === currentQuestion.value.answer) return 'correct-item'
        if (letter === stat.selected && !stat.isCorrect) return 'wrong-item'
        return 'disabled-item'
    }
    return stat.selected === letter ? 'selected-item' : ''
}

const submitAnswer = async () => {
    const userInfo = JSON.parse(localStorage.getItem('user'));
    const stat = questionStats.value[currentIndex.value];
    const question = currentQuestion.value;

    // 判断对错
    stat.isCorrect = (stat.selected === question.answer);
    stat.isSubmitted = true;

    // --- 调用后端 RecordController ---
    await request.post('/record/add', {
        userId: userInfo.id,       // 对应实体类字段
        questionId: question.id,
        userAnswer: stat.selected,
        isCorrect: stat.isCorrect ? 1 : 0
    });

    if (!stat.isCorrect) {
        await request.post('/question/saveWrong', {
            userId: userInfo.id,
            questionId: question.id
        });
    }
}

const nextQuestion = () => {
    if (currentIndex.value < questionList.value.length - 1) {
        currentIndex.value++
    } else {
        showResult.value = true
        clearInterval(timerInterval)
    }
}

const prevQuestion = () => {
    if (currentIndex.value > 0) currentIndex.value--
}

const jumpToQuestion = (index) => {
    currentIndex.value = index
    showResult.value = false
    startTimer()
}

const restart = () => {
    currentIndex.value = 0
    showResult.value = false
    seconds.value = 0
    loadData()
}

const getOptionLetter = (opt) => opt.charAt(0)
const getOptionContent = (opt) => opt.includes('.') ? opt.split('.')[1] : opt.substring(2)
const getTypeName = (t) => ({ 1: '单选题', 2: '多选题', 3: '填空题' }[t] || '题目')
const getTypeTag = (t) => ({ 1: '', 2: 'warning', 3: 'success' }[t] || 'info')

onMounted(loadData)
onUnmounted(() => { if (timerInterval) clearInterval(timerInterval) })
</script>




<style scoped>
.exercise-container {
    max-width: 850px;
    margin: 0 auto;
    padding: 20px;
}

.card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
}

.header-left {
    display: flex;
    align-items: center;
    gap: 15px;
}

.subject-title {
    font-weight: bold;
    color: #303133;
}

.timer-text {
    font-family: monospace;
    color: #909399;
    font-size: 16px;
    display: flex;
    align-items: center;
    gap: 5px;
}

.info-bar {
    margin: 15px 0;
}

.question-content {
    font-size: 18px;
    line-height: 1.8;
    margin-bottom: 30px;
    color: #2c3e50;
    font-weight: 500;
}

.options-list {
    display: flex;
    flex-direction: column;
    gap: 12px;
}

.option-item {
    display: flex;
    align-items: center;
    padding: 16px 20px;
    border: 1px solid #dcdfe6;
    border-radius: 12px;
    cursor: pointer;
    transition: all 0.2s;
}

.option-prefix {
    width: 32px;
    height: 32px;
    border-radius: 50%;
    background: #f0f2f5;
    display: flex;
    align-items: center;
    justify-content: center;
    margin-right: 15px;
    font-weight: bold;
    color: #606266;
}

.selected-item {
    border-color: #409eff;
    background-color: #ecf5ff;
}

.selected-item .option-prefix {
    background: #409eff;
    color: white;
}

.correct-item {
    border-color: #67c23a;
    background-color: #f0f9eb;
}

.correct-item .option-prefix {
    background: #67c23a;
    color: white;
}

.wrong-item {
    border-color: #f56c6c;
    background-color: #fef0f0;
}

.wrong-item .option-prefix {
    background: #f56c6c;
    color: white;
}

.disabled-item {
    opacity: 0.7;
    cursor: not-allowed;
}

.action-bar {
    margin-top: 40px;
    padding-top: 20px;
    border-top: 1px solid #f0f0f0;
    display: flex;
    justify-content: center;
}

.analysis-panel {
    margin-top: 25px;
    padding: 20px;
    background: #f8f9fa;
    border-radius: 12px;
    border: 1px solid #ebeef5;
}

.res-banner {
    display: flex;
    align-items: center;
    gap: 8px;
    font-weight: bold;
    margin-bottom: 15px;
}

.correct {
    color: #67c23a;
}

.wrong {
    color: #f56c6c;
}

.ans {
    color: #67c23a;
    font-weight: bold;
    font-size: 18px;
}

/* 报告页 */
.result-container {
    padding: 20px 0;
    text-align: center;
}

.stat-grid {
    display: flex;
    flex-wrap: wrap;
    gap: 10px;
    justify-content: center;
    margin: 30px auto;
    max-width: 450px;
}

.stat-node {
    width: 40px;
    height: 40px;
    border-radius: 8px;
    display: flex;
    align-items: center;
    justify-content: center;
    cursor: pointer;
    font-weight: bold;
}

.node-correct {
    background-color: #f0f9eb;
    color: #67c23a;
    border: 1px solid #67c23a;
}

.node-wrong {
    background-color: #fef0f0;
    color: #f56c6c;
    border: 1px solid #f56c6c;
}

.result-btns {
    display: flex;
    gap: 15px;
    justify-content: center;
    margin-top: 20px;
}
</style>