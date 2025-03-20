package com.tianji.aigc.service.impl;

import cn.hutool.extra.spring.SpringUtil;
import com.tianji.aigc.agent.Agent;
import com.tianji.aigc.config.SystemPromptConfig;
import com.tianji.aigc.enums.AgentTypeEnum;
import com.tianji.aigc.enums.ChatEventTypeEnum;
import com.tianji.aigc.service.ChatService;
import com.tianji.aigc.vo.ChatEventVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class AgentServiceImpl implements ChatService {

    private final Agent routeAgent;
    private final ChatClient openAiChatClient;
    private final SystemPromptConfig systemPromptConfig;

    public static final ChatEventVO DEFAULT_EVENT = ChatEventVO.builder()
            .eventType(ChatEventTypeEnum.DATA.getValue())
            .eventData("你的问题，我还不明白，请重新提问吧!")
            .build();

    @Override
    public Flux<ChatEventVO> chat(String question, String sessionId) {
        String result = this.routeAgent.process(question, sessionId);
        AgentTypeEnum agentTypeEnum = AgentTypeEnum.agentNameOf(result);

        Agent agent = this.findAgentByType(agentTypeEnum);
        if (agent == null) {
            ChatEventVO chatEventVO = ChatEventVO.builder()
                    .eventType(ChatEventTypeEnum.DATA.getValue())
                    .eventData(result)
                    .build();
            return Flux.just(chatEventVO, Agent.STOP_EVENT);
        }

        return agent.processStream(question, sessionId);
    }

    private Agent findAgentByType(AgentTypeEnum agentTypeEnum) {
        if (agentTypeEnum == null) {
            return null;
        }
        Map<String, Agent> beans = SpringUtil.getBeansOfType(Agent.class);
        for (Agent agent : beans.values()) {
            if (agentTypeEnum == agent.getAgentType()) {
                return agent;
            }
        }
        return null;
    }

    @Override
    public void stop(String sessionId) {
        this.routeAgent.stop(sessionId);
    }

    @Override
    public String chatText(String question) {
        return this.openAiChatClient.prompt()
                .system(promptSystem -> promptSystem.text(this.systemPromptConfig.getTextSystemMessage().get()))
                .user(question)
                .call()
                .content();
    }
}
