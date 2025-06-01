package com.tianji.common.ratelimiter.annotation;

import java.lang.annotation.*;
import java.util.concurrent.TimeUnit;

/**
 * @description 自定义限流注解
 */
@Inherited
@Documented
@Target({ElementType.METHOD, ElementType.FIELD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface TjRateLimiter {

    /**
     * 限流器名称，如果不设置，默认是类名加方法名。如果多个接口设置了同一个名称，则使用同一个限流器
     */
    String name() default "";

    /**
     * 一秒内允许通过的请求数QPS
     */
    int permitsPerSecond() default 0;

    /**
     * 获取令牌超时时间
     */
    long timeout() default 0;

    /**
     * 获取令牌超时时间单位
     */
    TimeUnit timeUnit() default TimeUnit.SECONDS;
}
