package com.example.aihub.controller;

import com.example.aihub.common.Result;
import com.example.aihub.dto.ClassesRequest;
import com.example.aihub.dto.JoinClassRequest;
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
    @Operation(summary = "创建新班级", description = "查询教师管理得所有班级。")
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
        List<ClassesEntity> allClasses = classesService.all(Integer.parseInt(String.valueOf(currentUser.getId())));
        return Result.success(allClasses, "获取所有班级成功");
    }

    @PreAuthorize("hasAnyAuthority('teacher', 'admin')")
    @Operation(summary = "获取教师指定状态的所有班级", description = "获取当前登录的教师用户下，处于指定状态的所有班级列表。状态可选值为: 'pending', 'active', 'finished', 'archived'")
    @GetMapping("/my")
    public Result<List<ClassesEntity>> getMyClassesByStatus(@RequestParam("status") String status) {
        // 1. 获取当前用户信息
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (!(principal instanceof UserDetails)) {
            return Result.failed("用户未登录或认证信息不正确");
        }
        String username = ((UserDetails) principal).getUsername();
        Users currentUser = usersService.findByUsername(username);
        if (currentUser == null) {
            return Result.failed("无法找到当前登录用户的数据");
        }

        log.info("请求获取教师 {} 的状态为 {} 的班级列表", currentUser.getId(), status);
        List<ClassesEntity> classes = classesService.findClassesByTeacherAndStatus(Long.valueOf(currentUser.getId()), status);
        log.debug("获取到班级 {} 条", classes.size());
        return Result.success(classes, "获取教师班级列表成功");
    }


    @Operation(summary = "根据ID获取班级信息", description = "根据提供的唯一ID获取单个班级的详细信息。")
    @GetMapping("/{id}")
    public Result<ClassesEntity> getClassById(@PathVariable Integer id) {
        log.info("请求获取ID为 {} 的班级信息", id);
        ClassesEntity classEntity = classesService.getById(id);

        if (classEntity == null) {
            log.warn("尝试获取一个不存在的班级，ID: {}", id);
            return Result.failed("班级不存在");
        }

        return Result.success(classEntity, "获取班级信息成功");
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

    @Operation(summary = "通过班级口令加入班级", description = "学生用户通过提供班级口令来加入一个班级。")
    @PostMapping("/join")
    @PreAuthorize("isAuthenticated()")
    public Result<?> joinClassByCode(@RequestBody JoinClassRequest request) {
        // 1. 获取当前用户信息
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (!(principal instanceof UserDetails)) {
            return Result.failed("用户未登录或认证信息不正确");
        }
        String username = ((UserDetails) principal).getUsername();
        Users currentUser = usersService.findByUsername(username);
        if (currentUser == null) {
            return Result.failed("无法找到当前登录用户的数据");
        }

        try {
            log.info("用户 {} 尝试使用口令 {} 加入班级", username, request.getClassCode());
            // 此处存在ID类型转换，与项目中其他部分保持一致
            Integer studentId = Integer.parseInt(String.valueOf(currentUser.getId()));
            classesService.joinClassByCode(studentId, request.getClassCode());
            log.info("用户 {} 成功加入班级", username);
            return Result.success(null, "成功加入班级");
        } catch (RuntimeException e) {
            log.warn("用户 {} 加入班级失败: {}", username, e.getMessage());
            return Result.failed(e.getMessage());
        }
    }
}
























