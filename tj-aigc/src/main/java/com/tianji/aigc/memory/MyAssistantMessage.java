package com.tianji.aigc.memory;

import lombok.*;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.model.Media;

import java.util.List;
import java.util.Map;

@Getter
@Setter
public class MyAssistantMessage extends AssistantMessage {
    private Map<String,Object> params;
    public MyAssistantMessage(String content, Map<String, Object> properties, List<ToolCall> toolCalls, List<Media> media,Map<String,Object> params) {
        super(content, properties, toolCalls, media);
        this.params = params;
    }

}
