package com.tianji.aigc.memory;

import cn.hutool.core.collection.CollStreamUtil;
import cn.hutool.core.collection.ListUtil;
import cn.hutool.core.lang.Assert;
import cn.hutool.json.JSONUtil;
import jakarta.annotation.Resource;
import org.springframework.ai.chat.memory.ChatMemoryRepository;
import org.springframework.ai.chat.messages.Message;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.util.List;

/**
 * 基于Redis实现的ChatMemoryRepository
 */
public class RedisChatMemoryRepository implements ChatMemoryRepository, MyChatMemoryRepository {

    // 默认redis中key的前缀
    public static final String DEFAULT_PREFIX = "CHAT:";

    private final String prefix;

    // 注入spring redis模板，进行redis的操作
    @Resource
    private StringRedisTemplate stringRedisTemplate;

    public RedisChatMemoryRepository() {
        this.prefix = DEFAULT_PREFIX;
    }

    public RedisChatMemoryRepository(String prefix) {
        this.prefix = prefix;
    }

    @Override
    public List<String> findConversationIds() {
        var keys = this.stringRedisTemplate.keys(this.prefix + "*");
        return ListUtil.toList(keys);
    }


    @Override
    public List<Message> findByConversationId(String conversationId) {
        // 生成Redis键名用于存储会话消息
        var redisKey = this.getKey(conversationId);
        // 获取Redis列表操作对象
        var listOps = this.stringRedisTemplate.boundListOps(redisKey);

        // 从Redis列表中获取所有的数据
        var messages = listOps.range(0, -1);
        // 将Redis返回的字符串列表转换为Message对象列表
        return CollStreamUtil.toList(messages, MessageUtil::toMessage);
    }

    @Override
    public void saveAll(String conversationId, List<Message> messages) {
        Assert.notEmpty(messages, "消息列表不能为空");
        var redisKey = this.getKey(conversationId);
        var listOps = this.stringRedisTemplate.boundListOps(redisKey);
        // 保存数据时，会传入全部的消息数据，包括之前的数据，所以需要先删除之前的数据，再添加新的数据
        this.deleteByConversationId(conversationId);
        // 将消息序列化并添加到Redis列表的右侧
        messages.forEach(message -> listOps.rightPush(MessageUtil.toJson(message)));
    }

    @Override
    public void deleteByConversationId(String conversationId) {
        var redisKey = this.getKey(conversationId);
        this.stringRedisTemplate.delete(redisKey);
    }

    private String getKey(String conversationId) {
        return prefix + conversationId;
    }

    /**
     * 根据对话ID优化对话记录，删除最后的2条消息，因为这2条消息是从路由智能体存储的，请求由后续的智能体处理
     * 为了确保历史消息的完整性，所以需要将中间转发的消息清理掉
     *
     * @param conversationId 对话的唯一标识符
     */
    public void optimization(String conversationId) {
        var redisKey = this.getKey(conversationId);
        var listOps = this.stringRedisTemplate.boundListOps(redisKey);
        // 从Redis列表右侧弹出2个元素
        listOps.rightPop(2);
    }
}
