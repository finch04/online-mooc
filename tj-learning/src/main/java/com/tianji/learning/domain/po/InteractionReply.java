package com.tianji.learning.domain.po;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.IdType;
import java.time.LocalDateTime;
import com.baomidou.mybatisplus.annotation.TableId;
import java.io.Serializable;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * <p>
 * 互动问题的回答或评论
 * </p>
 *
 * @author fsq
 * @since 2023-10-23
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("interaction_reply")
@ApiModel(value="InteractionReply对象", description="互动问题的回答或评论")
public class InteractionReply implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "互动问题的回答id")
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    @ApiModelProperty(value = "互动问题问题id")
    private Long questionId;

    @ApiModelProperty(value = "回复的上级回答id")
    private Long answerId;

    @ApiModelProperty(value = "回答者id")
    private Long userId;

    @ApiModelProperty(value = "回答内容")
    private String content;

    @ApiModelProperty(value = "回复的目标用户id")
    private Long targetUserId;

    @ApiModelProperty(value = "回复的目标回复id")
    private Long targetReplyId;

    @ApiModelProperty(value = "评论数量")
    private Integer replyTimes;

    @ApiModelProperty(value = "点赞数量")
    private Integer likedTimes;

    @ApiModelProperty(value = "是否被隐藏，默认false")
    private Boolean hidden;

    @ApiModelProperty(value = "是否匿名，默认false")
    private Boolean anonymity;

    @ApiModelProperty(value = "创建时间")
    private LocalDateTime createTime;

    @ApiModelProperty(value = "更新时间")
    private LocalDateTime updateTime;


}
