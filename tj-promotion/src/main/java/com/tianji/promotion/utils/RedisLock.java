package com.tianji.promotion.utils;

import com.tianji.common.utils.BooleanUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.util.concurrent.TimeUnit;

@RequiredArgsConstructor
public class RedisLock {

    private final String key;
    private final StringRedisTemplate redisTemplate;

    /**
     * 尝试获取锁
     * @param leaseTime 锁自动释放时间
     * @param unit 时间单位
     * @return 是否获取成功，true:获取锁成功;false:获取锁失败
     */
    public boolean tryLock(long leaseTime, TimeUnit unit){
        // 1.获取线程名称
        String value = Thread.currentThread().getName();
        // 2.获取锁
        Boolean success = redisTemplate.opsForValue().setIfAbsent(key, value, leaseTime, unit);
        // 3.返回结果
        return BooleanUtils.isTrue(success);
    }

    /**
     * 释放锁
     */
    public void unlock(){
        redisTemplate.delete(key);
    }
}