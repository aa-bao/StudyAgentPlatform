#!/bin/bash
# ========================================
# 项目结构重构脚本 (Linux/Mac)
# ========================================

echo ""
echo "========================================"
echo "  考研平台 - 项目结构重构"
echo "========================================"
echo ""
echo "此脚本将执行以下操作："
echo "  1. KaoYanPlatform-back  → java-back"
echo "  2. KaoYanPlatform-front → vue-front"
echo "  3. ai_service           → python-back"
echo "  4. ExtractQuestionFromPDF → python-back/tools/"
echo ""
read -p "确认执行？(y/n): " confirm

if [ "$confirm" != "y" ]; then
    echo "已取消"
    exit 0
fi

echo ""
echo "[1/6] 检查目录是否存在..."
if [ ! -d "KaoYanPlatform-back" ]; then
    echo "❌ 错误：KaoYanPlatform-back 不存在"
    exit 1
fi
if [ ! -d "KaoYanPlatform-front" ]; then
    echo "❌ 错误：KaoYanPlatform-front 不存在"
    exit 1
fi
if [ ! -d "ai_service" ]; then
    echo "❌ 错误：ai_service 不存在"
    exit 1
fi
echo "✅ 所有目录存在"

echo ""
echo "[2/6] 重命名 KaoYanPlatform-back → java-back..."
mv "KaoYanPlatform-back" "java-back"
if [ $? -ne 0 ]; then
    echo "❌ 失败"
    exit 1
fi
echo "✅ 完成"

echo ""
echo "[3/6] 重命名 KaoYanPlatform-front → vue-front..."
mv "KaoYanPlatform-front" "vue-front"
if [ $? -ne 0 ]; then
    echo "❌ 失败"
    exit 1
fi
echo "✅ 完成"

echo ""
echo "[4/6] 重命名 ai_service → python-back..."
mv "ai_service" "python-back"
if [ $? -ne 0 ]; then
    echo "❌ 失败"
    exit 1
fi
echo "✅ 完成"

echo ""
echo "[5/6] 移动 ExtractQuestionFromPDF 到 python-back/tools/..."
mkdir -p "python-back/tools"
mv "ExtractQuestionFromPDF" "python-back/tools/pdf-extractor"
if [ $? -ne 0 ]; then
    echo "❌ 失败"
    exit 1
fi
echo "✅ 完成"

echo ""
echo "[6/6] 更新 Git..."
git add -A
git status

echo ""
echo "========================================"
echo "  重构完成！"
echo "========================================"
echo ""
echo "新的目录结构："
echo "  java-back/         (Java后端)"
echo "  vue-front/         (Vue前端)"
echo "  python-back/       (Python后端)"
echo "  └── tools/         (开发工具)"
echo "      └── pdf-extractor/"
echo ""
echo "下一步："
echo "  1. 检查目录结构是否正确"
echo "  2. 更新配置文件中的路径引用"
echo "  3. 提交Git："
echo "     git add -A"
echo "     git commit -m 'refactor: 重构项目目录结构'"
echo ""
