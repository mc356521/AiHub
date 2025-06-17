package com.example.aihub.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.security.Timestamp;

/**
 *
 * 班级数据表对应模型
 *
 * @created: ii_kun
 * @createTime: 2025/6/16 23:12
 * @email: weijikun1@icloud.com
 */
@Getter
@Setter
@TableName("classes")
@Schema(name = "ClassesEntity", description = "教师可以创建班级来管理学生")
public class ClassesEntity {

    @TableId(type = IdType.AUTO)
    @Schema(description = "班级唯一ID")
    private Integer id;

    @Schema(description = "班级名称")
    private String name;

    @Schema(description = "外键, 关联到users表的教师ID")
    private Integer teacherId;

    @Schema(description = "外键, 关联到courses表的课程ID (可选)")
    private Integer courseId;

    @Schema(description = "班级口令/邀请码, 用于学生加入班级")
    private String classCode;

    @Schema(description = "班级创建时间")
    private Timestamp createTime;

    @Schema(description = "信息最后更新时间")
    private Timestamp updateTime;

    @Schema(description = "逻辑删除标志")
    private Boolean deleted;
}





















