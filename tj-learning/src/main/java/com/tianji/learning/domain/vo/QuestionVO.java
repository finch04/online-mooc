package com.tianji.learning.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Schema(description = "用户端互动问题信息")
public class QuestionVO {
    @Schema(description = "主键id")
    private Long id;
    @Schema(description = "互动问题名称")
    private String title;
    @Schema(description = "互动问题描述")
    private String description;
    @Schema(description = "回答数量，0表示没有回答")
    private Integer answerTimes;
    @Schema(description = "创建时间", example = "2022-7-18 19:52:36")
    private LocalDateTime createTime;
    @Schema(description = "是否匿名提问")
    private Boolean anonymity;
    @Schema(description = "提问者id")
    private Long userId;
    @Schema(description = "提问者昵称")
    private String userName;
    @Schema(description = "提问者头像")
    private String userIcon;
    @Schema(description = "最新的回答信息")
    private String latestReplyContent;
    @Schema(description = "最新的回答者昵称")
    private String latestReplyUser;
}
