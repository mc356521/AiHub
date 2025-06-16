package com.example.aihub.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.aihub.dto.CreateCourseRequest;
import com.example.aihub.entity.Chapters;
import com.example.aihub.entity.Courses;
import com.example.aihub.entity.Users;
import com.example.aihub.mapper.CoursesMapper;
import com.example.aihub.service.ChaptersService;
import com.example.aihub.service.CoursesService;
import com.example.aihub.service.UsersService;
import com.vladsch.flexmark.ast.Heading;
import com.vladsch.flexmark.parser.Parser;
import com.vladsch.flexmark.util.ast.Document;
import com.vladsch.flexmark.util.ast.Node;
import com.vladsch.flexmark.util.ast.NodeVisitor;
import com.vladsch.flexmark.util.ast.VisitHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.FileTime;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.Stack;

/**
 * <p>
 * 课程基本信息和Markdown文件元数据 服务实现类
 * </p>
 *
 * @author hahaha
 * @since 2024-07-25
 */
@Service
@Slf4j
public class CoursesServiceImpl extends ServiceImpl<CoursesMapper, Courses> implements CoursesService {

    @Value("${file.storage.path}")
    private String storagePath;

    @Autowired
    private UsersService usersService;

    @Autowired
    private ChaptersService chaptersService;

    @Override
    @Transactional
    public Courses createCourse(CreateCourseRequest request) {
        // 1. 获取当前用户信息
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (!(principal instanceof UserDetails)) {
            throw new IllegalStateException("用户未登录或认证信息不正确");
        }
        String username = ((UserDetails) principal).getUsername();
        Users currentUser = usersService.findByUsername(username);
        if (currentUser == null) {
            throw new IllegalStateException("无法找到当前登录用户的数据");
        }

        // 2. 检查用户角色
        String role = currentUser.getRole();
        if (!"teacher".equals(role) && !"admin".equals(role)) {
            throw new SecurityException("只有教师或管理员才能创建课程");
        }

        // 3. 创建并保存课程实体
        Courses course = new Courses();
        course.setTitle(request.getTitle());
        course.setDescription(request.getDescription());
        course.setTeacherId(currentUser.getId().intValue());
        course.setParseStatus("pending"); // 默认状态
        course.setChapterCount(0);

        // 4. 生成Markdown文件路径并创建文件
        try {
            // 创建存储目录（如果不存在）
            Path directory = Paths.get(storagePath);
            if (!Files.exists(directory)) {
                Files.createDirectories(directory);
            }

            // 生成唯一文件名
            String fileName = UUID.randomUUID().toString() + ".md";
            Path filePath = directory.resolve(fileName);

            // 创建空文件
            Files.createFile(filePath);

            course.setFilePath(filePath.toString().replace(File.separator, "/"));
        } catch (IOException e) {
            log.error("创建Markdown文件失败", e);
            throw new RuntimeException("创建课程文件时发生错误", e);
        }

        // 5. 保存课程信息到数据库
        this.save(course);
        return course;
    }

    @Override
    @Transactional
    public void parseAndSaveChapters(Integer courseId) {
        // 1. 获取课程信息
        Courses course = this.getById(courseId);
        if (course == null) {
            throw new IllegalArgumentException("未找到ID为 " + courseId + " 的课程");
        }

        // 2. 权限验证 (可选但推荐): 确保只有课程所有者或管理员能解析
        // 此处省略，因为Controller层已有@PreAuthorize

        // 3. 读取Markdown文件并计算元数据
        String content;
        LocalDateTime fileUpdatedAt;
        String fileHash;
        try {
            Path filePath = Paths.get(course.getFilePath());
            byte[] fileBytes = Files.readAllBytes(filePath);
            content = new String(fileBytes, StandardCharsets.UTF_8);

            // 获取文件最后修改时间
            FileTime lastModifiedTime = Files.getLastModifiedTime(filePath);
            fileUpdatedAt = LocalDateTime.ofInstant(lastModifiedTime.toInstant(), ZoneId.systemDefault());

            // 计算文件内容的SHA-256哈希
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] encodedhash = digest.digest(fileBytes);
            StringBuilder hexString = new StringBuilder(2 * encodedhash.length);
            for (byte b : encodedhash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            fileHash = hexString.toString();

        } catch (IOException | NoSuchAlgorithmException e) {
            updateCourseParseStatus(course, "failed", "读取或处理文件时出错: " + e.getMessage(), 0, null, null);
            throw new RuntimeException("读取或处理文件时出错", e);
        }

        // 4. 解析Markdown并提取章节
        Parser parser = Parser.builder().build();
        Document document = parser.parse(content);
        List<Chapters> chaptersList = new ArrayList<>();
        AtomicInteger sortOrder = new AtomicInteger(0);
        
        int[] levelCounters = new int[6];
        log.debug("--- [Chapter Parsing START] courseId: {} ---", courseId);

        Map<String, String> parentLinkMap = new HashMap<>();

        NodeVisitor visitor = new NodeVisitor(
            new VisitHandler<>(Heading.class, heading -> {
                int level = heading.getLevel();
                log.debug("Found heading: '{}', level: {}", heading.getText().toString(), level);
                log.debug("Counters state BEFORE: {}", Arrays.toString(levelCounters));

                levelCounters[level - 1]++;

                for (int i = level; i < levelCounters.length; i++) {
                    levelCounters[i] = 0;
                }
                log.debug("Counters state AFTER:  {}", Arrays.toString(levelCounters));

                StringBuilder keyBuilder = new StringBuilder();
                for (int i = 0; i < level; i++) {
                    keyBuilder.append(levelCounters[i]);
                    if (i < level - 1) {
                        keyBuilder.append(".");
                    }
                }
                String chapterKey = keyBuilder.toString();
                log.debug("==> Generated chapterKey: {}", chapterKey);

                String parentKey = null;
                if (level > 1) {
                    StringBuilder parentKeyBuilder = new StringBuilder();
                    for (int i = 0; i < level - 1; i++) {
                        parentKeyBuilder.append(levelCounters[i]);
                        if (i < level - 2) {
                            parentKeyBuilder.append(".");
                        }
                    }
                    parentKey = parentKeyBuilder.toString();
                }

                parentLinkMap.put(chapterKey, parentKey);
                
                StringBuilder contentBuilder = new StringBuilder();
                Node next = heading.getNext();
                while (next != null && !(next instanceof Heading && ((Heading) next).getLevel() <= level)) {
                    contentBuilder.append(next.getChars().toString());
                    next = next.getNext();
                }

                Chapters chapter = new Chapters();
                chapter.setCourseId(courseId);
                chapter.setChapterKey(chapterKey);
                chapter.setLevel(level);
                chapter.setTitle(heading.getText().toString());
                chapter.setContent(contentBuilder.toString().trim());
                chapter.setSortOrder(sortOrder.incrementAndGet());
                chapter.setLineStart(heading.getStartLineNumber());
                chapter.setLineEnd(next != null ? next.getStartLineNumber() - 1 : document.getEndLineNumber());
                
                chaptersList.add(chapter);
            })
        );
        visitor.visit(document);
        log.debug("--- [Chapter Parsing END] Total chapters found: {} ---", chaptersList.size());

        // 5. 存入数据库
        try {
            // 5.1. 先物理删除旧章节
            chaptersService.physicalDeleteByCourseId(courseId);
            
            // 5.2. 插入新章节（不带parentId）
            if (chaptersList.isEmpty()) {
                updateCourseParseStatus(course, "success", "课程为空或未包含章节", 0, fileHash, fileUpdatedAt);
                return;
            }
            chaptersService.saveBatch(chaptersList);

            // 5.3. 构建 chapterKey -> id 的映射
            Map<String, Long> keyToIdMap = chaptersList.stream()
                    .collect(Collectors.toMap(Chapters::getChapterKey, Chapters::getId));
            
            // 5.4. 设置 parentId 并准备更新
            List<Chapters> updateList = new ArrayList<>();
            for (Chapters chapter : chaptersList) {
                String parentKey = parentLinkMap.get(chapter.getChapterKey());
                if (parentKey != null) {
                    Long parentId = keyToIdMap.get(parentKey);
                    if (parentId != null) {
                        chapter.setParentId(parentId.intValue());
                        updateList.add(chapter);
                    }
                }
            }

            // 5.5. 批量更新 parentId
            if (!updateList.isEmpty()) {
                chaptersService.updateBatchById(updateList);
            }

            // 5.6. 更新课程解析状态
            updateCourseParseStatus(course, "success", null, chaptersList.size(), fileHash, fileUpdatedAt);
        } catch (Exception e) {
            log.error("保存章节信息到数据库时失败, courseId: {}", courseId, e);
            String rootCauseMessage = e.getMessage();
            if (e.getCause() != null) {
                rootCauseMessage = e.getCause().getMessage();
            }
            updateCourseParseStatus(course, "failed", "数据库操作失败: " + rootCauseMessage, 0, null, null);
            throw new RuntimeException("保存章节信息时发生数据库错误: " + rootCauseMessage, e);
        }
    }

    private void updateCourseParseStatus(Courses course, String status, String errorMessage, int chapterCount, String fileHash, LocalDateTime fileUpdatedAt) {
        course.setParseStatus(status);
        course.setParseError(errorMessage);
        course.setParsedAt(LocalDateTime.now());
        course.setChapterCount(chapterCount);
        course.setFileHash(fileHash);
        course.setFileUpdatedAt(fileUpdatedAt);
        this.updateById(course);
    }

    @Override
    public List<Courses> getMyCourses() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String username;
        if (principal instanceof UserDetails) {
            username = ((UserDetails) principal).getUsername();
        } else {
            username = principal.toString();
        }

        Users currentUser = usersService.findByUsername(username);
        if (currentUser == null) {
            // 在实际应用中，不太可能发生，因为JWT过滤器已经验证了用户存在
            // 但作为防御性编程，我们返回一个空列表
            return Collections.emptyList();
        }

        QueryWrapper<Courses> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("teacher_id", currentUser.getId());
        return list(queryWrapper);
    }
}
