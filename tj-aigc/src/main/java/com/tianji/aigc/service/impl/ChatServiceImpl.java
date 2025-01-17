package com.tianji.aigc.service.impl;

import cn.hutool.core.date.DateUtil;
import com.tianji.aigc.functions.CourseFunction;
import com.tianji.aigc.service.ChatService;
import jakarta.annotation.Resource;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.AbstractChatMemoryAdvisor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.util.HashMap;
import java.util.Map;

@Service
public class ChatServiceImpl implements ChatService {
    @Resource
    private ChatClient chatClient;

    private static final Map<String, Boolean> GENERATE_STATUS = new HashMap<>();

    @Override
    public Flux<String> chat(String question, String sessionId) {
        // 获取对话id
        String conversationId = ChatService.getConversationId(sessionId);
        return this.chatClient.prompt()
                .advisors(advisor -> advisor.param(AbstractChatMemoryAdvisor.CHAT_MEMORY_CONVERSATION_ID_KEY, conversationId))
                .system(promptSystem -> promptSystem.param("now", DateUtil.now()))
                // .functions("courseFunction")
                .user(question)
                .stream()
                .content()
                .doFirst(() -> {
                    GENERATE_STATUS.put(sessionId, true);
                }) //输出开始，标记正在输出
                .doOnComplete(() -> {
                    GENERATE_STATUS.remove(sessionId);
                }) //输出结束，清除标记
                // .doOnNext(System.out::println) // 打印输出
                .takeWhile(s -> GENERATE_STATUS.get(sessionId))
                .concatWith(Flux.just("&complete&"));
    }

    @Override
    public void stop(String sessionId) {
        GENERATE_STATUS.remove(sessionId);
    }
}
