package com.tianji.live.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tianji.api.client.unqid.UnqidClient;
import com.tianji.live.constants.IMConstants;
import com.tianji.live.protocol.GenericMessage;
import com.tianji.live.protocol.MessageBody;
import com.tianji.live.service.IChatBusiService;
import com.tianji.live.service.IIMService;
import com.tianji.live.utils.IMCacheKeyBuilder;
import jakarta.annotation.Resource;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * Author： fsq
 * Description：对消息做处理--使用List存储历史消息，List实现削峰批量发送（基于插入顺序自然排序）
 **/
@Service
@RequiredArgsConstructor
public class ChatBusiServiceImpl implements IChatBusiService {

    private static final ObjectMapper objectMapper = new ObjectMapper();
    private Logger logger = LoggerFactory.getLogger(ChatBusiServiceImpl.class);
    // 历史消息缓存数量
    @Value("${tj.im.message.cachesize:5}")
    private int roomMessageCacheSize;
    // 削峰批次最小数量
    @Value("${tj.im.message.batchsize:5}")
    private int batchSendSize = 5;
    private final StringRedisTemplate redisTemplate;
    @Resource
    private final IIMService imRPCService;
    @Resource
    private final UnqidClient unqidClient;
    @Resource
    private final IMCacheKeyBuilder imCacheKeyBuilder;


    /**
     * 对消息做处理，包含缓存和聚合逻辑
     */
    @Override
    public void handleMessage(GenericMessage message){
        logger.info("处理消息："+message);
        Long roomId = message.getRoomId();
        String historyListKey = imCacheKeyBuilder.buildRoomMessageCacheKey(roomId);
        String batchListKey = imCacheKeyBuilder.buildRoomBatchMessageKey(roomId); // 修改为List结构的键

        // 礼物消息不缓存，直接推送
        if(message.getType().equals(IMConstants.MESSAGE_TYPE_GIFT)){
            imRPCService.pushChatMessage(roomId,message);
        }
        // 聊天消息和进入广播需要处理
        else if(message.getType().equals(IMConstants.MESSAGE_TYPE_CHAT) ||
                message.getType().equals(IMConstants.MESSAGE_TYPE_JOIN_ROOM)){

            for (MessageBody messageBody : message.getBody()) {
                try {
                    messageBody.setMsgId(unqidClient.getUnSeqId());
                    messageBody.setUserId(message.getFromUserId());
                    messageBody.setUserName(message.getFromUserName());
                    // 移除时间戳字段，依赖Redis List的插入顺序维护时间序

                    // 1. 维护历史消息List（最新20条）
                    // 使用右插左截的方式，保证List头部是最新消息
                    String messageJson = objectMapper.writeValueAsString(messageBody);
                    redisTemplate.opsForList().rightPush(historyListKey, messageJson);
                    redisTemplate.opsForList().trim(historyListKey, -roomMessageCacheSize, -1); // 只保留最后N条
                    redisTemplate.expire(historyListKey, 24, TimeUnit.HOURS);

                    // 2. 加入削峰List（使用List的自然插入顺序维护时间序）
                    redisTemplate.opsForList().rightPush(batchListKey, messageJson);
                    redisTemplate.expire(batchListKey, 24, TimeUnit.HOURS);

                } catch (JsonProcessingException e) {
                    logger.error("消息序列化失败", e);
                    continue;
                }
            }

            // 3. 检查List中消息数量，达到批次阈值则立即发送
            Long messageCount = redisTemplate.opsForList().size(batchListKey);
            if (messageCount != null && messageCount >= batchSendSize) {
                sendBatchMessages(roomId, batchListKey);
            }
        }
    }

    /**
     * 获取房间历史消息（默认5条）
     */
    @Override
    public List<MessageBody> getRoomHistoryMessages(Long roomId) {
        String historyListKey = imCacheKeyBuilder.buildRoomMessageCacheKey(roomId);
        // 由于采用右插左截，range(0, -1)会按时间正序返回（最早的在前，最新的在后）
        List<String> messageJsonList = redisTemplate.opsForList().range(historyListKey, 0, -1);

        if (messageJsonList == null || messageJsonList.isEmpty()) {
            return new ArrayList<>();
        }

        // 反序列化为MessageBody列表（已按插入顺序排序）
        return messageJsonList.stream()
                .map(json -> {
                    try {
                        return objectMapper.readValue(json, MessageBody.class);
                    } catch (JsonProcessingException e) {
                        logger.error("消息反序列化失败", e);
                        return null;
                    }
                })
                .filter(msg -> msg != null)
                .collect(Collectors.toList());
    }


    /**
     * 批量发送消息并清理List
     */
    @Override
    public void sendBatchMessages(Long roomId, String batchListKey) {
        // 获取List中所有消息（按插入顺序返回）
        Long messageCount = redisTemplate.opsForList().size(batchListKey);
        if (messageCount == null || messageCount == 0) {
            return;
        }

        // 从List左侧开始获取所有元素（保持插入顺序）
        List<String> messageJsonList = redisTemplate.opsForList().range(batchListKey, 0, -1);
        if (messageJsonList == null || messageJsonList.isEmpty()) {
            return;
        }

        // 反序列化（已按插入顺序排序，无需额外排序）
        List<MessageBody> messageList = messageJsonList.stream()
                .map(json -> {
                    try {
                        return objectMapper.readValue(json, MessageBody.class);
                    } catch (JsonProcessingException e) {
                        logger.error("消息反序列化失败", e);
                        return null;
                    }
                })
                .filter(msg -> msg != null)
                .collect(Collectors.toList());

        logger.info("批量推送房间 {} 的消息，共 {} 条", roomId, messageList.size());

        try {
            // 推送消息
            GenericMessage downMessage = new GenericMessage();
            downMessage.setRoomId(roomId);
            downMessage.setBody(messageList);
            downMessage.setType(IMConstants.MESSAGE_TYPE_CHAT);
            imRPCService.pushChatMessage(roomId, downMessage);
            logger.info("房间 {} 批量消息推送成功", roomId);

            // 发送成功后清空List
            redisTemplate.delete(batchListKey);
        } catch (Exception e) {
            logger.error("房间 {} 批量消息推送失败，失败原因：{}", roomId, e.getMessage());
            // 推送失败保留List中的消息，等待下次重试
        }
    }
}