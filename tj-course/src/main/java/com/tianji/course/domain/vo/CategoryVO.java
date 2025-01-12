package com.tianji.course.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 课程分类信息
 */
@Data
@Schema(description = "课程分类信息")
public class CategoryVO {
    @Schema(description = "课程分类id", example = "1")
    private Long id;

    @Schema(description = "课程分类名称", example = "编程")
    private String name;

    @Schema(description = "三级分类数量", example = "5")
    private Integer thirdCategoryNum;

    @Schema(description = "课程数量", example = "10")
    private Integer courseNum;

    @Schema(description = "状态：1：正常，2：禁用", example = "1")
    private Integer status;

    @Schema(description = "状态描述", example = "正常")
    private String statusDesc;

    @Schema(description = "创建时间", example = "2022-07-10T11:32:00")
    private LocalDateTime createTime;

    @Schema(description = "更新时间", example = "2022-07-10T11:32:00")
    private LocalDateTime updateTime;

    @Schema(description = "排序", example = "1")
    private Integer index;

    @Schema(description = "父id", example = "0")
    private Long parentId;

    @Schema(description = "级别", example = "1")
    private Integer level;

    @Schema(description = "子分类列表")
    private List<CategoryVO> children;
}
