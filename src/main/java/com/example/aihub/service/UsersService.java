package com.example.aihub.service;

import com.example.aihub.entity.Users;
import com.baomidou.mybatisplus.extension.service.IService;
import com.example.aihub.dto.RegisterRequest;

/**
 * <p>
 * 存储所有系统用户，包括学生、教师和管理员 服务类
 * </p>
 *
 * @author AIHub Code Generator
 * @since 2025-06-15
 */
public interface UsersService extends IService<Users> {
    /**
     * 根据用户名获取用户
     *
     * @param username 用户名
     * @return 用户信息
     */
    Users findByUsername(String username);

    /**
     * 注册新用户
     *
     * @param registerRequest 注册请求
     * @return 创建的用户信息
     */
    Users register(RegisterRequest registerRequest);
}
