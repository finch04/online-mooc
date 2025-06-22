package com.tianji.gateway.config;

import lombok.Data;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Set;

/**
 * 日志配置  配置此路径将不被influxdb收集
 */
@Data
@Component
@ConfigurationProperties(prefix = "tj.data")
public class LogProperties implements InitializingBean {

    private Set<String> excludePath;

    // 构造函数中初始化集合
    public LogProperties() {
        this.excludePath = new HashSet<>();
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        // 添加默认不拦截的路径
        excludePath.add("/jwks");
        excludePath.add("/data/url/page/log/like");
        excludePath.add("/data/url/page/log");
    }
}
