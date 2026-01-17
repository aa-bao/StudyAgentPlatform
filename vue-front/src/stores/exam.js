import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { startExam, saveSnapshot, recordSwitch, submitExam as submitExamApi, getSessionDetail, getSessionDetails } from '@/api/examSession'
import { getPaperDetail, getPaperQuestions } from '@/api/paper'

export const useExamStore = defineStore('exam', () => {
  // --- State ---
  const paperId = ref(null)
  const sessionId = ref(null)
  const userId = ref('')
  
  const examInfo = ref({
    title: '',
    totalTime: 180 * 60, // 3 hours in seconds
    remainingTime: 180 * 60,
    startTime: null,
    paperType: null,
    examSpecId: null
  })

  const questions = ref([])
  const userAnswers = ref({})
  const markedQuestions = ref({})
  
  const examStatus = ref('pending') // pending, in-progress, grading, finished
  const switchCount = ref(0)
  const loading = ref(false)
  const error = ref(null)
  
  // --- Getters ---
  const answeredCount = computed(() => Object.keys(userAnswers.value).length)
  const totalCount = computed(() => questions.value.length)
  const progressPercentage = computed(() => Math.round((answeredCount.value / totalCount.value) * 100))
  const hasExamData = computed(() => !!paperId.value && !!sessionId.value)
  
  // --- Actions ---
  
  // 初始化考试（开始考试前调用）
  async function loadPaperInfo(paperIdParam, userIdParam) {
    try {
      loading.value = true
      error.value = null
      
      // 获取试卷详情
      const paperRes = await getPaperDetail(paperIdParam)
      if (paperRes.code === 200) {
        const paper = paperRes.data
        examInfo.value.title = paper.title
        examInfo.value.totalTime = (paper.timeLimit || 180) * 60
        examInfo.value.remainingTime = (paper.timeLimit || 180) * 60
        examInfo.value.paperType = paper.paperType
        examInfo.value.examSpecId = paper.examSpecId
        
        paperId.value = paperIdParam
        userId.value = userIdParam
      }
    } catch (err) {
      error.value = err.message || '加载试卷信息失败'
      console.error('Load paper info error:', err)
    } finally {
      loading.value = false
    }
  }
  
  // 开始考试
  async function initExam() {
    try {
      loading.value = true
      error.value = null
      
      if (!paperId.value || !userId.value) {
        throw new Error('试卷ID或用户ID不能为空')
      }
      
      // 调用后端开始考试 API
      const res = await startExam(userId.value, paperId.value)
      if (res.code === 200) {
        const data = res.data
        
        // 设置会话信息
        sessionId.value = data.session.id
        examInfo.value.startTime = data.session.startTime
        examInfo.value.totalTime = (data.paper.timeLimit || 180) * 60
        examInfo.value.remainingTime = (data.paper.timeLimit || 180) * 60
        
        // 设置题目列表（需要转换格式）
        questions.value = convertQuestions(data.questions)
        
        // 初始化答案
        userAnswers.value = {}
        markedQuestions.value = {}
        switchCount.value = 0
        
        // 更新状态
        examStatus.value = 'in-progress'
        
        // 尝试从 localStorage 恢复
        restoreFromLocal()
        
        // 保存初始快照
        await saveSnapshotToLocal()
      }
    } catch (err) {
      error.value = err.message || '开始考试失败'
      console.error('Init exam error:', err)
      throw err
    } finally {
      loading.value = false
    }
  }
  
  // 转换题目格式
  function convertQuestions(apiQuestions) {
    return apiQuestions.map(q => ({
      id: String(q.id), // 确保 ID 是字符串类型
      type: getQuestionType(q.type),
      content: q.content,
      options: convertOptions(q.options || []),
      answer: q.answer,
      analysis: q.analysis,
      difficulty: q.difficulty,
      source: q.source,
      score: getQuestionScore(q.type)
    }))
  }

  // 转换选项格式：从数组 ["选项1", "选项2"] 转为对象 {"A": "选项1", "B": "选项2"}
  function convertOptions(optionsArray) {
    if (!optionsArray || !Array.isArray(optionsArray)) {
      return {}
    }
    const optionsObj = {}
    const labels = ['A', 'B', 'C', 'D', 'E', 'F', 'G', 'H']

    optionsArray.forEach((opt, index) => {
      if (index < labels.length) {
        // 检查是否已经包含前缀（如 "A. xxx" 或 "(A) xxx"）
        const match = opt.match(/^[\(（]?([A-H])[.\.)\)）]\s*(.+)$/)
        if (match) {
          // 如果有前缀，使用提取的字母和内容
          optionsObj[match[1]] = match[2]
        } else {
          // 如果没有前缀，直接使用索引对应的字母
          optionsObj[labels[index]] = opt
        }
      }
    })

    return optionsObj
  }
  
  // 题目类型映射
  function getQuestionType(type) {
    const typeMap = {
      1: 'single-choice',
      2: 'multiple-choice',
      3: 'fill-blank',
      4: 'subjective'
    }
    return typeMap[type] || 'subjective'
  }
  
  // 获取题目分值（暂时使用默认值，后续可以从配置获取）
  function getQuestionScore(type) {
    const scoreMap = {
      1: 5,  // 单选 5分
      2: 10, // 多选 10分
      3: 10, // 填空 10分
      4: 15  // 解答 15分
    }
    return scoreMap[type] || 10
  }
  
  // 设置答案
  async function setAnswer(questionId, value) {
    userAnswers.value[questionId] = value
    await saveSnapshotToLocal()
  }
  
  // 切换标记
  function toggleMark(questionId) {
    if (markedQuestions.value[questionId]) {
      delete markedQuestions.value[questionId]
    } else {
      markedQuestions.value[questionId] = true
    }
    saveToLocal()
  }
  
  // 计时器 tick
  function tickTimer() {
    if (examStatus.value === 'in-progress' && examInfo.value.remainingTime > 0) {
      examInfo.value.remainingTime--
      
      // 每 5 秒保存一次状态
      if (examInfo.value.remainingTime % 5 === 0) {
        saveToLocal()
      }
    } else if (examInfo.value.remainingTime <= 0 && examStatus.value === 'in-progress') {
      submitExam()
    }
  }
  
  // 增加切换次数
  async function incrementSwitchCount() {
    switchCount.value++
    
    try {
      if (sessionId.value) {
        await recordSwitch(sessionId.value)
      }
    } catch (err) {
      console.error('Record switch error:', err)
    }
    
    saveToLocal()
  }
  
  // 保存答题快照到后端
  async function saveSnapshotToLocal() {
    if (!sessionId.value || examStatus.value !== 'in-progress') {
      return
    }
    
    try {
      const snapshotJson = JSON.stringify(userAnswers.value)
      await saveSnapshot(sessionId.value, snapshotJson)
    } catch (err) {
      console.error('Save snapshot error:', err)
    }
    
    saveToLocal()
  }
  
  // 保存状态到 localStorage
  function saveToLocal() {
    if (!sessionId.value) return
    
    localStorage.setItem('currentExamState', JSON.stringify({
      sessionId: sessionId.value,
      paperId: paperId.value,
      userId: userId.value,
      userAnswers: userAnswers.value,
      markedQuestions: markedQuestions.value,
      remainingTime: examInfo.value.remainingTime,
      switchCount: switchCount.value,
      examStatus: examStatus.value
    }))
  }
  
  // 从 localStorage 恢复
  function restoreFromLocal() {
    const savedState = localStorage.getItem('currentExamState')
    if (savedState) {
      try {
        const parsed = JSON.parse(savedState)
        
        // 只恢复同一会话的数据
        if (parsed.sessionId === sessionId.value) {
          userAnswers.value = parsed.userAnswers || {}
          markedQuestions.value = parsed.markedQuestions || {}
          examInfo.value.remainingTime = parsed.remainingTime || examInfo.value.totalTime
          switchCount.value = parsed.switchCount || 0
        }
      } catch (err) {
        console.error('Restore from local error:', err)
      }
    }
  }
  
  // 提交考试
  async function submitExam() {
    try {
      loading.value = true
      error.value = null
      
      if (!sessionId.value) {
        throw new Error('考试会话ID不存在')
      }
      
      // 先保存快照
      await saveSnapshotToLocal()
      
      // 更新状态为批改中
      examStatus.value = 'grading'
      
      // 调用后端提交考试 API
      const res = await submitExamApi(sessionId.value)
      
      if (res.code === 200) {
        // 获取考试会话详情
        const sessionRes = await getSessionDetail(sessionId.value)
        if (sessionRes.code === 200) {
          const session = sessionRes.data
          
          // 获取答题明细
          const detailsRes = await getSessionDetails(sessionId.value)
          if (detailsRes.code === 200) {
            const details = detailsRes.data
            
            // 保存考试结果到 store
            examInfo.value.totalScore = session.totalScore
            examInfo.value.submitTime = session.submitTime
            examInfo.value.aiSummary = session.aiSummary
            examInfo.value.examDetails = details
          }
        }
        
        // 清除 localStorage
        localStorage.removeItem('currentExamState')
        
        // 更新状态为完成
        examStatus.value = 'finished'
      }
    } catch (err) {
      error.value = err.message || '提交考试失败'
      console.error('Submit exam error:', err)
      // 如果提交失败，恢复到进行中状态
      examStatus.value = 'in-progress'
      throw err
    } finally {
      loading.value = false
    }
  }
  
  // 重置考试状态
  function resetExam() {
    paperId.value = null
    sessionId.value = null
    userId.value = ''
    examInfo.value = {
      title: '',
      totalTime: 180 * 60,
      remainingTime: 180 * 60,
      startTime: null,
      paperType: null,
      examSpecId: null
    }
    questions.value = []
    userAnswers.value = {}
    markedQuestions.value = {}
    examStatus.value = 'pending'
    switchCount.value = 0
    loading.value = false
    error.value = null
    localStorage.removeItem('currentExamState')
  }
  
  // 获取考试结果
  async function loadExamResult(sessionIdParam) {
    try {
      loading.value = true
      error.value = null
      
      const sessionRes = await getSessionDetail(sessionIdParam)
      const detailsRes = await getSessionDetails(sessionIdParam)
      
      if (sessionRes.code === 200 && detailsRes.code === 200) {
        sessionId.value = sessionIdParam
        examInfo.value.totalScore = sessionRes.data.totalScore
        examInfo.value.submitTime = sessionRes.data.submitTime
        examInfo.value.aiSummary = sessionRes.data.aiSummary
        examInfo.value.examDetails = detailsRes.data
        examStatus.value = 'finished'
      }
    } catch (err) {
      error.value = err.message || '加载考试结果失败'
      console.error('Load exam result error:', err)
      throw err
    } finally {
      loading.value = false
    }
  }
  
  return {
    // State
    paperId,
    sessionId,
    userId,
    examInfo,
    questions,
    userAnswers,
    markedQuestions,
    examStatus,
    switchCount,
    loading,
    error,
    
    // Getters
    answeredCount,
    totalCount,
    progressPercentage,
    hasExamData,
    
    // Actions
    loadPaperInfo,
    initExam,
    setAnswer,
    toggleMark,
    tickTimer,
    incrementSwitchCount,
    submitExam,
    resetExam,
    loadExamResult
  }
})
