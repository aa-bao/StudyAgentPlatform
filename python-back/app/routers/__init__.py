"""
工具路由主入口
"""
from fastapi import APIRouter
from .pdf_tools import router as pdf_tools_router
from .md_tools import router as md_tools_router

# 创建主工具路由器
tools_router = APIRouter(prefix="/tools", tags=["Tools"])

# 包含各个子工具路由
tools_router.include_router(pdf_tools_router)
tools_router.include_router(md_tools_router)