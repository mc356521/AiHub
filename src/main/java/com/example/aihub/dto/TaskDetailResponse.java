package com.example.aihub.dto;

import com.example.aihub.entity.LearningTasks;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 学习任务详情响应DTO
 */
@Data
@Schema(description = "学习任务详情响应")
public class TaskDetailResponse {

    @Schema(description = "任务ID")
    private Integer id;

    @Schema(description = "班级ID")
    private Integer classId;

    @Schema(description = "班级名称")
    private String className;

    @Schema(description = "创建者ID")
    private Integer creatorId;

    @Schema(description = "创建者姓名")
    private String creatorName;

    @Schema(description = "任务标题")
    private String title;

    @Schema(description = "任务详细描述")
    private String description;

    @Schema(description = "任务截止日期")
    private LocalDateTime dueDate;

    @Schema(description = "任务状态 (draft: 草稿, published: 已发布, closed: 已关闭)")
    private String status;

    @Schema(description = "关联的章节ID")
    private Integer relatedChapterId;

    @Schema(description = "关联的章节标题")
    private String relatedChapterTitle;

    @Schema(description = "关联的资源ID")
    private Integer relatedResourceId;

    @Schema(description = "关联的资源标题")
    private String relatedResourceTitle;

    @Schema(description = "提交总数")
    private Integer totalSubmissions;

    @Schema(description = "已完成提交数")
    private Integer completedSubmissions;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @Schema(description = "更新时间")
    private LocalDateTime updateTime;
} 