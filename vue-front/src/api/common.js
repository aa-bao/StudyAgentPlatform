import request from '../utils/request'

// 获取题目类型列表
export function getQuestionTypes() {
    return request({
        url: '/common/question-types',
        method: 'get'
    })
}
