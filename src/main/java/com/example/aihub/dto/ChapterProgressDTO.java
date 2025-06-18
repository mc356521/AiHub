package com.example.aihub.dto;

import com.example.aihub.entity.Chapters;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@Schema(description = "用于展示章节及其学习进度的DTO")
public class ChapterProgressDTO {

    private Integer id;
    private Integer parentId;
    private String chapterKey;
    private String title;
    private int level;
    private int sortOrder;
    private List<ChapterProgressDTO> children;

    @Schema(description = "学习状态 (not_started, in_progress, completed)")
    private String status = "not_started"; // 默认未开始

    public ChapterProgressDTO(Chapters chapter) {
        this.id = chapter.getId();
        this.parentId = chapter.getParentId();
        this.chapterKey = chapter.getChapterKey();
        this.title = chapter.getTitle();
        this.level = chapter.getLevel();
        this.sortOrder = chapter.getSortOrder();
    }
} 