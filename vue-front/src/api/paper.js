import request from '../utils/request'

// 获取试卷详情
export function getPaperDetail(paperId) {
    return request({
        url: `/paper/${paperId}`,
        method: 'get'
    })
}

// 获取试卷列表
export function getPaperList(params) {
    return request({
        url: '/paper/list',
        method: 'get',
        params
    })
}

// 分页查询试卷
export function getPaperPage(params) {
    return request({
        url: '/paper/page',
        method: 'get',
        params
    })
}

// 获取试卷题目列表
export function getPaperQuestions(paperId) {
    return request({
        url: `/paper/${paperId}/questions`,
        method: 'get'
    })
}

// 添加题目到试卷
export function addQuestionToPaper(paperId, data) {
    return request({
        url: `/paper/${paperId}/add-question`,
        method: 'post',
        params: {
            questionId: data.questionId,
            sortOrder: data.sortOrder,
            scoreValue: data.scoreValue,
            parentSectionName: data.parentSectionName
        }
    })
}

// 从试卷中移除题目
export function removeQuestionFromPaper(paperId, questionId) {
    return request({
        url: `/paper/${paperId}/remove-question/${questionId}`,
        method: 'delete'
    })
}

// 更新试卷中题目的顺序
export function updateQuestionOrder(paperId, data) {
    return request({
        url: `/paper/${paperId}/update-question-order`,
        method: 'post',
        data
    })
}

// 新增试卷
export function addPaper(data) {
    return request({
        url: '/paper/add',
        method: 'post',
        data
    })
}

// 更新试卷
export function updatePaper(data) {
    return request({
        url: '/paper/update',
        method: 'post',
        data
    })
}

// 删除试卷
export function deletePaper(id) {
    return request({
        url: `/paper/delete/${id}`,
        method: 'delete'
    })
}
