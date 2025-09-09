package com.tianji.aigc.config;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(AliYunProperties.class)
public class AliYunConfig {

    @Bean
    public OSS aliOssClient(AliYunProperties prop) {
        return new OSSClientBuilder()
                .build(prop.getOss().getEndpoint(),
                        prop.getOss().getAccessId(),
                        prop.getOss().getAccessKey());
    }

}
