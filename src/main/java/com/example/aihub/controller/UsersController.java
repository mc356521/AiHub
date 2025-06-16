package com.example.aihub.controller;

import com.example.aihub.common.Result;
import com.example.aihub.entity.Users;
import com.example.aihub.service.UsersService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * <p>
 * 存储所有系统用户，包括学生、教师和管理员 前端控制器
 * </p>
 *
 * @author AIHub Code Generator
 * @since 2025-06-15
 */
@Tag(name = "用户管理", description = "提供用户的增删改查功能")
@RestController
@RequestMapping("/api/users")
@Slf4j
public class UsersController extends BaseController {
    @Autowired
    private UsersService usersService;

    @Operation(summary = "根据ID获取用户", description = "根据用户ID获取单个用户详细信息")
    @GetMapping("/{id}")
    public Result<Users> getUserById(@PathVariable Long id) {
        log.info("开始请求获取用户，用户ID: {}", id);
        Users user = usersService.getById(id);
        if (user != null) {
            log.debug("成功获取到用户: {}", user.toString());
            return Result.success(user, "获取用户成功");
        } else {
            log.warn("尝试获取一个不存在的用户，ID: {}", id);
            return Result.failed("用户不存在");
        }
    }

    @Operation(summary = "获取所有用户列表", description = "获取所有注册用户的列表")
    @GetMapping
    public Result<List<Users>> getAllUsers() {
        log.info("请求获取所有用户列表");
        List<Users> userList = usersService.list();
        return Result.success(userList, "获取所有用户成功");
    }
}
