"""
使用 MinerU 提取 PDF 为 Markdown
支持自动识别扫描版PDF并进行OCR
"""

import os
import json
from typing import List, Dict


def extract_pdf_with_mineru(pdf_path: str, output_dir: str = "./output") -> Dict:
    """
    使用 MinerU 提取 PDF 为 Markdown

    Args:
        pdf_path: PDF 文件路径
        output_dir: 输出目录

    Returns:
        提取结果信息
    """
    # 确保输出目录存在
    os.makedirs(output_dir, exist_ok=True)

    # 获取文件名（不含扩展名）
    file_name = os.path.basename(pdf_path).rsplit(".", 1)[0]

    print("=" * 60)
    print(f"开始使用 MinerU 提取 PDF: {file_name}")
    print("=" * 60)

    try:
        # 尝试最新的 MinerU API (magic-pdf 0.7.x+)
        from magic_pdf.pipe.UNIPipe import UNIPipe
        from magic_pdf.rw.DiskReaderWriter import DiskReaderWriter

        print("使用新版 MinerU API...")

        # 读取 PDF 文件
        with open(pdf_path, 'rb') as f:
            pdf_bytes = f.read()

        # 初始化 Reader 和 Writer
        image_writer = DiskReaderWriter(os.path.join(output_dir, "images"))
        image_dir = os.path.join(output_dir, "images")

        # 创建解析管道
        jso_useful_key = {
            "_pdf_type": "",
            "model_list": []
        }

        pipe = UNIPipe(pdf_bytes, jso_useful_key, {}, image_writer=image_writer)

        # 执行解析
        print("正在解析 PDF...")
        pipe.pipe_classify()  # 分类
        pipe.pipe_parse()      # 解析

        # 生成 Markdown
        print("正在生成 Markdown...")
        md_content = pipe.pipe_mk_markdown(file_name, drop_mode="none")

        # 保存 Markdown 文件
        md_path = os.path.join(output_dir, f"{file_name}.md")
        with open(md_path, 'w', encoding='utf-8') as f:
            f.write(md_content)

        print(f"✓ Markdown 文件已保存: {md_path}")

        # 保存 JSON 格式数据
        try:
            content_list = pipe.pipe_mk_uni_format(file_name, drop_mode="none")
            json_path = os.path.join(output_dir, f"{file_name}.json")
            with open(json_path, 'w', encoding='utf-8') as f:
                json.dump(content_list, f, ensure_ascii=False, indent=2)
            print(f"✓ JSON 文件已保存: {json_path}")
        except Exception as e:
            print(f"⚠ JSON 保存失败: {e}")

        # 统计信息
        total_chars = len(md_content)

        result = {
            "file_name": file_name,
            "pdf_path": pdf_path,
            "markdown_file": md_path,
            "total_characters": total_chars,
            "images_dir": image_dir if os.path.exists(image_dir) else None,
            "success": True
        }

        print("\n" + "=" * 60)
        print("✓ 提取完成!")
        print(f"总字符数: {total_chars}")
        print(f"Markdown: {md_path}")
        print("=" * 60)

        return result

    except ImportError as e:
        print(f"❌ 导入错误: {e}")
        print("\n请安装 MinerU:")
        print("   pip install magic-pdf[full]")
        print("\n或使用命令行工具:")
        print(f"   magic-pdf pdf -p {pdf_path} -o {output_dir}")
        raise

    except Exception as e:
        print(f"❌ 提取失败: {e}")
        print("\n尝试使用命令行工具:")
        print(f"   magic-pdf pdf -p {pdf_path} -o {output_dir}")
        raise


def extract_pdf_cli(pdf_path: str, output_dir: str = "./output") -> bool:
    """
    使用 MinerU 命令行工具提取 PDF

    Args:
        pdf_path: PDF 文件路径
        output_dir: 输出目录

    Returns:
        是否成功
    """
    import subprocess

    print("=" * 60)
    print("使用 MinerU 命令行工具提取 PDF...")
    print("=" * 60)

    try:
        # 使用 magic-pdf 命令
        cmd = [
            "magic-pdf",
            "pdf",
            "-p", pdf_path,
            "-o", output_dir
        ]

        print(f"执行命令: {' '.join(cmd)}")
        result = subprocess.run(cmd, capture_output=True, text=True, encoding='utf-8')

        if result.returncode == 0:
            print("\n" + result.stdout)
            print("\n✓ 提取完成!")
            return True
        else:
            print(f"\n❌ 错误: {result.stderr}")
            return False

    except FileNotFoundError:
        print("❌ 未找到 magic-pdf 命令")
        print("\n请安装 MinerU:")
        print("   pip install magic-pdf[full]")
        return False
    except Exception as e:
        print(f"❌ 执行失败: {e}")
        return False


if __name__ == "__main__":
    # 配置
    PDF_PATH = "PDFdata/2010年计算机统考真题及解析.pdf"
    OUTPUT_DIR = "output_mineru"

    print("=" * 60)
    print("PDF 提取工具 (MinerU)")
    print("=" * 60)
    print("\nMinerU 是一个强大的 PDF 解析工具,支持:")
    print("  ✓ 文字层 PDF 直接提取")
    print("  ✓ 扫描版 PDF 自动 OCR")
    print("  ✓ 保留图片、表格、公式")
    print("\n安装命令:")
    print("   pip install magic-pdf[full]")
    print("\n或使用命令行:")
    print("   magic-pdf pdf -p <pdf_path> -o <output_dir>")
    print("=" * 60)

    # 先尝试 Python API
    try:
        result = extract_pdf_with_mineru(PDF_PATH, OUTPUT_DIR)
    except Exception as e:
        print(f"\nPython API 失败: {e}")
        print("\n尝试使用命令行工具...")
        success = extract_pdf_cli(PDF_PATH, OUTPUT_DIR)

        if not success:
            print("\n❌ 所有方法都失败了")
            print("\n请检查:")
            print("  1. 是否正确安装了 magic-pdf")
            print("  2. PDF 文件路径是否正确")
            print("  3. 查看上方错误信息")
