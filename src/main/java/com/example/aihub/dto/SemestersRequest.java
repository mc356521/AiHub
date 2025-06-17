package com.example.aihub.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDate;
import java.util.Date;

/**
 * 创建学期请求体
 *
 * @created: ii_kun
 * @createTime: 2025/6/17 21:24
 * @email: weijikun1@icloud.com
 */
@Data
@Schema(description = "创建学期请求体")
public class SemestersRequest {

    @Schema(description = "学期名称", required = true, example = "2024-2025学年第一学期")
    private String name;

    @Schema(description = "学期开始日期", required = true, example = "2025-09-01")
    private LocalDate startDate;

    @Schema(description = "学期结束日期", required = true, example = "2026-01-20")
    private LocalDate endDate;

    @Schema(description = "学期状态", required = false, allowableValues = {"current", "past", "future"}, example = "future")
    private String status;
}
