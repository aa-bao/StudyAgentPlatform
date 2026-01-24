"""
PDF处理工具的FastAPI路由实现
"""
import os
import tempfile
from fastapi import APIRouter, UploadFile, File, Form
from pydantic import BaseModel
from typing import Optional

from app.tools.PDFExtract.PDFExtract import extract_pdf_with_mineru, detect_pdf_type

router = APIRouter(prefix="/pdf-tools", tags=["PDF Extract Tools"])

# 请求模型
class PDFExtractRequest(BaseModel):
    output_dir: Optional[str] = "./output"
    lang: Optional[str] = "ch"
    parse_method: Optional[str] = "auto"  # auto, txt, ocr
    formula_enable: Optional[bool] = True
    table_enable: Optional[bool] = True
    start_page_id: Optional[int] = 0
    end_page_id: Optional[int] = None

class PDFDetectRequest(BaseModel):
    text_threshold: Optional[int] = 100

# 响应模型
class PDFExtractResponse(BaseModel):
    success: bool
    message: str
    data: Optional[dict] = None
    error: Optional[str] = None

class PDFDetectResponse(BaseModel):
    success: bool
    data: Optional[dict] = None
    error: Optional[str] = None

@router.post("/pdf-detect", response_model=PDFDetectResponse)
async def pdf_detect(
    file: UploadFile = File(...),
    text_threshold: int = Form(100)
):
    """
    检测PDF类型，确定最佳解析方法
    """
    try:
        # 创建临时文件
        with tempfile.NamedTemporaryFile(delete=False, suffix=".pdf") as temp_file:
            content = await file.read()
            temp_file.write(content)
            temp_pdf_path = temp_file.name

        try:
            # 检测PDF类型
            result = detect_pdf_type(temp_pdf_path, text_threshold=text_threshold)
            
            return PDFDetectResponse(
                success=True,
                data=result
            )
        finally:
            # 清理临时文件
            os.unlink(temp_pdf_path)
    
    except Exception as e:
        return PDFDetectResponse(
            success=False,
            error=str(e)
        )

@router.post("/pdf-extract", response_model=PDFExtractResponse)
async def pdf_extract(
    file: UploadFile = File(...),
    output_dir: str = Form("./output"),
    lang: str = Form("ch"),
    parse_method: str = Form("auto"),
    formula_enable: bool = Form(True),
    table_enable: bool = Form(True),
    start_page_id: int = Form(0),
    end_page_id: Optional[int] = Form(None)
):
    """
    提取PDF内容为Markdown格式
    """
    try:
        # 创建临时文件
        with tempfile.NamedTemporaryFile(delete=False, suffix=".pdf") as temp_file:
            content = await file.read()
            temp_file.write(content)
            temp_pdf_path = temp_file.name

        try:
            # 执行PDF提取
            result = extract_pdf_with_mineru(
                pdf_path=temp_pdf_path,
                output_dir=output_dir,
                lang=lang,
                parse_method=parse_method,
                formula_enable=formula_enable,
                table_enable=table_enable,
                start_page_id=start_page_id,
                end_page_id=end_page_id
            )
            
            if result["success"]:
                message = f"PDF提取成功！结果保存在: {result['output_dir']}"
                return PDFExtractResponse(
                    success=True,
                    message=message,
                    data=result
                )
            else:
                return PDFExtractResponse(
                    success=False,
                    message="PDF提取失败",
                    error=result.get("error", "未知错误")
                )
        
        finally:
            # 清理临时文件
            os.unlink(temp_pdf_path)
    
    except Exception as e:
        return PDFExtractResponse(
            success=False,
            message="服务器内部错误",
            error=str(e)
        )