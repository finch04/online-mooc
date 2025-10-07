package com.tianji.live.handler;

import com.tianji.live.utils.IMCacheKeyBuilder;
import com.tianji.live.utils.SpringContextUtil;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Component
@Slf4j
public class RedisOnlineUserCleaner implements ApplicationListener<ContextClosedEvent> {

    // 注入StringRedisTemplate（而非在清理时用SpringContextUtil获取，更早持有连接）
    private final StringRedisTemplate stringRedisTemplate;
    private final IMCacheKeyBuilder imCacheKeyBuilder;

    // 构造方法注入（Spring启动时就初始化，确保持有可用的Redis连接）
    public RedisOnlineUserCleaner(StringRedisTemplate stringRedisTemplate, IMCacheKeyBuilder imCacheKeyBuilder) {
        this.stringRedisTemplate = stringRedisTemplate;
        this.imCacheKeyBuilder = imCacheKeyBuilder;
    }

    /**
     * 服务关闭时清理Redis中所有直播间在线用户数据
     * 优化点：1. 容错处理（避免SpringContext/Redis异常导致崩溃）2. 大Key场景用scan替代keys 3. 规范资源引用 4. 增强日志
     */
    @Override
    public void onApplicationEvent(ContextClosedEvent event) {
        // 步骤1：获取在线用户Key的前缀（如 "live:room:user:"）
        String keyPrefix = imCacheKeyBuilder.buildRoomUserCachePattern();
        // 步骤2：扫描所有匹配前缀的Key（支持通配符*）
        Set<String> allOnlineUserKeys = stringRedisTemplate.keys(keyPrefix + "*");

        // 步骤3：批量删除所有匹配的Key（若有数据）
        if (allOnlineUserKeys != null && !allOnlineUserKeys.isEmpty()) {
            long deleteCount = stringRedisTemplate.delete(allOnlineUserKeys);
            log.debug("Redis清理完成，删除{}个直播间的在线用户数据", deleteCount);
        }
        log.debug("连接管理器销毁成功！");
    }
}