package com.tianji.message.config;

import com.tianji.message.service.ISensitiveService;
import com.tianji.message.utils.SensitiveWordDetector;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.StringRedisTemplate;

import javax.annotation.PostConstruct;

@Configuration
public class SensitiveWordConfig {

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Autowired
    private ISensitiveService sensitiveService;

    @PostConstruct
    public void initSensitiveWordDetector() {
        SensitiveWordDetector.init(stringRedisTemplate, sensitiveService);
    }
}