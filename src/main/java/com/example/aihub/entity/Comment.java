package com.example.aihub.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * @author ikun
 * @createTime: 2025/6/19 20:29
 * @email: weijikun1@icloud.com
 */
@Data
@Schema(description = "评论内容实体类")
public class Comment {

    @Schema(description = "评论用户id")
    private String commentAuthorId;

    @Schema(description = "用户名称")
    private String commentAuthorName;

    @Schema(description = "评论内容")
    private String commentContent;

    @Schema(description = "评论时间")
    private LocalDateTime commentTime;

    @Schema(description = "评论图片")
    private List<String> commentImages;
}