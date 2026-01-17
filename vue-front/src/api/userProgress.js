import request from '../utils/request'

// 获取用户学习进度
export function getUserProgress(userId) {
    return request({
        url: `/user-progress/user/${userId}`,
        method: 'get'
    })
}

// 获取用户指定科目进度
export function getUserSubjectProgress(userId, subjectId) {
    return request({
        url: `/user-progress/user/${userId}/subject/${subjectId}`,
        method: 'get'
    })
}

// ==================== 管理员接口 ====================

// 获取所有用户学习进度（管理员）
export function getAllUserProgress(params) {
    return request({
        url: '/user-progress/admin/all',
        method: 'get',
        params
    })
}

// 获取学习进度统计（管理员）
export function getProgressStats() {
    return request({
        url: '/user-progress/admin/stats',
        method: 'get'
    })
}

// 获取学习排行榜（管理员）
export function getLearningRanking(limit = 10) {
    return request({
        url: '/user-progress/admin/ranking',
        method: 'get',
        params: { limit }
    })
}
