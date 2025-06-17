package com.example.aihub.controller;

import com.example.aihub.common.Result;
import com.example.aihub.dto.SemestersRequest;
import com.example.aihub.entity.Semesters;
import com.example.aihub.service.SemestersService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Schema;
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
        try {
            val semesters = semestersService.selectAllSemesters();
            return Result.success(semesters);
        } catch (Exception e) {
            return Result.failed(e.getMessage());
        }
    }

}
