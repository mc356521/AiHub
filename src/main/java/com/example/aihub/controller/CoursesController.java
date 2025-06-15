package com.example.aihub.controller;


import com.example.aihub.common.Result;
import com.example.aihub.dto.CreateCourseRequest;
import com.example.aihub.entity.Courses;
import com.example.aihub.service.CoursesService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import org.springframework.web.bind.annotation.RestController;

/**
 * <p>
 * 课程基本信息和Markdown文件元数据 前端控制器
 * </p>
 *
 * @author hahaha
 * @since 2024-07-25
 */
@Tag(name = "课程管理", description = "提供课程的增删改查功能")
@RestController
@RequestMapping("/courses")
public class CoursesController {

    @Autowired
    private CoursesService coursesService;

    @PreAuthorize("hasAnyAuthority('teacher', 'admin')")
    @Operation(summary = "创建新课程", description = "创建一个新的课程，并自动生成对应的Markdown文件。只有教师或管理员可以访问。")
    @PostMapping
    public Result<Courses> createCourse(@RequestBody CreateCourseRequest createCourseRequest) {
        try {
            Courses course = coursesService.createCourse(createCourseRequest);
            return Result.success(course, "课程创建成功");
        } catch (SecurityException e) {
            return Result.failed(e.getMessage());
        } catch (Exception e) {
            return Result.failed("创建课程时发生未知错误");
        }
    }
} 