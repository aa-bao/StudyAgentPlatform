import {
    defineStore
} from 'pinia'

export const useUserStore = defineStore('user', {
    // 定义状态：存储用户信息
    state: () => ({
        userInfo: JSON.parse(localStorage.getItem('user') || '{}')
    }),

    actions: {
        // 设置用户信息并持久化到本地
        setUserInfo(userInfo) {
            this.userInfo = userInfo
            localStorage.setItem('user', JSON.stringify(userInfo))
        },

        // 退出登录时清除信息
        clearUserInfo() {
            this.userInfo = {}
            localStorage.removeItem('user')
            localStorage.removeItem('role')
        }
    },

    getters: {
        // 快速获取用户角色
        role: (state) => state.userInfo.role || localStorage.getItem('role') || ''
    }
})
