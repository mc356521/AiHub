package com.example.aihub.controller;

/**
 * @created: ii_kun
 * @createTime: 2025/6/19 23:33
 * @email: weijikun1@icloud.com
 */

import com.example.aihub.common.Result;
import com.example.aihub.entity.Comment;
import com.example.aihub.entity.Discussion;
import com.example.aihub.service.DiscussionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 控制器：处理讨论相关的 HTTP 请求接口
 *
 * @author ikun
 * @createTime 2025/6/19
 */
@RestController
@RequestMapping("/discussions") // 请求前缀
@Tag(name = "DiscussionController", description = "班级互动讨论接口")
public class DiscussionController {

    @Autowired
    private DiscussionService discussionService;

    @PostMapping
    @Operation(summary = "发布新讨论")
    public Result<Discussion> createDiscussion(@RequestBody Discussion discussion) {
        return discussionService.postDiscussion(discussion);
    }

    @GetMapping
    @Operation(summary = "获取全部讨论列表")
    public Result<List<Discussion>> getAllDiscussions() {
        return discussionService.getAllDiscussions();
    }

    @PostMapping("/{id}/comment")
    @Operation(summary = "给指定讨论添加评论")
    public Result<Discussion> addComment(@PathVariable("id") String id, @RequestBody Comment comment) {
        return discussionService.addComment(id, comment);
    }

    @DeleteMapping("/del/{id}")
    @Operation(summary = "根据id删除指定的互动讨论")
    public Result<String> deleteDiscussion(@PathVariable("id") String id) {
        if (id.isEmpty()) {
            return Result.failed("id不能为空!");
        } else {
            return discussionService.deleteDiscussionById(id);
        }
    }

    @GetMapping("/by-course/{courseId}")
    @Operation(summary = "根据课程ID获取讨论列表")
    public Result<List<Discussion>> getDiscussionsByCourseId(@PathVariable("courseId") Integer courseId) {
        if (courseId == null) {
            return Result.failed("课程id不能为空!");
        } else {
            return discussionService.getDiscussionsByCourseId(courseId);
        }
    }


    @GetMapping("/findByClassesId")
    @Operation(summary = "通过班级id获取讨论列表")
    public Result<List<Discussion>> getDiscussionsByClassesId(@RequestParam("classesId") Integer classesId) {
        if (classesId == null) {
            return Result.failed("班级id不能为空!");
        } else {
            return discussionService.getDiscussionsByClassesIdAll(classesId);
        }
    }

    @GetMapping("/findByCourceId")
    @Operation(summary = "通过课程id获取课程下所有讨论内容")
    public Result<List<Discussion>> getDiscussionsByCourceId(@RequestParam("courseId") Integer courseId) {
        if (courseId == null) {
            return Result.failed("课程id不能为空!");
        } else {
            return discussionService.getDiscussionsByCourseIdAll(courseId);
        }
    }

}
