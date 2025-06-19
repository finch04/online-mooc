package com.tianji.gateway.filter;

import com.tianji.authsdk.gateway.util.AuthUtil;
import com.tianji.common.autoconfigure.mq.RabbitMqHelper;
import com.tianji.common.constants.MqConstants;
import com.tianji.common.domain.R;
import com.tianji.common.domain.dto.LoginUserDTO;
import com.tianji.common.domain.vo.LogBusinessVO;
import com.tianji.gateway.config.AuthProperties;
import com.tianji.gateway.constants.RedisConstants;
import lombok.RequiredArgsConstructor;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static com.tianji.auth.common.constants.JwtConstants.AUTHORIZATION_HEADER;

@Component
@RequiredArgsConstructor
public class LogTrackingFilter implements GlobalFilter, Ordered {

    private final AuthUtil authUtil;
    private final AuthProperties authProperties;
    private final AntPathMatcher antPathMatcher = new AntPathMatcher();
    private final StringRedisTemplate redisTemplate;
    private final RabbitMqHelper mqHelper;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        // 1.获取请求信息
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getPath().toString();
        String method = request.getMethodValue();
        String antPath = method + ":" + path;

        // 2.创建日志对象
        LogBusinessVO log = new LogBusinessVO();
        log.setRequestId(exchange.getRequest().getId());
        log.setHost(request.getURI().getHost());
        log.setHostAddress(request.getRemoteAddress().getAddress().getHostAddress());
        log.setRequestUri(path);
        log.setRequestMethod(method);

        // 3.获取请求体长度（实际获取请求体内容需要特殊处理）
        log.setRequestBody(""); // 实际项目中可能需要特殊处理获取请求体

        // 4.处理用户信息
        List<String> authHeaders = request.getHeaders().get(AUTHORIZATION_HEADER);
        String token = authHeaders == null ? "" : authHeaders.get(0);
        R<LoginUserDTO> r = authUtil.parseToken(token);

        if(r.success()) {
            LoginUserDTO user = r.getData();
            log.setUserId(user.getUserId());
//            log.setUserName(user.getUsername());
        }

        // 5.记录请求时间
        long startTime = System.currentTimeMillis();

        // 6.继续处理请求并记录响应信息
        return chain.filter(exchange)
                .doOnSuccess(v -> {
                    // 处理成功响应
                    ServerHttpResponse response = exchange.getResponse();
                    logResponse(log, response, startTime, null);
                })
                .doOnError(throwable -> {
                    // 处理错误响应
                    ServerHttpResponse response = exchange.getResponse();
                    logResponse(log, response, startTime, throwable);
                })
                .onErrorResume(throwable -> {
                    // 确保错误被捕获但继续传播
                    return Mono.error(throwable);
                });
    }

    private void logResponse(LogBusinessVO log, ServerHttpResponse response, long startTime, Throwable throwable) {
        long endTime = System.currentTimeMillis();
        // 设置响应状态码
        log.setResponseCode(response.getStatusCode() != null ? response.getStatusCode().value() : 500);
        // 设置响应消息
        log.setResponseMsg(throwable == null ? "SUCCESS" : throwable.getMessage());
        // 设置响应时间
        log.setResponseTime(endTime - startTime);

        // 异步发送日志消息
        mqHelper.send(MqConstants.Exchange.DATA_EXCHANGE,
                MqConstants.Key.DATA_LOG_KEY, log);

    }

    @Override
    public int getOrder() {
        // 确保在AccountAuthFilter之后执行
        return Ordered.LOWEST_PRECEDENCE;
    }
}