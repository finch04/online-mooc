package com.tianji.aigc.service.impl;

import cn.hutool.core.date.DateUtil;
import com.tianji.aigc.service.ChatService;
import jakarta.annotation.Resource;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.AbstractChatMemoryAdvisor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

@Service
public class ChatServiceImpl implements ChatService {
    @Resource
    private ChatClient chatClient;

    @Override
    public Flux<String> chat(String question, String sessionId) {
        // 获取对话id
        String conversationId = ChatService.getConversationId(sessionId);
        return this.chatClient.prompt()
                .advisors(advisor -> advisor.param(AbstractChatMemoryAdvisor.CHAT_MEMORY_CONVERSATION_ID_KEY, conversationId))
                .system(promptSystem -> promptSystem.param("now", DateUtil.now()))
                .user(question)
                .stream()
                .content()
                // .doOnNext(System.out::println) // 打印输出
                .concatWith(Flux.just("&complete&"));
    }
}
