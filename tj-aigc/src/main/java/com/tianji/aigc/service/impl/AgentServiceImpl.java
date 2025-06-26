package com.tianji.aigc.service.impl;

import cn.hutool.extra.spring.SpringUtil;

import com.tianji.aigc.agent.AbstractAgent;
import com.tianji.aigc.agent.Agent;
import com.tianji.aigc.enums.AgentTypeEnum;
import com.tianji.aigc.enums.ChatEventTypeEnum;
import com.tianji.aigc.service.ChatService;
import com.tianji.aigc.vo.ChatEventVO;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.util.Map;

//@Service
public class AgentServiceImpl implements ChatService {


    @Override
    public Flux<ChatEventVO> chat(String question, String sessionId) {
        String result = this.getAgentByType(AgentTypeEnum.ROUTE).process(question, sessionId);
        AgentTypeEnum agentTypeEnum = AgentTypeEnum.agentNameOf(result);
        Agent agent = this.getAgentByType(agentTypeEnum);
        if (agent == null){
            ChatEventVO chatEventVO = ChatEventVO.builder()
                    .eventType(ChatEventTypeEnum.DATA.getValue())
                    .eventData(result)
                    .build();
            return Flux.just(chatEventVO, AbstractAgent.STOP_EVENT);
        }

        return agent.processStream(question,sessionId);
    }

    private Agent getAgentByType(AgentTypeEnum agentTypeEnum) {
        if (agentTypeEnum == null){
            return null;
        }
        Map<String, Agent> beans = SpringUtil.getBeansOfType(Agent.class);
        for (Agent agent : beans.values()){
            if (agentTypeEnum == agent.getAgentType()){
                return agent;
            }
        }
        return null;
    }

    @Override
    public void stop(String sessionId) {
        this.getAgentByType(AgentTypeEnum.ROUTE).stop(sessionId);
    }
}
