package com.example.aihub.service.impl;

import com.example.aihub.entity.Users;
import com.example.aihub.service.UsersService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * Spring Security UserDetailsService 核心实现类。
 * 负责在用户认证过程中，根据用户名从数据库加载用户核心信息（用户名、密码、权限）。
 */
@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    @Autowired
    private UsersService usersService;

    /**
     * 根据用户名加载用户信息。
     * Spring Security 在处理登录请求时会自动调用此方法。
     *
     * @param username 前端传入的用户名
     * @return 包含用户核心信息的 UserDetails 对象
     * @throws UsernameNotFoundException 如果数据库中不存在该用户
     */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Users user = usersService.findByUsername(username);
        if (user == null) {
            throw new UsernameNotFoundException("用户不存在: " + username);
        }
        
        List<GrantedAuthority> authorities = new ArrayList<>();
        if (user.getRole() != null && !user.getRole().isEmpty()) {
            authorities.add(new SimpleGrantedAuthority(user.getRole()));
        }

        return new User(user.getUsername(), user.getPasswordHash(), authorities);
    }
} 