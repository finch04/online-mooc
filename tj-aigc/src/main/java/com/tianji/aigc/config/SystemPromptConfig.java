package com.tianji.aigc.config;

import com.alibaba.cloud.nacos.NacosConfigManager;
import com.alibaba.nacos.api.exception.NacosException;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class SystemPromptConfig {

    private final NacosConfigManager nacosConfigManager;

    private String systemChatMessage;

    @PostConstruct
    public String getSystemPrompt() throws NacosException {
        return nacosConfigManager.getConfigService()
                .getConfig("system-chat-message.txt", "DEFAULT_GROUP", 5000);
    }

}
