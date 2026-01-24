import os
import json
import re
from openai import OpenAI
from concurrent.futures import ThreadPoolExecutor, as_completed
import time

# ================= 配置区域 =================
API_KEY = "sk-f07aded12d264640bb3ff865ac211bc3"
BASE_URL = "https://api.deepseek.com"
INPUT_FILE = "2009年计算机统考真题及解析.md"
OUTPUT_FILE = "2009年计算机统考真题及解析1.json"


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
            # 4. 如果还是失败，尝试修复常见的不完整JSON问题
            print(f"LaTeX修复失败 ({e2})，尝试修复不完整的JSON...")

            try:
                # 移除末尾可能的截断标记
                fixed_str = json_str

                # 修复常见的截断模式
                fixed_str = re.sub(r',id\)?"?$', '"', fixed_str, flags=re.MULTILINE)  # ,id)" 或 ,id)"
                fixed_str = re.sub(r'\(id:[^"]*$', '', fixed_str, flags=re.MULTILINE)  # (id:xxx
                fixed_str = re.sub(r',\s*$', '', fixed_str, flags=re.MULTILINE)  # 末尾逗号

                # 修复未闭合的字符串（在字符串内的截断）
                fixed_str = re.sub(r'[^"]\([^"]*$', '', fixed_str, flags=re.MULTILINE)

                # 修复analysis字段中可能的截断
                fixed_str = re.sub(r'"analysis":\s*"[^"]*$', '"analysis": ""', fixed_str, flags=re.MULTILINE)

                # 再次尝试LaTeX修复
                fixed_str = fixed_str.replace('\\\\', '___DOUBLE_BACKSLASH___')
                fixed_str = fixed_str.replace('\\"', '___QUOTE___') \
                                     .replace('\\n', '___NEWLINE___') \
                                     .replace('\\t', '___TAB___') \
                                     .replace('\\r', '___RETURN___')
                fixed_str = fixed_str.replace('\\', '\\\\')
                fixed_str = fixed_str.replace('___DOUBLE_BACKSLASH___', '\\\\') \
                                     .replace('___QUOTE___', '\\"') \
                                     .replace('___NEWLINE___', '\\n') \
                                     .replace('___TAB___', '\\t') \
                                     .replace('___RETURN___', '\\r')

                # 确保对象正确闭合
                if not fixed_str.rstrip().endswith('}'):
                    # 尝试智能闭合
                    lines = fixed_str.rstrip().split('\n')
                    last_line = lines[-1]

                    # 如果最后一行是未完成的对象，尝试完成它
                    if last_line.count('"') % 2 == 1:  # 奇数个引号，说明字符串未闭合
                        last_line += '"'
                    if not last_line.rstrip().endswith('}') and not last_line.rstrip().endswith(']'):
                        last_line += '\n  }'

                    lines[-1] = last_line
                    fixed_str = '\n'.join(lines)

                    # 如果还是没有结束，添加结束标记
                    if not fixed_str.rstrip().endswith('}]\n}'):
                        fixed_str = fixed_str.rstrip()
                        if fixed_str.endswith(']'):
                            fixed_str += '\n}'
                        elif fixed_str.endswith('}'):
                            fixed_str += '\n]\n}'
                        else:
                            fixed_str += '\n  }\n]\n}'

                return json.loads(fixed_str), None
            except Exception as e3:
                return None, f"所有修复尝试失败: {e3}\n\n原始片段:\n{json_str[:500]}..."


def convert_with_deepseek(content):
    client = OpenAI(api_key=API_KEY, base_url=BASE_URL)

    # 修改 Prompt，强调 JSON 格式安全性
    system_prompt = """
你是一个专业的试题数据结构化助手。
任务：将 Markdown 试题全量转换为 JSON 格式。

**关键指令：**
1. 输出必须是合法的 JSON 格式。
2. **保留图片**：如果题干中包含图片（如 `![](images/xxx.jpg)`），必须**原样保留**该字符串。
3. 题目中包含 LaTeX 公式（如 $\\frac{a}{b}$），在 JSON 字符串中，**反斜杠必须转义**。
   - 错误写法: "content": "$ \\alpha $"
   - 正确写法: "content": "$ \\\\alpha $"
   - 必须确保所有的 \\ 变成 \\\\，否则 JSON 无法解析。
4. 不要输出 ```json 头和结尾，直接输出纯文本的 JSON 字符串。

**字段要求：**

- questionNumber: 题号（数字，如1、2、3...）
- type: 数字 (1:单选, 2:多选, 3:填空, 4:综合大题)
- content: 题干（**只提取题目本身，不要包含解析和答案！**）
- options: 数组 ["A. xxx", "B. xxx"]（选择题必填，其他题型为空数组[]）
- answer: 答案（**如果题目中未给出答案则留空**，单选填字母如"A"，多选填"A,B,C"）
- analysis: 解析（**始终留空字符串""**，解析会单独处理）
- tags: 数组（如["数据结构", "栈"]，没有则填空数组[]）

⚠️ 重要：
- 必须包含questionNumber字段（用于试卷导入时的排序）
- 不要使用question字段，题干必须用content
- 字段名必须是: questionNumber, type, content, options, answer, analysis, tags
- content字段**只包含题目描述**，不要包含解析
- **analysis字段始终填空字符串""**
- **不要尝试提取答案或解析**，这些信息会在后续步骤中处理
- 图片标记 ![](images/xxx.jpg) 必须原样保留

**输出结构：**
{ "questions": [ ... ] }
    """

    user_prompt = f"""
将以下题目转换为JSON格式。**注意：只提取题目信息，不需要提取解析。**

---文档片段---
{content}
---文档结束---
    """

    print("正在发送请求给 DeepSeek...")
    start_time = time.time()
    try:
        response = client.chat.completions.create(
            model="deepseek-chat",
            messages=[
                {"role": "system", "content": system_prompt},
                {"role": "user", "content": user_prompt}
            ],
            temperature=0.1,  # 降低温度提高速度和稳定性
            max_tokens=8192  # 增加到16K,避免长解析被截断
        )
        elapsed = time.time() - start_time
        print(f"请求完成，耗时: {elapsed:.2f}秒")
        return response.choices[0].message.content
    except Exception as e:
        print(f"API请求失败: {e}")
        return None


def split_questions_and_answers(content):
    """
    将文档分为题目部分和解析部分
    """
    # 查找解析部分的开始位置(通常在"计算机专业基础综合考试真题思路分析"之后)
    # 或者查找 "解答:"、"答案:" 等关键词
    patterns = [
        r'# 计算机专业基础综合考试真题思路分析',
        r'#[\s]*解答',
        r'#[\s]*答案解析',
        r'\n\d+[\s]*\.[\s]*解答'
    ]

    split_pos = len(content)
    for pattern in patterns:
        match = re.search(pattern, content)
        if match:
            split_pos = min(split_pos, match.start())

    questions_part = content[:split_pos]
    answers_part = content[split_pos:]

    return questions_part, answers_part


def split_questions(content):
    """
    将题目部分按题号分割成多个批次
    每个批次最多15道题
    """
    # 按题号分割（假设题目以 "1." "2." 等开头）
    question_pattern = r'\n(?=\d+\.)'
    questions = re.split(question_pattern, content)

    # 过滤掉空段落
    questions = [q.strip() for q in questions if q.strip()]

    # 每10题一个批次
    batches = []
    batch_size = 10

    for i in range(0, len(questions), batch_size):
        batch = questions[i:i+batch_size]
        batches.append('\n\n'.join(batch))

    return batches


def process_batch(batch_info):
    """
    处理单个批次（用于并发调用）
    batch_info: (batch_index, batch_content)
    """
    batch_index, batch_content = batch_info
    print(f"\n[线程 {batch_index}] 正在处理第 {batch_index} 批...")
    result_text = convert_with_deepseek(batch_content)

    if result_text:
        # 使用新的解析函数
        data, error = clean_and_parse_json(result_text)

        if data and 'questions' in data:
            questions = data['questions']
            print(f"[线程 {batch_index}] 第 {batch_index} 批成功提取 {len(questions)} 道题")
            return batch_index, questions, None
        else:
            print(f"[线程 {batch_index}] 第 {batch_index} 批解析失败: {error}")
            # 调试用：把原始返回写入一个文本文件看看到底哪里错了
            with open(f"debug_batch_{batch_index}.txt", "w", encoding="utf-8") as f:
                f.write(result_text)
            print(f"[线程 {batch_index}] 已将原始返回内容写入 debug_batch_{batch_index}.txt 以便排查。")
            return batch_index, None, error
    else:
        print(f"[线程 {batch_index}] 第 {batch_index} 批API调用失败")
        return batch_index, None, "API调用失败"


def main():
    try:
        full_content = read_md_file(INPUT_FILE)
        print(f"成功读取文件，字符数: {len(full_content)}")
    except Exception as e:
        print(e)
        return

    # 第一步：分离题目和解析
    print("\n[第一步] 分离题目部分和解析部分...")
    questions_part, answers_part = split_questions_and_answers(full_content)
    print(f"题目部分字符数: {len(questions_part)}")
    print(f"解析部分字符数: {len(answers_part)}")

    # 第二步：处理题目部分
    print(f"\n[第二步] 提取题目信息...")
    batches = split_questions(questions_part)
    print(f"题目将分成 {len(batches)} 个批次处理")

    all_questions = []
    total_start_time = time.time()

    # 使用线程池并发处理（最多3个并发，避免API限流）
    max_workers = 3
    batch_info_list = [(i, batch) for i, batch in enumerate(batches, 1)]

    with ThreadPoolExecutor(max_workers=max_workers) as executor:
        # 提交所有任务
        future_to_batch = {
            executor.submit(process_batch, batch_info): batch_info[0]
            for batch_info in batch_info_list
        }

        # 按完成顺序处理结果
        for future in as_completed(future_to_batch):
            batch_index = future_to_batch[future]
            try:
                index, questions, error = future.result()
                if questions is not None:
                    all_questions.extend(questions)
            except Exception as exc:
                print(f"第 {batch_index} 批处理异常: {exc}")

    total_elapsed = time.time() - total_start_time

    # 第三步：处理解析部分(可选,如果需要的话)
    print(f"\n[第三步] 解析部分暂不处理(可根据需要启用)")
    # TODO: 可以添加一个单独的函数来处理解析部分,将解析匹配到对应题号

    # 保存所有题目
    if all_questions:
        # 按questionNumber排序（确保题目顺序正确）
        all_questions.sort(key=lambda x: x.get('questionNumber', 0))

        final_data = {"questions": all_questions}
        with open(OUTPUT_FILE, 'w', encoding='utf-8') as f:
            json.dump(final_data, f, ensure_ascii=False, indent=2)
        print(f"\n{'='*50}")
        print(f"转换成功！共提取 {len(all_questions)} 道题。")
        print(f"总耗时: {total_elapsed:.2f}秒")
        print(f"平均每批: {total_elapsed/len(batches):.2f}秒")
        print(f"文件已保存至: {OUTPUT_FILE}")
        print(f"\n注意: 解析字段为空,如需添加解析请手动处理或运行解析提取脚本")
    else:
        print("\n转换失败：没有提取到任何题目")


if __name__ == "__main__":
    main()