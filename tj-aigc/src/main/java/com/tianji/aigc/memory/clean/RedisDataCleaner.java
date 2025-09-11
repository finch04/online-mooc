package com.tianji.aigc.memory.clean;

import com.tianji.aigc.memory.redis.RedisChatMemoryRepository;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class RedisDataCleaner implements OriginalDataCleaner {

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Override
    public void clean(String conversationId) {
        String redisKey = RedisChatMemoryRepository.DEFAULT_PREFIX + conversationId;
        stringRedisTemplate.delete(redisKey);
        log.info("Redis原始数据已删除，conversationId: {}", conversationId);
    }

    @Override
    public DataSourceType getSupportedType() {
        return DataSourceType.REDIS;
    }
}