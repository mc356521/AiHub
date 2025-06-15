package com.example.aihub.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "登录请求体")
public class LoginRequest {

    @Schema(description = "用户名", required = true, example = "testuser")
    private String username;

    @Schema(description = "密码", required = true, example = "password123")
    private String password;
} 