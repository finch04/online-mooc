package com.tianji.course.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * 章信息
 */
@Data
@Schema(description = "章信息")
public class ChapterVO {
    @Schema(description = "章id", example = "1")
    private Long id;

    @Schema(description = "章索引", example = "1")
    private Integer index;

    @Schema(description = "章名称", example = "第一章")
    private String name;

    @Schema(description = "本章视频总时长", example = "3600")
    private Integer mediaDuration;

    @Schema(description = "小节列表")
    private List<SectionVO> sections;
}
