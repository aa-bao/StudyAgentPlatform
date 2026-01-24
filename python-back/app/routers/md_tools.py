"""
Markdown转JSON工具的FastAPI路由实现
"""
import os
import tempfile
from fastapi import APIRouter, UploadFile, File, Form
from pydantic import BaseModel
from typing import Optional
import asyncio
from concurrent.futures import ThreadPoolExecutor

from app.tools.MDToJson.extract_json import (
    read_md_file,
    split_questions_and_answers,
    split_questions,
    process_batch
)

router = APIRouter(prefix="/md-tools", tags=["Markdown Convert Tools"])

# 请求模型
class MDConvertRequest(BaseModel):
    max_workers: Optional[int] = 3
    batch_size: Optional[int] = 10

# 响应模型
class MDConvertResponse(BaseModel):
    success: bool
    message: str
    data: Optional[dict] = None
    error: Optional[str] = None

@router.post("/md-to-json", response_model=MDConvertResponse)
async def md_to_json_convert(
    file: UploadFile = File(...),
    max_workers: int = Form(3),
    batch_size: int = Form(10)
):
    """
    将Markdown文件转换为JSON格式的试题
    """
    try:
        # 创建临时文件
        with tempfile.NamedTemporaryFile(delete=False, suffix=".md", mode='w+', encoding='utf-8') as temp_file:
            content = await file.read()
            decoded_content = content.decode('utf-8')
            temp_file.write(decoded_content)
            temp_md_path = temp_file.name

        try:
            # 读取Markdown文件内容
            full_content = read_md_file(temp_md_path)
            print(f"成功读取文件，字符数: {len(full_content)}")
            
            # 分离题目和解析部分
            print("\n[第一步] 分离题目部分和解析部分...")
            questions_part, answers_part = split_questions_and_answers(full_content)
            print(f"题目部分字符数: {len(questions_part)}")
            print(f"解析部分字符数: {len(answers_part)}")

            # 处理题目部分
            print(f"\n[第二步] 提取题目信息...")
            batches = split_questions(questions_part)
            print(f"题目将分成 {len(batches)} 个批次处理")

            all_questions = []

            # 使用线程池并发处理
            batch_info_list = [(i, batch) for i, batch in enumerate(batches, 1)]

            loop = asyncio.get_event_loop()
            with ThreadPoolExecutor(max_workers=max_workers) as executor:
                # 提交所有任务
                futures = [
                    loop.run_in_executor(executor, process_batch, batch_info)
                    for batch_info in batch_info_list
                ]
                
                # 等待所有任务完成
                results = await asyncio.gather(*futures, return_exceptions=True)
                
                # 处理结果
                for result in results:
                    if isinstance(result, Exception):
                        print(f"批处理异常: {result}")
                        continue
                    
                    if result and result[1] is not None:  # result = (index, questions, error)
                        _, questions, _ = result
                        if questions:
                            all_questions.extend(questions)

            # 按questionNumber排序
            if all_questions:
                all_questions.sort(key=lambda x: x.get('questionNumber', 0))

                final_data = {"questions": all_questions}
                
                # 生成输出文件名
                output_filename = f"{os.path.splitext(file.filename)[0]}.json"
                
                # 将结果写入临时文件并返回内容
                result_data = {
                    "questions": all_questions,
                    "total_count": len(all_questions),
                    "filename": output_filename
                }
                
                return MDConvertResponse(
                    success=True,
                    message=f"转换成功！共提取 {len(all_questions)} 道题。",
                    data=result_data
                )
            else:
                return MDConvertResponse(
                    success=False,
                    message="转换失败：没有提取到任何题目",
                    error="没有提取到任何题目"
                )
        
        finally:
            # 清理临时文件
            os.unlink(temp_md_path)
    
    except Exception as e:
        return MDConvertResponse(
            success=False,
            message="服务器内部错误",
            error=str(e)
        )