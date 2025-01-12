package com.tianji.course.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 目录简单信息
 */
@Data
@Schema(description = "目录简单信息")
@AllArgsConstructor
@NoArgsConstructor
public class CataSimpleInfoVO {
    @Schema(description = "目录id", example = "1")
    private Long id;

    @Schema(description = "目录名称", example = "第一章")
    private String name;

    @Schema(description = "目录序号1-1", example = "1-1")
    private String index;

    @Schema(description = "数字序号，不包含章序号", example = "1")
    private Integer cIndex;

    @Schema(description = "数字序号章序号", example = "1")
    private Integer chapterIndex;
}
