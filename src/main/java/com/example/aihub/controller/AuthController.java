package com.example.aihub.controller;

import com.example.aihub.common.Result;
import com.example.aihub.dto.LoginRequest;
import com.example.aihub.dto.RegisterRequest;
import com.example.aihub.service.UsersService;
import com.example.aihub.util.JwtUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
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
@Slf4j
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
        log.info("用户尝试登录: {}", loginRequest.getUsername());
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword())
            );
            SecurityContextHolder.getContext().setAuthentication(authentication);

            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            String token = jwtUtil.generateToken(userDetails);

            Map<String, String> tokenMap = new HashMap<>();
            tokenMap.put("token", token);

            log.info("用户登录成功: {}", userDetails.getUsername());
            return Result.success(tokenMap, "登录成功");
        } catch (BadCredentialsException e) {
            log.warn("用户登录失败，用户名或密码错误: {}", loginRequest.getUsername());
            return Result.failed("用户名或密码错误");
        } catch (Exception e) {
            log.error("登录过程中发生未知错误，用户: {}", loginRequest.getUsername(), e);
            return Result.failed("认证失败: " + e.getMessage());
        }
    }

    @PostMapping("/register")
    @Operation(summary = "用户注册", description = "提供新用户注册功能")
    public Result<Void> register(@RequestBody RegisterRequest registerRequest) {
        log.info("新用户尝试注册: {}", registerRequest.getUsername());
        try {
            usersService.register(registerRequest);
            log.info("用户注册成功: {}", registerRequest.getUsername());
            return Result.success(null, "注册成功");
        } catch (Exception e) {
            log.error("用户注册失败: {}", registerRequest.getUsername(), e);
            return Result.failed(e.getMessage());
        }
    }
} 