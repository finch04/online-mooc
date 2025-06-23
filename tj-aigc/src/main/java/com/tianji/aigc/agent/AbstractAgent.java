package com.tianji.aigc.agent;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.IdUtil;
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
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.model.ChatModel;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public abstract class AbstractAgent implements Agent {

    @Resource
    private ChatModel chatModel;
    @Resource
    private ChatMemory chatMemory;
    @Resource
    private ChatSessionService chatSessionService;
    @Resource
    private Advisor loggerAdvisor;

    // 存储大模型的生成状态，这里采用ConcurrentHashMap是确保线程安全
    // 目前的版本暂时用Map实现，如果考虑分布式环境的话，可以考虑用redis来实现
    private static final Map<String, Boolean> GENERATE_STATUS = new ConcurrentHashMap<>();
    // 结束标识
    public static final ChatEventVO STOP_EVENT = ChatEventVO.builder().eventType(ChatEventTypeEnum.STOP.getValue()).build();

    @Override
    public String process(String question, String sessionId) {
        var requestId = this.generateRequestId();
        // 获取当前请求用户id
        Long userId = UserContext.getUser();

        var result = this.getChatClient(question, sessionId, requestId)
                .call()
                .content();

        log.info("[大模型输出] sessionId:{}, question:{}, result:{}", sessionId, question, result);

        // 需要更新对话的标题 或 更新时间
        this.updateTitleOrUpdateTime(question, result, sessionId, userId);

        return result;
    }

    @Override
    public Flux<ChatEventVO> processStream(String question, String sessionId) {
        var requestId = this.generateRequestId();
        // 将会话id转化为对话id
        var conversationId = ChatService.getConversationId(sessionId);
        // 收集大模型生成的内容
        var outputBuilder = new StringBuilder();
        // 获取当前请求用户id
        Long userId = UserContext.getUser();

        return this.getChatClient(question, sessionId, requestId)
                .stream()
                .chatResponse()
                .doFirst(() -> GENERATE_STATUS.put(sessionId, true)) // 第一次输出内容时执行
                .doOnError(throwable -> GENERATE_STATUS.remove(sessionId)) // 出现异常时，删除标识
                .doOnComplete(() -> GENERATE_STATUS.remove(sessionId)) // 完成时执行，删除标识
                .doOnCancel(() -> {
                    // 这里如果执行到，就说明流被中断了，手动调用ChatMemory中的add方法，存储 中断之前 大模型生成的内容
                    this.saveStopHistoryRecord(conversationId, outputBuilder.toString());
                })
                .doFinally(signalType -> {
                    // 需要更新对话的标题 或 更新时间
                    this.updateTitleOrUpdateTime(question, outputBuilder.toString(), sessionId, userId);
                })
                .takeWhile(response -> { // 通过返回值来控制Flux流是否继续，true：继续，false：终止
                    return GENERATE_STATUS.getOrDefault(sessionId, false);
                })
                .map(response -> {
                    // AI大模型响应的文本内容
                    String text = response.getResult().getOutput().getText();

                    // 将大模型生成的内容写入到缓存中
                    outputBuilder.append(text);

                    var finishReason = response.getResult().getMetadata().getFinishReason();
                    if (StrUtil.equals(Constant.STOP, finishReason)) {
                        // 将消息id与请求id关联
                        String messageId = response.getMetadata().getId();
                        ToolResultHolder.put(messageId, Constant.REQUEST_ID, requestId);
                    }

                    // 构造VO对象返回
                    return ChatEventVO.builder()
                            .eventData(text)
                            .eventType(ChatEventTypeEnum.DATA.getValue())
                            .build();
                })
                .concatWith(Flux.defer(() -> {
                    Map<String, Object> map = ToolResultHolder.get(requestId);
                    if (CollUtil.isNotEmpty(map)) {
                        // 使用完成后，要删除容器中的响应数据，及时的释放内存资源
                        ToolResultHolder.remove(requestId);

                        // 封装返回的VO对象，返回给前端
                        ChatEventVO chatEventVO = ChatEventVO.builder()
                                .eventType(ChatEventTypeEnum.PARAM.getValue())
                                .eventData(map)
                                .build();

                        return Flux.just(chatEventVO, STOP_EVENT);
                    }
                    return Flux.just(STOP_EVENT);
                }));
    }

    private void updateTitleOrUpdateTime(String question, String outputBuilder, String sessionId, Long userId) {
        var content = StrUtil.format("""
                ------------
                USER:{} \n
                ASSISTANT:{}
                ------------
                """, question, outputBuilder);
        this.chatSessionService.update(sessionId, content, userId);
    }

    private ChatClient.ChatClientRequestSpec getChatClient(String question, String sessionId, String requestId) {
        return ChatClient.builder(this.chatModel).build()
                .prompt()
                .system(promptSystem -> promptSystem
                        .text(this.systemMessage())
                        .params(this.systemMessageParams()))
                .advisors(advisor -> advisor
                        .advisors(this.defaultAdvisors(question)) //添加默认的增强器
                        .advisors(this.advisors(question))  //添加自定义的增强器
                        .params(this.advisorParams(sessionId, requestId)))
                .tools(this.tools())
                .toolContext(this.toolContext(sessionId, requestId))
                .user(question);
    }

    private void saveStopHistoryRecord(String conversationId, String text) {
        // 手动封装AssistantMessage对象，存储到redis中
        this.chatMemory.add(conversationId, new AssistantMessage(text));
    }

    @Override
    public void stop(String sessionId) {
        GENERATE_STATUS.remove(sessionId);
    }


    private String generateRequestId() {
        return IdUtil.fastSimpleUUID();
    }

    @Override
    public Map<String, Object> advisorParams(String sessionId, String requestId) {
        var conversationId = ChatService.getConversationId(sessionId);
        return Map.of(AbstractChatMemoryAdvisor.CHAT_MEMORY_CONVERSATION_ID_KEY, conversationId);
    }
    @Override
    public List<Advisor> defaultAdvisors(String question) {
        return List.of(this.loggerAdvisor);
    }


}
