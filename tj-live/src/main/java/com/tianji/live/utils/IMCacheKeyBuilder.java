package com.tianji.live.utils;

import org.springframework.context.annotation.Configuration;

@Configuration
public class IMCacheKeyBuilder {

    private static final String IM_TOKEN_PREFIX = "IM_TOKEN_";
    private static final String ROOM_MESSAGE_PREFIX = "ROOM_MESSAGE_";
    private static final String ROOM_BATCH_MESSAGE_PREFIX = "ROOM_BATCH_MESSAGE_";
    private static final String ROOM_USER_PREFIX = "ROOM_USER_";
    private static final String ROOM_MAX_ONLINE_PREFIX = "ROOM_MAX_ONLINE_";
    //用户关注
    public static final String FOLLOW_PREFIX = "user:follow:";  // 关注的人
    public static final String FANS_PREFIX = "user:fans:";      // 粉丝

    // 直播间点赞数Key前缀（String类型，存总点赞数）
    public static final String ROOM_LIKE_COUNT_PREFIX = "ROOM_LIKE_COUNT_";

    //用户关注
    public static final String IM_FOLLOW_PREFIX = "user:follow:";  // 关注的人
    public static final String IM_FANS_PREFIX = "user:fans:";      // 粉丝


    public String buildIMTokenCacheKey(String token) {
        return IM_TOKEN_PREFIX + token;
    }

    public String buildRoomUserCacheKey(String roomId) {
        return ROOM_USER_PREFIX + roomId;
    }

    // 构建房间历史消息List的缓存键（用于存储最新20条消息）
    public String buildRoomMessageCacheKey(Long roomId) {
        return ROOM_MESSAGE_PREFIX + roomId;
    }

    // 构建房间批量发送消息Set的缓存键（用于削峰，批量发送）
    public String buildRoomBatchMessageKey(Long roomId) {
        return ROOM_BATCH_MESSAGE_PREFIX + roomId;
    }

    /**
     * 构建房间历史消息缓存键的匹配模式
     */
    public String buildRoomMessageCachePattern() {
        return ROOM_MESSAGE_PREFIX + "*";
    }

    /**
     * 构建房间用户缓存键的匹配模式
     */
    public String buildRoomUserCachePattern() {
        return ROOM_USER_PREFIX + "*";
    }

    /**
     * 构建房间批量消息缓存键的匹配模式（用于定时任务批量查询）
     */
    public String buildRoomBatchMessagePattern() {
        return ROOM_BATCH_MESSAGE_PREFIX + "*";
    }

    /**
     * 从历史消息缓存键中解析出房间ID
     */
    public Long parseRoomIdFromCacheKey(String cacheKey) {
        if (cacheKey == null || !cacheKey.startsWith(ROOM_MESSAGE_PREFIX)) {
            throw new IllegalArgumentException("无效的房间消息缓存键：" + cacheKey);
        }
        String roomIdStr = cacheKey.substring(ROOM_MESSAGE_PREFIX.length());
        return Long.parseLong(roomIdStr);
    }

    /**
     * 从批量消息缓存键中解析出房间ID
     */
    public Long parseRoomIdFromBatchKey(String batchKey) {
        if (batchKey == null || !batchKey.startsWith(ROOM_BATCH_MESSAGE_PREFIX)) {
            throw new IllegalArgumentException("无效的房间批量消息缓存键：" + batchKey);
        }
        String roomIdStr = batchKey.substring(ROOM_BATCH_MESSAGE_PREFIX.length());
        return Long.parseLong(roomIdStr);
    }


    // 最高在线人数缓存键
    public String buildRoomMaxOnlineKey(String roomId) {
        return ROOM_MAX_ONLINE_PREFIX + roomId;
    }

    // 房间点赞数缓存键
    public String buildRoomLikeCountKey(String roomId) {
        return ROOM_LIKE_COUNT_PREFIX + roomId;
    }

    //用户关注
    public String buildUserFollowCacheKey(Long userId) {
        return FOLLOW_PREFIX + userId;
    }

    //用户粉丝
    public String buildUserFansCacheKey(Long userId) {
        return FANS_PREFIX + userId;
    }

    //用户关注
    public String buildUserFollowCachePattern() {
        return FOLLOW_PREFIX + "*";
    }

    //用户粉丝
    public String buildUserFansCachePattern() {
        return FANS_PREFIX + "*";
    }

    //（从Key中截取：user:follow:123 → 123）
    public Long parseUserIdFromCacheKey(String cacheKey) {
        if (cacheKey == null || !cacheKey.startsWith(FOLLOW_PREFIX)) {
            throw new IllegalArgumentException("无效的用户关注缓存键：" + cacheKey);
        }
        String userIdStr = cacheKey.substring(FOLLOW_PREFIX.length());
        return Long.parseLong(userIdStr);
    }

}