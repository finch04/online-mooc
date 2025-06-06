package com.tianji.aigc.config;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.client.advisor.api.Advisor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 作用：用来配置SpringAI，生成ChatClient对象以及其他的相关bean
 */
@Configuration
public class SpringAIConfig {

    @Bean
    public ChatClient chatClient(ChatClient.Builder chatClientBuilder,
                                 Advisor loggerAdvisor
    ) {
        return chatClientBuilder
                .defaultAdvisors(loggerAdvisor) // 设置默认的增强器
                .build();
    }

    /**
     * 记录日志增强器
     */
    @Bean
    public Advisor loggerAdvisor() {
        return new SimpleLoggerAdvisor();
    }

}
