package com.tianji.aigc.service;

import com.tianji.aigc.vo.ChatEventVO;
import reactor.core.publisher.Flux;

public interface ChatService {

    /**
     * AI大模型的对话
     *
     * @param question  用户问题
     * @param sessionId 会话id
     * @return 内容流
     */
    Flux<ChatEventVO> chat(String question, String sessionId);
}
