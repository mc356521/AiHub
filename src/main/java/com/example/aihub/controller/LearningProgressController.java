package com.example.aihub.controller;

import com.example.aihub.common.Result;
import com.example.aihub.dto.UpdateProgressRequest;
import com.example.aihub.entity.LearningProgress;
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
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Tag(name = "学习进度管理", description = "用于更新和查询用户的学习进度")
@RestController
@RequestMapping("/learning-progress")
@Slf4j
public class LearningProgressController extends BaseController {

    @Autowired
    private LearningProgressService learningProgressService;

    @Autowired
    private UsersService usersService;

    /**
     * 获取用户在特定课程中的所有学习进度
     * 
     * @param courseId 课程ID
     * @return 学习进度记录列表
     */
    @Operation(summary = "获取课程学习进度", description = "获取当前用户在指定课程的所有章节学习进度")
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public Result<List<LearningProgress>> getCourseProgress(@RequestParam Integer courseId) {
        log.info("请求获取课程进度: courseId={}", courseId);
        
        Users currentUser = getCurrentUser();
        if (currentUser == null) {
            return Result.failed("用户未登录或认证信息不正确");
        }

        List<LearningProgress> progressList = learningProgressService.getCourseProgress(
            currentUser.getId(), courseId);
        
        log.info("成功获取课程进度: userId={}, courseId={}, count={}", 
            currentUser.getId(), courseId, progressList.size());
        return Result.success(progressList, "获取课程进度成功");
    }

    /**
     * 获取用户在特定章节的学习进度
     * 
     * @param courseId 课程ID
     * @param chapterKey 章节标识键
     * @return 学习进度记录
     */
    @Operation(summary = "获取章节学习进度", description = "获取当前用户在指定章节的学习进度")
    @GetMapping("/chapter")
    @PreAuthorize("isAuthenticated()")
    public Result<LearningProgress> getChapterProgress(
            @RequestParam Integer courseId,
            @RequestParam String chapterKey) {
        log.info("请求获取章节进度: courseId={}, chapterKey={}", courseId, chapterKey);
        
        Users currentUser = getCurrentUser();
        if (currentUser == null) {
            return Result.failed("用户未登录或认证信息不正确");
        }

        LearningProgress progress = learningProgressService.getChapterProgress(
            currentUser.getId(), courseId, chapterKey);
        
        if (progress == null) {
            log.info("章节进度不存在: userId={}, courseId={}, chapterKey={}", 
                currentUser.getId(), courseId, chapterKey);
            return Result.success(null, "章节进度不存在");
        }
        
        log.info("成功获取章节进度: userId={}, courseId={}, chapterKey={}", 
            currentUser.getId(), courseId, chapterKey);
        return Result.success(progress, "获取章节进度成功");
    }

    /**
     * 更新学习进度
     * 
     * @param request 进度更新请求
     * @return 更新结果
     */
    @Operation(summary = "更新学习进度", description = "更新当前用户对某一章节的学习状态")
    @PostMapping("/update")
    @PreAuthorize("isAuthenticated()")
    public Result<LearningProgress> updateProgress(@RequestBody UpdateProgressRequest request) {
        log.info("请求更新学习进度: {}", request);
        
        Users currentUser = getCurrentUser();
        if (currentUser == null) {
            return Result.failed("用户未登录或认证信息不正确");
        }

        LearningProgress updated = learningProgressService.updateProgress(currentUser.getId(), request);
        log.info("用户 {} 对章节 {} 的进度更新成功", currentUser.getUsername(), request.getChapterKey());
        return Result.success(updated, "进度更新成功");
    }
    
    /**
     * 批量更新学习进度（用于离线同步）
     * 
     * @param request 批量更新请求
     * @return 更新结果
     */
    @Operation(summary = "批量更新学习进度", description = "批量更新当前用户的多个章节学习进度（用于离线同步）")
    @PostMapping("/batch-update")
    @PreAuthorize("isAuthenticated()")
    public Result<Map<String, Boolean>> batchUpdateProgress(
            @RequestBody Map<String, Object> request) {
        log.info("请求批量更新学习进度");
        
        Users currentUser = getCurrentUser();
        if (currentUser == null) {
            return Result.failed("用户未登录或认证信息不正确");
        }
        
        @SuppressWarnings("unchecked")
        List<UpdateProgressRequest> progressRecords = (List<UpdateProgressRequest>) request.get("progressRecords");
        
        if (progressRecords == null || progressRecords.isEmpty()) {
            return Result.failed("进度记录列表为空");
        }
        
        log.info("批量更新记录数量: {}", progressRecords.size());
        boolean success = learningProgressService.batchUpdateProgress(currentUser.getId(), progressRecords);
        
        Map<String, Boolean> result = Map.of("success", success);
        return Result.success(result, success ? "批量更新成功" : "部分更新失败");
    }
    
    /**
     * 获取当前登录用户
     * 
     * @return 当前用户实体，如果未登录则返回null
     */
    private Users getCurrentUser() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (!(principal instanceof UserDetails)) {
            return null;
        }
        String username = ((UserDetails) principal).getUsername();
        return usersService.findByUsername(username);
    }
} 