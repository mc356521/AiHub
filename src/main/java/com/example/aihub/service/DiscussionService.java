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

/**
 * 讨论服务类：负责处理讨论帖子的业务逻辑
 *
 * @author ikun
 * @created: ii_kun
 * @createTime: 2025/6/19 20:27
 * @email: weijikun1@icloud.com
 */
@Slf4j
@Schema(description = "讨论服务类：负责处理讨论帖子的业务逻辑")
@Service
public class DiscussionService {

    // 自动注入 DiscussionRepository，用于操作 MongoDB
    @Autowired
    private DiscussionRepository repository;


    @Autowired
    private UsersService usersService;

    @Autowired
    private CoursesService coursesService;

    @Autowired
    private ClassesService classesService;


    /**
     * @param discussion 讨论实体（包含标题、内容、作者、图片等）
     * @return 保存后的 Discussion 对象（包含生成的 ID 和时间）
     */
    @Schema(description = "发布一个新的讨论帖子")
    public Result<Discussion> postDiscussion(Discussion discussion) {
        // 从token解析数据
        // 1. 获取当前用户信息
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (!(principal instanceof UserDetails)) {
            throw new IllegalStateException("用户未登录或认证信息不正确");
        }
        String username = ((UserDetails) principal).getUsername();
        Users currentUser = usersService.findByUsername(username);
        if (currentUser == null) {
            throw new IllegalStateException("无法找到当前登录用户的数据");
        }
        // 设置发布者
        discussion.setPublisherId(currentUser.getId());
        // 设置当前时间为创建时间
        discussion.setCreatedAt(LocalDateTime.now());
        // 保存到 MongoDB，并返回保存结果
        return Result.success(repository.save(discussion));
    }

    /**
     * @return 讨论列表
     */
    @Schema(description = "获取所有讨论帖子")
    public Result<List<Discussion>> getAllDiscussions() {
        List<Discussion> discussions = repository.findAll();
        // 查询课程信息
        val courseById = coursesService.getCourseById(discussions.get(0).getCoursesId());
        // 查询班级信息
        val classById = classesService.findClassById(discussions.get(0).getClassesId());
        // 填充信息
        for (Discussion discussion : discussions) {
            discussion.setCoursesName(courseById.getTitle());
            discussion.setClassesName(classById.getName());
            for (Comment comment : discussion.getComments()) {
                // 查询评论者信息
                val users = usersService.findById(Long.valueOf(comment.getCommentAuthorId()));
                comment.setCommentAuthorName(users.getUsername());
            }
        }
        // 查询 discussions 集合中的所有文档
        return Result.success(discussions);
    }

    /**
     * @param discussionId 讨论帖子的 ID
     * @param comment      评论对象（包括内容、作者、图片等）
     * @return 更新后的 Discussion 对象
     */
    @Schema(description = "给指定的讨论帖子添加评论")
    public Result<Discussion> addComment(String discussionId, Comment comment) {
        try {
            Discussion discussion = repository.findById(discussionId)
                    .orElseThrow(() -> new RuntimeException("讨论不存在"));
            comment.setCommentTime(LocalDateTime.now());
            // 初始化评论列表（防止 NPE）
            if (discussion.getComments() == null) {
                discussion.setComments(new ArrayList<>());
            }
            discussion.getComments().add(comment);
            Discussion saved = repository.save(discussion);
            // 判断保存后的对象是否为空或 id 丢失
            if (saved.getId() != null) {
                return Result.success(saved);
            } else {
                return Result.failed("保存失败，请重试");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return Result.failed("添加评论时发生错误：" + e.getMessage());
        }
    }


    /**
     * @param discussionId 讨论帖 ID
     * @return 操作结果
     */
    @Schema(description = "根据 ID 删除指定讨论帖")
    public Result<String> deleteDiscussionById(String discussionId) {
        try {
            // 判断是否存在
            if (!repository.existsById(discussionId)) {
                return Result.failed("删除失败：讨论不存在");
            }
            // 执行删除
            repository.deleteById(discussionId);
            return Result.success("删除成功");
        } catch (Exception e) {
            e.printStackTrace();
            return Result.failed("删除失败：" + e.getMessage());
        }
    }


    /**
     * @param courseId 课程 ID
     * @return 讨论列表
     */
    @Schema(description = "根据课程 ID 获取该课程下的所有讨论")
    public Result<List<Discussion>> getDiscussionsByCourseId(Integer courseId) {
        try {
            List<Discussion> discussions = repository.findByCoursesId(courseId);
            // 查询课程信息
            val courseById = coursesService.getCourseById(discussions.get(0).getCoursesId());
            // 查询班级信息
            val classById = classesService.findClassById(discussions.get(0).getClassesId());
            // 填充信息
            for (Discussion discussion : discussions) {
                discussion.setCoursesName(courseById.getTitle());
                discussion.setClassesName(classById.getName());
                for (Comment comment : discussion.getComments()) {
                    // 查询评论者信息
                    val users = usersService.findById(Long.valueOf(comment.getCommentAuthorId()));
                    comment.setCommentAuthorName(users.getUsername());
                }
            }
            return Result.success(discussions);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.failed("获取讨论失败：" + e.getMessage());
        }
    }


    @Schema(description = "根据班级id获取班级下所有讨论内容")
    public Result<List<Discussion>> getDiscussionsByClassesIdAll(Integer classesId) {
        try {
            val discussionByClassesId = repository.findDiscussionByClassesId(classesId);
            // 查询课程信息
            val courseById = coursesService.getCourseById(discussionByClassesId.get(0).getCoursesId());
            // 查询班级信息
            val classById = classesService.findClassById(discussionByClassesId.get(0).getClassesId());
            // 填充信息
            for (Discussion discussion : discussionByClassesId) {
                discussion.setCoursesName(courseById.getTitle());
                discussion.setClassesName(classById.getName());
                for (Comment comment : discussion.getComments()) {
                    // 查询评论者信息
                    val users = usersService.findById(Long.valueOf(comment.getCommentAuthorId()));
                    comment.setCommentAuthorName(users.getUsername());
                }
            }
            return Result.success(discussionByClassesId);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.failed("获取班级讨论失败: " + e.getMessage());
        }
    }

    @Schema(description = "通过课程id获取课程下所有讨论内容")
    public Result<List<Discussion>> getDiscussionsByCourseIdAll(Integer courseId) {
        try {
            val discussionByCoursesId = repository.findDiscussionByCoursesId(courseId);
            // 查询课程信息
            val courseById = coursesService.getCourseById(discussionByCoursesId.get(0).getCoursesId());
            // 查询班级信息
            val classById = classesService.findClassById(discussionByCoursesId.get(0).getClassesId());
            // 填充信息
            for (Discussion discussion : discussionByCoursesId) {
                discussion.setCoursesName(courseById.getTitle());
                discussion.setClassesName(classById.getName());
                for (Comment comment : discussion.getComments()) {
                    // 查询评论者信息
                    val users = usersService.findById(Long.valueOf(comment.getCommentAuthorId()));
                    comment.setCommentAuthorName(users.getUsername());
                }
            }
            return Result.success(discussionByCoursesId);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.failed("获取讨论内容失败: " + e.getMessage());
        }
    }
}
