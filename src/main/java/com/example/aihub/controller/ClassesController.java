package com.example.aihub.controller;

import com.example.aihub.common.Result;
import com.example.aihub.dto.ClassesRequest;
import com.example.aihub.entity.ClassesEntity;
import com.example.aihub.entity.Users;
import com.example.aihub.service.ClassesService;
import com.example.aihub.service.UsersService;
import com.example.aihub.util.BasicUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cglib.core.Local;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.security.Timestamp;
import java.time.LocalDateTime;
import java.util.List;

/**
 *
 * <p>
 * 班级管理控制器
 * </p>
 *
 * @created: ii_kun
 * @createTime: 2025/6/17 11:10
 * @email: weijikun1@icloud.com
 */
@Tag(name = "班级管理", description = "创建班级，获取班数据")
@RestController
@RequestMapping("/classes")
@Slf4j
public class ClassesController extends BaseController {

    @Autowired
    private UsersService usersService;

    @Autowired
    private ClassesService  classesService;


    @PreAuthorize("hasAnyAuthority('teacher', 'admin')")
    @Operation(summary = "创建新课程", description = "创建一个新的课程，并自动生成对应的Markdown文件。只有教师或管理员可以访问。")
    @PostMapping
    public Result<ClassesRequest> createClasses(@RequestBody ClassesRequest ClassesRequest) {
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
        // 组装数据
        ClassesEntity classesModel = new ClassesEntity();
        classesModel.setName(ClassesRequest.getName());
        classesModel.setTeacherId(Integer.parseInt(String.valueOf(currentUser.getId())));
        classesModel.setCourseId(ClassesRequest.getCourseId());
        classesModel.setSemesterId(ClassesRequest.getSemesterId());
        classesModel.setClassCode(BasicUtil.getRandomCommand());
        classesModel.setStatus(ClassesRequest.getStatus());
        classesModel.setCreateTime(LocalDateTime.now());
        classesModel.setUpdateTime(LocalDateTime.now());

        val classes = classesService.addClass(classesModel);
        if (classes) {
            return Result.success(ClassesRequest, "创建班级成功");
        } else {
            return Result.failed("创建班级失败");
        }
    }



    @PreAuthorize("hasAnyAuthority('teacher', 'admin')")
    @Operation(summary = "获取教师管理得所有班级信息", description = "获取教师管理下得所有班级信息需要token， 必须时教师/管理员创建过得班级信息才能获取")
    @GetMapping("/all")
    public Result<List<ClassesEntity>> getClasses() {
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
        val all = classesService.all(Integer.parseInt(String.valueOf(currentUser.getId())));
        if (!all.isEmpty()) {
            return Result.success(all);
        } else {
            return Result.failed("获取所有班级失败!");
        }
    }


    @PutMapping("/update")
    @PreAuthorize("hasAnyAuthority('teacher', 'admin')")
    public Result<String> updateClass(@RequestBody ClassesRequest request) {
        boolean updated = classesService.updateClasses(request);
        if (updated) {
            return Result.success("更新成功");
        } else {
            return Result.failed("更新失败，请检查参数或班级是否存在");
        }
    }
}
























