package com.example.aihub.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("class_members")
@Schema(name = "ClassMembers", description = "班级成员关联表")
public class ClassMembers extends BaseEntity {

    @Schema(description = "班级ID, 关联到 classes.id")
    private Integer classId;

    @Schema(description = "学生ID, 关联到 users.id")
    private Integer studentId;
} 