package com.example.aihub.mapper;

import com.example.aihub.entity.Users;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * <p>
 * 存储所有系统用户，包括学生、教师和管理员 Mapper 接口
 * </p>
 *
 * @author AIHub Code Generator
 * @since 2025-06-15
 */
@Mapper
public interface UsersMapper extends BaseMapper<Users> {

}
