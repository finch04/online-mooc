package com.tianji.course.domain.dto;

import com.tianji.course.constants.CourseErrorInfo;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * 课程分类新增模型
 */
@Data
@Schema(description = "课程分类新增模型")
public class CategoryAddDTO {
    @Schema(description = "父分类id, 如果是新增一级分类parentId传0", example = "0")
    private Long parentId;

    @Schema(description = "名称", required = true, example = "编程")
    @NotNull(message = CourseErrorInfo.Msg.CATEGORY_ADD_NAME_NOT_NULL)
    @Size(max = 15, message = CourseErrorInfo.Msg.CATEGORY_ADD_NAME_SIZE)
    private String name;

    @Schema(description = "分类序号", required = true, example = "1")
    @Max(value = 99, message = CourseErrorInfo.Msg.CATEGORY_ADD_INDEX_MAX_MIN)
    @Min(value = 1, message = CourseErrorInfo.Msg.CATEGORY_ADD_INDEX_MAX_MIN)
    @NotNull(message = CourseErrorInfo.Msg.CATEGORY_ADD_INDEX_NOT_NULL)
    private Integer index;
}
