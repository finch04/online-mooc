package com.tianji.learning.domain.vo;

import com.tianji.learning.enums.LessonStatus;
import com.tianji.learning.enums.PlanStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Schema(description = "课程表信息")
public class LearningLessonVO {

    @Schema(description = "主键lessonId")
    private Long id;

    @Schema(description = "课程id")
    private Long courseId;

    @Schema(description = "课程名称")
    private String courseName;

    @Schema(description = "课程封面")
    private String courseCoverUrl;

    @Schema(description = "课程章节数量")
    private Integer sections;

    @Schema(description = "课程状态，0-未学习，1-学习中，2-已学完，3-已失效")
    private LessonStatus status;

    @Schema(description = "总已学习章节数")
    private Integer learnedSections;

    @Schema(description = "总已报名课程数")
    private Integer courseAmount;

    @Schema(description = "课程购买时间")
    private LocalDateTime createTime;

    @Schema(description = "课程过期时间，如果为null代表课程永久有效")
    private LocalDateTime expireTime;

    @Schema(description = "计划的学习频率")
    private Integer weekFreq;

    @Schema(description = "习计划状态，0-没有计划，1-计划进行中")
    private PlanStatus planStatus;

    @Schema(description = "最近学习的小节名")
    private String latestSectionName;

    @Schema(description = "最近学习的小节编号")
    private Integer latestSectionIndex;
}
