package com.tianji.live.config;

import com.tianji.live.protocol.MessageBody;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
public class RedisConfig {

    // 专门用于MessageBody的RedisTemplate，使用JSON序列化
    @Bean(name = "messageBodyRedisTemplate")
    public RedisTemplate<String, MessageBody> messageBodyRedisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, MessageBody> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);
        
        // 字符串序列化器（key使用）
        StringRedisSerializer stringSerializer = new StringRedisSerializer();
        // JSON序列化器（value使用）
        GenericJackson2JsonRedisSerializer jsonSerializer = new GenericJackson2JsonRedisSerializer();
        
        // 设置key的序列化器
        template.setKeySerializer(stringSerializer);
        template.setHashKeySerializer(stringSerializer);
        
        // 设置value的序列化器
        template.setValueSerializer(jsonSerializer);
        template.setHashValueSerializer(jsonSerializer);
        
        template.afterPropertiesSet();
        return template;
    }
}