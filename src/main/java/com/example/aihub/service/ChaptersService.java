package com.example.aihub.service;

import com.example.aihub.entity.Chapters;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 * 章节解析缓存，用于快速查询和导航 服务类
 * </p>
 *
 * @author AIHub Code Generator
 * @since 2025-06-16
 */
public interface ChaptersService extends IService<Chapters> {

    /**
     * 根据课程ID逻辑删除所有相关的章节。
     *
     * @param courseId 课程ID
     */
    void deleteByCourseId(Integer courseId);
    
    /**
     * 根据课程ID物理删除所有相关的章节。
     *
     * @param courseId 课程ID
     */
    void physicalDeleteByCourseId(Integer courseId);
    
    /**
     * 根据课程ID和章节Key获取章节详情
     * 
     * @param courseId 课程ID
     * @param chapterKey 章节标识键
     * @return 章节详情
     */
    Chapters getChapterByKey(Integer courseId, String chapterKey);
}
