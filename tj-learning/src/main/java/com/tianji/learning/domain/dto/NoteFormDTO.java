package com.tianji.learning.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.NotNull;

/**
 * 笔记信息
 */
@Data
@Schema(description = "笔记信息")
public class NoteFormDTO {
    @Schema(description = "笔记内容")
    private Long id;

    @Schema(description = "笔记内容")
    @NotNull(message = "笔记内容不能为空")
    private String content;

    @Schema(description = "记录笔记时的视频播放时间，单位秒")
    @NotNull(message = "笔记时间不能为空")
    private Integer noteMoment;

    @Schema(description = "是否是隐私笔记，默认false")
    @NotNull(message = "隐私标记不能为空")
    private Boolean isPrivate;

    @Schema(description = "课程id")
    @NotNull(message = "课程id不能为空")
    private Long courseId;

    @Schema(description = "章id")
    @NotNull(message = "章id不能为空")
    private Long chapterId;

    @Schema(description = "小节id")
    @NotNull(message = "节id不能为空")
    private Long sectionId;
}
