package com.tianji.aigc.memory;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.convert.Convert;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.json.JSONUtil;
import com.tianji.aigc.config.ToolResultHolder;
import com.tianji.aigc.constants.Constant;
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

            // 通过 messageId 获取 requestId，再通过 requestId 获取参数列表，如果有，就存储起来
            // 最后，删除 messageId 对应的数据
            var messageId = Convert.toStr(assistantMessage.getMetadata().get(Constant.ID));
            var requestId = Convert.toStr(ToolResultHolder.get(messageId, Constant.REQUEST_ID));
            var params = ToolResultHolder.get(requestId);
            if (ObjectUtil.isNotEmpty(params)) {
                redisMessage.setParams(params);
            }
            ToolResultHolder.remove(messageId);
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
                // 这里使用了 properties 字段来存储 额外的参数信息，如果以后properties有用，需要该这个逻辑
                return new MyAssistantMessage(redisMessage.getTextContent(), redisMessage.getProperties(),
                        redisMessage.getToolCalls(), redisMessage.getParams());
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
    private Map<String, Object> params = Map.of();
}
