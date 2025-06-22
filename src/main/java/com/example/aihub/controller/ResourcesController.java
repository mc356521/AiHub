package com.example.aihub.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.aihub.common.Result;
import com.example.aihub.entity.Resources;
import com.example.aihub.service.ResourcesService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import jakarta.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * <p>
 * 存储上传的教学资源，如课件、视频、文档等 前端控制器
 * </p>
 *
 * @author AIHub Code Generator
 * @since 2025-06-21
 */
@RestController
@RequestMapping("/resources")
@Tag(name = "教学资源管理", description = "提供教学资源的上传、查询等功能")
public class ResourcesController extends BaseController {

    @Autowired
    private ResourcesService resourcesService;

    @PostMapping("/upload")
    @Operation(summary = "上传教学资源文件", description = "上传文件，并关联到指定课程（可选）")
    public Result<Resources> uploadResource(
            @RequestParam("file") MultipartFile file,
            @RequestParam("title") String title,
            @RequestParam(value = "courseId", required = false) Integer courseId) {
        try {
            Resources resource = resourcesService.uploadFile(file, title, courseId);
            return Result.success(resource, "文件上传成功");
        } catch (IOException e) {
            return Result.failed("文件上传失败: " + e.getMessage());
        } catch (IllegalStateException e) {
            return Result.failed(e.getMessage());
        }
    }

    @GetMapping("/course/{courseId}")
    @Operation(summary = "根据课程ID查询资源列表", description = "获取指定课程下的所有教学资源")
    public Result<List<Resources>> getResourcesByCourse(@PathVariable Integer courseId) {
        List<Resources> resourcesList = resourcesService.list(
                new QueryWrapper<Resources>().eq("course_id", courseId)
        );
        // 遵循开发规范：即使列表为空，也返回成功的Result，data为一个空数组
        return Result.success(resourcesList);
    }

    @GetMapping("/download/{resourceId}")
    @Operation(summary = "下载教学资源文件", description = "根据资源ID下载对应的文件")
    public ResponseEntity<?> downloadResource(@PathVariable Integer resourceId, HttpServletRequest request) {
        // 1. 获取资源元数据
        Resources resourceEntity = resourcesService.getById(resourceId);
        if (resourceEntity == null) {
            return ResponseEntity.status(404).body(Result.failed("找不到ID为 " + resourceId + " 的资源"));
        }

        try {
            // 2. 加载文件为Resource
            Resource resource = resourcesService.loadFileAsResource(resourceEntity.getFilePath());

            // 3. 构造下载的文件名 (使用数据库中的title)
            String originalFilename = resourceEntity.getTitle();
            String fileExtension = "";
            String storedPath = resourceEntity.getFilePath();
            if (storedPath.contains(".")) {
                fileExtension = storedPath.substring(storedPath.lastIndexOf("."));
            }
            // 防止文件名没有后缀
            if (!originalFilename.toLowerCase().endsWith(fileExtension.toLowerCase())) {
                originalFilename += fileExtension;
            }

            // 解决中文文件名乱码问题
            String encodedFilename = URLEncoder.encode(originalFilename, StandardCharsets.UTF_8.toString()).replaceAll("\\+", "%20");

            // 4. 构建响应
            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(resourceEntity.getFileType()))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename*=UTF-8''" + encodedFilename)
                    .body(resource);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Result.failed("文件下载失败: " + e.getMessage()));
        }
    }
}
