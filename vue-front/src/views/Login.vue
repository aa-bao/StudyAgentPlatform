<template>
  <div class="login-container">
    <!-- 动态背景 -->
    <div class="animated-bg">
      <div class="gradient-bg"></div>
      <div class="floating-shapes">
        <div class="shape shape-1"></div>
        <div class="shape shape-2"></div>
        <div class="shape shape-3"></div>
        <div class="shape shape-4"></div>
        <div class="shape shape-5"></div>
        <div class="shape shape-6"></div>
      </div>
    </div>

    <!-- 网格背景 -->
    <div class="grid-pattern"></div>

    <!-- 主卡片 -->
    <div class="login-card">
      <!-- 左侧装饰区 -->
      <div class="card-left">
        <div class="left-content">
          <!-- Logo区域 -->
          <div class="brand-logo">
            <div class="logo-icon">
              <svg viewBox="0 0 48 48" fill="none" xmlns="http://www.w3.org/2000/svg">
                <rect x="4" y="4" width="40" height="40" rx="8" fill="white" fill-opacity="0.2"/>
                <path d="M14 16C14 14.8954 14.8954 14 16 14H20C21.1046 14 22 14.8954 22 16V32C22 33.1046 21.1046 34 20 34H16C14.8954 34 14 33.1046 14 32V16Z" fill="white" fill-opacity="0.9"/>
                <path d="M26 20C26 18.8954 26.8954 18 28 18H32C33.1046 18 34 18.8954 34 20V32C34 33.1046 33.1046 34 32 34H28C26.8954 34 26 33.1046 26 32V20Z" fill="white" fill-opacity="0.9"/>
                <circle cx="24" cy="38" r="2" fill="white" fill-opacity="0.6"/>
              </svg>
            </div>
            <span class="brand-name">考研平台</span>
          </div>

          <!-- 欢迎文字 -->
          <div class="welcome-section">
            <h1 class="welcome-title">{{ isLogin ? '欢迎回来' : '加入我们' }}</h1>
            <p class="welcome-desc">{{ isLogin ? '继续您的考研复习之旅' : '开启您的研究生梦想之路' }}</p>
          </div>

          <!-- 数据展示卡片 -->
          <div class="stats-container">
            <div class="stat-card">
              <div class="stat-icon">
                <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                  <path d="M12 2L13.09 8.26L19 7L14.74 11.74L21 14L14.74 15.5L19 20L13.09 18.26L12 24L10.91 18.26L5 20L9.26 15.5L3 14L9.26 11.74L5 7L10.91 8.26L12 2Z" fill="currentColor"/>
                </svg>
              </div>
              <div class="stat-content">
                <div class="stat-number">50K+</div>
                <div class="stat-label">活跃用户</div>
              </div>
            </div>
            <div class="stat-card">
              <div class="stat-icon">
                <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                  <path d="M19 3H5C3.89543 3 3 3.89543 3 5V19C3 20.1046 3.89543 21 5 21H19C20.1046 21 21 20.1046 21 19V5C21 3.89543 20.1046 3 19 3Z" stroke="currentColor" stroke-width="2"/>
                  <path d="M9 12H15M12 9V15" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
                </svg>
              </div>
              <div class="stat-content">
                <div class="stat-number">100K+</div>
                <div class="stat-label">题目库</div>
              </div>
            </div>
            <div class="stat-card">
              <div class="stat-icon">
                <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                  <path d="M12 2L15.09 8.26L22 9.17L17 14.14L18.18 21.02L12 17.77L5.82 21.02L7 14.14L2 9.17L8.91 8.26L12 2Z" fill="currentColor"/>
                </svg>
              </div>
              <div class="stat-content">
                <div class="stat-number">4.9</div>
                <div class="stat-label">用户评分</div>
              </div>
            </div>
          </div>

          <!-- 底部装饰 -->
          <div class="bottom-decoration">
            <div class="decoration-line"></div>
            <p class="decoration-text">智能备考 · 高效学习</p>
          </div>
        </div>
      </div>

      <!-- 右侧表单区 -->
      <div class="card-right">
        <div class="form-container">
          <!-- 顶部标签切换 -->
          <div class="form-header">
            <div class="mode-tabs">
              <div
                class="tab-item"
                :class="{ active: isLogin }"
                @click="switchToLogin"
              >
                <span>登录</span>
              </div>
              <div
                class="tab-item"
                :class="{ active: !isLogin }"
                @click="switchToRegister"
              >
                <span>注册</span>
              </div>
              <div class="tab-indicator" :class="{ register: !isLogin }"></div>
            </div>
            <p class="form-subtitle">
              {{ isLogin ? '输入您的账号信息以继续' : '创建一个新账号开始使用' }}
            </p>
          </div>

          <!-- 表单 -->
          <el-form
            :model="form"
            :rules="rules"
            ref="formRef"
            class="login-form"
            size="large"
          >
            <transition name="form-slide" mode="out-in">
              <el-form-item v-if="!isLogin" key="nickname" prop="nickname">
                <el-input
                  v-model="form.nickname"
                  placeholder="昵称"
                  class="form-input"
                >
                  <template #prefix>
                    <el-icon class="input-icon">
                      <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                        <path d="M12 12C14.7614 12 17 9.76142 17 7C17 4.23858 14.7614 2 12 2C9.23858 2 7 4.23858 7 7C7 9.76142 9.23858 12 12 12Z" fill="currentColor"/>
                        <path d="M12 14C9.33 14 4 15.34 4 18V20H20V18C20 15.34 14.67 14 12 14Z" fill="currentColor"/>
                      </svg>
                    </el-icon>
                  </template>
                </el-input>
              </el-form-item>
            </transition>

            <el-form-item prop="username">
              <el-input
                v-model="form.username"
                placeholder="用户名 / 手机号"
                class="form-input"
              >
                <template #prefix>
                  <el-icon class="input-icon">
                    <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                      <path d="M12 2C13.1 2 14 2.9 14 4C14 5.1 13.1 6 12 6C10.9 6 10 5.1 10 4C10 2.9 10.9 2 12 2ZM21 9V7L15 1L9 7V9H3V11H5V19C5 20.1 5.9 21 7 21H17C18.1 21 19 20.1 19 19V11H21V9H21ZM11 19H8V17H11V19ZM11 15H8V13H11V15ZM11 11H8V9H11V11ZM16 19H13V17H16V19ZM16 15H13V13H16V15ZM16 11H13V9H16V11Z" fill="currentColor"/>
                    </svg>
                  </el-icon>
                </template>
              </el-input>
            </el-form-item>

            <el-form-item prop="password">
              <el-input
                v-model="form.password"
                type="password"
                placeholder="密码"
                show-password
                class="form-input"
              >
                <template #prefix>
                  <el-icon class="input-icon">
                    <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                      <path d="M18 8H17V6C17 3.24 14.76 1 12 1C9.24 1 7 3.24 7 6V8H6C4.9 8 4 8.9 4 10V20C4 21.1 4.9 22 6 22H18C19.1 22 20 21.1 20 20V10C20 8.9 19.1 8 18 8ZM9 6C9 4.34 10.34 3 12 3C13.66 3 15 4.34 15 6V8H9V6ZM18 20H6V10H18V20ZM12 17C13.1 17 14 16.1 14 15C14 13.9 13.1 13 12 13C10.9 13 10 13.9 10 15C10 16.1 10.9 17 12 17Z" fill="currentColor"/>
                    </svg>
                  </el-icon>
                </template>
              </el-input>
            </el-form-item>

            <el-form-item v-if="isLogin" class="remember-forgot">
              <el-checkbox v-model="rememberMe" class="custom-checkbox">记住我</el-checkbox>
              <a class="forgot-link">忘记密码?</a>
            </el-form-item>

            <el-button
              type="primary"
              @click="handleSubmit"
              class="submit-button"
              :loading="loading"
            >
              <span>{{ isLogin ? '登 录' : '注 册' }}</span>
              <el-icon class="btn-icon">
                <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                  <path d="M12 4L10.59 5.41L16.17 11H4V13H16.17L10.59 18.59L12 20L20 12L12 4Z" fill="currentColor"/>
                </svg>
              </el-icon>
            </el-button>
          </el-form>

          <!-- 社交登录 -->
          <div v-if="isLogin" class="social-login">
            <div class="divider">
              <span>或使用以下方式登录</span>
            </div>
            <div class="social-buttons">
              <div class="social-btn">
                <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                  <path d="M12 2C6.48 2 2 6.48 2 12C2 17.52 6.48 22 12 22C17.52 22 22 17.52 22 12C22 6.48 17.52 2 12 2ZM12 5C13.66 5 15 6.34 15 8C15 9.66 13.66 11 12 11C10.34 11 9 9.66 9 8C9 6.34 10.34 5 12 5ZM12 19.2C9.5 19.2 7.29 17.92 6 15.98C6.03 13.99 10 12.9 12 12.9C13.99 12.9 17.97 13.99 18 15.98C16.71 17.92 14.5 19.2 12 19.2Z" fill="currentColor"/>
                </svg>
              </div>
              <div class="social-btn">
                <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                  <path d="M22 12C22 6.477 17.523 2 12 2S2 6.477 2 12C2 16.991 5.657 21.128 10.438 21.878V14.89H7.898V12H10.44V9.796C10.44 7.292 11.931 5.906 14.148 5.906C15.194 5.906 16.287 6.079 16.287 6.079V8.56H15.032C13.789 8.56 13.44 9.284 13.44 10.028V12H16.268L15.772 14.89H13.44V21.878C18.221 21.128 22 16.991 22 12Z" fill="currentColor"/>
                </svg>
              </div>
              <div class="social-btn">
                <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                  <path d="M12 2C6.477 2 2 6.477 2 12C2 17.523 6.477 22 12 22C17.523 22 22 17.523 22 12C22 6.477 17.523 2 12 2ZM12 18C9.243 18 7 15.757 7 13C7 10.243 9.243 8 12 8C14.757 8 17 10.243 17 13C17 15.757 14.757 18 12 18ZM12 10C10.343 10 9 11.343 9 13C9 14.657 10.343 16 12 16C13.657 16 15 14.657 15 13C15 11.343 13.657 10 12 10Z" fill="currentColor"/>
                </svg>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 底部版权 -->
    <div class="footer-copyright">
      <p>© 2024 考研平台. All rights reserved.</p>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { loginApi, registerApi } from '../api/user'
import { useUserStore } from '../stores/user'

const router = useRouter()
const userStore = useUserStore()
const formRef = ref(null)
const loading = ref(false)
const isLogin = ref(true)
const rememberMe = ref(false)

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

const switchToLogin = () => {
  if (!isLogin.value) {
    isLogin.value = true
    formRef.value?.resetFields()
  }
}

const switchToRegister = () => {
  if (isLogin.value) {
    isLogin.value = false
    formRef.value?.resetFields()
  }
}

const handleSubmit = () => {
  formRef.value.validate(async (valid) => {
    if (!valid) return

    loading.value = true
    try {
      if (isLogin.value) {
        const res = await loginApi({
          username: form.username,
          password: form.password
        })
        ElMessage.success('登录成功')
        userStore.setUserInfo(res.data)
        localStorage.setItem('role', res.data.role)

        setTimeout(() => {
          if (res.data.role === 'admin') {
            router.push('/admin/home')
          } else {
            router.push('/user/dashboard')
          }
        }, 100)
      } else {
        const res = await registerApi(form)
        ElMessage.success('注册成功,请登录')
        isLogin.value = true
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
/* ========== 容器 ========== */
.login-container {
  min-height: 100vh;
  width: 100vw;
  display: flex;
  justify-content: center;
  align-items: center;
  position: relative;
  overflow: hidden;
  background: linear-gradient(135deg, #f5f7fa 0%, #e8ecf1 100%);
}

/* ========== 动态背景 ========== */
.animated-bg {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  z-index: 0;
}

.gradient-bg {
  position: absolute;
  width: 100%;
  height: 100%;
  background: linear-gradient(135deg, #409EFF 0%, #0066CC 50%, #409EFF 100%);
  background-size: 200% 200%;
  animation: gradientShift 15s ease infinite;
}

@keyframes gradientShift {
  0% { background-position: 0% 50%; }
  50% { background-position: 100% 50%; }
  100% { background-position: 0% 50%; }
}

/* ========== 浮动形状 ========== */
.floating-shapes {
  position: absolute;
  width: 100%;
  height: 100%;
  overflow: hidden;
}

.shape {
  position: absolute;
  background: rgba(255, 255, 255, 0.05);
  border-radius: 50%;
  backdrop-filter: blur(10px);
  animation: float 20s infinite;
}

.shape-1 {
  width: 80px;
  height: 80px;
  top: 10%;
  left: 10%;
  animation-delay: 0s;
}

.shape-2 {
  width: 120px;
  height: 120px;
  top: 70%;
  left: 5%;
  animation-delay: 2s;
}

.shape-3 {
  width: 100px;
  height: 100px;
  top: 20%;
  right: 15%;
  animation-delay: 4s;
}

.shape-4 {
  width: 60px;
  height: 60px;
  bottom: 20%;
  right: 20%;
  animation-delay: 6s;
}

.shape-5 {
  width: 90px;
  height: 90px;
  top: 50%;
  left: 50%;
  animation-delay: 8s;
}

.shape-6 {
  width: 70px;
  height: 70px;
  bottom: 10%;
  left: 30%;
  animation-delay: 10s;
}

@keyframes float {
  0%, 100% {
    transform: translateY(0) rotate(0deg);
    opacity: 0.7;
  }
  50% {
    transform: translateY(-30px) rotate(180deg);
    opacity: 0.3;
  }
}

/* ========== 网格背景 ========== */
.grid-pattern {
  position: absolute;
  width: 100%;
  height: 100%;
  background-image:
    linear-gradient(rgba(255, 255, 255, 0.03) 1px, transparent 1px),
    linear-gradient(90deg, rgba(255, 255, 255, 0.03) 1px, transparent 1px);
  background-size: 50px 50px;
  z-index: 1;
}

/* ========== 主卡片 ========== */
.login-card {
  position: relative;
  z-index: 10;
  display: flex;
  width: 1100px;
  height: 650px;
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(20px);
  border-radius: 24px;
  box-shadow:
    0 20px 60px rgba(64, 158, 255, 0.3),
    0 0 1px rgba(0, 0, 0, 0.1);
  overflow: hidden;
  animation: cardEntrance 0.6s ease-out;
}

@keyframes cardEntrance {
  from {
    opacity: 0;
    transform: translateY(30px) scale(0.95);
  }
  to {
    opacity: 1;
    transform: translateY(0) scale(1);
  }
}

/* ========== 左侧装饰区 ========== */
.card-left {
  flex: 1;
  background: linear-gradient(135deg, #409EFF 0%, #0066CC 100%);
  padding: 60px 50px;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  position: relative;
  overflow: hidden;
}

.card-left::before {
  content: '';
  position: absolute;
  top: -50%;
  right: -50%;
  width: 200%;
  height: 200%;
  background: radial-gradient(circle, rgba(255, 255, 255, 0.1) 0%, transparent 70%);
  animation: rotate 30s linear infinite;
}

@keyframes rotate {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
}

.left-content {
  position: relative;
  z-index: 1;
  height: 100%;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
}

/* Logo */
.brand-logo {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 40px;
}

.logo-icon {
  width: 48px;
  height: 48px;
  background: rgba(255, 255, 255, 0.2);
  backdrop-filter: blur(10px);
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 8px 16px rgba(0, 0, 0, 0.1);
  animation: logoFloat 3s ease-in-out infinite;
}

@keyframes logoFloat {
  0%, 100% { transform: translateY(0); }
  50% { transform: translateY(-5px); }
}

.logo-icon svg {
  width: 28px;
  height: 28px;
}

.brand-name {
  font-size: 24px;
  font-weight: 700;
  color: #ffffff;
  letter-spacing: 1px;
}

/* 欢迎区域 */
.welcome-section {
  margin-bottom: 40px;
}

.welcome-title {
  font-size: 42px;
  font-weight: 700;
  color: #ffffff;
  margin-bottom: 16px;
  line-height: 1.2;
  text-shadow: 0 4px 8px rgba(0, 0, 0, 0.1);
}

.welcome-desc {
  font-size: 16px;
  color: rgba(255, 255, 255, 0.9);
  line-height: 1.6;
}

/* 数据卡片 */
.stats-container {
  display: flex;
  flex-direction: column;
  gap: 16px;
  margin-bottom: 40px;
}

.stat-card {
  display: flex;
  align-items: center;
  gap: 16px;
  background: rgba(255, 255, 255, 0.15);
  backdrop-filter: blur(10px);
  padding: 16px 20px;
  border-radius: 16px;
  border: 1px solid rgba(255, 255, 255, 0.2);
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

.stat-card:hover {
  background: rgba(255, 255, 255, 0.25);
  transform: translateX(10px);
  box-shadow: 0 8px 16px rgba(0, 0, 0, 0.1);
}

.stat-icon {
  width: 44px;
  height: 44px;
  background: rgba(255, 255, 255, 0.2);
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #ffffff;
  flex-shrink: 0;
}

.stat-icon svg {
  width: 24px;
  height: 24px;
}

.stat-content {
  flex: 1;
}

.stat-number {
  font-size: 24px;
  font-weight: 700;
  color: #ffffff;
  margin-bottom: 4px;
}

.stat-label {
  font-size: 13px;
  color: rgba(255, 255, 255, 0.8);
  font-weight: 500;
}

/* 底部装饰 */
.bottom-decoration {
  text-align: center;
}

.decoration-line {
  width: 60px;
  height: 3px;
  background: rgba(255, 255, 255, 0.3);
  border-radius: 2px;
  margin: 0 auto 16px;
}

.decoration-text {
  font-size: 14px;
  color: rgba(255, 255, 255, 0.8);
  font-weight: 500;
  letter-spacing: 2px;
}

/* ========== 右侧表单区 ========== */
.card-right {
  flex: 1.2;
  background: #ffffff;
  padding: 60px 50px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.form-container {
  width: 100%;
  max-width: 400px;
}

/* 表单头部 */
.form-header {
  margin-bottom: 40px;
}

.mode-tabs {
  display: flex;
  position: relative;
  background: #f5f7fa;
  border-radius: 12px;
  padding: 4px;
  margin-bottom: 16px;
}

.tab-item {
  flex: 1;
  text-align: center;
  padding: 12px 0;
  cursor: pointer;
  position: relative;
  z-index: 1;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

.tab-item span {
  font-size: 16px;
  font-weight: 600;
  color: #606266;
  transition: all 0.3s;
  position: relative;
  z-index: 1;
}

.tab-item.active span {
  color: #409EFF;
}

.tab-indicator {
  position: absolute;
  top: 4px;
  left: 4px;
  width: calc(50% - 4px);
  height: calc(100% - 8px);
  background: #ffffff;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(64, 158, 255, 0.15);
  transition: transform 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

.tab-indicator.register {
  transform: translateX(100%);
}

.form-subtitle {
  font-size: 14px;
  color: #909399;
  text-align: center;
  margin-top: 16px;
}

/* 表单样式 */
.login-form {
  margin-top: 20px;
}

.form-input :deep(.el-input__wrapper) {
  border-radius: 12px;
  background: #f5f7fa;
  box-shadow: none;
  padding: 12px 16px;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  border: 2px solid transparent;
}

.form-input :deep(.el-input__wrapper:hover) {
  background: #ffffff;
  border-color: rgba(64, 158, 255, 0.2);
}

.form-input :deep(.el-input__wrapper.is-focus) {
  background: #ffffff;
  border-color: #409EFF;
  box-shadow: 0 0 0 3px rgba(64, 158, 255, 0.1);
}

.form-input :deep(.el-input__inner) {
  font-size: 15px;
  color: #303133;
}

.form-input :deep(.el-input__inner::placeholder) {
  color: #909399;
}

.input-icon {
  font-size: 18px;
  color: #909399;
  transition: color 0.3s;
}

.form-input :deep(.el-input__wrapper.is-focus) .input-icon {
  color: #409EFF;
}

/* 记住我 & 忘记密码 */
.remember-forgot {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.custom-checkbox :deep(.el-checkbox__label) {
  font-size: 14px;
  color: #606266;
}

.forgot-link {
  font-size: 14px;
  color: #409EFF;
  text-decoration: none;
  cursor: pointer;
  transition: all 0.3s;
}

.forgot-link:hover {
  color: #0066CC;
  text-decoration: underline;
}

/* 提交按钮 */
.submit-button {
  width: 100%;
  height: 50px;
  border-radius: 12px;
  background: linear-gradient(135deg, #409EFF 0%, #0066CC 100%);
  border: none;
  font-size: 16px;
  font-weight: 600;
  color: #ffffff;
  box-shadow: 0 4px 12px rgba(64, 158, 255, 0.3);
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
}

.submit-button:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 20px rgba(64, 158, 255, 0.4);
}

.submit-button:active {
  transform: translateY(0);
}

.submit-button .btn-icon {
  font-size: 18px;
  transition: transform 0.3s;
}

.submit-button:hover .btn-icon {
  transform: translateX(4px);
}

/* 社交登录 */
.social-login {
  margin-top: 32px;
}

.divider {
  display: flex;
  align-items: center;
  text-align: center;
  margin-bottom: 20px;
}

.divider::before,
.divider::after {
  content: '';
  flex: 1;
  border-bottom: 1px solid #e4e7ed;
}

.divider span {
  padding: 0 12px;
  font-size: 13px;
  color: #909399;
  font-weight: 500;
}

.social-buttons {
  display: flex;
  justify-content: center;
  gap: 16px;
}

.social-btn {
  width: 48px;
  height: 48px;
  border-radius: 12px;
  background: #f5f7fa;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  border: 2px solid transparent;
}

.social-btn svg {
  width: 20px;
  height: 20px;
  color: #606266;
}

.social-btn:hover {
  background: #ffffff;
  border-color: #409EFF;
  transform: translateY(-4px);
  box-shadow: 0 4px 12px rgba(64, 158, 255, 0.2);
}

.social-btn:hover svg {
  color: #409EFF;
}

/* 底部版权 */
.footer-copyright {
  position: absolute;
  bottom: 20px;
  left: 0;
  right: 0;
  text-align: center;
  z-index: 10;
}

.footer-copyright p {
  font-size: 13px;
  color: rgba(255, 255, 255, 0.7);
  font-weight: 500;
}

/* ========== 动画 ========== */
.form-slide-enter-active,
.form-slide-leave-active {
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

.form-slide-enter-from {
  opacity: 0;
  transform: translateY(-10px);
}

.form-slide-leave-to {
  opacity: 0;
  transform: translateY(10px);
}

/* ========== 响应式 ========== */
@media (max-width: 1200px) {
  .login-card {
    width: 90%;
    height: auto;
    flex-direction: column;
  }

  .card-left {
    padding: 40px;
  }

  .card-right {
    padding: 40px;
  }

  .stats-container {
    flex-direction: row;
    flex-wrap: wrap;
  }

  .stat-card {
    flex: 1;
    min-width: 120px;
  }
}

@media (max-width: 768px) {
  .card-left {
    display: none;
  }

  .card-right {
    padding: 30px 20px;
  }

  .welcome-title {
    font-size: 32px;
  }
}
</style>
