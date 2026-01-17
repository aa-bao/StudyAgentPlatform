@echo off
REM ========================================
REM 项目结构重构脚本
REM ========================================

echo.
echo ========================================
echo   考研平台 - 项目结构重构
echo ========================================
echo.
echo 此脚本将执行以下操作：
echo   1. KaoYanPlatform-back  → java-back
echo   2. KaoYanPlatform-front → vue-front
echo   3. ai_service           → python-back
echo   4. ExtractQuestionFromPDF → python-back/tools/
echo.
echo ⚠️  警告：请关闭所有IDE和编辑器后再执行！
echo.
pause

echo.
echo [1/6] 检查目录是否存在...
if not exist "KaoYanPlatform-back" (
    echo ❌ 错误：KaoYanPlatform-back 不存在
    pause
    exit /b 1
)
if not exist "KaoYanPlatform-front" (
    echo ❌ 错误：KaoYanPlatform-front 不存在
    pause
    exit /b 1
)
if not exist "ai_service" (
    echo ❌ 错误：ai_service 不存在
    pause
    exit /b 1
)
echo ✅ 所有目录存在

echo.
echo [2/6] 重命名 KaoYanPlatform-back → java-back...
move "KaoYanPlatform-back" "java-back"
if errorlevel 1 (
    echo ❌ 失败：可能需要管理员权限或关闭IDE
    pause
    exit /b 1
)
echo ✅ 完成

echo.
echo [3/6] 重命名 KaoYanPlatform-front → vue-front...
move "KaoYanPlatform-front" "vue-front"
if errorlevel 1 (
    echo ❌ 失败：可能需要管理员权限或关闭IDE
    pause
    exit /b 1
)
echo ✅ 完成

echo.
echo [4/6] 重命名 ai_service → python-back...
move "ai_service" "python-back"
if errorlevel 1 (
    echo ❌ 失败：可能需要管理员权限或关闭IDE
    pause
    exit /b 1
)
echo ✅ 完成

echo.
echo [5/6] 移动 ExtractQuestionFromPDF 到 python-back/tools...
if not exist "python-back\tools" mkdir "python-back\tools"
move "ExtractQuestionFromPDF" "python-back\tools\pdf-extractor"
if errorlevel 1 (
    echo ❌ 失败：可能需要管理员权限或关闭IDE
    pause
    exit /b 1
)
echo ✅ 完成

echo.
echo [6/6] 更新 .gitignore（如果需要）...
echo 已完成目录重构

echo.
echo ========================================
echo   重构完成！
echo ========================================
echo.
echo 新的目录结构：
echo   java-back/         (Java后端)
echo   vue-front/         (Vue前端)
echo   python-back/       (Python后端)
echo   └── tools/         (开发工具)
echo       └── pdf-extractor/
echo.
echo 下一步：
echo   1. 检查目录结构是否正确
echo   2. 更新配置文件中的路径引用
echo   3. 提交Git：git add -A && git commit -m "refactor: 重构项目目录结构"
echo.
pause
