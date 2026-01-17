import os
import json
import re
from zhipuai import ZhipuAI

# ================= 配置区域 =================
API_KEY = "e90579b6f4df4a8ab4b9381dfa1b7466.cgxYg4MwekKDlZHO"
INPUT_FILE = "2009年计算机统考真题及解析.md"
OUTPUT_FILE = "2009年计算机统考真题及解析.json"


def read_md_file(file_path):
    if not os.path.exists(file_path):
        raise FileNotFoundError(f"文件 {file_path} 未找到")
    with open(file_path, 'r', encoding='utf-8') as f:
        return f.read()


def clean_and_parse_json(response_text):
    """
    增强版清洗函数，专门处理Markdown包裹和LaTeX转义问题
    """
    # 1. 去除 Markdown 代码块标记 (```json ... ```)
    pattern = r"```(?:json)?\s*(\{.*?\})\s*```"
    match = re.search(pattern, response_text, re.DOTALL)

    if match:
        json_str = match.group(1)
    else:
        # 如果正则没匹配到，尝试暴力查找首尾大括号
        start = response_text.find('{')
        end = response_text.rfind('}')
        if start != -1 and end != -1:
            json_str = response_text[start:end + 1]
        else:
            return None, "未找到有效的JSON片段"

    # 2. 尝试直接解析
    try:
        return json.loads(json_str), None
    except json.JSONDecodeError as e:
        # 3. 补救措施：处理 LaTeX 单反斜杠问题
        print(f"标准解析失败 ({e})，尝试自动修复 LaTeX 转义...")

        try:
            # 先把合法的 JSON 转义符保护起来
            fixed_str = json_str.replace('\\\\', '___DOUBLE_BACKSLASH___')
            fixed_str = fixed_str.replace('\\"', '___QUOTE___') \
                                 .replace('\\n', '___NEWLINE___') \
                                 .replace('\\t', '___TAB___') \
                                 .replace('\\r', '___RETURN___')

            # 将剩余的（即LaTeX的）反斜杠变成双反斜杠
            fixed_str = fixed_str.replace('\\', '\\\\')

            # 还原合法的 JSON 转义符
            fixed_str = fixed_str.replace('___DOUBLE_BACKSLASH___', '\\\\') \
                                 .replace('___QUOTE___', '\\"') \
                                 .replace('___NEWLINE___', '\\n') \
                                 .replace('___TAB___', '\\t') \
                                 .replace('___RETURN___', '\\r')

            return json.loads(fixed_str), None
        except Exception as e2:
            return None, f"修复失败: {e2}\n\n原始片段:\n{json_str[:200]}..."


def convert_with_glm(content):
    client = ZhipuAI(api_key=API_KEY)

    # 修改 Prompt，强调 JSON 格式安全性
    system_prompt = """
你是一个专业的试题数据结构化助手。
任务：将 Markdown 试题全量转换为 JSON 格式。

**关键指令：**
1. 输出必须是合法的 JSON 格式。
2. **保留图片**：如果题干或解析中包含图片（如 `![](images/xxx.jpg)`），必须**原样保留**该字符串，绝对不要删除或修改图片路径。
3. 题目中包含 LaTeX 公式（如 $\\frac{a}{b}$），在 JSON 字符串中，**反斜杠必须转义**。
   - 错误写法: "content": "$ \\alpha $"
   - 正确写法: "content": "$ \\\\alpha $"
   - 必须确保所有的 \\ 变成 \\\\，否则 JSON 无法解析。
4. 不要输出 ```json 头和结尾，直接输出纯文本的 JSON 字符串最好，如果必须输出代码块请确保闭合。

**字段要求：**

- questionNumber: 题号（数字，如1、2、3...）
- type: 数字 (1:单选, 2:多选, 3:填空, 4:综合大题)
- content: 题干（注意：是content不是question）
- options: 数组 ["A. xxx", "B. xxx"]（选择题必填，其他题型为空数组[]）
- answer: 答案（单选填字母如"A"，多选填"A,B,C"）
- analysis: 解析
- tags: 数组（如["数据结构", "栈"]，没有则填空数组[]）

⚠️ 重要：
- 必须包含questionNumber字段（用于试卷导入时的排序）
- 不要使用question字段，题干必须用content
- 字段名必须是: questionNumber, type, content, options, answer, analysis, tags
- 图片标记 ![](images/xxx.jpg) 必须原样保留在content和analysis中

**输出结构：**
{ "questions": [ ... ] }
    """

    user_prompt = f"""
注意查找文档末尾的答案解析。

---文档片段---
{content}
---文档结束---
    """

    print("正在发送请求给 GLM...")
    try:
        response = client.chat.completions.create(
            model="glm-4.5",
            messages=[
                {"role": "system", "content": system_prompt},
                {"role": "user", "content": user_prompt}
            ],
            temperature=0.1,
            max_tokens=4095
        )
        return response.choices[0].message.content
    except Exception as e:
        print(f"API请求失败: {e}")
        return None


def main():
    try:
        full_content = read_md_file(INPUT_FILE)
        print(f"成功读取文件，字符数: {len(full_content)}")
    except Exception as e:
        print(e)
        return

    # 为了防止上下文过长，截取头尾（根据你的实际文档长度，如果小于10k token可以直接传全文）
    # 稍微增加截取长度以确保覆盖第46题
    header_part = full_content[:8000]
    footer_part = full_content[-5000:]
    content_to_process = header_part + "\n\n...[省略]...\n\n" + footer_part

    result_text = convert_with_glm(content_to_process)

    if result_text:
        # 使用新的解析函数
        data, error = clean_and_parse_json(result_text)

        if data:
            with open(OUTPUT_FILE, 'w', encoding='utf-8') as f:
                json.dump(data, f, ensure_ascii=False, indent=2)
            print(f"转换成功！共提取 {len(data.get('questions', []))} 道题。")
            print(f"文件已保存至: {OUTPUT_FILE}")
        else:
            print("解析JSON最终失败。")
            print(f"错误信息: {error}")
            # 调试用：把原始返回写入一个文本文件看看到底哪里错了
            with open("debug_raw_response.txt", "w", encoding="utf-8") as f:
                f.write(result_text)
            print("已将原始返回内容写入 debug_raw_response.txt 以便排查。")


if __name__ == "__main__":
    main()