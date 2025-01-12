package com.tianji.learning.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Schema(description = "课程计划信息")
public class LearningPlanVO {

    @Schema(description = "主键lessonId")
    private Long id;

    @Schema(description = "课程id")
    private Long courseId;

    @Schema(description = "课程名称")
    private String courseName;

    @Schema(description = "每周计划学习章节数")
    private Integer weekFreq;

    @Schema(description = "课程章节数量")
    private Integer sections;

    @Schema(description = "本周已学习章节数")
    private Integer weekLearnedSections;

    @Schema(description = "总已学习章节数")
    private Integer learnedSections;

    @Schema(description = "最近一次学习时间")
    private LocalDateTime latestLearnTime;
}
