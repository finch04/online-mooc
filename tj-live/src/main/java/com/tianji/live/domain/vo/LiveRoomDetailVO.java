package com.tianji.live.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 直播间实体类
 */

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "直播间详细信息")
public class LiveRoomDetailVO {
    /**
     * 直播间id
     */
    private Long id;

    /**
     * 主播用户id
     */
    private Long anchorId;

    /**
     * 主播用户名称
     */
    private String anchorName;

    /**
     * 主播用户头像url
     */
    private String anchorIcon;

    /**
     * 直播间标题
     */
    private String roomTitle;

    /**
     * 直播间描述
     */
    private String roomDesc;


    /**
     * 直播间公告
     */
    private String roomNotice;


    /**
     * 直播间封面图URL
     */
    private String roomCover;

    /**
     * 直播间状态(0-未开播,1-直播中,2-已关闭,3-禁播)
     */
    private Integer status;

    /**
     * 主播是否已关注
     */
    private Boolean followed;


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
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}