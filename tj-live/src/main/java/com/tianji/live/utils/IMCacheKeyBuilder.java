package com.tianji.live.utils;

import org.springframework.context.annotation.Configuration;

@Configuration
public class IMCacheKeyBuilder {

    private static final String IM_TOKEN_PREFIX = "IM_TOKEN_";
    private static final String ROOM_MESSAGE_PREFIX = "ROOM_MESSAGE_";
    private static final String ROOM_BATCH_MESSAGE_PREFIX = "ROOM_BATCH_MESSAGE_";

    public String buildIMTokenCacheKey(String token) {
        return IM_TOKEN_PREFIX + token;
    }

    public String buildRoomUserCacheKey(String roomId) {
        return "ROOM_USER_" + roomId;
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

}