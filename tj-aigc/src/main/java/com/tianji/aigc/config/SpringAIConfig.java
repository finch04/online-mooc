package com.tianji.aigc.config;

import com.tianji.aigc.memory.JdbcChatMemory;
import com.tianji.aigc.memory.RedisChatMemory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.client.advisor.api.Advisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 作用：用来配置SpringAI，生成ChatClient对象以及其他的相关bean
 */
@Configuration
public class SpringAIConfig {

    @Bean
    public ChatClient chatClient(ChatClient.Builder chatClientBuilder,
                                 Advisor loggerAdvisor, // 日志增强器
                                 Advisor messageChatMemoryAdvisor // 对话记忆的增强器
    ) {
        return chatClientBuilder
                .defaultAdvisors(loggerAdvisor, messageChatMemoryAdvisor) // 设置默认的增强器
                .build();
    }

    /**
     * 记录日志增强器
     */
    @Bean
    public Advisor loggerAdvisor() {
        return new SimpleLoggerAdvisor();
    }

    // @Bean
    // public ChatMemory redisChatMemory() {
    //     return new RedisChatMemory();
    // }

    /**
     * 对话记忆的增强器
     *
     * @param redisChatMemory 基于Redis的ChatMemory
     */
    // @Bean
    // public Advisor messageChatMemoryAdvisor(ChatMemory redisChatMemory) {
    //     return new MessageChatMemoryAdvisor(redisChatMemory);
    // }

    @Bean
    public ChatMemory jdbcChatMemory(){
        return new JdbcChatMemory();
    }

    /**
     * 对话记忆的增强器
     *
     * @param jdbcChatMemory 基于MySQL的ChatMemory
     */
    @Bean
    public Advisor messageChatMemoryAdvisor(ChatMemory jdbcChatMemory) {
        return new MessageChatMemoryAdvisor(jdbcChatMemory);
    }

}
