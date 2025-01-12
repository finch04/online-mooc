package com.tianji.api.dto.leanring;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "小节信息及学习进度")
public class LearningRecordDTO {
    @Schema(description = "对应节的id")
    private Long sectionId;
    @Schema(description = "视频的当前观看时长，单位秒")
    private Integer moment;
    @Schema(description = "是否完成学习，默认false")
    private Boolean finished;
}
