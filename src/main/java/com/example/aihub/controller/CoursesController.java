package com.example.aihub.controller;

import com.example.aihub.common.Result;
import com.example.aihub.dto.CreateCourseRequest;
import com.example.aihub.entity.Courses;
import com.example.aihub.service.CoursesService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * <p>
 * 课程基本信息和Markdown文件元数据 前端控制器
 * </p>
 *
 * @author hahaha
 * @since 2024-07-25
 */
@Tag(name = "课程管理", description = "提供课程的增删改查及解析功能")
@RestController
@RequestMapping("/courses")
@Slf4j
public class CoursesController extends BaseController {

    @Autowired
    private CoursesService coursesService;

    @PreAuthorize("hasAnyAuthority('teacher', 'admin')")
    @Operation(summary = "创建新课程", description = "创建一个新的课程，并自动生成对应的Markdown文件。只有教师或管理员可以访问。")
    @PostMapping
    public Result<Courses> createCourse(@RequestBody CreateCourseRequest createCourseRequest) {
        log.info("请求创建新课程，标题: {}", createCourseRequest.getTitle());
        Courses course = coursesService.createCourse(createCourseRequest);
        log.info("课程创建成功，课程ID: {}", course.getId());
        return Result.success(course, "课程创建成功");
    }

    @PreAuthorize("hasAnyAuthority('teacher', 'admin')")
    @Operation(summary = "解析课程内容", description = "解析指定课程的Markdown文件，并将其结构化的章节信息存入数据库。")
    @PostMapping("/{courseId}/parse")
    public Result<?> parseCourse(@PathVariable Integer courseId) throws Exception {
        log.info("请求解析课程，课程ID: {}", courseId);
        coursesService.parseAndSaveChapters(courseId);
        log.info("课程解析成功，课程ID: {}", courseId);
        return Result.success(null, "课程解析成功");
    }

    @Operation(summary = "获取当前教师的课程列表", description = "获取当前登录的教师用户创建的所有课程列表。")
    @GetMapping("/my")
    @PreAuthorize("hasAuthority('teacher')")
    public Result<List<Courses>> getMyCourses() {
        log.info("请求获取当前教师的课程列表");
        List<Courses> courses = coursesService.getMyCourses();
        log.debug("获取到课程 {} 条", courses.size());
        return Result.success(courses, "获取我的课程列表成功");
    }

    @Operation(summary = "获取课程Markdown原文", description = "获取指定课程的完整Markdown文件内容，用于前端渲染。任何认证过的用户都可以访问。")
    @GetMapping("/{courseId}/content")
    @PreAuthorize("isAuthenticated()")
    public Result<String> getCourseContent(@PathVariable Integer courseId) throws Exception {
        log.info("请求获取课程Markdown内容，课程ID: {}", courseId);
        String content = coursesService.getCourseMarkdownContent(courseId);
        log.debug("成功获取到课程内容，课程ID: {}", courseId);
        return Result.success(content, "获取课程内容成功");
    }
} 