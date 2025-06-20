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
 * 教师发布的学习任务，可关联章节、资源等，有截止日期
 * </p>
 *
 * @author AIHub Code Generator
 * @since 2025-06-20
 */
@Getter
@Setter
@TableName("learning_tasks")
@Schema(name = "LearningTasks", description = "教师发布的学习任务，可关联章节、资源等，有截止日期")
public class LearningTasks extends BaseEntity {

    @Schema(description = "外键, 关联到classes表的班级ID")
    @TableField("class_id")
    private Integer classId;

    @Schema(description = "外键, 关联到users表的创建者（教师）ID")
    @TableField("creator_id")
    private Integer creatorId;

    @Schema(description = "任务标题")
    @TableField("title")
    private String title;

    @Schema(description = "任务详细描述")
    @TableField("description")
    private String description;

    @Schema(description = "任务截止日期")
    @TableField("due_date")
    private LocalDateTime dueDate;

    @Schema(description = "任务状态 (draft: 草稿, published: 已发布, closed: 已关闭)")
    @TableField("status")
    private String status;

    @Schema(description = "关联的章节ID, 外键关联到chapters表")
    @TableField("related_chapter_id")
    private Integer relatedChapterId;

    @Schema(description = "关联的资源ID, 外键关联到resources表")
    @TableField("related_resource_id")
    private Integer relatedResourceId;
}
