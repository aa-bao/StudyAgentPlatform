<template>
  <div class="paper-exam-container">
    <div class="page-header">
      <h1>套卷刷题</h1>
      <p>选择一份试卷开始模考</p>
    </div>

    <!-- 筛选栏 -->
    <div class="filter-bar">
      <div class="filter-group">
        <el-radio-group v-model="filterType" @change="loadPapers">
          <el-radio-button :label="null">全部</el-radio-button>
          <el-radio-button :label="0">真题</el-radio-button>
          <el-radio-button :label="1">模拟题</el-radio-button>
        </el-radio-group>

        <el-select
          v-model="filterYear"
          placeholder="选择年份"
          clearable
          @change="loadPapers"
          style="width: 150px; margin-left: 12px;"
        >
          <el-option
            v-for="year in availableYears"
            :key="year"
            :label="year + '年'"
            :value="year"
          />
        </el-select>
      </div>

      <el-input
        v-model="searchKeyword"
        placeholder="搜索试卷名称"
        style="width: 300px;"
        clearable
        @clear="loadPapers"
        @keyup.enter="loadPapers"
      >
        <template #append>
          <el-button :icon="Search" @click="loadPapers" />
        </template>
      </el-input>
    </div>

    <!-- 试卷列表 -->
    <div v-loading="loading" class="paper-list">
      <el-row :gutter="20">
        <el-col
          v-for="paper in papers"
          :key="paper.id"
          :xs="24"
          :sm="12"
          :md="8"
          :lg="6"
        >
          <el-card class="paper-card" shadow="hover" @click="startExam(paper.id)">
            <div class="paper-header">
              <div class="header-tags">
                <el-tag v-if="paper.paperType === 0" type="success" size="small">真题</el-tag>
                <el-tag v-else type="warning" size="small">模拟题</el-tag>
                <el-tag v-if="paper.year" type="info" size="small">{{ paper.year }}年</el-tag>
              </div>
            </div>
            <h3 class="paper-title">{{ paper.title }}</h3>
            <div class="paper-info">
              <div class="info-item">
                <el-icon><Timer /></el-icon>
                <span>{{ paper.timeLimit || 180 }} 分钟</span>
              </div>
              <div class="info-item">
                <el-icon><Document /></el-icon>
                <span>{{ paper.totalScore || 150 }} 分</span>
              </div>
            </div>
            <div class="paper-footer">
              <el-button type="primary" size="small" @click.stop="startExam(paper.id)">
                开始考试
              </el-button>
            </div>
          </el-card>
        </el-col>
      </el-row>

      <el-empty v-if="!loading && papers.length === 0" description="暂无试卷数据" />
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Timer, Document } from '@element-plus/icons-vue'
import { getPaperPage } from '@/api/paper'

const router = useRouter()

const loading = ref(false)
const papers = ref([])
const filterType = ref(null)
const filterYear = ref(null)
const searchKeyword = ref('')
const availableYears = ref([])

// 生成年份列表(最近10年)
const generateYears = () => {
  const currentYear = new Date().getFullYear()
  const years = []
  for (let i = 0; i < 10; i++) {
    years.push(currentYear - i)
  }
  availableYears.value = years
}

// 加载试卷列表
const loadPapers = async () => {
  try {
    loading.value = true

    const params = {
      pageNum: 1,
      pageSize: 100  // 获取前100条数据
    }
    if (filterType.value !== null) {
      params.paperType = filterType.value
    }
    if (filterYear.value !== null) {
      params.year = filterYear.value
    }
    if (searchKeyword.value) {
      params.keyword = searchKeyword.value
    }

    const res = await getPaperPage(params)
    if (res.code === 200) {
      papers.value = res.data.records || []
    }
  } catch (error) {
    console.error('加载试卷列表失败:', error)
    ElMessage.error('加载试卷列表失败')
  } finally {
    loading.value = false
  }
}

// 开始考试
const startExam = async (paperId) => {
  // 获取当前用户信息
  const userStr = localStorage.getItem('user')
  if (!userStr) {
    ElMessage.warning('请先登录')
    router.push('/login')
    return
  }

  try {
    const user = JSON.parse(userStr)

    // 弹出确认弹窗
    await ElMessageBox.confirm(
      '开始考试后将进入全屏模式，考试过程中请勿离开页面，确认开始吗？',
      '开始考试确认',
      {
        confirmButtonText: '确认开始',
        cancelButtonText: '再想想',
        type: 'info',
        customClass: 'exam-confirm-box'
      }
    )

    // 用户确认后跳转到沉浸式考试页面，传递试卷ID和用户ID
    // 使用 replace 而非 push，防止用户通过浏览器返回按钮回到试卷列表
    router.replace({
      path: '/user/mock-exam',
      query: {
        paperId: paperId,
        userId: user.id
      }
    })
  } catch (error) {
    // 用户取消操作
    if (error === 'cancel') {
      return
    }
    console.error('解析用户信息失败:', error)
    ElMessage.error('获取用户信息失败')
  }
}

onMounted(() => {
  generateYears()
  loadPapers()
})
</script>

<style scoped>
.paper-exam-container {
  padding: 24px;
  max-width: 1400px;
  margin: 0 auto;
  min-height: 100vh;
  background: linear-gradient(135deg, #f0f9ff 0%, #e6f7ff 100%);
  font-family: 'Segoe UI', 'PingFang SC', 'Microsoft YaHei', sans-serif;
}

.page-header {
  margin-bottom: 32px;
  text-align: center;
  animation: fadeInDown 0.6s ease-out;
}

@keyframes fadeInDown {
  from {
    opacity: 0;
    transform: translateY(-20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.page-header h1 {
  margin: 0 0 12px 0;
  font-size: 36px;
  font-weight: 700;
  background: linear-gradient(to right, #3b82f6, #60a5fa);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.page-header p {
  margin: 0;
  font-size: 16px;
  color: #6b7280;
  font-weight: 500;
}

.filter-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 32px;
  padding: 20px 24px;
  background: rgba(255, 255, 255, 0.8);
  backdrop-filter: blur(10px);
  border-radius: 16px;
  box-shadow: 0 4px 16px rgba(59, 130, 246, 0.1);
  border: 1px solid rgba(209, 213, 219, 0.3);
  animation: fadeIn 0.6s ease-out 0.2s backwards;
}

.filter-group {
  display: flex;
  align-items: center;
  gap: 12px;
}

@keyframes fadeIn {
  from {
    opacity: 0;
  }
  to {
    opacity: 1;
  }
}

.paper-list {
  min-height: 400px;
}

.paper-card {
  cursor: pointer;
  transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
  margin-bottom: 20px;
  border-radius: 16px;
  overflow: hidden;
  border: 1px solid rgba(209, 213, 219, 0.3);
  background: rgba(255, 255, 255, 0.8);
  backdrop-filter: blur(10px);
  animation: scaleIn 0.5s ease-out backwards;
  position: relative;
}

.paper-card:nth-child(1) { animation-delay: 0.05s; }
.paper-card:nth-child(2) { animation-delay: 0.1s; }
.paper-card:nth-child(3) { animation-delay: 0.15s; }
.paper-card:nth-child(4) { animation-delay: 0.2s; }
.paper-card:nth-child(5) { animation-delay: 0.25s; }
.paper-card:nth-child(6) { animation-delay: 0.3s; }

@keyframes scaleIn {
  from {
    opacity: 0;
    transform: scale(0.9);
  }
  to {
    opacity: 1;
    transform: scale(1);
  }
}

.paper-card::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 4px;
  background: linear-gradient(90deg, #3b82f6, #60a5fa);
  transform: scaleX(0);
  transition: transform 0.4s ease;
}

.paper-card:hover::before {
  transform: scaleX(1);
}

.paper-card:hover {
  transform: translateY(-8px) scale(1.02);
  box-shadow: 0 12px 32px rgba(59, 130, 246, 0.2);
  border-color: rgba(59, 130, 246, 0.3);
}

.paper-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.header-tags {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}

.paper-title {
  margin: 0 0 20px 0;
  font-size: 18px;
  font-weight: 600;
  color: #1f2937;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  line-clamp: 2; 
  -webkit-box-orient: vertical;
  min-height: 52px;
  line-height: 1.5;
  transition: color 0.3s ease;
}

.paper-card:hover .paper-title {
  color: #3b82f6;
}

.paper-info {
  display: flex;
  gap: 20px;
  margin-bottom: 20px;
  padding: 12px;
  background: rgba(59, 130, 246, 0.05);
  border-radius: 8px;
}

.info-item {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 14px;
  color: #4b5563;
  font-weight: 500;
}

.info-item .el-icon {
  color: #3b82f6;
  font-size: 16px;
}

.paper-footer {
  display: flex;
  justify-content: flex-end;
  gap: 8px;
}

/* 美化按钮 */
.paper-footer :deep(.el-button) {
  background: linear-gradient(45deg, #3b82f6, #60a5fa);
  border: none;
  border-radius: 8px;
  padding: 10px 20px;
  font-weight: 500;
  transition: all 0.3s ease;
}

.paper-footer :deep(.el-button:hover) {
  transform: translateY(-2px);
  box-shadow: 0 6px 16px rgba(59, 130, 246, 0.3);
}

/* 美化标签 */
.paper-header :deep(.el-tag) {
  border-radius: 6px;
  padding: 4px 12px;
  font-weight: 500;
  border: none;
}

/* 美化筛选器 */
.filter-bar :deep(.el-radio-group) {
  display: flex;
  gap: 8px;
}

/* 美化年份选择器 */
.filter-bar :deep(.el-select) {
  --el-select-input-focus-border-color: #3b82f6;
}

.filter-bar :deep(.el-select .el-input__wrapper) {
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
  transition: all 0.3s ease;
}

.filter-bar :deep(.el-select .el-input__wrapper:hover) {
  box-shadow: 0 4px 12px rgba(59, 130, 246, 0.2);
}

.filter-bar :deep(.el-radio-button__inner) {
  border-radius: 8px;
  padding: 10px 20px;
  font-weight: 500;
  transition: all 0.3s ease;
  border: 1px solid rgba(209, 213, 219, 0.3);
}

.filter-bar :deep(.el-radio-button__original-radio:checked + .el-radio-button__inner) {
  background: linear-gradient(45deg, #3b82f6, #60a5fa);
  border-color: #3b82f6;
  box-shadow: 0 4px 12px rgba(59, 130, 246, 0.3);
}

/* 美化搜索框 */
.filter-bar :deep(.el-input__wrapper) {
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
  transition: all 0.3s ease;
}

.filter-bar :deep(.el-input__wrapper:hover),
.filter-bar :deep(.el-input__wrapper.is-focus) {
  box-shadow: 0 4px 12px rgba(59, 130, 246, 0.2);
}

.filter-bar :deep(.el-input-group__append) {
  background: linear-gradient(45deg, #3b82f6, #60a5fa);
  border: none;
  color: white;
  border-radius: 0 8px 8px 0;
}

.filter-bar :deep(.el-input-group__append .el-button) {
  background: transparent;
  border: none;
  color: white;
}

/* 响应式调整 */
@media (max-width: 768px) {
  .paper-exam-container {
    padding: 16px;
  }

  .page-header h1 {
    font-size: 28px;
  }

  .filter-bar {
    flex-direction: column;
    gap: 16px;
    padding: 16px;
  }

  .filter-group {
    flex-direction: column;
    width: 100%;
  }

  .filter-bar :deep(.el-radio-group) {
    width: 100%;
    justify-content: center;
  }

  .filter-bar :deep(.el-select) {
    width: 100% !important;
    margin-left: 0 !important;
  }

  .filter-bar :deep(.el-input) {
    width: 100% !important;
  }

  .paper-info {
    flex-direction: column;
    gap: 8px;
  }
}

/* 美化确认弹窗 */
:deep(.exam-confirm-box) {
  border-radius: 16px;
  overflow: hidden;
}

:deep(.exam-confirm-box .el-message-box__header) {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  padding: 20px 24px;
}

:deep(.exam-confirm-box .el-message-box__title) {
  color: white;
  font-size: 18px;
  font-weight: 600;
}

:deep(.exam-confirm-box .el-message-box__headerbtn .el-message-box__close) {
  color: white;
  font-size: 18px;
}

:deep(.exam-confirm-box .el-message-box__content) {
  padding: 24px;
  font-size: 15px;
  color: #4b5563;
  line-height: 1.6;
}

:deep(.exam-confirm-box .el-message-box__btns) {
  padding: 12px 24px 20px;
  display: flex;
  gap: 12px;
  justify-content: flex-end;
}

:deep(.exam-confirm-box .el-button--primary) {
  background: linear-gradient(45deg, #3b82f6, #60a5fa);
  border: none;
  border-radius: 8px;
  padding: 10px 24px;
  font-weight: 500;
  transition: all 0.3s ease;
}

:deep(.exam-confirm-box .el-button--primary:hover) {
  transform: translateY(-2px);
  box-shadow: 0 6px 16px rgba(59, 130, 246, 0.3);
}

:deep(.exam-confirm-box .el-button--default) {
  border-radius: 8px;
  padding: 10px 24px;
  font-weight: 500;
  border: 1px solid rgba(209, 213, 219, 0.5);
  transition: all 0.3s ease;
}

:deep(.exam-confirm-box .el-button--default:hover) {
  border-color: #3b82f6;
  color: #3b82f6;
}
</style>
