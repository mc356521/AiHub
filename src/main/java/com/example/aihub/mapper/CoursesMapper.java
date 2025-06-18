package com.example.aihub.mapper;

import com.example.aihub.entity.Courses;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import com.example.aihub.dto.MyCourseResponse;
import org.apache.ibatis.annotations.Param;
import java.util.List;

/**
 * <p>
 * 课程基本信息和Markdown文件元数据 Mapper 接口
 * </p>
 *
 * @author AIHub Code Generator
 * @since 2025-06-15
 */
@Mapper
public interface CoursesMapper extends BaseMapper<Courses> {

    List<MyCourseResponse> findCoursesByStudentId(@Param("studentId") Integer studentId);

}
