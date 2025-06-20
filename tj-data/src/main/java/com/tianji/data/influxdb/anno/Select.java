package com.tianji.data.influxdb.anno;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Select {
    // 执行的 Flux 查询语句
    String value();

    // 返回的类型
    Class resultType();

    // 执行的目标存储桶
    String bucket();
}