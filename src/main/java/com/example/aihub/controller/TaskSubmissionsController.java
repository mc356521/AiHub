package com.example.aihub.controller;

import com.example.aihub.common.Result;
import com.example.aihub.dto.TaskSubmissionRequest;
import com.example.aihub.entity.TaskSubmissions;
import com.example.aihub.entity.Users;
import com.example.aihub.service.TaskSubmissionsService;
import com.example.aihub.service.UsersService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * <p>
 * 记录学生对学习任务的完成情况 前端控制器
 * </p>
 *
 * @author AIHub Code Generator
 * @since 2025-06-20
 */
@RestController
@RequestMapping("/task-submissions")
@Tag(name = "任务提交管理", description = "提供学习任务提交的相关功能")
@Slf4j
public class TaskSubmissionsController extends BaseController {

    @Autowired
    private TaskSubmissionsService taskSubmissionsService;
    
    @Autowired
    private UsersService usersService;

    @PostMapping
    @Operation(summary = "提交学习任务", description = "学生提交学习任务")
    public Result<Boolean> submitTask(@RequestBody TaskSubmissionRequest request) {
        log.info("提交学习任务请求：{}", request);
        boolean success = taskSubmissionsService.submitTask(request);
        if (!success) {
            return Result.failed("提交任务失败，请检查任务是否存在");
        }
        return Result.success(true, "任务提交成功");
    }

    @GetMapping("/task/{taskId}")
    @Operation(summary = "获取任务的所有提交记录", description = "教师获取指定任务的所有学生提交记录")
    public Result<List<TaskSubmissions>> getTaskSubmissions(
            @PathVariable @Parameter(description = "任务ID") Integer taskId
    ) {
        log.info("获取任务提交记录请求，任务ID：{}", taskId);
        List<TaskSubmissions> submissions = taskSubmissionsService.getTaskSubmissions(taskId);
        return Result.success(submissions);
    }

    @GetMapping("/my/{taskId}")
    @Operation(summary = "获取学生的任务提交记录", description = "学生获取自己对某任务的提交记录")
    public Result<TaskSubmissions> getMySubmission(
            @PathVariable @Parameter(description = "任务ID") Integer taskId
    ) {
        log.info("获取我的任务提交记录请求，任务ID：{}", taskId);
        // 获取当前登录用户
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String username = ((UserDetails) principal).getUsername();
        Integer studentId = getUserIdByUsername(username);
        
        if (studentId == null) {
            return Result.failed("获取用户信息失败");
        }
        
        TaskSubmissions submission = taskSubmissionsService.getStudentSubmission(taskId, studentId);
        if (submission == null) {
            return Result.failed("未找到提交记录");
        }
        return Result.success(submission);
    }

    @PostMapping("/initialize/{taskId}/{classId}")
    @Operation(summary = "初始化任务提交记录", description = "为班级中的所有学生创建任务提交记录")
    public Result<Integer> initializeTaskSubmissions(
            @PathVariable @Parameter(description = "任务ID") Integer taskId,
            @PathVariable @Parameter(description = "班级ID") Integer classId
    ) {
        log.info("初始化任务提交记录请求，任务ID：{}，班级ID：{}", taskId, classId);
        int count = taskSubmissionsService.initializeTaskSubmissions(taskId, classId);
        return Result.success(count, "初始化任务提交记录成功");
    }
    
    /**
     * 根据用户名获取用户ID
     *
     * @param username 用户名
     * @return 用户ID
     */
    private Integer getUserIdByUsername(String username) {
        try {
            Users user = usersService.findByUsername(username);
            if (user != null) {
                return user.getId();
            }
            return null;
        } catch (Exception e) {
            log.error("获取用户ID失败", e);
            return null;
        }
    }
}
