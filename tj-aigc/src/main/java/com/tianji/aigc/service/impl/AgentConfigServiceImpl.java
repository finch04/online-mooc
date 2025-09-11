package com.tianji.aigc.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tianji.aigc.constants.RedisConstants;
import com.tianji.aigc.domain.vo.AgentConfigVO;
import com.tianji.aigc.domain.vo.SessionVO;
import com.tianji.aigc.service.AgentConfigService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class AgentConfigServiceImpl implements AgentConfigService {

    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper; // Jackson用于JSON序列化

    /**
     * 保存完整配置到Redis
     */
    public void saveAgentConfig(AgentConfigVO config) throws JsonProcessingException {
        HashOperations<String, String, String> hashOps = redisTemplate.opsForHash();
        
        // 序列化热门问题为JSON字符串
        String hotQuestionsJson = objectMapper.writeValueAsString(config.getHotQuestions());
        
        // 批量设置Hash字段
        hashOps.putAll(RedisConstants.AGENT_CONFIG_KEY, Map.of(
                RedisConstants.TITLE_FIELD, config.getTitle(),
                RedisConstants.DESCRIBE_FIELD, config.getDescribe(),
                RedisConstants.HOT_QUESTIONS_FIELD, hotQuestionsJson
        ));
        
        // 设置过期时间
        redisTemplate.expire(RedisConstants.AGENT_CONFIG_KEY, 30, TimeUnit.DAYS);
    }

    /**
     * 从Redis获取完整配置
     */
    public AgentConfigVO getAgentConfig() throws JsonProcessingException {
        HashOperations<String, String, String> hashOps = redisTemplate.opsForHash();
        Map<String, String> configMap = hashOps.entries(RedisConstants.AGENT_CONFIG_KEY);
        
        if (configMap.isEmpty()) {
            return null;
        }
        
        // 反序列化热门问题
        List<AgentConfigVO.Example> hotQuestions = null;
        String hotQuestionsJson = configMap.get(RedisConstants.HOT_QUESTIONS_FIELD);
        if (hotQuestionsJson != null) {
            hotQuestions = objectMapper.readValue(hotQuestionsJson, new TypeReference<List<AgentConfigVO.Example>>() {});
        }
        
        return AgentConfigVO.builder()
                .title(configMap.get(RedisConstants.TITLE_FIELD))
                .describe(configMap.get(RedisConstants.DESCRIBE_FIELD))
                .hotQuestions(hotQuestions)
                .build();
    }
}