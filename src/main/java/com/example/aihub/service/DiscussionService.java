package com.example.aihub.service;

import com.example.aihub.common.Result;
import com.example.aihub.entity.Comment;
import com.example.aihub.entity.Discussion;
import com.example.aihub.repository.DiscussionRepository;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
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
@Schema(description = "讨论服务类：负责处理讨论帖子的业务逻辑")
@Service
public class DiscussionService {

    // 自动注入 DiscussionRepository，用于操作 MongoDB
    @Autowired
    private DiscussionRepository repository;

    /**
     * @param discussion 讨论实体（包含标题、内容、作者、图片等）
     * @return 保存后的 Discussion 对象（包含生成的 ID 和时间）
     */
    @Schema(description = "发布一个新的讨论帖子")
    public Result<Discussion> postDiscussion(Discussion discussion) {
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
        // 查询 discussions 集合中的所有文档
        return Result.success(repository.findAll());
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
            if (saved != null && saved.getId() != null) {
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
            if (discussionByCoursesId == null) {
                return Result.failed("获取讨论失败!");
            } else {
                return Result.success(discussionByCoursesId);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return Result.failed("获取讨论内容失败: " + e.getMessage());
        }
    }
}
