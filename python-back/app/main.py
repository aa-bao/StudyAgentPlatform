from fastapi import FastAPI

from app.routers import tools_router

app = FastAPI()

# 添加工具路由
app.include_router(tools_router)

import fastapi_cdn_host
fastapi_cdn_host.patch_docs(app)

@app.get("/")
async def root():
    return {"message": "考研平台Python后端 - 专用于数据处理和AI访问"}

@app.get("/health")
async def health_check():
    return {"status": "healthy", "service": "python-backend"}