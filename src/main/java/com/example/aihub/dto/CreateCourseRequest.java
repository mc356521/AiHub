package com.example.aihub.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "创建课程请求对象")
public class CreateCourseRequest {

    @Schema(description = "课程名称", required = true)
    private String title;

    @Schema(description = "课程描述")
    private String description;
} 