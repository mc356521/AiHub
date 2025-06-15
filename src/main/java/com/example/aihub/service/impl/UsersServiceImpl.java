package com.example.aihub.service.impl;

import com.example.aihub.entity.Users;
import com.example.aihub.mapper.UsersMapper;
import com.example.aihub.service.UsersService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import com.example.aihub.dto.RegisterRequest;

/**
 * <p>
 * 存储所有系统用户，包括学生、教师和管理员 服务实现类
 * </p>
 *
 * @author AIHub Code Generator
 * @since 2025-06-15
 */
@Service
public class UsersServiceImpl extends ServiceImpl<UsersMapper, Users> implements UsersService {

    @Lazy
    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public Users findByUsername(String username) {
        return lambdaQuery().eq(Users::getUsername, username).one();
    }

    @Override
    public Users register(RegisterRequest registerRequest) {
        // 检查用户名是否已存在
        if (lambdaQuery().eq(Users::getUsername, registerRequest.getUsername()).count() > 0) {
            throw new RuntimeException("用户名已存在");
        }
        // 检查邮箱是否已存在
        if (lambdaQuery().eq(Users::getEmail, registerRequest.getEmail()).count() > 0) {
            throw new RuntimeException("电子邮箱已注册");
        }

        Users user = new Users();
        user.setUsername(registerRequest.getUsername());
        user.setEmail(registerRequest.getEmail());
        user.setFullName(registerRequest.getFullName());
        user.setUserCode(registerRequest.getUserCode());
        user.setPasswordHash(passwordEncoder.encode(registerRequest.getPassword()));
        // 处理角色，默认为学生
        String role = registerRequest.getRole();
        if (!"teacher".equals(role)) {
            role = "student";
        }
        user.setRole(role);

        save(user);
        return user;
    }
}
