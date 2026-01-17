<template>
  <div class="login-container">
    <!-- 背景装饰圆 -->
    <div class="bg-circle circle-1"></div>
    <div class="bg-circle circle-2"></div>

    <div class="login-box glass-effect">
      <div class="login-left">
        <div class="welcome-text">
          <h1>{{ isLogin ? '欢迎回来' : '加入我们' }}</h1>
          <p>{{ isLogin ? '继续您的考研复习之旅' : '开启您的研究生梦想之路' }}</p>
        </div>
        <div class="illustration-placeholder">
          <el-icon :size="120" color="rgba(255,255,255,0.8)">
            <Reading />
          </el-icon>
        </div>
      </div>

      <div class="login-right">
        <div class="login-header">
          <h2>{{ isLogin ? '账号登录' : '创建账号' }}</h2>
          <span class="subtitle">{{ isLogin ? '请输入您的账号密码' : '填写以下信息完成注册' }}</span>
        </div>

        <el-form :model="form" :rules="rules" ref="formRef" class="custom-form" size="large">
          <el-form-item prop="username">
            <el-input v-model="form.username" placeholder="用户名 / 手机号" prefix-icon="User" class="custom-input" />
          </el-form-item>

          <el-form-item prop="password">
            <el-input v-model="form.password" type="password" placeholder="密码" prefix-icon="Lock" show-password
              class="custom-input" />
          </el-form-item>

          <transition name="fade-slide">
            <el-form-item v-if="!isLogin" prop="nickname">
              <el-input v-model="form.nickname" placeholder="您的昵称" prefix-icon="Edit" class="custom-input" />
            </el-form-item>
          </transition>

          <el-form-item>
            <el-button type="primary" @click="handleSubmit" class="submit-btn" :loading="loading">
              {{ isLogin ? '登 录' : '注 册' }}
              <el-icon class="el-icon--right">
                <Right />
              </el-icon>
            </el-button>
          </el-form-item>

          <div class="switch-link">
            <span>{{ isLogin ? '还没有账号？' : '已经有账号了？' }}</span>
            <a @click="toggleMode" class="link-text">{{ isLogin ? '立即注册' : '去登录' }}</a>
          </div>
        </el-form>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { User, Lock, Edit, Right, Reading } from '@element-plus/icons-vue'
import { loginApi, registerApi } from '../api/user'
import { useUserStore } from '../stores/user'

const router = useRouter()
const userStore = useUserStore()
const formRef = ref(null)
const loading = ref(false)
const isLogin = ref(true) // 控制登录/注册状态

const form = reactive({
  username: '',
  password: '',
  nickname: ''
})

const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, message: '密码长度至少为6位', trigger: 'blur' }
  ],
  nickname: [{ required: false }]
}

// 切换模式并重置表单校验
const toggleMode = () => {
  isLogin.value = !isLogin.value
  formRef.value?.resetFields()
}

const handleSubmit = () => {
  formRef.value.validate(async (valid) => {
    if (!valid) return

    loading.value = true
    try {
      if (isLogin.value) {
        // 登录逻辑
        const res = await loginApi({
          username: form.username,
          password: form.password
        })
        console.log('登录提交表单：', form)

        ElMessage.success('登录成功')

        // 1. 调用 store 存入全部数据（含 role）
        userStore.setUserInfo(res.data)

        // 2. 再次手动确认 localStorage 存入（双重保险）
        localStorage.setItem('role', res.data.role)

        // 3. 延迟一丁点时间跳转，确保缓存写入
        setTimeout(() => {
          if (res.data.role === 'admin') {
            router.push('/admin/home')
          } else {
            router.push('/user/dashboard')
          }
        }, 100)

      } else {
        // 注册逻辑  
        const res = await registerApi(form)
        ElMessage.success('注册成功，请登录')
        isLogin.value = true // 注册后自动跳回登录
      }
    } catch (error) {
      console.error('登录/注册流程中断:', error)
    } finally {
      loading.value = false
    }
  })
}
</script>

<style scoped>
.login-container {
  height: 100vh;
  width: 100vw;
  display: flex;
  justify-content: center;
  align-items: center;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  position: relative;
  overflow: hidden;
}

/* 背景装饰 */
.bg-circle {
  position: absolute;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.1);
  backdrop-filter: blur(10px);
  z-index: 0;
}

.circle-1 {
  width: 300px;
  height: 300px;
  top: -50px;
  left: -50px;
}

.circle-2 {
  width: 400px;
  height: 400px;
  bottom: -100px;
  right: -100px;
}

.login-box {
  position: relative;
  z-index: 1;
  display: flex;
  width: 900px;
  height: 550px;
  background: rgba(255, 255, 255, 0.95);
  border-radius: 20px;
  box-shadow: 0 20px 50px rgba(0, 0, 0, 0.2);
  overflow: hidden;
}

/* 左侧装饰区 */
.login-left {
  flex: 1;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  padding: 40px;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  color: #fff;
  text-align: center;
  position: relative;
}

.welcome-text h1 {
  font-size: 32px;
  margin-bottom: 10px;
  font-weight: 700;
}

.welcome-text p {
  font-size: 16px;
  opacity: 0.9;
}

.illustration-placeholder {
  margin-top: 60px;
  animation: float 6s ease-in-out infinite;
}

@keyframes float {
  0% {
    transform: translateY(0px);
  }

  50% {
    transform: translateY(-20px);
  }

  100% {
    transform: translateY(0px);
  }
}

/* 右侧表单区 */
.login-right {
  flex: 1;
  padding: 50px;
  display: flex;
  flex-direction: column;
  justify-content: center;
  background: #fff;
}

.login-header {
  margin-bottom: 40px;
}

.login-header h2 {
  font-size: 28px;
  color: #333;
  margin-bottom: 8px;
  font-weight: 700;
}

.subtitle {
  color: #999;
  font-size: 14px;
}

.custom-form {
  margin-top: 20px;
}

.custom-input :deep(.el-input__wrapper) {
  border-radius: 25px;
  background-color: #f5f7fa;
  box-shadow: none !important;
  padding: 0 20px;
  transition: all 0.3s;
}

.custom-input :deep(.el-input__wrapper.is-focus) {
  background-color: #fff;
  box-shadow: 0 0 0 1px #764ba2 !important;
}

.submit-btn {
  width: 100%;
  border-radius: 25px;
  height: 45px;
  font-size: 16px;
  font-weight: 600;
  background: linear-gradient(to right, #667eea, #764ba2);
  border: none;
  margin-top: 10px;
  transition: transform 0.2s;
}

.submit-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 5px 15px rgba(118, 75, 162, 0.3);
}

.switch-link {
  text-align: center;
  margin-top: 20px;
  font-size: 14px;
  color: #666;
}

.link-text {
  color: #764ba2;
  cursor: pointer;
  font-weight: 600;
  margin-left: 5px;
  transition: color 0.2s;
}

.link-text:hover {
  color: #667eea;
  text-decoration: underline;
}

/* 动画 */
.fade-slide-enter-active,
.fade-slide-leave-active {
  transition: all 0.3s ease;
}

.fade-slide-enter-from,
.fade-slide-leave-to {
  opacity: 0;
  transform: translateY(-10px);
}

/* 响应式适配 */
@media (max-width: 900px) {
  .login-box {
    width: 90%;
    height: auto;
    flex-direction: column;
  }

  .login-left {
    display: none;
    /* 移动端隐藏左侧装饰 */
  }

  .login-right {
    padding: 40px 20px;
  }
}
</style>