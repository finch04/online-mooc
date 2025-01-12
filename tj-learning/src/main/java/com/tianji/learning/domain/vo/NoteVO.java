package com.tianji.learning.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Schema(description = "笔记信息")
public class NoteVO {
    @Schema(description = "主键id")
    private Long id;
    @Schema(description = "笔记内容")
    private String content;
    @Schema(description = "记录笔记时的视频播放时间，单位秒")
    private Integer noteMoment;
    @Schema(description = "是否是隐私笔记，默认false")
    private Boolean isPrivate;
    @Schema(description = "是否是采集的笔记")
    private Boolean isGathered;
    @Schema(description = "作者id")
    private Long authorId;
    @Schema(description = "作者名字")
    private String authorName;
    @Schema(description = "作者头像")
    private String authorIcon;
    @Schema(description = "笔记发布时间", example = "2022-7-18 19:52:36")
    private LocalDateTime createTime;
}
