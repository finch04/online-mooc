package com.tianji.aigc.config;

import com.tianji.aigc.memory.RedisChatMemory;
import com.tianji.common.utils.WebUtils;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.PromptChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.retry.RetryCallback;
import org.springframework.retry.RetryContext;
import org.springframework.retry.RetryListener;
import org.springframework.retry.support.RetryTemplate;

@Configuration
public class SpringAIConfig {

    /**
     * 创建并配置自定义重试监听器Bean
     *
     * @param retryTemplate Spring Retry模板对象，用于注册重试监听器
     * @return RetryListener 已注册到模板的重试监听器实例，将由Spring容器管理
     *
     * 实现说明：
     * 1. 创建匿名RetryListener实现，在重试操作期间管理Web属性
     * 2. 将监听器注册到提供的RetryTemplate实例
     */
    @Bean
    public RetryListener customizeRetryTemplate(RetryTemplate retryTemplate) {
        // 创建自定义重试监听器，实现以下核心功能：
        // - 重试开始时设置上下文标识
        // - 重试结束后清理上下文标识
        RetryListener retryListener = new RetryListener() {
            @Override
            public <T, E extends Throwable> boolean open(RetryContext context, RetryCallback<T, E> callback) {
                WebUtils.setAttribute("SpringAI", "yes");
                return true;
            }

            @Override
            public <T, E extends Throwable> void close(RetryContext context, RetryCallback<T, E> callback, Throwable throwable) {
                WebUtils.removeAttribute("SpringAI");
            }
        };

        // 将监听器注册到重试模板
        retryTemplate.registerListener(retryListener);
        return retryListener;
    }


    @Bean
    public ChatClient dashScopeChatClient(ChatClient.Builder dashScopeChatClientBuilder, ChatMemory chatMemory) {
        return dashScopeChatClientBuilder
                .defaultAdvisors(new PromptChatMemoryAdvisor(chatMemory))//会话记忆
                .build();
    }

    @Bean
    public ChatClient openAiChatClient(ChatClient.Builder openAiChatClientBuilder) {
        return openAiChatClientBuilder.build();
    }

    @Bean
    public ChatMemory chatMemory() {
        //基于内存存储会话
        // return new InMemoryChatMemory();
        return new RedisChatMemory();
    }

}
