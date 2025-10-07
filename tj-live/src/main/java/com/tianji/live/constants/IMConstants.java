package com.tianji.live.constants;


/**
 * @Author: fsq
 * @Date: 2025/10/3 21:05
 * @Version: 1.0
 */
public class IMConstants {

    public static final String PROP_USER_ID="UserId";
    public static final String PROP_ROOM_ID="RoomId";
    public static final String PROP_USER_NAME="UserName";

    public static final String MESSAGE_HEARTBEAT="Heartbeat";

    // 消息类型
    public static final int MESSAGE_TYPE_JOIN_ROOM=0;      // 加入房间
    public static final int MESSAGE_TYPE_EXIT_ROOM=1;      // 退出房间
    public static final int MESSAGE_TYPE_CHAT=2;           // 聊天消息
    public static final int MESSAGE_TYPE_GIFT=5;           // 礼物消息
    public static final int MESSAGE_TYPE_NOTICE=6;

    // 消息缓存数量
    public static final int MESSAGE_CACHE_SIZE=20;

    //用户关注
    public static final String FOLLOW_PREFIX = "user:follow:";  // 关注的人
    public static final String FANS_PREFIX = "user:fans:";      // 粉丝

}
