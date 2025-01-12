package com.tianji.course.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 三级分类
 */
@Data
@Schema(description = "分类")
public class CateSimpleInfoDTO {
    @Schema(description = "一级分类id", example = "1")
    private Long firstCateId;

    @Schema(description = "二级分类id", example = "2")
    private Long secondCateId;

    @Schema(description = "三级分类id", example = "3")
    private Long thirdCateId;
}
