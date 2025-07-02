package com.tianji.aigc.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.dashscope.app.Application;
import com.alibaba.dashscope.app.ApplicationParam;
import com.alibaba.dashscope.app.ApplicationResult;
import com.alibaba.dashscope.utils.JsonUtils;
import com.tianji.aigc.config.DashScopeProperties;
import com.tianji.aigc.config.SystemPromptConfig;
import com.tianji.aigc.config.ToolResultHolder;
import com.tianji.aigc.constants.Constant;
import com.tianji.aigc.enums.ChatEventTypeEnum;
import com.tianji.aigc.service.ChatService;
import com.tianji.aigc.service.ChatSessionService;
import com.tianji.aigc.vo.ChatEventVO;
import com.tianji.common.utils.TokenContext;
import com.tianji.common.utils.UserContext;
import io.reactivex.Flowable;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "tj.ai", name = "chat-type", havingValue = "APP")
public class AppAgentChatService implements ChatService {

    private final DashScopeProperties dashScopeProperties;
    private final ChatMemory chatMemory;
    private final ChatSessionService chatSessionService;
    private final ChatClient openAiChatClient;
    private final SystemPromptConfig systemPromptConfig;

    // 存储大模型的生成状态，这里采用ConcurrentHashMap是确保线程安全
    // 目前的版本暂时用Map实现，如果考虑分布式环境的话，可以考虑用redis来实现
    private static final Map<String, Boolean> GENERATE_STATUS = new ConcurrentHashMap<>();
    // 输出结束的标记
    private static final ChatEventVO STOP_EVENT = ChatEventVO.builder().eventType(ChatEventTypeEnum.STOP.getValue()).build();

    @Override
    public Flux<ChatEventVO> chat(String question, String sessionId) {
        // 收集大模型生成的内容
        var outputBuilder = new StringBuilder();

        // 获取用户id
        var userId = UserContext.getUser();

        // 获取对话id
        var conversationId = ChatService.getConversationId(sessionId);
        String token = TokenContext.getToken();
        Map<String, Object> toolsMap = new HashMap<>();
        for (String tool : dashScopeProperties.getAppAgent().getTools()) {
            toolsMap.put(tool, MapUtil.of("user_token", token));
        }
        Map<String, Object> bizParams = MapUtil.<String, Object>builder()
                .put("user_defined_tokens", toolsMap)
                .build();

        ApplicationParam param = ApplicationParam.builder()
                .apiKey(dashScopeProperties.getKey())
                .appId(dashScopeProperties.getAppAgent().getId()) // 智能体id
                .prompt(question)
                .incrementalOutput(true) // 开启增量输出
                .bizParams(JsonUtils.toJsonObject(bizParams))
                .sessionId(conversationId) // 设置会话ID
                .build();

        Application application = new Application();
        try {
            Flowable<ApplicationResult> result = application.streamCall(param);

            // 将Flowable 转化为 Flux 进行处理输出
            return Flux.from(result)
                    .doFirst(() -> GENERATE_STATUS.put(sessionId, true)) // 第一次输出内容时执行
                    .doOnError(throwable -> GENERATE_STATUS.remove(sessionId)) // 出现异常时，删除标识
                    .doOnComplete(() -> GENERATE_STATUS.remove(sessionId)) // 完成时执行，删除标识
                    .doOnCancel(() -> {
                        // 这里如果执行到，就说明流被中断了，手动调用ChatMemory中的add方法，存储 中断之前 大模型生成的内容
                        this.saveStopHistoryRecord(conversationId, outputBuilder.toString());
                    })
                    .doFinally(signalType -> {
                        //需要更新对话的标题或更新时间
                        var content = StrUtil.format("""
                                --------
                                USER:{}\n
                                ASSISTANT:{}
                                --------
                                """, question, outputBuilder.toString());
                        this.chatSessionService.update(sessionId, content, userId);
                    })
                    .takeWhile(response -> { // 通过返回值来控制Flux流是否继续，true：继续，false：终止
                        return GENERATE_STATUS.getOrDefault(sessionId, false);
                    })
                    .map(response -> {
                        // AI大模型响应的文本内容
                        String text = response.getOutput().getText();

                        // 将大模型生成的内容写入到缓存中
                        outputBuilder.append(text);

                        // 构造VO对象返回
                        return ChatEventVO.builder()
                                .eventData(text)
                                .eventType(ChatEventTypeEnum.DATA.getValue())
                                .build();
                    })
                    .concatWith(Flux.just(STOP_EVENT));
        }catch (Exception e){
            throw new RuntimeException(e);
        }
    }

    @Override
    public void stop(String sessionId) {
        // 移除标记
        GENERATE_STATUS.remove(sessionId);
    }

    @Override
    public String chatText(String question) {
        // 设置APP请求参数
        ApplicationParam applicationParam = ApplicationParam.builder()
                .apiKey(this.dashScopeProperties.getKey())
                .appId(this.dashScopeProperties.getTextAppAgent().getId())
                .prompt(question)
                .build();

        try {
            // 创建APP对象并且开始执行
            var application = new Application();
            ApplicationResult result = application.call(applicationParam);
            return result.getOutput().getText();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void saveStopHistoryRecord(String conversationId, String text) {
        // 手动封装AssistantMessage对象，存储到redis中
        this.chatMemory.add(conversationId, new AssistantMessage(text));
    }
}
