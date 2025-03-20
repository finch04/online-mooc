package com.tianji.aigc.agent;

import com.tianji.aigc.enums.AgentTypeEnum;
import com.tianji.aigc.enums.ChatEventTypeEnum;
import com.tianji.aigc.vo.ChatEventVO;
import reactor.core.publisher.Flux;

/**
 * AI代理接口，定义处理聊天事件和会话的核心能力
 */
public interface Agent {

    /**
     * 表示空参数的预定义数组
     */
    Object[] EMPTY_OBJECTS = new Object[0];

    /**
     * 表示停止事件的预定义ChatEventVO对象
     * 用于在流式处理中标识终止标识
     */
    ChatEventVO STOP_EVENT = ChatEventVO.builder().eventType(ChatEventTypeEnum.STOP.getValue()).build();

    /**
     * 处理流式请求（如流式回答）
     *
     * @param question  用户输入的问题
     * @param sessionId 会话唯一标识
     * @return 包含中间结果的反应式事件流（Flux）
     */
    Flux<ChatEventVO> processStream(String question, String sessionId);

    /**
     * 处理标准请求（非流式）
     *
     * @param question  用户输入的问题
     * @param sessionId 会话唯一标识
     * @return 最终处理结果字符串
     */
    String process(String question, String sessionId);

    /**
     * 获取智能体类型标识
     *
     * @return 代理类型枚举值（如：ROUTE、RECOMMEND等）
     */
    AgentTypeEnum getAgentType();

    /**
     * 停止指定会话的处理
     *
     * @param sessionId 需要终止的会话ID
     */
    void stop(String sessionId);
}

