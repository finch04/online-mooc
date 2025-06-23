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
        private Chat chat; //系统提示语，用于课程推荐、购买业务
        private Chat title; //用于提取标题
        private Chat routeAgent; // 路由智能体系统提示词
        private Chat recommendAgent; //课程推荐智能体系统提示词
        private Chat buyAgent; //购买智能体系统提示词
        private Chat consultAgent; // 咨询智能体系统提示词
        private Chat knowledgeAgent; // 知识讲解智能体系统提示词


        @Data
        public static class Chat {
            private String dataId;
            private String group = "DEFAULT_GROUP";
            private long timeoutMs = 20000L; // 读取的超时时间，单位毫秒
        }
    }
}
