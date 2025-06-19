package com.example.aihub.controller;

import com.example.aihub.common.Result;
import com.example.aihub.dto.CreateCourseRequest;
import com.example.aihub.dto.MyCourseResponse;
import com.example.aihub.dto.UpdateProgressRequest;
import com.example.aihub.entity.Chapters;
import com.example.aihub.entity.Courses;
import com.example.aihub.entity.Users;
import com.example.aihub.service.ChaptersService;
import com.example.aihub.service.CoursesService;
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
import com.example.aihub.dto.ChapterProgressDTO;
import java.util.Map;
import java.util.HashMap;

/**
 * 课程管理控制器，提供课程的增删改查、解析及内容获取等功能。
 *
 * @author hahaha
 * @since 2024-07-25
 */
@Tag(name = "课程管理", description = "提供课程的增删改查及解析功能")
@RestController
@RequestMapping("/courses")
@Slf4j
public class CoursesController extends BaseController {

    @Autowired
    private CoursesService coursesService;

    @Autowired
    private UsersService usersService;
    
    @Autowired
    private ChaptersService chaptersService;
    
    @Autowired
    private LearningProgressService learningProgressService;

    /**
     * 创建一个新课程。
     * 此接口需要 'teacher' 或 'admin' 权限。
     *
     * @param createCourseRequest 创建课程所需的请求数据
     * @return 包含已创建课程信息的Result响应
     */
    @PreAuthorize("hasAnyAuthority('teacher', 'admin')")
    @Operation(summary = "创建新课程", description = "创建一个新的课程，并自动生成对应的Markdown文件。只有教师或管理员可以访问。")
    @PostMapping
    public Result<Courses> createCourse(@RequestBody CreateCourseRequest createCourseRequest) {
        log.info("请求创建新课程，标题: {}", createCourseRequest.getTitle());
        Courses course = coursesService.createCourse(createCourseRequest);
        log.info("课程创建成功，课程ID: {}", course.getId());
        return Result.success(course, "课程创建成功");
    }

    /**
     * 解析指定课程的Markdown文件。
     * 读取文件内容，将其解析为结构化的章节信息并存入数据库。
     *
     * @param courseId 要解析的课程ID
     * @return 操作结果
     * @throws Exception 解析过程中可能发生文件读写或数据库错误
     */
    @PreAuthorize("hasAnyAuthority('teacher', 'admin')")
    @Operation(summary = "解析课程内容", description = "解析指定课程的Markdown文件，并将其结构化的章节信息存入数据库。")
    @PostMapping("/{courseId}/parse")
    public Result<?> parseCourse(@PathVariable Integer courseId) throws Exception {
        log.info("请求解析课程，课程ID: {}", courseId);

        // 检查课程是否存在
        Courses course = coursesService.getById(courseId);
        if (course == null) {
            log.warn("尝试解析一个不存在的课程，ID: {}", courseId);
            return Result.failed("课程不存在");
        }

        coursesService.parseAndSaveChapters(courseId);
        log.info("课程解析成功，课程ID: {}", courseId);
        return Result.success(null, "课程解析成功");
    }

    /**
     * 获取当前登录的教师所创建的所有课程列表。
     * 此接口需要 'teacher' 权限。
     *
     * @return 包含课程列表的Result响应
     */
    @Operation(summary = "获取当前教师的课程列表", description = "获取当前登录的教师用户创建的所有课程列表。")
    @GetMapping("/my")
    @PreAuthorize("hasAuthority('teacher')")
    public Result<List<Courses>> getMyCourses() {
        log.info("请求获取当前教师的课程列表");
        List<Courses> courses = coursesService.getMyCourses();
        log.debug("获取到课程 {} 条", courses.size());
        return Result.success(courses, "获取我的课程列表成功");
    }

    /**
     * 获取当前登录的学生所加入的所有课程列表。
     * 此接口需要 'student' 权限。
     *
     * @return 包含课程列表的Result响应
     */
    @Operation(summary = "获取当前学生的课程列表", description = "获取当前登录的学生用户所加入的所有班级对应的课程列表。")
    @GetMapping("/my-student")
    @PreAuthorize("hasAuthority('student')")
    public Result<List<MyCourseResponse>> getMyStudentCourses() {
        log.info("请求获取当前学生的课程列表");

        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (!(principal instanceof UserDetails)) {
            return Result.failed("用户未登录或认证信息不正确");
        }
        String username = ((UserDetails) principal).getUsername();
        Users currentUser = usersService.findByUsername(username);
        if (currentUser == null) {
            return Result.failed("无法找到当前登录用户的数据");
        }

        Integer studentId = Integer.parseInt(String.valueOf(currentUser.getId()));

        List<MyCourseResponse> courses = coursesService.getStudentCourses(studentId);
        log.debug("获取到学生的课程 {} 条", courses.size());
        return Result.success(courses, "获取学生课程列表成功");
    }

    /**
     * 根据课程ID获取课程基本信息。
     * 此接口需要用户已认证。
     *
     * @param courseId 课程ID
     * @return 包含课程基本信息的Result响应
     */
    @Operation(summary = "获取课程基本信息", description = "根据课程ID获取课程的基本信息。任何认证过的用户都可以访问。")
    @GetMapping("/{courseId}")
    @PreAuthorize("isAuthenticated()")
    public Result<Courses> getCourseById(@PathVariable Integer courseId) {
        log.info("请求获取课程基本信息，课程ID: {}", courseId);
        Courses course = coursesService.getById(courseId);
        if (course == null) {
            log.warn("尝试获取一个不存在的课程，ID: {}", courseId);
            return Result.failed("课程不存在");
        }
        log.info("成功获取到课程基本信息，课程ID: {}", courseId);
        return Result.success(course, "获取课程基本信息成功");
    }

    /**
     * 获取指定课程的完整Markdown原文内容。
     * 此接口需要用户已认证。
     *
     * @param courseId 课程ID
     * @return 包含Markdown原文的Result响应
     * @throws Exception 如果文件未找到或读取失败
     */
    @Operation(summary = "获取课程Markdown原文", description = "获取指定课程的完整Markdown文件内容，用于前端渲染。任何认证过的用户都可以访问。")
    @GetMapping("/{courseId}/content")
    @PreAuthorize("isAuthenticated()")
    public Result<String> getCourseContent(@PathVariable Integer courseId) throws Exception {
        log.info("请求获取课程Markdown内容，课程ID: {}", courseId);

        // 检查课程是否存在
        Courses course = coursesService.getById(courseId);
        if (course == null) {
            log.warn("尝试获取一个不存在的课程内容，ID: {}", courseId);
            return Result.failed("课程不存在");
        }

        String content = coursesService.getCourseMarkdownContent(courseId);
        log.debug("成功获取到课程内容，课程ID: {}", courseId);
        return Result.success(content, "获取课程内容成功");
    }

    /**
     * 更新指定课程的Markdown原文内容。
     * 此接口需要 'teacher' 或 'admin' 权限，并且只能由课程所有者操作。
     * 更新后会自动重新解析课程章节。
     *
     * @param courseId 课程ID
     * @param content  新的Markdown内容
     * @return 操作结果
     * @throws Exception 如果文件写入或解析失败
     */
    @Operation(summary = "更新课程Markdown原文", description = "更新指定课程的Markdown文件内容，并自动重新解析章节。只有课程所有者或管理员可以访问。")
    @PutMapping("/{courseId}/content")
    @PreAuthorize("hasAnyAuthority('teacher', 'admin')")
    public Result<?> updateCourseContent(@PathVariable Integer courseId, @RequestBody String content) throws Exception {
        log.info("请求更新课程Markdown内容，课程ID: {}", courseId);

        // 检查课程是否存在
        Courses course = coursesService.getById(courseId);
        if (course == null) {
            log.warn("尝试更新一个不存在的课程内容，ID: {}", courseId);
            return Result.failed("课程不存在");
        }

        coursesService.updateCourseContent(courseId, content);
        log.info("课程内容更新并重新解析成功，课程ID: {}", courseId);
        return Result.success(null, "课程内容更新成功");
    }

    /**
     * 获取指定课程的章节列表及当前学生的学习进度。
     * 此接口需要用户已认证。
     *
     * @param courseId 课程ID
     * @return 包含章节进度树的Result响应
     */
    @Operation(summary = "获取课程章节及学习进度", description = "获取指定课程的章节树状列表，并附带当前学生的学习进度状态。")
    @GetMapping("/{courseId}/progress")
    @PreAuthorize("isAuthenticated()")
    public Result<List<ChapterProgressDTO>> getCourseChapterProgress(@PathVariable Integer courseId) {
        log.info("请求获取课程章节及学习进度，课程ID: {}", courseId);
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (!(principal instanceof UserDetails)) {
            return Result.failed("用户未登录或认证信息不正确");
        }
        String username = ((UserDetails) principal).getUsername();
        Users currentUser = usersService.findByUsername(username);
        if (currentUser == null) {
            return Result.failed("无法找到当前登录用户的数据");
        }

        List<ChapterProgressDTO> chapterProgress = coursesService.getCourseChaptersWithProgress(courseId, currentUser.getId());
        log.info("成功获取到课程 {} 的章节进度，共 {} 个顶级章节", courseId, chapterProgress.size());
        return Result.success(chapterProgress, "获取课程进度成功");
    }

    /**
     * 获取指定章节的详细内容。
     * 此接口需要用户已认证。
     * 访问时会自动更新用户的学习进度。
     *
     * @param courseId   课程ID
     * @param chapterKey 章节标识键
     * @return 包含章节详情的Result响应
     */
    @Operation(summary = "获取章节详情内容", description = "获取指定章节的详细内容，并记录用户访问进度")
    @GetMapping("/{courseId}/chapters/{chapterKey}")
    @PreAuthorize("isAuthenticated()")
    public Result<Map<String, Object>> getChapterDetail(
            @PathVariable Integer courseId,
            @PathVariable String chapterKey) {
        
        log.info("请求获取章节详情，课程ID: {}, 章节KEY: {}", courseId, chapterKey);
        
        // 获取当前登录用户
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (!(principal instanceof UserDetails)) {
            return Result.failed("用户未登录或认证信息不正确");
        }
        String username = ((UserDetails) principal).getUsername();
        Users currentUser = usersService.findByUsername(username);
        if (currentUser == null) {
            return Result.failed("无法找到当前登录用户的数据");
        }
        
        // 获取章节详情
        Chapters chapter = chaptersService.getChapterByKey(courseId, chapterKey);
        if (chapter == null) {
            log.warn("尝试获取不存在的章节，课程ID: {}, 章节KEY: {}", courseId, chapterKey);
            return Result.failed("章节不存在");
        }
        
        // 自动更新学习进度为"进行中"
        UpdateProgressRequest progressRequest = new UpdateProgressRequest();
        progressRequest.setCourseId(courseId);
        progressRequest.setChapterKey(chapterKey);
        progressRequest.setStatus("in_progress");
        
        learningProgressService.updateProgress(currentUser.getId(), progressRequest);
        
        // 构建响应数据
        Map<String, Object> responseData = new HashMap<>();
        responseData.put("chapter", chapter);
        
        // 返回结果
        log.info("成功获取章节详情，用户ID: {}, 章节: {}", currentUser.getId(), chapter.getTitle());
        return Result.success(responseData, "获取章节详情成功");
    }
} 