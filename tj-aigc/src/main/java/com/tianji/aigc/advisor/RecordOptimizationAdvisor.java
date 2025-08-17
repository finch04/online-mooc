package com.tianji.aigc.advisor;

import cn.hutool.core.convert.Convert;
import com.tianji.aigc.enums.AgentTypeEnum;
import com.tianji.aigc.memory.MyChatMemoryRepository;
import org.springframework.ai.chat.client.ChatClientRequest;
import org.springframework.ai.chat.client.ChatClientResponse;
import org.springframework.ai.chat.client.advisor.api.Advisor;
import org.springframework.ai.chat.client.advisor.api.AdvisorChain;
import org.springframework.ai.chat.client.advisor.api.BaseAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;

public class RecordOptimizationAdvisor implements BaseAdvisor {

    private final MyChatMemoryRepository myChatMemoryRepository;

    public RecordOptimizationAdvisor(MyChatMemoryRepository myChatMemoryRepository) {
        this.myChatMemoryRepository = myChatMemoryRepository;
    }

    @Override
    public ChatClientRequest before(ChatClientRequest chatClientRequest, AdvisorChain advisorChain) {
        return chatClientRequest;
    }

    @Override
    public ChatClientResponse after(ChatClientResponse chatClientResponse, AdvisorChain advisorChain) {
        var response = chatClientResponse.chatResponse();
        // 获取大模型返回的文本数据，如果返回的文本数据中包含agentName，则进行记录优化
        assert response != null;
        var text = response.getResult().getOutput().getText();
        var agentTypeEnum = AgentTypeEnum.agentNameOf(text);
        if (null != agentTypeEnum) {
            // 需要进行对记录优化
            var key = ChatMemory.CONVERSATION_ID;
            var conversationId = Convert.toStr(chatClientResponse.context().get(key));
            this.myChatMemoryRepository.optimization(conversationId);
        }
        return chatClientResponse;
    }

    @Override
    public int getOrder() {
        return Advisor.DEFAULT_CHAT_MEMORY_PRECEDENCE_ORDER - 100;
    }
}
