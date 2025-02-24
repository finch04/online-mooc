package com.tianji.aigc.memory;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.json.JSONUtil;
import lombok.Data;
import org.springframework.ai.chat.messages.*;
import org.springframework.ai.model.Media;

import java.util.List;
import java.util.Map;

public class MessageUtil {

    public static String toJson(Message message) {
        var redisMessage = BeanUtil.toBean(message, RedisMessage.class);
        redisMessage.setTextContent(message.getText());
        if (message instanceof AssistantMessage assistantMessage) {
            redisMessage.setToolCalls(assistantMessage.getToolCalls());
        }

        if (message instanceof ToolResponseMessage toolResponseMessage) {
            redisMessage.setToolResponses(toolResponseMessage.getResponses());
        }

        return JSONUtil.toJsonStr(redisMessage);
    }

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

@Data
class RedisMessage {
    private String messageType;
    private Map<String, Object> metadata = Map.of();
    private List<Media> media = List.of();
    private List<AssistantMessage.ToolCall> toolCalls = List.of();
    private String textContent;
    private List<ToolResponseMessage.ToolResponse> toolResponses = List.of();
    private Map<String, Object> properties = Map.of();
}
