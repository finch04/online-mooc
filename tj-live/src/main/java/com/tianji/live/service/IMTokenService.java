package com.tianji.live.service;

import com.tianji.live.utils.IMCacheKeyBuilder;
import jakarta.annotation.Resource;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

/**
 * Author： roy
 * Description：
 **/
@Service
public class IMTokenService {

    @Resource
    private IMCacheKeyBuilder imCacheKeyBuilder;

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    public String generateIMToken(String userId){
        String imToken = generateRandomString(8);
        String tokenKey = imCacheKeyBuilder.buildIMTokenCacheKey(imToken);
        stringRedisTemplate.opsForValue().set(tokenKey,userId);
        //这个Token只在获取WS的URL和建立WS连接两个操作之间使用，过期时间不用很长
        stringRedisTemplate.expire(tokenKey,10, TimeUnit.SECONDS);
        return imToken;
    }

    public boolean checkIMToken(String imToken){
        String tokenKey = imCacheKeyBuilder.buildIMTokenCacheKey(imToken);
        Object imTokenRecord = stringRedisTemplate.opsForValue().get(tokenKey);
        if(null == imTokenRecord){
            return false;
        }
        return true;
    }

    private String generateRandomString(int length){
        String result = "";
        String letters = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";

        for (int i = 0; i < length; i++) {
            result += letters.charAt(ThreadLocalRandom.current().nextInt(0,letters.length()));
        }

        return result;
    }
}
