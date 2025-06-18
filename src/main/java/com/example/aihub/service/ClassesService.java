package com.example.aihub.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.aihub.dto.ClassesRequest;
import com.example.aihub.entity.Chapters;
import com.example.aihub.entity.ClassesEntity;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * <p>
 *     班级业务逻辑操作接口定义
 * <p/>
 *
 * @created: ii_kun
 * @createTime: 2025/6/16 23:17
 * @email: weijikun1@icloud.com
 */
public interface ClassesService extends IService<ClassesEntity> {
    /**
     * 新增班级
     *
     * @param classes 班级实体
     * @return 是否新增成功
     */
    boolean addClass(ClassesEntity classes);


    /**
     * 获取教师管理得所有班级信息
     * @param teacherId 教师id
     */
     List<ClassesEntity> all(Integer teacherId);


    /**
     * 更新/修改班级信息
     */
    boolean updateClasses(ClassesRequest classesRequest);


    /**
     * 删除指定的班级
     * @param classesId 班级id
     */
    boolean deleteClasses(Integer classesId);

}
