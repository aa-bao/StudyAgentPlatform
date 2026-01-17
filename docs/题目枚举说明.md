# 题目类型枚举使用说明

## 概述
已将题目类型定义为 Java 枚举，并提供统一的接口供前端使用。避免了前端硬编码类型转换，便于维护。

## 后端

### 枚举定义
文件位置：`KaoYanPlatform-back/src/main/java/org/example/kaoyanplatform/enums/QuestionType.java`

包含的题目类型：
- 1: 单选题
- 2: 多选题
- 3: 填空题
- 4: 综合应用题
- 5: 完型填空
- 6: 阅读理解
- 7: 新题型
- 8: 翻译题
- 9: 小作文
- 10: 大作文

### 接口
**获取所有题目类型列表**
```
GET /common/question-types
```

响应示例：
```json
{
  "code": 200,
  "data": [
    { "code": 1, "name": "单选题" },
    { "code": 2, "name": "多选题" },
    ...
  ]
}
```

## 前端

### 工具函数
文件位置：`KaoYanPlatform-front/src/utils/questionTypes.js`

提供三个函数：

#### 1. `loadQuestionTypes()`
加载题目类型列表（带缓存）
```javascript
import { loadQuestionTypes } from '@/utils/questionTypes'

const types = await loadQuestionTypes()
// 返回: [{ code: 1, name: '单选题' }, ...]
```

#### 2. `getQuestionTypeName(code)`
根据代码获取题目类型名称
```javascript
import { getQuestionTypeName } from '@/utils/questionTypes'

const name = getQuestionTypeName(1) // 返回: '单选题'
const name = getQuestionTypeName(999) // 返回: '未知类型'
```

#### 3. `resetQuestionTypesCache()`
重置缓存（用于退出登录等场景）
```javascript
import { resetQuestionTypesCache } from '@/utils/questionTypes'

resetQuestionTypesCache()
```

### 使用示例

#### 在组件中使用

**示例 1：显示题目类型名称**
```vue
<template>
  <el-table :data="questions">
    <el-table-column label="类型" width="100">
      <template #default="scope">
        {{ getQuestionTypeName(scope.row.type) }}
      </template>
    </el-table-column>
  </el-table>
</template>

<script setup>
import { getQuestionTypeName } from '@/utils/questionTypes'
</script>
```

**示例 2：题目类型下拉选择**
```vue
<template>
  <el-select v-model="form.type" placeholder="请选择题目类型">
    <el-option
      v-for="type in questionTypes"
      :key="type.code"
      :label="type.name"
      :value="type.code"
    />
  </el-select>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { loadQuestionTypes } from '@/utils/questionTypes'

const questionTypes = ref([])

onMounted(async () => {
  questionTypes.value = await loadQuestionTypes()
})
</script>
```

**示例 3：在主应用中预加载（推荐）**
在 `App.vue` 或主布局中预加载，避免每个组件都重复加载：

```vue
<script setup>
import { onMounted } from 'vue'
import { loadQuestionTypes } from '@/utils/questionTypes'

onMounted(() => {
  // 预加载题目类型缓存
  loadQuestionTypes()
})
</script>
```

### API 调用
如果需要直接调用接口：

```javascript
import { getQuestionTypes } from '@/api/common'

const res = await getQuestionTypes()
if (res.code === 200) {
  console.log(res.data) // 题目类型列表
}
```

## 优势

1. **类型安全**：后端使用枚举，编译时检查
2. **集中管理**：修改题目类型只需改一处
3. **性能优化**：前端缓存，避免重复请求
4. **易于维护**：新增类型只需在枚举中添加一行
5. **降级方案**：提供静态映射作为降级，接口失败不影响显示

## 扩展新的题目类型

如需添加新的题目类型：

1. **后端**：在 `QuestionType.java` 中添加
```java
NEW_TYPE(11, "新类型名称"),
```

2. **前端无需修改**：重新加载后会自动获取最新类型
