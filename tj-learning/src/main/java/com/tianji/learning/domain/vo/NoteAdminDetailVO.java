package com.tianji.learning.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Schema(description = "管理端笔记详情信息")
public class NoteAdminDetailVO {
    @Schema(description = "主键id")
    private Long id;
    @Schema(description = "课程名称")
    private String courseName;
    @Schema(description = "章名称")
    private String chapterName;
    @Schema(description = "节名称")
    private String sectionName;
    @Schema(description = "课程分类名称，以/拼接")
    private String categoryNames;
    @Schema(description = "笔记内容")
    private String content;
    @Schema(description = "记录笔记时的视频播放时间，单位秒")
    private Integer noteMoment;
    @Schema(description = "被采集次数")
    private Integer usedTimes;
    @Schema(description = "用户端是否被隐藏")
    private Boolean hidden;
    @Schema(description = "作者名称")
    private String authorName;
    @Schema(description = "作者手机号")
    private String authorPhone;
    @Schema(description = "笔记发布时间", example = "2022-7-18 19:52:36")
    private LocalDateTime createTime;
    @Schema(description = "采集过笔记的用户名集合")
    private List<String> gathers;
}
