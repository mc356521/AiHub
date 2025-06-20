package com.example.aihub.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.example.aihub.entity.BaseEntity;
import java.time.LocalDateTime;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

/**
 * <p>
 * 记录学生对学习任务的完成情况
 * </p>
 *
 * @author AIHub Code Generator
 * @since 2025-06-20
 */
@Getter
@Setter
@TableName("task_submissions")
@Schema(name = "TaskSubmissions", description = "记录学生对学习任务的完成情况")
public class TaskSubmissions extends BaseEntity {

    @Schema(description = "外键, 关联到learning_tasks表的任务ID")
    @TableField("task_id")
    private Integer taskId;

    @Schema(description = "外键, 关联到users表的学生ID")
    @TableField("student_id")
    private Integer studentId;

    @Schema(description = "提交状态 (pending: 待完成, completed: 已完成, overdue: 已逾期)")
    @TableField("status")
    private String status;

    @Schema(description = "学生标记完成的时间")
    @TableField("completed_at")
    private LocalDateTime completedAt;

    @Schema(description = "学生提交的简短内容或笔记")
    @TableField("content")
    private String content;
}
