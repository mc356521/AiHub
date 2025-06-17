package com.example.aihub.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.aihub.dto.ClassesRequest;
import com.example.aihub.entity.ClassesEntity;
import com.example.aihub.entity.Users;
import com.example.aihub.mapper.ClassesMapper;
import com.example.aihub.service.ClassesService;
import com.example.aihub.service.CoursesService;
import com.example.aihub.service.UsersService;
import com.example.aihub.util.BasicUtil;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

/**
 * @created: ii_kun
 * @createTime: 2025/6/16 23:20
 * @email: weijikun1@icloud.com
 */
@Service
public class ClassesServiceImpl extends ServiceImpl<ClassesMapper, ClassesRequest> implements ClassesService {

    @Autowired
    private UsersService usersService;

    @Autowired
    private CoursesService coursesService;

    /**
     * 新增班级
     *
     * @param classes 班级实体
     * @return 是否新增成功
     */
    @SuppressWarnings("DuplicatedCode")
    @Override
    public boolean addClass(ClassesRequest classes) {
        // 1. 获取当前用户信息
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (!(principal instanceof UserDetails)) {
            throw new IllegalStateException("用户未登录或认证信息不正确");
        }
        String username = ((UserDetails) principal).getUsername();
        Users currentUser = usersService.findByUsername(username);
        if (currentUser == null) {
            throw new IllegalStateException("无法找到当前登录用户的数据");
        }

        // 2. 检查用户角色
        String role = currentUser.getRole();
        if (!"teacher".equals(role) && !"admin".equals(role)) {
            throw new SecurityException("只有教师或管理员才能创建班级");
        }
        return this.save(classes);
    }
}
