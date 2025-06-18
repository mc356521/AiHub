package com.example.aihub.service;

import com.example.aihub.dto.CreateCourseRequest;
import com.example.aihub.entity.Courses;
import com.baomidou.mybatisplus.extension.service.IService;
import com.example.aihub.dto.MyCourseResponse;
import org.springframework.web.multipart.MultipartFile;
import com.example.aihub.dto.ChapterProgressDTO;

import java.util.List;

/**
 * <p>
 * 课程基本信息和Markdown文件元数据 服务类
 * </p>
 *
 * @author AIHub Code Generator
 * @since 2025-06-15
 */
public interface CoursesService extends IService<Courses> {
    /**
     * 创建一个新课程，并为其在服务器上生成一个空的Markdown文件。
     *
     * @param request 包含课程标题和描述的请求对象
     * @return 已创建并保存到数据库的课程实体
     */
    Courses createCourse(CreateCourseRequest request);

    /**
     * 解析指定课程的Markdown文件内容，并将结构化的章节信息保存到数据库。
     *
     * @param courseId 要解析的课程ID
     * @throws Exception 如果文件读取、解析或数据库操作失败
     */
    void parseAndSaveChapters(Integer courseId) throws Exception;

    /**
     * 获取当前登录教师创建的所有课程。
     *
     * @return 当前教师的课程列表
     */
    List<Courses> getMyCourses();

    /**
     * 获取指定课程的Markdown文件原文。
     *
     * @param courseId 课程ID
     * @return 课程的Markdown文件内容字符串
     * @throws Exception 如果文件不存在或读取时发生IO错误
     */
    String getCourseMarkdownContent(Integer courseId) throws Exception;

    void updateCourseContent(Integer courseId, String content) throws Exception;

    List<MyCourseResponse> getStudentCourses(Integer studentId);

    List<ChapterProgressDTO> getCourseChaptersWithProgress(Integer courseId, Integer userId);
}
