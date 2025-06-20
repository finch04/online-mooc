package com.tianji.aigc.agent;

import com.tianji.aigc.config.SystemPromptConfig;
import com.tianji.aigc.enums.AgentTypeEnum;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * 路由智能体
 */
@Component
@RequiredArgsConstructor
public class RouteAgent extends AbstractAgent {

    private final SystemPromptConfig systemPromptConfig;

    @Override
    public String systemMessage() {
        return this.systemPromptConfig.getRouteAgentSystemMessage().get();
    }

    @Override
    public AgentTypeEnum getAgentType() {
        return AgentTypeEnum.ROUTE;
    }

}
