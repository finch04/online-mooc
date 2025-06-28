package com.tianji.common.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

// 定义一个注解 `NoWrapper`,用于标记Controller中的方法，表示该方法不需要包装返回结果。
@Retention(RetentionPolicy.RUNTIME)  // 保留策略为运行时
@Target(ElementType.METHOD)  // 该注解仅适用于方法
public @interface NoWrapper {

}
