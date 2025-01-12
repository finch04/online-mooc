package com.tianji.course.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

/**
 * 课程保存结果
 */
@Data
@Schema(description = "课程保存结果")
@AllArgsConstructor
@Builder
public class CourseSaveVO {
    @Schema(description = "课程id", example = "1")
    private Long id;
}
