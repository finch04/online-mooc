package com.tianji.aigc.service.impl;

import com.alibaba.dashscope.app.Application;
import com.alibaba.dashscope.app.ApplicationParam;
import com.alibaba.dashscope.utils.JsonUtils;
import com.tianji.aigc.config.DashScopeProperties;
import com.tianji.aigc.enums.ChatEventTypeEnum;
import com.tianji.aigc.service.ChatService;
import com.tianji.aigc.vo.ChatEventVO;
import com.tianji.common.utils.TokenContext;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

//@Service
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "tj.ai", name = "chat-type", havingValue = "APP")
public class AppAgentChatService implements ChatService {

    private final DashScopeProperties dashScopeProperties;

    // 存储大模型的生成状态，这里采用ConcurrentHashMap是确保线程安全
    // 目前的版本暂时用Map实现，如果考虑分布式环境的话，可以考虑用redis来实现
    private static final Map<String, Boolean> GENERATE_STATUS = new ConcurrentHashMap<>();
    // 输出结束的标记
    private static final ChatEventVO STOP_EVENT = ChatEventVO.builder().eventType(ChatEventTypeEnum.STOP.getValue()).build();

    @Override
    public Flux<ChatEventVO> chat(String question, String sessionId) {
        // 获取对话id
        var conversationId = ChatService.getConversationId(sessionId);
        var token = TokenContext.getToken();
        var toolsMap = new HashMap<String, Object>();
        for (var tool : dashScopeProperties.getAppAgent().getTools()) {
            toolsMap.put(tool, Map.of("user_token", token));
        }
        var bizParams = Map.of("user_defined_tokens", toolsMap);

        var param = ApplicationParam.builder()
                .apiKey(dashScopeProperties.getKey())
                .appId(dashScopeProperties.getAppAgent().getId()) // 智能体id
                .prompt(question)
                .incrementalOutput(true) // 开启增量输出
                .bizParams(JsonUtils.toJsonObject(bizParams))
                .sessionId(conversationId) // 设置会话ID
                .build();

        var application = new Application();
        try {
            var result = application.streamCall(param);

            // 将Flowable 转化为 Flux 进行处理输出
            return Flux.from(result)
                    .doFirst(() -> { //输出开始，标记正在输出
                        GENERATE_STATUS.put(sessionId, true);
                    })
                    .doOnComplete(() -> { //输出结束，清除标记
                        GENERATE_STATUS.remove(sessionId);
                    })
                    .doOnError(throwable -> GENERATE_STATUS.remove(sessionId)) // 错误时清除标记
                    // 输出过程中，判断是否正在输出，如果正在输出，则继续输出，否则结束输出
                    .takeWhile(s -> GENERATE_STATUS.getOrDefault(sessionId, false))
                    .map(applicationResult -> {
                        // 获取大模型的输出的内容
                        var text = applicationResult.getOutput().getText();
                        // 封装响应对象
                        return ChatEventVO.builder()
                                .eventData(text)
                                .eventType(ChatEventTypeEnum.DATA.getValue())
                                .build();
                    })
                    .concatWith(Flux.just(STOP_EVENT));

        } catch (Exception e) {
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
        return "";
    }
}
