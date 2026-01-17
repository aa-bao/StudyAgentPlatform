import request from '@/utils/request'

/**
 * 压缩包导入题目
 * @param {FormData} formData - 包含 zipFile, bookId, subjectIds, source, checkDuplicate
 * @returns {Promise}
 */
export function importQuestionsFromZip(formData) {
    return request({
        url: '/question/import/zip',
        method: 'post',
        data: formData,
        headers: {
            'Content-Type': 'multipart/form-data'
        }
    })
}

/**
 * 预览压缩包内容（可选功能，用于导入前预览）
 * @param {FormData} formData - 包含 zipFile
 * @returns {Promise}
 */
export function previewZipFile(formData) {
    return request({
        url: '/question/import/zip/preview',
        method: 'post',
        data: formData,
        headers: {
            'Content-Type': 'multipart/form-data'
        }
    })
}
