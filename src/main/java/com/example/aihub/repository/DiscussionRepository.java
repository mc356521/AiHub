package com.example.aihub.repository;

import com.example.aihub.entity.Discussion;
import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

/**
 * @author ikun
 * @created: ii_kun
 * @createTime: 2025/6/19 20:26
 * @email: weijikun1@icloud.com
 */
public interface DiscussionRepository extends MongoRepository<Discussion, String> {

    @Schema(description = "根据课程 ID 查询所有互动讨论")
    List<Discussion> findByCoursesId(Integer coursesId);

    @Schema(description = "根据班级id获取班级下所有讨论")
    List<Discussion> findDiscussionByClassesId(Integer classesId);

    @Schema(description = "根据课程id查询讨论内容")
    List<Discussion> findDiscussionByCoursesId(Integer coursesId);
}