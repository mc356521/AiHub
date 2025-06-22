package com.example.aihub.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.example.aihub.entity.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * <p>
 * 存储上传的教学资源，如课件、视频、文档等
 * </p>
 *
 * @author AIHub Code Generator
 * @since 2025-06-21
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("resources")
@Schema(name = "Resources", description = "存储上传的教学资源，如课件、视频、文档等")
public class Resources extends BaseEntity {

    @Schema(description = "资源标题")
    @TableField("title")
    private String title;

    @Schema(description = "类型")
    @TableField("file_type")
    private String fileType;

    @Schema(description = "文件在服务器上的存储路径")
    @TableField("file_path")
    private String filePath;

    @Schema(description = "文件大小 (Bytes)")
    @TableField("file_size")
    private Long fileSize;

    @Schema(description = "视频时长 (秒)")
    @TableField("duration")
    private Integer duration;

    @Schema(description = "外键, 关联到users表的上传者ID")
    @TableField("uploader_id")
    private Integer uploaderId;

    @Schema(description = "外键, 关联到courses表的课程ID (可选)")
    @TableField("course_id")
    private Integer courseId;
}
