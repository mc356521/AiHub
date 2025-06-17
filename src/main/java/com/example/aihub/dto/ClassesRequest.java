package com.example.aihub.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * @created: ii_kun
 * @createTime: 2025/6/17 11:17
 * @email: weijikun1@icloud.com
 */
@Data
@Schema(description = "创建班级请求对象")
public class ClassesRequest {

    @Schema(description = "班级名称",example = "软件一班")
    private String name;



}
