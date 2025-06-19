package com.example.aihub.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.aihub.dto.UpdateProgressRequest;
import com.example.aihub.entity.LearningProgress;
import com.example.aihub.mapper.LearningProgressMapper;
import com.example.aihub.service.LearningProgressService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class LearningProgressServiceImpl extends ServiceImpl<LearningProgressMapper, LearningProgress> implements LearningProgressService {

    @Override
    @Transactional
    public LearningProgress updateProgress(Integer userId, UpdateProgressRequest request) {
        log.info("更新用户学习进度: userId={}, courseId={}, chapterKey={}, status={}",
                userId, request.getCourseId(), request.getChapterKey(), request.getStatus());
        
        LambdaQueryWrapper<LearningProgress> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(LearningProgress::getUserId, userId)
                    .eq(LearningProgress::getCourseId, request.getCourseId())
                    .eq(LearningProgress::getChapterKey, request.getChapterKey());
        
        LearningProgress progress = getOne(queryWrapper);

        if (progress == null) {
            // 如果不存在，则创建新的进度记录
            progress = new LearningProgress();
            progress.setUserId(userId);
            progress.setCourseId(request.getCourseId());
            progress.setChapterKey(request.getChapterKey());
            progress.setProgressPercentage(BigDecimal.ZERO);
            progress.setReadingTimeSeconds(0);
            progress.setFirstVisitTime(Timestamp.from(Instant.now()));
        }

        // 更新状态和时间
        if (request.getStatus() != null) {
            progress.setStatus(request.getStatus());
        }
        progress.setLastVisitTime(Timestamp.from(Instant.now()));

        // 更新进度百分比（如果提供）
        if (request.getProgressPercentage() != null) {
            progress.setProgressPercentage(BigDecimal.valueOf(request.getProgressPercentage()));
        }
        
        // 更新阅读时长（如果提供）
        if (request.getReadingTimeSeconds() != null) {
            Integer currentSeconds = progress.getReadingTimeSeconds() != null ? 
                                    progress.getReadingTimeSeconds() : 0;
            progress.setReadingTimeSeconds(currentSeconds + request.getReadingTimeSeconds());
        }
        
        // 更新最后阅读位置（如果提供）
        if (request.getLastReadPosition() != null) {
            progress.setLastReadPosition(request.getLastReadPosition());
        }

        // 如果进度标记为已完成
        boolean isCompleted = "completed".equals(request.getStatus()) || Boolean.TRUE.equals(request.getIsCompleted());
        if (isCompleted && progress.getCompletionTime() == null) {
            progress.setCompletionTime(Timestamp.from(Instant.now()));
            progress.setProgressPercentage(BigDecimal.valueOf(100.0));
            progress.setStatus("completed");
        }

        saveOrUpdate(progress);
        return progress;
    }
    
    @Override
    public List<LearningProgress> getCourseProgress(Integer userId, Integer courseId) {
        log.info("获取课程学习进度: userId={}, courseId={}", userId, courseId);
        
        LambdaQueryWrapper<LearningProgress> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(LearningProgress::getUserId, userId)
                   .eq(LearningProgress::getCourseId, courseId);
        
        return list(queryWrapper);
    }
    
    @Override
    public LearningProgress getChapterProgress(Integer userId, Integer courseId, String chapterKey) {
        log.info("获取章节学习进度: userId={}, courseId={}, chapterKey={}", 
                userId, courseId, chapterKey);
        
        LambdaQueryWrapper<LearningProgress> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(LearningProgress::getUserId, userId)
                   .eq(LearningProgress::getCourseId, courseId)
                   .eq(LearningProgress::getChapterKey, chapterKey);
        
        return getOne(queryWrapper);
    }
    
    @Override
    @Transactional
    public boolean batchUpdateProgress(Integer userId, List<UpdateProgressRequest> progressRecords) {
        log.info("批量更新学习进度: userId={}, recordsCount={}", userId, progressRecords.size());
        
        List<LearningProgress> successRecords = new ArrayList<>();
        
        try {
            for (UpdateProgressRequest record : progressRecords) {
                LearningProgress updated = updateProgress(userId, record);
                successRecords.add(updated);
            }
            return true;
        } catch (Exception e) {
            log.error("批量更新学习进度失败", e);
            return false;
        }
    }
} 