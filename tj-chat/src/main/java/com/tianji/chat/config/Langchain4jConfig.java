package com.tianji.chat.config;

import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.chat.StreamingChatLanguageModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.model.openai.OpenAiStreamingChatModel;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

@Configuration
public class Langchain4jConfig {

    @Bean
    public ChatLanguageModel qwenChatModel() {
        return OpenAiChatModel.builder()
                .baseUrl("http://localhost:11434/v1")
                .apiKey("EMPTY")
                .maxTokens(1000)
                .temperature(0d)
                .timeout(Duration.ofSeconds(15))
                .modelName("deepseek-r1:1.5b")
                .maxRetries(3)
                .build();
    }

    @Bean
    public StreamingChatLanguageModel qwenStreamingChatModel() {
        return OpenAiStreamingChatModel.builder()
                .baseUrl("http://localhost:11434/v1")
                .apiKey("EMPTY")
                .maxTokens(1000)
                .temperature(0d)
                .timeout(Duration.ofSeconds(15))
                .modelName("deepseek-r1:1.5b")
                .build();
    }
}