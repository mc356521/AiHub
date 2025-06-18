package com.example.aihub.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.aihub.dto.UpdateProgressRequest;
import com.example.aihub.entity.LearningProgress;

public interface LearningProgressService extends IService<LearningProgress> {
    void updateProgress(Integer userId, UpdateProgressRequest request);
} 