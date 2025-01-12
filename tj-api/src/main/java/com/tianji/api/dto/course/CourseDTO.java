package com.tianji.api.dto.course;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Schema(description = "课程信息")
public class CourseDTO {
    @Schema(description = "课程id")
    private Long id;
    @Schema(description = "课程名称")
    private String name;
    @Schema(description = "一级课程分类id")
    private Long categoryIdLv1;
    @Schema(description = "二级课程分类id")
    private Long categoryIdLv2;
    @Schema(description = "三级课程分类id")
    private Long categoryIdLv3;
    @Schema(description = "课程封面")
    private String coverUrl;
    @Schema(description = "创建时间")
    private LocalDateTime createTime;
    @Schema(description = "更新时间")
    private LocalDateTime updateTime;
    @Schema(description = "价格")
    private Integer price;
    @Schema(description = "视频播放时长")
    private Integer duration;
    @Schema(description = "课程有效期天数")
    private Integer validDuration;
    @Schema(description = "是否免费")
    private Boolean free;
    @Schema(description = "发布时间")
    private LocalDateTime publishTime;
    @Schema(description = "章节数")
    private Integer sections;
    @Schema(description = "课程状态")
    private Byte status;
    @Schema(description = "老师id")
    private Long teacher;
    @Schema(description = "课程类型，1：直播课程，2：录播课程")
    private Integer courseType;
    @Schema(description = "更新时间")
    private Long updater;
    @Schema(description = "课程进行到的步骤，1：基本信息，2：目录，3：课程视频，4：课程题目，5：课程老师")
    private Integer step;
    @Schema(description = "课程报名人数（销量）", example = "3920", defaultValue = "0")
    private Integer sold = 0;
    @Schema(description = "课程评价得分，45代表4.5星", example = "35", defaultValue = "0")
    private Integer score = 0;
    @Schema(description = "课程是否禁用,0:禁用，1：启用")
    private Integer enable;
}
