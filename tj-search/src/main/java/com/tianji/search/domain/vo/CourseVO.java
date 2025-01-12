package com.tianji.search.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "课程信息")
public class CourseVO {
    @Schema(description = "课程id", example = "1")
    private Long id;
    @Schema(description = "课程名称", example = "Java")
    private String name;
    @Schema(description = "课程价格，单位分", example = "32900")
    private Long price;
    @Schema(description = "老师名字", example = "张老师")
    private String teacher;
    @Schema(description = "老师头像", example = "default-user-icon.jpg")
    private String icon;
    @Schema(description = "课程时长，单位秒", example = "3280")
    private Integer duration;
    @Schema(description = "课程封面地址", example = "default-cover-url.jpg")
    private String coverUrl;
    @Schema(description = "课程章节数量", example = "25")
    private Integer sections;
    @Schema(description = "课程报名人数（销量）", example = "3920")
    private Integer sold;

    public static final String[] EXCLUDE_FIELDS =
            {"categoryIdLv1", "categoryIdLv2", "categoryIdLv3", "free",
                    "publishTime", "type", "status", "score"};
}
