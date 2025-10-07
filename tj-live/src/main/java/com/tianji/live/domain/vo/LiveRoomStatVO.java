package com.tianji.live.domain.vo;

import lombok.Data;

/**
 * @Author: fsq
 * @Date: 2025/10/7 12:47
 * @Version: 1.0
 */
@Data
public class LiveRoomStatVO {

    //主播粉丝数
    private Long fansCount;

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

}
