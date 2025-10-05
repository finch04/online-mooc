package com.tianji.live.utils;

import org.springframework.context.annotation.Configuration;

@Configuration
public class IMCacheKeyBuilder {

    private static final String IM_TOKEN_PREFIX = "IM_TOKEN_";
    public String buildIMTokenCacheKey(String token){
        return IM_TOKEN_PREFIX+token;
    }

    public String buildRoomUserCacheKey(String roomId){
        return "ROOM_USER_"+roomId;
    }

    public String buildRoomMessageCacheKey(Long roomId){
        return "ROOM_MESSAGE_"+roomId;
    }

    /**
     * 构建房间消息缓存键的匹配模式，用于批量查询所有房间的消息缓存键
     * @return 匹配所有房间消息缓存键的模式字符串
     */
    public String buildRoomMessageCachePattern() {
        // 匹配所有以"ROOM_MESSAGE"为前缀的键
        return "ROOM_MESSAGE_" + "*";
    }

    /**
     * 从缓存键中解析出房间ID
     * 需要根据实际的key生成规则来实现
     */
    public Long parseRoomIdFromCacheKey(String cacheKey) {
        // 验证键格式是否正确
        if (cacheKey == null || !cacheKey.startsWith("ROOM_MESSAGE_")) {
            throw new IllegalArgumentException("无效的房间消息缓存键：" + cacheKey);
        }
        // 截取前缀后的部分作为房间ID
        String roomIdStr = cacheKey.substring("ROOM_MESSAGE_".length());
        return Long.parseLong(roomIdStr);
    }

}
