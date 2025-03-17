package com.tianji.aigc.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.ReflectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tianji.aigc.config.SystemPromptConfig;
import com.tianji.aigc.config.ToolResultHolder;
import com.tianji.aigc.constants.Constant;
import com.tianji.aigc.enums.ChatEventTypeEnum;
import com.tianji.aigc.service.ChatService;
import com.tianji.aigc.service.ChatSessionService;
import com.tianji.aigc.vo.ChatEventVO;
import com.tianji.common.utils.UserContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.AbstractChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.QuestionAnswerAdvisor;
import org.springframework.ai.chat.metadata.ChatResponseMetadata;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatServiceImpl implements ChatService {

    private final ChatClient dashScopeChatClient;
    private final ChatClient openAiChatClient;
    private final SystemPromptConfig systemPromptConfig;
    private final VectorStore vectorStore;
    private final ChatSessionService chatSessionService;
    private final ObjectMapper objectMapper;

    private static final Map<String, Boolean> GENERATE_STATUS = new HashMap<>();

    @Override
    public Flux<String> chat(String question, String sessionId) {
        // 获取用户id
        var userId = UserContext.getUser();
        // 获取对话id
        var conversationId = ChatService.getConversationId(sessionId);
        var requestId = IdUtil.fastSimpleUUID();

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
                .toolContext(MapUtil.<String, Object>builder()
                        .put(Constant.USER_ID, userId)
                        .put(Constant.REQUEST_ID, requestId)
                        .build()) // 设置用户id参数
                .user(question)
                .stream()
                .chatResponse()
                .doFirst(() -> {
                    GENERATE_STATUS.put(sessionId, true);
                }) //输出开始，标记正在输出
                .doOnComplete(() -> {
                    //输出结束，清除标记
                    GENERATE_STATUS.remove(sessionId);
                })
                .takeWhile(s -> Optional.ofNullable(GENERATE_STATUS.get(sessionId)).orElse(false))
                // .concatWith(Flux.just("&complete&"));
                .map(chatResponse -> {
                    // 对于响应结果进行处理，如果是最后一条数据，就把此次消息id放到内存中
                    // 主要用于存储消息数据到 redis中，可以根据消息di获取的请求id，再通过请求id就可以获取到参数列表了
                    // 从而解决，在历史聊天记录中没有外参数的问题
                    var finishReason = chatResponse.getResult().getMetadata().getFinishReason();
                    if (StrUtil.equals(Constant.STOP, finishReason)) {
                        var messageId = ((ChatResponseMetadata) ReflectUtil.getFieldValue(chatResponse, Constant.Chats.CHAT_RESPONSE_METADATA)).getId();
                        ToolResultHolder.put(messageId, Constant.REQUEST_ID, requestId);
                    }
                    //不做额外处理，直接返回原本的数据
                    return chatResponse.getResult().getOutput().getText();
                })
                .concatWith(Flux.defer(() -> {
                    // 通过请求id获取到参数列表，如果不为空，就将其追加到返回结果中
                    var map = ToolResultHolder.get(requestId);
                    if (CollUtil.isNotEmpty(map)) {
                        try {
                            var result = StrUtil.format(Constant.Chats.PARAM_TAG, objectMapper.writeValueAsString(map));
                            ToolResultHolder.remove(requestId); // 清除参数列表
                            return Flux.just(result, Constant.Chats.COMPLETE_TAG);
                        } catch (JsonProcessingException e) {
                            log.error("json序列化出错。", e);
                        }
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

    @Override
    public Flux<ChatEventVO> chatMock2(String question, String sessionId) {
        String data = """
                {"eventData":"根据","eventType":1001}
                {"eventData":"课程","eventType":1001}
                {"eventData":"介绍","eventType":1001}
                {"eventData":"，我为您准备","eventType":1001}
                {"eventData":"了一个简单的Java入门","eventType":1001}
                {"eventData":"示例。由于","eventType":1001}
                {"eventData":"这是个基础示","eventType":1001}
                {"eventData":"例，所以代码","eventType":1001}
                {"eventData":"可能不是最优解","eventType":1001}
                {"eventData":"，但应该能满足","eventType":1001}
                {"eventData":"您的需求。这里","eventType":1001}
                {"eventData":"是一个简单的Hello World","eventType":1001}
                {"eventData":"程序，它是所有","eventType":1001}
                {"eventData":"程序员的入门经典","eventType":1001}
                {"eventData":"：\\n\\n```java","eventType":1001}
                {"eventData":"\\npublic class HelloWorld {","eventType":1001}
                {"eventData":"\\n    public static void","eventType":1001}
                {"eventData":" main(String[] args","eventType":1001}
                {"eventData":") {\\n        System","eventType":1001}
                {"eventData":".out.println(\\"Hello","eventType":1001}
                {"eventData":", World!\\");","eventType":1001}
                {"eventData":"\\n    }\\n}\\n```","eventType":1001}
                {"eventData":"\\n\\n运行此程序，","eventType":1001}
                {"eventData":"您将在控制台","eventType":1001}
                {"eventData":"看到输出：\\"Hello","eventType":1001}
                {"eventData":", World!\\"。","eventType":1001}
                {"eventData":"这就是经典的Java入门","eventType":1001}
                {"eventData":"程序，它演示","eventType":1001}
                {"eventData":"了Java语言的一些","eventType":1001}
                {"eventData":"基本特性，如","eventType":1001}
                {"eventData":"声明变量、使用","eventType":1001}
                {"eventData":"系统输出打印字符串","eventType":1001}
                {"eventData":"等。如果您需要","eventType":1001}
                {"eventData":"更多的示例或者","eventType":1001}
                {"eventData":"具体的实现细节，","eventType":1001}
                {"eventData":"我可以为您提供更详","eventType":1001}
                {"eventData":"尽的指导。","eventType":1001}
                {"eventData":{"courseInfo_1589905661084430337":{"id":"1589905661084430337","name":"可能是史上最全的微服务技术栈课程","price":199.0,"validDuration":9999,"usePeople":"有一定的Java开发基础，熟练使用了SpringBoot、MyBatis等基础框架","detail":"可能是史上最全的微服务技术栈课程，由黑马名师授课，你值的拥有"}},"eventType":1003}
                {"eventType":1002}
                """;

        return Flux.fromIterable(StrUtil.split(data, "\n").stream()
                        .filter(StrUtil::isNotBlank)
                        .map(s -> JSONUtil.toBean(s, ChatEventVO.class))
                        .filter(Objects::nonNull)
                        .toList())
                .delayElements(Duration.ofMillis(300));
    }

    @Override
    public Flux<ChatEventVO> chatMock(String question, String sessionId) {
        // 获取用户id
        var userId = UserContext.getUser();
        // 获取对话id
        var conversationId = ChatService.getConversationId(sessionId);
        var requestId = IdUtil.fastSimpleUUID();

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
                .toolContext(MapUtil.<String, Object>builder()
                        .put(Constant.USER_ID, userId)
                        .put(Constant.REQUEST_ID, requestId)
                        .build()) // 设置用户id参数
                .user(question)
                .stream()
                .chatResponse()
                .doFirst(() -> {
                    GENERATE_STATUS.put(sessionId, true);
                }) //输出开始，标记正在输出
                .doOnComplete(() -> {
                    //输出结束，清除标记
                    GENERATE_STATUS.remove(sessionId);
                })
                .takeWhile(s -> Optional.ofNullable(GENERATE_STATUS.get(sessionId)).orElse(false))
                // .concatWith(Flux.just("&complete&"));
                .map(chatResponse -> {
                    // 对于响应结果进行处理，如果是最后一条数据，就把此次消息id放到内存中
                    // 主要用于存储消息数据到 redis中，可以根据消息di获取的请求id，再通过请求id就可以获取到参数列表了
                    // 从而解决，在历史聊天记录中没有外参数的问题
                    var finishReason = chatResponse.getResult().getMetadata().getFinishReason();
                    if (StrUtil.equals(Constant.STOP, finishReason)) {
                        var messageId = ((ChatResponseMetadata) ReflectUtil.getFieldValue(chatResponse, Constant.Chats.CHAT_RESPONSE_METADATA)).getId();
                        ToolResultHolder.put(messageId, Constant.REQUEST_ID, requestId);
                    }
                    //不做额外处理，直接返回原本的数据
                    return ChatEventVO.builder()
                            .eventData(chatResponse.getResult().getOutput().getText())
                            .eventType(ChatEventTypeEnum.DATA.getValue())
                            .build();
                })
                .concatWith(Flux.defer(() -> {
                    ChatEventVO stopEvent = ChatEventVO.builder()
                            .eventType(ChatEventTypeEnum.STOP.getValue())
                            .build();

                    // 通过请求id获取到参数列表，如果不为空，就将其追加到返回结果中
                    var map = ToolResultHolder.get(requestId);
                    if (CollUtil.isNotEmpty(map)) {
                        ToolResultHolder.remove(requestId); // 清除参数列表
                        return Flux.just(ChatEventVO.builder()
                                .eventData(map)
                                .eventType(ChatEventTypeEnum.PARAM.getValue())
                                .build(), stopEvent);
                    }
                    return Flux.just(stopEvent);
                }));
    }
}
