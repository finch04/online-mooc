package com.tianji.learning.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import jakarta.validation.constraints.NotNull;
import lombok.NoArgsConstructor;

/**
 * 互动回答信息
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "互动回答信息")
public class ReplyDTO {

    @Schema(description = "用户id，可以为空，为空表示是当前登录用户")
    private Long userId;

    @Schema(description = "回答内容")
    @NotNull(message = "回答内容不能为空")
    private String content;

    @Schema(description = "是否匿名提问")
    private Boolean anonymity;

    @Schema(description = "互动问题id")
    @NotNull(message = "问题id不能为空")
    private Long questionId;

    @Schema(description = "回复的上级回答id，没有可不填")
    private Long answerId;

    @Schema(description = "回复的目标回复id，没有可不填")
    private Long targetReplyId;

    @Schema(description = "回复的目标用户id，没有可不填")
    private Long targetUserId;

    @Schema(description = "标记是否是学生提交的回答，默认true")
    private Boolean isStudent = true;
}
