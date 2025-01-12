package com.tianji.course.domain.dto;

import com.tianji.common.validate.annotations.EnumValid;
import com.tianji.course.constants.CourseErrorInfo;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.NotNull;

/**
 * 课程分类启用或停用模型
 */
@Data
@Schema(description = "课程分类启用/禁用")
public class CategoryDisableOrEnableDTO {
    @Schema(description = "课程分类id", example = "1")
    @NotNull(message = CourseErrorInfo.Msg.CATEGORY_ID_NOT_NULL)
    private Long id;

    @Schema(description = "课程分类状态，1：启用，0：禁用", example = "1")
    @EnumValid(enumeration = {0, 1}, message = CourseErrorInfo.Msg.CATEGORY_DISABLE_ENABLE_STATUS_ENUM)
    private Integer status;
}
