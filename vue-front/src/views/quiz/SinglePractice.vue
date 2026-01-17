<template>
  <div class="practice-container">
    <div v-if="mode === 'daily'" class="content-wrapper">
      <el-row :gutter="20" v-if="currentQuestion" class="full-height-row">
        <!-- 左侧：题目显示区 -->
        <el-col :span="12" class="left-col">
          <div class="question-card">
            <!-- 顶部导航栏 -->
            <div class="card-header">
              <div class="header-left">
                <el-button @click="goBack" circle icon="Back" class="back-btn" />
                <div class="subject-info">
                  <span class="subject-name">{{ $route.query.name || '专项练习' }}</span>
                  <span class="q-progress">进度 {{ currentIndex + 1 }} / {{ questions.length }}</span>
                </div>
              </div>
              <div class="header-right">
                <el-tooltip content="题目ID" placement="top">
                  <span class="q-id">#{{ currentQuestion.id }}</span>
                </el-tooltip>
              </div>
            </div>

            <!-- 进度条 -->
            <div class="progress-bar-area">
              <div class="progress-label">
                <span>答题进度</span>
                <span>{{ Math.round(progressPercentage) }}%</span>
              </div>
              <el-progress :percentage="progressPercentage" :show-text="false" :stroke-width="8"
                color="linear-gradient(90deg, #409EFF 0%, #36D1DC 100%)" />
            </div>

            <!-- 题目内容 -->
            <div class="q-content-area">
              <div class="q-meta-row">
                <el-tag effect="dark" class="type-tag" size="large">
                  {{ formatType(currentQuestion.type) }}
                </el-tag>
                <el-tag effect="plain" type="success" size="large">
                  {{ $route.query.name || '专项练习' }}
                </el-tag>
                <el-tag effect="plain" type="warning" size="large" v-if="currentQuestion.year">
                  {{ currentQuestion.year }}年真题
                </el-tag>
              </div>

              <div class="q-stem-wrapper">
                <div class="q-stem" v-html="renderLatex(currentQuestion.content)"></div>
              </div>

              <!-- 占位插画 (当题目较短时显示) -->
              <div class="empty-placeholder" v-if="currentQuestion.content && currentQuestion.content.length < 100">
                <div class="placeholder-icon">📖</div>
                <div class="placeholder-text">认真审题，仔细思考</div>
              </div>
            </div>

            <!-- 底部装饰 -->
            <div class="card-footer-decor">
              <span class="decor-text">KaoYan Platform · 专注考研每一题</span>
            </div>
          </div>
        </el-col>

        <!-- 右侧：做题操作区 -->
        <el-col :span="12" class="right-col">
          <div class="answer-card">
            <div class="answer-header">
              <div class="title-group">
                <span class="title-text">请选择答案</span>
                <span class="hint-text" v-if="currentQuestion.type === 2">(多选题)</span>
              </div>
              <el-button :icon="isCollected ? StarFilled : Star" :type="isCollected ? 'warning' : 'default'" circle
                @click="handleCollectionClick" />
            </div>

            <div class="options-container">
              <div v-for="(opt, index) in currentQuestion.displayOptions" :key="index" class="option-item" :class="{
                'selected': isOptionSelected(opt),
                'disabled': hasSubmitted,
                'correct-highlight': hasSubmitted && isCorrectAnswer(opt),
                'wrong-highlight': hasSubmitted && isOptionSelected(opt) && !isCorrectAnswer(opt),
                'unknown-option': opt === '我不会做'
              }" @click="!hasSubmitted && toggleOption(opt)">
                <div class="opt-letter">{{ String.fromCharCode(65 + index) }}</div>
                <div class="opt-text" v-html="renderLatex(formatOptionText(opt))"></div>

                <!-- 选中/正确/错误 图标指示 -->
                <div class="opt-icon" v-if="isOptionSelected(opt) || (hasSubmitted && isCorrectAnswer(opt))">
                  <el-icon v-if="hasSubmitted && isCorrectAnswer(opt)" color="#67C23A">
                    <Select />
                  </el-icon>
                  <el-icon v-else-if="hasSubmitted && isOptionSelected(opt) && !isCorrectAnswer(opt)" color="#F56C6C">
                    <CloseBold />
                  </el-icon>
                  <el-icon v-else color="#409EFF">
                    <Select />
                  </el-icon>
                </div>
              </div>
            </div>

            <!-- 提交后的简短状态栏 (不显示解析) -->
            <transition name="el-zoom-in-top">
              <div v-if="hasSubmitted" class="status-bar" :class="isCorrect ? 'success-bar' : 'error-bar'">
                <div class="status-content">
                  <el-icon v-if="isCorrect" size="20">
                    <Select />
                  </el-icon>
                  <el-icon v-else size="20">
                    <CloseBold />
                  </el-icon>
                  <span class="status-text">{{ isCorrect ? '回答正确' : '回答错误' }}</span>
                </div>
                <el-button type="primary" link @click="showAnalysisDialog = true">
                  查看解析 >
                </el-button>
              </div>
            </transition>

            <!-- 底部按钮组 -->
            <div class="action-footer">
              <div class="footer-left">
                <el-button @click="prevQuestion" :disabled="currentIndex === 0" plain icon="ArrowLeft">上一题</el-button>
                <el-tooltip content="打开草稿纸" placement="top">
                  <el-button class="draw-btn-footer" @click="toggleDrawingBoard" round>
                    <el-icon>
                      <EditPen />
                    </el-icon>
                    <span style="margin-left: 4px;">草稿纸</span>
                  </el-button>
                </el-tooltip>
              </div>

              <el-button type="primary" class="submit-btn" @click="submitAnswer" v-if="!hasSubmitted"
                :disabled="!userAnswer || (Array.isArray(userAnswer) && userAnswer.length === 0)" round>
                提交答案
              </el-button>

              <el-button type="success" class="next-btn" @click="nextQuestion" v-else round>
                {{ isLastQuestion ? '查看报告' : '下一题' }} <el-icon class="el-icon--right">
                  <ArrowRight />
                </el-icon>
              </el-button>
            </div>
          </div>
        </el-col>
      </el-row>

      <el-empty v-else description="题目加载中..." />

      <!-- 画板覆盖层 -->
      <transition name="el-fade-in">
        <div v-if="showDrawingBoard" class="drawing-board-overlay">
          <div class="drawing-toolbar">
            <el-radio-group v-model="currentTool" size="small" class="tool-group">
              <el-radio-button label="pen">
                <el-icon>
                  <EditPen />
                </el-icon> 画笔
              </el-radio-button>
              <el-radio-button label="eraser">
                <el-icon>
                  <Delete />
                </el-icon> 橡皮
              </el-radio-button>
            </el-radio-group>

            <el-divider direction="vertical" />

            <div class="color-picker" v-if="currentTool === 'pen'">
              <div v-for="color in colors" :key="color" class="color-dot"
                :style="{ backgroundColor: color, transform: currentColor === color ? 'scale(1.2)' : 'scale(1)' }"
                :class="{ active: currentColor === color }" @click="currentColor = color"></div>
            </div>

            <el-divider direction="vertical" />

            <div class="width-slider" style="width: 100px; margin-right: 10px">
              <el-slider v-model="currentWidth" :min="1" :max="20" :show-tooltip="false" size="small" />
            </div>

            <el-button-group>
              <el-button @click="undoStroke" icon="RefreshLeft" :disabled="history.length === 0">撤回</el-button>
              <el-button @click="clearCanvas" icon="Delete">清空</el-button>
              <el-button @click="showDrawingBoard = false" icon="Close">关闭</el-button>
            </el-button-group>
          </div>
          <canvas ref="drawingCanvas" class="drawing-canvas" @mousedown="startDrawing" @mousemove="draw"
            @mouseup="stopDrawing" @mouseleave="stopDrawing" @touchstart="startDrawing" @touchmove="draw"
            @touchend="stopDrawing"></canvas>
        </div>
      </transition>
    </div>

    <!-- 解析弹窗 -->
    <el-dialog v-model="showAnalysisDialog" title="答案解析" width="1000px" center destroy-on-close
      custom-class="analysis-dialog" top="5vh">
      <div class="analysis-content">
        <!-- 题目回顾区域 -->
        <div class="analysis-question-box">
          <div class="analysis-q-type">{{ formatType(currentQuestion?.type) }}</div>
          <div class="analysis-q-stem" v-html="renderLatex(currentQuestion?.content)"></div>

          <!-- 显示所有选项 -->
          <div class="analysis-options-review">
            <div v-for="(opt, index) in currentQuestion?.displayOptions" :key="index" class="analysis-opt-item"
              v-show="opt !== '我不会做'" :class="{
                'is-correct': isCorrectAnswer(opt),
                'is-wrong': isOptionSelected(opt) && !isCorrectAnswer(opt)
              }">
              <span class="opt-label">{{ String.fromCharCode(65 + index) }}.</span>
              <span class="opt-content" v-html="renderLatex(formatOptionText(opt))"></span>
              <el-icon v-if="isCorrectAnswer(opt)" color="#67C23A"><Select /></el-icon>
              <el-icon v-if="isOptionSelected(opt) && !isCorrectAnswer(opt)" color="#F56C6C">
                <CloseBold />
              </el-icon>
            </div>
          </div>
        </div>

        <div class="analysis-result-row">
          <div class="result-item">
            <span class="label">正确答案：</span>
            <span class="value correct">{{ correctAnswer }}</span>
          </div>
          <div class="result-item">
            <span class="label">你的答案：</span>
            <span class="value" :class="isCorrect ? 'correct' : 'wrong'">{{ userAnswer }}</span>
          </div>
        </div>

        <el-divider content-position="left">
          <el-icon>
            <Reading />
          </el-icon> 详细解析
        </el-divider>

        <div class="analysis-text" v-html="renderLatex(analysis || '暂无详细解析')"></div>
      </div>
      <template #footer>
        <el-button type="primary" @click="showAnalysisDialog = false">我知道了</el-button>
      </template>
    </el-dialog>

    <!-- 结果统计弹窗 -->
    <el-dialog v-model="showReport" title="本次练习报告" width="420px" center destroy-on-close custom-class="report-dialog"
      top="5vh">
      <div class="report-dashboard">
        <div class="score-circle">
          <el-progress type="circle" :percentage="accuracy" :color="accuracyColor" :width="140" :stroke-width="10" />
          <div class="score-label">正确率</div>
        </div>

        <div class="stats-cards">
          <div class="stat-card">
            <div class="val">{{ questions.length }}</div>
            <div class="lbl">总题数</div>
          </div>
          <div class="stat-card">
            <div class="val">{{ formatTime(totalTime) }}</div>
            <div class="lbl">总用时</div>
          </div>
          <div class="stat-card green">
            <div class="val">{{ correctCount }}</div>
            <div class="lbl">答对</div>
          </div>
          <div class="stat-card red">
            <div class="val">{{ errorCount }}</div>
            <div class="lbl">答错</div>
          </div>
        </div>

        <div class="review-section">
          <div class="review-title">
            <el-icon>
              <List />
            </el-icon> 题目回顾
          </div>
          <div class="review-grid">
            <div v-for="(q, index) in questions" :key="q.id" class="review-item" :class="getReviewClass(index)"
              @click="reviewQuestion(index)">
              {{ index + 1 }}
            </div>
          </div>
        </div>
      </div>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="restartPractice" icon="Refresh">再练一次</el-button>
          <el-button type="primary" @click="$router.push('/user/subject')" icon="Back">返回题库</el-button>
        </div>
      </template>
    </el-dialog>

    <!-- 收藏弹窗 -->
    <el-dialog v-model="showCollectionDialog" title="收藏题目" width="520px" center destroy-on-close
      custom-class="collection-dialog">
      <template #header>
        <div class="dialog-header-custom">
          <el-icon class="header-icon">
            <StarFilled />
          </el-icon>
          <span class="header-title">添加收藏</span>
        </div>
      </template>

      <div class="collection-form">
        <div class="collection-intro">
          <p>为这道题添加标签，方便日后专项复习。</p>
        </div>

        <!-- 已选标签展示区 -->
        <div class="tags-section" v-if="collectionTags.length > 0">
          <div class="section-label">已选标签</div>
          <div class="selected-tags-container">
            <el-tag v-for="tag in collectionTags" :key="tag" closable effect="dark" color="#409EFF"
              class="selected-tag-item" @close="removeTag(tag)">
              {{ tag }}
            </el-tag>
          </div>
        </div>

        <div class="tags-section">
          <div class="section-label">常用标签</div>
          <div class="tag-suggestions">
            <div v-for="tag in allDisplayTags" :key="tag" class="suggestion-tag-item"
              :class="{ active: collectionTags.includes(tag) }" @click.stop="toggleTag(tag)">
              <span class="tag-text">{{ tag }}</span>
              <!-- 区分显示：已选中显示对号，未选中且是用户自定义的显示删除号，系统预设的显示加号 -->
              <el-icon v-if="collectionTags.includes(tag)" class="check-icon"><Select /></el-icon>

              <!-- 如果是用户历史标签且未选中，显示删除按钮 -->
              <el-icon v-else-if="userHistoryTags.includes(tag) && !systemTags.includes(tag)" class="close-icon"
                @click.stop="deleteUserTag(tag)">
                <Close />
              </el-icon>

              <!-- 系统标签未选中显示加号 -->
              <el-icon v-else class="plus-icon">
                <Plus />
              </el-icon>
            </div>
          </div>
        </div>

        <div class="custom-tag-input">
          <div class="section-label">自定义标签</div>
          <div class="input-wrapper">
            <el-input v-model="newTagInput" placeholder="输入标签名称，按回车添加" @keyup.enter="addCustomTag" class="custom-input">
              <template #prefix>
                <el-icon>
                  <EditPen />
                </el-icon>
              </template>
              <template #suffix>
                <el-button type="primary" link @click="addCustomTag" :disabled="!newTagInput">添加</el-button>
              </template>
            </el-input>
          </div>
        </div>
      </div>

      <template #footer>
        <div class="dialog-footer-custom">
          <el-button @click="removeCollection" type="danger" plain v-if="isCollected" class="footer-btn">
            <el-icon>
              <Delete />
            </el-icon> 取消收藏
          </el-button>
          <div class="right-btns"
            :style="{ marginLeft: isCollected ? 'auto' : '0', width: isCollected ? 'auto' : '100%', justifyContent: isCollected ? 'flex-end' : 'flex-end' }">
            <el-button @click="showCollectionDialog = false" class="footer-btn">关闭</el-button>
            <el-button type="primary" @click="saveCollection" class="footer-btn save-btn">
              <el-icon><Select /></el-icon> 保存
            </el-button>
          </div>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted, nextTick } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import request from '@/utils/request'
import { ElMessage } from 'element-plus'
import { Select, CloseBold, Back, ArrowLeft, ArrowRight, EditPen, Delete, Close, List, Refresh, Reading, RefreshLeft, Star, StarFilled, Plus } from '@element-plus/icons-vue'
import katex from 'katex'

const route = useRoute()
const router = useRouter()

const goBack = () => {
  // 直接返回到科目选择首页 (SubjectList)
  router.push('/user/subject')
}

const mode = ref('daily')
const questions = ref([])
const currentIndex = ref(0)
const currentQuestion = ref(null)

// 收藏功能状态
const isCollected = ref(false)
const collectionTags = ref([])
const newTagInput = ref('')
const showCollectionDialog = ref(false)
const systemTags = ['难题', '好题', '易错', '重点']
const userHistoryTags = ref([])

const allDisplayTags = computed(() => {
  // 合并系统标签和用户历史标签，去重
  const set = new Set([...systemTags, ...userHistoryTags.value])
  return Array.from(set)
})

// 历史状态记录
const historyMap = ref({})

// 当前状态
const userAnswer = ref('')
const hasSubmitted = ref(false)
const isCorrect = ref(false)
const analysis = ref('')
const correctAnswer = ref('')
const showAnalysisDialog = ref(false)

// 计时相关
const totalTime = ref(0)
const currentQuestionStartTime = ref(0)
let timer = null

// 画板相关
const showDrawingBoard = ref(false)
const drawingCanvas = ref(null)
const isDrawing = ref(false)
const ctx = ref(null)

// 画板新状态
const currentTool = ref('pen')
const currentColor = ref('#f56c6c')
const currentWidth = ref(3)
const colors = ['#f56c6c', '#409EFF', '#67C23A', '#303133']
const history = ref([])
const currentPath = ref([])

// 统计
const showReport = ref(false)
const errorCount = computed(() => Object.values(historyMap.value).filter(r => r.hasSubmitted && !r.isCorrect).length)
const correctCount = computed(() => Object.values(historyMap.value).filter(r => r.hasSubmitted && r.isCorrect).length)
const accuracy = computed(() => {
  const answered = Object.values(historyMap.value).filter(r => r.hasSubmitted).length
  if (answered === 0) return 0
  return Math.round((correctCount.value / answered) * 100)
})
const accuracyColor = computed(() => {
  if (accuracy.value >= 80) return '#67C23A'
  if (accuracy.value >= 60) return '#E6A23C'
  return '#F56C6C'
})

const isLastQuestion = computed(() => currentIndex.value >= questions.value.length - 1)
const progressPercentage = computed(() => {
  if (questions.value.length === 0) return 0
  return Math.round(((currentIndex.value + 1) / questions.value.length) * 100)
})

// 初始化
const loadQuestions = async () => {
  // 优先使用 bookId 查询 (新逻辑)
  // 兼容旧逻辑：如果只有 name (source) 也可以查
  const bookId = route.params.subjectId // 注意：路由参数名暂未改，这里其实是 bookId
  const sourceName = route.query.name

  const res = await request.get('/question/list-by-subject', {
    params: {
      bookId: bookId,
      source: sourceName
    }
  })

  if (res.code === 200 && res.data.length > 0) {
    questions.value = res.data
    loadQuestionAtIndex(0)
    startTimer()
  } else {
    ElMessage.info('该习题集暂无题目')
  }
}

const startTimer = () => {
  if (timer) clearInterval(timer)
  timer = setInterval(() => {
    totalTime.value++
  }, 1000)
}

const formatTime = (seconds) => {
  const m = Math.floor(seconds / 60)
  const s = seconds % 60
  return `${m}分${s}秒`
}

const loadQuestionAtIndex = (index) => {
  currentIndex.value = index
  currentQuestion.value = questions.value[index]
  showAnalysisDialog.value = false
  currentQuestionStartTime.value = Date.now()

  // 处理选项随机化
  // 1. 获取原始选项
  let rawOptions = [...currentQuestion.value.options]
  // 2. 随机打乱
  rawOptions.sort(() => Math.random() - 0.5)
  // 3. 添加"不会做"作为固定选项
  currentQuestion.value.displayOptions = [...rawOptions, "我不会做"]

  // 检查收藏状态
  checkCollectionStatus()

  const history = historyMap.value[index]
  if (history) {
    userAnswer.value = history.userAnswer
    hasSubmitted.value = history.hasSubmitted
    isCorrect.value = history.isCorrect
    analysis.value = history.analysis
    correctAnswer.value = history.correctAnswer
  } else {
    // 根据题型初始化 userAnswer
    if (currentQuestion.value.type === 2) {
      userAnswer.value = [] // 多选初始化为数组
    } else {
      userAnswer.value = ''
    }
    hasSubmitted.value = false
    isCorrect.value = false
    analysis.value = ''
    correctAnswer.value = ''
  }
}

const formatType = (type) => ({ 1: '单选', 2: '多选', 3: '填空', 4: '简答' }[type] || '未知')

// 选项交互逻辑
const isOptionSelected = (opt) => {
  if (Array.isArray(userAnswer.value)) {
    return userAnswer.value.includes(opt)
  }
  return userAnswer.value === opt
}

const toggleOption = (opt) => {
  // 1. 处理 "我不会做" 的特殊逻辑
  if (opt === '我不会做') {
    // 如果当前已经选了"我不会做"，再次点击则是取消选择
    if (userAnswer.value === '我不会做') {
      userAnswer.value = currentQuestion.value.type === 2 ? [] : ''
    } else {
      // 否则选中"我不会做"
      userAnswer.value = '我不会做'
    }
    return
  }

  // 2. 如果之前选了"我不会做"，现在选普通选项 -> 清除"我不会做"
  if (userAnswer.value === '我不会做') {
    userAnswer.value = currentQuestion.value.type === 2 ? [] : ''
  }

  // 3. 单选模式
  if (currentQuestion.value.type === 1) {
    userAnswer.value = opt
    return
  }

  // 4. 多选模式
  if (currentQuestion.value.type === 2) {
    if (!Array.isArray(userAnswer.value)) {
      userAnswer.value = []
    }

    const idx = userAnswer.value.indexOf(opt)
    if (idx > -1) {
      userAnswer.value.splice(idx, 1)
    } else {
      userAnswer.value.push(opt)
    }
  }
}

// 辅助判断：提交后高亮正确答案
const isCorrectAnswer = (optText) => {
  if (!correctAnswer.value) return false

  // 转换选项内容为字母
  const idx = currentQuestion.value.options.indexOf(optText)
  if (idx === -1) return false
  const letter = String.fromCharCode(65 + idx)

  // 如果正确答案包含该字母，则高亮
  // 兼容单选 'A' 和 多选 'A,B' 或 'AB'
  return correctAnswer.value.includes(letter)
}

const formatOptionText = (text) => {
  if (!text) return ''
  return text.replace(/^[A-Z][.、\s]\s*/, '')
}

const submitAnswer = async () => {
  if (!userAnswer.value || (Array.isArray(userAnswer.value) && userAnswer.value.length === 0)) {
    return ElMessage.warning('请先选择答案')
  }

  // 检查是否选了"不会做"
  if (userAnswer.value === '不会做') {
    await processSubmission(null, true)
  } else {
    await processSubmission(userAnswer.value)
  }
}

const markAsUnknown = async () => {
  await processSubmission(null, true)
}

const processSubmission = async (rawAnswer, isUnknown = false) => {
  let finalAnswer = ''

  if (isUnknown) {
    finalAnswer = '我不会做'
  } else if (currentQuestion.value.type === 2) {
    // 多选：将选项数组转为字母数组，排序后拼接
    const selectedIndices = rawAnswer.map(opt => currentQuestion.value.options.indexOf(opt))
    selectedIndices.sort((a, b) => a - b)
    finalAnswer = selectedIndices.map(i => String.fromCharCode(65 + i)).join(',')
  } else if (currentQuestion.value.type === 1) {
    // 单选
    const index = currentQuestion.value.options.indexOf(rawAnswer)
    if (index !== -1) finalAnswer = String.fromCharCode(65 + index)
  } else {
    finalAnswer = rawAnswer
  }

  const duration = Math.floor((Date.now() - currentQuestionStartTime.value) / 1000)

  const payload = {
    questionId: currentQuestion.value.id,
    userAnswer: finalAnswer,
    userId: JSON.parse(localStorage.getItem('user')).id,
    duration: duration > 0 ? duration : 1
  }

  const res = await request.post('/record/submit', payload)

  if (res.code === 200) {
    hasSubmitted.value = true
    isCorrect.value = res.data.isCorrect === 1
    correctAnswer.value = res.data.correctAnswer
    analysis.value = res.data.analysis

    historyMap.value[currentIndex.value] = {
      userAnswer: isUnknown ? '我不会做' : rawAnswer, // 保存当前状态（可能是数组或字符串）
      hasSubmitted: true,
      isCorrect: isCorrect.value,
      analysis: analysis.value,
      correctAnswer: correctAnswer.value
    }
  }
}

const nextQuestion = () => {
  if (currentIndex.value < questions.value.length - 1) {
    loadQuestionAtIndex(currentIndex.value + 1)
  } else {
    clearInterval(timer)
    showReport.value = true
  }
}

const prevQuestion = () => {
  if (currentIndex.value > 0) loadQuestionAtIndex(currentIndex.value - 1)
}

const restartPractice = () => {
  showReport.value = false
  historyMap.value = {}
  totalTime.value = 0
  startTimer()
  loadQuestionAtIndex(0)
}

const getReviewClass = (index) => {
  const record = historyMap.value[index]
  if (!record || !record.hasSubmitted) return 'unanswered'
  return record.isCorrect ? 'correct' : 'wrong'
}

const reviewQuestion = (index) => {
  showReport.value = false
  loadQuestionAtIndex(index)
}

// 画板功能
const toggleDrawingBoard = async () => {
  showDrawingBoard.value = true
  await nextTick()
  initCanvas()
}

const initCanvas = () => {
  const canvas = drawingCanvas.value
  if (!canvas) return

  canvas.width = window.innerWidth
  canvas.height = window.innerHeight
  ctx.value = canvas.getContext('2d')
  ctx.value.lineCap = 'round'
  ctx.value.lineJoin = 'round'

  // 重绘历史
  redrawCanvas()
}

const startDrawing = (e) => {
  isDrawing.value = true
  const { clientX, clientY } = e.touches ? e.touches[0] : e

  // 记录新路径的起点
  currentPath.value = [{ x: clientX, y: clientY }]

  ctx.value.beginPath()
  ctx.value.moveTo(clientX, clientY)

  // 设置当前笔触样式
  updateContextStyle()
}

const updateContextStyle = () => {
  if (currentTool.value === 'eraser') {
    ctx.value.globalCompositeOperation = 'destination-out'
    ctx.value.lineWidth = currentWidth.value * 2 // 橡皮擦粗一点
  } else {
    ctx.value.globalCompositeOperation = 'source-over'
    ctx.value.lineWidth = currentWidth.value
    ctx.value.strokeStyle = currentColor.value
  }
}

const draw = (e) => {
  if (!isDrawing.value) return
  const { clientX, clientY } = e.touches ? e.touches[0] : e

  // 记录路径点
  currentPath.value.push({ x: clientX, y: clientY })

  ctx.value.lineTo(clientX, clientY)
  ctx.value.stroke()
}

const stopDrawing = () => {
  if (!isDrawing.value) return
  isDrawing.value = false

  // 保存当前路径到历史
  if (currentPath.value.length > 0) {
    history.value.push({
      tool: currentTool.value,
      color: currentColor.value,
      width: currentTool.value === 'eraser' ? currentWidth.value * 2 : currentWidth.value,
      points: [...currentPath.value]
    })
  }
  currentPath.value = []
}

const redrawCanvas = () => {
  if (!ctx.value || !drawingCanvas.value) return
  const canvas = drawingCanvas.value

  // 清空画布
  ctx.value.clearRect(0, 0, canvas.width, canvas.height)

  // 遍历历史重绘
  history.value.forEach(path => {
    ctx.value.beginPath()
    const points = path.points
    if (points.length === 0) return

    ctx.value.moveTo(points[0].x, points[0].y)
    for (let i = 1; i < points.length; i++) {
      ctx.value.lineTo(points[i].x, points[i].y)
    }

    if (path.tool === 'eraser') {
      ctx.value.globalCompositeOperation = 'destination-out'
      ctx.value.lineWidth = path.width
    } else {
      ctx.value.globalCompositeOperation = 'source-over'
      ctx.value.strokeStyle = path.color
      ctx.value.lineWidth = path.width
    }
    ctx.value.stroke()
  })
}

const undoStroke = () => {
  if (history.value.length > 0) {
    history.value.pop()
    redrawCanvas()
  }
}

const clearCanvas = () => {
  const canvas = drawingCanvas.value
  if (canvas && ctx.value) {
    ctx.value.clearRect(0, 0, canvas.width, canvas.height)
    history.value = [] // 清空历史
  }
}

onMounted(loadQuestions)
onUnmounted(() => {
  if (timer) clearInterval(timer)
})

// 收藏相关逻辑
const checkCollectionStatus = async () => {
  if (!currentQuestion.value) return
  const userStr = localStorage.getItem('user')
  if (!userStr) return
  const user = JSON.parse(userStr)

  try {
    const res = await request.get('/collection/check', {
      params: { userId: user.id, questionId: currentQuestion.value.id }
    })
    if (res.code === 200 && res.data) {
      isCollected.value = true
      // 确保 tags 是数组
      collectionTags.value = Array.isArray(res.data.tags) ? res.data.tags : (res.data.tags ? JSON.parse(res.data.tags) : [])
    } else {
      isCollected.value = false
      collectionTags.value = []
    }
  } catch (e) {
    console.error(e)
  }
}

const handleCollectionClick = () => {
  showCollectionDialog.value = true
  fetchUserTags()
}

const fetchUserTags = async () => {
  const userStr = localStorage.getItem('user')
  if (!userStr) return
  const user = JSON.parse(userStr)
  try {
    const res = await request.get('/collection/tags', { params: { userId: user.id } })
    if (res.code === 200) {
      userHistoryTags.value = res.data
    }
  } catch (e) {
    console.error(e)
  }
}

const toggleTag = (tag) => {
  const index = collectionTags.value.indexOf(tag)
  if (index > -1) {
    collectionTags.value.splice(index, 1)
  } else {
    collectionTags.value.push(tag)
  }
}

const addCustomTag = () => {
  const val = newTagInput.value.trim()
  if (!val) return

  if (!collectionTags.value.includes(val)) {
    collectionTags.value.push(val)
  }
  newTagInput.value = ''
}

const removeTag = (tag) => {
  const index = collectionTags.value.indexOf(tag)
  if (index > -1) {
    collectionTags.value.splice(index, 1)
  }
}

const saveCollection = async () => {
  const userStr = localStorage.getItem('user')
  if (!userStr) return
  const user = JSON.parse(userStr)

  const payload = {
    userId: user.id,
    questionId: currentQuestion.value.id,
    tags: collectionTags.value
  }

  try {
    const res = await request.post('/collection/add', payload)
    if (res.code === 200) {
      ElMessage.success('收藏成功')
      isCollected.value = true
      showCollectionDialog.value = false
    }
  } catch (e) {
    console.error(e)
  }
}

const removeCollection = async () => {
  const userStr = localStorage.getItem('user')
  if (!userStr) return
  const user = JSON.parse(userStr)

  const payload = {
    userId: user.id,
    questionId: currentQuestion.value.id
  }

  try {
    const res = await request.post('/collection/remove', payload)
    if (res.code === 200) {
      ElMessage.success('已取消收藏')
      isCollected.value = false
      collectionTags.value = []
      showCollectionDialog.value = false
    }
  } catch (e) {
    console.error(e)
  }
}

const deleteUserTag = (tag) => {
  // 从本地显示列表中移除（实际项目中可能需要调用后端 API 删除用户标签记录）
  const index = userHistoryTags.value.indexOf(tag)
  if (index > -1) {
    userHistoryTags.value.splice(index, 1)
  }
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
  }).replace(/\$\$([^$]+)\$\$/g, (match, tex) => { // 块级公式
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
</script>

<style scoped>
.practice-container {
  height: 100%;
  display: flex;
  flex-direction: column;
  background-color: #f0f2f5;
  padding: 16px;
  box-sizing: border-box;
  position: relative;
  overflow: hidden;
}

.content-wrapper {
  flex: 1;
  overflow: hidden;
  width: 100%;
  height: calc(100% - 20px);
}

.full-height-row {
  height: 100%;
}

.left-col,
.right-col {
  height: 100%;
}

/* 通用卡片样式 */
.question-card,
.answer-card {
  background: #fff;
  border-radius: 12px;
  height: 100%;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.04);
  display: flex;
  flex-direction: column;
  padding: 20px;
  box-sizing: border-box;
  transition: transform 0.3s ease;
  border: 1px solid rgba(0, 0, 0, 0.02);
}

.question-card:hover,
.answer-card:hover {
  box-shadow: 0 12px 32px rgba(0, 0, 0, 0.08);
}

/* 左侧：题目区 */
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
  flex-shrink: 0;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 16px;
}

.back-btn {
  border-color: transparent;
  background: #f5f7fa;
  color: #606266;
}

.back-btn:hover {
  background: #e6e8eb;
  color: #303133;
}

.subject-info {
  display: flex;
  flex-direction: column;
}

.subject-name {
  font-weight: 700;
  color: #303133;
  font-size: 16px;
  line-height: 1.4;
}

.q-progress {
  font-size: 12px;
  color: #909399;
}

.q-id {
  font-family: monospace;
  color: #c0c4cc;
  font-size: 14px;
  background: #f5f7fa;
  padding: 2px 6px;
  border-radius: 4px;
}

.progress-bar-area {
  margin-bottom: 24px;
  flex-shrink: 0;
}

.progress-label {
  display: flex;
  justify-content: space-between;
  margin-bottom: 6px;
  font-size: 12px;
  color: #909399;
}

.q-content-area {
  flex: 1;
  overflow-y: auto;
  padding-right: 10px;
  min-height: 0;
}

.q-meta-row {
  display: flex;
  align-items: center;
  gap: 16px;
  margin-bottom: 24px;
}

.type-tag {
  font-weight: bold;
  border-radius: 8px;
  padding: 0 12px;
}

.difficulty-tag {
  font-weight: bold;
}

.q-stem-wrapper {
  background: #fcfcfc;
  padding: 20px;
  border-radius: 8px;
  border: 1px solid #f0f2f5;
  box-shadow: inset 0 0 8px rgba(0, 0, 0, 0.02);
}

.q-stem {
  font-size: 16px;
  line-height: 1.8;
  color: #2c3e50;
  white-space: pre-wrap;
  text-align: justify;
  letter-spacing: 0.5px;
}

.empty-placeholder {
  margin-top: 40px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  opacity: 0.5;
}

.placeholder-icon {
  font-size: 48px;
  margin-bottom: 10px;
}

.placeholder-text {
  font-size: 14px;
  color: #909399;
  letter-spacing: 2px;
}

.card-footer-decor {
  margin-top: 20px;
  text-align: center;
  border-top: 1px dashed #ebeef5;
  padding-top: 16px;
  flex-shrink: 0;
}

.decor-text {
  font-size: 12px;
  color: #dcdfe6;
  letter-spacing: 1px;
}

/* 右侧：答题区 */
.answer-header {
  margin-bottom: 12px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  flex-shrink: 0;
}

.title-group {
  display: flex;
  align-items: baseline;
  gap: 8px;
}

.title-text {
  font-size: 18px;
  font-weight: 700;
  color: #303133;
}

.hint-text {
  font-size: 13px;
  color: #909399;
}

.draw-btn {
  color: #909399;
  border-color: #ebeef5;
}

.draw-btn:hover {
  color: #409EFF;
  border-color: #c6e2ff;
  background-color: #ecf5ff;
}

.options-container {
  flex: 1;
  overflow-y: auto;
  display: flex;
  flex-direction: column;
  gap: 16px;
  min-height: 0;
  padding: 4px;
}

.option-item {
  position: relative;
  display: flex;
  align-items: center;
  padding: 18px 24px;
  border: 2px solid #f0f2f5;
  border-radius: 12px;
  cursor: pointer;
  transition: all 0.25s cubic-bezier(0.25, 0.8, 0.25, 1);
  background: #fff;
}

.option-item:hover:not(.disabled) {
  border-color: #409EFF;
  background-color: #ecf5ff;
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(64, 158, 255, 0.15);
}

.option-item.selected {
  border-color: #409EFF;
  background-color: #ecf5ff;
  color: #409EFF;
  font-weight: 600;
}

.option-item.correct-highlight {
  border-color: #67C23A;
  background: #f0f9eb;
  color: #67C23A;
}

.option-item.wrong-highlight {
  border-color: #F56C6C;
  background: #fef0f0;
  color: #F56C6C;
}

.opt-letter {
  width: 36px;
  height: 36px;
  background: #f0f2f5;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 16px;
  font-weight: 700;
  color: #606266;
  font-size: 16px;
  transition: all 0.2s;
}

.selected .opt-letter {
  background: #409EFF;
  color: #fff;
}

.correct-highlight .opt-letter {
  background: #67C23A;
  color: #fff;
}

.wrong-highlight .opt-letter {
  background: #F56C6C;
  color: #fff;
}

.opt-text {
  flex: 1;
  font-size: 16px;
  line-height: 1.5;
}

.opt-icon {
  margin-left: 10px;
  font-size: 20px;
  font-weight: bold;
}

/* 状态栏 (内联状态) */
.status-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 20px;
  border-radius: 8px;
  margin-bottom: 20px;
  flex-shrink: 0;
}

.success-bar {
  background: #f0f9eb;
  border: 1px solid #b3e19d;
  color: #67C23A;
}

.error-bar {
  background: #fef0f0;
  border: 1px solid #fab6b6;
  color: #F56C6C;
}

.status-content {
  display: flex;
  align-items: center;
  gap: 8px;
  font-weight: bold;
}

/* 底部按钮 */
.action-footer {
  margin-top: 16px;
  padding-top: 16px;
  border-top: 1px solid #f0f2f5;
  display: flex;
  justify-content: space-between;
  align-items: center;
  flex-shrink: 0;
}

.footer-left {
  display: flex;
  align-items: center;
  gap: 12px;
}

.draw-btn-footer {
  color: #909399;
  border-color: #dcdfe6;
}

.draw-btn-footer:hover {
  color: #409EFF;
  border-color: #c6e2ff;
  background-color: #ecf5ff;
}

.submit-btn,
.next-btn {
  padding: 12px 32px;
  font-size: 16px;
  letter-spacing: 1px;
  font-weight: 600;
  box-shadow: 0 4px 12px rgba(64, 158, 255, 0.3);
  transition: all 0.3s;
}

.submit-btn:hover,
.next-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 16px rgba(64, 158, 255, 0.4);
}

/* 解析弹窗内容 */
.analysis-content {
  padding: 0 10px;
}

.analysis-question-box {
  background: #f8f9fa;
  padding: 16px;
  border-radius: 8px;
  margin-bottom: 20px;
  border-left: 4px solid #409EFF;
}

.analysis-q-type {
  display: inline-block;
  background: #409EFF;
  color: #fff;
  font-size: 12px;
  padding: 2px 8px;
  border-radius: 4px;
  margin-bottom: 8px;
}

.analysis-q-stem {
  font-size: 15px;
  color: #303133;
  line-height: 1.6;
}

.analysis-options-review {
  margin-top: 16px;
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.analysis-opt-item {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px 12px;
  background: #fff;
  border-radius: 6px;
  border: 1px solid #ebeef5;
  font-size: 14px;
}

.analysis-opt-item.is-correct {
  background: #f0f9eb;
  border-color: #b3e19d;
  color: #67C23A;
  font-weight: bold;
}

.analysis-opt-item.is-wrong {
  background: #fef0f0;
  border-color: #fab6b6;
  color: #F56C6C;
}

.analysis-result-row {
  display: flex;
  gap: 32px;
  margin-bottom: 24px;
  padding: 0 8px;
}

.result-item {
  display: flex;
  align-items: center;
  font-size: 16px;
}

.result-item .label {
  color: #606266;
}

.result-item .value {
  font-weight: bold;
  font-family: monospace;
  font-size: 18px;
  margin-left: 8px;
}

.result-item .value.correct {
  color: #67C23A;
}

.result-item .value.wrong {
  color: #F56C6C;
}

.analysis-text {
  font-size: 16px;
  line-height: 1.8;
  color: #303133;
  text-align: justify;
  background: #f0f9eb;
  /* 浅绿色背景区分解析 */
  padding: 16px;
  border-radius: 8px;
  border: 1px solid #e1f3d8;
}

/* 画板覆盖层 */
.drawing-board-overlay {
  position: fixed;
  top: 0;
  left: 0;
  width: 100vw;
  height: 100vh;
  background: rgba(255, 255, 255, 0.1);
  z-index: 3000;
  cursor: crosshair;
}

.drawing-toolbar {
  position: absolute;
  top: 20px;
  right: 20px;
  z-index: 3001;
  background: #fff;
  padding: 8px 12px;
  border-radius: 8px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  display: flex;
  align-items: center;
  gap: 12px;
}

.color-picker {
  display: flex;
  gap: 8px;
}

.color-dot {
  width: 20px;
  height: 20px;
  border-radius: 50%;
  cursor: pointer;
  border: 2px solid #fff;
  box-shadow: 0 0 0 1px #dcdfe6;
  transition: transform 0.2s;
}

.color-dot:hover {
  transform: scale(1.1);
}

.color-dot.active {
  box-shadow: 0 0 0 2px #409EFF;
}

.drawing-canvas {
  width: 100%;
  height: 100%;
  touch-action: none;
}

/* 弹窗美化复用 */
.report-dashboard {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 32px;
  padding: 20px 0;
  background: linear-gradient(to bottom, #ffffff, #f9f9f9);
  border-radius: 8px;
}

.score-circle {
  position: relative;
  text-align: center;
  transform: scale(1.1);
  margin-bottom: 10px;
}

.score-label {
  margin-top: 12px;
  font-weight: bold;
  color: #606266;
}

.stats-cards {
  display: flex;
  gap: 12px;
  width: 100%;
}

.stat-card {
  flex: 1;
  background: #f5f7fa;
  border-radius: 12px;
  padding: 16px 8px;
  text-align: center;
}

.stat-card.green {
  background: #f0f9eb;
}

.stat-card.red {
  background: #fef0f0;
}

.stat-card .val {
  font-size: 20px;
  font-weight: 800;
  margin-bottom: 4px;
  color: #303133;
}

.stat-card.green .val {
  color: #67C23A;
}

.stat-card.red .val {
  color: #F56C6C;
}

.stat-card .lbl {
  font-size: 12px;
  color: #909399;
}

.review-section {
  width: 100%;
  border-top: 1px solid #ebeef5;
  padding-top: 20px;
}

.review-title {
  font-size: 14px;
  font-weight: bold;
  color: #606266;
  margin-bottom: 12px;
  text-align: left;
}

.review-grid {
  display: grid;
  grid-template-columns: repeat(6, 1fr);
  gap: 10px;
}

.review-item {
  height: 32px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 4px;
  font-size: 12px;
  cursor: pointer;
  transition: all 0.2s;
}

.review-item.unanswered {
  background: #f0f2f5;
  color: #909399;
}

.review-item.correct {
  background: #f0f9eb;
  color: #67C23A;
  border: 1px solid #b3e19d;
}

.review-item.wrong {
  background: #fef0f0;
  color: #F56C6C;
  border: 1px solid #fab6b6;
}

.review-item:hover {
  opacity: 0.8;
  transform: scale(1.05);
}

/* 收藏弹窗样式 */
:deep(.collection-dialog) {
  border-radius: 16px;
  overflow: hidden;
  box-shadow: 0 12px 32px rgba(0, 0, 0, 0.1);
}

:deep(.collection-dialog .el-dialog__header) {
  margin: 0;
  padding: 24px 24px 16px;
  border-bottom: 1px solid #f0f2f5;
  background: #fff;
}

:deep(.collection-dialog .el-dialog__body) {
  padding: 30px;
  background: #fff;
}

:deep(.collection-dialog .el-dialog__footer) {
  padding: 16px 24px;
  border-top: 1px solid #f0f2f5;
  background-color: #f9fafe;
}

.dialog-header-custom {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 10px;
}

.header-icon {
  font-size: 24px;
  margin-top: -3px;
  color: #E6A23C;
  /* Ensure icon has color */
}

.header-title {
  font-size: 20px;
  font-weight: 700;
  color: #303133;
}

.collection-intro {
  margin-bottom: 30px;
  text-align: center;
  background: #fdf6ec;
  padding: 12px;
  border-radius: 8px;
  border: 1px solid #faecd8;
}

.collection-intro p {
  margin: 0;
  color: #e6a23c;
  font-size: 14px;
  font-weight: 500;
}

.section-label {
  font-size: 15px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 14px;
  display: flex;
  align-items: center;
}

.section-label::before {
  content: '';
  display: block;
  width: 4px;
  height: 16px;
  background: linear-gradient(to bottom, #409EFF, #36D1DC);
  border-radius: 2px;
  margin-right: 10px;
}

.tags-section {
  margin-bottom: 30px;
}

.tag-suggestions {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
}

.suggestion-tag-item {
  padding: 8px 18px;
  background: #f5f7fa;
  border: 1px solid #e4e7ed;
  border-radius: 20px;
  font-size: 14px;
  color: #606266;
  cursor: pointer;
  transition: all 0.2s;
  display: flex;
  align-items: center;
  gap: 8px;
  user-select: none;
}

.suggestion-tag-item:hover {
  background: #ecf5ff;
  border-color: #c6e2ff;
  color: #409EFF;
  transform: translateY(-2px);
}

.suggestion-tag-item.active {
  background: linear-gradient(135deg, #409EFF, #36D1DC);
  border-color: transparent;
  color: #fff;
  font-weight: 600;
  box-shadow: 0 4px 10px rgba(64, 158, 255, 0.3);
}

.check-icon {
  font-size: 14px;
}

.custom-tag-input {
  margin-bottom: 10px;
}

.dialog-footer-custom {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.right-btns {
  display: flex;
  gap: 12px;
  margin-left: auto;
}

.footer-btn {
  padding: 10px 24px;
  border-radius: 22px;
  font-size: 14px;
}

.save-btn {
  padding-left: 28px;
  padding-right: 28px;
  font-weight: 600;
  letter-spacing: 1px;
  box-shadow: 0 4px 12px rgba(64, 158, 255, 0.3);
}

.save-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 16px rgba(64, 158, 255, 0.4);
}

/* 覆盖 Element Plus Select 样式 */
:deep(.custom-input .el-input__wrapper) {
  border-radius: 12px;
  box-shadow: 0 0 0 1px #dcdfe6 inset;
  padding: 4px 12px;
  background: #f8f9fa;
}

:deep(.custom-input .el-input__wrapper:hover) {
  box-shadow: 0 0 0 1px #c0c4cc inset;
  background: #fff;
}

:deep(.custom-input .el-input__wrapper.is-focus) {
  box-shadow: 0 0 0 2px #409EFF inset !important;
  background: #fff;
}

.selected-tags-container {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.selected-tag-item {
  border-radius: 16px;
  padding: 0 12px;
  height: 28px;
  line-height: 26px;
  border: none;
}

.plus-icon,
.close-icon {
  font-size: 12px;
  opacity: 0.5;
  transition: all 0.2s;
}

.suggestion-tag-item:hover .plus-icon,
.suggestion-tag-item:hover .close-icon {
  opacity: 1;
}

.close-icon:hover {
  color: #F56C6C;
  transform: scale(1.2);
}

.dialog-footer {
  display: flex;
  justify-content: center;
  gap: 20px;
}

/* 题目和选项中的 Latex 样式调整 */
:deep(.katex) {
  font-size: 1.15em;
  /* 稍微调大一点 */
  line-height: 1.2;
}

:deep(.katex-display) {
  margin: 0.8em 0;
  overflow-x: auto;
  overflow-y: hidden;
  padding: 4px 0;
}

/* 题干样式微调，使文字与公式对齐更自然 */
.q-stem,
.analysis-q-stem,
.analysis-text,
.opt-text {
  line-height: 1.8;
  font-size: 16px;
  /* 确立基准字号 */
}
</style>
