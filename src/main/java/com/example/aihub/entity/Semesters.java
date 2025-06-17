package com.example.aihub.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.example.aihub.entity.BaseEntity;
import java.time.LocalDate;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

/**
 * <p>
 * 统一管理所有学期信息
 * </p>
 *
 * @author AIHub Code Generator
 * @since 2025-06-17
 */
@Getter
@Setter
@TableName("semesters")
@Schema(name = "Semesters", description = "统一管理所有学期信息")
public class Semesters {

    @Schema(description = "学期名称, 用于显示 (如: 2024-2025学年第一学期)")
    @TableField("name")
    private String name;

    @Schema(description = "学期开始日期")
    @TableField("start_date")
    private LocalDate startDate;

    @Schema(description = "学期结束日期")
    @TableField("end_date")
    private LocalDate endDate;

    @Schema(description = "学期状态 (current: 当前, past: 已结束, future: 未开始)")
    @TableField("status")
    private String status;
}
