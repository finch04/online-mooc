package com.tianji.aigc.config;

import com.tianji.aigc.memory.RedisChatMemory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.PromptChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;

@Configuration
public class SpringAIConfig {

    @Value("classpath:/prompts/system-message.txt")
    private Resource systemResource;

    @Bean
    public ChatClient chatClient(ChatClient.Builder builder, ChatMemory chatMemory, VectorStore vectorStore) {
        return builder
                .defaultSystem(systemResource)
                .defaultAdvisors(new PromptChatMemoryAdvisor(chatMemory),//会话记忆
                        new SimpleLoggerAdvisor() //输出日志
                        //         new QuestionAnswerAdvisor(vectorStore, SearchRequest.defaults())
                )
                // .defaultFunctions("orderFunction")
                .build();
    }

    @Bean
    public ChatMemory chatMemory() {
        //基于内存存储会话
        // return new InMemoryChatMemory();
        return new RedisChatMemory();
    }

}
