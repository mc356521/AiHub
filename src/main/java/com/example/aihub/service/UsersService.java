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
     * 根据用户名查找用户实体。
     *
     * @param username 用户名
     * @return 匹配的用户实体，如果不存在则返回 null
     */
    Users findByUsername(String username);

    /**
     * 注册一个新用户。
     * 该方法会处理密码加密和角色分配。
     *
     * @param registerRequest 包含新用户所有必需信息的注册请求
     * @return 已创建并保存到数据库的用户实体
     * @throws RuntimeException 如果用户名或邮箱已存在
     */
    Users register(RegisterRequest registerRequest);
}
