package com.tianji.message.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tianji.common.domain.dto.PageDTO;
import com.tianji.common.exceptions.BadRequestException;
import com.tianji.common.utils.StringUtils;
import com.tianji.message.domain.dto.NoticeTaskDTO;
import com.tianji.message.domain.po.NoticeTask;
import com.tianji.message.domain.po.Sensitive;
import com.tianji.message.domain.query.SensitiveQuery;
import com.tianji.message.mapper.SensitiveMapper;
import com.tianji.message.service.ISensitiveService;
import com.tianji.message.utils.SensitiveWordDetector; // 假设工具类路径
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import static com.tianji.message.constants.RedisConstants.*;
import javax.annotation.Resource;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
public class SensitiveServiceImpl extends ServiceImpl<SensitiveMapper, Sensitive> implements ISensitiveService {

    @Resource
    private RedisTemplate<String, Object> redisTemplate;


    @Override
    public PageDTO<Sensitive> getAllSensitiveWords(SensitiveQuery query) {
        Page<Sensitive> page = query.toMpPage();
        // 2.过滤条件
        page = lambdaQuery()
                .like(query.getName() != null, Sensitive::getSensitives, query.getName()).page(page);
        // 3.数据转换
        return PageDTO.of(page, Sensitive.class);
    }

    @Override
    public boolean saveSensitive(Sensitive sensitive) {
        boolean b = SensitiveWordDetector.addSensitiveWord(sensitive);
        refreshRedisCache();
        return b;
    }

    @Override
    public boolean updateSensitive(Sensitive sensitive) {
        boolean b = SensitiveWordDetector.updateSensitiveWord(sensitive);
        refreshRedisCache();
        return b;
    }

    @Override
    public boolean deleteSensitive(Long id) {
        boolean b = SensitiveWordDetector.deleteSensitiveWord(id);
        refreshRedisCache();
        return b;
    }

    /**
     * 刷新Redis缓存
     */
    private void refreshRedisCache() {
        try {
            // 从数据库获取所有敏感词
            List<Sensitive> sensitives = this.list();
            if (sensitives == null || sensitives.isEmpty()) {
                // 清空Redis缓存
                redisTemplate.delete(SENSITIVE_WORDS_KEY);
                redisTemplate.delete(SENSITIVE_DICTIONARY_KEY);
                return;
            }

            // 提取敏感词文本
            List<String> sensitiveWordsList = sensitives.stream()
                    .map(Sensitive::getSensitives)
                    .collect(java.util.stream.Collectors.toList());

            // 生成敏感词字典
            generateAndSaveDictionary(sensitiveWordsList);

            // 保存敏感词列表到Redis
            String wordsStr = String.join(",", sensitiveWordsList);
            redisTemplate.opsForValue().set(SENSITIVE_WORDS_KEY, wordsStr, SENSITIVE_CACHE_TTL, TimeUnit.SECONDS);

        } catch (Exception e) {
            throw new BadRequestException("刷新敏感词缓存失败");
        }
    }

    /**
     * 生成敏感词字典并保存到Redis
     */
    private void generateAndSaveDictionary(List<String> words) {
        if (words == null || words.isEmpty()) {
            return;
        }
        // 生成敏感词字典
        java.util.Map<String, Object> map = new java.util.HashMap<>(words.size());
        for (String word : words) {
            java.util.Map<String, Object> curMap = map;
            int len = word.length();
            for (int i = 0; i < len; i++) {
                String key = String.valueOf(word.charAt(i));
                java.util.Map<String, Object> wordMap = (java.util.Map<String, Object>) curMap.get(key);
                if (wordMap == null) {
                    wordMap = new java.util.HashMap<>(2);
                    wordMap.put("isEnd", "0");
                    curMap.put(key, wordMap);
                }
                curMap = wordMap;
                if (i == len - 1) {
                    curMap.put("isEnd", "1");
                }
            }
        }

        // 保存到Redis
        redisTemplate.opsForValue().set(SENSITIVE_DICTIONARY_KEY, map, SENSITIVE_CACHE_TTL, TimeUnit.SECONDS);
    }
}