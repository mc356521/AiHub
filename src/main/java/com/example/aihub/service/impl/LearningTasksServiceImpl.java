package com.example.aihub.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.aihub.dto.CreateTaskRequest;
import com.example.aihub.dto.TaskDetailResponse;
import com.example.aihub.dto.TaskListResponse;
import com.example.aihub.dto.UpdateTaskRequest;
import com.example.aihub.entity.Chapters;
import com.example.aihub.entity.ClassesEntity;
import com.example.aihub.entity.LearningTasks;
import com.example.aihub.entity.TaskSubmissions;
import com.example.aihub.entity.Users;
import com.example.aihub.mapper.ChaptersMapper;
import com.example.aihub.mapper.ClassesMapper;
import com.example.aihub.mapper.LearningTasksMapper;
import com.example.aihub.mapper.TaskSubmissionsMapper;
import com.example.aihub.mapper.UsersMapper;
import com.example.aihub.service.LearningTasksService;
import com.example.aihub.service.TaskSubmissionsService;
import com.example.aihub.service.UsersService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * <p>
 * 教师发布的学习任务，可关联章节、资源等，有截止日期 服务实现类
 * </p>
 *
 * @author AIHub Code Generator
 * @since 2025-06-20
 */
@Service
@Slf4j
public class LearningTasksServiceImpl extends ServiceImpl<LearningTasksMapper, LearningTasks> implements LearningTasksService {

    @Autowired
    private UsersService usersService;

    @Autowired
    private ClassesMapper classesMapper;

    @Autowired
    private ChaptersMapper chaptersMapper;

    @Autowired
    private UsersMapper usersMapper;

    @Autowired
    private TaskSubmissionsMapper taskSubmissionsMapper;
    
    @Autowired
    private TaskSubmissionsService taskSubmissionsService;

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
    public Integer createTask(CreateTaskRequest request) {
        log.info("创建学习任务：{}", request);
        
        // 获取当前登录用户作为创建者
        Users currentUser = getCurrentUser();
        
        // 验证班级是否存在且当前用户是否是该班级的教师
        ClassesEntity classEntity = classesMapper.selectById(request.getClassId());
        if (classEntity == null || !Objects.equals(classEntity.getTeacherId(), currentUser.getId())) {
            log.error("创建任务失败：班级不存在或当前用户不是该班级的教师");
            return null;
        }
        
        // 创建任务实体
        LearningTasks task = new LearningTasks();
        BeanUtils.copyProperties(request, task);
        task.setCreatorId(currentUser.getId());
        
        // 保存任务
        baseMapper.insert(task);
        log.info("学习任务创建成功，ID：{}", task.getId());
        
        // 如果是已发布状态，初始化学生提交记录
        if ("published".equals(task.getStatus())) {
            log.info("任务是已发布状态，初始化学生提交记录");
            taskSubmissionsService.initializeTaskSubmissions(task.getId(), task.getClassId());
        }
        
        return task.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateTask(UpdateTaskRequest request) {
        log.info("更新学习任务：{}", request);
        
        // 获取当前登录用户
        Users currentUser = getCurrentUser();
        
        // 验证任务是否存在且当前用户是否是任务创建者
        LearningTasks existingTask = baseMapper.selectById(request.getId());
        if (existingTask == null) {
            log.error("更新任务失败：任务不存在");
            return false;
        }
        
        if (!Objects.equals(existingTask.getCreatorId(), currentUser.getId())) {
            log.error("更新任务失败：当前用户不是任务创建者");
            return false;
        }
        
        // 检查是否从草稿变为已发布
        boolean isPublishing = "draft".equals(existingTask.getStatus()) && "published".equals(request.getStatus());
        
        // 更新任务
        LearningTasks task = new LearningTasks();
        BeanUtils.copyProperties(request, task);
        
        // 保留原有的不可修改字段
        task.setCreatorId(existingTask.getCreatorId());
        task.setClassId(existingTask.getClassId());
        
        // 更新任务
        int result = baseMapper.updateById(task);
        
        // 如果从草稿变为已发布，初始化学生提交记录
        if (isPublishing && result > 0) {
            log.info("任务状态从草稿变为已发布，初始化学生提交记录");
            taskSubmissionsService.initializeTaskSubmissions(task.getId(), task.getClassId());
        }
        
        log.info("学习任务更新结果：{}", result > 0);
        return result > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteTask(Integer taskId) {
        log.info("删除学习任务，ID：{}", taskId);
        
        // 获取当前登录用户
        Users currentUser = getCurrentUser();
        
        // 验证任务是否存在且当前用户是否是任务创建者
        LearningTasks existingTask = baseMapper.selectById(taskId);
        if (existingTask == null) {
            log.error("删除任务失败：任务不存在");
            return false;
        }
        
        if (!Objects.equals(existingTask.getCreatorId(), currentUser.getId())) {
            log.error("删除任务失败：当前用户不是任务创建者");
            return false;
        }
        
        // 删除任务（逻辑删除）
        int result = baseMapper.deleteById(taskId);
        log.info("学习任务删除结果：{}", result > 0);
        return result > 0;
    }

    @Override
    public TaskDetailResponse getTaskDetail(Integer taskId) {
        log.info("获取学习任务详情，ID：{}", taskId);
        
        // 获取任务基本信息
        LearningTasks task = baseMapper.selectById(taskId);
        if (task == null) {
            log.error("获取任务详情失败：任务不存在");
            return null;
        }
        
        // 转换为DTO
        TaskDetailResponse response = new TaskDetailResponse();
        BeanUtils.copyProperties(task, response);
        
        // 获取班级信息
        ClassesEntity classEntity = classesMapper.selectById(task.getClassId());
        if (classEntity != null) {
            response.setClassName(classEntity.getName());
        }
        
        // 获取创建者信息
        Users creator = usersMapper.selectById(task.getCreatorId());
        if (creator != null) {
            response.setCreatorName(creator.getFullName());
        }
        
        // 获取章节信息（如果有）
        if (task.getRelatedChapterId() != null) {
            Chapters chapter = chaptersMapper.selectById(task.getRelatedChapterId());
            if (chapter != null) {
                response.setRelatedChapterTitle(chapter.getTitle());
            }
        }
        
        // 获取提交统计信息
        LambdaQueryWrapper<TaskSubmissions> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(TaskSubmissions::getTaskId, taskId);
        queryWrapper.eq(TaskSubmissions::getDeleted, 0);
        
        // 计算总提交数
        Long totalSubmissionsCount = taskSubmissionsMapper.selectCount(queryWrapper);
        response.setTotalSubmissions(totalSubmissionsCount.intValue());
        
        // 计算已完成提交数
        LambdaQueryWrapper<TaskSubmissions> completedQueryWrapper = new LambdaQueryWrapper<>();
        completedQueryWrapper.eq(TaskSubmissions::getTaskId, taskId);
        completedQueryWrapper.eq(TaskSubmissions::getStatus, "completed");
        completedQueryWrapper.eq(TaskSubmissions::getDeleted, 0);
        Long completedSubmissionsCount = taskSubmissionsMapper.selectCount(completedQueryWrapper);
        response.setCompletedSubmissions(completedSubmissionsCount.intValue());
        
        log.info("获取学习任务详情成功");
        return response;
    }

    @Override
    public IPage<TaskListResponse> getTeacherTasks(Page<TaskListResponse> page, Integer classId, String status) {
        log.info("分页获取教师任务列表，classId：{}，status：{}", classId, status);
        
        // 获取当前登录用户
        Users currentUser = getCurrentUser();
        
        // 查询当前教师创建的任务
        LambdaQueryWrapper<LearningTasks> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(LearningTasks::getCreatorId, currentUser.getId());
        
        // 根据班级ID筛选（如果有）
        if (classId != null) {
            queryWrapper.eq(LearningTasks::getClassId, classId);
        }
        
        // 根据状态筛选（如果有）
        if (StringUtils.hasText(status)) {
            queryWrapper.eq(LearningTasks::getStatus, status);
        }
        
        // 按创建时间降序排序
        queryWrapper.orderByDesc(LearningTasks::getCreateTime);
        
        // 执行分页查询
        Page<LearningTasks> taskPage = new Page<>(page.getCurrent(), page.getSize());
        Page<LearningTasks> taskResult = baseMapper.selectPage(taskPage, queryWrapper);
        
        // 转换结果为DTO
        Page<TaskListResponse> resultPage = new Page<>(taskResult.getCurrent(), taskResult.getSize(), taskResult.getTotal());
        List<TaskListResponse> resultRecords = new ArrayList<>();
        
        for (LearningTasks task : taskResult.getRecords()) {
            TaskListResponse item = new TaskListResponse();
            BeanUtils.copyProperties(task, item);
            
            // 获取班级名称
            ClassesEntity classEntity = classesMapper.selectById(task.getClassId());
            if (classEntity != null) {
                item.setClassName(classEntity.getName());
            }
            
            // 计算提交情况
            LambdaQueryWrapper<TaskSubmissions> submissionQueryWrapper = new LambdaQueryWrapper<>();
            submissionQueryWrapper.eq(TaskSubmissions::getTaskId, task.getId());
            submissionQueryWrapper.eq(TaskSubmissions::getDeleted, 0);
            Long totalSubmissionsCount = taskSubmissionsMapper.selectCount(submissionQueryWrapper);
            
            // 获取已完成提交数
            LambdaQueryWrapper<TaskSubmissions> completedQueryWrapper = new LambdaQueryWrapper<>();
            completedQueryWrapper.eq(TaskSubmissions::getTaskId, task.getId());
            completedQueryWrapper.eq(TaskSubmissions::getStatus, "completed");
            completedQueryWrapper.eq(TaskSubmissions::getDeleted, 0);
            Long completedSubmissionsCount = taskSubmissionsMapper.selectCount(completedQueryWrapper);
            
            // 设置提交情况
            item.setSubmissionStatus(completedSubmissionsCount.intValue() + "/" + totalSubmissionsCount.intValue() + "人");
            
            resultRecords.add(item);
        }
        
        resultPage.setRecords(resultRecords);
        log.info("获取教师任务列表成功，总数：{}", resultPage.getTotal());
        return resultPage;
    }

    @Override
    public List<TaskListResponse> getTasksByClassId(Integer classId) {
        log.info("获取班级任务列表，classId：{}", classId);
        
        // 查询班级下的所有任务
        LambdaQueryWrapper<LearningTasks> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(LearningTasks::getClassId, classId);
        queryWrapper.eq(LearningTasks::getStatus, "published"); // 只查询已发布的任务
        queryWrapper.orderByDesc(LearningTasks::getCreateTime);
        
        List<LearningTasks> tasks = baseMapper.selectList(queryWrapper);
        List<TaskListResponse> result = new ArrayList<>();
        
        for (LearningTasks task : tasks) {
            TaskListResponse item = new TaskListResponse();
            BeanUtils.copyProperties(task, item);
            
            // 获取班级名称
            ClassesEntity classEntity = classesMapper.selectById(task.getClassId());
            if (classEntity != null) {
                item.setClassName(classEntity.getName());
            }
            
            // 计算提交情况
            LambdaQueryWrapper<TaskSubmissions> submissionQueryWrapper = new LambdaQueryWrapper<>();
            submissionQueryWrapper.eq(TaskSubmissions::getTaskId, task.getId());
            submissionQueryWrapper.eq(TaskSubmissions::getDeleted, 0);
            Long totalSubmissionsCount = taskSubmissionsMapper.selectCount(submissionQueryWrapper);
            
            LambdaQueryWrapper<TaskSubmissions> completedQueryWrapper = new LambdaQueryWrapper<>();
            completedQueryWrapper.eq(TaskSubmissions::getTaskId, task.getId());
            completedQueryWrapper.eq(TaskSubmissions::getStatus, "completed");
            completedQueryWrapper.eq(TaskSubmissions::getDeleted, 0);
            Long completedSubmissionsCount = taskSubmissionsMapper.selectCount(completedQueryWrapper);
            
            // 设置提交情况
            item.setSubmissionStatus(completedSubmissionsCount.intValue() + "/" + totalSubmissionsCount.intValue() + "人");
            
            result.add(item);
        }
        
        log.info("获取班级任务列表成功，总数：{}", result.size());
        return result;
    }
}
