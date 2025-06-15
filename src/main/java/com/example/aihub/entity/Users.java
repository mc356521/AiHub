package com.example.aihub.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.example.aihub.entity.BaseEntity;
import java.time.LocalDateTime;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

/**
 * <p>
 * 存储所有系统用户，包括学生、教师和管理员
 * </p>
 *
 * @author AIHub Code Generator
 * @since 2025-06-15
 */
@Getter
@Setter
@TableName("users")
@Schema(name = "Users", description = "存储所有系统用户，包括学生、教师和管理员")
public class Users extends BaseEntity {

    @Schema(description = "用户名, 用于登录")
    @TableField("username")
    private String username;

    @Schema(description = "电子邮箱, 用于登录和通知")
    @TableField("email")
    private String email;

    @Schema(description = "学号或教师编号, 具有唯一性")
    @TableField("user_code")
    private String userCode;

    @Schema(description = "加密后的密码哈希值")
    @TableField("password_hash")
    private String passwordHash;

    @Schema(description = "用户真实姓名")
    @TableField("full_name")
    private String fullName;

    @Schema(description = "用户头像的URL链接")
    @TableField("avatar")
    private String avatar;

    @Schema(description = "用户角色: student, teacher, or admin")
    @TableField("role")
    private String role;
}
