package com.example.aihub.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.aihub.dto.UpdateProgressRequest;
import com.example.aihub.entity.LearningProgress;
import com.example.aihub.mapper.LearningProgressMapper;
import com.example.aihub.service.LearningProgressService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.sql.Timestamp;
import java.time.Instant;

@Service
public class LearningProgressServiceImpl extends ServiceImpl<LearningProgressMapper, LearningProgress> implements LearningProgressService {

    @Override
    @Transactional
    public void updateProgress(Integer userId, UpdateProgressRequest request) {
        LambdaQueryWrapper<LearningProgress> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(LearningProgress::getUserId, userId)
                    .eq(LearningProgress::getCourseId, request.getCourseId())
                    .eq(LearningProgress::getChapterId, request.getChapterId());
        
        LearningProgress progress = getOne(queryWrapper);

        if (progress == null) {
            // 如果不存在，则创建新的进度记录
            progress = new LearningProgress();
            progress.setUserId(userId);
            progress.setCourseId(request.getCourseId());
            progress.setChapterId(request.getChapterId());
            progress.setFirstVisitTime(Timestamp.from(Instant.now()));
        }

        // 更新状态和时间
        progress.setStatus(request.getStatus());
        progress.setLastVisitTime(Timestamp.from(Instant.now()));

        if ("completed".equals(request.getStatus()) && progress.getCompletionTime() == null) {
            progress.setCompletionTime(Timestamp.from(Instant.now()));
            progress.setProgressPercentage(java.math.BigDecimal.valueOf(100.0));
        }

        saveOrUpdate(progress);
    }
} 