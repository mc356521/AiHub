package com.example.aihub.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.aihub.dto.ClassesRequest;
import com.example.aihub.entity.ClassesEntity;
import com.example.aihub.entity.Users;
import com.example.aihub.mapper.ClassesMapper;
import com.example.aihub.mapper.ClassMembersMapper;
import com.example.aihub.service.ClassesService;
import com.example.aihub.service.CoursesService;
import com.example.aihub.service.UsersService;
import com.example.aihub.util.BasicUtil;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * @created: ii_kun
 * @createTime: 2025/6/16 23:20
 * @email: weijikun1@icloud.com
 */
@Service
public class ClassesServiceImpl extends ServiceImpl<ClassesMapper, ClassesEntity> implements ClassesService {

    @Autowired
    private UsersService usersService;

    @Autowired
    private CoursesService coursesService;

    @Autowired
    private ClassesMapper classesMapper;

    @Autowired
    private ClassMembersMapper classMembersMapper;

    /**
     * 新增班级
     *
     * @param classes 班级实体
     * @return 是否新增成功
     */
    @SuppressWarnings("DuplicatedCode")
    @Override
    public boolean addClass(ClassesEntity classes) {
        return this.save(classes);
    }

    /**
     * 根据token解析获取到教师id根据教师id查询教师管理得所有班级
     * @param teacherId 教师id
     * @return 班级列表
     */
    @Override
    public List<ClassesEntity> all(Integer teacherId) {
        QueryWrapper<ClassesEntity> wrapper = new QueryWrapper<>();
        wrapper.eq("teacher_id", teacherId); // 指定查询条件
        return classesMapper.selectList(wrapper);
    }


    /**
     * 更改/修改班级信息
     *
     * @param classesRequest 修改班级请求体
     * @return true: 修改成功, false: 修改失败
     */
    @Override
    public boolean updateClasses(ClassesRequest classesRequest) {
        if (classesRequest.getId() == null) {
            throw new IllegalArgumentException("更新时必须传入班级ID");
        }
        // 更新数据
        ClassesEntity entity = new ClassesEntity();
        entity.setId(classesRequest.getId());
        entity.setName(classesRequest.getName());
        entity.setCourseId(classesRequest.getCourseId());
        entity.setSemesterId(classesRequest.getSemesterId());
        entity.setStatus(classesRequest.getStatus());
        entity.setUpdateTime(LocalDateTime.now());
        return classesMapper.updateById(entity) > 0;
    }

    @Override
    public List<ClassesEntity> findClassesByTeacherAndStatus(Long teacherId, String status) {
        QueryWrapper<ClassesEntity> wrapper = new QueryWrapper<>();
        wrapper.eq("teacher_id", teacherId);
        wrapper.eq("status", status);
        return classesMapper.selectList(wrapper);
    }


    @Override
    public void joinClassByCode(Integer studentId, String classCode) {
        // 1. 根据口令查找班级
        QueryWrapper<ClassesEntity> classWrapper = new QueryWrapper<>();
        classWrapper.eq("class_code", classCode);
        ClassesEntity targetClass = classesMapper.selectOne(classWrapper);

        if (targetClass == null) {
            throw new RuntimeException("无效的班级口令");
        }
        // 2. 检查班级状态
        if (!("pending".equals(targetClass.getStatus()) || "active".equals(targetClass.getStatus()))) {
            throw new RuntimeException("该班级已结束或已归档，无法加入");
        }

        // 3. 检查用户是否为该班级教师
        if (studentId.equals(targetClass.getTeacherId())) {
            throw new RuntimeException("教师不能加入自己创建的班级");
        }

        // 4. 检查用户是否已在班级中
        QueryWrapper<com.example.aihub.entity.ClassMembers> memberWrapper = new QueryWrapper<>();
        memberWrapper.eq("class_id", targetClass.getId());
        memberWrapper.eq("student_id", studentId);
        if (classMembersMapper.selectCount(memberWrapper) > 0) {
            throw new RuntimeException("您已经在此班级中");
        }

        // 5. 添加成员
        com.example.aihub.entity.ClassMembers newMember = new com.example.aihub.entity.ClassMembers();
        newMember.setStudentId(studentId);
        newMember.setClassId(targetClass.getId());
        classMembersMapper.insert(newMember);
    }
}
