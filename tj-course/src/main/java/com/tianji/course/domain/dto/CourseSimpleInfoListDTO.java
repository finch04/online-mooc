package com.tianji.course.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * 课程简单信息列表查询条件
 */
@Data
@Schema(description = "课程简单信息列表查询条件")
public class CourseSimpleInfoListDTO {

    @Schema(description = "三级分类id列表", example = "[1, 2, 3]")
    private List<Long> thirdCataIds;

    @Schema(description = "课程id列表", example = "[101, 102, 103]")
    private List<Long> ids;
}
