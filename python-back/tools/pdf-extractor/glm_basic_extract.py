"""
GLM基础版题目提取工具 - 用于测试效果
简化版本，专注于核心功能
"""

import os
import json
import re
from pathlib import Path
from zhipuai import ZhipuAI


def extract_images_from_md(md_content, md_file_path):
    """从Markdown中提取图片并转换为标记"""
    images = []
    md_dir = os.path.dirname(md_file_path)

    # 匹配 ![](images/xxx.jpg) 格式
    pattern = r'!\[\]\(images/([^\)]+)\)'

    def replace_image(match):
        image_filename = match.group(1)
        image_path = os.path.join(md_dir, "images", image_filename)

        if os.path.exists(image_path):
            image_id = f"img_{len(images)}"
            images.append({
                "id": image_id,
                "filename": image_filename
            })
            return f"[图片:{image_id}]"
        return match.group(0)

    cleaned_content = re.sub(pattern, replace_image, md_content)
    return cleaned_content, images


def build_simple_prompt(md_content):
    """构建简化的提示词"""

    prompt = f"""请将以下考试题目转换为JSON格式。

**题目内容：**
{md_content}

**JSON格式要求：**
```json
{{
  "questions": [
    {{
      "questionNumber": 1,
      "type": 1,
      "content": "题干内容",
      "options": ["A. 选项1", "B. 选项2", "C. 选项3", "D. 选项4"],
      "answer": "A",
      "analysis": "解析内容",
      "tags": ["数据结构", "栈"]
    }}
  ]
}}
```

**字段说明：**
- questionNumber: 题号（数字，如1、2、3...），用于试卷导入时的排序
- type: 题目类型（1=单选题, 2=多选题, 3=填空题, 4=简答题/综合应用题）
- content: 题目内容（题干），注意是content不是question
- options: 选项数组（选择题必填，其他题型填空数组[]）
- answer: 答案（单选填字母如"A"，多选填"A,B,C"，填空题填内容，简答题填要点）
- analysis: 解析（如果没有解析填空字符串""）
- tags: 标签数组（如["数据结构", "栈"]，如果没有相关标签填空数组[]）

**重要：**
1. 只输出JSON，不要其他文字
2. 不要使用markdown代码块标记（```json 或 ```）
3. 图片标记 ![](images/xxx.jpg) 必须原样保留在content和analysis中
4. 如果某道题没有选项，options填空数组 []
5. 如果没有标签，tags填空数组 []
6. 字段名必须是: questionNumber, type, content, options, answer, analysis, tags
7. 不要使用question字段

请直接输出JSON："""

    return prompt


def parse_with_llm(md_file_path, api_key):
    """使用GLM解析MD文件"""

    print(f"\n{'='*60}")
    print(f"处理文件: {os.path.basename(md_file_path)}")
    print(f"{'='*60}")

    # 1. 读取文件
    with open(md_file_path, 'r', encoding='utf-8') as f:
        md_content = f.read()

    print(f"✓ 读取文件: {len(md_content)} 字符")

    # 2. 截取前面部分（避免token超限）
    lines = md_content.split('\n')
    selected_lines = []
    char_count = 0
    max_chars = 10000

    for line in lines:
        # 停止条件：遇到综合应用题
        if '综合应用题' in line or '二、' in line:
            print(f"✓ 检测到综合应用题标记，停止")
            break

        selected_lines.append(line)
        char_count += len(line)

        if char_count > max_chars:
            print(f"⚠ 达到字符限制 ({max_chars})")
            break

    cleaned_md = '\n'.join(selected_lines)
    print(f"✓ 截取内容: {char_count} 字符")

    # 3. 处理图片
    cleaned_md, images = extract_images_from_md(cleaned_md, md_file_path)
    if images:
        print(f"✓ 发现图片: {len(images)} 张")

    # 4. 构建提示词
    prompt = build_simple_prompt(cleaned_md)

    # 5. 调用GLM
    print(f"🤖 调用GLM-4...")

    client = ZhipuAI(api_key=api_key)

    try:
        response = client.chat.completions.create(
            model="glm-4-flash",
            messages=[
                {"role": "system", "content": "你是题目解析助手，只输出标准JSON，无其他文字。"},
                {"role": "user", "content": prompt}
            ],
            temperature=0.1,
            max_tokens=8000
        )

        result_text = response.choices[0].message.content.strip()
        print(f"✓ GLM返回: {len(result_text)} 字符")

        # 6. 清理响应
        # 移除可能的代码块标记
        if result_text.startswith("```json"):
            result_text = result_text[7:]
        if result_text.startswith("```"):
            result_text = result_text[3:]
        if result_text.endswith("```"):
            result_text = result_text[:-3]

        result_text = result_text.strip()

        # 保存原始响应用于调试
        with open("debug_raw_response.txt", 'w', encoding='utf-8') as f:
            f.write(result_text)
        print(f"✓ 保存原始响应: debug_raw_response.txt")

        # 7. 解析JSON
        try:
            result = json.loads(result_text)

            # 处理不同格式
            if isinstance(result, list):
                result = {"questions": result}
            elif not isinstance(result, dict):
                raise ValueError(f"无法识别的格式: {type(result)}")

            print(f"✓ JSON解析成功: {len(result.get('questions', []))} 道题")

            # 8. 添加图片信息
            if images:
                result["images"] = images

            return result

        except json.JSONDecodeError as e:
            print(f"✗ JSON解析失败: {e}")
            print(f"  错误位置: 行{e.lineno}, 列{e.colno}")

            # 尝试清理后重新解析
            try:
                cleaned = re.sub(r'[\x00-\x1f\x7f-\x9f]', '', result_text)
                result = json.loads(cleaned)

                if isinstance(result, list):
                    result = {"questions": result}

                print("✓ 清理后解析成功")
                return result

            except Exception as e2:
                print(f"✗ 清理后仍失败: {e2}")
                return None

    except Exception as e:
        print(f"✗ GLM调用失败: {e}")
        import traceback
        traceback.print_exc()
        return None


def save_json(result, output_file):
    """保存JSON文件"""
    output_path = Path(output_file)
    output_path.parent.mkdir(parents=True, exist_ok=True)

    with open(output_file, 'w', encoding='utf-8') as f:
        json.dump(result, f, ensure_ascii=False, indent=2)

    print(f"✓ 保存文件: {output_file}")
    print(f"  - 题目数: {len(result.get('questions', []))}")
    print(f"  - 图片数: {len(result.get('images', []))}")


def main():
    """主函数"""
    # 配置
    API_KEY = "e90579b6f4df4a8ab4b9381dfa1b7466.cgxYg4MwekKDlZHO"

    # 测试单个文件
    INPUT_FILE = r"F:\Coding\JavaProject\KaoYanPlatform\data\2009年计算机统考真题及解析.md"
    OUTPUT_FILE = r"F:\Coding\JavaProject\KaoYanPlatform\data\test_output.json"

    # 解析
    result = parse_with_llm(INPUT_FILE, API_KEY)

    if result:
        # 保存
        save_json(result, OUTPUT_FILE)

        # 显示第一道题作为示例
        questions = result.get('questions', [])
        if questions:
            print(f"\n{'='*60}")
            print("示例 - 第1道题:")
            print(f"{'='*60}")
            print(json.dumps(questions[0], ensure_ascii=False, indent=2))
            print(f"{'='*60}")

        print("\n✅ 处理完成!")
    else:
        print("\n❌ 处理失败")


if __name__ == "__main__":
    main()
