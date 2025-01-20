package com.tianji.aigc.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "tj.ai.prompt")
public class AIProperties {
    private System system;

    @Data
    public static class System {
        private Chat chat;

        @Data
        public static class Chat {
            private String dataId;
            private String group = "DEFAULT_GROUP";

            private long timeoutMs = 20000L; // 读取的超时时间，单位毫秒
        }
    }
}
