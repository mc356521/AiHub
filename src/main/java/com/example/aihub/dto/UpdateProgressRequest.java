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

    @Schema(description = "新的学习状态 (e.g., 'in_progress', 'completed')", required = true)
    private String status;
    
    // 可以根据需要添加其他要更新的字段，例如阅读时长等
} 