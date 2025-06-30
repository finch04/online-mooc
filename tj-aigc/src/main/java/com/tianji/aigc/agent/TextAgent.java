package com.tianji.aigc.agent;

import com.tianji.aigc.config.SystemPromptConfig;
import com.tianji.aigc.enums.AgentTypeEnum;
import com.tianji.aigc.enums.ChatEventTypeEnum;
import com.tianji.aigc.vo.ChatEventVO;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

@Component
@RequiredArgsConstructor
public class TextAgent implements Agent {

    private final ChatClient openAiChatClient;
    private final SystemPromptConfig systemPromptConfig;

    @Override
    public Flux<ChatEventVO> processStream(String question, String sessionId) {
        return this.openAiChatClient.prompt()
                .system(this.systemMessage())
                .user(question)
                .stream()
                .chatResponse()
                .map(chatResponse -> ChatEventVO.builder()
                        .eventType(ChatEventTypeEnum.DATA.getValue())
                        .eventData(chatResponse.getResult().getOutput().getText())
                        .build()
                )
                .concatWith(Flux.just(ChatEventVO.builder()
                        .eventType(ChatEventTypeEnum.STOP.getValue())
                        .build()));
    }

    @Override
    public String process(String question, String sessionId) {
        return this.openAiChatClient.prompt()
                .system(this.systemMessage())
                .user(question)
                .call()
                .content();
    }

    @Override
    public AgentTypeEnum getAgentType() {
        return AgentTypeEnum.TEXT;
    }

    @Override
    public void stop(String sessionId) {
        throw new RuntimeException("不支持停止操作！");
    }

    @Override
    public String systemMessage() {
        return this.systemPromptConfig.getTextSystemMessage().get();
    }
}
