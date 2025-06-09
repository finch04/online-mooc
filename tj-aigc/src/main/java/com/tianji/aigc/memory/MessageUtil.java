package com.tianji.aigc.memory;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.json.JSONUtil;
import org.springframework.ai.chat.messages.*;

/**
 * 消息转换工具类，提供消息对象与JSON字符串之间的转换功能，主要用于Redis存储格式转换
 */
public class MessageUtil {

    /**
     * 将Message对象转换为Redis存储格式的JSON字符串
     *
     * @param message 需要转换的原始消息对象
     * @return 符合Redis存储规范的JSON字符串
     */
    public static String toJson(Message message) {
        var redisMessage = BeanUtil.toBean(message, RedisMessage.class);
        // 设置消息内容
        redisMessage.setTextContent(message.getText());
        if (message instanceof AssistantMessage assistantMessage) {
            redisMessage.setToolCalls(assistantMessage.getToolCalls());
        }

        if (message instanceof ToolResponseMessage toolResponseMessage) {
            redisMessage.setToolResponses(toolResponseMessage.getResponses());
        }

        return JSONUtil.toJsonStr(redisMessage);
    }

    /**
     * 将Redis存储的JSON字符串反序列化为对应的Message对象
     *
     * @param json Redis存储的JSON格式消息数据
     * @return 对应类型的Message对象
     * @throws RuntimeException 当无法识别的消息类型时抛出异常
     */
    public static Message toMessage(String json) {
        var redisMessage = JSONUtil.toBean(json, RedisMessage.class);
        var messageType = MessageType.valueOf(redisMessage.getMessageType());
        switch (messageType) {
            case SYSTEM -> {
                return new SystemMessage(redisMessage.getTextContent());
            }
            case USER -> {
                return new UserMessage(redisMessage.getTextContent(), redisMessage.getMedia(), redisMessage.getMetadata());
            }
            case ASSISTANT -> {
                return new AssistantMessage(redisMessage.getTextContent(), redisMessage.getProperties(), redisMessage.getToolCalls());
            }
            case TOOL -> {
                return new ToolResponseMessage(redisMessage.getToolResponses(), redisMessage.getMetadata());
            }
        }

        throw new RuntimeException("Message data conversion failed.");
    }

}
