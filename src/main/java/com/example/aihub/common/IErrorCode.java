package com.example.aihub.common;

/**
 * 错误码接口
 */
public interface IErrorCode {
    /**
     * 获取错误码
     */
    Integer getCode();

    /**
     * 获取错误信息
     */
    String getMessage();
} 