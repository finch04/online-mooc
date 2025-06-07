package com.tianji.chat.config;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.json.JSONUtil;
import com.tianji.chat.domain.po.ChatSession;
import com.tianji.chat.service.IChatSessionService;
import com.tianji.chat.utils.DataDelayTaskHandler;
import com.tianji.common.utils.UserContext;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.store.memory.chat.ChatMemoryStore;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static dev.langchain4j.data.message.ChatMessageDeserializer.messagesFromJson;
import static dev.langchain4j.data.message.ChatMessageSerializer.messageToJson;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class PersistentChatMemoryStore implements ChatMemoryStore {

    private final StringRedisTemplate redisTemplate;

    private final IChatSessionService chatSessionService;

    private final DataDelayTaskHandler dataDelayTaskHandler;

    private final ExecutorService executorService = Executors.newCachedThreadPool();

    private static final String REDIS_PREFIX = "chat:memory:";

    private String getKey(Object memoryId) {
        return REDIS_PREFIX + UserContext.getUser() + memoryId;
    }

    @Override
    public List<ChatMessage> getMessages(Object memoryId) {
        try {
            List<String> messageList = redisTemplate.opsForList().range(getKey(memoryId), 0, -1);

            if (CollUtil.isNotEmpty(messageList)) {
                String json = messageList.toString();
                return messagesFromJson(json);
            }
            // 获取不到对话历史，则从数据库中获取
            List<ChatSession> chatSessionList = chatSessionService.lambdaQuery()
                    .eq(ChatSession::getUserId, UserContext.getUser())
                    .eq(ChatSession::getSessionId, memoryId)
                    .orderByAsc(ChatSession::getSegmentIndex)
                    .list();

            // 判断是否为空
            if (CollUtil.isNotEmpty(chatSessionList)) {
                messageList = chatSessionList.stream()
                        .map(ChatSession::getContent)
                        .collect(Collectors.toList());

                // 缓存到Redis
                List<String> finalMessageList = messageList;
                executorService.submit(() -> {
                    try {
                        redisTemplate.opsForList().rightPushAll(getKey(memoryId), finalMessageList);
                    } catch (Exception e) {
                        log.error("同步数据库到 Redis 失败", e);
                    }
                });

                return messagesFromJson(messageList.toString());
            }

            return Collections.emptyList();
        } catch (Exception e) {
            log.error("读取对话历史失败", e);
            return Collections.emptyList();
        }
    }

    @Override
    public void updateMessages(Object memoryId, List<ChatMessage> messages) {
        if (messages == null || messages.isEmpty()) {
            return;
        }
        try {
            // 将最新的一条数据存储到Redis中
            String json = messageToJson(messages.get(messages.size() - 1));
            redisTemplate.opsForList().rightPush(getKey(memoryId), json);
            // 开启延时任务
            // 封装实体类
            Map<String, Object> map = new HashMap<>();
            map.put("key", getKey(memoryId));
            map.put("num", messages.size());
            String jsonStr = JSONUtil.toJsonStr(map);
            dataDelayTaskHandler.addDelayedTask(jsonStr, 1, TimeUnit.MINUTES);
        } catch (Exception e) {
            log.error("更新对话历史失败", e);
        }
    }

    @Override
    public void deleteMessages(Object memoryId) {
        try {
            redisTemplate.delete(getKey(memoryId));
        } catch (Exception e) {
            log.error("删除对话历史失败", e);
        }
    }
}
