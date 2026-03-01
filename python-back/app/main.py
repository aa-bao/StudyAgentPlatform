"""
Python 后端 - AI 服务
"""
from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware
from contextlib import asynccontextmanager
import logging

from app.routers import api_router
from app.config import settings
import uvicorn


@asynccontextmanager
async def lifespan(app: FastAPI):
    """应用生命周期管理"""
    # 配置日志
    logging.basicConfig(
        level=getattr(logging, settings.LOG_LEVEL),
        format='%(asctime)s - %(name)s - %(levelname)s - %(message)s'
    )
    logging.info("AI 服务启动中...")
    yield
    logging.info("AI 服务已停止")


# 创建 FastAPI 应用
app = FastAPI(
    title=settings.APP_NAME,
    description="",
    version="1.0.0",
    lifespan=lifespan,
    docs_url="/docs",
    redoc_url="/redoc"
)

# 配置 CORS
app.add_middleware(
    CORSMiddleware,
    allow_origins=settings.cors_origins,
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

# 包含 API 路由
app.include_router(api_router)


# ========== 根路径 ==========
@app.get("/", tags=["Root"])
async def root():
    """服务根路径"""
    return {
        "service": settings.APP_NAME,
        "version": "1.0.0",
        "status": "running",
        "endpoints": {
            "ai_services": "/ai",
            "pdf_tools": "/tools/pdf",
            "md_tools": "/tools/md",
            "docs": "/docs",
            "health": "/health"
        }
    }


# ========== 健康检查 ==========
@app.get("/health", tags=["Health"])
async def health_check():
    """健康检查接口"""
    return {
        "status": "healthy",
        "service": "python-backend",
        "version": "1.0.0",
        "timestamp": "2026-01-25"
    }


# ========== 主程序入口 ==========
if __name__ == "__main__":
    uvicorn.run(
        "main:app",
        host=settings.APP_HOST,
        port=settings.APP_PORT,
        reload=True,
        log_level=settings.LOG_LEVEL.lower()
    )
