package com.tianji.learning.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.hibernate.validator.constraints.Range;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

/**
 * 学习计划表单实体
 */
@Data
@Schema(description = "学习计划表单实体")
public class LearningPlanDTO {
    @NotNull
    @Schema(description = "课程表id")
    @Min(1)
    private Long courseId;

    @NotNull
    @Range(min = 1, max = 50)
    @Schema(description = "每周学习频率")
    private Integer freq;
}
