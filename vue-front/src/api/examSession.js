import request from '../utils/request'

// 开始考试
export function startExam(userId, paperId) {
    return request({
        url: '/exam-session/start',
        method: 'post',
        params: {
            userId,
            paperId
        }
    })
}

// 保存答题快照
export function saveSnapshot(sessionId, snapshotJson) {
    return request({
        url: '/exam-session/snapshot',
        method: 'post',
        params: {
            sessionId,
            snapshotJson
        }
    })
}

// 记录题目切换
export function recordSwitch(sessionId) {
    return request({
        url: '/exam-session/switch',
        method: 'post',
        params: {
            sessionId
        }
    })
}

// 提交考试
export function submitExam(sessionId) {
    return request({
        url: '/exam-session/submit',
        method: 'post',
        params: {
            sessionId
        }
    })
}

// 获取考试会话详情
export function getSessionDetail(sessionId) {
    return request({
        url: `/exam-session/${sessionId}`,
        method: 'get'
    })
}

// 获取答题明细
export function getSessionDetails(sessionId) {
    return request({
        url: `/exam-session/${sessionId}/details`,
        method: 'get'
    })
}

// 获取正确率统计
export function getCorrectRate(sessionId) {
    return request({
        url: `/exam-answer-detail/session/${sessionId}/correct-rate`,
        method: 'get'
    })
}

// 获取用户未完成考试列表
export function getIncompleteSessions(userId) {
    return request({
        url: `/exam-session/user/${userId}/incomplete`,
        method: 'get'
    })
}

// ==================== 管理员接口 ====================

// 获取所有考试记录（管理员）
export function getAllExamSessions(params) {
    return request({
        url: '/exam-session/admin/all',
        method: 'get',
        params
    })
}

// 获取考试统计数据（管理员）
export function getExamStats() {
    return request({
        url: '/exam-session/admin/stats',
        method: 'get'
    })
}
