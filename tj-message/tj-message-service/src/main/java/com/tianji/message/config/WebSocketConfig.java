package com.tianji.message.config;

import com.tianji.authsdk.gateway.util.AuthUtil;
import com.tianji.common.domain.R;
import com.tianji.common.domain.dto.LoginUserDTO;
import com.tianji.common.utils.UserContext;
import com.tianji.message.constants.RedisConstants;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.SimpAttributes;
import org.springframework.messaging.simp.SimpAttributesContextHolder;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

import java.util.Date;
import java.util.concurrent.TimeUnit;

@Slf4j
@Configuration
@EnableWebSocketMessageBroker
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    private final AuthUtil authUtil;

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns("*")
                .withSockJS();

        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns("*");
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        config.enableSimpleBroker("/topic", "/queue");
        config.setApplicationDestinationPrefixes("/app");
        config.setUserDestinationPrefix("/user");
    }

    // 添加认证拦截器
    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(new ChannelInterceptor() {
            @Override
            public Message<?> preSend(Message<?> message, MessageChannel channel) {
                StompHeaderAccessor accessor = StompHeaderAccessor.wrap(message);

                try {

                    String token = accessor.getFirstNativeHeader("authorization");
                    if (token.isEmpty()) {
                        log.error("未携带token");
                    }

                    // 验证Token并获取用户信息
                    R<LoginUserDTO> r = authUtil.parseToken(token.replace("Bearer ", ""));
                    if (!r.success() || r.getData() == null) {
                        log.error("不合法的token");
                    }

                    // 绑定用户到当前连接
                    Long userId = r.getData().getUserId();
                    // 存储用户ID到SimpAttributes
                    SimpAttributes simpAttributes = SimpAttributesContextHolder.currentAttributes();
                    simpAttributes.setAttribute("userId", userId);

                } catch (Exception e) {
                    // 认证失败处理
                    if (StompCommand.CONNECT.equals(accessor.getCommand())) {
                        log.error("认证失败: " + e.getMessage());
                    }
                    log.warn("Authentication error: {}", e.getMessage());
                }

                return message;
            }

            @Override
            public void afterSendCompletion(Message<?> message, MessageChannel channel, boolean sent, Exception ex) {
                // 清理SimpAttributes
                if (StompCommand.DISCONNECT.equals(StompHeaderAccessor.wrap(message).getCommand())) {
                    SimpAttributes simpAttributes = SimpAttributesContextHolder.currentAttributes();
                    simpAttributes.removeAttribute("userId");
                }
            }
        });
    }
}