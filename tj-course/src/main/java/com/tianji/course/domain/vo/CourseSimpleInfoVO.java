package com.tianji.course.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 课程简单信息
 */
@Data
@Schema(description = "课程简单信息")
public class CourseSimpleInfoVO {
    @Schema(description = "课程id", example = "1")
    private Long id;

    @Schema(description = "课程名称", example = "Java编程基础")
    private String name;

    @Schema(description = "封面url", example = "http://example.com/cover.jpg")
    private String coverUrl;

    @Schema(description = "价格", example = "199")
    private Integer price;

    @Schema(description = "一级分类id", example = "1")
    private Long firstCateId;

    @Schema(description = "二级分类id", example = "2")
    private Long secondCateId;

    @Schema(description = "三级分类id", example = "3")
    private Long thirdCateId;

    @Schema(description = "章节数量", example = "25")
    private Integer sectionNum;

    @Schema(description = "课程有效期", example = "12")
    private Integer validDuration;

    @Schema(description = "课程过期时间", example = "2023-07-11T11:59:00")
    private LocalDateTime purchaseEndTime;
}
