package com.example.aihub.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * @created: ii_kun
 * @createTime: 2025/6/17 11:17
 * @email: weijikun1@icloud.com
 */
@Data
@Schema(description = "创建班级请求对象")
public class ClassesRequest {
    @Schema(description = "班级id")
    private Integer id;

    @Schema(description = "班级名称",example = "软件一班")
    private String name;

    @Schema(description = "外键, 关联到courses表的课程ID (可选)")
    private Integer courseId;

    @Schema(description = "外键, 关联到semesters表的学期ID")
    private String semesterId;

    @Schema(description = "班级状态 (pending: 未开课, active: 进行中, finished: 已结课, archived: 已归档)")
    private String status;
}
