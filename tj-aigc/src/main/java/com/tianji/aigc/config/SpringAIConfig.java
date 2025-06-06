package com.tianji.aigc.config;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.client.advisor.api.Advisor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SpringAIConfig {

    @Bean
    public ChatClient chatClient(ChatClient.Builder chatClientBuilder,Advisor loggerAdvisor) {
        return chatClientBuilder
                .defaultAdvisors(loggerAdvisor)
                .build();
    }

    @Bean
    public Advisor loggerAdivisor() {
        return new SimpleLoggerAdvisor();
    }
}
