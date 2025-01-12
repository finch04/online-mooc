package com.tianji.learning.domain.dto;

import com.tianji.common.validate.annotations.EnumValid;
import com.tianji.learning.enums.SectionType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

/**
 * 学习记录
 */
@Data
@Schema(description = "学习记录")
public class LearningRecordFormDTO {

    @Schema(description = "小节类型：1-视频，2-考试")
    @NotNull(message = "小节类型不能为空")
    @EnumValid(enumeration = {1, 2}, message = "小节类型错误，只能是：1-视频，2-考试")
    private SectionType sectionType;

    @Schema(description = "课表id")
    @NotNull(message = "课表id不能为空")
    private Long lessonId;

    @Schema(description = "对应节的id")
    @NotNull(message = "节的id不能为空")
    private Long sectionId;

    @Schema(description = "视频总时长，单位秒")
    private Integer duration;

    @Schema(description = "视频的当前观看时长，单位秒，第一次提交填0")
    private Integer moment;

    @Schema(description = "提交时间")
    private LocalDateTime commitTime;
}
