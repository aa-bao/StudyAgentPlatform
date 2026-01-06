package org.example.kaoyanplatform.handler;

import lombok.extern.slf4j.Slf4j;
import org.example.kaoyanplatform.common.Result;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;

/**
 * 全局异常处理器
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(NoResourceFoundException.class)
    public void handleNoResourceFoundException(NoResourceFoundException e) {
        // 忽略favicon.ico等静态资源找不到的异常
        if (!e.getResourcePath().contains("favicon.ico")) {
            log.warn("资源未找到: {}", e.getResourcePath());
        }
    }

    @ExceptionHandler(Exception.class)
    public Result<String> handleException(Exception e) {
        // 忽略favicon.ico相关异常
        if (e instanceof NoResourceFoundException) {
            return null;
        }
        log.error("系统异常", e);
        return Result.error("系统繁忙，请稍后重试: " + e.getMessage());
    }

    @ExceptionHandler(RuntimeException.class)
    public Result<String> handleRuntimeException(RuntimeException e) {
        log.error("业务异常", e);
        return Result.error(e.getMessage());
    }
}
