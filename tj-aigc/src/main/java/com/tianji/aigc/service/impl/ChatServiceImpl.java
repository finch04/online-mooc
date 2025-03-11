package com.tianji.aigc.service.impl;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.map.MapUtil;
import com.tianji.aigc.config.SystemPromptConfig;
import com.tianji.aigc.constants.Constant;
import com.tianji.aigc.service.ChatService;
import com.tianji.aigc.service.ChatSessionService;
import com.tianji.common.utils.UserContext;
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

    private final ChatClient dashScopeChatClient;
    private final ChatClient openAiChatClient;
    private final SystemPromptConfig systemPromptConfig;
    private final VectorStore vectorStore;
    private final ChatSessionService chatSessionService;

    private static final Map<String, Boolean> GENERATE_STATUS = new HashMap<>();

    @Override
    public Flux<String> chat(String question, String sessionId) {
        // 获取用户id
        Long userId = UserContext.getUser();
        // 获取对话id
        String conversationId = ChatService.getConversationId(sessionId);

        //更新会话时间
        this.chatSessionService.update(sessionId, question, userId);

        return this.dashScopeChatClient.prompt()
                .system(promptSystem -> promptSystem
                        .text(this.systemPromptConfig.getSystemChatMessage()) // 设置系统提示语
                        .param("now", DateUtil.now()) // 设置当前时间的参数
                )
                .advisors(advisor -> advisor
                        .advisors(new QuestionAnswerAdvisor(vectorStore, SearchRequest.builder().query("").topK(999).build()))
                        .param(AbstractChatMemoryAdvisor.CHAT_MEMORY_CONVERSATION_ID_KEY, conversationId)
                )
                // .functions(Constant.Functions.COURSE_FUNCTION,
                //         // Constant.Functions.CART_ADD_FUNCTION,
                //         Constant.Functions.PRE_PLACE_ORDER_FUNCTION)
                .toolContext(MapUtil.of(Constant.USER_ID, userId)) // 设置用户id参数
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

    @Override
    public String chatText(String question) {
        return this.openAiChatClient.prompt()
                .system(promptSystem -> promptSystem.text(this.systemPromptConfig.getTextSystemChatMessage()))
                .user(question)
                .call()
                .content();
    }
}
