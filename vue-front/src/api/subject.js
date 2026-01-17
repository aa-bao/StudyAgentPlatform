import request from '../utils/request'

// 获取考试规格列表（Level 2 - 政治、英语、数学、408）
export function getExamSpecListApi() {
    return request({
        url: '/subject/exam-specs',
        method: 'get'
    })
}

// 获取科目树
export function getSubjectTreeApi(params) {
    return request({
        url: '/subject/tree',
        method: 'get',
        params
    })
}

// 根据考试规格获取科目树
export function getTreeByExamSpecApi(examSpecId, userId) {
    return request({
        url: `/subject/by-exam-spec/${examSpecId}`,
        method: 'get',
        params: { userId }
    })
}

// 根据科目ID获取支持的题型列表
export function getQuestionTypesBySubject(subjectId) {
    return request({
        url: `/subject/${subjectId}/question-types`,
        method: 'get'
    })
}

// 按层级获取科目树
export function getTreeByLevelApi(level, userId) {
    return request({
        url: '/subject/by-level',
        method: 'get',
        params: { level, userId }
    })
}

// 获取管理用科目树（完整树结构）
export function getManageTreeApi() {
    return request({
        url: '/subject/manage-tree',
        method: 'get'
    })
}

// 获取所有科目列表（扁平化）
export function getSubjectListApi() {
    return request({
        url: '/subject/list',
        method: 'get'
    })
}
