package com.tianji.live.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.server.standard.ServerEndpointExporter;

/** 开启WebSocket支持
 * @Author: fsq
 * @Date: 2025/10/3 16:27
 * @Version: 1.0
 */
@Configuration
@EnableWebSocket
public class WebSocketConfig{

    @Bean
    public ServerEndpointExporter serverEndpointExporter() {
        return new ServerEndpointExporter();
    }
}
