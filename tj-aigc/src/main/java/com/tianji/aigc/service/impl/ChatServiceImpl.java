package com.tianji.aigc.service.impl;

import cn.hutool.core.date.DateUtil;
import com.tianji.aigc.config.SystemPromptConfig;
import com.tianji.aigc.service.ChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.AbstractChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.QuestionAnswerAdvisor;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ChatServiceImpl implements ChatService {

    private final ChatClient chatClient;
    private final SystemPromptConfig systemPromptConfig;
    private final VectorStore vectorStore;

    private static final Map<String, Boolean> GENERATE_STATUS = new HashMap<>();

    @Override
    public Flux<String> chat(String question, String sessionId) {
        // 获取对话id
        String conversationId = ChatService.getConversationId(sessionId);
        return this.chatClient.prompt()
                .system(this.systemPromptConfig.getSystemChatMessage())
                .system(promptSystem -> promptSystem.param("now", DateUtil.now()))
                .advisors(new QuestionAnswerAdvisor(vectorStore, SearchRequest.builder().query("").topK(999).build()))
                .advisors(advisor -> advisor.param(AbstractChatMemoryAdvisor.CHAT_MEMORY_CONVERSATION_ID_KEY, conversationId))
                .functions("courseFunction")
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
