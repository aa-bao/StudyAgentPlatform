import os
import tempfile
from pathlib import Path
from typing import Optional


def create_temp_file(data: bytes, suffix: str = "") -> str:
    """
    创建临时文件并返回路径
    """
    temp_file = tempfile.NamedTemporaryFile(delete=False, suffix=suffix)
    temp_file.write(data)
    temp_file.close()
    return temp_file.name


def cleanup_temp_file(filepath: str) -> bool:
    """
    清理临时文件
    """
    try:
        os.unlink(filepath)
        return True
    except OSError:
        return False


def validate_file_extension(filename: str, allowed_extensions: list) -> bool:
    """
    验证文件扩展名
    """
    ext = Path(filename).suffix.lower()
    return ext in allowed_extensions