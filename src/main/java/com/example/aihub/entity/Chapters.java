package com.example.aihub.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.example.aihub.entity.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

/**
 * <p>
 * 章节解析缓存，用于快速查询和导航
 * </p>
 *
 * @author AIHub Code Generator
 * @since 2025-06-16
 */
@Getter
@Setter
@TableName("chapters")
@Schema(name = "Chapters", description = "章节解析缓存，用于快速查询和导航")
public class Chapters extends BaseEntity {

    @Schema(description = "课程ID")
    @TableField("course_id")
    private Integer courseId;

    @Schema(description = "父章节ID, 引用本表的id字段")
    @TableField("parent_id")
    private Integer parentId;

    @Schema(description = "章节标识（如: 1, 1.1, 1.2.1）")
    @TableField("chapter_key")
    private String chapterKey;

    @Schema(description = "层级深度 1,2,3...")
    @TableField("level")
    private Integer level;

    @Schema(description = "章节标题")
    @TableField("title")
    private String title;

    @Schema(description = "章节内容")
    @TableField("content")
    private String content;

    @Schema(description = "排序序号")
    @TableField("sort_order")
    private Integer sortOrder;

    @Schema(description = "起始行号")
    @TableField("line_start")
    private Integer lineStart;

    @Schema(description = "结束行号")
    @TableField("line_end")
    private Integer lineEnd;
}
