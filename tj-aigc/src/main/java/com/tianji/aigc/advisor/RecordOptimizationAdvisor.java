package com.tianji.aigc.advisor;

import cn.hutool.core.convert.Convert;
import com.tianji.aigc.enums.AgentTypeEnum;
import com.tianji.aigc.memory.RedisChatMemory;
import org.springframework.ai.chat.client.advisor.AbstractChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.api.*;
import org.springframework.ai.chat.model.ChatResponse;
import reactor.core.publisher.Flux;

public class RecordOptimizationAdvisor implements CallAroundAdvisor, StreamAroundAdvisor {

    private final RedisChatMemory redisChatMemory;

    public RecordOptimizationAdvisor(RedisChatMemory redisChatMemory) {
        this.redisChatMemory = redisChatMemory;
    }

    @Override
    public AdvisedResponse aroundCall(AdvisedRequest advisedRequest, CallAroundAdvisorChain chain) {
        AdvisedResponse advisedResponse = chain.nextAroundCall(advisedRequest);
        ChatResponse response = advisedResponse.response();
        // 获取大模型返回的文本数据，如果返回的文本数据中包含agentName，则进行记录优化
        String text = response.getResult().getOutput().getText();
        AgentTypeEnum agentTypeEnum = AgentTypeEnum.agentNameOf(text);
        if (null != agentTypeEnum) {
            // 需要进行对记录优化
            String key = AbstractChatMemoryAdvisor.CHAT_MEMORY_CONVERSATION_ID_KEY;
            String conversationId = Convert.toStr(advisedResponse.adviseContext().get(key));
            this.redisChatMemory.optimization(conversationId);
        }
        return advisedResponse;
    }

    @Override
    public Flux<AdvisedResponse> aroundStream(AdvisedRequest advisedRequest, StreamAroundAdvisorChain chain) {
        // 对于stream方式不做处理
        return chain.nextAroundStream(advisedRequest);
    }

    @Override
    public String getName() {
        return this.getClass().getSimpleName();
    }

    @Override
    public int getOrder() {
        return Advisor.DEFAULT_CHAT_MEMORY_PRECEDENCE_ORDER - 100;
    }
}
