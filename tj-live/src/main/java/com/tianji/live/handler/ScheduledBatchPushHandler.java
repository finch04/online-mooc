package com.tianji.live.handler;

import com.tianji.live.service.impl.ChatBusiServiceImpl;
import com.tianji.live.utils.IMCacheKeyBuilder;
import jakarta.annotation.Resource;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Set;

/**
 * @Author: fsq
 * @Date: 2025/10/6 22:03
 * @Version: 1.0
 */
@Component
@RequiredArgsConstructor
public class ScheduledBatchPushHandler {

    private Logger logger = LoggerFactory.getLogger(ChatBusiServiceImpl.class);
    private final StringRedisTemplate redisTemplate;
    @Resource
    private final IMCacheKeyBuilder imCacheKeyBuilder;
    @Resource
    private final ChatBusiServiceImpl chatBusiService;

    /**
     * 定时任务：每5秒检查并发送所有房间的批量消息（处理不足批次的情况）
     */
    @Scheduled(fixedRate = 5000)
    public void scheduledBatchPush() {
//        logger.info("开始执行定时批量推送任务");

        // 获取所有房间的批量消息List键
        Set<String> batchListKeys = redisTemplate.keys(imCacheKeyBuilder.buildRoomBatchMessagePattern());

        if (batchListKeys != null && !batchListKeys.isEmpty()) {
            for (String batchListKey : batchListKeys) {
                // 提取房间ID
                Long roomId = imCacheKeyBuilder.parseRoomIdFromBatchKey(batchListKey);
                // 无论数量多少，强制发送当前List中的所有消息
                chatBusiService.sendBatchMessages(roomId, batchListKey);
            }
        }
//        logger.info("定时批量推送任务执行完毕");
    }

}
