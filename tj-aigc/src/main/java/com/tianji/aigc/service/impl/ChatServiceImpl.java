package com.tianji.aigc.service.impl;

import cn.hutool.core.date.DateUtil;
import com.tianji.aigc.config.SystemPromptConfig;
import com.tianji.aigc.enums.ChatEventTypeEnum;
import com.tianji.aigc.service.ChatService;
import com.tianji.aigc.vo.ChatEventVO;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

@Service
@RequiredArgsConstructor
public class ChatServiceImpl implements ChatService {

    private final ChatClient chatClient;
    private final SystemPromptConfig systemPromptConfig;


    @Override
    public Flux<ChatEventVO> chat(String question, String sessionId) {
        return this.chatClient.prompt()
                .system(promptSystem -> promptSystem.text(this.systemPromptConfig.getChatSystemMessage().get())
                        .param("now", DateUtil.now())
                ) // 设置系统提示词
                .user(question)
                .stream()
                .chatResponse()
                .map(response -> {
                    // AI大模型响应的文本内容
                    String text = response.getResult().getOutput().getText();

                    // 构造VO对象返回
                    return ChatEventVO.builder()
                            .eventData(text)
                            .eventType(ChatEventTypeEnum.DATA.getValue())
                            .build();
                })
                .concatWith(Flux.just(ChatEventVO.builder()
                        .eventType(ChatEventTypeEnum.STOP.getValue()) // 结束标识
                        .build()));
    }

}
