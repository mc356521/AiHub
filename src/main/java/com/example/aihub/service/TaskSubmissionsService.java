package com.example.aihub.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.aihub.dto.TaskSubmissionRequest;
import com.example.aihub.entity.TaskSubmissions;

import java.util.List;

/**
 * <p>
 * 记录学生对学习任务的完成情况 服务类
 * </p>
 *
 * @author AIHub Code Generator
 * @since 2025-06-20
 */
public interface TaskSubmissionsService extends IService<TaskSubmissions> {

    /**
     * 学生提交任务
     *
     * @param request 提交请求
     * @return 是否提交成功
     */
    boolean submitTask(TaskSubmissionRequest request);

    /**
     * 获取学生对任务的提交记录
     *
     * @param taskId    任务ID
     * @param studentId 学生ID
     * @return 提交记录
     */
    TaskSubmissions getStudentSubmission(Integer taskId, Integer studentId);

    /**
     * 获取任务的所有提交记录
     *
     * @param taskId 任务ID
     * @return 提交记录列表
     */
    List<TaskSubmissions> getTaskSubmissions(Integer taskId);

    /**
     * 初始化班级学生的任务提交记录
     * 当任务发布时，为班级中的所有学生创建提交记录（状态为pending）
     *
     * @param taskId  任务ID
     * @param classId 班级ID
     * @return 创建的记录数量
     */
    int initializeTaskSubmissions(Integer taskId, Integer classId);
}
