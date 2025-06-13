package com.tianji.chat.config;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.tianji.chat.domain.po.ChatSession;
import com.tianji.chat.domain.po.UserSession;
import com.tianji.chat.service.IChatSessionService;
import com.tianji.chat.service.IUserSessionService;
import com.tianji.chat.utils.DataDelayTaskHandler;
import com.tianji.common.utils.UserContext;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.UserMessage;
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

import static com.tianji.chat.constants.RedisConstants.*;
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

    private final IUserSessionService userSessionService;


    private String getKey(Object sessionId) {
        Long userId = UserContext.getUser();
        if(userId==null){
            UserSession one = userSessionService.lambdaQuery().eq(UserSession::getSessionId, sessionId).one();
            userId = one.getUserId();
        }
        //示例：chat:memory:2:1
        return CHAT_MEMORY_KEY_PREFIX + userId +":"+ sessionId ;
    }

    @Override
    public List<ChatMessage> getMessages(Object sessionId) {
        try {
            List<String> messageList = redisTemplate.opsForList().range(getKey(sessionId), 0, -1);
            log.info("getMessages messageList:{}", messageList);

            if (CollUtil.isNotEmpty(messageList)) {
                String json = messageList.toString();
                return messagesFromJson(json);
            }
            // 获取不到对话历史，则从数据库中获取
            List<ChatSession> chatSessionList = chatSessionService.lambdaQuery()
                    .eq(ChatSession::getUserId, UserContext.getUser())
                    .eq(ChatSession::getSessionId, sessionId)
                    .orderByAsc(ChatSession::getSegmentIndex)
                    .list();

            // 判断是否为空
            if (CollUtil.isNotEmpty(chatSessionList)) {
                messageList = chatSessionList.stream()
                        .map(ChatSession::getContent)
                        .collect(Collectors.toList());

                return messagesFromJson(messageList.toString());
            }

            return Collections.emptyList();
        } catch (Exception e) {
            log.error("读取对话历史失败", e);
            return Collections.emptyList();
        }
    }

    @Override
    public void updateMessages(Object sessionId, List<ChatMessage> messages) {
        if (messages == null || messages.isEmpty()) {
            return;
        }
        try {
            for (ChatMessage message : messages) {
                //只存储用户消息和AI回复的消息
                if(!(message instanceof UserMessage ||  message instanceof AiMessage)){
                    return;
                }
            }
            // 将最新的一条数据存储到Redis中
            String json = messageToJson(messages.get(messages.size() - 1));
            redisTemplate.opsForList().rightPush(getKey(sessionId), json);

            log.info("存数据到redis中 sessionId{}:json:{}", sessionId,json);
            // 开启延时任务
            // 封装实体类
            Map<String, Object> map = new HashMap<>();
            map.put("key", getKey(sessionId));
            map.put("num", messages.size());
            String jsonStr = JSONUtil.toJsonStr(map);
            dataDelayTaskHandler.addDelayedTask(jsonStr,DELAY_TASK_EXECUTE_TIME , TimeUnit.SECONDS);
        } catch (Exception e) {
            log.error("更新对话历史失败", e);
        }
    }

    @Override
    public void deleteMessages(Object sessionId) {
        try {
            redisTemplate.delete(getKey(sessionId));
        } catch (Exception e) {
            log.error("删除对话历史失败", e);
        }
    }
}
