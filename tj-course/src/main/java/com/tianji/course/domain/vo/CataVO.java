package com.tianji.course.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * 课程目录
 */
@Data
@Schema(description = "课程目录")
public class CataVO {
    @Schema(description = "章、节、练习id", example = "1")
    private Long id;

    @Schema(description = "序号", example = "1")
    private Integer index;

    @Schema(description = "章节练习名称", example = "第一章")
    private String name;

    @Schema(description = "课程总时长,单位秒", example = "3600")
    private Integer mediaDuration;

    @Schema(description = "是否支持免费试看", example = "true")
    private Boolean trailer;

    @Schema(description = "媒资名称", example = "课程视频1")
    private String mediaName;

    @Schema(description = "媒资id", example = "101")
    private Long mediaId;

    @Schema(description = "目录类型1：章，2：节，3：测试", example = "1")
    private Integer type;

    @Schema(description = "题目数量", example = "5")
    private Integer subjectNum;

    @Schema(description = "题目总分", example = "25")
    private Integer totalScore;

    @Schema(description = "是否可以修改,默认不能修改", example = "false")
    private Boolean canUpdate = false;

    @Schema(description = "该章的所有小节和练习")
    private List<CataVO> sections;

    @Schema(description = "已上架最大序号，查看时值为空，编辑查看时小节必有值", example = "1")
    private Integer maxIndexOnShelf;

    @Schema(description = "已上架小节最大序号，查看时，值为空，编辑查看时小节必有字段", example = "1")
    private Integer maxSectionIndexOnShelf;
}
