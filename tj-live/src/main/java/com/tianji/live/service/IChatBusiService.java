package com.tianji.live.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tianji.api.client.unqid.UnqidClient;
import com.tianji.live.constants.IMConstants;
import com.tianji.live.protocol.GenericMessage;
import com.tianji.live.protocol.MessageBody;
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
public interface IChatBusiService {

    /**
     * 对消息做处理，包含缓存和聚合逻辑
     * @param message 通用消息对象
     */
    void handleMessage(GenericMessage message);

    /**
     * 获取房间历史消息
     * @param roomId 房间ID
     * @return 消息体列表
     */
    List<MessageBody> getRoomHistoryMessages(Long roomId);

    /**
     * 批量发送消息并清理List
     * @param roomId 房间ID
     * @param batchListKey 批量消息List键
     * @return void
     */
    void sendBatchMessages(Long roomId, String batchListKey);
}