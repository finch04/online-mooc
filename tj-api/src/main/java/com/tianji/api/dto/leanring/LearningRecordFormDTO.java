package com.tianji.api.dto.leanring;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Schema(description = "学习记录表单数据")
public class LearningRecordFormDTO {

    @Schema(description = "小节类型：1-视频，2-考试")
    private Integer sectionType;

    @Schema(description = "课表id")
    private Long lessonId;

    @Schema(description = "对应节的id")
    private Long sectionId;

    @Schema(description = "视频总时长，单位秒")
    private Integer duration;

    @Schema(description = "视频的当前观看时长，单位秒，第一次提交填0")
    private Integer moment;

    @Schema(description = "提交时间")
    private LocalDateTime commitTime;
}
