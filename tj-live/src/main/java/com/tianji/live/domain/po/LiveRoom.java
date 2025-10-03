package com.tianji.live.domain.po;

import java.time.LocalDateTime;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * 直播间实体类
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("live_room")
public class LiveRoom {
    /**
     * 直播间id
     */
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 主播用户id
     */
    private Long anchorId;

    /**
     * 直播间标题
     */
    private String roomTitle;

    /**
     * 直播间封面图URL
     */
    private String roomCover;

    /**
     * 直播间描述
     */
    private String roomDesc;

    /**
     * 直播间分类id
     */
    private Long categoryId;

    /**
     * 直播间状态(0-未开播,1-直播中,2-已关闭,3-禁播)
     */
    private Integer status;

    /**
     * 开始直播时间
     */
    private LocalDateTime startTime;

    /**
     * 结束直播时间
     */
    private LocalDateTime endTime;

    /**
     * 当前在线人数
     */
    private Integer onlineCount;

    /**
     * 最高在线人数
     */
    private Integer maxOnlineCount;

    /**
     * 点赞总数
     */
    private Long likeCount;

    /**
     * 分享次数
     */
    private Integer shareCount;

    /**
     * 是否私有直播间(0-公开,1-私有)
     */
    private Boolean isPrivate;

    /**
     * 私有直播间密码
     */
    private String password;

    /**
     * 是否被隐藏
     */
    private Boolean hidden;

    /**
     * 隐藏原因
     */
    private String hiddenReason;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}