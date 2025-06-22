package com.example.aihub.service.impl;

import com.baomidou.mybatisplus.core.toolkit.StringPool;
import com.example.aihub.entity.Resources;
import com.example.aihub.entity.Users;
import com.example.aihub.mapper.ResourcesMapper;
import com.example.aihub.service.ResourcesService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.aihub.service.UsersService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * <p>
 * 存储上传的教学资源 服务实现类
 * </p>
 *
 * @author AIHub Developer
 * @since 2025-06-21
 */
@Service
@Slf4j
public class ResourcesServiceImpl extends ServiceImpl<ResourcesMapper, Resources> implements ResourcesService {

    @Value("${file.resource-dir}")
    private String resourceDir;

    @Autowired
    private UsersService usersService;

    @Override
    public Resources uploadFile(MultipartFile file, String title, Integer courseId) throws IOException {
        // 1. 获取当前登录用户
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String username = ((UserDetails) principal).getUsername();
        Users currentUser = usersService.findByUsername(username);
        if (currentUser == null) {
            throw new IllegalStateException("无法获取当前登录用户信息");
        }

        // 2. 存储文件到服务器
        String originalFilename = file.getOriginalFilename();
        String fileExtension = "";
        if (originalFilename != null && originalFilename.contains(StringPool.DOT)) {
            fileExtension = originalFilename.substring(originalFilename.lastIndexOf(StringPool.DOT));
        }
        String newFileName = UUID.randomUUID().toString() + fileExtension;
        Path directoryPath = Paths.get(resourceDir);
        if (!Files.exists(directoryPath)) {
            Files.createDirectories(directoryPath);
        }
        Path filePath = directoryPath.resolve(newFileName);
        file.transferTo(filePath);

        // 3. 创建资源对象
        Resources resource = new Resources();
        resource.setTitle(title);
        resource.setUploaderId(currentUser.getId());
        resource.setCourseId(courseId);
        resource.setFilePath(filePath.toString());
        resource.setFileSize(file.getSize());
        resource.setFileType(file.getContentType());


        // 4. 如果是视频文件，尝试获取时长
        if (file.getContentType() != null && file.getContentType().startsWith("video")) {
            try {
                int duration = getVideoDuration(filePath);
                resource.setDuration(duration);
            } catch (Exception e) {
                log.error("获取视频时长失败: {}", filePath, e);
                // 即使获取时长失败，也继续保存文件信息
            }
        }

        // 5. 保存到数据库
        this.save(resource);
        return resource;
    }

    /**
     * 使用 ffprobe 获取视频时长
     *
     * @param videoPath 视频文件路径
     * @return 时长（秒）
     */
    private int getVideoDuration(Path videoPath) throws IOException, InterruptedException {
        String command = String.format(
                "ffprobe -v error -show_entries format=duration -of default=noprint_wrappers=1:nokey=1 \"%s\"",
                videoPath.toAbsolutePath().toString()
        );

        ProcessBuilder processBuilder = new ProcessBuilder();
        // 对于Windows和Linux/macOS，使用不同的方式执行命令
        if (System.getProperty("os.name").toLowerCase().contains("win")) {
            processBuilder.command("cmd.exe", "/c", command);
        } else {
            processBuilder.command("sh", "-c", command);
        }

        Process process = processBuilder.start();

        // 读取标准输出
        StringBuilder output = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                output.append(line);
            }
        }
        
        // 读取错误输出
        StringBuilder errorOutput = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getErrorStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                errorOutput.append(line).append(System.lineSeparator());
            }
        }

        boolean finishedInTime = process.waitFor(10, TimeUnit.SECONDS);
        if (!finishedInTime) {
            process.destroy();
            log.error("ffprobe process timed out for file: {}", videoPath);
            throw new IOException("ffprobe process timed out");
        }
        
        int exitCode = process.exitValue();
        if (exitCode == 0) {
            String durationStr = output.toString().trim();
            if(!durationStr.isEmpty()){
                return (int) Math.round(Double.parseDouble(durationStr));
            }
        }
        
        log.error("ffprobe failed with exit code {}. Error output: {}", exitCode, errorOutput.toString().trim());
        return 0;
    }

    @Override
    public Resource loadFileAsResource(String filePath) throws MalformedURLException {
        Path file = Paths.get(filePath).normalize();
        Resource resource = new UrlResource(file.toUri());

        if (resource.exists() && resource.isReadable()) {
            return resource;
        } else {
            log.error("无法读取文件或文件不存在: {}", filePath);
            throw new RuntimeException("无法读取文件: " + file.getFileName());
        }
    }
}
