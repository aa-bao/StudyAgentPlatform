import { ref } from 'vue'
import { getQuestionTypes } from '@/api/common'

// 题目类型缓存
const questionTypesCache = ref([])
const loaded = ref(false)

/**
 * 加载题目类型列表（带缓存）
 */
export async function loadQuestionTypes() {
    if (loaded.value) {
        return questionTypesCache.value
    }

    try {
        const res = await getQuestionTypes()
        if (res.code === 200 && res.data) {
            questionTypesCache.value = res.data
            loaded.value = true
            return questionTypesCache.value
        }
    } catch (e) {
        console.error('加载题目类型失败:', e)
    }

    return []
}

/**
 * 根据代码获取题目类型名称
 * @param {number} code - 题目类型代码
 * @returns {string} 题目类型名称
 */
export function getQuestionTypeName(code) {
    if (code === null || code === undefined) {
        return '未知类型'
    }

    // 如果已加载，从缓存查找
    if (loaded.value) {
        const type = questionTypesCache.value.find(t => t.code === code)
        return type ? type.name : '未知类型'
    }

    // 如果未加载，使用静态映射（作为降级方案）
    const staticMapping = {
        1: '单选题',
        2: '多选题',
        3: '填空题',
        4: '综合应用题',
        5: '完型填空',
        6: '阅读理解',
        7: '新题型',
        8: '翻译题',
        9: '小作文',
        10: '大作文'
    }

    return staticMapping[code] || '未知类型'
}

/**
 * 重置缓存（用于退出登录等场景）
 */
export function resetQuestionTypesCache() {
    questionTypesCache.value = []
    loaded.value = false
}
