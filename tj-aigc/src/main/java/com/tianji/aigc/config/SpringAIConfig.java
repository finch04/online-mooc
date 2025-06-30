package com.tianji.aigc.config;

import com.tianji.aigc.advisor.RecordOptimizationAdvisor;
import com.tianji.aigc.memory.MyChatMemory;
import com.tianji.aigc.memory.jdbc.JdbcChatMemory;
import com.tianji.aigc.memory.mogodb.MongoDBChatMemory;
import com.tianji.aigc.memory.redis.RedisChatMemory;
import com.tianji.aigc.tools.CourseTools;
import com.tianji.aigc.tools.OrderTools;
import com.tianji.common.constants.Constant;
import com.tianji.common.utils.WebUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.client.advisor.api.Advisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.embedding.TokenCountBatchingStrategy;
import org.springframework.ai.vectorstore.VectorStore;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.retry.RetryCallback;
import org.springframework.retry.RetryContext;
import org.springframework.retry.RetryListener;
import org.springframework.retry.support.RetryTemplate;
import redis.clients.jedis.JedisPooled;


import java.net.URI;
import java.net.URISyntaxException;

/**
 * 作用：用来配置SpringAI，生成ChatClient对象以及其他的相关bean
 */
@Configuration
@RequiredArgsConstructor
@Slf4j
public class SpringAIConfig {

//    private final RedisProperties redisProperties;

//    @Bean
//    public ChatClient chatClient(ChatClient.Builder chatClientBuilder,
//                                 Advisor loggerAdvisor, // 日志增强器
//                                 Advisor messageChatMemoryAdvisor, // 对话记忆的增强器
//                                 CourseTools courseTools, // 课程工具
//                                 OrderTools orderTools // 预下单工具
//    ) {
//        return chatClientBuilder
//                .defaultAdvisors(loggerAdvisor, messageChatMemoryAdvisor) // 设置默认的增强器
//                .defaultTools(courseTools,orderTools) // 设置默认的tools
//                .build();
//    }


    /**
     * 配置 ChatClient
     */
    @Bean
    public ChatClient dashScopeChatClient(ChatClient.Builder dashScopeChatClientBuilder,
                                          Advisor loggerAdvisor,
                                          Advisor messageChatMemoryAdvisor,
                                          Advisor recordOptimizationAdvisor, // 记录优化
                                          CourseTools courseTools, // 课程工具
                                          OrderTools orderTools // 预下单工具
    ) {  // 日志记录器
        return dashScopeChatClientBuilder
                .defaultAdvisors(loggerAdvisor, messageChatMemoryAdvisor, recordOptimizationAdvisor) //添加 Advisor 功能增强
                // .defaultTools(courseTools, orderTools) //添加默认工具
                .build();
    }

    @Bean
    public ChatClient openAiChatClient(ChatClient.Builder openAiChatClientBuilder,
                                       Advisor loggerAdvisor  // 日志记录器
    ) {
        return openAiChatClientBuilder
                .defaultAdvisors(loggerAdvisor)
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

    @Bean
    public Advisor recordOptimizationAdvisor(MyChatMemory myChatMemory){
        return new RecordOptimizationAdvisor(myChatMemory);
    }

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


//    @Bean
//    public VectorStore RedisVectorStore(JedisPooled jedisPooled, EmbeddingModel embeddingModel) {
//        return RedisVectorStore.builder(jedisPooled, embeddingModel)
//                .indexName("tianji:")                // Optional: defaults to "spring-ai-index"
//                .prefix("tianji:embedding:")                  // Optional: defaults to "embedding:"
//                .metadataFields(                         // Optional: define metadata fields for filtering
//                        RedisVectorStore.MetadataField.tag("country"),
//                        RedisVectorStore.MetadataField.numeric("year"))
//                .initializeSchema(true)                   // Optional: defaults to false
//                .batchingStrategy(new TokenCountBatchingStrategy()) // Optional: defaults to TokenCountBatchingStrategy
//                .build();
//    }
//
//    @Bean
//    public JedisPooled jedisPooled() throws URISyntaxException {
//        URI uri = new URI(redisProperties.getUrl());
//
//        //获取host
//        String host = uri.getHost();
//
//        //获取port
//        var port = uri.getPort();
//
//        //获取密码
//        String password = extractPasswordFromURI(uri);
//        log.info("密码是:"+password);
//        return new JedisPooled(host,port,"default",password);
//    }
//
//    // 提取密码的辅助方法
//    private String extractPasswordFromURI(URI uri) {
//        String userInfo = uri.getUserInfo();
//        if (userInfo != null) {
//            // 处理标准格式: redis://:password@host:port
//            String[] parts = userInfo.split(":", 2);
//            // 当格式为 ":password" 时，parts[0]为空字符串，parts[1]为密码
//            return parts.length >= 2 ? parts[1] : userInfo;
//        }
//        return null;
//    }
}
