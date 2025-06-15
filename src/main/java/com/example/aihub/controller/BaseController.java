package com.example.aihub.controller;

import com.example.aihub.common.Result;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * 基础控制器类
 */
public class BaseController {

    /**
     * 获取当前登录用户名
     */
    protected String getCurrentUsername(HttpServletRequest request) {
        return (String) request.getAttribute("username");
    }

    /**
     * 全局异常处理
     */
    @ExceptionHandler(Exception.class)
    @ResponseBody
    public Result<String> handleException(Exception e) {
        e.printStackTrace();
        return Result.failed(e.getMessage());
    }
} 