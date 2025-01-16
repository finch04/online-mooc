package com.tianji.api.dto.course;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 课程基本信息
 */
@Data
@Schema(description = "课程基本信息")
public class CourseBaseInfoDTO {
    @Schema(description = "课程id", example = "1")
    private Long id;

    @Schema(description = "一级分类id", example = "1")
    private Long firstCateId;

    @Schema(description = "二级分类id", example = "2")
    private Long secondCateId;

    @Schema(description = "三级分类id", example = "3")
    private Long thirdCateId;

    @Schema(description = "课程创建人", example = "张三")
    private String createrName;

    private Long creater;

    @Schema(description = "创建时间", example = "2022-07-11T11:59:00")
    private LocalDateTime createTime;

    @Schema(description = "封面url", example = "http://example.com/cover.jpg")
    private String coverUrl;

    @Schema(description = "更新时间", example = "2022-07-11T11:59:00")
    private LocalDateTime updateTime;

    @Schema(description = "更新人名称", example = "李四")
    private String updaterName;

    private Long updater;

    @Schema(description = "课时总数量,去掉章，测试，用于编辑回显时该值为空", example = "10")
    private Integer cataTotalNum;

    @Schema(description = "课程评分，用于编辑回显时该值为空", example = "0.0")
    private Double coureScore = 0d;

    @Schema(description = "课程评分", example = "4")
    private Integer score;

    @Schema(description = "报名人数，用于编辑回显时该值为空", example = "0")
    private Integer enrollNum = 0;

    @Schema(description = "学习人数，用于编辑回显时该值为空", example = "0")
    private Integer studyNum = 0;

    @Schema(description = "退款人数，用于编辑回显时该值为空", example = "0")
    private Integer refundNum = 0;

    @Schema(description = "实付总金额，用于编辑回显时该值为空", example = "0")
    private Integer realPayAmount = 0;

    @Schema(description = "课程名称", example = "Java编程基础")
    private String name;

    @Schema(description = "课程分类名称，中间使用/隔开", example = "计算机科学/软件开发/编程")
    private String cateNames;

    @Schema(description = "课程价格", example = "199")
    private Integer price;

    @Schema(description = "购买有效期开始", example = "2022-07-11T11:59:00")
    private LocalDateTime purchaseStartTime;

    @Schema(description = "购买有效期结束", example = "2023-07-11T11:59:00")
    private LocalDateTime purchaseEndTime;

    @Schema(description = "有效期", example = "12")
    private Integer validDuration;

    @Schema(description = "课程介绍", example = "这是一门Java编程基础课程")
    private String introduce;

    @Schema(description = "使用人群", example = "初学者")
    private String usePeople;

    @Schema(description = "详情", example = "课程详细内容")
    private String detail;

    @Schema(description = "是否可以修改，默认不能修改", example = "false")
    private Boolean canUpdate = false;

    @Schema(description = "是否免费", example = "false")
    private Boolean free;

    @Schema(description = "步骤,1:已保存基本信息，2：已保存课程目录，3：已保存课程视频，4：已保存题目，5：已保存课程老师", example = "1")
    private Integer step;

    @Schema(description = "课程状态，1：待上架，2：已上架，3：下架，4：已完结", example = "1")
    private Integer status;
}
