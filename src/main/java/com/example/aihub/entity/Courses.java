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
 * 课程基本信息和Markdown文件元数据
 * </p>
 *
 * @author AIHub Code Generator
 * @since 2025-06-15
 */
@Getter
@Setter
@TableName("courses")
@Schema(name = "Courses", description = "课程基本信息和Markdown文件元数据")
public class Courses extends BaseEntity {

    @Schema(description = "课程名称")
    @TableField("title")
    private String title;

    @Schema(description = "课程描述")
    @TableField("description")
    private String description;

    @Schema(description = "教师ID")
    @TableField("teacher_id")
    private Integer teacherId;

    @Schema(description = "Markdown文件路径")
    @TableField("file_path")
    private String filePath;

    @Schema(description = "文件内容哈希，用于检测变化")
    @TableField("file_hash")
    private String fileHash;

    @Schema(description = "文件最后修改时间")
    @TableField("file_updated_at")
    private LocalDateTime fileUpdatedAt;

    @Schema(description = "解析状态")
    @TableField("parse_status")
    private String parseStatus;

    @Schema(description = "解析错误信息")
    @TableField("parse_error")
    private String parseError;

    @Schema(description = "最后解析时间")
    @TableField("parsed_at")
    private LocalDateTime parsedAt;

    @Schema(description = "章节数量")
    @TableField("chapter_count")
    private Integer chapterCount;
}
