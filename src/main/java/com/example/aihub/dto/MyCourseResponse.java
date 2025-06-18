package com.example.aihub.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "我的课程信息响应")
public class MyCourseResponse {

    @Schema(description = "课程ID")
    private Integer courseId;

    @Schema(description = "课程标题")
    private String courseTitle;

    @Schema(description = "课程描述")
    private String courseDescription;

    @Schema(description = "教师姓名")
    private String teacherName;

    @Schema(description = "教师头像URL")
    private String teacherAvatar;

    @Schema(description = "班级状态 (pending: 未开课, active: 进行中, finished: 已结课, archived: 已归档)")
    private String classStatus;

    @Schema(description = "班级ID")
    private Integer classId;

    @Schema(description = "班级名称")
    private String className;
} 