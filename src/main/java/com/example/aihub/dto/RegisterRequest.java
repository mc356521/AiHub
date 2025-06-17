package com.example.aihub.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "用户注册请求体")
public class RegisterRequest {

    @Schema(description = "用户名", required = true, example = "newuser")
    private String username;

    @Schema(description = "密码", required = true, example = "cbg356521")
    private String password;

    @Schema(description = "电子邮箱", required = true, example = "newuser@example.com")
    private String email;

    @Schema(description = "真实姓名", example = "张三")
    private String fullName;

    @Schema(description = "学号或教师编号", example = "20230001")
    private String userCode;

    @Schema(description = "用户角色 (student 或 teacher)", example = "student", defaultValue = "student", allowableValues = {"student", "teacher"})
    private String role;
} 