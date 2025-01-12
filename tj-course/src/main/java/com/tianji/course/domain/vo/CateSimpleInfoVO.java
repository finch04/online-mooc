package com.tianji.course.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 分类
 */
@Data
@Schema(description = "分类")
public class CateSimpleInfoVO {
    @Schema(description = "一级分类id", example = "1")
    private Long firstCateId;

    @Schema(description = "一级分类名称", example = "计算机科学")
    private String firstCateName;

    @Schema(description = "二级分类id", example = "2")
    private Long secondCateId;

    @Schema(description = "二级分类名称", example = "软件开发")
    private String secondCateName;

    @Schema(description = "三级分类id", example = "3")
    private Long thirdCateId;

    @Schema(description = "三级分类名称", example = "编程")
    private String thirdCateName;
}
