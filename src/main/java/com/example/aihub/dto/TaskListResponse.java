package com.example.aihub.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 学习任务列表项响应DTO
 */
@Data
@Schema(description = "学习任务列表项响应")
public class TaskListResponse {

    @Schema(description = "任务ID")
    private Integer id;

    @Schema(description = "班级ID")
    private Integer classId;

    @Schema(description = "班级名称")
    private String className;

    @Schema(description = "任务标题")
    private String title;

    @Schema(description = "任务状态 (draft: 草稿, published: 已发布, closed: 已关闭)")
    private String status;

    @Schema(description = "任务截止日期")
    private LocalDateTime dueDate;

    @Schema(description = "提交情况 (例如：35/48人)")
    private String submissionStatus;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;
} 