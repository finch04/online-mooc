package com.tianji.live.service;

import com.tianji.api.client.unqid.UnqidClient;
import com.tianji.live.constants.IMConstants;
import com.tianji.live.protocol.GenericMessage;
import com.tianji.live.protocol.MessageBody;
import com.tianji.live.utils.IMCacheKeyBuilder;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * Author： roy
 * Description：对消息做处理--简单实现
 **/
@Service
public class ChatBusiService {

    private Logger logger = LoggerFactory.getLogger(ChatBusiService.class);

    @Value("${tl-live.message.cachesize:5}")
    private int roomMessageCacheSize;

    @Resource(name = "messageBodyRedisTemplate")
    private RedisTemplate<String, MessageBody> redisTemplate;
    @Resource
    private IIMRPCService imRPCService;
    @Resource
    private UnqidClient unqidClient;
    @Resource
    private IMCacheKeyBuilder imCacheKeyBuilder;


    /**
     * 对消息做处理，包含缓存和聚合逻辑
     */
    public void handleMessage(GenericMessage message){
        logger.info("处理消息："+message);
        Long roomId = message.getRoomId();
        String cacheKey = imCacheKeyBuilder.buildRoomMessageCacheKey(roomId);

        // 礼物消息不缓存，直接推送
        if(message.getType().equals(IMConstants.MESSAGE_TYPE_GIFT)){
            imRPCService.pushChatMessage(roomId,message);
            // 聊天消息，缓存Redis，满足条件时推送
        }else{
            for (MessageBody messageBody : message.getBody()) {
                messageBody.setMsgId(unqidClient.getUnSeqId());
                messageBody.setUserId(message.getFromUserId());
                messageBody.setUserName(message.getFromUserName());
                redisTemplate.opsForSet().add(cacheKey, messageBody);

                // 设置缓存过期时间，避免长期未推送的消息占用空间
                redisTemplate.expire(cacheKey, 30, TimeUnit.MINUTES);
            }

            // 检查是否达到推送阈值
            checkAndPushMessages(roomId, cacheKey);
        }
    }

    /**
     * 检查消息数量并推送
     */
    private void checkAndPushMessages(Long roomId, String cacheKey) {
        Set<MessageBody> roomMessages = redisTemplate.opsForSet().members(cacheKey);
        if (roomMessages != null && roomMessages.size() >= roomMessageCacheSize) {
            pushAndClearMessages(roomId, cacheKey, roomMessages);
        }
    }

    /**
     * 定时任务：每30秒检查并推送所有房间的缓存消息
     * 确保即使消息数量不足，也能定时推送
     */
    @Scheduled(fixedRate = 5000) // 5秒执行一次
    public void scheduledPushMessages() {
        logger.info("开始执行定时消息推送任务");

        // 获取所有房间的消息缓存键（实际实现需要根据你的key命名规则来获取）
        Set<String> roomCacheKeys = redisTemplate.keys(imCacheKeyBuilder.buildRoomMessageCachePattern());

        if (roomCacheKeys != null && !roomCacheKeys.isEmpty()) {
            for (String cacheKey : roomCacheKeys) {
                // 提取房间ID（需要根据你的key格式来解析）
                Long roomId = imCacheKeyBuilder.parseRoomIdFromCacheKey(cacheKey);

                Set<MessageBody> roomMessages = redisTemplate.opsForSet().members(cacheKey);
                if (roomMessages != null && !roomMessages.isEmpty()) {
                    logger.info("定时任务推送房间 {} 的消息，共 {} 条", roomId, roomMessages.size());
                    pushAndClearMessages(roomId, cacheKey, roomMessages);
                }
            }
        }
        logger.info("定时消息推送任务执行完毕");
    }

    /**
     * 推送消息并清理缓存
     */
    private void pushAndClearMessages(Long roomId, String cacheKey, Set<MessageBody> messages) {
        try {
            GenericMessage downMessage = new GenericMessage();
            downMessage.setRoomId(roomId);
            downMessage.setBody(messages.stream().toList());
            downMessage.setType(IMConstants.MESSAGE_TYPE_CHAT);

            imRPCService.pushChatMessage(roomId, downMessage);
            logger.info("房间 {} 推送消息 {} 条成功", roomId, messages.size());

            // 清理缓存
            redisTemplate.delete(cacheKey);
        } catch (Exception e) {
            logger.error("房间 {} 消息推送失败，失败原因：{}", roomId, e.getMessage());
            // 可以考虑将消息移到失败队列，进行重试
        }
    }


}
