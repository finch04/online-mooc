package com.tianji.live.service;

import com.tianji.live.utils.IMCacheKeyBuilder;
import jakarta.annotation.Resource;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

/**
 * Author： fsq
 * Description：
 **/
public interface IIMTokenService {
    String generateIMToken(String userId);
    boolean checkIMToken(String imToken);

}
