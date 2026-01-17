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
OUTPUT_FILE = "analysis.json"


def read_md_file(file_path):
    if not os.path.exists(file_path):
        raise FileNotFoundError(f"文件 {file_path} 未找到")
    with open(file_path, 'r', encoding='utf-8') as f:
        return f.read()


def extract_analysis_part(content):
    """
    提取解析部分
    """
    # 查找解析部分的开始位置
    patterns = [
        r'# 计算机专业基础综合考试真题思路分析',
        r'#[\s]*解答',
        r'#[\s]*答案解析',
    ]

    split_pos = len(content)
    for pattern in patterns:
        match = re.search(pattern, content)
        if match:
            split_pos = min(split_pos, match.start())

    return content[split_pos:]


def split_analysis_by_question(content):
    """
    将解析按题号分割
    """
    # 查找所有题号的解析 (例如 "41.解答", "42.解答" 等)
    pattern = r'\n(\d+)\.解答'
    matches = list(re.finditer(pattern, content))

    if not matches:
        # 如果没有找到 "题号.解答" 的格式,尝试其他模式
        pattern = r'\n(\d+)\.'
        matches = list(re.finditer(pattern, content))

    analysis_dict = {}

    for i, match in enumerate(matches):
        question_num = match.group(1)
        start_pos = match.start()

        # 确定结束位置
        if i + 1 < len(matches):
            end_pos = matches[i + 1].start()
        else:
            end_pos = len(content)

        # 提取该题的解析内容
        analysis_text = content[start_pos:end_pos].strip()
        analysis_dict[question_num] = analysis_text

    return analysis_dict


def convert_analysis_with_deepseek(analysis_data):
    """
    使用DeepSeek提取解析的JSON
    """
    client = OpenAI(api_key=API_KEY, base_url=BASE_URL)

    system_prompt = """
你是一个专业的试题解析提取助手。
任务：将题目解析转换为简化的JSON格式。

**输入格式：**
题目编号 + 解析内容

**输出格式：**
返回一个JSON对象,格式如下：
{
  "questionNumber": 题号,
  "analysis": "解析内容(最多500字符,超出则截取关键部分)"
}

**关键指令：**
1. 只提取解析的核心内容,去除冗余信息
2. 如果解析过长,只保留前500字符
3. 不要包含题目本身,只保留解析
4. 输出必须是合法的JSON格式
5. 不要输出 ```json 头和结尾
"""

    all_analysis = []

    # 按题号排序
    sorted_questions = sorted(analysis_data.keys(), key=int)

    for question_num in sorted_questions:
        analysis_text = analysis_data[question_num]

        user_prompt = f"""
提取以下题目的解析:

---解析内容---
{analysis_text}
---解析结束---

请返回JSON格式: {{"questionNumber": {question_num}, "analysis": "解析内容"}}
"""

        print(f"正在处理第 {question_num} 题的解析...")
        start_time = time.time()

        try:
            response = client.chat.completions.create(
                model="deepseek-chat",
                messages=[
                    {"role": "system", "content": system_prompt},
                    {"role": "user", "content": user_prompt}
                ],
                temperature=0.1,
                max_tokens=1000
            )

            result = response.choices[0].message.content
            elapsed = time.time() - start_time
            print(f"第 {question_num} 题解析完成,耗时: {elapsed:.2f}秒")

            # 解析JSON
            try:
                # 清洗可能的markdown标记
                result = result.strip()
                if result.startswith('```'):
                    result = re.sub(r'^```(?:json)?\s*', '', result)
                    result = re.sub(r'\s*```$', '', result)

                analysis_json = json.loads(result)
                all_analysis.append(analysis_json)

            except json.JSONDecodeError as e:
                print(f"第 {question_num} 题解析JSON解析失败: {e}")
                # 手动构建
                all_analysis.append({
                    "questionNumber": int(question_num),
                    "analysis": analysis_text[:500]  # 直接截取前500字符
                })

            # 避免请求过快
            time.sleep(0.5)

        except Exception as e:
            print(f"第 {question_num} 题API调用失败: {e}")
            # 使用原始文本
            all_analysis.append({
                "questionNumber": int(question_num),
                "analysis": analysis_text[:500]
            })

    return all_analysis


def main():
    try:
        full_content = read_md_file(INPUT_FILE)
        print(f"成功读取文件，字符数: {len(full_content)}")
    except Exception as e:
        print(e)
        return

    # 提取解析部分
    print("\n[第一步] 提取解析部分...")
    analysis_part = extract_analysis_part(full_content)
    print(f"解析部分字符数: {len(analysis_part)}")

    # 按题号分割解析
    print(f"\n[第二步] 按题号分割解析...")
    analysis_dict = split_analysis_by_question(analysis_part)
    print(f"共找到 {len(analysis_dict)} 道题的解析")

    # 使用DeepSeek提取JSON
    print(f"\n[第三步] 使用DeepSeek提取解析JSON...")
    all_analysis = convert_analysis_with_deepseek(analysis_dict)

    # 保存结果
    with open(OUTPUT_FILE, 'w', encoding='utf-8') as f:
        json.dump(all_analysis, f, ensure_ascii=False, indent=2)

    print(f"\n{'='*50}")
    print(f"解析提取完成！共处理 {len(all_analysis)} 道题的解析。")
    print(f"文件已保存至: {OUTPUT_FILE}")


if __name__ == "__main__":
    main()
