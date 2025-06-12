package com.tianji.chat.config;

import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.chat.StreamingChatLanguageModel;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.model.openai.OpenAiEmbeddingModel;
import dev.langchain4j.model.openai.OpenAiStreamingChatModel;
import dev.langchain4j.service.SystemMessage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

@Configuration
public class Langchain4jConfig {

    @Value("${langchain4j.base-url}")
    private String baseUrl;

    @Value("${langchain4j.api-key}")
    private String apiKey;

    @Value("${langchain4j.max-tokens}")
    private int maxTokens;

    @Value("${langchain4j.timeout-seconds}")
    private int timeoutSeconds;

    @Value("${langchain4j.model-name}")
    private String modelName;

    @Value("${langchain4j.max-retries}")
    private int maxRetries;

    @Value("${langchain4j.chat-model-temperature}")
    private double chatModelTemperature;

    @Value("${langchain4j.streaming-chat-model-temperature}")
    private double streamingChatModelTemperature;

    @Bean
    public ChatLanguageModel qwenChatModel() {
        return OpenAiChatModel.builder()
                .baseUrl(baseUrl)
                .apiKey(apiKey)
                .maxTokens(maxTokens)
                .temperature(chatModelTemperature)
                .timeout(Duration.ofSeconds(timeoutSeconds))
                .modelName(modelName)
                .maxRetries(maxRetries)
                .build();
    }


    @Bean
    public StreamingChatLanguageModel qwenStreamingChatModel() {
        return OpenAiStreamingChatModel.builder()
                .baseUrl(baseUrl)
                .apiKey(apiKey)
                .maxTokens(maxTokens)
                .temperature(streamingChatModelTemperature)
                .timeout(Duration.ofSeconds(timeoutSeconds))
                .modelName(modelName)
                .build();
    }

    @Bean
    public EmbeddingModel embeddingModel() {
        return OpenAiEmbeddingModel.builder()
                .baseUrl(baseUrl)
                .apiKey(apiKey)
                .timeout(Duration.ofSeconds(timeoutSeconds))
                .modelName(modelName)
                .build();
    }
}