package com.tianji.aigc.memory;

import cn.hutool.core.collection.CollStreamUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.IterUtil;
import jakarta.annotation.Resource;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.messages.Message;
import org.springframework.data.redis.core.BoundListOperations;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.util.List;

/**
 * 基于Redis的会话历史记录存储
 */
public class RedisChatMemory implements ChatMemory {

    public static final String DEFAULT_PREFIX = "CHAT:";

    private String prefix;

    public RedisChatMemory() {
        this.prefix = DEFAULT_PREFIX;
    }

    public RedisChatMemory(String prefix) {
        this.prefix = prefix;
    }

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Override
    public void add(String conversationId, List<Message> messages) {
        if (CollUtil.isEmpty(messages)) {
            return;
        }
        String redisKey = this.getKey(conversationId);
        BoundListOperations<String, String> listOps = this.stringRedisTemplate.boundListOps(redisKey);
        messages.forEach(message -> {
            listOps.rightPush(MessageUtil.toJson(message));
        });
    }

    @Override
    public List<Message> get(String conversationId, int lastN) {
        if (lastN <= 0) {
            return List.of();
        }
        String redisKey = this.getKey(conversationId);
        BoundListOperations<String, String> listOps = this.stringRedisTemplate.boundListOps(redisKey);

        List<String> messages = listOps.range(0, lastN);
        return CollStreamUtil.toList(messages, MessageUtil::toMessage);
    }

    @Override
    public void clear(String conversationId) {
        String redisKey = this.getKey(conversationId);
        this.stringRedisTemplate.delete(redisKey);
    }

    private String getKey(String conversationId) {
        return prefix + conversationId;
    }
}
