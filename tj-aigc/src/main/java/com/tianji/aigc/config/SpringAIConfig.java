package com.tianji.aigc.config;

import com.tianji.aigc.memory.RedisChatMemory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.PromptChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SpringAIConfig {


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
