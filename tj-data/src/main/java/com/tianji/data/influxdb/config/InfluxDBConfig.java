package com.tianji.data.influxdb.config;


import com.tianji.data.influxdb.core.Executor;
import com.tianji.data.influxdb.core.ParameterHandler;
import com.tianji.data.influxdb.core.ResultSetHandler;
import org.influxdb.InfluxDB;
import org.influxdb.InfluxDBFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;

/**
 * 时序数据库配置类
 */
@Configuration
public class InfluxDBConfig {

    @Value("${spring.influx.url}")
    private String url;

    @Value("${spring.influx.username:admin}")  // 默认值：admin
    private String username;

    @Value("${spring.influx.password:}")  // 默认空值
    private String password;

//    @Value("${spring.influx.retention-policy:autogen}")  // 默认保留策略
//    private String retentionPolicy;

    /**
     * 创建InfluxDB连接实例并设置保留策略
     */
    @Bean
    public InfluxDB influxDB() {
        // 创建InfluxDB客户端连接
        InfluxDB influxDB = InfluxDBFactory.connect(url, username, password);

        // 设置保留策略
//        influxDB.setRetentionPolicy("rp_point");

        // 设置日志级别（可选）
        influxDB.setLogLevel(InfluxDB.LogLevel.BASIC);

        // 验证连接并返回
        influxDB.ping();
        System.out.println("InfluxDB连接成功: " + url);
        return influxDB;
    }

    @Bean(name = "executor")
    public Executor executor(InfluxDB influxDB) {
        return new Executor(influxDB);
    }

    @Bean(name = "parameterHandler")
    public ParameterHandler parameterHandler(InfluxDB influxDB) {
        return new ParameterHandler();
    }

    @Bean(name = "resultSetHandler")
    public ResultSetHandler resultSetHandler(InfluxDB influxDB) {
        return new ResultSetHandler();
    }
}
