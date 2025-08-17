package com.tianji.aigc.agent;

import com.tianji.aigc.config.SystemPromptConfig;
import com.tianji.aigc.constants.Constant;
import com.tianji.aigc.enums.AgentTypeEnum;
import com.tianji.aigc.tools.OrderTools;
import com.tianji.common.utils.UserContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@RequiredArgsConstructor
public class BuyAgent extends AbstractAgent {

    private final SystemPromptConfig systemPromptConfig;
    private final OrderTools orderTools;

    @Override
    public String systemMessage() {
        return this.systemPromptConfig.getBuyAgentSystemMessage().get();
    }

    @Override
    public AgentTypeEnum getAgentType() {
        return AgentTypeEnum.BUY;
    }

    @Override
    public Object[] tools() {
        return new Object[]{orderTools};
    }

    @Override
    public Map<String, Object> toolContext(String sessionId, String requestId) {
        var userId = UserContext.getUser();
        return Map.of(
                Constant.USER_ID, userId, // 设置用户id参数
                Constant.REQUEST_ID, requestId  // 设置请求id参数
        );
    }
}
