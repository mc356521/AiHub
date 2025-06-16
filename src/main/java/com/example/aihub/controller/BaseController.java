package com.example.aihub.controller;

import com.example.aihub.common.Result;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 基础控制器类，提供全局异常处理
 */
@Slf4j
@RestControllerAdvice
public class BaseController {

    /**
     * 全局异常处理
     * @param e 捕获到的异常
     * @return 统一的失败响应
     */
    @ExceptionHandler(Exception.class)
    @ResponseBody
    public Result<String> handleException(Exception e) {
        log.error("捕获到全局异常", e); // 使用log.error记录异常堆栈
        return Result.failed("服务器内部错误: " + e.getMessage());
    }
} 