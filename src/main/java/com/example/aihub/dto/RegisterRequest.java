package com.example.aihub.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "用户注册请求体")
public class RegisterRequest {

    @Schema(description = "用户名", required = true, example = "newuser")
    private String username;

    @Schema(description = "密码", required = true, example = "password123")
    private String password;

    @Schema(description = "电子邮箱", required = true, example = "newuser@example.com")
    private String email;

    @Schema(description = "真实姓名", example = "张三")
    private String fullName;

    @Schema(description = "学号或教师编号", example = "20230001")
    private String userCode;

    @Schema(description = "用户头像的URL链接", example = "https://example.com/avatar.png")
    private String avatar;
} 