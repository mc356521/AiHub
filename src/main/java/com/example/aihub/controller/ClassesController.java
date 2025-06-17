package com.example.aihub.controller;

import com.example.aihub.common.Result;
import com.example.aihub.dto.ClassesRequest;
import com.example.aihub.service.ClassesService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
    private ClassesService  classesService;


    @PreAuthorize("hasAnyAuthority('teacher', 'admin')")
    @Operation(summary = "创建新课程", description = "创建一个新的课程，并自动生成对应的Markdown文件。只有教师或管理员可以访问。")
    @PostMapping
    public Result<ClassesRequest> createClasses(@RequestBody ClassesRequest ClassesRequest) {
        val classes = classesService.addClass(ClassesRequest);
        if (classes) {
            return Result.success(ClassesRequest, "创建班级成功");
        } else {
            return Result.failed("创建班级失败");
        }
    }

}
























