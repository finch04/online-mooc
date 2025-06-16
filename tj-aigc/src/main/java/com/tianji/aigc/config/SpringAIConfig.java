package com.tianji.aigc.config;

import com.tianji.aigc.memory.jdbc.JdbcChatMemory;
import com.tianji.aigc.memory.mogodb.MongoDBChatMemory;
import com.tianji.aigc.memory.redis.RedisChatMemory;
import com.tianji.aigc.tools.CourseTools;
import com.tianji.aigc.tools.OrderTools;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.client.advisor.api.Advisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
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
                                 Advisor messageChatMemoryAdvisor, // 对话记忆的增强器
                                 CourseTools courseTools, // 课程工具
                                 OrderTools orderTools // 预下单工具
    ) {
        return chatClientBuilder
                .defaultAdvisors(loggerAdvisor, messageChatMemoryAdvisor) // 设置默认的增强器
                .defaultTools(courseTools,orderTools) // 设置默认的tools
                .build();
    }

    /**
     * 记录日志增强器
     */
    @Bean
    public Advisor loggerAdvisor() {
        return new SimpleLoggerAdvisor();
    }

    @Bean
    @ConditionalOnProperty(prefix = "tj.ai", name = "chat-memory", havingValue = "Redis")
    public ChatMemory redisChatMemory() {
        return new RedisChatMemory();
    }

    @Bean
    @ConditionalOnProperty(prefix = "tj.ai", name = "chat-memory", havingValue = "MYSQL")
    public ChatMemory jdbcChatMemory() {
        return new JdbcChatMemory();
    }

    @Bean
    @ConditionalOnProperty(prefix = "tj.ai", name = "chat-memory", havingValue = "MongoDB")
    public ChatMemory mongoDBChatMemory() {
        return new MongoDBChatMemory();
    }

    /**
     * 对话记忆的增强器
     */
    @Bean
    public Advisor messageChatMemoryAdvisor(ChatMemory chatMemory) {
        return new MessageChatMemoryAdvisor(chatMemory);
    }

}
