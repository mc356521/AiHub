package com.example.aihub.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.example.aihub.dto.CreateTaskRequest;
import com.example.aihub.dto.TaskDetailResponse;
import com.example.aihub.dto.TaskListResponse;
import com.example.aihub.dto.UpdateTaskRequest;
import com.example.aihub.entity.LearningTasks;

import java.util.List;

/**
 * <p>
 * 教师发布的学习任务，可关联章节、资源等，有截止日期 服务类
 * </p>
 *
 * @author AIHub Code Generator
 * @since 2025-06-20
 */
public interface LearningTasksService extends IService<LearningTasks> {

    /**
     * 创建学习任务
     *
     * @param request 创建任务请求
     * @return 创建的任务ID
     */
    Integer createTask(CreateTaskRequest request);

    /**
     * 更新学习任务
     *
     * @param request 更新任务请求
     * @return 是否更新成功
     */
    boolean updateTask(UpdateTaskRequest request);

    /**
     * 删除学习任务
     *
     * @param taskId 任务ID
     * @return 是否删除成功
     */
    boolean deleteTask(Integer taskId);

    /**
     * 获取任务详情
     *
     * @param taskId 任务ID
     * @return 任务详情
     */
    TaskDetailResponse getTaskDetail(Integer taskId);

    /**
     * 分页获取教师创建的任务列表
     *
     * @param page     分页参数
     * @param classId  班级ID，可选
     * @param status   任务状态，可选
     * @return 分页的任务列表
     */
    IPage<TaskListResponse> getTeacherTasks(Page<TaskListResponse> page, Integer classId, String status);

    /**
     * 获取班级下的所有任务
     *
     * @param classId 班级ID
     * @return 任务列表
     */
    List<TaskListResponse> getTasksByClassId(Integer classId);
}
