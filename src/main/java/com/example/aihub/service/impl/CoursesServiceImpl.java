package com.example.aihub.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.aihub.dto.CreateCourseRequest;
import com.example.aihub.entity.Chapters;
import com.example.aihub.entity.Courses;
import com.example.aihub.entity.Users;
import com.example.aihub.mapper.CoursesMapper;
import com.example.aihub.mapper.ChaptersMapper;
import com.example.aihub.mapper.ClassMembersMapper;
import com.example.aihub.service.ChaptersService;
import com.example.aihub.service.CoursesService;
import com.example.aihub.service.UsersService;
import com.vladsch.flexmark.ast.Heading;
import com.vladsch.flexmark.parser.Parser;
import com.vladsch.flexmark.util.ast.Document;
import com.vladsch.flexmark.util.ast.Node;
import com.vladsch.flexmark.util.ast.NodeVisitor;
import com.vladsch.flexmark.util.ast.VisitHandler;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import com.example.aihub.dto.ChapterProgressDTO;
import com.example.aihub.entity.LearningProgress;
import com.example.aihub.service.LearningProgressService;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

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
import java.io.FileNotFoundException;
import java.nio.file.StandardOpenOption;
import com.example.aihub.common.Result;
import com.example.aihub.dto.MyCourseResponse;
import com.example.aihub.util.BasicUtil;

/**
 * 课程服务实现类，负责处理课程创建、解析、查询等核心业务逻辑。
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

    @Autowired
    private ClassMembersMapper classMembersMapper;

    @Autowired
    private ChaptersMapper chaptersMapper;

    @Autowired
    private LearningProgressService learningProgressService;

    @Value("${file.upload-dir}")
    private String uploadDir;

    @Autowired
    private CoursesMapper coursesMapper;

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

            course.setFilePath(fileName);
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
    public void parseAndSaveChapters(Integer courseId) throws Exception {
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
            String relativePath = course.getFilePath();
            // 兼容处理：检查数据库中存储的路径是否已包含根目录（为了兼容脏数据）
            Path filePath;
            if (relativePath.startsWith(storagePath)) {
                // 如果是 'courses-md/xxxx.md' 格式，直接使用
                filePath = Paths.get(relativePath);
            } else {
                // 如果是 'xxxx.md' 格式，与根目录拼接
                filePath = Paths.get(storagePath, relativePath);
            }

            if (!Files.exists(filePath.normalize())) {
                throw new FileNotFoundException("课程文件不存在: " + filePath.normalize());
            }

            byte[] fileBytes = Files.readAllBytes(filePath.normalize());
            content = new String(fileBytes, StandardCharsets.UTF_8);

            // 获取文件最后修改时间
            FileTime lastModifiedTime = Files.getLastModifiedTime(filePath.normalize());
            fileUpdatedAt = LocalDateTime.ofInstant(lastModifiedTime.toInstant(), ZoneId.systemDefault());

            // 计算文件内容的SHA-256哈希，用于版本比对
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

        // 4. 使用FlexMark解析Markdown并提取章节
        Parser parser = Parser.builder().build();
        Document document = parser.parse(content);
        List<Chapters> chaptersList = new ArrayList<>();
        AtomicInteger sortOrder = new AtomicInteger(0);
        
        // 用于生成章节序号（如1, 1.1, 1.2.1）的计数器数组，索引对应标题级别(H1-H6)
        int[] levelCounters = new int[6];
        log.debug("--- [Chapter Parsing START] courseId: {} ---", courseId);

        // 用于暂存章节与其父章节对应关系的映射
        Map<String, String> parentLinkMap = new HashMap<>();

        NodeVisitor visitor = new NodeVisitor(
            new VisitHandler<>(Heading.class, heading -> {
                int level = heading.getLevel(); // 获取标题级别 (1-6)
                log.debug("Found heading: '{}', level: {}", heading.getText().toString(), level);
                log.debug("Counters state BEFORE: {}", Arrays.toString(levelCounters));

                // 核心算法：增加当前级别的计数器，并重置所有更深级别的计数器
                levelCounters[level - 1]++;
                for (int i = level; i < levelCounters.length; i++) {
                    levelCounters[i] = 0;
                }
                log.debug("Counters state AFTER:  {}", Arrays.toString(levelCounters));

                // 根据计数器状态构建章节的唯一键 (e.g., "1.2.1")
                StringBuilder keyBuilder = new StringBuilder();
                for (int i = 0; i < level; i++) {
                    keyBuilder.append(levelCounters[i]);
                    if (i < level - 1) {
                        keyBuilder.append(".");
                    }
                }
                String chapterKey = keyBuilder.toString();
                log.debug("==> Generated chapterKey: {}", chapterKey);

                // 生成父章节的键 (e.g., "1.2" for "1.2.1")
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
                
                // 提取章节内容：从当前标题到下一个同级或更高级别标题之间的所有内容
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
            // 5.1. 先物理删除旧章节，确保幂等性
            chaptersService.physicalDeleteByCourseId(courseId);
            
            // 5.2. 插入新章节（此时不含parentId）
            if (chaptersList.isEmpty()) {
                updateCourseParseStatus(course, "success", "课程为空或未包含章节", 0, fileHash, fileUpdatedAt);
                return;
            }
            chaptersService.saveBatch(chaptersList);

            // 5.3. 从数据库重新获取已保存的章节，确保获得自动生成的ID
            List<Chapters> savedChapters = chaptersService.list(new QueryWrapper<Chapters>().eq("course_id", courseId));
            Map<String, Integer> keyToIdMap = savedChapters.stream()
                    .collect(Collectors.toMap(Chapters::getChapterKey, Chapters::getId));
            
            // 5.4. 根据之前记录的父子关系，设置parentId并准备批量更新
            List<Chapters> updateList = new ArrayList<>();
            for (Chapters chapter : savedChapters) {
                String parentKey = parentLinkMap.get(chapter.getChapterKey());
                if (parentKey != null) {
                    Integer parentId = keyToIdMap.get(parentKey);
                    if (parentId != null) {
                        chapter.setParentId(parentId);
                        updateList.add(chapter);
                    }
                }
            }

            // 5.5. 批量更新parentId
            if (!updateList.isEmpty()) {
                chaptersService.updateBatchById(updateList);
            }

            // 5.6. 更新课程主表的解析状态
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

    /**
     * 更新课程的解析状态及相关元数据。
     * 这是一个私有辅助方法，用于在解析过程的不同阶段更新课程记录。
     *
     * @param course        要更新的课程实体
     * @param status        新的解析状态 (e.g., "success", "failed")
     * @param errorMessage  如果失败，记录错误信息
     * @param chapterCount  成功解析出的章节数
     * @param fileHash      当前文件内容的哈希值
     * @param fileUpdatedAt 当前文件的最后修改时间
     */
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

    @Override
    public String getCourseMarkdownContent(Integer courseId) throws Exception {
        Courses course = this.getById(courseId);
        if (course == null) {
            throw new RuntimeException("课程不存在");
        }

        String relativePath = course.getFilePath();
        if (relativePath == null || relativePath.isBlank()) {
            throw new RuntimeException("课程文件路径未设置");
        }

        try {
            // 兼容处理：检查数据库中存储的路径是否已包含根目录（为了兼容脏数据）
            Path filePath;
            if (relativePath.startsWith(storagePath)) {
                // 如果是 'courses-md/xxxx.md' 格式，直接使用
                filePath = Paths.get(relativePath);
            } else {
                // 如果是 'xxxx.md' 格式，与根目录拼接
                filePath = Paths.get(storagePath, relativePath);
            }

            if (!Files.exists(filePath.normalize())) {
                throw new FileNotFoundException("课程文件不存在: " + filePath.normalize());
            }
            return Files.readString(filePath.normalize(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            // 将IO异常包装成更通用的Exception，由全局异常处理器捕获
            throw new Exception("读取课程文件时发生错误", e);
        }
    }

    @Override
    @Transactional
    public void updateCourseContent(Integer courseId, String content) throws Exception {
        // 1. 获取课程信息
        Courses course = this.getById(courseId);
        if (course == null) {
            throw new RuntimeException("课程不存在");
        }

        // 2. 验证用户权限
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String username = ((UserDetails) principal).getUsername();
        Users currentUser = usersService.findByUsername(username);

        if (!currentUser.getId().equals(Long.valueOf(course.getTeacherId())) && !"teacher".equals(currentUser.getRole())) {
            throw new SecurityException("无权修改此课程");
        }

        // 对前端发来的字符串进行清理，将 "\\n" 替换为真正的换行符
        String correctedContent = content.replace("\\n", "\n");
        // 同时，移除可能存在的多余的包围引号
        if (correctedContent.startsWith("\"") && correctedContent.endsWith("\"") && correctedContent.length() > 1) {
            correctedContent = correctedContent.substring(1, correctedContent.length() - 1);
        }

        // 3. 写入文件
        String relativePath = course.getFilePath();
        try {
            Path filePath;
            if (relativePath.startsWith(storagePath)) {
                filePath = Paths.get(relativePath);
            } else {
                filePath = Paths.get(storagePath, relativePath);
            }
            Files.writeString(filePath.normalize(), correctedContent, StandardCharsets.UTF_8, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        } catch (IOException e) {
            throw new Exception("写入课程文件时发生错误", e);
        }

        // 4. 立即重新解析章节
        this.parseAndSaveChapters(courseId);
    }

    @Override
    public List<MyCourseResponse> getStudentCourses(Integer studentId) {
        return baseMapper.findCoursesByStudentId(studentId);
    }

    @Override
    public List<ChapterProgressDTO> getCourseChaptersWithProgress(Integer courseId, Integer userId) {
        // 1. 获取课程的所有章节
        LambdaQueryWrapper<Chapters> chaptersQuery = new LambdaQueryWrapper<>();
        chaptersQuery.eq(Chapters::getCourseId, courseId);
        List<Chapters> allChapters = chaptersMapper.selectList(chaptersQuery);

        // 2. 获取用户在该课程的学习进度
        LambdaQueryWrapper<LearningProgress> progressQuery = new LambdaQueryWrapper<>();
        progressQuery.eq(LearningProgress::getCourseId, courseId)
                     .eq(LearningProgress::getUserId, userId);
        List<LearningProgress> progresses = learningProgressService.list(progressQuery);
        Map<String, String> chapterProgressMap = progresses.stream()
                .collect(Collectors.toMap(LearningProgress::getChapterKey, LearningProgress::getStatus));

        // 3. 将章节转换为DTO并填充进度
        List<ChapterProgressDTO> dtoList = allChapters.stream().map(chapter -> {
            ChapterProgressDTO dto = new ChapterProgressDTO(chapter);
            dto.setStatus(chapterProgressMap.getOrDefault(chapter.getChapterKey(), "not_started"));
            return dto;
        }).collect(Collectors.toList());

        // 4. 构建章节树形结构
        Map<Integer, ChapterProgressDTO> map = dtoList.stream()
                .collect(Collectors.toMap(ChapterProgressDTO::getId, dto -> dto));
        
        List<ChapterProgressDTO> tree = new ArrayList<>();
        for (ChapterProgressDTO dto : dtoList) {
            if (dto.getParentId() == null || dto.getParentId() == 0) { // 根节点
                tree.add(dto);
            } else {
                ChapterProgressDTO parentDto = map.get(dto.getParentId());
                if (parentDto != null) {
                    if (parentDto.getChildren() == null) {
                        parentDto.setChildren(new ArrayList<>());
                    }
                    parentDto.getChildren().add(dto);
                }
            }
        }

        // 对子章节和顶层章节进行排序
        tree.sort(Comparator.comparing(ChapterProgressDTO::getSortOrder));
        tree.forEach(this::sortChildren);

        return tree;
    }

    @Schema(description = "根据课程id查询课程信息")
    @Override
    public Courses getCourseById(Integer courseId) {
        return coursesMapper.selectById(courseId);
    }

    private void sortChildren(ChapterProgressDTO parent) {
        if (parent.getChildren() != null && !parent.getChildren().isEmpty()) {
            parent.getChildren().sort(Comparator.comparing(ChapterProgressDTO::getSortOrder));
            parent.getChildren().forEach(this::sortChildren);
        }
    }

    private String getMarkdownFilePath(Courses course) {
        return Paths.get(storagePath, course.getFilePath()).toString();
    }
}
