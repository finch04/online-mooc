package com.tianji.message.constants;

/**
 * @Author: fsq
 * @Date: 2025/5/26 17:34
 * @Version: 1.0
 */

public interface RedisConstants {

    /**
     * websocket连接的Hashkey  完整格式为 ws:用户id
     */
    String WEBSOCKET_KEY="ws";

    /**
     * websocket连接的Hashkey前缀
     */
    String WEBSOCKET_KEY_PREFIX="ws";


    // 敏感词库在Redis中的存储Key
    String SENSITIVE_WORDS_KEY = "sensitive:words";
    String SENSITIVE_DICTIONARY_KEY = "sensitive:dictionary";
    // 敏感词库在Redis中缓存过期时间（默认2小时）
    long SENSITIVE_CACHE_TTL = 7200;

}
