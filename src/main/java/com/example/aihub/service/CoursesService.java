package com.example.aihub.service;

import com.example.aihub.dto.CreateCourseRequest;
import com.example.aihub.entity.Courses;
import com.baomidou.mybatisplus.extension.service.IService;

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
    Courses createCourse(CreateCourseRequest request);
    void parseAndSaveChapters(Integer courseId) throws Exception;
    List<Courses> getMyCourses();
}
