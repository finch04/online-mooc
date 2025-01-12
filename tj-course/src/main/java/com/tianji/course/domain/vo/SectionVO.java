package com.tianji.course.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 小节信息及学习进度
 */
@Data
@Schema(description = "小节信息及学习进度")
public class SectionVO {
    @Schema(description = "对应章节的id", example = "1")
    private Long id;

    @Schema(description = "对应章节的名称", example = "第一节")
    private String name;

    @Schema(description = "小节序号", example = "1")
    private Integer index;

    @Schema(description = "对应章节的类型，2-视频（小节），3-考试", example = "2")
    private Integer type;

    @Schema(description = "视频总时长，单位秒", example = "3600")
    private Integer mediaDuration;

    @Schema(description = "媒资id", example = "101")
    private Long mediaId;

    @Schema(description = "是否支持免费试看", example = "true")
    private Boolean trailer;

    @Schema(description = "题目数量", example = "5")
    private Integer subjectNum;

    @Schema(description = "是否包含小节测试", example = "true")
    private Boolean hasTest;

    @Schema(description = "视频的当前观看时长，单位秒", example = "1800")
    private Integer moment = 0;

    @Schema(description = "是否完成学习，默认false", example = "false")
    private Boolean finished;

    public Boolean getHasTest() {
        return subjectNum != null && subjectNum > 0;
    }
}
