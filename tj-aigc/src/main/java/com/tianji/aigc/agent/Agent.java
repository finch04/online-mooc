package com.tianji.aigc.agent;

import com.tianji.aigc.enums.AgentTypeEnum;
import com.tianji.aigc.vo.ChatEventVO;
import org.springframework.ai.chat.client.advisor.api.Advisor;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.Map;

/**
 * AI代理接口，定义处理聊天事件和会话的核心能力
 */
public interface Agent {

    /**
     * 表示空参数的预定义数组
     */
    Object[] EMPTY_OBJECTS = new Object[0];

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

    /**
     * 获取系统提示信息模板，默认为空字符串，子类可以覆盖重写该方法以返回自定义的系统提示信息。
     *
     * @return 系统提示的文本模板
     */
    default String systemMessage() {
        return "";
    }


    /**
     * 获取工具列表，默认返回空数组。子类需根据需求覆盖此方法。
     */
    default Object[] tools() {
        return EMPTY_OBJECTS;
    }

    /**
     * 创建并返回一个工具上下文的空Map对象。
     *
     * @param sessionId 会话标识符
     * @param requestId 请求标识符
     * @return 默认返回一个空的Map对象，子类可以覆盖重写该方法以返回自定义的工具上下文。
     */
    default Map<String, Object> toolContext(String sessionId, String requestId) {
        return Map.of();
    }

    /**
     * Advisor列表，默认返回空对象
     */
    default List<Advisor> advisors() {
        return List.of();
    }

    /**
     * 创建并返回一个Advisor的空Map对象。
     *
     * @param sessionId 会话标识符
     * @param requestId 请求标识符
     * @return 默认返回一个空的Map对象，子类可以覆盖重写该方法以返回自定义的工具上下文。
     */
    default Map<String, Object> advisorParams(String sessionId, String requestId) {
        return Map.of();
    }

    /**
     * 获取系统提示信息模板的参数，默认为空Map，子类可以覆盖重写该方法以返回自定义的系统提示信息参数。
     */
    default Map<String, Object> systemMessageParams() {
        return Map.of();
    }

}