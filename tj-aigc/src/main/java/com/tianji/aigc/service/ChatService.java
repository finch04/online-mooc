package com.tianji.aigc.service;

import com.tianji.aigc.vo.ChatEventVO;
import com.tianji.common.utils.UserContext;
import reactor.core.publisher.Flux;

public interface ChatService {

    /**
     * 获取对话id，规则：用户id_会话id
     *
     * @param sessionId 会话id
     * @return 对话id
     */
    static String getConversationId(String sessionId) {
        return UserContext.getUser() + "_" + sessionId;
    }

    /**
     * AI大模型的对话
     *
     * @param question  用户问题
     * @param sessionId 会话id
     * @return 内容流
     */
    Flux<ChatEventVO> chat(String question, String sessionId);

    /**
     * 停止对话
     *
     * @param sessionId 会话id
     */
    void stop(String sessionId);
}
