package com.example.aihub.repository;

import com.example.aihub.entity.Discussion;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

/**
 * @created: ii_kun
 * @createTime: 2025/6/19 20:26
 * @email: weijikun1@icloud.com
 */
public interface DiscussionRepository extends MongoRepository<Discussion, String> {
    /**
     * 根据课程 ID 查询所有互动讨论
     *
     * @param coursesId 课程 ID
     * @return 该课程下的讨论列表
     */
    List<Discussion> findByCoursesId(Integer coursesId);
}