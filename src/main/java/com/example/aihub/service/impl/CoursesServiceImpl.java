package com.example.aihub.service.impl;

import com.example.aihub.entity.Courses;
import com.example.aihub.entity.Users;
import com.example.aihub.mapper.CoursesMapper;
import com.example.aihub.service.CoursesService;
import com.example.aihub.service.UsersService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.aihub.dto.CreateCourseRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

/**
 * <p>
 * 课程基本信息和Markdown文件元数据 服务实现类
 * </p>
 *
 * @author hahaha
 * @since 2024-07-25
 */
@Service
public class CoursesServiceImpl extends ServiceImpl<CoursesMapper, Courses> implements CoursesService {

    @Value("${file.storage.path}")
    private String storagePath;

    @Autowired
    private UsersService usersService;

    @Override
    @Transactional
    public Courses createCourse(CreateCourseRequest request) {
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
            throw new SecurityException("只有教师或管理员才能创建课程");
        }

        // 3. 创建并保存课程实体
        Courses course = new Courses();
        course.setTitle(request.getTitle());
        course.setDescription(request.getDescription());
        course.setTeacherId(currentUser.getId().intValue());
        course.setParseStatus("pending"); // 默认状态
        course.setChapterCount(0);

        // 4. 生成Markdown文件路径并创建文件
        try {
            // 创建存储目录（如果不存在）
            Path directory = Paths.get(storagePath);
            if (!Files.exists(directory)) {
                Files.createDirectories(directory);
            }

            // 生成唯一文件名
            String fileName = UUID.randomUUID().toString() + ".md";
            Path filePath = directory.resolve(fileName);

            // 创建空文件
            Files.createFile(filePath);

            course.setFilePath(filePath.toString().replace(File.separator, "/"));
        } catch (IOException e) {
            log.error("创建Markdown文件失败", e);
            throw new RuntimeException("创建课程文件时发生错误", e);
        }

        // 5. 保存课程信息到数据库
        this.save(course);
        return course;
    }
}
