package com.tianji.course.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 课程信息
 */
@Data
@Schema(description = "课程信息")
public class CoursePageVO {
    @Schema(description = "课程id", example = "1")
    private Long id;

    @Schema(description = "课程名称", example = "Java")
    private String name;

    @Schema(description = "课程价格，单位分", example = "32900")
    private Long price;

    @Schema(description = "课程封面地址", example = "default-cover-url.jpg")
    private String coverUrl;

    @Schema(description = "课程分类，三级分类，以/隔开", example = "计算机科学/软件开发/编程")
    private String categories;

    @Schema(description = "课程章节数量", example = "25")
    private Integer sections;

    @Schema(description = "课程报名人数（销量）", example = "3920")
    private Integer sold;

    @Schema(description = "课程评价得分，45代表4.5星", example = "35")
    private Integer score;

    @Schema(description = "课程状态，1：待上架，2：已上架，3：已下架，4：已完结", example = "1")
    private Integer status;

    @Schema(description = "更新人名字", example = "张三")
    private String updaterName;

    @Schema(description = "更新时间", example = "2022-07-18T19:52:36")
    private LocalDateTime updateTime;

    @Schema(description = "课程编辑进度：1：基本信息已经保存，2：课程目录已经保存，3：课程视频已保存，4：课程题目已保存，5：课程老师已经保存", example = "1")
    private Integer step;

    @Schema(description = "课程发布时间")
    private LocalDateTime publishTime;

    @Schema(description = "下架时间")
    private LocalDateTime purchaseEndTime;

    public static final String[] EXCLUDE_FIELDS =
            {"free", "type", "teacher", "duration", "publishTime"};
}
