import os
import json
import re
from openai import OpenAI
from concurrent.futures import ThreadPoolExecutor, as_completed
import time
from typing import List, Dict, Tuple, Optional


class MDToJSONConverter:
    """Markdown试题转JSON转换器"""

    def __init__(self, api_key: str, base_url: str = "https://api.deepseek.com"):
        """
        初始化转换器

        Args:
            api_key: DeepSeek API密钥
            base_url: API基础URL
        """
        self.api_key = api_key
        self.base_url = base_url
        self.client = OpenAI(api_key=api_key, base_url=base_url)

    def read_file(self, file_path: str) -> str:
        """
        读取Markdown文件

        Args:
            file_path: 文件路径

        Returns:
            文件内容字符串
        """
        if not os.path.exists(file_path):
            raise FileNotFoundError(f"文件 {file_path} 未找到")
        with open(file_path, 'r', encoding='utf-8') as f:
            return f.read()

    def split_questions_and_answers(self, content: str) -> Tuple[str, str]:
        """
        将文档分为题目部分和解析部分

        Args:
            content: 完整的文档内容

        Returns:
            (题目部分, 解析部分)
        """
        # 查找解析部分的开始位置
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

    def split_questions(self, content: str, batch_size: int = 10) -> List[str]:
        """
        将题目部分按题号分割成批次

        Args:
            content: 题目部分内容
            batch_size: 每批次题目数量

        Returns:
            批次列表
        """
        # 按题号分割
        question_pattern = r'\n(?=\d+\.)'
        questions = re.split(question_pattern, content)

        # 过滤掉空段落
        questions = [q.strip() for q in questions if q.strip()]

        # 分批
        batches = []
        for i in range(0, len(questions), batch_size):
            batch = questions[i:i+batch_size]
            batches.append('\n\n'.join(batch))

        return batches

    def split_analysis_by_question(self, content: str) -> Dict[str, str]:
        """
        将解析按题号分割

        Args:
            content: 解析部分内容

        Returns:
            {题号: 解析内容} 的字典
        """
        # 查找所有题号的解析
        pattern = r'\n(\d+)\.解答'
        matches = list(re.finditer(pattern, content))

        if not matches:
            # 尝试其他模式
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

    def clean_json(self, response_text: str) -> Tuple[Optional[Dict], Optional[str]]:
        """
        清洗和解析JSON响应

        Args:
            response_text: API返回的原始文本

        Returns:
            (解析后的JSON对象, 错误信息)
        """
        # 去除 Markdown 代码块标记
        pattern = r"```(?:json)?\s*(\{.*?\})\s*```"
        match = re.search(pattern, response_text, re.DOTALL)

        if match:
            json_str = match.group(1)
        else:
            # 尝试暴力查找首尾大括号
            start = response_text.find('{')
            end = response_text.rfind('}')
            if start != -1 and end != -1:
                json_str = response_text[start:end + 1]
            else:
                return None, "未找到有效的JSON片段"

        # 尝试直接解析
        try:
            return json.loads(json_str), None
        except json.JSONDecodeError as e:
            # LaTeX修复
            try:
                fixed_str = json_str.replace('\\\\', '___DOUBLE_BACKSLASH___')
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

                return json.loads(fixed_str), None
            except Exception as e2:
                # 进一步修复
                try:
                    fixed_str = json_str
                    fixed_str = re.sub(r',id\)?"?$', '"', fixed_str, flags=re.MULTILINE)
                    fixed_str = re.sub(r'\(id:[^"]*$', '', fixed_str, flags=re.MULTILINE)
                    fixed_str = re.sub(r',\s*$', '', fixed_str, flags=re.MULTILINE)
                    fixed_str = re.sub(r'[^"]\([^"]*$', '', fixed_str, flags=re.MULTILINE)
                    fixed_str = re.sub(r'"analysis":\s*"[^"]*$', '"analysis": ""', fixed_str, flags=re.MULTILINE)

                    # LaTeX修复
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

                    # 智能闭合
                    if not fixed_str.rstrip().endswith('}'):
                        lines = fixed_str.rstrip().split('\n')
                        last_line = lines[-1]
                        if last_line.count('"') % 2 == 1:
                            last_line += '"'
                        if not last_line.rstrip().endswith('}') and not last_line.rstrip().endswith(']'):
                            last_line += '\n  }'
                        lines[-1] = last_line
                        fixed_str = '\n'.join(lines)

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

    def call_deepseek_api(self, system_prompt: str, user_prompt: str,
                          max_tokens: int = 8192, temperature: float = 0.1) -> Optional[str]:
        """
        调用DeepSeek API

        Args:
            system_prompt: 系统提示词
            user_prompt: 用户提示词
            max_tokens: 最大token数
            temperature: 温度参数

        Returns:
            API响应内容,失败返回None
        """
        try:
            response = self.client.chat.completions.create(
                model="deepseek-chat",
                messages=[
                    {"role": "system", "content": system_prompt},
                    {"role": "user", "content": user_prompt}
                ],
                temperature=temperature,
                max_tokens=max_tokens
            )
            return response.choices[0].message.content
        except Exception as e:
            print(f"API请求失败: {e}")
            return None

    def extract_questions_batch(self, batch_content: str, batch_index: int) -> Tuple[int, Optional[List], Optional[str]]:
        """
        提取一个批次的题目

        Args:
            batch_content: 批次内容
            batch_index: 批次索引

        Returns:
            (批次索引, 题目列表, 错误信息)
        """
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
                        {batch_content}
                        ---文档结束---
                        """

        print(f"[线程 {batch_index}] 正在处理第 {batch_index} 批...")
        start_time = time.time()

        result_text = self.call_deepseek_api(system_prompt, user_prompt, max_tokens=8192)

        if result_text:
            elapsed = time.time() - start_time
            print(f"[线程 {batch_index}] 请求完成，耗时: {elapsed:.2f}秒")

            # 解析JSON
            data, error = self.clean_json(result_text)

            if data and 'questions' in data:
                questions = data['questions']
                print(f"[线程 {batch_index}] 第 {batch_index} 批成功提取 {len(questions)} 道题")
                return batch_index, questions, None
            else:
                print(f"[线程 {batch_index}] 第 {batch_index} 批解析失败: {error}")
                # 调试输出
                with open(f"debug_batch_{batch_index}.txt", "w", encoding="utf-8") as f:
                    f.write(result_text)
                print(f"[线程 {batch_index}] 已将原始返回内容写入 debug_batch_{batch_index}.txt 以便排查。")
                return batch_index, None, error
        else:
            print(f"[线程 {batch_index}] 第 {batch_index} 批API调用失败")
            return batch_index, None, "API调用失败"

    def extract_questions(self, questions_part: str, batch_size: int = 10,
                         max_workers: int = 3) -> List[Dict]:
        """
        提取所有题目

        Args:
            questions_part: 题目部分内容
            batch_size: 每批次题目数量
            max_workers: 并发线程数

        Returns:
            题目列表
        """
        # 分批
        batches = self.split_questions(questions_part, batch_size)
        print(f"题目将分成 {len(batches)} 个批次处理")

        all_questions = []
        total_start_time = time.time()

        # 并发处理
        batch_info_list = [(i, batch) for i, batch in enumerate(batches, 1)]

        with ThreadPoolExecutor(max_workers=max_workers) as executor:
            future_to_batch = {
                executor.submit(self.extract_questions_batch, batch, idx): idx
                for idx, batch in batch_info_list
            }

            for future in as_completed(future_to_batch):
                batch_index = future_to_batch[future]
                try:
                    index, questions, error = future.result()
                    if questions is not None:
                        all_questions.extend(questions)
                except Exception as exc:
                    print(f"第 {batch_index} 批处理异常: {exc}")

        total_elapsed = time.time() - total_start_time
        print(f"题目提取完成，总耗时: {total_elapsed:.2f}秒，平均每批: {total_elapsed/len(batches):.2f}秒")

        # 按题号排序
        all_questions.sort(key=lambda x: x.get('questionNumber', 0))

        return all_questions

    def extract_analysis_single(self, question_num: str, analysis_text: str) -> Dict:
        """
        提取单个题目的解析

        Args:
            question_num: 题号
            analysis_text: 解析文本

        Returns:
            包含questionNumber和analysis的字典
        """
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

        user_prompt = f"""
提取以下题目的解析:

---解析内容---
{analysis_text}
---解析结束---

请返回JSON格式: {{"questionNumber": {question_num}, "analysis": "解析内容"}}
"""

        print(f"正在处理第 {question_num} 题的解析...")
        start_time = time.time()

        result_text = self.call_deepseek_api(system_prompt, user_prompt, max_tokens=1000)

        if result_text:
            elapsed = time.time() - start_time
            print(f"第 {question_num} 题解析完成,耗时: {elapsed:.2f}秒")

            # 解析JSON
            try:
                result = result_text.strip()
                if result.startswith('```'):
                    result = re.sub(r'^```(?:json)?\s*', '', result)
                    result = re.sub(r'\s*```$', '', result)

                analysis_json = json.loads(result)

                # 避免请求过快
                time.sleep(0.5)
                return analysis_json

            except json.JSONDecodeError as e:
                print(f"第 {question_num} 题解析JSON解析失败: {e}")
                # 手动构建
                return {
                    "questionNumber": int(question_num),
                    "analysis": analysis_text[:500]
                }
        else:
            print(f"第 {question_num} 题API调用失败")
            return {
                "questionNumber": int(question_num),
                "analysis": analysis_text[:500]
            }

    def extract_analysis(self, analysis_part: str) -> List[Dict]:
        """
        提取所有解析

        Args:
            analysis_part: 解析部分内容

        Returns:
            解析列表
        """
        # 按题号分割
        analysis_dict = self.split_analysis_by_question(analysis_part)
        print(f"共找到 {len(analysis_dict)} 道题的解析")

        all_analysis = []
        total_start_time = time.time()

        # 按题号排序
        sorted_questions = sorted(analysis_dict.keys(), key=int)

        for question_num in sorted_questions:
            analysis_text = analysis_dict[question_num]
            result = self.extract_analysis_single(question_num, analysis_text)
            all_analysis.append(result)

        total_elapsed = time.time() - total_start_time
        print(f"解析提取完成，总耗时: {total_elapsed:.2f}秒")

        return all_analysis

    def merge_questions_and_analysis(self, questions: List[Dict],
                                   analysis_list: List[Dict]) -> List[Dict]:
        """
        合并题目和解析

        Args:
            questions: 题目列表
            analysis_list: 解析列表

        Returns:
            合并后的题目列表
        """
        # 构建题号到解析的映射
        analysis_map = {item['questionNumber']: item['analysis'] for item in analysis_list}

        # 合并
        for question in questions:
            question_num = question.get('questionNumber')
            if question_num in analysis_map:
                question['analysis'] = analysis_map[question_num]

        return questions

    def save_json(self, data: Dict, output_file: str):
        """
        保存JSON文件

        Args:
            data: 要保存的数据
            output_file: 输出文件路径
        """
        with open(output_file, 'w', encoding='utf-8') as f:
            json.dump(data, f, ensure_ascii=False, indent=2)
        print(f"文件已保存至: {output_file}")

    def convert_full_document(self, input_file: str, output_file: str,
                            include_analysis: bool = False) -> Dict:
        """
        转换完整文档

        Args:
            input_file: 输入Markdown文件
            output_file: 输出JSON文件
            include_analysis: 是否包含解析

        Returns:
            转换后的JSON数据
        """
        # 读取文件
        print(f"正在读取文件: {input_file}")
        content = self.read_file(input_file)
        print(f"成功读取文件，字符数: {len(content)}")

        # 分离题目和解析
        print("\n[第一步] 分离题目部分和解析部分...")
        questions_part, analysis_part = self.split_questions_and_answers(content)
        print(f"题目部分字符数: {len(questions_part)}")
        print(f"解析部分字符数: {len(analysis_part)}")

        # 提取题目
        print(f"\n[第二步] 提取题目信息...")
        questions = self.extract_questions(questions_part, batch_size=10, max_workers=3)
        print(f"成功提取 {len(questions)} 道题")

        # 提取解析(可选)
        if include_analysis:
            print(f"\n[第三步] 提取解析信息...")
            analysis_list = self.extract_analysis(analysis_part)

            print(f"\n[第四步] 合并题目和解析...")
            questions = self.merge_questions_and_analysis(questions, analysis_list)
            print(f"成功合并 {len(analysis_list)} 道题的解析")
        else:
            print(f"\n[第三步] 跳过解析提取")

        # 保存结果
        final_data = {"questions": questions}
        self.save_json(final_data, output_file)

        print(f"\n{'='*50}")
        print(f"转换成功！共处理 {len(questions)} 道题")
        if include_analysis:
            print(f"包含解析: 是")
        else:
            print(f"包含解析: 否")

        return final_data


# 使用示例
if __name__ == "__main__":
    # 配置
    API_KEY = "sk-f07aded12d264640bb3ff865ac211bc3"
    INPUT_FILE = "2009年计算机统考真题及解析.md"
    OUTPUT_FILE = "2009年计算机统考真题及解析_complete.json"

    # 创建转换器
    converter = MDToJSONConverter(api_key=API_KEY)

    # 转换文档(不包含解析)
    print("=" * 50)
    print("开始转换(不包含解析)...")
    print("=" * 50)
    converter.convert_full_document(INPUT_FILE, OUTPUT_FILE, include_analysis=False)

    # 转换文档(包含解析)
    OUTPUT_FILE_WITH_ANALYSIS = "2009年计算机统考真题及解析_with_analysis.json"
    print("\n" + "=" * 50)
    print("开始转换(包含解析)...")
    print("=" * 50)
    converter.convert_full_document(INPUT_FILE, OUTPUT_FILE_WITH_ANALYSIS, include_analysis=True)
