package com.tianji.aigc.service.impl;

import cn.hutool.core.date.DateUtil;
import com.tianji.aigc.config.SystemPromptConfig;
import com.tianji.aigc.enums.ChatEventTypeEnum;
import com.tianji.aigc.service.ChatService;
import com.tianji.aigc.vo.ChatEventVO;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.AbstractChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
public class ChatServiceImpl implements ChatService {

    private final ChatClient chatClient;
    private final SystemPromptConfig systemPromptConfig;
    private final ChatMemory jdbcChatMemory;

    // 存储大模型的生成状态，这里采用ConcurrentHashMap是确保线程安全
    // 目前的版本暂时用Map实现，如果考虑分布式环境的话，可以考虑用redis来实现
    private static final Map<String, Boolean> GENERATE_STATUS = new ConcurrentHashMap<>();

    @Override
    public Flux<ChatEventVO> chat(String question, String sessionId) {
        // 将会话id转化为对话id
        var conversationId = ChatService.getConversationId(sessionId);

        // 收集大模型生成的内容
        var outputBuilder = new StringBuilder();
        return this.chatClient.prompt()
                .system(promptSystem -> promptSystem.text(this.systemPromptConfig.getChatSystemMessage().get())
                        .param("now", DateUtil.now())
                ) // 设置系统提示词
                .advisors(advisor -> advisor.param(AbstractChatMemoryAdvisor.CHAT_MEMORY_CONVERSATION_ID_KEY, conversationId))
                .user(question)
                .stream()
                .chatResponse()
                .doFirst(() -> GENERATE_STATUS.put(sessionId, true)) // 第一次输出内容时执行
                .doOnError(throwable -> GENERATE_STATUS.remove(sessionId)) // 出现异常时，删除标识
                .doOnComplete(() -> GENERATE_STATUS.remove(sessionId)) // 完成时执行，删除标识
                .doOnCancel(() -> {
                    // 这里如果执行到，就说明流被中断了，手动调用ChatMemory中的add方法，存储 中断之前 大模型生成的内容
                    this.saveStopHistoryRecord(conversationId, outputBuilder.toString());
                })
                .takeWhile(response -> { // 通过返回值来控制Flux流是否继续，true：继续，false：终止
                    return GENERATE_STATUS.getOrDefault(sessionId, false);
                })
                .map(response -> {
                    // AI大模型响应的文本内容
                    String text = response.getResult().getOutput().getText();

                    // 将大模型生成的内容写入到缓存中
                    outputBuilder.append(text);

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

    @Override
    public void stop(String sessionId) {
        // 删除会话所对应的标识
        GENERATE_STATUS.remove(sessionId);
    }

    private void saveStopHistoryRecord(String conversationId, String text) {
        // 手动封装AssistantMessage对象，存储到redis中
        this.jdbcChatMemory.add(conversationId, new AssistantMessage(text));
    }
}
