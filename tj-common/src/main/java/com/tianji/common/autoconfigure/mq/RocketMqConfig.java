package com.tianji.common.autoconfigure.mq;

import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * rocketmq配置类
 */
@Configuration
@ConditionalOnClass(RocketMQTemplate.class)
public class RocketMqConfig {

    @Autowired
    private RocketMQTemplate rocketMQTemplate;

    /**
     * 配置rocketmqHelpler bean
     * @return
     */
    @Bean
    @ConditionalOnClass(RocketMQTemplate.class)
    @ConditionalOnMissingBean
    public RocketMqHelper rocketMqHelper() {
        return new RocketMqHelper(rocketMQTemplate);
    }
}
