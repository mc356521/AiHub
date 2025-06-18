package com.example.aihub.service;

import com.example.aihub.entity.Semesters;
import com.baomidou.mybatisplus.extension.service.IService;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

/**
 * <p>
 * 统一管理所有学期信息 服务类
 * </p>
 *
 * @author AIHub Code Generator
 * @since 2025-06-17
 */
public interface SemestersService extends IService<Semesters> {


    @Schema(description = "学期新增接口定义")
    boolean insertSemesters(Semesters semesters);

    @Schema(description = "获取学期信息接口定义")
    List<Semesters> selectAllSemesters();

    @Schema(description = "根据id获取学期信息")
    Semesters getSemesterById(Integer id);

}
