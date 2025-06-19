package com.example.aihub.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.aihub.dto.UpdateProgressRequest;
import com.example.aihub.entity.LearningProgress;

import java.util.List;

public interface LearningProgressService extends IService<LearningProgress> {
    
    /**
     * 更新学习进度
     * 
     * @param userId 用户ID
     * @param request 进度更新请求
     * @return 更新后的进度记录
     */
    LearningProgress updateProgress(Integer userId, UpdateProgressRequest request);
    
    /**
     * 获取用户在特定课程中的所有学习进度
     * 
     * @param userId 用户ID
     * @param courseId 课程ID
     * @return 该用户在该课程下的所有学习进度记录
     */
    List<LearningProgress> getCourseProgress(Integer userId, Integer courseId);
    
    /**
     * 获取用户在特定章节中的学习进度
     * 
     * @param userId 用户ID
     * @param courseId 课程ID
     * @param chapterKey 章节标识键
     * @return 学习进度记录，如果不存在则返回null
     */
    LearningProgress getChapterProgress(Integer userId, Integer courseId, String chapterKey);
    
    /**
     * 批量更新学习进度
     * 
     * @param userId 用户ID
     * @param progressRecords 需要更新的进度记录列表
     * @return 更新是否成功
     */
    boolean batchUpdateProgress(Integer userId, List<UpdateProgressRequest> progressRecords);
} 