package com.tianji.course.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 课程视频保存模型
 */
@Data
@Schema(description = "课程视频保存模型")
public class CourseMediaSaveDTO {
    @Schema(description = "小节id", example = "1")
    private Long cataId;

    @Schema(description = "媒资id", example = "101")
    private Long mediaId;

    @Schema(description = "是否支持试看", example = "true")
    private Boolean trailer;
}
