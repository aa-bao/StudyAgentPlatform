<template>
  <div class="exam-app" ref="examAppRef">
    <!-- 加载状态 -->
    <div v-if="loading" class="loading-screen">
      <div class="loader"></div>
      <p>{{ loadingText }}</p>
    </div>

    <!-- 错误状态 -->
    <div v-else-if="error" class="error-screen">
      <div class="error-content">
        <h2>加载失败</h2>
        <p>{{ error }}</p>
        <el-button @click="goBack">返回</el-button>
      </div>
    </div>

    <!-- 考试界面 -->
    <template v-else>
      <!-- 导航栏 -->
      <nav class="system-nav">
        <div class="nav-left">
          <div class="logo-group">
            <span class="logo-group-eng">KaoYan Platform</span>
            <span class="logo-group-ch">考研真题模考系统</span>
          </div>
          <span class="ai-tag">AI ENHANCED</span>
        </div>

        <div class="nav-right">
          <div class="top-tools">
            <button @click="openDraft" class="nav-tool-btn">📝 草稿纸</button>
          </div>

          <div class="timer-box">
            <button @click="toggleFullScreen" class="fullscreen-btn">
              <span class="icon">全屏</span>
            </button>
            <span class="timer-label">倒计时</span>
            <span class="timer-value">{{ formatTime }}</span>
          </div>
          <button @click="handleSubmit" :disabled="isSubmitted || submitting" class="submit-btn"
            :class="{ 'submit-btn--disabled': isSubmitted || submitting }">
            {{ isSubmitted ? '已提交' : '提交试卷' }}
          </button>
        </div>
      </nav>

      <!-- 试卷 -->
      <div class="main-layout">
        <div class="paper-wrapper">
          <div class="paper-page-container" :class="{ 'submitted-mask': isSubmitted }">
            <div v-for="(page, pageIndex) in paperPages" :key="pageIndex" class="paper-sheet shadow-effect">
              <div v-if="pageIndex === 0" class="sealing-line">
                <span class="seal-warning">密 封 线 内 不 要 答 题</span>
              </div>

              <header v-if="pageIndex === 0" class="paper-header">
                <div class="confidential-mark">绝密 <span class="star">★</span> 启用前</div>
                <h1 class="main-title">{{ paperInfo.title }}</h1>
                <div class="title-divider"></div>
                <div class="exam-info">
                  <span>考试时间：{{ paperInfo.timeLimit || 180 }} 分钟</span>
                  <span>满分：{{ paperInfo.totalScore || 150 }} 分</span>
                </div>
                <div class="notice-box">
                  <p class="notice-title">考生注意事项：</p>
                  <ol class="notice-list">
                    <li>答题前，考生务必将自己的姓名、准考证号填写在答题卡上。</li>
                    <li>选择题每小题选出答案后，请在系统中点击对应选项。</li>
                    <li>主观题请在输入框内作答，或使用草稿纸记录思路。</li>
                  </ol>
                </div>
              </header>

              <!-- 题目列表 -->
              <div v-for="q in page.questions" :key="q.id" :id="'q' + q.id" class="question-item">
                <!-- 题型标题 -->
                <div v-if="q.sectionTitle" class="section-banner">{{ q.sectionTitle }}</div>

                <!-- 题目内容（跳过sectionTitle对象） -->
                <template v-else>
                  <!-- 题号和题目内容 -->
                  <p class="question-title">
                    <span class="question-number">{{ q.index }}.</span>
                    <span v-html="renderLatex(q.content)"></span>
                  </p>

                  <!-- 选择题选项 -->
                  <div v-if="isChoiceQuestion(q.type)" class="options-grid">
                    <label v-for="opt in Object.keys(q.options || {})" :key="opt" class="option-label">
                      <input
                        :type="q.type === 'single-choice' ? 'radio' : 'checkbox'"
                        :name="'q' + q.id"
                        :value="opt"
                        :checked="isOptionSelected(q.id, opt)"
                        @change="handleOptionChange(q.id, opt, q.type)"
                      >
                      <span class="option-text" v-html="'(' + opt + ') ' + renderLatex(q.options[opt])"></span>
                    </label>
                  </div>

                  <!-- 填空题/主观题输入框 -->
                  <textarea
                    v-else
                    v-model="answers[q.id]"
                    @input="markDone(q.id)"
                    class="answer-area"
                    rows="4"
                    placeholder="请输入你的答案..."
                  ></textarea>
                </template>
              </div>

              <footer class="paper-footer">第 {{ pageIndex + 1 }} 页（共 {{ paperPages.length }} 页）</footer>
            </div>
          </div>
        </div>
      </div>

      <!-- 答题卡侧边栏 -->
      <aside class="sidebar-container" :class="{ 'is-hidden': !showSidebar }" :style="sidebarStyle"
        @mousedown="handleDragStart">
        <div class="toggle-handler" @click.stop="toggleSidebar">
          {{ showSidebar ? '▶' : '◀' }}
          <span class="toggle-text">答题卡</span>
        </div>

        <div class="sidebar-card card-glow">
          <h3 class="card-title" style="cursor: move;">
            <span>答题状态</span>
            <span class="count-tag">{{ doneCount }} / {{ totalCount }}</span>
          </h3>
          <div class="answer-card-grid">
            <div
              v-for="q in allQuestions"
              :key="q.id"
              @click.stop="scrollToQuestion(q.id)"
              class="card-number"
              :class="{ 'is-done': doneSet.has(q.id) }"
            >
              {{ q.index }}
            </div>
          </div>
        </div>
      </aside>

      <!-- 草稿区 -->
      <transition name="fade">
        <div v-show="showDraft" class="full-page-draft">
          <div class="draft-toolbar">
            <div class="tool-group">
              <label>颜色：</label>
              <input type="color" v-model="brushConfig.color">
            </div>
            <div class="tool-group">
              <label>粗细：</label>
              <input type="range" min="1" max="10" v-model="brushConfig.size">
            </div>
            <div class="divider"></div>
            <button @click="undoLast" class="draft-btn">↩️ 撤销</button>
            <button @click="clearCanvas" class="draft-btn danger">🗑️ 清空</button>
            <button @click="showDraft = false" class="draft-btn primary">关闭</button>
          </div>

          <canvas ref="draftCanvas" class="draft-canvas" @mousedown="startDraw" @mousemove="draw" @mouseup="endDraw"
            @mouseleave="endDraw"></canvas>
        </div>
      </transition>

      <!-- 确认提交模态框 -->
      <transition name="fade">
        <div v-if="showConfirmModal" class="modal-overlay">
          <div class="modal-content card-glow">
            <div class="modal-icon">⚠️</div>
            <h3 class="modal-title">确认提交试卷</h3>
            <p class="modal-tips">
              当前已完成 <span class="highlight">{{ doneCount }}</span> 题，剩余 <span class="highlight">{{ totalCount - doneCount }}</span> 题未作答。
              提交后将无法修改答案，并立即生成 AI 阅卷报告。
            </p>
            <div class="modal-btns">
              <button @click="showConfirmModal = false" class="modal-btn cancel">返回检查</button>
              <button @click="confirmSubmit" class="modal-btn confirm">确认交卷</button>
            </div>
          </div>
        </div>
      </transition>

      <!-- 提交中提示 -->
      <transition name="fade">
        <div v-if="submitting" class="modal-overlay">
          <div class="modal-content card-glow">
            <div class="loader"></div>
            <h3 class="modal-title">正在提交...</h3>
            <p class="modal-tips">AI 正在批改您的试卷，请稍候</p>
          </div>
        </div>
      </transition>

      <!-- AI 阅卷报告模态框 -->
      <transition name="fade">
        <div v-if="showResultModal" class="modal-overlay">
          <div class="modal-content report-modal card-glow">
            <div class="modal-header">
              <div class="modal-icon success">🎉</div>
              <h3 class="modal-title">AI 智能阅卷报告</h3>
            </div>

            <div class="report-body">
              <div class="report-card-content">
                <div class="score-box">
                  <span class="score-label">总得分</span>
                  <span class="score-value">{{ examResult.totalScore || 0 }}</span>
                  <span class="score-total">/ {{ paperInfo.totalScore || 150 }}</span>
                </div>
                <div class="analysis-box">
                  <h4>💡 AI 总结</h4>
                  <p style="white-space: pre-line;">{{ examResult.aiSummary || '暂无总结' }}</p>
                </div>
              </div>
            </div>

            <div class="modal-btns">
              <button @click="goBackToHome" class="modal-btn cancel">返回首页</button>
              <button @click="showResultModal = false" class="modal-btn confirm">关闭</button>
            </div>
          </div>
        </div>
      </transition>
    </template>

    <!-- 回到主页 -->
    <div v-if="isSubmitted" class="home-fab" @click="goBackToHome" title="返回首页">
      <img :src="homeIcon" alt="主页" class="home-icon" />
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, onUnmounted, nextTick, watch } from 'vue';
import { useRouter, useRoute } from 'vue-router';
import { ElMessage, ElMessageBox } from 'element-plus';
import katex from 'katex';
import 'katex/dist/katex.min.css';
import { startExam, saveSnapshot, recordSwitch, submitExam as submitExamApi, getSessionDetail } from '@/api/examSession';
import { getPaperDetail } from '@/api/paper';
import homeIcon from '@/assets/icons/home.svg?url'

const router = useRouter();
const route = useRoute();

// --- 状态数据 ---
const examAppRef = ref(null);
const isFullScreen = ref(false);
const isSubmitted = ref(false);
const showResultModal = ref(false);
const loading = ref(true);
const loadingText = ref('正在加载试卷...');
const error = ref(null);
const submitting = ref(false);

// 试卷信息
const paperInfo = ref({});
const allQuestions = ref([]); // 所有题目
const answers = reactive({}); // 用户答案
const doneSet = reactive(new Set()); // 已答题目集合

// 考试会话
const sessionId = ref(null);
const switchCount = ref(0);
const secondsLeft = ref(180 * 60); // 剩余秒数
const examEndTime = ref(null); // 考试结束时间戳
let timerInterval = null;

// AI 阅卷结果
const examResult = ref({
  totalScore: 0,
  aiSummary: ''
});

// 侧边栏
const showSidebar = ref(true);
const position = reactive({ x: window.innerWidth - 300, y: 120 });
const isDragging = ref(false);
const lastPositionX = ref(0);

// 草稿纸
const showDraft = ref(false);
const draftCanvas = ref(null);
const ctx = ref(null);
const isDrawing = ref(false);
const strokes = ref([]);
const currentStroke = ref(null);
const brushConfig = reactive({ color: '#333333', size: 3 });

// 确认弹窗
const showConfirmModal = ref(false);

// 历史记录保护
const exitAttemptCount = ref(0); // 用户尝试退出次数

// --- 计算属性 ---
const totalCount = computed(() => allQuestions.value.length);
const doneCount = computed(() => doneSet.size);

// 分页显示（每页5道题）
const paperPages = computed(() => {
  const pages = [];
  const pageSize = 5;
  let currentPage = { questions: [] };

  allQuestions.value.forEach((q, index) => {
    // 添加题型标题
    if (q.type === 'single-choice' && (index === 0 || allQuestions.value[index - 1].type !== 'single-choice')) {
      currentPage.questions.push({ sectionTitle: '一、选择题', id: `section-${index}` });
    } else if (q.type === 'fill-blank' && (index === 0 || allQuestions.value[index - 1].type !== 'fill-blank')) {
      currentPage.questions.push({ sectionTitle: '二、填空题', id: `section-${index}` });
    } else if (q.type === 'subjective' && (index === 0 || allQuestions.value[index - 1].type !== 'subjective')) {
      currentPage.questions.push({ sectionTitle: '三、解答题', id: `section-${index}` });
    }

    currentPage.questions.push(q);

    if (currentPage.questions.length >= pageSize) {
      pages.push(currentPage);
      currentPage = { questions: [] };
    }
  });

  if (currentPage.questions.length > 0) {
    pages.push(currentPage);
  }

  return pages;
});

// 格式化时间
const formatTime = computed(() => {
  const m = Math.floor(secondsLeft.value / 60);
  const s = secondsLeft.value % 60;
  return `${m}:${s < 10 ? '0' : ''}${s}`;
});

// --- 初始化考试 ---
const initExam = async () => {
  try {
    loading.value = true;
    loadingText.value = '正在加载试卷信息...';

    const { paperId, userId } = route.query;
    if (!paperId || !userId) {
      throw new Error('缺少必要参数');
    }

    // 检查本地是否已有考试结束时间 (刷新恢复的情况)
    const storageKey = `exam_session_${paperId}_${userId}`;
    const savedSessionData = localStorage.getItem(storageKey);

    // 获取试卷详情
    const paperRes = await getPaperDetail(paperId);
    if (paperRes.code !== 200 || !paperRes.data) {
      throw new Error('试卷不存在');
    }
    paperInfo.value = paperRes.data;

    // 如果本地有保存的结束时间,先使用本地值(用于刷新恢复)
    if (savedSessionData) {
      try {
        const parsed = JSON.parse(savedSessionData);
        examEndTime.value = parsed.examEndTime;

        // 计算剩余时间
        const now = Date.now();
        const remaining = Math.max(0, Math.floor((examEndTime.value - now) / 1000));

        if (remaining === 0) {
          // 考试时间已到，清除本地数据
          localStorage.removeItem(storageKey);
          localStorage.removeItem(`exam_end_time_${parsed.sessionId}`);
          examEndTime.value = null;
        }
      } catch (err) {
        console.error('恢复时间失败，重新开始考试:', err);
        localStorage.removeItem(storageKey);
        examEndTime.value = null;
      }
    }

    // 开始考试 (无论是新开始还是刷新，都调用此接口获取题目)
    loadingText.value = '正在初始化考试...';
    const startRes = await startExam(userId, paperId);
    if (startRes.code !== 200) {
      throw new Error('初始化考试失败');
    }

    const data = startRes.data;
    sessionId.value = data.session.id;

    // 使用后端返回的预期结束时间
    // 后端逻辑: 如果有未完成会话(status=0)则复用，否则创建新会话
    // expectedEndTime = startTime + paper.timeLimit
    if (data.session.expectedEndTime) {
      examEndTime.value = new Date(data.session.expectedEndTime).getTime();

      // 计算剩余时间(秒)
      const now = Date.now();
      const remaining = Math.max(0, Math.floor((examEndTime.value - now) / 1000));
      secondsLeft.value = remaining;

      // 保存到本地存储用于刷新恢复
      localStorage.setItem(`exam_end_time_${sessionId.value}`, examEndTime.value.toString());
      localStorage.setItem(storageKey, JSON.stringify({
        sessionId: sessionId.value,
        examEndTime: examEndTime.value,
        paperId,
        userId
      }));
    } else {
      throw new Error('后端未返回考试结束时间');
    }

    // 转换题目格式
    allQuestions.value = convertQuestions(data.questions).map((q, index) => ({
      ...q,
      index: index + 1
    }));

    // 恢复后端保存的答题快照（如果有）
    if (data.session.snapshotAnswers && data.session.snapshotAnswers !== '{}') {
      try {
        const snapshot = JSON.parse(data.session.snapshotAnswers);
        // 将快照答案应用到题目
        allQuestions.value.forEach(q => {
          if (snapshot[q.id]) {
            q.userAnswer = snapshot[q.id];
          }
        });
      } catch (err) {
        console.error('恢复答题快照失败:', err);
      }
    }

    // 初始化计时器
    initTimer();

    loading.value = false;
  } catch (err) {
    console.error('初始化考试失败:', err);
    error.value = err.message || '加载失败';
    loading.value = false;
  }
};

// 转换题目格式
const convertQuestions = (apiQuestions) => {
  return apiQuestions.map(q => ({
    id: String(q.id),
    type: getQuestionType(q.type),
    content: q.content,
    options: convertOptions(q.options || []),
    answer: q.answer
  }));
};

// 题目类型映射
const getQuestionType = (type) => {
  const typeMap = {
    1: 'single-choice',
    2: 'multiple-choice',
    3: 'fill-blank',
    4: 'subjective'
  };
  return typeMap[type] || 'subjective';
};

// 转换选项格式
const convertOptions = (optionsArray) => {
  if (!optionsArray || !Array.isArray(optionsArray)) {
    return {};
  }
  const optionsObj = {};
  const labels = ['A', 'B', 'C', 'D', 'E', 'F', 'G', 'H'];

  optionsArray.forEach((opt, index) => {
    if (index < labels.length) {
      const match = opt.match(/^[\(（]?([A-H])[.\.)\)）]\s*(.+)$/);
      if (match) {
        optionsObj[match[1]] = match[2];
      } else {
        optionsObj[labels[index]] = opt;
      }
    }
  });

  return optionsObj;
};

// 判断是否为选择题
const isChoiceQuestion = (type) => {
  return type === 'single-choice' || type === 'multiple-choice';
};

// 判断选项是否被选中
const isOptionSelected = (questionId, option) => {
  const answer = answers[questionId];
  if (!answer) return false;

  if (Array.isArray(answer)) {
    return answer.includes(option);
  }
  return answer === option;
};

// 处理选项变化
const handleOptionChange = (questionId, option, questionType) => {
  if (questionType === 'single-choice') {
    answers[questionId] = option;
  } else if (questionType === 'multiple-choice') {
    if (!Array.isArray(answers[questionId])) {
      answers[questionId] = [];
    }
    const index = answers[questionId].indexOf(option);
    if (index > -1) {
      answers[questionId].splice(index, 1);
    } else {
      answers[questionId].push(option);
    }
  }
  markDone(questionId);
  saveSnapshotDebounced();
};

// 标记已答
const markDone = (id) => {
  const answer = answers[id];
  if (answer && (typeof answer === 'string' ? answer.trim() : true)) {
    doneSet.add(id);
  } else {
    doneSet.delete(id);
  }
  saveSnapshotDebounced();
};

// LaTeX 渲染
const renderLatex = (latex) => {
  if (!latex) return '';
  try {
    const result = [];
    let lastIndex = 0;
    let match;
    const regex = /\$([^$]+)\$/g;

    while ((match = regex.exec(latex)) !== null) {
      const [fullMatch, formula] = match;
      const before = latex.slice(lastIndex, match.index);
      if (before) result.push(before);
      const rendered = katex.renderToString(formula, {
        throwOnError: false,
        displayMode: false,
        output: 'html',
      });
      result.push(rendered);
      lastIndex = regex.lastIndex;
    }

    const after = latex.slice(lastIndex);
    if (after) result.push(after);

    return result.join('');
  } catch (e) {
    console.error('LaTeX 渲染失败:', e);
    return latex;
  }
};

// 计时器
const initTimer = () => {
  const updateTimer = () => {
    if (!examEndTime.value) return;

    const now = Date.now();
    const remaining = Math.max(0, Math.floor((examEndTime.value - now) / 1000));
    secondsLeft.value = remaining;

    if (remaining === 0) {
      clearInterval(timerInterval);
      handleSubmit(true);
    }
  };

  // 立即更新一次
  updateTimer();

  // 每秒更新一次
  timerInterval = setInterval(updateTimer, 1000);
};

// 保存快照（防抖）
let saveSnapshotTimer = null;
const saveSnapshotDebounced = () => {
  if (saveSnapshotTimer) clearTimeout(saveSnapshotTimer);
  saveSnapshotTimer = setTimeout(() => {
    saveSnapshotToLocal();
  }, 1000);
};

// 保存快照到后端
const saveSnapshotToLocal = async () => {
  if (!sessionId.value) return;

  try {
    // 将多选答案数组转换为逗号分隔的字符串
    const answersToSave = {};
    Object.keys(answers).forEach(qid => {
      const answer = answers[qid];
      if (Array.isArray(answer)) {
        answersToSave[qid] = answer.sort().join(',');
      } else {
        answersToSave[qid] = answer;
      }
    });

    await saveSnapshot(sessionId.value, JSON.stringify(answersToSave));
  } catch (err) {
    console.error('保存快照失败:', err);
  }
};

// 保存本地状态
const saveLocalState = () => {
  localStorage.setItem('mock_exam_state', JSON.stringify({
    sessionId: sessionId.value,
    answers,
    strokes: strokes.value
  }));
};

// 滚动到题目
const scrollToQuestion = (id) => {
  const el = document.getElementById('q' + id);
  if (el) el.scrollIntoView({ behavior: 'smooth', block: 'center' });
};

// 全屏切换
const toggleFullScreen = () => {
  if (!document.fullscreenElement) {
    examAppRef.value.requestFullscreen().then(() => isFullScreen.value = true);
  } else {
    document.exitFullscreen();
    isFullScreen.value = false;
  }
};

// 侧边栏切换
const toggleSidebar = () => {
  if (showSidebar.value) {
    lastPositionX.value = position.x;
    position.x = window.innerWidth;
    showSidebar.value = false;
  } else {
    position.x = lastPositionX.value;
    if (position.x > window.innerWidth - 260) position.x = window.innerWidth - 260;
    showSidebar.value = true;
  }
};

const sidebarStyle = computed(() => ({
  left: `${position.x}px`,
  top: `${position.y}px`,
  transition: isDragging.value ? 'none' : 'left 0.4s cubic-bezier(0.4, 0, 0.2, 1), top 0.4s cubic-bezier(0.4, 0, 0.2, 1)'
}));

// 侧边栏拖动
const handleDragStart = (e) => {
  if (e.target.closest('.card-number') || e.target.closest('.toggle-handler')) return;
  isDragging.value = true;
  let startX = e.clientX - position.x;
  let startY = e.clientY - position.y;

  const handleMouseMove = (e) => {
    if (!isDragging.value) return;
    position.x = e.clientX - startX;
    position.y = e.clientY - startY;
  };

  const handleMouseUp = () => {
    isDragging.value = false;
    if (position.x < 0) position.x = 0;
    if (position.x > window.innerWidth - 260) position.x = window.innerWidth - 260;
    if (position.y < 0) position.y = 0;
    if (position.y > window.innerHeight - 100) position.y = window.innerHeight - 100;
    window.removeEventListener('mousemove', handleMouseMove);
    window.removeEventListener('mouseup', handleMouseUp);
  };
  window.addEventListener('mousemove', handleMouseMove);
  window.addEventListener('mouseup', handleMouseUp);
};

// 草稿纸功能
const openDraft = () => {
  showDraft.value = true;
  nextTick(() => {
    const canvas = draftCanvas.value;
    canvas.width = window.innerWidth;
    canvas.height = window.innerHeight;
    ctx.value = canvas.getContext('2d');
    redrawCanvas();
  });
};

const startDraw = (e) => {
  isDrawing.value = true;
  currentStroke.value = { color: brushConfig.color, size: brushConfig.size, points: [{ x: e.clientX, y: e.clientY }] };
};

const draw = (e) => {
  if (!isDrawing.value) return;
  const point = { x: e.clientX, y: e.clientY };
  currentStroke.value.points.push(point);
  drawStroke(currentStroke.value);
};

const endDraw = () => {
  if (isDrawing.value) {
    strokes.value.push(currentStroke.value);
    currentStroke.value = null;
  }
  isDrawing.value = false;
};

const drawStroke = (stroke) => {
  if (!stroke || !stroke.points.length) return;
  const c = ctx.value;
  c.beginPath();
  c.strokeStyle = stroke.color;
  c.lineWidth = stroke.size;
  c.lineCap = 'round';
  c.lineJoin = 'round';
  c.moveTo(stroke.points[0].x, stroke.points[0].y);
  stroke.points.forEach(p => c.lineTo(p.x, p.y));
  c.stroke();
};

const redrawCanvas = () => {
  if (!ctx.value) return;
  ctx.value.clearRect(0, 0, draftCanvas.value.width, draftCanvas.value.height);
  strokes.value.forEach(drawStroke);
};

const undoLast = () => { strokes.value.pop(); redrawCanvas(); };
const clearCanvas = () => { strokes.value = []; redrawCanvas(); };

// 提交试卷
const handleSubmit = (isAuto = false) => {
  if (isAuto === true) {
    confirmSubmit();
  } else {
    showConfirmModal.value = true;
  }
};

const confirmSubmit = async () => {
  showConfirmModal.value = false;
  isSubmitted.value = true;
  submitting.value = true;

  try {
    // 先保存快照
    await saveSnapshotToLocal();

    // 提交考试（后端会立即返回客观题分数，并触发异步批改主观题）
    const submitRes = await submitExamApi(sessionId.value);
    if (submitRes.code !== 200) {
      throw new Error('提交失败');
    }

    // 获取考试结果（此时包含客观题分数）
    const resultRes = await getSessionDetail(sessionId.value);
    if (resultRes.code === 200 && resultRes.data) {
      examResult.value = {
        totalScore: resultRes.data.totalScore || 0,
        aiSummary: resultRes.data.aiSummary || ''
      };
    }

    // 清除本地缓存
    localStorage.removeItem('mock_exam_state');
    localStorage.removeItem(`exam_end_time_${sessionId.value}`);

    // 清除session存储
    const { paperId, userId } = route.query;
    if (paperId && userId) {
      const storageKey = `exam_session_${paperId}_${userId}`;
      localStorage.removeItem(storageKey);
    }

    // 移除历史记录保护，允许用户正常返回
    removeHistoryProtection();

    // 显示结果（此时可能还在批改主观题）
    showResultModal.value = true;

    // 开始轮询获取完整成绩
    startPollingForResult();
  } catch (err) {
    console.error('提交考试失败:', err);
    ElMessage.error(err.message || '提交失败');
    isSubmitted.value = false;
  } finally {
    submitting.value = false;
  }
};

// 轮询获取完整成绩
let pollingInterval = null;
const startPollingForResult = () => {
  // 先清除之前的轮询
  if (pollingInterval) {
    clearInterval(pollingInterval);
  }

  // 每5秒轮询一次，最多轮询20次（100秒）
  let pollingCount = 0;
  const maxPolling = 20;

  pollingInterval = setInterval(async () => {
    pollingCount++;

    try {
      const resultRes = await getSessionDetail(sessionId.value);
      if (resultRes.code === 200 && resultRes.data) {
        // 检查是否还在批改中（aiSummary包含"批改中"）
        const isGrading = resultRes.data.aiSummary && resultRes.data.aiSummary.includes('批改中');

        if (!isGrading || pollingCount >= maxPolling) {
          // 批改完成或达到最大轮询次数，停止轮询
          clearInterval(pollingInterval);
          pollingInterval = null;

          // 更新最终结果
          examResult.value = {
            totalScore: resultRes.data.totalScore || 0,
            aiSummary: resultRes.data.aiSummary || ''
          };

          if (pollingCount >= maxPolling && isGrading) {
            ElMessage.warning('主观题批改仍在进行中，请稍后刷新查看完整成绩');
          }
        } else {
          // 还在批改中，更新当前进度
          examResult.value = {
            totalScore: resultRes.data.totalScore || 0,
            aiSummary: resultRes.data.aiSummary || ''
          };
        }
      }
    } catch (err) {
      console.error('轮询获取成绩失败:', err);
    }
  }, 5000); // 每5秒轮询一次
};

// 切屏检测
const handleVisibilityChange = () => {
  if (document.hidden && sessionId.value && !isSubmitted.value) {
    switchCount.value++;
    recordSwitch(sessionId.value).catch(err => console.error('记录切屏失败:', err));
  }
};

// 添加历史记录保护
const addHistoryProtection = () => {
  // 在历史记录中添加一个新条目
  window.history.pushState(null, '', window.location.href);
  // 监听 popstate 事件
  window.addEventListener('popstate', handlePopState);
};

// 处理返回按钮点击
const handlePopState = (event) => {
  // 如果考试已提交，不再阻止返回
  if (isSubmitted.value) {
    return;
  }

  // 再次推送状态，阻止返回
  window.history.pushState(null, '', window.location.href);

  // 增加尝试退出次数
  exitAttemptCount.value++;

  // 显示警告提示
  ElMessage.warning(`考试期间请勿离开页面!（违规尝试 ${exitAttemptCount.value} 次）`);

  // 如果用户多次尝试返回，可以强制退出
  if (exitAttemptCount.value >= 3) {
    ElMessageBox.confirm(
      '检测到您多次尝试退出考试，是否强制结束考试？',
      '警告',
      {
        confirmButtonText: '强制结束',
        cancelButtonText: '继续考试',
        type: 'warning'
      }
    ).then(() => {
      // 强制提交考试
      handleSubmit();
    }).catch(() => {
      // 用户选择继续考试
    });
  }
};

// 移除历史记录保护
const removeHistoryProtection = () => {
  window.removeEventListener('popstate', handlePopState);
};

// 返回
const goBack = () => {
  router.back();
};

const goBackToHome = () => {
  router.push('/user/paper-list');
};

// 监听状态变化
watch(answers, () => saveLocalState(), { deep: true });
watch(strokes, () => saveLocalState(), { deep: true });

// 生命周期
onMounted(() => {
  initExam();
  document.addEventListener('visibilitychange', handleVisibilityChange);
  // 添加历史记录保护，防止用户通过浏览器返回按钮离开考试页面
  addHistoryProtection();
});

onUnmounted(() => {
  if (timerInterval) clearInterval(timerInterval);
  if (pollingInterval) clearInterval(pollingInterval);
  document.removeEventListener('visibilitychange', handleVisibilityChange);
  // 移除历史记录保护
  removeHistoryProtection();
});
</script>

<style scoped>
@import url('https://fonts.googleapis.com/css2?family=Noto+Serif+SC:wght@400;700;900&display=swap');

/* 页面大容器 */
.exam-app {
  min-height: 100vh;
  background-color: #f0f2f5;
  width: 100%;
  overflow-y: auto;
}

/* 加载和错误状态 */
.loading-screen,
.error-screen {
  height: 100vh;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  background: #fff;
}

.loader {
  border: 4px solid #f3f3f3;
  border-radius: 50%;
  border-top: 4px solid #3498db;
  width: 40px;
  height: 40px;
  animation: spin 1s linear infinite;
  margin-bottom: 20px;
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

.error-content {
  text-align: center;
}

.error-content h2 {
  color: #f56c6c;
  margin-bottom: 10px;
}

/* 顶部栏 */
.system-nav {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 24px;
  height: 64px;
  background: rgba(255, 255, 255, 0.9);
  backdrop-filter: blur(10px);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
  border-bottom: 1px solid #eaeaea;
  position: fixed;
  width: 100%;
  top: 0;
  z-index: 1000;
}

.nav-left {
  display: flex;
  align-items: center;
  gap: 16px;
}

.logo-group {
  display: flex;
  flex-direction: column;
  line-height: 1.2;
}

.logo-group-eng {
  font-size: 18px;
  font-weight: 800;
  letter-spacing: 0.5px;
  color: #1a1a1a;
  font-family: 'Inter', sans-serif;
}

.logo-group-ch {
  font-size: 12px;
  color: #666;
  letter-spacing: 2px;
}

.ai-tag {
  background: linear-gradient(135deg, #6366f1 0%, #a855f7 100%);
  color: white;
  font-size: 10px;
  font-weight: bold;
  padding: 2px 8px;
  border-radius: 4px;
  letter-spacing: 0.5px;
  box-shadow: 0 2px 4px rgba(99, 102, 241, 0.3);
}

.nav-right {
  display: flex;
  align-items: center;
  gap: 20px;
  margin-right: 50px;
  font-size: 14px;
}

.top-tools {
  display: flex;
  gap: 12px;
  margin-right: 20px;
  border-right: 1px solid #eee;
  padding-right: 20px;
}

.nav-tool-btn {
  background: #f8fafc;
  border: 1px solid #e2e8f0;
  padding: 6px 12px;
  border-radius: 6px;
  font-size: 13px;
  color: #475569;
  cursor: pointer;
  transition: all 0.2s;
  display: flex;
  align-items: center;
  gap: 4px;
}

.nav-tool-btn:hover {
  background: #f1f5f9;
  border-color: #cbd5e1;
  color: #1e293b;
}

.timer-box {
  display: flex;
  align-items: center;
  background: #f4f4f7;
  padding: 6px 16px;
  border-radius: 20px;
  border: 1px solid #e5e7eb;
  height: 30px;
}

.timer-label {
  font-size: 13px;
  color: #6b7280;
  margin-right: 8px;
  white-space: nowrap;
}

.timer-value {
  font-size: 18px;
  font-weight: bold;
  color: #ef4444;
  text-align: center;
}

button {
  cursor: pointer;
  transition: all 0.2s ease;
  border: none;
  outline: none;
}

.fullscreen-btn {
  background: none;
  border: none;
  color: #64748b;
  font-size: 14px;
  padding: 0 8px;
  margin-right: 8px;
  border-right: 1px solid #e2e8f0;
  cursor: pointer;
  display: flex;
  align-items: center;
  height: 100%;
}

.fullscreen-btn:hover {
  background: #e5e7eb;
  color: #111827;
}

.submit-btn {
  background: #2563eb;
  color: white;
  padding: 8px 20px;
  border-radius: 6px;
  font-weight: 600;
  font-size: 14px;
  box-shadow: 0 4px 6px -1px rgba(37, 99, 235, 0.2);
}

.submit-btn:hover {
  background: #1d4ed8;
  transform: translateY(-1px);
  box-shadow: 0 6px 12px -2px rgba(37, 99, 235, 0.3);
}

/* 禁用状态 */
.submit-btn:disabled,
.submit-btn--disabled {
  background: #cbd5e1; /* 灰色 */
  color: #94a3b8;
  cursor: not-allowed;
  box-shadow: none;
  transform: none;
}

.submit-btn:not(:disabled):hover {
  background: #3b82f6;
  transform: translateY(-2px);
  box-shadow: 0 4px 8px rgba(37, 99, 235, 0.4);
}

.main-layout {
  max-width: 1400px;
  margin: 0 auto;
  display: flex;
  gap: 24px;
  /* padding: 24px; */
  /* padding-top: 0px; */
}

.paper-wrapper {
  margin: 0 auto;
  display: flex;
  gap: 24px;
  padding: 24px;
}

.paper-page-container {
  display: flex;
  flex-direction: column;
  gap: 30px;
  padding-bottom: 50px;
}

.paper-sheet {
  background: white;
  width: 900px;
  min-height: 1100px;
  margin: 0 auto;
  margin-top: 40px;
  padding: 30mm 20mm 20mm 35mm;
  position: relative;
  box-shadow: 0 0 15px rgba(0, 0, 0, 0.1);
  font-family: 'Noto Serif SC', serif;
}

.sealing-line {
  position: absolute;
  left: 0;
  top: 0;
  bottom: 0;
  width: 5px;
  display: flex;
  flex-direction: column;
  justify-content: space-around;
  padding: 30px;
  background-color: #fff;
  border-right: 2px dashed #555;
  box-sizing: border-box;
  z-index: 10;
}

.seal-warning {
  writing-mode: vertical-rl;
  font-size: 14px;
  font-weight: bold;
  color: #666;
  letter-spacing: 20px;
  padding: 10px;
}

.confidential-mark {
  font-weight: bold;
  font-size: 16px;
  color: #c8102e;
  margin-bottom: 20px;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
}

.confidential-mark .star {
  color: #c8102e;
  font-size: 18px;
}

.paper-header {
  text-align: center;
}

.main-title {
  font-size: 24px;
  font-weight: 900;
  color: #1a1a1a;
  margin: 20px 0;
  font-family: 'Noto Serif SC', serif;
}

.title-divider {
  width: 300px;
  height: 2px;
  background: linear-gradient(to right, transparent, #1a1a1a, transparent);
  margin: 20px auto;
}

.exam-info {
  display: flex;
  justify-content: center;
  gap: 40px;
  font-size: 16px;
  margin-bottom: 30px;
  color: #636363;
  font-style: italic;
}

.notice-box {
  border: 2px solid black;
  margin: 20px auto;
  padding: 5px 15px;
  text-align: left;
  font-size: 14px;
  line-height: 1.6;
}

.notice-title {
  font-weight: bold;
  margin-left: 0;
  padding-left: 0;
}

.notice-list {
  padding-left: 1.2em;
  margin: 0;
  list-style-position: outside;
  margin-bottom: 10px;
}

.section-banner {
  background: #eef3f8;
  padding: 8px;
  font-weight: bold;
  margin-top: 20px;
}

.question-item {
  padding: 5px;
  transition: background-color 0.3s;
  border-radius: 10px;
}

.question-title {
  font-size: 16px;
  line-height: 1.6;
  color: #1a1a1a;
  margin-bottom: 16px;
  font-weight: 500;
  display: flex;
  align-items: flex-start;
}

.question-number {
  font-weight: 700;
  color: #1a1a1a;
  margin-right: 8px;
  flex-shrink: 0;
  font-size: 16px;
}

.options-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 12px 24px;
  padding-left: 10px;
}

.option-label {
  display: flex;
  align-items: flex-start;
  gap: 10px;
  cursor: pointer;
  padding: 10px 14px;
  border: 1px solid transparent;
  border-radius: 6px;
  background-color: #f9fafb;
  transition: all 0.2s ease;
}

.option-label:hover {
  background-color: #f3f4f6;
  border-color: #d1d5db;
}

.option-label:has(input:checked),
.option-label:has(input:checked ~ input) {
  background-color: #eff6ff;
  border-color: #3b82f6;
  color: #1d4ed8;
  font-weight: 600;
}

.option-label input[type="radio"],
.option-label input[type="checkbox"] {
  margin-top: 4px;
  cursor: pointer;
  accent-color: #2563eb;
}

.option-text {
  font-size: 15px;
  line-height: 1.5;
  color: inherit;
}

.answer-area {
  width: 100%;
  padding: 10px;
  border: 1px solid #ccc;
}

.paper-footer {
  margin-top: auto;
  padding: 20px 0;
  text-align: center;
  font-size: 12px;
  color: #666;
}

:deep(.katex) {
  font-size: 1.05em;
}

.question-title :deep(.katex) {
  font-size: 1.1em;
}

.option-text :deep(.katex) {
  font-size: 1.05em;
}

.sidebar-container {
  position: fixed;
  z-index: 2000;
  width: 260px;
  transition: left 0.4s cubic-bezier(0.4, 0, 0.2, 1), top 0.4s cubic-bezier(0.4, 0, 0.2, 1);
  user-select: none;
}

.toggle-handler {
  position: absolute;
  left: -30px;
  top: 50%;
  transform: translateY(-50%);
  width: 30px;
  height: 100px;
  background: #2563eb;
  color: white;
  border-radius: 8px 0 0 8px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  box-shadow: -2px 0 5px rgba(0, 0, 0, 0.1);
}

.toggle-text {
  writing-mode: vertical-lr;
  font-size: 12px;
  margin-top: 5px;
  letter-spacing: 2px;
}

.sidebar-card {
  background: white;
  border-radius: 12px;
  padding: 16px;
  border: 1px solid #e2e8f0;
  box-shadow: 0 10px 25px rgba(0, 0, 0, 0.1);
}

.card-title {
  font-size: 15px;
  font-weight: 600;
  color: #1e293b;
  margin-bottom: 16px;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.count-tag {
  font-size: 12px;
  background: #f1f5f9;
  color: #64748b;
  padding: 2px 8px;
  border-radius: 10px;
  font-family: monospace;
}

.answer-card-grid {
  display: grid;
  grid-template-columns: repeat(5, 1fr);
  gap: 8px;
}

.card-number {
  height: 32px;
  border-radius: 6px;
  border: 1px solid #e2e8f0;
  background: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  font-size: 13px;
  color: #64748b;
  transition: all 0.2s ease;
}

.card-number:hover {
  border-color: #2563eb;
  color: #2563eb;
  background: #eff6ff;
}

.card-number.is-done {
  background: #2563eb;
  border-color: #2563eb;
  color: white;
  font-weight: bold;
  box-shadow: 0 2px 4px rgba(37, 99, 235, 0.2);
}

.full-page-draft {
  position: fixed;
  top: 0;
  left: 0;
  width: 100vw;
  height: 100vh;
  z-index: 3000;
  background: rgba(255, 255, 255, 0.4);
  backdrop-filter: blur(2px);
}

.draft-toolbar {
  position: absolute;
  top: 80px;
  left: 50%;
  transform: translateX(-50%);
  background: white;
  padding: 10px 20px;
  border-radius: 30px;
  display: flex;
  align-items: center;
  gap: 15px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.15);
  z-index: 3001;
}

.draft-canvas {
  cursor: crosshair;
}

.draft-btn {
  padding: 6px 12px;
  border-radius: 6px;
  border: 1px solid #ddd;
  background: white;
  cursor: pointer;
  font-size: 13px;
}

.draft-btn:hover {
  background: #f4f4f4;
}

.draft-btn.primary {
  background: #2563eb;
  color: white;
  border: none;
}

.draft-btn.danger {
  color: #ef4444;
}

.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: rgba(0, 0, 0, 0.6);
  backdrop-filter: blur(4px);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 9999;
}

.modal-content {
  background: white;
  padding: 40px;
  border-radius: 16px;
  text-align: center;
  width: 400px;
  box-shadow: 0 20px 40px rgba(0, 0, 0, 0.2);
}

.modal-icon {
  font-size: 48px;
  margin-bottom: 15px;
}

.modal-title {
  font-size: 22px;
  color: #333;
  margin-bottom: 15px;
}

.modal-tips {
  color: #666;
  line-height: 1.6;
  margin-bottom: 30px;
}

.highlight {
  color: #e74c3c;
  font-weight: bold;
  font-size: 1.1em;
}

.modal-btns {
  display: flex;
  gap: 15px;
  justify-content: center;
  margin-top: 20px;
}

.modal-btn {
  padding: 12px 25px;
  border-radius: 8px;
  border: none;
  cursor: pointer;
  font-weight: bold;
  transition: all 0.3s;
}

.modal-btn.cancel {
  background: #f5f5f5;
  color: #666;
}

.modal-btn.cancel:hover {
  background: #e0e0e0;
}

.modal-btn.confirm {
  background: #007aff;
  color: white;
}

.modal-btn.confirm:hover {
  background: #0056b3;
  transform: translateY(-2px);
}

.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.3s;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}

.report-modal {
  width: 600px;
  max-width: 90vw;
  text-align: left;
  padding: 30px;
}

.modal-header {
  text-align: center;
  margin-bottom: 20px;
}

.modal-icon.success {
  color: #10b981;
}

.report-body {
  max-height: 400px;
  overflow-y: auto;
}

.report-card-content {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.score-box {
  background: linear-gradient(135deg, #fffbeb 0%, #fef3c7 100%);
  border: 1px solid #fcd34d;
  padding: 20px;
  border-radius: 12px;
  text-align: center;
  color: #92400e;
}

.score-label {
  display: block;
  font-size: 14px;
  opacity: 0.8;
  margin-bottom: 5px;
}

.score-value {
  font-size: 48px;
  font-weight: 900;
  line-height: 1;
}

.score-total {
  font-size: 16px;
  opacity: 0.6;
  margin-left: 5px;
}

.analysis-box {
  background: #f0f9ff;
  border: 1px solid #bae6fd;
  padding: 20px;
  border-radius: 12px;
  color: #0369a1;
}

.analysis-box h4 {
  margin: 0 0 10px 0;
  font-size: 16px;
  font-weight: bold;
}

.analysis-box p {
  margin: 0;
  font-size: 14px;
  line-height: 1.6;
}


.submitted-mask {
  position: relative;
}

.submitted-mask::after {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  z-index: 100;
}

/* 回到主页按钮 */
.home-fab {
  position: fixed;
  bottom: 32px;
  right: 32px;
  width: 56px;
  height: 56px;
  border-radius: 50%;

  background: rgba(37, 100, 235, 0.849);
  backdrop-filter: blur(8px);
  -webkit-backdrop-filter: blur(8px);

  border: 1px solid rgba(255, 255, 255, 0.25);
  box-shadow:
    0 6px 20px rgba(37, 99, 235, 0.3),
    0 3px 8px rgba(0, 0, 0, 0.08),
    inset 0 1px 0 rgba(255, 255, 255, 0.15);

  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  z-index: 1000;

  transition: all 0.4s cubic-bezier(0.34, 1.56, 0.64, 1);
  outline: none;
}

/* 悬停 */
.home-fab:hover {
  background: rgb(37, 100, 235);
  transform: translateY(-4px) scale(1.08);
  box-shadow:
    0 10px 30px rgba(37, 99, 235, 0.4),
    0 5px 12px rgba(0, 0, 0, 0.12),
    inset 0 1px 0 rgba(255, 255, 255, 0.25);
}

/* 点击 */
.home-fab:active {
  transform: translateY(0) scale(1.02);
  background: rgba(37, 99, 235, 0.5);
  box-shadow:
    0 4px 16px rgba(37, 99, 235, 0.25),
    0 2px 6px rgba(0, 0, 0, 0.06),
    inset 0 1px 0 rgba(255, 255, 255, 0.1);
}

/* 图标 */
.home-icon {
  width: 26px;
  height: 26px;
  fill: rgb(255, 255, 255);
  filter: drop-shadow(0 0 2px rgba(255, 255, 255, 0.4));
}

</style>
