package com.tianji.common.ratelimiter.Aspect;

import cn.hutool.core.util.StrUtil;
import com.google.common.util.concurrent.RateLimiter;
import com.tianji.common.exceptions.CommonException;
import com.tianji.common.ratelimiter.annotation.TjRateLimiter;
import com.tianji.common.ratelimiter.machine.CFRateLimiter;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.EnvironmentAware;
import org.springframework.core.env.Environment;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * @description 限流切面
 */
@Aspect
//@Component
public class RateLimiterAspect implements EnvironmentAware {
    private final Logger logger = LoggerFactory.getLogger(RateLimiterAspect.class);
    private static final Map<String, CFRateLimiter> BH_RATE_LIMITER_MAP = new ConcurrentHashMap<>();
    private Environment environment;

    @Value("${rate.limit.local.default.permitsPerSecond:1000}")
    private double defaultPermitsPerSecond;

    @Value("${rate.limit.local.default.timeout:1}")
    private long defaultTimeout;

    @Pointcut("@annotation(tjRateLimiter)")
    public void pointCut(TjRateLimiter tjRateLimiter){

    }

    @Around(value = "pointCut(tjRateLimiter)")
    public Object around(ProceedingJoinPoint pjp, TjRateLimiter tjRateLimiter) throws Throwable {
        MethodSignature signature = (MethodSignature) pjp.getSignature();
        String className = pjp.getTarget().getClass().getSimpleName();
        String methodName = signature.getName();
        String rateLimitName = environment.resolvePlaceholders(tjRateLimiter.name());
        if (StrUtil.isEmpty(rateLimitName) || rateLimitName.contains("${")) {
            rateLimitName = className + "-" + methodName;
        }
        CFRateLimiter rateLimiter = this.getRateLimiter(rateLimitName, tjRateLimiter);
        boolean success = rateLimiter.tryAcquire();
        Object[] args = pjp.getArgs();
        if (success){
            return pjp.proceed(args);
        }
        logger.error("around|访问接口过于频繁|{}|{}", className, methodName);
        throw new CommonException("当前访问人数太多啦，请稍后再试");
    }

    /**
     * 获取CFRateLimiter对象
     */
    private CFRateLimiter getRateLimiter(String rateLimitName, TjRateLimiter seckillRateLimiter) {
        //先从Map缓存中获取
        CFRateLimiter bhRateLimiter = BH_RATE_LIMITER_MAP.get(rateLimitName);
        //如果获取的bhRateLimiter为空，则创建bhRateLimiter，注意并发，创建的时候需要加锁
        if (bhRateLimiter == null){
            final String finalRateLimitName = rateLimitName.intern();
            synchronized (finalRateLimitName){
                //double check
                bhRateLimiter = BH_RATE_LIMITER_MAP.get(rateLimitName);
                //获取的cfRateLimiter再次为空
                if (bhRateLimiter == null){
                    double permitsPerSecond = seckillRateLimiter.permitsPerSecond() <= 0 ? defaultPermitsPerSecond : seckillRateLimiter.permitsPerSecond();
                    long timeout = seckillRateLimiter.timeout() <= 0 ? defaultTimeout : seckillRateLimiter.timeout();
                    TimeUnit timeUnit = seckillRateLimiter.timeUnit();
                    bhRateLimiter = new CFRateLimiter(RateLimiter.create(permitsPerSecond), timeout, timeUnit);
                    BH_RATE_LIMITER_MAP.putIfAbsent(rateLimitName, bhRateLimiter);
                }
            }
        }
        return bhRateLimiter;
    }


    @Override
    public void setEnvironment(Environment environment) {
        this.environment = environment;
    }
}
