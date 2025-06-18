package com.example.aihub.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "加入班级请求")
public class JoinClassRequest {

    @Schema(description = "班级口令/邀请码", requiredMode = Schema.RequiredMode.REQUIRED)
    private String classCode;
} 