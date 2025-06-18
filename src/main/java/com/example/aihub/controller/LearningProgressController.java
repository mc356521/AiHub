package com.example.aihub.controller;

import com.example.aihub.common.Result;
import com.example.aihub.dto.UpdateProgressRequest;
import com.example.aihub.entity.Users;
import com.example.aihub.service.LearningProgressService;
import com.example.aihub.service.UsersService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "学习进度管理", description = "用于更新和查询用户的学习进度")
@RestController
@RequestMapping("/progress")
@Slf4j
public class LearningProgressController extends BaseController {

    @Autowired
    private LearningProgressService learningProgressService;

    @Autowired
    private UsersService usersService;

    @Operation(summary = "更新学习进度", description = "更新当前用户对某一章节的学习状态。")
    @PostMapping("/update")
    @PreAuthorize("isAuthenticated()")
    public Result<?> updateProgress(@RequestBody UpdateProgressRequest request) {
        log.info("请求更新学习进度: {}", request);
        
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (!(principal instanceof UserDetails)) {
            return Result.failed("用户未登录或认证信息不正确");
        }
        String username = ((UserDetails) principal).getUsername();
        Users currentUser = usersService.findByUsername(username);
        if (currentUser == null) {
            return Result.failed("无法找到当前登录用户的数据");
        }

        learningProgressService.updateProgress(currentUser.getId(), request);
        log.info("用户 {} 对章节 {} 的进度更新成功", currentUser.getUsername(), request.getChapterId());
        return Result.success(null, "进度更新成功");
    }
} 