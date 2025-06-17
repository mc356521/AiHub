package com.example.aihub.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.aihub.dto.ClassesRequest;
import com.example.aihub.entity.Chapters;
import com.example.aihub.entity.ClassesEntity;
import org.springframework.stereotype.Service;

/**
 * <p>
 *     班级业务逻辑操作接口定义
 * <p/>
 *
 * @created: ii_kun
 * @createTime: 2025/6/16 23:17
 * @email: weijikun1@icloud.com
 */
public interface ClassesService extends IService<ClassesRequest> {
    /**
     * 新增班级
     *
     * @param classes 班级实体
     * @return 是否新增成功
     */
    boolean addClass(ClassesRequest classes);
}
