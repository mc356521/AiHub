package com.example.aihub.service.impl;

import com.example.aihub.entity.Semesters;
import com.example.aihub.entity.Users;
import com.example.aihub.mapper.SemestersMapper;
import com.example.aihub.service.SemestersService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.aihub.service.UsersService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * <p>
 * 统一管理所有学期信息 服务实现类
 * </p>
 *
 * @author AIHub Code Generator
 * @since 2025-06-17
 */
@Slf4j
@Service
public class SemestersServiceImpl extends ServiceImpl<SemestersMapper, Semesters> implements SemestersService {

    @Autowired
    private UsersService usersService;

    @Override
    public boolean insertSemesters(Semesters semesters) {
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
            throw new SecurityException("只有教师或管理员才能创建学期信息");
        }
        return this.save(semesters);
    }


    /**
     * 获取学期列表全部内容
     * @return 学期列表内容
     */
    @Override
    public List<Semesters> selectAllSemesters() {
        return this.list();
    }

    @Override
    public Semesters getSemesterById(Integer id) {
        log.info("在Service层开始通过ID查询学期: {}", id);
        Semesters semester = this.getById(id);
        log.info("Service层查询结果: {}", semester);
        return semester;
    }
}
