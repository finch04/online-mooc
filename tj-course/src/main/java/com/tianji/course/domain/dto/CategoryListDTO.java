package com.tianji.course.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 课程类目分页查询
 */
@Schema(description = "课程类目分页查询条件")
@Data
public class CategoryListDTO {
    @Schema(description = "类目状态1:正常，2：禁用", example = "1")
    private Integer status;

    @Schema(description = "类目名称", example = "编程")
    private String name;
}
