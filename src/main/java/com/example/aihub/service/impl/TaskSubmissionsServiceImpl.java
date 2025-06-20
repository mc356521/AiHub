package com.example.aihub.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.aihub.dto.TaskSubmissionRequest;
import com.example.aihub.entity.ClassMembers;
import com.example.aihub.entity.LearningTasks;
import com.example.aihub.entity.TaskSubmissions;
import com.example.aihub.entity.Users;
import com.example.aihub.mapper.ClassMembersMapper;
import com.example.aihub.mapper.LearningTasksMapper;
import com.example.aihub.mapper.TaskSubmissionsMapper;
import com.example.aihub.service.TaskSubmissionsService;
import com.example.aihub.service.UsersService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * <p>
 * 记录学生对学习任务的完成情况 服务实现类
 * </p>
 *
 * @author AIHub Code Generator
 * @since 2025-06-20
 */
@Service
@Slf4j
public class TaskSubmissionsServiceImpl extends ServiceImpl<TaskSubmissionsMapper, TaskSubmissions> implements TaskSubmissionsService {

    @Autowired
    private UsersService usersService;

    @Autowired
    private LearningTasksMapper learningTasksMapper;

    @Autowired
    private ClassMembersMapper classMembersMapper;

    /**
     * 获取当前登录用户
     *
     * @return 当前登录用户
     */
    private Users getCurrentUser() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String username = ((UserDetails) principal).getUsername();
        return usersService.findByUsername(username);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean submitTask(TaskSubmissionRequest request) {
        log.info("提交学习任务：{}", request);
        
        // 获取当前登录用户（学生）
        Users currentUser = getCurrentUser();
        
        // 验证任务是否存在
        LearningTasks task = learningTasksMapper.selectById(request.getTaskId());
        if (task == null) {
            log.error("提交任务失败：任务不存在");
            return false;
        }
        
        // 检查任务是否已过期
        if (task.getDueDate() != null && task.getDueDate().isBefore(LocalDateTime.now())) {
            log.warn("任务已过期，但仍允许提交，标记为逾期提交");
            // 允许提交，但会标记为逾期
        }
        
        // 查找是否已有提交记录
        LambdaQueryWrapper<TaskSubmissions> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(TaskSubmissions::getTaskId, request.getTaskId());
        queryWrapper.eq(TaskSubmissions::getStudentId, currentUser.getId());
        TaskSubmissions existingSubmission = baseMapper.selectOne(queryWrapper);
        
        if (existingSubmission != null) {
            // 更新现有提交
            existingSubmission.setContent(request.getContent());
            existingSubmission.setCompletedAt(LocalDateTime.now());
            
            // 根据截止日期判断状态
            if (task.getDueDate() != null && task.getDueDate().isBefore(LocalDateTime.now())) {
                existingSubmission.setStatus("overdue");
            } else {
                existingSubmission.setStatus("completed");
            }
            
            int result = baseMapper.updateById(existingSubmission);
            log.info("更新任务提交结果：{}", result > 0);
            return result > 0;
        } else {
            // 创建新提交
            TaskSubmissions submission = new TaskSubmissions();
            submission.setTaskId(request.getTaskId());
            submission.setStudentId(currentUser.getId());
            submission.setContent(request.getContent());
            submission.setCompletedAt(LocalDateTime.now());
            
            // 根据截止日期判断状态
            if (task.getDueDate() != null && task.getDueDate().isBefore(LocalDateTime.now())) {
                submission.setStatus("overdue");
            } else {
                submission.setStatus("completed");
            }
            
            int result = baseMapper.insert(submission);
            log.info("创建任务提交结果：{}", result > 0);
            return result > 0;
        }
    }

    @Override
    public TaskSubmissions getStudentSubmission(Integer taskId, Integer studentId) {
        log.info("获取学生任务提交记录，任务ID：{}，学生ID：{}", taskId, studentId);
        
        LambdaQueryWrapper<TaskSubmissions> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(TaskSubmissions::getTaskId, taskId);
        queryWrapper.eq(TaskSubmissions::getStudentId, studentId);
        
        return baseMapper.selectOne(queryWrapper);
    }

    @Override
    public List<TaskSubmissions> getTaskSubmissions(Integer taskId) {
        log.info("获取任务所有提交记录，任务ID：{}", taskId);
        
        LambdaQueryWrapper<TaskSubmissions> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(TaskSubmissions::getTaskId, taskId);
        queryWrapper.orderByDesc(TaskSubmissions::getUpdateTime);
        
        return baseMapper.selectList(queryWrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int initializeTaskSubmissions(Integer taskId, Integer classId) {
        log.info("初始化班级学生的任务提交记录，任务ID：{}，班级ID：{}", taskId, classId);
        
        // 验证任务是否存在
        LearningTasks task = learningTasksMapper.selectById(taskId);
        if (task == null) {
            log.error("初始化任务提交记录失败：任务不存在");
            return 0;
        }
        
        // 验证任务是否属于指定班级
        if (!Objects.equals(task.getClassId(), classId)) {
            log.error("初始化任务提交记录失败：任务不属于指定班级");
            return 0;
        }
        
        // 获取班级所有学生
        LambdaQueryWrapper<ClassMembers> membersQueryWrapper = new LambdaQueryWrapper<>();
        membersQueryWrapper.eq(ClassMembers::getClassId, classId);
        membersQueryWrapper.eq(ClassMembers::getDeleted, 0);
        List<ClassMembers> classMembers = classMembersMapper.selectList(membersQueryWrapper);
        
        if (classMembers.isEmpty()) {
            log.warn("班级没有学生，无需初始化任务提交记录");
            return 0;
        }
        
        // 为每个学生创建提交记录
        List<TaskSubmissions> submissionsList = new ArrayList<>();
        for (ClassMembers member : classMembers) {
            // 检查是否已有提交记录
            LambdaQueryWrapper<TaskSubmissions> existingQueryWrapper = new LambdaQueryWrapper<>();
            existingQueryWrapper.eq(TaskSubmissions::getTaskId, taskId);
            existingQueryWrapper.eq(TaskSubmissions::getStudentId, member.getStudentId());
            long count = baseMapper.selectCount(existingQueryWrapper);
            
            if (count == 0) {
                // 创建新提交记录
                TaskSubmissions submission = new TaskSubmissions();
                submission.setTaskId(taskId);
                submission.setStudentId(member.getStudentId());
                submission.setStatus("pending");
                submissionsList.add(submission);
            }
        }
        
        // 批量插入提交记录
        if (!submissionsList.isEmpty()) {
            for (TaskSubmissions submission : submissionsList) {
                baseMapper.insert(submission);
            }
            log.info("成功初始化{}条任务提交记录", submissionsList.size());
            return submissionsList.size();
        } else {
            log.info("所有学生已有提交记录，无需初始化");
            return 0;
        }
    }
}
