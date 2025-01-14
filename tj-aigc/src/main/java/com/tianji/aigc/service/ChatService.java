package com.tianji.aigc.service;

import com.tianji.common.utils.UserContext;
import reactor.core.publisher.Flux;

public interface ChatService {

    /**
     * 聊天
     *
     * @param question  问题
     * @param sessionId 会话id
     * @return 回答内容
     */
    Flux<String> chat(String question, String sessionId);

    /**
     * 获取对话id，规则：用户id_会话id
     *
     * @param sessionId 会话id
     * @return 对话id
     */
    static String getConversationId(String sessionId) {
        return UserContext.getUser() + "_" + sessionId;
    }

}
