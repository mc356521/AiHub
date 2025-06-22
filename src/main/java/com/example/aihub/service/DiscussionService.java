package com.example.aihub.service;

import com.example.aihub.common.Result;
import com.example.aihub.entity.Comment;
import com.example.aihub.entity.Courses;
import com.example.aihub.entity.Discussion;
import com.example.aihub.entity.Users;
import com.example.aihub.repository.DiscussionRepository;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Schema(description = "讨论服务类：负责处理讨论帖子的业务逻辑")
@Service
public class DiscussionService {

    @Autowired
    private DiscussionRepository repository;

    @Autowired
    private UsersService usersService;

    @Autowired
    private CoursesService coursesService;

    @Autowired
    private ClassesService classesService;

    @Schema(description = "发布一个新的讨论帖子")
    public Result<Discussion> postDiscussion(Discussion discussion) {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (!(principal instanceof UserDetails)) {
            return Result.failed("用户未登录或认证信息不正确");
        }

        String username = ((UserDetails) principal).getUsername();
        Users currentUser = usersService.findByUsername(username);
        if (currentUser == null) {
            return Result.failed("无法找到当前登录用户的数据");
        }

        discussion.setPublisherId(currentUser.getId());
        discussion.setCreatedAt(LocalDateTime.now());

        return Result.success(repository.save(discussion));
    }

    @Schema(description = "获取所有讨论帖子")
    public Result<List<Discussion>> getAllDiscussions() {
        List<Discussion> discussions = repository.findAll();
        if (discussions.isEmpty()) return Result.success(discussions);

        enrichDiscussions(discussions);
        return Result.success(discussions);
    }

    @Schema(description = "给指定的讨论帖子添加评论")
    public Result<Discussion> addComment(String discussionId, Comment comment) {
        try {
            Discussion discussion = repository.findById(discussionId)
                    .orElseThrow(() -> new RuntimeException("讨论不存在"));

            comment.setCommentTime(LocalDateTime.now());
            if (discussion.getComments() == null) {
                discussion.setComments(new ArrayList<>());
            }
            discussion.getComments().add(comment);

            Discussion saved = repository.save(discussion);
            return saved.getId() != null
                    ? Result.success(saved)
                    : Result.failed("保存失败，请重试");

        } catch (Exception e) {
            log.error("添加评论异常", e);
            return Result.failed("添加评论时发生错误：" + e.getMessage());
        }
    }

    @Schema(description = "根据 ID 删除指定讨论帖")
    public Result<String> deleteDiscussionById(String discussionId) {
        try {
            if (!repository.existsById(discussionId)) {
                return Result.failed("删除失败：讨论不存在");
            }
            repository.deleteById(discussionId);
            return Result.success("删除成功");
        } catch (Exception e) {
            log.error("删除讨论异常", e);
            return Result.failed("删除失败：" + e.getMessage());
        }
    }

    @Schema(description = "根据课程 ID 获取该课程下的所有讨论")
    public Result<List<Discussion>> getDiscussionsByCourseId(Integer courseId) {
        try {
            List<Discussion> discussions = repository.findByCoursesId(courseId);
            if (discussions.isEmpty()) return Result.success(discussions);

            enrichDiscussions(discussions);
            return Result.success(discussions);
        } catch (Exception e) {
            log.error("获取课程讨论失败", e);
            return Result.failed("获取讨论失败：" + e.getMessage());
        }
    }

    @Schema(description = "根据班级id获取班级下所有讨论内容")
    public Result<List<Discussion>> getDiscussionsByClassesIdAll(Integer classesId) {
        try {
            List<Discussion> discussions = repository.findDiscussionByClassesId(classesId);
            if (discussions.isEmpty()) return Result.success(discussions);

            enrichDiscussions(discussions);
            return Result.success(discussions);
        } catch (Exception e) {
            log.error("获取班级讨论失败", e);
            return Result.failed("获取班级讨论失败: " + e.getMessage());
        }
    }

    @Schema(description = "通过课程id获取课程下所有讨论内容")
    public Result<List<Discussion>> getDiscussionsByCourseIdAll(Integer courseId) {
        try {
            List<Discussion> discussions = repository.findDiscussionByCoursesId(courseId);
            if (discussions.isEmpty()) return Result.success(discussions);

            enrichDiscussions(discussions);
            return Result.success(discussions);
        } catch (Exception e) {
            log.error("获取课程讨论失败", e);
            return Result.failed("获取讨论内容失败: " + e.getMessage());
        }
    }

    /**
     * 批量填充课程名称、班级名称、发布者信息和评论者信息，避免代码重复
     */
    private void enrichDiscussions(List<Discussion> discussions) {
        for (Discussion discussion : discussions) {
            try {
                Courses course = coursesService.getCourseById(discussion.getCoursesId());
                discussion.setCoursesName(course != null ? course.getTitle() : "未知课程");

                val classModel = classesService.findClassById(discussion.getClassesId());
                discussion.setClassesName(classModel != null ? classModel.getName() : "未知班级");

                val publisher = usersService.findById(Long.valueOf(discussion.getPublisherId()));
                discussion.setPublisherName(publisher != null ? publisher.getFullName() : "匿名用户");

                if (discussion.getComments() != null) {
                    for (Comment comment : discussion.getComments()) {
                        val user = usersService.findById(Long.valueOf(comment.getCommentAuthorId()));
                        comment.setCommentAuthorName(user != null ? user.getUsername() : "匿名用户");
                    }
                }
            } catch (Exception e) {
                log.warn("填充讨论信息失败: {}", discussion.getId(), e);
            }
        }
    }
}
