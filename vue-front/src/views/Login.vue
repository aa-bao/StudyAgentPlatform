<template>
    <div class="app-root">
        <div class="visual-section">
            <div class="bg-decoration">
                <div class="gradient-overlay"></div>
                <div class="blob blob-1"></div>
                <div class="blob blob-2"></div>
            </div>

            <div class="logo-container">
                <div class="logo-wrapper">
                    <div class="logo-icon-box">
                        <span class="logo-letter">Y</span>
                    </div>
                    <span class="logo-text">YanGUO</span>
                </div>
            </div>

            <div class="center-content">
                <div class="daily-goal-card">
                    <div class="goal-icon-wrapper">
                        <svg width="20" height="20" viewBox="0 0 20 20" fill="none" xmlns="http://www.w3.org/2000/svg">
                            <path d="M16.6666 5.5L7.49992 14.6667L3.33325 10.5" stroke="currentColor" stroke-width="2.5"
                                stroke-linecap="round" stroke-linejoin="round" />
                        </svg>
                    </div>
                    <div class="goal-content">
                        <p class="goal-label">今日目标</p>
                        <p class="goal-status">全部完成</p>
                    </div>
                </div>

                <div class="cards-container">
                    <div class="data-card">
                        <div class="card-header">
                            <div>
                                <p class="label">今日学习时长</p>
                                <p class="time-value">8 小时 45 分</p>
                            </div>
                            <div class="graph-icon">
                                <img :src="lineChartIcon" alt="trophy">
                            </div>
                        </div>
                        <div class="progress-section">
                            <div class="progress-item">
                                <div class="progress-info">
                                    <span>英语单词</span>
                                    <span>85%</span>
                                </div>
                                <div class="progress-track">
                                    <div class="progress-bar" style="width: 85%; background: white;"></div>
                                </div>
                            </div>
                            <div class="progress-item">
                                <div class="progress-info">
                                    <span>政治考点</span>
                                    <span>42%</span>
                                </div>
                                <div class="progress-track">
                                    <div class="progress-bar" style="width: 42%; background: #bfdbfe;"></div>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
            </div>

            <div class="footer-quote">
                <blockquote class="quote-text">
                    “看似不起眼的日复一日，<br>会在将来的某一天，让你看到坚持的意义。”
                </blockquote>
            </div>
        </div>

        <div class="form-section">
            <div class="blur-background-blob"></div>

            <div class="auth-container">
                <div class="mobile-logo">
                    <div class="logo-icon-box-dark">
                        <img :src="scienceIcon" alt="logo">
                    </div>
                    <span class="logo-text-dark">YANTU</span>
                </div>

                <div class="header">
                    <h1 class="title">{{ activeTab === 'login' ? '欢迎回来' : '创建账号' }}</h1>
                    <p class="subtitle">{{ activeTab === 'login' ? '继续你的备考进度，保持专注。' : '加入研途，开启你的上岸之旅。' }}</p>
                </div>

                <div class="tab-switcher">
                    <div class="tab-indicator" :class="{ 'translate-right': activeTab === 'register' }"></div>
                    <button @click="activeTab = 'login'" :class="{ 'active': activeTab === 'login' }">登录</button>
                    <button @click="activeTab = 'register'" :class="{ 'active': activeTab === 'register' }">注册</button>
                </div>

                <form v-if="activeTab === 'login'" class="fade-in" @submit.prevent="handleLogin">
                    <div class="input-group">
                        <label>邮箱 / 账号</label>
                        <div class="input-wrapper">
                            <img :src="emailIcon" class="field-icon" alt="email">
                            <input type="text" v-model="loginForm.username" placeholder="请输入邮箱或账号">
                        </div>
                    </div>
                    <div class="input-group">
                        <div class="flex-between">
                            <label>密码</label>
                            <a href="#" class="link" @click.prevent="handleForgetPassword">忘记密码?</a>
                        </div>
                        <div class="input-wrapper">
                            <img :src="lockIcon" class="field-icon" alt="lock">
                            <input type="password" v-model="loginForm.password" placeholder="请输入密码">
                        </div>
                    </div>
                    <div class="checkbox-group">
                        <label class="custom-checkbox">
                            <input type="checkbox" v-model="loginForm.remember">
                            <span class="checkmark">
                                <svg v-if="loginForm.remember" width="12" height="12" viewBox="0 0 12 12" fill="none" xmlns="http://www.w3.org/2000/svg">
                                    <path d="M2 6L5 9L10 3" stroke="white" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"/>
                                </svg>
                            </span>
                            <span class="label-text">30天内自动登录</span>
                        </label>
                    </div>
                    <button type="submit" class="btn-submit" :disabled="loading">
                        {{ loading ? '登录中...' : '立即登录' }}
                    </button>
                </form>

                <form v-else class="fade-in" @submit.prevent="handleRegister">
                    <div class="input-group">
                        <label>邮箱</label>
                        <div class="input-wrapper">
                            <img :src="emailIcon" class="field-icon" alt="email">
                            <input type="email" v-model="registerForm.username" placeholder="请输入邮箱">
                        </div>
                    </div>
                    <div class="input-group verify-group">
                        <label>验证码</label>
                        <div class="flex-row gap-12">
                            <div class="input-wrapper flex-1">
                                <img :src="securityIcon" class="field-icon" alt="security">
                                <input type="text" v-model="registerForm.code" placeholder="6位数字">
                            </div>
                            <button type="button" class="btn-verify" @click="handleGetCode" :disabled="isCodeDisabled">
                                {{ isCodeDisabled ? `${codeCountdown}秒后重新获取` : '获取验证码' }}
                            </button>
                        </div>
                    </div>
                    <div class="input-group">
                        <label>设置密码</label>
                        <div class="input-wrapper">
                            <img :src="lockIcon" class="field-icon" alt="lock">
                            <input :type="showPassword ? 'text' : 'password'" v-model="registerForm.password" placeholder="8-20位字符">
                            <img :src="eyesIcon" class="field-icon password-toggle-icon" alt="toggle password" @click="showPassword = !showPassword" style="cursor: pointer;">
                        </div>
                    </div>
                    <div class="checkbox-group">
                        <label class="custom-checkbox">
                            <input type="checkbox" v-model="registerForm.agree">
                            <span class="checkmark">
                                <svg v-if="registerForm.agree" width="12" height="12" viewBox="0 0 12 12" fill="none" xmlns="http://www.w3.org/2000/svg">
                                    <path d="M2 6L5 9L10 3" stroke="white" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"/>
                                </svg>
                            </span>
                            <span class="label-text">
                                我已阅读并同意 <a href="#" class="link">《用户服务协议》</a> 和 <a href="#" class="link">《隐私政策》</a>
                            </span>
                        </label>
                    </div>
                    <button type="submit" class="btn-submit" :disabled="loading">
                        {{ loading ? '注册中...' : '注册账号' }}
                    </button>
                </form>

                <div class="divider">
                    <span>其他方式登录</span>
                </div>

                <div class="social-login">
                    <button class="social-icon"><img :src="weixinIcon" alt="wechat" style="width: 20px; height: 20px;"></button>
                    <button class="social-icon"><img :src="qqIcon" alt="wechat"
                    style="width: 20px; height: 20px;"></button>
                </div>
            </div>

            <div class="footer-info">
                <div class="created-by">Created by Tian</div>
            </div>
        </div>
    </div>
</template>

<script setup>
import { ref, reactive, computed } from 'vue';
import { useRouter } from 'vue-router';
import { ElMessage } from 'element-plus';
import scienceIcon from '@/assets/icons/science.svg?url'
import lineChartIcon from '@/assets/icons/line-chart.svg?url'
import emailIcon from '@/assets/icons/email.svg?url'
import lockIcon from '@/assets/icons/lock.svg?url'
import securityIcon from '@/assets/icons/security.svg?url'
import weixinIcon from '@/assets/icons/weixin.svg?url'
import qqIcon from '@/assets/icons/qq.svg?url'
import eyesIcon from '@/assets/icons/eyes.svg?url'
import { loginApi, registerApi } from '@/api/user';
import { sendMailCode } from '@/api/mail';
import { useUserStore } from '@/stores/user';

const router = useRouter();
const userStore = useUserStore();
const activeTab = ref('login');
const loading = ref(false);

// 登录表单
const loginForm = reactive({
    username: '',
    password: '',
    remember: false
});

// 注册表单
const registerForm = reactive({
    username: '',
    code: '',
    password: '',
    agree: false
});

// 密码可见性控制
const showPassword = ref(false)

// 登录处理
const handleLogin = async () => {
    if (!loginForm.username) {
        ElMessage.warning('请输入用户名');
        return;
    }
    if (!loginForm.password) {
        ElMessage.warning('请输入密码');
        return;
    }

    try {
        loading.value = true;
        const res = await loginApi({
            username: loginForm.username,
            password: loginForm.password,
            remember: loginForm.remember
        });

        // request.js拦截器已经处理了code === 200的判断
        // 如果走到这里说明登录成功
        userStore.setUserInfo(res.data.userInfo);
        userStore.setToken(res.data.token);

        // 保存角色信息到localStorage
        if (res.data.userInfo.role) {
            localStorage.setItem('role', res.data.userInfo.role);
        }

        if (loginForm.remember) {
            localStorage.setItem('rememberLogin', 'true');
        }

        ElMessage.success('登录成功！');

        // 立即跳转到home页面
        await router.push('/user/home');
    } catch (error) {
        // 显示错误信息
        ElMessage.error(error.message || '登录失败，请检查用户名和密码');
    } finally {
        loading.value = false;
    }
};

// 注册处理
const handleRegister = async () => {
    if (!registerForm.username) {
        ElMessage.warning('请输入邮箱');
        return;
    }
    const emailReg = /^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$/;
    if (!emailReg.test(registerForm.username)) {
        ElMessage.warning('请输入正确的邮箱格式');
        return;
    }
    if (!registerForm.code) {
        ElMessage.warning('请输入验证码');
        return;
    }
    if (!registerForm.password) {
        ElMessage.warning('请设置密码');
        return;
    }
    if (registerForm.password.length < 6 || registerForm.password.length > 20) {
        ElMessage.warning('密码长度应为6-20位');
        return;
    }
    if (!registerForm.agree) {
        ElMessage.warning('请阅读并同意用户协议和隐私政策');
        return;
    }

    try {
        loading.value = true;
        const res = await registerApi({
            username: registerForm.username,
            password: registerForm.password,
            code: registerForm.code,
            nickname: '考研人',
            examYear: '27考研'
        });

        // 注册成功
        ElMessage.success('注册成功！请登录');
        activeTab.value = 'login';
        // 将注册时输入的账号和密码设置到登录表单中
        loginForm.username = registerForm.username.split('@')[0]; // 使用邮箱前缀作为用户名
        loginForm.password = registerForm.password;
        registerForm.username = '';
        registerForm.code = '';
        registerForm.password = '';
        registerForm.agree = false;
    } catch (error) {
        // 显示错误信息
        ElMessage.error(error.message || '注册失败，请稍后重试');
    } finally {
        loading.value = false;
    }
};

// 验证码倒计时
const codeCountdown = ref(0)
const isCodeDisabled = computed(() => codeCountdown.value > 0)

// 启动验证码倒计时
const startCodeCountdown = () => {
    codeCountdown.value = 30
    const timer = setInterval(() => {
        codeCountdown.value--
        if (codeCountdown.value <= 0) {
            clearInterval(timer)
        }
    }, 1000)
}

// 获取验证码
const handleGetCode = async () => {
    if (!registerForm.username) {
        ElMessage.warning('请先输入邮箱');
        return;
    }
    const emailReg = /^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$/;
    if (!emailReg.test(registerForm.username)) {
        ElMessage.warning('请输入正确的邮箱格式');
        return;
    }

    try {
        console.log('发送验证码请求参数:', {
            email: registerForm.username,
            bizType: 'register'
        });

        const res = await sendMailCode({
            email: registerForm.username,
            bizType: 'register'
        });

        console.log('发送验证码响应:', res);
        ElMessage.success(res.data || '验证码已发送到您的邮箱');
        startCodeCountdown()
    } catch (error) {
        console.error('发送验证码失败:', error);
        ElMessage.error(error.response?.data?.message || '验证码发送失败');
    }
};

// 忘记密码
const handleForgetPassword = () => {
    ElMessage.info('忘记密码功能开发中，请联系管理员重置密码');
};
</script>

<style scoped>
/* 1. 基础重置与字体加载 */
@import url('https://fonts.googleapis.com/css2?family=Inter:wght@300;400;500;600&family=Noto+Sans+SC:wght@300;400;500;600&display=swap');

.app-root {
    font-family: 'Inter', 'Noto Sans SC', sans-serif;
    height: 100vh;
    display: flex;
    overflow: hidden;
    background-color: #f8fafc;
    color: #475569;
    -webkit-font-smoothing: antialiased;
}

/* 2. 左侧视觉区域样式 */
.visual-section {
    display: none;
    width: 50%;
    position: relative;
    flex-direction: column;
    justify-content: space-between;
    padding: 3rem;
    background-color: #2563eb;
    color: white;
    overflow: hidden;
}

@media (min-width: 1024px) {
    .visual-section {
        display: flex;
    }
}

.bg-decoration {
    position: absolute;
    inset: 0;
    z-index: 0;
}

.gradient-overlay {
    position: absolute;
    inset: 0;
    background: radial-gradient(circle at top right, rgba(96, 165, 250, 0.4), #2563eb, #1d4ed8);
}

.blob {
    position: absolute;
    border-radius: 9999px;
    filter: blur(64px);
    background: rgba(255, 255, 255, 0.1);
}

.blob-1 {
    width: 24rem;
    height: 24rem;
    bottom: -8rem;
    left: -8rem;
}

.blob-2 {
    width: 16rem;
    height: 16rem;
    top: 25%;
    right: 0;
    background: rgba(147, 197, 253, 0.2);
}

.logo-container {
    position: relative;
    z-index: 10;
}

.logo-wrapper {
    display: flex;
    align-items: center;
    gap: 0.5rem;
}

.logo-icon-box {
    width: 2rem;
    height: 2rem;
    border-radius: 0.5rem;
    background: rgba(255, 255, 255, 0.2);
    backdrop-filter: blur(4px);
    border: 1px solid rgba(255, 255, 255, 0.3);
    display: flex;
    align-items: center;
    justify-content: center;
}

.logo-letter {
    font-size: 1.25rem;
    font-weight: 600;
    color: white;
    line-height: 1;
}

.logo-text {
    font-size: 1.25rem;
    font-weight: 500;
    letter-spacing: -0.05em;
}

.center-content {
    position: relative;
    z-index: 10;
    flex-grow: 1;
    display: flex;
    align-items: center;
    justify-content: center;
}

.cards-container {
    width: 100%;
    max-width: 24rem;
    display: flex;
    flex-direction: column;
    gap: 1rem;
}

.data-card {
    width: 100%;
    padding: 1.5rem;
    background: rgba(255, 255, 255, 0.1);
    backdrop-filter: blur(12px);
    border: 1px solid rgba(255, 255, 255, 0.2);
    border-radius: 1rem;
    box-shadow: 0 25px 50px -12px rgba(0, 0, 0, 0.25);
    animation: swing 4s ease-in-out infinite;
    position: relative;
}

.daily-goal-card {
    position: absolute;
    top: 2rem;
    right: 2rem;
    display: flex;
    align-items: center;
    gap: 12px;
    /* 缩小图标与文字之间的间距 */
    padding: 0.6rem 1rem;
    /* 稍微增加内边距让视觉更平衡 */
    background: rgba(255, 255, 255, 0.15);
    backdrop-filter: blur(16px);
    border: 1px solid rgba(255, 255, 255, 0.3);
    border-radius: 0.75rem;
    box-shadow: 0 20px 40px -12px rgba(0, 0, 0, 0.3);
    animation: float 3s ease-in-out infinite;
    z-index: 10;
}

/* 1. 放大图标容器 */
.goal-icon-wrapper {
    width: 2.5rem;
    /* 从 1.75rem 放大 */
    height: 2.5rem;
    border-radius: 50%;
    background: rgba(74, 222, 128, 0.25);
    border: 1px solid rgba(74, 222, 128, 0.4);
    display: flex;
    align-items: center;
    justify-content: center;
    color: #86efac;
    flex-shrink: 0;
}

.goal-content {
    display: flex;
    flex-direction: column;
    justify-content: center;
}

.goal-label {
    margin: 0;
    font-size: 0.7rem;
    color: #e0e7ff;
    opacity: 0.9;
}

.goal-status {
    margin: 0;
    font-size: 1rem;
    font-weight: 600;
    color: white;
}

.card-header {
    display: flex;
    justify-content: space-between;
    margin-bottom: 1.5rem;
}

.label {
    font-size: 0.75rem;
    color: #dbeafe;
    font-weight: 500;
    text-transform: uppercase;
    letter-spacing: 0.05em;
}

.time-value {
    font-size: 1.875rem;
    font-weight: 500;
    margin-top: 0.25rem;
}

.graph-icon {
    width: 2.5rem;
    height: 2.5rem;
    background: rgba(255, 255, 255, 0.2);
    border-radius: 9999px;
    display: flex;
    align-items: center;
    justify-content: center;
}

.graph-icon img {
    width: 20px;
    height: 20px;
    filter: brightness(0) invert(1);
}

.progress-item {
    margin-top: 0.75rem;
}

.progress-info {
    display: flex;
    justify-content: space-between;
    font-size: 0.75rem;
    color: #dbeafe;
    margin-bottom: 0.25rem;
}

.progress-track {
    width: 100%;
    height: 0.375rem;
    background: rgba(30, 58, 138, 0.3);
    border-radius: 9999px;
    overflow: hidden;
}

.progress-bar {
    height: 100%;
    border-radius: 9999px;
}

.footer-quote {
    position: relative;
    z-index: 10;
    display: flex;
    flex-direction: column;
    align-items: flex-start;
    text-align: left;
    width: 100%;
}

.quote-text {
    font-size: 1.125rem;
    color: #f8fafc;
    line-height: 1.6;
    text-align: left;
    margin: 0;
}

.quote-text {
    font-size: 1.125rem;
    color: #f8fafc;
    line-height: 1.6;
}

/* 3. 右侧表单区域样式 */
.form-section {
    width: 100%;
    position: relative;
    display: flex;
    align-items: center;
    justify-content: center;
    padding: 1.5rem;
    background-image: linear-gradient(to right, #eff6ff 1px, transparent 1px), linear-gradient(to bottom, #eff6ff 1px, transparent 1px);
    background-size: 24px 24px;
}

@media (min-width: 1024px) {
    .form-section {
        width: 50%;
    }
}

.blur-background-blob {
    position: absolute;
    top: 50%;
    left: 50%;
    transform: translate(-50%, -50%);
    width: 500px;
    height: 500px;
    background: rgba(219, 234, 254, 0.5);
    border-radius: 9999px;
    filter: blur(100px);
    z-index: -10;
}

.auth-container {
    width: 100%;
    max-width: 28rem;
    background: rgba(255, 255, 255, 0.8);
    backdrop-filter: blur(4px);
    padding: 2rem;
    border-radius: 1rem;
    border: 1px solid rgba(255, 255, 255, 0.5);
    box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.05);
}

.mobile-logo {
    display: flex;
    align-items: center;
    gap: 0.5rem;
    margin-bottom: 2rem;
    justify-content: center;
}

@media (min-width: 1024px) {
    .mobile-logo {
        display: none;
    }
}

.logo-icon-box-dark {
    width: 2rem;
    height: 2rem;
    border-radius: 0.5rem;
    background: #2563eb;
    color: white;
    display: flex;
    align-items: center;
    justify-content: center;
}

.logo-icon-box-dark img {
    width: 20px;
    height: 20px;
    filter: brightness(0) invert(1);
}

.logo-text-dark {
    font-size: 1.25rem;
    font-weight: 500;
    color: #0f172a;
}

.header {
    text-align: center;
    margin-bottom: 2rem;
}

@media (min-width: 1024px) {
    .header {
        text-align: left;
    }
}

.title {
    font-size: 1.5rem;
    font-weight: 500;
    color: #0f172a;
}

.subtitle {
    font-size: 0.875rem;
    color: #64748b;
    margin-top: 0.5rem;
}

.tab-switcher {
    display: flex;
    padding: 4px;
    /* 使用固定数值更易控制精确度 */
    background: #f1f5f9;
    border-radius: 12px;
    margin-bottom: 1.5rem;
    position: relative;
    height: 44px;
    /* 固定高度确保内部元素垂直居中 */
    box-sizing: border-box;
    /* 确保 padding 不撑开高度 */
}

.tab-indicator {
    position: absolute;
    top: 4px;
    bottom: 4px;
    left: 4px;
    width: calc(50% - 8px);
    background: white;
    border-radius: 8px;
    box-shadow: 0 2px 4px rgba(0, 0, 0, 0.05);
    transition: transform 0.3s cubic-bezier(0.4, 0, 0.2, 1);
    border: 1px solid rgba(226, 232, 240, 0.8);
    z-index: 1;
}

.translate-right {
    transform: translateX(calc(100% + 4px));
}

.tab-switcher button {
    flex: 1;
    position: relative;
    z-index: 10;
    padding: 0;
    font-size: 0.875rem;
    font-weight: 500;
    border: none;
    background: transparent;
    cursor: pointer;
    color: #64748b;
    transition: color 0.2s;
    height: 100%;
    display: flex;
    align-items: center;
    justify-content: center;
}

.tab-switcher button.active {
    color: #0f172a;
}

.input-group {
    margin-bottom: 1.25rem;
}

.verify-group {
    margin-bottom: 1.25rem;
}

.verify-group label {
    display: block;
    font-size: 0.75rem;
    font-weight: 500;
    color: #334155;
    margin-left: 0.25rem;
    margin-bottom: 0.375rem;
}

.input-group label {
    display: block;
    font-size: 0.75rem;
    font-weight: 500;
    color: #334155;
    margin-left: 0.25rem;
    margin-bottom: 0.375rem;
}

.input-wrapper {
    position: relative;
    width: 100%;
    box-sizing: border-box;
}

.field-icon {
    position: absolute;
    left: 0.875rem;
    top: 50%;
    transform: translateY(-50%);
    color: #000000;
    transition: all 0.2s;
    width: 20px;
    height: 20px;
    opacity: 0.8;
    filter: brightness(0);
}

.input-wrapper:focus-within .field-icon {
    opacity: 1;
}

.input-wrapper input {
    width: 100%;
    background: #f8fafc;
    border: 1px solid #e2e8f0;
    border-radius: 0.75rem;
    padding: 0.75rem 2.5rem 0.75rem 2.5rem;
    font-size: 0.875rem;
    outline: none;
    transition: all 0.2s;
    box-sizing: border-box;
}

.input-wrapper input:focus {
    border-color: #3b82f6;
    box-shadow: 0 0 0 4px rgba(59, 130, 246, 0.1);
}

.input-wrapper:focus-within .field-icon {
    color: #3b82f6;
}

.flex-between {
    display: flex;
    justify-content: space-between;
    align-items: center;
}

.flex-row {
    display: flex;
    align-items: center;
    gap: 0.75rem;
}

.flex-1 {
    flex: 1;
}

.gap-12 {
    gap: 0.75rem;
}

.link {
    color: #2563eb;
    text-decoration: none;
    font-size: 0.75rem;
}

.link:hover {
    text-decoration: underline;
}

.checkbox-group {
    margin-bottom: 1.25rem;
}

.custom-checkbox {
    display: flex;
    align-items: center;
    cursor: pointer;
    position: relative;
}

.custom-checkbox input {
    position: absolute;
    opacity: 0;
    cursor: pointer;
}

.checkmark {
    width: 1rem;
    height: 1rem;
    border: 1px solid #cbd5e1;
    border-radius: 0.25rem;
    display: flex;
    align-items: center;
    justify-content: center;
    background: white;
    flex-shrink: 0;
}

.custom-checkbox input:checked~.checkmark {
    background: #2563eb;
    border-color: #2563eb;
}

.checkmark svg {
    display: block;
}

.label-text {
    margin-left: 0.5rem;
    font-size: 0.75rem;
    color: #64748b;
    line-height: 1.2;
}

.btn-submit {
    width: 100%;
    background: #2563eb;
    color: white;
    border: none;
    padding: 0.75rem;
    border-radius: 0.75rem;
    font-weight: 500;
    font-size: 0.875rem;
    cursor: pointer;
    box-shadow: 0 4px 6px -1px rgba(37, 99, 235, 0.2);
    transition: all 0.2s;
}

.btn-submit:hover {
    background: #1d4ed8;
    box-shadow: 0 10px 15px -3px rgba(37, 99, 235, 0.3);
}

.btn-submit:active {
    transform: scale(0.98);
}

.btn-verify {
    height: 2.875rem;
    padding: 0 1rem;
    background: white;
    border: 1px solid #e2e8f0;
    border-radius: 0.75rem;
    font-size: 0.875rem;
    color: #475569;
    cursor: pointer;
    transition: all 0.2s;
    white-space: nowrap;
    flex-shrink: 0;
}

.btn-verify:hover {
    color: #2563eb;
    border-color: #bfdbfe;
    background: #f8fafc;
}

.divider {
    position: relative;
    margin: 2rem 0;
    text-align: center;
}

.divider::before {
    content: "";
    position: absolute;
    top: 50%;
    left: 0;
    width: 100%;
    height: 1px;
    background: #e2e8f0;
}

.divider span {
    position: relative;
    background: white;
    padding: 0 0.5rem;
    font-size: 0.75rem;
    color: #94a3b8;
}

.social-login {
    display: flex;
    justify-content: center;
    gap: 1rem;
}

.social-icon {
    width: 2.5rem;
    height: 2.5rem;
    border-radius: 9999px;
    border: 1px solid #e2e8f0;
    background: white;
    color: #475569;
    display: flex;
    align-items: center;
    justify-content: center;
    cursor: pointer;
    transition: all 0.2s;
    font-size: 1.25rem;
    padding: 0;
}

/* 密码切换按钮样式 */
.password-toggle-icon {
    width: 20px;
    height: 20px;
    filter: brightness(0) invert(0.5);
    position: absolute;
    right: 0.875rem;
    left: auto;
}

.social-icon img {
    width: 24px;
    height: 24px;
    object-fit: contain;
    filter: brightness(0);
}

.social-icon:hover {
    border-color: #bfdbfe;
    background: #eff6ff;
    color: #2563eb;
    transform: scale(1.1);
}

.social-icon:hover img {
    filter: brightness(0) invert(40%);
}

.footer-info {
    position: absolute;
    bottom: 1.5rem;
    width: 100%;
    text-align: center;
}

.created-by, .copyright {
    font-size: 0.75rem;
    color: #94a3b8;
    line-height: 1.4;
}

/* 4. 动画 */
.fade-in {
    animation: fadeIn 0.3s ease-out forwards;
}

@keyframes fadeIn {
    from {
        opacity: 0;
        transform: translateY(8px);
    }

    to {
        opacity: 1;
        transform: translateY(0);
    }
}

@keyframes swing {
    0%, 100% {
        transform: rotate(3deg);
    }
    50% {
        transform: rotate(-3deg);
    }
}

@keyframes float {
    0%, 100% {
        transform: translateY(0px);
    }
    50% {
        transform: translateY(-8px);
    }
}
</style>