package com.example.aihub.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 创建学习任务请求DTO
 */
@Data
@Schema(description = "创建学习任务请求")
public class CreateTaskRequest {

    @Schema(description = "班级ID")
    private Integer classId;

    @Schema(description = "任务标题")
    private String title;

    @Schema(description = "任务详细描述")
    private String description;

    @Schema(description = "任务截止日期")
    private LocalDateTime dueDate;

    @Schema(description = "任务状态 (draft: 草稿, published: 已发布, closed: 已关闭)")
    private String status;

    @Schema(description = "关联的章节ID (可选)")
    private Integer relatedChapterId;

    @Schema(description = "关联的资源ID (可选)")
    private Integer relatedResourceId;
} 