<template>
  <div class="mock-exam-container">
    <el-empty description="真题模考功能开发中..." image-size="200">
      <template #extra>
        <p>即将上线：全真模拟考场环境，历年真题成套演练，智能评分报告。</p>
        <el-button type="primary" @click="$router.push('/user/dashboard')">返回首页</el-button>
      </template>
    </el-empty>
  </div>
</template>

<script setup>
// Placeholder for Mock Exam Logic
</script>

<style scoped>
.mock-exam-container {
  padding: 40px;
  background-color: #f5f7fa;
  min-height: calc(100vh - 80px);
  display: flex;
  justify-content: center;
  align-items: center;
}
</style>
