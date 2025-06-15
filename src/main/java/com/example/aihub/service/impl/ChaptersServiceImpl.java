package com.example.aihub.service.impl;

import com.example.aihub.entity.Chapters;
import com.example.aihub.mapper.ChaptersMapper;
import com.example.aihub.service.ChaptersService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 章节解析缓存，用于快速查询和导航 服务实现类
 * </p>
 *
 * @author AIHub Code Generator
 * @since 2025-06-16
 */
@Service
public class ChaptersServiceImpl extends ServiceImpl<ChaptersMapper, Chapters> implements ChaptersService {

    @Override
    public void deleteByCourseId(Integer courseId) {
        this.remove(new QueryWrapper<Chapters>().eq("course_id", courseId));
    }

    @Override
    public void physicalDeleteByCourseId(Integer courseId) {
        baseMapper.physicalDeleteByCourseId(courseId);
    }
}
