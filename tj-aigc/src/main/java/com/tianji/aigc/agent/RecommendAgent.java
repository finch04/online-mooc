package com.tianji.aigc.agent;

import cn.hutool.core.map.MapUtil;
import com.tianji.aigc.config.SystemPromptConfig;
import com.tianji.aigc.constants.Constant;
import com.tianji.aigc.enums.AgentTypeEnum;
import com.tianji.aigc.tools.CourseTools;
import com.tianji.common.utils.UserContext;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.advisor.QuestionAnswerAdvisor;
import org.springframework.ai.chat.client.advisor.api.Advisor;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.redis.RedisVectorStore;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class RecommendAgent extends AbstractAgent{

    private final SystemPromptConfig systemPromptConfig;
    private final CourseTools courseTools;
    private final VectorStore vectorStore;
    private final Advisor messageChatMemoryAdvisor;//对话增强

    @Override
    public String systemMessage() {
        return this.systemPromptConfig.getRecommendAgentSystemMessage().get();
    }

    @Override
    public AgentTypeEnum getAgentType() {
        return AgentTypeEnum.RECOMMEND;
    }
    @Override
    public Object[] tools() {
        return new Object[]{courseTools};
    }

    @Override
    public Map<String, Object> toolContext(String sessionId, String requestId) {
        var userId = UserContext.getUser();
        return MapUtil.<String,Object>builder()
                .put(Constant.USER_ID, userId)
                .put(Constant.REQUEST_ID,requestId)
                .build();
    }

    @Override
    public List<Advisor> advisors(String question) {
        var searchRequest = SearchRequest.builder().query(question).similarityThreshold(0.63f).topK(5).build();
        return List.of(new QuestionAnswerAdvisor(vectorStore,searchRequest),this.messageChatMemoryAdvisor);
    }
}
