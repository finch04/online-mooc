package com.tianji.aigc.agent;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.ReflectUtil;
import cn.hutool.core.util.StrUtil;
import com.tianji.aigc.config.ToolResultHolder;
import com.tianji.aigc.constants.Constant;
import com.tianji.aigc.enums.ChatEventTypeEnum;
import com.tianji.aigc.service.ChatService;
import com.tianji.aigc.service.ChatSessionService;
import com.tianji.aigc.vo.ChatEventVO;
import com.tianji.common.utils.UserContext;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.AbstractChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.api.Advisor;
import org.springframework.ai.chat.metadata.ChatResponseMetadata;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public abstract class AbstractAgent implements Agent {

    @Resource
    private ChatSessionService chatSessionService;
    @Resource
    private ChatClient dashScopeChatClient;

    public static final Map<String, Boolean> GENERATE_STATUS = new ConcurrentHashMap<>();

    @Override
    public String process(String question, String sessionId) {
        // 获取用户id
        var userId = UserContext.getUser();
        var requestId = this.generateRequestId();

        //更新会话时间
        this.chatSessionService.update(sessionId, question, userId);

        return this.getChatClientRequest(userId, sessionId, requestId, question)
                .call()
                .content();
    }

    private ChatClient.ChatClientRequestSpec getChatClientRequest(Long userId, String sessionId, String requestId, String question) {
        return this.dashScopeChatClient.prompt()
                .system(promptSystem -> promptSystem.text(this.systemMessage()).params(this.systemMessageParams()))
                .advisors(advisor -> advisor.advisors(this.advisors()).params(this.advisorParams(sessionId)))
                .tools(this.tools())
                .toolContext(this.toolContext(userId, requestId))
                .user(question);
    }

    public Flux<ChatEventVO> processStream(String question, String sessionId) {
        // 获取用户id
        var userId = UserContext.getUser();
        var requestId = this.generateRequestId();

        //更新会话时间
        this.chatSessionService.update(sessionId, question, userId);

        return this.getChatClientRequest(userId, sessionId, requestId, question)
                .stream()
                .chatResponse()
                .doFirst(() -> {
                    //输出开始，标记正在输出
                    GENERATE_STATUS.put(sessionId, true);
                })
                .doOnComplete(() -> {
                    //输出结束，清除标记
                    GENERATE_STATUS.remove(sessionId);
                })
                .doOnError(throwable -> GENERATE_STATUS.remove(sessionId)) // 错误时清除标记
                .takeWhile(s -> Optional.ofNullable(GENERATE_STATUS.get(sessionId)).orElse(false)) // 只输出标记为true的流
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
                    // 通过请求id获取到参数列表，如果不为空，就将其追加到返回结果中
                    var map = ToolResultHolder.get(requestId);
                    if (CollUtil.isNotEmpty(map)) {
                        ToolResultHolder.remove(requestId); // 清除参数列表
                        // 响应给前端的参数数据
                        ChatEventVO chatEventVO = ChatEventVO.builder()
                                .eventData(map)
                                .eventType(ChatEventTypeEnum.PARAM.getValue())
                                .build();
                        return Flux.just(chatEventVO, STOP_EVENT);
                    }
                    return Flux.just(STOP_EVENT);
                }));
    }

    private String generateRequestId() {
        return IdUtil.fastSimpleUUID();
    }

    /**
     * 获取系统提示信息模板，子类必须实现以定义具体的系统提示内容。
     *
     * @return 系统提示的文本模板
     */
    public abstract String systemMessage();


    public List<Advisor> advisors() {
        return List.of();
    }

    public Map<String, Object> systemMessageParams() {
        return Map.of();
    }

    public Map<String, Object> advisorParams(String sessionId) {
        String conversationId = ChatService.getConversationId(sessionId);
        return Map.of(AbstractChatMemoryAdvisor.CHAT_MEMORY_CONVERSATION_ID_KEY, conversationId);
    }

    public Map<String, Object> toolContext(Long userId, String requestId) {
        return Map.of();
    }

    /**
     * 获取工具列表，默认返回空数组。子类需根据需求覆盖此方法。
     */
    public Object[] tools() {
        return EMPTY_OBJECTS;
    }

    @Override
    public void stop(String sessionId) {
        GENERATE_STATUS.remove(sessionId);
    }
}
