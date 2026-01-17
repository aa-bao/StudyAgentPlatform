import { createRouter, createWebHistory } from 'vue-router'
import { ElMessage } from 'element-plus'

const routes = [
  // 登录页
  {
    path: '/',
    redirect: '/login'
  },
  {
    path: '/login',
    component: () => import('@/views/Login.vue')
  },
  // 用户端
  {
    path: '/user',
    component: () => import('@/views/layout/UserLayout.vue'),
    children: [
      {
      // 首页
        path: 'home',
        component: () => import('@/views/Home.vue'),
        meta: { hideLayout: true }
      },
      {
      // 备考面板
        path: 'dashboard',
        component: () => import('@/views/Dashboard.vue')
      },
      {
      // 错题本
        path: 'correction-notebook',
        component: () => import('@/views/CorrectionNotebook.vue')
      },
      {
      // 个人资料
        path: 'profile',
        component: () => import('@/views/UserProfile.vue')
      },
      // 题库页面
      {
        path: 'subject',
        component: () => import('@/views/SubjectList.vue')
      },
      // 逐题精练
      {
        path: 'single-practice/:subjectId',
        component: () => import('@/views/quiz/SinglePractice.vue'),
        props: true
      },
      // 专项突破
      {
        path: 'topic-drill',
        component: () => import('@/views/quiz/TopicDrill.vue')
      },
      // 套卷刷题列表
       {
         path: 'paper-list',
         component: () => import('@/views/PaperList.vue')
       },
      // 沉浸式套卷刷题页面
       {
         path: 'mock-exam',
         component: () => import('@/views/quiz/MockExam.vue'),
         meta: { hideLayout: true }
       },
    ]
  },

  // 管理端
  {
    path: '/admin',
    component: () => import('@/views/layout/AdminLayout.vue'),
    children: [{
        // 后台首页
        path: 'home',
        component: () => import('@/views/admin/AdminHome.vue')
      },
      {
        // 用户管理
        path: 'users-manage',
        component: () => import('@/views/admin/UserManage.vue')
      },
      {
        // 错题监控
        path: 'mistake-monitor',
        component: () => import('@/views/admin/MistakeMonitor.vue')
      },
      {
        // 科目体系管理
        path: 'subjects-manage',
        component: () => import('@/views/admin/SubjectManage.vue')
      },
      {
        // 习题册管理
        path: 'books-manage',
        component: () => import('@/views/admin/BookManage.vue')
      },
      {
        // 题目管理
        path: 'questions-manage',
        component: () => import('@/views/admin/QuestionManage.vue')
      },
      {
        // 试卷管理
        path: 'papers-manage',
        component: () => import('@/views/admin/PaperManage.vue')
      },
      {
        // 考试记录管理
        path: 'exam-record-manage',
        component: () => import('@/views/admin/ExamRecordManage.vue')
      },
      {
        // 学习进度监控
        path: 'user-progress-monitor',
        component: () => import('@/views/admin/UserProgressMonitor.vue')
      },
      {
        // 题目批量导入（JSON文件）
        path: 'question-import',
        component: () => import('@/views/admin/QuestionImport.vue')
      },
      {
        // 题目导出
        path: 'question-export',
        component: () => import('@/views/admin/QuestionExport.vue')
      },
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

/**
 * 核心逻辑：路由守卫
 */
router.beforeEach((to, from, next) => {
  const role = localStorage.getItem('role')
  const userStr = localStorage.getItem('user') // 修正 key 为 'user'，与 store 保持一致
  let userRole = null
  let isLogin = false

  if (role && userStr) {
    try {
      const userObj = JSON.parse(userStr)
      if (userObj.role === role) {
        userRole = role
        isLogin = true
      }
    } catch (e) {
      console.error('用户信息解析失败', e)
    }
  }

  if (!isLogin && (role || userStr)) {
    localStorage.removeItem('role')
    localStorage.removeItem('user')
  }

  console.log('当前路由去向:', to.path)
  console.log('登录状态:', isLogin, '角色:', userRole)

  // 2. 路由逻辑判断
  if (to.path === '/login') {
    // 如果已登录，且访问登录页 -> 自动跳转到对应首页
    if (isLogin) {
      next(userRole === 'admin' ? '/admin/home' : '/user/dashboard')
    } else {
      // 未登录 -> 放行进入登录页
      next()
    }
  } else if (to.path.startsWith('/admin')) {
    if (!isLogin) {
      next('/login')
    } else if (userRole !== 'admin') {
      ElMessage.error("权限不足，无法进入管理后台")
      next('/user/dashboard')
    } else {
      next()
    }
  } else {
    if (!isLogin) {
      next('/login')
    } else {
      next()
    }
  }
})

export default router