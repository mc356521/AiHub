package com.example.aihub.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.sql.Timestamp;

@Data
@TableName("learning_progress")
@Schema(description = "学习进度跟踪表，记录用户对每个章节的学习情况")
public class LearningProgress {
    @Schema(description = "进度记录唯一ID")
    private Integer id;

    @Schema(description = "学习者用户ID")
    private Integer userId;

    @Schema(description = "课程ID")
    private Integer courseId;

    @Schema(description = "章节标识键")
    private String chapterKey;

    @Schema(description = "学习状态")
    private String status;

    @Schema(description = "学习进度百分比", defaultValue = "0.00")
    private BigDecimal progressPercentage;

    @Schema(description = "阅读时长（秒）", defaultValue = "0")
    private Integer readingTimeSeconds;

    @Schema(description = "最后阅读位置（字符偏移量）", defaultValue = "0")
    private Integer lastReadPosition;

    @Schema(description = "首次访问时间")
    private Timestamp firstVisitTime;

    @Schema(description = "最后访问时间")
    private Timestamp lastVisitTime;

    @Schema(description = "完成时间")
    private Timestamp completionTime;

    @Schema(description = "创建时间")
    private Timestamp createTime;

    @Schema(description = "更新时间")
    private Timestamp updateTime;
} 