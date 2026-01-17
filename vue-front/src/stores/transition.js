import { defineStore } from 'pinia'
import { ref } from 'vue'

export const useTransitionStore = defineStore('transition', () => {
  // 是否正在从 Home 离开
  const isLeavingHome = ref(false)

  // 是否正在进入需要布局的页面
  const isEnteringLayout = ref(false)

  // 开始从 Home 离开的动画
  const startLeavingHome = () => {
    isLeavingHome.value = true
  }

  // 重置 Home 离开状态
  const resetLeavingHome = () => {
    isLeavingHome.value = false
  }

  // 开始进入布局页面
  const startEnteringLayout = () => {
    isEnteringLayout.value = true
    // 动画完成后自动重置
    setTimeout(() => {
      isEnteringLayout.value = false
    }, 300)
  }

  // 重置所有状态
  const reset = () => {
    isLeavingHome.value = false
    isEnteringLayout.value = false
  }

  return {
    isLeavingHome,
    isEnteringLayout,
    startLeavingHome,
    resetLeavingHome,
    startEnteringLayout,
    reset
  }
})
