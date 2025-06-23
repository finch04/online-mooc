package com.tianji.aigc.agent;

import com.tianji.aigc.config.SystemPromptConfig;
import com.tianji.aigc.enums.AgentTypeEnum;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.advisor.api.Advisor;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 路由智能体
 */
@Component
@RequiredArgsConstructor
public class RouteAgent extends AbstractAgent {

    private final SystemPromptConfig systemPromptConfig;

    private final Advisor messageChatMemoryAdvisor;

    private final Advisor recordOptimizationAdvisor;

    @Override
    public String systemMessage() {
        return this.systemPromptConfig.getRouteAgentSystemMessage().get();
    }

    @Override
    public AgentTypeEnum getAgentType() {
        return AgentTypeEnum.ROUTE;
    }

    @Override
    public List<Advisor> advisors(String question) {
        return List.of(this.messageChatMemoryAdvisor,this.recordOptimizationAdvisor);
    }



}
