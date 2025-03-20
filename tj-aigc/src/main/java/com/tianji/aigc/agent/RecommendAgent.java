package com.tianji.aigc.agent;

import cn.hutool.core.map.MapUtil;
import com.tianji.aigc.config.SystemPromptConfig;
import com.tianji.aigc.constants.Constant;
import com.tianji.aigc.enums.AgentTypeEnum;
import com.tianji.aigc.tools.CourseTools;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.advisor.QuestionAnswerAdvisor;
import org.springframework.ai.chat.client.advisor.api.Advisor;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class RecommendAgent extends AbstractAgent {

    private final SystemPromptConfig systemPromptConfig;
    private final VectorStore vectorStore;
    private final CourseTools courseTools;

    @Override
    public String systemMessage() {
        return this.systemPromptConfig.getRecommendAgentSystemMessage().get();
    }

    @Override
    public AgentTypeEnum getAgentType() {
        return AgentTypeEnum.RECOMMEND;
    }

    @Override
    public List<Advisor> advisors() {
        var searchRequest = SearchRequest.builder().query("").topK(999).build();
        return List.of(new QuestionAnswerAdvisor(vectorStore, searchRequest));
    }

    @Override
    public Object[] tools() {
        return new Object[]{courseTools};
    }

    @Override
    public Map<String, Object> toolContext(Long userId, String requestId) {
        return MapUtil.<String, Object>builder() // 设置tool列表
                .put(Constant.USER_ID, userId) // 设置用户id参数
                .put(Constant.REQUEST_ID, requestId) // 设置请求id参数
                .build();
    }

    @Override
    public Map<String, Object> systemMessageParams() {
        return super.systemMessageParams();
    }
}
