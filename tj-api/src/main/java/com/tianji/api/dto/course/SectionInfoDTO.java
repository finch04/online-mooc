package com.tianji.api.dto.course;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Schema(description = "小节信息，包含课程id和媒资id")
@AllArgsConstructor
@NoArgsConstructor
public class SectionInfoDTO {
    @Schema(description = "课程id")
    private Long courseId;
    @Schema(description = "媒资id")
    private Long mediaId;
    @Schema(description = "是否支持免费试看")
    private Boolean trailer;
    @Schema(description = "免费时长，不免费为0，单位分钟")
    private Integer freeDuration;
}
