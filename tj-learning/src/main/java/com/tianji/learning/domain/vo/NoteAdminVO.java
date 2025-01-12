package com.tianji.learning.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Schema(description = "管理端笔记分页信息")
public class NoteAdminVO {
    @Schema(description = "主键id")
    private Long id;
    @Schema(description = "课程名称")
    private String courseName;
    @Schema(description = "章名称")
    private String chapterName;
    @Schema(description = "节名称")
    private String sectionName;
    @Schema(description = "笔记内容")
    private String content;
    @Schema(description = "是否被隐藏")
    private Boolean hidden;
    @Schema(description = "作者名称")
    private String authorName;
    @Schema(description = "笔记发布时间", example = "2022-7-18 19:52:36")
    private LocalDateTime createTime;
}
