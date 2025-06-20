package com.example.aihub.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 学习任务提交请求DTO
 */
@Data
@Schema(description = "学习任务提交请求")
public class TaskSubmissionRequest {

    @Schema(description = "任务ID")
    private Integer taskId;

    @Schema(description = "提交内容")
    private String content;
} 