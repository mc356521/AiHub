package com.example.aihub.mapper;

import com.example.aihub.entity.LearningTasks;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * <p>
 * 教师发布的学习任务，可关联章节、资源等，有截止日期 Mapper 接口
 * </p>
 *
 * @author AIHub Code Generator
 * @since 2025-06-20
 */
@Mapper
public interface LearningTasksMapper extends BaseMapper<LearningTasks> {

}
