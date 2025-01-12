package com.tianji.course.domain.dto;

import com.tianji.common.exceptions.BadRequestException;
import com.tianji.common.utils.DateUtils;
import com.tianji.common.validate.Checker;
import com.tianji.course.constants.CourseErrorInfo;
import com.tianji.course.utils.CourseSaveBaseGroup;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

/**
 * 课程基本信息
 */
@Data
@Schema(description = "课程基本信息保存")
public class CourseBaseInfoSaveDTO implements Checker {
    @Schema(description = "课程id，新课程该值不能传，老课程必填", example = "1")
    private Long id;

    @Schema(description = "课程名称", example = "Java编程基础")
    @NotNull(message = CourseErrorInfo.Msg.COURSE_SAVE_NAME_NULL)
    private String name;

    @Schema(description = "三级课程分类id", example = "3")
    @NotNull(message = CourseErrorInfo.Msg.COURSE_SAVE_CATEGORY_NULL)
    private Long thirdCateId;

    @Schema(description = "封面链接url", example = "http://example.com/cover.jpg")
    @NotNull(message = CourseErrorInfo.Msg.COURSE_SAVE_COVER_URL_NULL, groups = CourseSaveBaseGroup.class)
    private String coverUrl;

    @Schema(description = "是否是免费", example = "false")
    @NotNull(message = CourseErrorInfo.Msg.COURSE_SAVE_FREE_NULL)
    private Boolean free;

    @Schema(description = "课程价格", example = "199")
    @Min(value = 0, message = CourseErrorInfo.Msg.COURSE_SAVE_PRICE_NEGATIVE)
    private Integer price;

    @Schema(description = "购买周期结束时间", example = "2023-12-31T23:59:59")
    @NotNull(message = CourseErrorInfo.Msg.COURSE_SAVE_PURCHASE_TIME_NULL)
    private LocalDateTime purchaseEndTime;

    @Schema(description = "课程介绍", example = "这是一门Java编程基础课程")
    @NotNull(message = CourseErrorInfo.Msg.COURSE_SAVE_INTRODUCE_NULL, groups = CourseSaveBaseGroup.class)
    private String introduce;

    @Schema(description = "使用人群", example = "初学者")
    @NotNull(message = CourseErrorInfo.Msg.COURSE_SAVE_USE_PEOPLE_NULL, groups = CourseSaveBaseGroup.class)
    private String usePeople;

    @Schema(description = "详情", example = "课程详细内容")
    @NotNull(message = CourseErrorInfo.Msg.COURSE_SAVE_DETAIL_NULL, groups = CourseSaveBaseGroup.class)
    private String detail;

    @Schema(description = "学习周期，0或不传表示没有期限，其他表示月数", example = "6")
    @NotNull(message = CourseErrorInfo.Msg.COURSE_SAVE_DURATION_NULL)
    private Integer validDuration;

    @Override
    public void check() {
        if (!free) { // 非免费
            if (price == null) { // 付费课程未设置价格
                throw new BadRequestException(CourseErrorInfo.Msg.COURSE_SAVE_PRICE_NULL);
            }
            if (price <= 0) { // 付费课程设置价格小于0
                throw new BadRequestException(CourseErrorInfo.Msg.COURSE_SAVE_PRICE_NEGATIVE);
            }
        } else { // 免费
            if (price != null && price > 0) { // 免费课程设置了价格
                throw new BadRequestException(CourseErrorInfo.Msg.COURSE_SAVE_PRICE_FREE);
            }
        }
        if (purchaseEndTime.isBefore(DateUtils.now())) {
            throw new BadRequestException(CourseErrorInfo.Msg.COURSE_SAVE_PURCHASE_ILLEGAL);
        }
    }
}
