package com.tianji.aigc.config;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Data
@Configuration
@ConfigurationProperties(prefix = "tj.ai.dashscope")
public class DashScopeProperties {

    private String key;
    private AppAgent appAgent;

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class AppAgent {
        private String id;
        private List<String> tools;
    }

}