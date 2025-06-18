package com.example.aihub.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.aihub.dto.ClassesRequest;
import com.example.aihub.entity.ClassesEntity;
import org.apache.ibatis.annotations.Mapper;

/**
 * <p>
 * 班级表 Mapper 接口
 * </p>
 *
 * 主要用于数据库中 classes 表的基本 CRUD 操作，由 MyBatis-Plus 自动实现。
 * 如需自定义复杂 SQL，可在本接口中添加方法，并在对应 XML 中编写 SQL 语句。
 *
 * @created: ii_kun
 * @createTime: 2025/6/16 23:15
 * @email: weijikun1@icloud.com
 */
@Mapper
public interface ClassesMapper extends BaseMapper<ClassesEntity> {

}
