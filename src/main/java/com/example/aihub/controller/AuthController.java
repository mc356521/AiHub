package com.example.aihub.controller;

import com.example.aihub.common.Result;
import com.example.aihub.dto.LoginRequest;
import com.example.aihub.dto.RegisterRequest;
import com.example.aihub.service.UsersService;
import com.example.aihub.util.JwtUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/auth")
@Tag(name = "用户认证", description = "提供用户登录和注册功能")
public class AuthController extends BaseController {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private UsersService usersService;

    @PostMapping("/login")
    @Operation(summary = "用户登录", description = "用户使用用户名和密码进行登录认证，成功后返回JWT")
    public Result<Map<String, String>> login(@RequestBody LoginRequest loginRequest) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword())
            );
            SecurityContextHolder.getContext().setAuthentication(authentication);

            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            String token = jwtUtil.generateToken(userDetails);

            Map<String, String> tokenMap = new HashMap<>();
            tokenMap.put("token", token);

            return Result.success(tokenMap, "登录成功");
        } catch (Exception e) {
            return Result.failed("认证失败: " + e.getMessage());
        }
    }

    @PostMapping("/register")
    @Operation(summary = "用户注册", description = "提供新用户注册功能")
    public Result<Void> register(@RequestBody RegisterRequest registerRequest) {
        try {
            usersService.register(registerRequest);
            return Result.success(null, "注册成功");
        } catch (Exception e) {
            return Result.failed(e.getMessage());
        }
    }
} 