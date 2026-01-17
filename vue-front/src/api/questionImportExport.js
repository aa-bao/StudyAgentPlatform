import request from '../utils/request'

// JSON批量导入题目
export function importQuestions(data) {
    return request({
        url: '/question/import',
        method: 'post',
        data
    })
}

// 导出题目为PDF
export function exportToPDF(data) {
    return request({
        url: '/question/export/pdf',
        method: 'post',
        data
    })
}

// 获取所有科目（树形结构）
export function getSubjectTree() {
    return request({
        url: '/subject/manage-tree',
        method: 'get'
    })
}

// 获取所有习题册
export function getAllBooks() {
    return request({
        url: '/book/all',
        method: 'get'
    })
}

// 根据科目获取题目
export function getQuestionsBySubject(subjectId, bookId) {
    return request({
        url: '/question/list-by-subject',
        method: 'get',
        params: { subjectId, bookId }
    })
}
