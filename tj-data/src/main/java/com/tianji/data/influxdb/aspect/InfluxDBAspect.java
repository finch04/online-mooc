package com.tianji.data.influxdb.aspect;

import com.tianji.data.influxdb.anno.Insert;
import com.tianji.data.influxdb.anno.Select;
import com.tianji.data.influxdb.core.Executor;
import com.tianji.data.influxdb.core.ParameterHandler;
import com.tianji.data.influxdb.core.ResultSetHandler;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.List;
import java.util.Map;

/**
 * @ClassName InfluxDBAspect.java
 * @Description TODO
 */
@Aspect
@Component
public class InfluxDBAspect {

    private final Executor executor;
    private final ParameterHandler parameterHandler;
    private final ResultSetHandler resultSetHandler;

    @Autowired
    public InfluxDBAspect(Executor executor, ParameterHandler parameterHandler, ResultSetHandler resultSetHandler) {
        this.executor = executor;
        this.parameterHandler = parameterHandler;
        this.resultSetHandler = resultSetHandler;
    }

    @Around("@annotation(select)")
    public Object select(ProceedingJoinPoint joinPoint, Select select) {
        MethodSignature methodSignature = (MethodSignature) joinPoint.getSignature();
        Method method = methodSignature.getMethod();
        Select selectAnnotation = method.getAnnotation(Select.class);
        // 获得执行参数
        Parameter[] parameters = method.getParameters();
        // 获得执行参数值
        Object[] args = joinPoint.getArgs();
        // 获得执行 Flux 查询语句
        String sql = selectAnnotation.value();
        // 替换参数
        sql = parameterHandler.handleParameter(parameters, args, sql);
        // 注解声明返回类型
        Class<?> resultType = selectAnnotation.resultType();
        // 查询结果
        List<Map<String, Object>> reultList = executor.select(sql, selectAnnotation.bucket());
        // 根据返回类型返回结果
        return resultSetHandler.handleResultSet(reultList, method, sql, resultType);
    }

    @Around("@annotation(insert)")
    public void insert(ProceedingJoinPoint joinPoint, Insert insert) {
        // 获得执行参数值
        Object[] args = joinPoint.getArgs();
        executor.insert(args);
    }
}