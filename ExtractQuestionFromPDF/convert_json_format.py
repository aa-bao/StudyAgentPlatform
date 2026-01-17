"""
JSON格式转换工具
将GLM输出的JSON格式转换为后端期望的QuestionImportDTO格式
"""

import json
from pathlib import Path


def convert_to_backend_format(input_file, output_file):
    """
    转换JSON格式

    输入格式:
    {
      "questions": [
        {
          "questionNumber": 1,
          "question": "题干",
          "type": 1,
          "options": [...],
          "answer": "A",
          "analysis": "解析",
          "tags": [...]
        }
      ]
    }

    输出格式:
    {
      "questions": [
        {
          "type": 1,
          "content": "题干",
          "options": [...],
          "answer": "A",
          "analysis": "解析",
          "tags": [...]
        }
      ]
    }
    """

    # 读取输入文件
    with open(input_file, 'r', encoding='utf-8') as f:
        data = json.load(f)

    print(f"读取文件: {input_file}")
    print(f"原始题目数: {len(data.get('questions', []))}")

    # 转换格式
    converted_questions = []

    for q in data.get('questions', []):
        converted_q = {
            "type": q.get("type", 1),
            "content": q.get("question", ""),  # question -> content
            "options": q.get("options", []),
            "answer": q.get("answer", ""),
            "analysis": q.get("analysis", ""),
            "tags": q.get("tags", [])
        }
        converted_questions.append(converted_q)

    # 构建输出数据
    output_data = {
        "questions": converted_questions
    }

    # 如果有images，保留
    if "images" in data:
        output_data["images"] = data["images"]

    # 保存输出文件
    output_path = Path(output_file)
    output_path.parent.mkdir(parents=True, exist_ok=True)

    with open(output_file, 'w', encoding='utf-8') as f:
        json.dump(output_data, f, ensure_ascii=False, indent=2)

    print(f"转换后题目数: {len(converted_questions)}")
    print(f"输出文件: {output_file}")

    # 显示第一道题示例
    if converted_questions:
        print(f"\n示例 - 第1道题:")
        print(json.dumps(converted_questions[0], ensure_ascii=False, indent=2))

    return output_data


def batch_convert(input_dir, output_dir):
    """批量转换目录中的所有JSON文件"""

    input_path = Path(input_dir)
    output_path = Path(output_dir)
    output_path.mkdir(parents=True, exist_ok=True)

    json_files = list(input_path.glob("*.json"))
    print(f"\n找到 {len(json_files)} 个JSON文件\n")

    for json_file in json_files:
        print(f"{'='*60}")
        output_file = output_path / f"{json_file.stem}_converted.json"

        try:
            convert_to_backend_format(str(json_file), str(output_file))
            print("✓ 转换成功")
        except Exception as e:
            print(f"✗ 转换失败: {e}")

    print(f"\n{'='*60}")
    print("批量转换完成")


def main():
    """主函数"""

    # 单文件转换
    INPUT_FILE = r"F:\Coding\JavaProject\KaoYanPlatform\ExtractQuestionFromPDF\2009年计算机统考真题及解析.json"
    OUTPUT_FILE = r"F:\Coding\JavaProject\KaoYanPlatform\data\converted_2009.json"

    print("="*60)
    print("JSON格式转换工具")
    print("="*60)

    # 单文件转换
    convert_to_backend_format(INPUT_FILE, OUTPUT_FILE)

    # 或者批量转换
    # batch_convert(
    #     input_dir=r"F:\Coding\JavaProject\KaoYanPlatform\ExtractQuestionFromPDF",
    #     output_dir=r"F:\Coding\JavaProject\KaoYanPlatform\data\converted"
    # )

    print("\n✅ 转换完成!")
    print(f"\n提示: 使用转换后的文件 {OUTPUT_FILE} 进行批量导入")


if __name__ == "__main__":
    main()
