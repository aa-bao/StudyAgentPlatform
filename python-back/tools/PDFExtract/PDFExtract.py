import os
import json
import time
from pathlib import Path
from loguru import logger

from mineru.cli.common import convert_pdf_bytes_to_bytes_by_pypdfium2, prepare_env, read_fn
from mineru.data.data_reader_writer import FileBasedDataWriter
from mineru.utils.draw_bbox import draw_layout_bbox, draw_span_bbox
from mineru.utils.enum_class import MakeMode
from mineru.backend.pipeline.pipeline_analyze import doc_analyze as pipeline_doc_analyze
from mineru.backend.pipeline.pipeline_middle_json_mkcontent import union_make as pipeline_union_make
from mineru.backend.pipeline.model_json_to_middle_json import result_to_middle_json as pipeline_result_to_middle_json


def detect_pdf_type(pdf_path: str, text_threshold: int = 100) -> dict:
    """
    快速检测 PDF 类型

    Args:
        pdf_path: PDF 文件路径
        text_threshold: 文本量阈值,少于这个字数认为是扫描版

    Returns:
        dict: {
            "has_text": bool,        # 是否有文字层
            "text_ratio": float,     # 文本占比
            "is_scanned": bool,      # 是否为扫描版
            "recommended_method": str # 推荐的解析方法
        }
    """
    import fitz  # PyMuPDF

    print("正在检测 PDF 类型...")
    start_time = time.time()

    try:
        doc = fitz.open(pdf_path)
        total_text = ""
        check_pages = min(5, len(doc))  # 只检查前5页

        for page_num in range(check_pages):
            page = doc[page_num]
            text = page.get_text()
            total_text += text

        doc.close()

        # 统计文本
        text_length = len(total_text.strip())
        avg_text_per_page = text_length / check_pages if check_pages > 0 else 0

        # 判断
        has_text = text_length > text_threshold
        is_scanned = avg_text_per_page < 50  # 平均每页少于50字认为是扫描版

        # 推荐方法
        if is_scanned:
            recommended_method = "ocr"
            reason = f"检测到扫描版 PDF (平均每页 {avg_text_per_page:.0f} 字)"
        else:
            recommended_method = "txt"
            reason = f"检测到文字版 PDF (平均每页 {avg_text_per_page:.0f} 字)"

        result = {
            "has_text": has_text,
            "text_length": text_length,
            "avg_text_per_page": avg_text_per_page,
            "is_scanned": is_scanned,
            "recommended_method": recommended_method,
            "reason": reason
        }

        detect_time = time.time() - start_time
        print(f"✓ 检测完成: {reason} (耗时 {detect_time:.2f}秒)")
        return result

    except Exception as e:
        print(f"⚠ 检测失败: {e}, 默认使用 auto 模式")
        return {
            "has_text": False,
            "is_scanned": True,
            "recommended_method": "auto",
            "reason": "检测失败,使用自动模式"
        }


def extract_pdf_with_mineru(
    pdf_path: str,
    output_dir: str = "./output",
    lang: str = "ch",
    parse_method: str = "auto",
    formula_enable: bool = True,
    table_enable: bool = True,
    start_page_id: int = 0,
    end_page_id: int = None,
) -> dict:
    """
    使用 MinerU 提取 PDF 为 Markdown

    参数说明:
    -----------
    pdf_path : str
        PDF 文件路径
    output_dir : str
        输出目录
    lang : str
        语言选项，默认 'ch' (中文)
        可选: 'ch', 'ch_server', 'ch_lite', 'en', 'korean', 'japan' 等
    parse_method : str
        解析方法，默认 'auto'
        - 'auto': 自动判断
        - 'txt': 文本提取
        - 'ocr': OCR 识别
    formula_enable : bool
        是否启用公式识别，默认 True
    table_enable : bool
        是否启用表格识别，默认 True
    start_page_id : int
        起始页码（从 0 开始），默认 0
    end_page_id : int
        结束页码，默认 None（解析到文档末尾）

    返回:
    -------
    dict : 包含提取结果信息的字典
    """

    pdf_path_obj = Path(pdf_path)

    if not pdf_path_obj.exists():
        raise FileNotFoundError(f"PDF 文件不存在: {pdf_path}")

    print("=" * 60)
    print(f"开始使用 MinerU 提取 PDF: {pdf_path_obj.name}")
    print("=" * 60)

    total_start_time = time.time()

    # 读取 PDF 文件
    pdf_bytes = read_fn(pdf_path)

    # 如果指定了页码范围，裁剪 PDF
    if start_page_id > 0 or end_page_id is not None:
        pdf_bytes = convert_pdf_bytes_to_bytes_by_pypdfium2(pdf_bytes, start_page_id, end_page_id)

    # 准备输出环境
    file_name = pdf_path_obj.stem
    local_image_dir, local_md_dir = prepare_env(output_dir, file_name, parse_method)

    # 初始化写入器
    image_writer = FileBasedDataWriter(local_image_dir)
    md_writer = FileBasedDataWriter(local_md_dir)

    # 使用 pipeline 后端解析
    print(f"正在解析 PDF (方法: {parse_method}, 语言: {lang})...")
    parse_start_time = time.time()

    try:
        # 执行解析
        infer_results, all_image_lists, all_pdf_docs, lang_list, ocr_enabled_list = pipeline_doc_analyze(
            [pdf_bytes],
            [lang],
            parse_method=parse_method,
            formula_enable=formula_enable,
            table_enable=table_enable
        )

        parse_time = time.time() - parse_start_time
        print(f"✓ 解析完成 (耗时 {parse_time:.2f}秒)")

        # 处理第一个（也是唯一一个）PDF 的结果
        model_list = infer_results[0]
        images_list = all_image_lists[0]
        pdf_doc = all_pdf_docs[0]
        _lang = lang_list[0]
        _ocr_enable = ocr_enabled_list[0]

        # 转换为中间 JSON
        print("正在处理解析结果...")
        process_start_time = time.time()

        middle_json = pipeline_result_to_middle_json(
            model_list,
            images_list,
            pdf_doc,
            image_writer,
            _lang,
            _ocr_enable,
            formula_enable
        )

        pdf_info = middle_json["pdf_info"]

        # 生成 Markdown
        print("正在生成 Markdown...")
        image_dir = str(os.path.basename(local_image_dir))
        md_content = pipeline_union_make(pdf_info, MakeMode.MM_MD, image_dir)

        process_time = time.time() - process_start_time
        print(f"✓ 处理完成 (耗时 {process_time:.2f}秒)")

        # 保存 Markdown
        md_filename = f"{file_name}.md"
        md_writer.write_string(md_filename, md_content)
        md_path = os.path.join(local_md_dir, md_filename)
        print(f"✓ Markdown 已保存: {md_path}")

        # 可选: 保存 content_list JSON（如需后续处理可取消注释）
        # content_list = pipeline_union_make(pdf_info, MakeMode.CONTENT_LIST, image_dir)
        # content_list_filename = f"{file_name}_content_list.json"
        # md_writer.write_string(
        #     content_list_filename,
        #     json.dumps(content_list, ensure_ascii=False, indent=2)
        # )
        # print(f"✓ Content List 已保存")

        # 可选: 保存 middle JSON（完整结构化数据，如需可取消注释）
        # middle_json_filename = f"{file_name}_middle.json"
        # md_writer.write_string(
        #     middle_json_filename,
        #     json.dumps(middle_json, ensure_ascii=False, indent=2)
        # )
        # print(f"✓ Middle JSON 已保存")

        # 不生成可视化 PDF 文件（layout.pdf, span.pdf），这些很慢

        # 图片已自动保存到 local_image_dir

        total_time = time.time() - total_start_time

        print("\n" + "=" * 60)
        print("✓ 提取完成!")
        print(f"总字符数: {len(md_content)}")
        print(f"输出目录: {local_md_dir}")
        print(f"总耗时: {total_time:.2f}秒 ({total_time/60:.1f}分钟)")
        print("=" * 60)

        return {
            "success": True,
            "file_name": file_name,
            "md_path": md_path,
            "output_dir": local_md_dir,
            "total_chars": len(md_content),
            "images_dir": local_image_dir,
        }

    except Exception as e:
        print(f"❌ 提取失败: {e}")
        import traceback
        traceback.print_exc()
        return {
            "success": False,
            "error": str(e),
            "file_name": file_name
        }


if __name__ == "__main__":
    # 配置
    PDF_PATH = "./PDFData/2014年计算机统考真题及解析.pdf"
    OUTPUT_DIR = "./output_mineru"

    # 设置模型源
    os.environ["MINERU_MODEL_SOURCE"] = "local"  # 本地模型
    # 如果没有本地模型,可以用 modelscope (国内) 或 huggingface
    # os.environ["MINERU_MODEL_SOURCE"] = "modelscope"

    overall_start_time = time.time()

    print("=" * 60)
    print("PDF 提取工具 (MinerU) - 优化版")
    print("=" * 60)

    # 步骤 1: 快速检测 PDF 类型
    pdf_info = detect_pdf_type(PDF_PATH)

    print("\n" + "-" * 60)
    print("PDF 信息:")
    print(f"  文字量: {pdf_info['text_length']} 字")
    print(f"  平均每页: {pdf_info['avg_text_per_page']:.0f} 字")
    print(f"  类型: {'扫描版' if pdf_info['is_scanned'] else '文字版'}")
    print("-" * 60)

    # 步骤 2: 根据类型选择最优配置
    if pdf_info['is_scanned']:
        # 扫描版 PDF - 需要 OCR
        print("\n使用 OCR 模式 (较慢,但准确)")
        parse_method = "ocr"
        lang = "ch"  # 完整中文模型
        print("  - 使用完整 OCR 模型以保证精度")
    else:
        # 文字版 PDF - 直接提取
        print("\n使用文本提取模式 (快速)")
        parse_method = "txt"
        lang = "ch"
        print("  - 直接提取文字,无需 OCR")

    # 步骤 3: 执行提取
    print("\n开始提取...")
    result = extract_pdf_with_mineru(
        pdf_path=PDF_PATH,
        output_dir=OUTPUT_DIR,
        lang=lang,
        parse_method=parse_method,
        formula_enable=True,   # 保留公式识别(精度优先)
        table_enable=True,     # 保留表格识别(精度优先)
        # start_page_id=0,     # 从第一页开始
        # end_page_id=3        # 测试用: 只处理前3页
    )

    overall_time = time.time() - overall_start_time

    if result["success"]:
        print(f"\n✓ 成功! 结果保存在: {result['output_dir']}")
        print(f"  Markdown: {result['md_path']}")
        print(f"  图片目录: {result['images_dir']}")
        print(f"\n全程总耗时: {overall_time:.2f}秒 ({overall_time/60:.1f}分钟)")
    else:
        print(f"\n❌ 失败: {result.get('error', '未知错误')}")
        print(f"\n总耗时: {overall_time:.2f}秒")
