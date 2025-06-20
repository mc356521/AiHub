package com.example.aihub.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;


import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 班级互动讨论实体类，对应 MongoDB 中的 discussions 集合
 *
 * @author ikun
 * @createTime: 2025/6/19 20:24
 * @email: weijikun1@icloud.com
 */
@Data // Lombok 注解，自动生成 getter、setter、toString、equals、hashCode 等方法
@Schema(description = "班级互动讨论实体类")
@Document(collection = "discussions") // 指定 MongoDB 中的集合名称为 discussions
public class Discussion {

    @Id
    @Schema(description = "MongoDB 中的主键字段，对应 _id")
    private String id;

    @Schema(description = "讨论课程id", required = true)
    private Integer coursesId;

    @Schema(description = "讨论标题，例如“期末复习资料分享”")
    private String title;

    @Schema(description = "讨论正文内容，可以是纯文本或富文本（如带格式的 HTML 字符串）")
    private String content;

    @Schema(description = "发起讨论的作者名称（可使用用户昵称或 ID）")
    private String authorId;

    /**
     * 需要先提交文件获取到的文件路径填充
     * 示例：
     * ["https://img-server.com/image1.jpg", "https://img-server.com/image2.png"]
     */
    @Schema(description = "附带的图片 URL 列表，支持上传多个图片")
    private List<String> images;

    @Schema(description = "帖子创建时间，通常在后台自动赋值 LocalDateTime.now()")
    private LocalDateTime createdAt;

    // 初始化评论列表为空列表，避免 null
    @Schema(description = "评论列表，包含所有对该帖子的评论内容、使用嵌套 Comment 类进行结构化存储")
    private List<Comment> comments = new ArrayList<>();

}