<template>
    <div class="tutorial-container">
        <!-- 选项卡 -->
        <el-tabs v-model="activeTab" type="border-card">
            <el-tab-pane label="使用教程" name="guide">
                <div class="guide-content">
                    <!-- 第一步 -->
                    <div class="guide-step">
                        <h3>第一步：准备题目 PDF</h3>
                        <p>将您的题目整理成 PDF 格式，确保题目内容清晰、格式规范。</p>
                        <el-alert
                            title="提示"
                            type="info"
                            :closable="false"
                            show-icon
                        >
                            PDF 可以来源于教材、习题册、试卷扫描件等任何包含题目的文档。
                        </el-alert>
                    </div>

                    <!-- 第二步 -->
                    <div class="guide-step">
                        <h3>第二步：使用 AI 工作流转换 PDF 为 JSON</h3>
                        <p>我们推荐使用以下 AI 工作流平台进行 PDF 到 JSON 的转换：</p>

                        <div class="platform-list">
                            <el-card shadow="hover" class="platform-card">
                                <template #header>
                                    <div class="card-title">
                                        <el-icon><Platform /></el-icon>
                                        <span>Dify</span>
                                    </div>
                                </template>
                                <div class="platform-content">
                                    <p><strong>网址：</strong><a href="https://dify.ai" target="_blank">https://dify.ai</a></p>
                                    <p><strong>特点：</strong>国内可访问，功能强大，支持工作流编排</p>
                                    <el-button type="primary" size="small" @click="openDifyGuide">
                                        查看详细教程
                                    </el-button>
                                </div>
                            </el-card>

                            <el-card shadow="hover" class="platform-card">
                                <template #header>
                                    <div class="card-title">
                                        <el-icon><Platform /></el-icon>
                                        <span>Coze</span>
                                    </div>
                                </template>
                                <div class="platform-content">
                                    <p><strong>网址：</strong><a href="https://www.coze.cn" target="_blank">https://www.coze.cn</a></p>
                                    <p><strong>特点：</strong>字节跳动出品，支持工作流和插件</p>
                                    <el-button type="primary" size="small" @click="openCozeGuide">
                                        查看详细教程
                                    </el-button>
                                </div>
                            </el-card>

                            <el-card shadow="hover" class="platform-card">
                                <template #header>
                                    <div class="card-title">
                                        <el-icon><Platform /></el-icon>
                                        <span>FastGPT</span>
                                    </div>
                                </template>
                                <div class="platform-content">
                                    <p><strong>网址：</strong><a href="https://fastgpt.cn" target="_blank">https://fastgpt.cn</a></p>
                                    <p><strong>特点：</strong>开源知识库问答，支持工作流</p>
                                </div>
                            </el-card>
                        </div>

                        <el-divider />

                        <h4>通用工作流配置步骤：</h4>
                        <ol class="step-list">
                            <li>在平台上创建新工作流</li>
                            <li>添加「文件读取」或「PDF 解析」节点，上传题目 PDF</li>
                            <li>添加「LLM 大模型」节点，配置 Prompt 提取题目信息</li>
                            <li>添加「JSON 格式化」节点，确保输出符合指定格式</li>
                            <li>运行工作流，获取 JSON 结果</li>
                        </ol>

                        <el-collapse class="prompt-collapse">
                            <el-collapse-item title="查看推荐 Prompt 模板" name="1">
                                <el-input
                                    v-model="promptTemplate"
                                    type="textarea"
                                    :rows="12"
                                    readonly
                                    class="prompt-textarea"
                                />
                                <el-button
                                    type="primary"
                                    size="small"
                                    @click="copyPrompt"
                                    style="margin-top: 10px"
                                >
                                    复制 Prompt
                                </el-button>
                            </el-collapse-item>
                        </el-collapse>
                    </div>

                    <!-- 第三步 -->
                    <div class="guide-step">
                        <h3>第三步：验证 JSON 格式</h3>
                        <p>在导入页面使用内置的 JSON 验证功能，确保格式正确。</p>
                        <el-alert
                            title="JSON 格式要求"
                            type="warning"
                            :closable="false"
                            show-icon
                        >
                            <template #default>
                                <ul class="format-requirements">
                                    <li>必须包含 questions 数组</li>
                                    <li>每个题目必须包含：type（类型）、content（题干）、answer（答案）</li>
                                    <li>选择题必须包含 options 数组</li>
                                    <li>可选字段：analysis（解析）、tags（标签）、source（来源）</li>
                                </ul>
                            </template>
                        </el-alert>
                    </div>

                    <!-- 第四步 -->
                    <div class="guide-step">
                        <h3>第四步：选择目标习题册和科目</h3>
                        <p>在导入页面选择题目要导入到的习题册和科目。可以关联多个科目。</p>
                    </div>

                    <!-- 第五步 -->
                    <div class="guide-step">
                        <h3>第五步：确认并导入</h3>
                        <p>预览题目信息，确认无误后点击「开始导入」按钮。</p>
                        <el-alert
                            title="注意事项"
                            type="error"
                            :closable="false"
                            show-icon
                        >
                            <template #default>
                                <ul>
                                    <li>导入过程不可逆，请仔细检查 JSON 内容</li>
                                    <li>重复的题目会被重复导入（暂无去重功能）</li>
                                    <li>建议先少量测试，确认无误后再批量导入</li>
                                </ul>
                            </template>
                        </el-alert>
                    </div>
                </div>
            </el-tab-pane>

            <el-tab-pane label="JSON 格式说明" name="format">
                <div class="format-content">
                    <h3>JSON 格式规范</h3>

                    <el-card shadow="never" class="example-card">
                        <template #header>
                            <span>完整示例</span>
                        </template>
                        <el-input
                            v-model="jsonExample"
                            type="textarea"
                            :rows="20"
                            readonly
                            class="code-example"
                        />
                        <el-button
                            type="primary"
                            size="small"
                            @click="copyExample"
                            style="margin-top: 10px"
                        >
                            复制示例
                        </el-button>
                    </el-card>

                    <h3>字段说明</h3>
                    <el-table :data="fieldDescriptions" border class="field-table">
                        <el-table-column prop="field" label="字段名" width="150" />
                        <el-table-column prop="type" label="类型" width="120" />
                        <el-table-column prop="required" label="必填" width="80" align="center">
                            <template #default="scope">
                                <el-tag :type="scope.row.required ? 'danger' : 'info'" size="small">
                                    {{ scope.row.required ? '是' : '否' }}
                                </el-tag>
                            </template>
                        </el-table-column>
                        <el-table-column prop="description" label="说明" />
                        <el-table-column prop="values" label="可选值" width="200" />
                    </el-table>
                </div>
            </el-tab-pane>

            <el-tab-pane label="JSON 校验工具" name="validator">
                <div class="validator-content">
                    <h3>在线 JSON 校验</h3>
                    <p>将您的 JSON 粘贴到下方进行格式验证：</p>

                    <el-input
                        v-model="validatorInput"
                        type="textarea"
                        :rows="15"
                        placeholder="粘贴您的 JSON 内容..."
                        class="validator-input"
                    />

                    <div class="validator-actions">
                        <el-button type="primary" icon="Check" @click="validateInput">
                            验证格式
                        </el-button>
                        <el-button icon="Delete" @click="clearValidator">
                            清空
                        </el-button>
                        <el-button icon="Download" @click="downloadTemplate">
                            下载模板
                        </el-button>
                    </div>

                    <div v-if="validatorResult.valid" class="validation-result success">
                        <el-icon><SuccessFilled /></el-icon>
                        <span>验证通过！共 {{ validatorResult.count }} 道题目</span>
                    </div>

                    <div v-if="validatorResult.error" class="validation-result error">
                        <el-icon><CircleCloseFilled /></el-icon>
                        <span>{{ validatorResult.error }}</span>
                    </div>
                </div>
            </el-tab-pane>

            <el-tab-pane label="视频教程" name="video">
                <div class="video-content">
                    <el-empty description="视频教程正在制作中，敬请期待...">
                        <template #image>
                            <el-icon :size="100" color="#909399">
                                <VideoCameraFilled />
                            </el-icon>
                        </template>
                    </el-empty>

                    <el-divider />

                    <h3>推荐学习资源</h3>
                    <div class="resource-list">
                        <el-card shadow="hover" class="resource-card">
                            <h4>Dify 官方文档</h4>
                            <p>详细的工作流配置指南</p>
                            <el-button
                                type="primary"
                                link
                                @click="openLink('https://docs.dify.ai/guides/workflow')"
                            >
                                查看文档
                            </el-button>
                        </el-card>

                        <el-card shadow="hover" class="resource-card">
                            <h4>Coze 使用教程</h4>
                            <p>从零开始创建工作流</p>
                            <el-button
                                type="primary"
                                link
                                @click="openLink('https://www.coze.cn/docs/developer_guides/workflow')"
                            >
                                查看文档
                            </el-button>
                        </el-card>
                    </div>
                </div>
            </el-tab-pane>
        </el-tabs>
    </div>
</template>

<script setup>
import { ref } from 'vue'
import { ElMessage } from 'element-plus'
import {
    Platform,
    SuccessFilled,
    CircleCloseFilled,
    VideoCameraFilled
} from '@element-plus/icons-vue'
// import { emit } from '@/utils/eventBus'

const activeTab = ref('guide')

// Prompt 模板
const promptTemplate = ref(`你是一个专业的题目提取助手。请从提供的 PDF 内容中提取所有题目，并按照以下 JSON 格式输出：

要求：
1. 提取所有题目，包括选择题、填空题、简答题等
2. type 题目类型：1-单选, 2-多选, 3-填空, 4-简答
3. content 题干内容：完整提取，包括公式、符号等
4. options 选项数组：仅选择题需要，格式为 ["A. 选项1", "B. 选项2", ...]
5. answer 正确答案：单选填选项字母（如A），多选填字母组合（如ABC），填空和简答填完整答案
6. analysis 解析：如果原题有解析则提取，没有则留空
7. tags 标签数组：从题目内容中提取关键词，如["导数", "基础题"]

输出示例：
{
  "questions": [
    {
      "type": 1,
      "content": "设函数 f(x) = x³ - 3x + 1，求 f'(x)",
      "options": ["A. 3x² - 3", "B. 3x² + 3", "C. x² - 3", "D. x² + 3"],
      "answer": "A",
      "analysis": "根据求导法则，f'(x) = 3x² - 3",
      "tags": ["导数", "基础题"]
    }
  ]
}

请仅输出 JSON 格式，不要添加其他说明文字。`)

// JSON 示例
const jsonExample = ref(`{
  "questions": [
    {
      "type": 1,
      "content": "设函数 f(x) = x³ - 3x + 1，求 f'(x)",
      "options": [
        "A. 3x² - 3",
        "B. 3x² + 3",
        "C. x² - 3",
        "D. x² + 3"
      ],
      "answer": "A",
      "analysis": "根据求导法则，f'(x) = 3x² - 3",
      "tags": ["导数", "基础题"],
      "source": "高等数学例题"
    },
    {
      "type": 2,
      "content": "下列哪些函数在区间 (0, +∞) 上单调递增？",
      "options": [
        "A. f(x) = x²",
        "B. f(x) = eˣ",
        "C. f(x) = ln(x)",
        "D. f(x) = 1/x"
      ],
      "answer": "ABC",
      "analysis": "x²在x>0时单调递增；eˣ始终单调递增；ln(x)在定义域内单调递增；1/x在x>0时单调递减",
      "tags": ["单调性", "多选题"]
    }
  ]
}`)

// 字段说明
const fieldDescriptions = [
    {
        field: 'type',
        type: 'Integer',
        required: true,
        description: '题目类型',
        values: '1-单选, 2-多选, 3-填空, 4-简答'
    },
    {
        field: 'content',
        type: 'String',
        required: true,
        description: '题干内容，支持 LaTeX 公式',
        values: '-'
    },
    {
        field: 'options',
        type: 'Array',
        required: false,
        description: '选项数组，仅选择题需要',
        values: '["A. 选项1", "B. 选项2", ...]'
    },
    {
        field: 'answer',
        type: 'String',
        required: true,
        description: '正确答案',
        values: '单选为字母，多选为字母组合'
    },
    {
        field: 'analysis',
        type: 'String',
        required: false,
        description: '题目解析',
        values: '-'
    },
    {
        field: 'tags',
        type: 'Array',
        required: false,
        description: '题目标签',
        values: '["标签1", "标签2", ...]'
    },
    {
        field: 'source',
        type: 'String',
        required: false,
        description: '题目来源',
        values: '-'
    }
]

// 验证器
const validatorInput = ref('')
const validatorResult = ref({
    valid: false,
    count: 0,
    error: ''
})

// 复制 Prompt
const copyPrompt = () => {
    navigator.clipboard.writeText(promptTemplate.value)
    ElMessage.success('Prompt 已复制到剪贴板')
}

// 复制示例
const copyExample = () => {
    navigator.clipboard.writeText(jsonExample.value)
    ElMessage.success('示例已复制到剪贴板')
}

// 验证输入
const validateInput = () => {
    try {
        const data = JSON.parse(validatorInput.value)

        if (!data.questions || !Array.isArray(data.questions)) {
            throw new Error('JSON 缺少 questions 数组')
        }

        if (data.questions.length === 0) {
            throw new Error('questions 数组为空')
        }

        for (let i = 0; i < data.questions.length; i++) {
            const q = data.questions[i]

            if (!q.type || q.type < 1 || q.type > 10) {
                throw new Error(`第 ${i + 1} 题的 type 字段无效`)
            }

            if (!q.content) {
                throw new Error(`第 ${i + 1} 题缺少 content 字段`)
            }

            if ((q.type === 1 || q.type === 2) && !q.options) {
                throw new Error(`第 ${i + 1} 题是选择题，必须包含 options`)
            }
        }

        validatorResult.value = {
            valid: true,
            count: data.questions.length,
            error: ''
        }

        ElMessage.success(`验证通过！共 ${data.questions.length} 道题目`)
    } catch (e) {
        validatorResult.value = {
            valid: false,
            count: 0,
            error: e.message
        }
        ElMessage.error('验证失败：' + e.message)
    }
}

// 清空验证器
const clearValidator = () => {
    validatorInput.value = ''
    validatorResult.value = {
        valid: false,
        count: 0,
        error: ''
    }
}

// 下载模板
const downloadTemplate = () => {
    const blob = new Blob([jsonExample.value], { type: 'application/json' })
    const url = URL.createObjectURL(blob)
    const a = document.createElement('a')
    a.href = url
    a.download = 'questions_template.json'
    a.click()
    URL.revokeObjectURL(url)
    ElMessage.success('模板已下载')
}

// 打开链接
const openLink = (url) => {
    window.open(url, '_blank')
}

// 平台教程
const openDifyGuide = () => {
    openLink('https://docs.dify.ai/zh/use-dify/getting-started/introduction')
}

const openCozeGuide = () => {
    openLink('https://www.coze.cn/docs/developer_guides/workflow')
}

</script>

<style scoped>
.tutorial-container {
    padding: 20px;
}

.guide-content {
    padding: 20px 0;
}

.guide-step {
    margin-bottom: 40px;
}

.guide-step h3 {
    font-size: 18px;
    font-weight: 600;
    color: #303133;
    margin-bottom: 15px;
}

.guide-step p {
    color: #606266;
    line-height: 1.8;
    margin-bottom: 15px;
}

.platform-list {
    display: grid;
    grid-template-columns: repeat(auto-fit, minmax(280px, 1fr));
    gap: 20px;
    margin: 20px 0;
}

.platform-card {
    border-radius: 8px;
}

.platform-card .card-title {
    display: flex;
    align-items: center;
    gap: 10px;
    font-weight: 600;
}

.platform-content p {
    margin: 10px 0;
}

.platform-content a {
    color: #409eff;
    text-decoration: none;
}

.platform-content a:hover {
    text-decoration: underline;
}

.step-list {
    padding-left: 20px;
}

.step-list li {
    margin: 10px 0;
    line-height: 1.8;
    color: #606266;
}

.prompt-collapse {
    margin-top: 20px;
}

.prompt-textarea {
    font-family: 'Monaco', 'Courier New', monospace;
    font-size: 13px;
}

.format-requirements {
    margin: 10px 0;
    padding-left: 20px;
}

.format-requirements li {
    margin: 5px 0;
    line-height: 1.6;
}

.format-content {
    padding: 20px 0;
}

.format-content h3 {
    font-size: 18px;
    font-weight: 600;
    color: #303133;
    margin: 20px 0 15px;
}

.example-card {
    margin-bottom: 20px;
}

.code-example {
    font-family: 'Monaco', 'Courier New', monospace;
    font-size: 13px;
}

.field-table {
    margin-top: 15px;
}

.validator-content {
    padding: 20px 0;
}

.validator-content h3 {
    font-size: 18px;
    font-weight: 600;
    color: #303133;
    margin-bottom: 15px;
}

.validator-input {
    font-family: 'Monaco', 'Courier New', monospace;
    font-size: 13px;
    margin-bottom: 15px;
}

.validator-actions {
    display: flex;
    gap: 10px;
    margin-bottom: 20px;
}

.validation-result {
    padding: 15px;
    border-radius: 4px;
    display: flex;
    align-items: center;
    gap: 10px;
}

.validation-result.success {
    background: #f0f9ff;
    border-left: 3px solid #67c23a;
    color: #67c23a;
}

.validation-result.error {
    background: #fef0f0;
    border-left: 3px solid #f56c6c;
    color: #f56c6c;
}

.video-content {
    padding: 20px 0;
    text-align: center;
}

.resource-list {
    display: grid;
    grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
    gap: 20px;
    margin-top: 20px;
}

.resource-card {
    text-align: left;
}

.resource-card h4 {
    font-size: 16px;
    font-weight: 600;
    color: #303133;
    margin-bottom: 10px;
}

.resource-card p {
    color: #909399;
    font-size: 14px;
    margin-bottom: 15px;
}
</style>
