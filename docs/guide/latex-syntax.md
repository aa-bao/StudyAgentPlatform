# LaTeX 公式使用指南

## 概述

本系统全面支持 LaTeX 数学公式，包括：
- **AI 识别**：GLM-4.6V-Flash 自动识别图片中的数学公式并转换为 LaTeX
- **前端渲染**：使用 KaTeX 实时渲染数学公式
- **数据库存储**：以文本形式存储 LaTeX 代码
- **PDF 导出**：导出时正确渲染公式

## 基础语法

### 行内公式

使用 `$...$` 包裹，嵌入文本中：

```
设函数 $f(x) = x^2$，求其导数
```

渲染效果：设函数 $f(x) = x^2$，求其导数

### 块级公式

使用 `$$...$$` 包裹，独立成行显示：

```
答案是：$$\frac{1}{3}$$
```

渲染效果：

答案是：$$\frac{1}{3}$$

## 常用符号

### 数字和字母

| 类型 | 示例 | LaTeX | 渲染 |
|------|------|-------|------|
| 普通数字 | 123 | `123` | 123 |
| 希腊字母小写 | α, β, γ | `\alpha`, `\beta`, `\gamma` | α, β, γ |
| 希腊字母大写 | Γ, Δ, Σ | `\Gamma`, `\Delta`, `\Sigma` | Γ, Δ, Σ |
| 罗马数字 | Ⅰ, Ⅱ, Ⅲ | `\mathrm{I}`, `\mathrm{II}`, `\mathrm{III}` | Ⅰ, Ⅱ, Ⅲ |
| 花体字母 | 𝒜, ℬ | `\mathcal{A}`, `\mathcal{B}` | 𝒜, ℬ |

### 运算符

| 运算 | 符号 | LaTeX | 渲染 |
|------|------|-------|------|
| 加 | + | `+` | + |
| 减 | - | `-` | - |
| 乘 | × | `\times` | × |
| 除 | ÷ | `\div` | ÷ |
| 加减 | ± | `\pm` | ± |
| 点乘 | ⋅ | `\cdot` | ⋅ |

### 关系符号

| 关系 | 符号 | LaTeX | 渲染 |
|------|------|-------|------|
| 等于 | = | `=` | = |
| 不等于 | ≠ | `\neq` 或 `\ne` | ≠ |
| 大于 | > | `>` | > |
| 小于 | < | `<` | < |
| 大于等于 | ≥ | `\geq` 或 `\ge` | ≥ |
| 小于等于 | ≤ | `\leq` 或 `\le` | ≤ |
| 约等于 | ≈ | `\approx` | ≈ |
| 恒等于 | ≡ | `\equiv` | ≡ |
| 相似 | ∼ | `\sim` | ∼ |
| 属于 | ∈ | `\in` | ∈ |
| 不属于 | ∉ | `\notin` | ∉ |

### 高级运算

| 运算 | 符号 | LaTeX | 渲染 |
|------|------|-------|------|
| 分数 | $\frac{a}{b}$ | `\frac{a}{b}` | $\frac{a}{b}$ |
| 根号 | $\sqrt{x}$ | `\sqrt{x}` | $\sqrt{x}$ |
| n 次根号 | $\sqrt[3]{x}$ | `\sqrt[3]{x}` | $\sqrt[3]{x}$ |
| 上标 | $x^2$ | `x^2` | $x^2$ |
| 下标 | $x_1$ | `x_1` | $x_1$ |
| 上下标 | $x_1^2$ | `x_1^2` | $x_1^2$ |
| 求和 | $\sum_{i=1}^{n}$ | `\sum_{i=1}^{n}` | $\sum_{i=1}^{n}$ |
| 积分 | $\int_{a}^{b}$ | `\int_{a}^{b}` | $\int_{a}^{b}$ |
| 极限 | $\lim_{x \to 0}$ | `\lim_{x \to 0}` | $\lim_{x \to 0}$ |
| 偏导数 | $\frac{\partial f}{\partial x}$ | `\frac{\partial f}{\partial x}` | $\frac{\partial f}{\partial x}$ |

### 箭头

| 类型 | 符号 | LaTeX | 渲染 |
|------|------|-------|------|
| 右箭头 | → | `\to` 或 `\rightarrow` | → |
| 左箭头 | ← | `\leftarrow` | ← |
| 双箭头 | ↔ | `\leftrightarrow` | ↔ |
| 长箭头 | ⟶ | `\longrightarrow` | ⟶ |
| 上箭头 | ↑ | `\uparrow` | ↑ |
| 下箭头 | ↓ | `\downarrow` | ↓ |

## 矩阵和向量

### 矩阵

```
$$
\begin{pmatrix}
a & b \\
c & d
\end{pmatrix}
$$
```

渲染效果：
$$
\begin{pmatrix}
a & b \\
c & d
\end{pmatrix}
$$

### 其他矩阵环境

| 类型 | LaTeX | 渲染 |
|------|-------|------|
| 圆括号 | `\begin{pmatrix} ... \end{pmatrix}` | $\begin{pmatrix} a & b \\ c & d \end{pmatrix}$ |
| 方括号 | `\begin{bmatrix} ... \end{bmatrix}` | $\begin{bmatrix} a & b \\ c & d \end{bmatrix}$ |
| 花括号 | `\begin{Bmatrix} ... \end{Bmatrix}` | $\begin{Bmatrix} a & b \\ c & d \end{Bmatrix}$ |
| 竖线 | `\begin{vmatrix} ... \end{vmatrix}` | $\begin{vmatrix} a & b \\ c & d \end{vmatrix}$ |

### 向量

**行向量**：
```
$$\vec{a} = (a_1, a_2, a_3)$$
```

$$\vec{a} = (a_1, a_2, a_3)$$

**列向量**：
```
$$\vec{v} = \begin{pmatrix} v_1 \\ v_2 \\ v_3 \end{pmatrix}$$
```

$$\vec{v} = \begin{pmatrix} v_1 \\ v_2 \\ v_3 \end{pmatrix}$$

## 微积分

### 导数

```
一阶导数：$f'(x)$ 或 $\frac{dy}{dx}$
二阶导数：$f''(x)$ 或 $\frac{d^2y}{dx^2}$
偏导数：$\frac{\partial f}{\partial x}$
```

渲染效果：
- 一阶导数：$f'(x)$ 或 $\frac{dy}{dx}$
- 二阶导数：$f''(x)$ 或 $\frac{d^2y}{dx^2}$
- 偏导数：$\frac{\partial f}{\partial x}$

### 积分

**不定积分**：
```
$$\int f(x) dx$$
```

$$\int f(x) dx$$

**定积分**：
```
$$\int_{a}^{b} f(x) dx$$
```

$$\int_{a}^{b} f(x) dx$$

**多重积分**：
```
二重积分：$$\iint_{D} f(x,y) dxdy$$
三重积分：$$\iiint_{V} f(x,y,z) dxdydz$$
曲线积分：$$\oint_{C} \mathbf{F} \cdot d\mathbf{r}$$
```

### 极限

```
$$\lim_{x \to \infty} \frac{1}{x} = 0$$
```

$$\lim_{x \to \infty} \frac{1}{x} = 0$$

**单侧极限**：
```
左极限：$$\lim_{x \to a^-} f(x)$$
右极限：$$\lim_{x \to a^+} f(x)$$
```

## 方程和不等式

### 多行公式

使用 `\\` 换行：

```
$$
\begin{cases}
x + y = 1 \\
x - y = 0
\end{cases}
$$
```

渲染效果：
$$
\begin{cases}
x + y = 1 \\
x - y = 0
\end{cases}
$$

### 对齐

使用 `&` 对齐，`\\` 换行：

```
$$
\begin{aligned}
f(x) &= x^2 + 2x + 1 \\
&= (x + 1)^2
\end{aligned}
$$
```

渲染效果：
$$
\begin{aligned}
f(x) &= x^2 + 2x + 1 \\
&= (x + 1)^2
\end{aligned}
$$

## 常见函数

| 函数 | LaTeX | 渲染 |
|------|-------|------|
| 正弦 | `\sin x` | $\sin x$ |
| 余弦 | `\cos x` | $\cos x$ |
| 正切 | `\tan x` | $\tan x$ |
| 反正弦 | `\arcsin x` | $\arcsin x$ |
| 对数 | `\log x` | $\log x$ |
| 自然对数 | `\ln x` | $\ln x$ |
| 指数 | `e^x` 或 `\exp(x)` | $e^x$ 或 $\exp(x)$ |
| 最大值 | `\max` | $\max$ |
| 最小值 | `\min` | $\min$ |

## 空格和间距

| 空格类型 | LaTeX | 渲染 |
|---------|-------|------|
| 正常空格 | `a b` | $a b$ |
| 小空格 | `a\,b` | $a\,b$ |
| 中等空格 | `a\;b` | $a\;b$ |
| 大空格 | `a\ b` | $a\ b$ |
| quad 空格 | `a\quad b` | $a\quad b$ |
| 两个 quad | `a\qquad b` | $a\qquad b$ |

## 在系统中使用

### 1. 题目内容

```json
{
  "content": "设函数 $f(x) = x^3 - 3x + 1$，求 $f'(x)$"
}
```

### 2. 选项

```json
{
  "options": [
    {"label": "A", "text": "$$3x^2-3$$"},
    {"label": "B", "text": "$$3x^2+3$$"}
  ]
}
```

### 3. 答案

```json
{
  "answer": "$$\\frac{1}{3}$$"
}
```

### 4. 解析

```json
{
  "analysis": "根据求导法则：$$f'(x) = \\lim_{h \\to 0} \\frac{f(x+h)-f(x)}{h} = 3x^2-3$$"
}
```

## 前端渲染

### Vue 3 + KaTeX

**安装 KaTeX**：
```bash
npm install katex
```

**渲染函数**：
```javascript
import katex from 'katex'

const renderLatex = (text) => {
  if (!text) return ''

  // 匹配 $$...$$ (块级公式)
  const blockRegex = /\$\$([^$]+)\$\$/g
  // 匹配 $...$ (行内公式)
  const inlineRegex = /\$([^$]+)\$/g

  let result = text
  const replacements = []

  // 先收集所有块级公式
  result = result.replace(blockRegex, (_, tex) => {
    const placeholder = `___LATEX_BLOCK_${replacements.length}___`
    replacements.push({ type: 'block', tex })
    return placeholder
  })

  // 再收集所有行内公式
  result = result.replace(inlineRegex, (_, tex) => {
    const placeholder = `___LATEX_INLINE_${replacements.length}___`
    replacements.push({ type: 'inline', tex })
    return placeholder
  })

  // 替换占位符为实际的 KaTeX HTML
  replacements.forEach((item, idx) => {
    const placeholder = item.type === 'block' ? `___LATEX_BLOCK_${idx}___` : `___LATEX_INLINE_${idx}___`
    try {
      const html = katex.renderToString(item.tex, {
        throwOnError: false,
        displayMode: item.type === 'block'
      })
      result = result.replace(placeholder, html)
    } catch (e) {
      console.error('KaTeX render error:', e)
    }
  })

  return result
}
```

**模板使用**：
```vue
<template>
  <div v-html="renderLatex(question.content)"></div>
</template>
```

## AI 识别

### GLM-4.6V-Flash 提示词

系统会自动提示 AI 使用 LaTeX 格式：

```
你是一个专业的数学题目识别助手。

【LaTeX公式处理要求】
- 题目中的数学公式必须用LaTeX语法表示
- 行内公式用 $...$ 包裹
- 独立公式用 $$...$$ 包裹
- 支持常见LaTeX数学符号：分数\frac{}{}、上下标_^{}、积分\int、求和\sum、根号\sqrt{}、希腊字母等
```

## 常见问题

### 1. 反斜杠转义

**问题**：AI 返回的 JSON 中 `\frac` 导致解析错误

**解决**：后端自动转义，将 `\` 替换为 `\\`

```java
// GLMServiceImpl.java
private String fixLaTeXEscaping(String json) {
    // 自动修复 LaTeX 反斜杠
}
```

### 2. 公式不显示

**问题**：前端公式渲染为空白

**可能原因**：
- LaTeX 语法错误
- KaTeX 未正确导入
- 反斜杠转义问题

**解决**：
1. 检查浏览器控制台错误
2. 验证 LaTeX 语法
3. 确认 KaTeX 已安装

### 3. 性能问题

**问题**：大量公式渲染慢

**解决**：
- 使用 `v-once` 指令避免重复渲染
- 分页加载
- 缓存渲染结果

## 最佳实践

### 1. 一致性

- ✅ 统一使用 `$` 和 `$$`
- ✅ 简单公式用行内，复杂公式用块级
- ✅ 数学符号用 LaTeX，普通文本用中文

### 2. 可读性

- ✅ 适当添加空格：`$x^2 + y^2$`
- ✅ 复杂公式添加括号：`$\frac{(a+b)^2}{c}$`
- ✅ 长公式换行展示

### 3. 验证

- ✅ AI 识别后手动检查
- ✅ 使用 LaTeX 编辑器预览
- ✅ 测试前端渲染效果

## 在线工具

### LaTeX 编辑器

- **Overleaf**：https://www.overleaf.com/
- **Codecogs**：https://www.codecogs.com/latex/eqneditor.php
- **Mathpix**：截图转 LaTeX（https://mathpix.com/）

### 语法参考

- **KaTeX 支持的函数**：https://katex.org/docs/supported.html
- **LaTeX Wiki**：https://en.wikibooks.org/wiki/LaTeX/Mathematics
- **Markdown + LaTeX**：https://www.math.ubc.ca/~pwalls/mathjax/md-tutorial/

## 相关文档

- [数据库设计文档](../database/question-table-design.md)
- [AI 图片识别文档](../ai/image-recognition.md)
- [JSON 导入导出格式](../format/json-import-export.md)
