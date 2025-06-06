package com.tianji.aigc.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "tj.ai.prompt")
public class AIProperties {

    private System system; // 系统提示语，用于课程推荐、购买业务

    @Data
    public static class System {
        private Chat chat; // 系统提示语，用于课程推荐、购买业务

        @Data
        public static class Chat {
            private String dataId;
            private String group = "DEFAULT_GROUP";
            private long timeoutMs = 20000L; // 读取的超时时间，单位毫秒
        }
    }
}
