package com.example.aihub.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "用于更新学习进度的请求体")
public class UpdateProgressRequest {

    @Schema(description = "课程ID", required = true)
    private Integer courseId;

    @Schema(description = "章节的唯一标识 (chapter_key)", required = true)
    private String chapterKey;

    @Schema(description = "新的学习状态 (e.g., 'in_progress', 'completed')")
    private String status;
    
    @Schema(description = "学习进度百分比 (0-100)")
    private Double progressPercentage;
    
    @Schema(description = "增加的阅读时长（秒）")
    private Integer readingTimeSeconds;
    
    @Schema(description = "最后阅读位置（字符偏移量）")
    private Integer lastReadPosition;
    
    @Schema(description = "是否标记为已完成")
    private Boolean isCompleted;
} 