package com.tianji.aigc.config;

import com.tianji.aigc.advisor.RecordOptimizationAdvisor;
import com.tianji.aigc.memory.MyChatMemoryRepository;
import com.tianji.aigc.memory.RedisChatMemoryRepository;
import com.tianji.aigc.tools.CourseTools;
import com.tianji.aigc.tools.OrderTools;
import com.tianji.common.constants.Constant;
import com.tianji.common.utils.WebUtils;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.client.advisor.api.Advisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.ChatMemoryRepository;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.retry.RetryCallback;
import org.springframework.retry.RetryContext;
import org.springframework.retry.RetryListener;
import org.springframework.retry.support.RetryTemplate;

@Configuration
public class SpringAIConfig {

    @Value("${tj.ai.memory.max:100}")
    private Integer maxMessages;

    /**
     * 创建并配置自定义重试监听器Bean
     * <p>
     * 实现说明：
     * 1. 创建匿名RetryListener实现，在重试操作期间管理Web属性
     * 2. 将监听器注册到提供的RetryTemplate实例
     *
     * @param retryTemplate Spring Retry模板对象，用于注册重试监听器
     * @return RetryListener 已注册到模板的重试监听器实例，将由Spring容器管理
     */
    @Bean
    public RetryListener customizeRetryTemplate(RetryTemplate retryTemplate) {
        // 创建自定义重试监听器，实现以下核心功能：
        // - 重试开始时设置上下文标识
        // - 重试结束后清理上下文标识
        RetryListener retryListener = new RetryListener() {
            @Override
            public <T, E extends Throwable> boolean open(RetryContext context, RetryCallback<T, E> callback) {
                WebUtils.setAttribute(Constant.SPRING_AI_ATTR, Constant.SPRING_AI_FLAG);
                return true;
            }

            @Override
            public <T, E extends Throwable> void close(RetryContext context, RetryCallback<T, E> callback, Throwable throwable) {
                WebUtils.removeAttribute(Constant.SPRING_AI_ATTR);
            }
        };

        // 将监听器注册到重试模板
        retryTemplate.registerListener(retryListener);
        return retryListener;
    }

    /**
     * 配置 ChatClient
     */
    @Bean
    public ChatClient chatClient(@Qualifier("dashscopeChatModel") ChatModel dashScopeChatModel,
                                 Advisor loggerAdvisor, // 日志记录器
                                 Advisor messageChatMemoryAdvisor,
                                 Advisor recordOptimizationAdvisor, // 记录优化
                                  CourseTools courseTools, // 课程工具
                                  OrderTools orderTools // 预下单工具
    ) {
        return ChatClient.builder(dashScopeChatModel)
                .defaultAdvisors(loggerAdvisor, messageChatMemoryAdvisor, recordOptimizationAdvisor) //添加 Advisor 功能增强
                 .defaultTools(courseTools, orderTools)
                .build();
    }

    @Bean
    public ChatClient openAiChatClient(@Qualifier("openAiChatModel") ChatModel openAiChatModel,
                                       Advisor loggerAdvisor  // 日志记录器
    ) {
        return ChatClient.builder(openAiChatModel)
                .defaultAdvisors(loggerAdvisor)
                .build();
    }

    /**
     * 日志记录器
     */
    @Bean
    public Advisor loggerAdvisor() {
        return new SimpleLoggerAdvisor();
    }

    @Bean
    public ChatMemoryRepository redisChatMemoryRepository() {
        return new RedisChatMemoryRepository();
    }

    @Bean
    public ChatMemory chatMemory(ChatMemoryRepository chatMemoryRepository) {
        // 基于 chatMemoryRepository 对象构建 chatMemory 对象
        return MessageWindowChatMemory.builder()
                .chatMemoryRepository(chatMemoryRepository)
                .maxMessages(this.maxMessages) // 最多保存 100 条对话, 如果超出的话，会自动删除最旧的对话
                .build();
    }

    /**
     * 基于Redis的会话记忆，聊天记忆整合到message列表中实现多轮对话
     */
    @Bean
    public Advisor messageChatMemoryAdvisor(ChatMemory chatMemory) {
        // 创建基于 chatMemory 的 Advisor 对象
        return MessageChatMemoryAdvisor.builder(chatMemory).build();
    }

    /**
     * 优化对话历史记录
     */
    @Bean
    public Advisor recordOptimizationAdvisor(MyChatMemoryRepository myChatMemoryRepository) {
        return new RecordOptimizationAdvisor(myChatMemoryRepository);
    }
}
