package com.tianji.aigc.agent;

import cn.hutool.core.date.DateUtil;
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
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * 课程咨询智能体
 */
@Component
@RequiredArgsConstructor
public class ConsultAgent extends AbstractAgent {

    private final SystemPromptConfig systemPromptConfig;
    private final VectorStore vectorStore;
    private final CourseTools courseTools;

    @Override
    public String systemMessage() {
        return this.systemPromptConfig.getConsultAgentSystemMessage().get();
    }

    @Override
    public AgentTypeEnum getAgentType() {
        return AgentTypeEnum.CONSULT;
    }

    @Override
    public List<Advisor> advisors(String question) {
        var searchRequest = SearchRequest.builder().query(question).similarityThreshold(0.56f).topK(5).build();
        return List.of(new QuestionAnswerAdvisor(vectorStore, searchRequest));
    }

    @Override
    public Object[] tools() {
        return new Object[]{courseTools};
    }

    @Override
    public Map<String, Object> toolContext(String sessionId, String requestId) {
        var userId = UserContext.getUser();
        return MapUtil.<String, Object>builder() // 设置tool列表
                .put(Constant.USER_ID, userId) // 设置用户id参数
                .put(Constant.REQUEST_ID, requestId) // 设置请求id参数
                .build();
    }

    @Override
    public Map<String, Object> systemMessageParams() {
        return Map.of("now", DateUtil.now());
    }
}
