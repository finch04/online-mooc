package com.tianji.course.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 课程分类信息
 */
@Data
@Schema(description = "课程分类信息")
public class CategoryInfoVO {
    @Schema(description = "课程分类id", example = "1")
    private Long id;

    @Schema(description = "课程分类名称", example = "编程")
    private String name;

    @Schema(description = "状态：1：正常，2：禁用", example = "1")
    private Integer status;

    @Schema(description = "状态描述", example = "正常")
    private String statusDesc;

    @Schema(description = "创建时间", example = "2022-07-10T15:06:00")
    private LocalDateTime createTime;

    @Schema(description = "更新时间", example = "2022-07-10T15:06:00")
    private LocalDateTime updateTime;

    @Schema(description = "分类级别，1：一级分类，2：二级分类，3：三级分类", example = "1")
    private Integer categoryLevel;

    @Schema(description = "一级分类名称", example = "计算机科学")
    private String firstCategoryName;

    @Schema(description = "二级分类名称", example = "软件开发")
    private String secondCategoryName;

    @Schema(description = "排序", example = "1")
    private Integer index;
}
