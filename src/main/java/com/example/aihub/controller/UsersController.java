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
 * 用户管理控制器，提供查询用户等相关功能。
 *
 * @author AIHub Code Generator
 * @since 2025-06-15
 */
@Tag(name = "用户管理", description = "提供用户的增删改查功能")
@RestController
@RequestMapping("/users")
@Slf4j
public class UsersController extends BaseController {
    @Autowired
    private UsersService usersService;

    /**
     * 根据用户ID获取单个用户的详细信息。
     *
     * @param id 要查询的用户ID
     * @return 包含用户信息的Result响应；如果用户不存在，则返回失败。
     */
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

    /**
     * 获取所有用户的列表。
     *
     * @return 包含所有用户列表的Result响应。
     */
    @Operation(summary = "获取所有用户列表", description = "获取所有注册用户的列表")
    @GetMapping
    public Result<List<Users>> getAllUsers() {
        log.info("请求获取所有用户列表");
        List<Users> userList = usersService.list();
        return Result.success(userList, "获取所有用户成功");
    }
}
