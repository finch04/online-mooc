package com.tianji.aigc.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.ReflectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.tianji.aigc.config.SystemPromptConfig;
import com.tianji.aigc.config.ToolResultHandler;
import com.tianji.aigc.constants.Constant;
import com.tianji.aigc.service.ChatService;
import com.tianji.aigc.service.ChatSessionService;
import com.tianji.common.utils.UserContext;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.AbstractChatMemoryAdvisor;
import org.springframework.ai.chat.metadata.ChatResponseMetadata;
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
        String requestId = IdUtil.fastSimpleUUID();

        //更新会话时间
        this.chatSessionService.update(sessionId, question, userId);

        return this.dashScopeChatClient.prompt()
                .system(promptSystem -> promptSystem
                        .text(this.systemPromptConfig.getSystemChatMessage()) // 设置系统提示语
                        .param("now", DateUtil.now()) // 设置当前时间的参数
                )
                .advisors(advisor -> advisor
                        // .advisors(new QuestionAnswerAdvisor(vectorStore, SearchRequest.builder().query("").topK(999).build()))
                        .param(AbstractChatMemoryAdvisor.CHAT_MEMORY_CONVERSATION_ID_KEY, conversationId)
                )
                // .functions(Constant.Functions.COURSE_FUNCTION,
                //         // Constant.Functions.CART_ADD_FUNCTION,
                //         Constant.Functions.PRE_PLACE_ORDER_FUNCTION)
                .toolContext(MapUtil.<String, Object>builder()
                        .put(Constant.USER_ID, userId)
                        .put(Constant.REQUEST_ID, requestId)
                        .build()) // 设置用户id参数
                .user(question)
                .stream()
                // .content()
                .chatResponse()
                .doFirst(() -> {
                    GENERATE_STATUS.put(sessionId, true);
                }) //输出开始，标记正在输出
                .doOnComplete(() -> {
                    //输出结束，清除标记
                    GENERATE_STATUS.remove(sessionId);
                })
                // .doOnNext(System.out::println) // 打印输出
                .takeWhile(s -> GENERATE_STATUS.get(sessionId))
                // .concatWith(Flux.just("&complete&"));
                .map(chatResponse -> {
                    // 对于响应结果进行处理，如果是最后一条数据，就把此次消息id放到内存中
                    // 主要用于存储消息数据到 redis中，可以根据消息di获取的请求id，再通过请求id就可以获取到参数列表了
                    // 从而解决，在历史聊天记录中没有外参数的问题
                    String finishReason = chatResponse.getResult().getMetadata().getFinishReason();
                    if (StrUtil.equals(Constant.STOP, finishReason)) {
                        String messageId = ((ChatResponseMetadata) ReflectUtil.getFieldValue(chatResponse, Constant.Chats.CHAT_RESPONSE_METADATA)).getId();
                        ToolResultHandler.put(messageId, Constant.REQUEST_ID, requestId);
                    }
                    //不做额外处理，直接返回原本的数据
                    return chatResponse.getResult().getOutput().getText();
                })
                .concatWith(Flux.defer(() -> {
                    // 通过请求id获取到参数列表，如果不为空，就将其追加到返回结果中
                    Map<String, Object> map = ToolResultHandler.get(requestId);
                    if (CollUtil.isNotEmpty(map)) {
                        String result = StrUtil.format(Constant.Chats.PARAM_TAG, JSONUtil.toJsonStr(map));
                        ToolResultHandler.remove(requestId); // 清除参数列表

                        return Flux.just(result, Constant.Chats.COMPLETE_TAG);
                    }
                    return Flux.just(Constant.Chats.COMPLETE_TAG);
                }));

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
