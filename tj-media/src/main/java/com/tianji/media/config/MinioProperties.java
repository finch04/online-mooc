package com.tianji.media.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

// MinioProperties.java
@Data
@ConfigurationProperties(prefix = "minio")
public class MinioProperties {
    private String endpoint;
    private String accessKey;
    private String secretKey;
    private String bucketName;
    private Integer previewExpiry;//预览到期时间 单位：小时
}