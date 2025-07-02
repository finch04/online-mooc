package com.tianji.aigc.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@ConfigurationProperties(prefix = "tj.ali")
public class AliYunProperties {

    private OssProperties oss;

    @Data
    public static class OssProperties {
        private String accessId;
        private String accessKey;
        private String endpoint;
        private String bucket;
    }
}