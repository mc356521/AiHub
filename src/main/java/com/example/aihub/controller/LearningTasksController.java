package com.example.aihub.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.aihub.common.Result;
import com.example.aihub.dto.CreateTaskRequest;
import com.example.aihub.dto.TaskDetailResponse;
import com.example.aihub.dto.TaskListResponse;
import com.example.aihub.dto.UpdateTaskRequest;
import com.example.aihub.service.LearningTasksService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * <p>
 * 教师发布的学习任务，可关联章节、资源等，有截止日期 前端控制器
 * </p>
 *
 * @author AIHub Code Generator
 * @since 2025-06-20
 */
@RestController
@RequestMapping("/learning-tasks")
@Tag(name = "学习任务管理", description = "提供学习任务的创建、查询、更新和删除功能")
@Slf4j
public class LearningTasksController extends BaseController {

    @Autowired
    private LearningTasksService learningTasksService;

    @PostMapping
    @Operation(summary = "创建学习任务", description = "教师创建新的学习任务")
    public Result<Integer> createTask(@RequestBody CreateTaskRequest request) {
        log.info("创建学习任务请求：{}", request);
        Integer taskId = learningTasksService.createTask(request);
        if (taskId == null) {
            return Result.failed("创建任务失败，请检查班级是否存在或您是否有权限");
        }
        return Result.success(taskId, "学习任务创建成功");
    }

    @PutMapping
    @Operation(summary = "更新学习任务", description = "教师更新现有学习任务")
    public Result<Boolean> updateTask(@RequestBody UpdateTaskRequest request) {
        log.info("更新学习任务请求：{}", request);
        boolean success = learningTasksService.updateTask(request);
        if (!success) {
            return Result.failed("更新任务失败，请检查任务是否存在或您是否有权限");
        }
        return Result.success(true, "学习任务更新成功");
    }

    @DeleteMapping("/{taskId}")
    @Operation(summary = "删除学习任务", description = "教师删除现有学习任务")
    public Result<Boolean> deleteTask(@PathVariable @Parameter(description = "任务ID") Integer taskId) {
        log.info("删除学习任务请求，ID：{}", taskId);
        boolean success = learningTasksService.deleteTask(taskId);
        if (!success) {
            return Result.failed("删除任务失败，请检查任务是否存在或您是否有权限");
        }
        return Result.success(true, "学习任务删除成功");
    }

    @GetMapping("/{taskId}")
    @Operation(summary = "获取任务详情", description = "获取单个学习任务的详细信息")
    public Result<TaskDetailResponse> getTaskDetail(@PathVariable @Parameter(description = "任务ID") Integer taskId) {
        log.info("获取任务详情请求，ID：{}", taskId);
        TaskDetailResponse taskDetail = learningTasksService.getTaskDetail(taskId);
        if (taskDetail == null) {
            return Result.failed("任务不存在");
        }
        return Result.success(taskDetail);
    }

    @GetMapping("/teacher")
    @Operation(summary = "获取教师创建的任务列表", description = "分页获取当前教师创建的所有任务")
    public Result<IPage<TaskListResponse>> getTeacherTasks(
            @RequestParam(defaultValue = "1") @Parameter(description = "页码") long current,
            @RequestParam(defaultValue = "10") @Parameter(description = "每页记录数") long size,
            @RequestParam(required = false) @Parameter(description = "班级ID（可选）") Integer classId,
            @RequestParam(required = false) @Parameter(description = "任务状态（可选）：draft, published, closed") String status
    ) {
        log.info("获取教师任务列表请求，页码：{}，每页大小：{}，班级ID：{}，状态：{}", current, size, classId, status);
        Page<TaskListResponse> page = new Page<>(current, size);
        IPage<TaskListResponse> taskPage = learningTasksService.getTeacherTasks(page, classId, status);
        return Result.success(taskPage);
    }

    @GetMapping("/class/{classId}")
    @Operation(summary = "获取班级任务列表", description = "获取指定班级的所有已发布任务")
    public Result<List<TaskListResponse>> getClassTasks(@PathVariable @Parameter(description = "班级ID") Integer classId) {
        log.info("获取班级任务列表请求，班级ID：{}", classId);
        List<TaskListResponse> tasks = learningTasksService.getTasksByClassId(classId);
        return Result.success(tasks);
    }
}
