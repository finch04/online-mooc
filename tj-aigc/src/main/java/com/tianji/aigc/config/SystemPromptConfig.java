package com.tianji.aigc.config;

import com.alibaba.cloud.nacos.NacosConfigManager;
import com.alibaba.nacos.api.config.listener.Listener;
import com.alibaba.nacos.api.exception.NacosException;
import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.Executor;

@Slf4j
@Getter
@Configuration
@RequiredArgsConstructor
public class SystemPromptConfig {

    private final NacosConfigManager nacosConfigManager;
    private final AIProperties aiProperties;

    private String systemChatMessage;

    @PostConstruct
    public void getSystemPrompt() throws NacosException {
        // 从配置属性中获取Nacos配置的数据ID和组名
        String dataId = aiProperties.getSystem().getChat().getDataId();
        String group = aiProperties.getSystem().getChat().getGroup();

        // 从Nacos读取系统聊天提示配置
        this.systemChatMessage = nacosConfigManager.getConfigService()
                .getConfig(dataId,
                        group,
                        this.aiProperties.getSystem().getChat().getTimeoutMs());
        log.info("读取systemChatMessage成功，内容为：{}", this.systemChatMessage);

        // 添加监听器，当Nacos中的配置发生变化时更新本地配置
        nacosConfigManager.getConfigService().addListener(dataId, group, new Listener() {
            @Override
            public Executor getExecutor() {
                return null;
            }

            @Override
            public void receiveConfigInfo(String configInfo) {
                // 当Nacos配置发生变化时，更新systemChatMessage变量
                systemChatMessage = configInfo;
                log.info("更新systemChatMessage成功，内容为：{}", systemChatMessage);
            }
        });
    }

}
