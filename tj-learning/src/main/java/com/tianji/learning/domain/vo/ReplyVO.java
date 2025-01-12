package com.tianji.learning.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Schema(description = "互动回答信息")
public class ReplyVO {
    @Schema(description = "id")
    private Long id;
    @Schema(description = "回答内容")
    private String content;
    @Schema(description = "是否匿名提问")
    private Boolean anonymity;
    @Schema(description = "是否隐藏")
    private Boolean hidden;
    @Schema(description = "评论数量")
    private Integer replyTimes;
    @Schema(description = "创建时间，也就是回答时间")
    private LocalDateTime createTime;
    @Schema(description = "当前回复者id")
    private Long userId;
    @Schema(description = "当前回复者昵称")
    private String userName;
    @Schema(description = "当前回复者头像")
    private String userIcon;
    @Schema(description = "当前回复者类型，2-学员，其它-老师")
    private Integer userType;
    @Schema(description = "是否点过赞")
    private Boolean liked;
    @Schema(description = "点赞数量")
    private Integer likedTimes;
    @Schema(description = "目标用户名字")
    private String targetUserName;
}
