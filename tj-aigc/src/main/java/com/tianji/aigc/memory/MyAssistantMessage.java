package com.tianji.aigc.memory;

import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.model.Media;

import java.util.List;
import java.util.Map;

public class MyAssistantMessage extends AssistantMessage {

    private Map<String, Object> params = Map.of();

    public MyAssistantMessage(String content) {
        super(content);
    }

    public MyAssistantMessage(String content, Map<String, Object> properties) {
        super(content, properties);
    }

    public MyAssistantMessage(String content, Map<String, Object> properties, List<ToolCall> toolCalls) {
        super(content, properties, toolCalls);
    }

    public MyAssistantMessage(String content, Map<String, Object> properties, List<ToolCall> toolCalls, Map<String, Object> params) {
        super(content, properties, toolCalls);
        this.params = params;
    }

    public MyAssistantMessage(String content, Map<String, Object> properties, List<ToolCall> toolCalls, List<Media> media) {
        super(content, properties, toolCalls, media);
    }

    public Map<String, Object> getParams() {
        return params;
    }

    public void setParams(Map<String, Object> params) {
        this.params = params;
    }
}
