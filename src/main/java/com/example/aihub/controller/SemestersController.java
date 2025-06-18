package com.example.aihub.controller;

import com.example.aihub.common.Result;
import com.example.aihub.dto.SemestersRequest;
import com.example.aihub.entity.Semesters;
import com.example.aihub.service.SemestersService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * <p>
 * 统一管理所有学期信息 前端控制器
 * </p>
 *
 * @author AIHub Code Generator
 * @since 2025-06-17
 */
@Slf4j
@RestController
@RequestMapping("/semesters")
public class SemestersController extends BaseController {

    @Autowired
    private SemestersService semestersService;

    @Operation(summary = "新增学期", description = "需要教师或管理员手动添加或者定时任务自动添加学期信息")
    @PostMapping
    public Result<SemestersRequest> createSemesters(@RequestBody SemestersRequest semestersRequest)
            throws Exception {
        // 组装数据
        Semesters semesters = new Semesters();
        semesters.setName(semestersRequest.getName());
        semesters.setStartDate(semestersRequest.getStartDate());
        semesters.setEndDate(semestersRequest.getEndDate());
        semesters.setStatus(semestersRequest.getStatus());

        // 插入数据
        val insertSemesters = semestersService.insertSemesters(semesters);
        if (insertSemesters) {
            return Result.success(semestersRequest);
        } else {
            return Result.failed("新增学期失败!");
        }
    }

    @Operation(summary = "获取所有学期信息", description = "学生教师均可以获取全部学期信息")
    @GetMapping("/all")
    public Result<List<Semesters>> getSemestersAll() {
        val semesters = semestersService.selectAllSemesters();
        return Result.success(semesters);
    }

    @Operation(summary = "根据ID获取学期信息", description = "根据提供的唯一ID获取单个学期的详细信息")
    @GetMapping("/{id}")
    public Result<Semesters> getSemesterById(@PathVariable Integer id) {
        log.info("开始查询ID为 {} 的学期信息", id);
        Semesters semester = semestersService.getSemesterById(id);
        if (semester != null) {
            log.info("成功查询到ID为 {} 的学期: {}", id, semester);
            return Result.success(semester);
        } else {
            log.warn("未找到ID为 {} 的学期", id);
            return Result.failed("未找到ID为 " + id + " 的学期");
        }
    }
}
