package com.tianji.aigc.memory.redis;

import cn.hutool.core.collection.CollStreamUtil;
import cn.hutool.core.collection.CollUtil;
import com.tianji.aigc.memory.MessageUtil;
import jakarta.annotation.Resource;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.messages.Message;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.util.List;

public class RedisChatMemory implements ChatMemory {

    // 思考问题：如何把消息存到redis中？关键是使用什么样的数据结构来存储聊天对话数据？ 1. list 2. hash 3. string 4. set

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    // 默认redis中key的前缀
    public static final String DEFAULT_PREFIX = "CHAT:";

    private final String prefix;

    public RedisChatMemory() {
        this.prefix = DEFAULT_PREFIX;
    }

    public RedisChatMemory(String prefix) {
        this.prefix = prefix;
    }

    /**
     * 存储对话记录到redis中
     *
     * @param conversationId 对话id，格式：{用户id}_{sessionId}
     * @param messages       消息列表
     */
    @Override
    public void add(String conversationId, List<Message> messages) {
        if (CollUtil.isEmpty(messages)) {
            return;
        }

        var redisKey = this.getKey(conversationId);
        var listOps = this.stringRedisTemplate.boundListOps(redisKey);
        // 遍历消息列表，从右边存储到list队列中
        messages.forEach(message -> listOps.rightPush(MessageUtil.toJson(message)));
    }

    private String getKey(String conversationId) {
        return this.prefix + conversationId;
    }

    @Override
    public List<Message> get(String conversationId, int lastN) {
        // TODO 作业：实现从redis中获取对话记录
        // 验证参数有效性，当lastN非正数时直接返回空结果
        if (lastN <= 0) {
            return List.of();
        }
        // 生成Redis键名用于存储会话消息
        var redisKey = this.getKey(conversationId);
        // 获取Redis列表操作对象
        var listOps = this.stringRedisTemplate.boundListOps(redisKey);

        // 从Redis列表中获取指定范围的元素（从第一个元素开始到lastN位置）
        var messages = listOps.range(0, lastN);
        // 将Redis返回的字符串列表转换为Message对象列表
        return CollStreamUtil.toList(messages, MessageUtil::toMessage);
    }

    @Override
    public void clear(String conversationId) {
        var redisKey = this.getKey(conversationId);
        this.stringRedisTemplate.delete(redisKey);
    }
}
