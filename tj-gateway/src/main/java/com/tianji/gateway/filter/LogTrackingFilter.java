package com.tianji.gateway.filter;

import com.tianji.authsdk.gateway.util.AuthUtil;
import com.tianji.common.autoconfigure.mq.RabbitMqHelper;
import com.tianji.common.constants.MqConstants;
import com.tianji.common.domain.R;
import com.tianji.common.domain.dto.LoginUserDTO;
import com.tianji.common.domain.vo.LogBusinessVO;
import com.tianji.gateway.config.AuthProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpRequestDecorator;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

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

        // 3.获取并缓存请求体
        return cacheRequestBody(exchange, log)
                .flatMap(cachedExchange -> {
                    // 4.处理用户信息
                    List<String> authHeaders = request.getHeaders().get(AUTHORIZATION_HEADER);
                    String token = authHeaders == null ? "" : authHeaders.get(0);
                    R<LoginUserDTO> r = authUtil.parseToken(token);

                    if(r.success()) {
                        LoginUserDTO user = r.getData();
                        log.setUserId(user.getUserId());
                    }

                    // 5.记录请求时间
                    long startTime = System.currentTimeMillis();

                    // 6.继续处理请求并记录响应信息
                    return chain.filter(cachedExchange)
                            .doOnSuccess(v -> {
                                // 处理成功响应
                                ServerHttpResponse response = cachedExchange.getResponse();
                                logResponse(log, response, startTime, null);
                            })
                            .doOnError(throwable -> {
                                // 处理错误响应
                                ServerHttpResponse response = cachedExchange.getResponse();
                                logResponse(log, response, startTime, throwable);
                            })
                            .onErrorResume(throwable -> {
                                // 确保错误被捕获但继续传播
                                return Mono.error(throwable);
                            });
                });
    }

    private Mono<ServerWebExchange> cacheRequestBody(ServerWebExchange exchange, LogBusinessVO log) {
        ServerHttpRequest request = exchange.getRequest();

        // 处理GET请求（没有请求体）
        if (request.getMethodValue().equalsIgnoreCase("GET")) {
            log.setRequestBody("");
            return Mono.just(exchange);
        }

        // 处理有请求体的情况
        AtomicReference<String> requestBodyRef = new AtomicReference<>("");

        // 读取并缓存请求体
        Flux<DataBuffer> body = request.getBody();
        return DataBufferUtils.join(body)
                .flatMap(dataBuffer -> {
                    byte[] bytes = new byte[dataBuffer.readableByteCount()];
                    dataBuffer.read(bytes);
                    DataBufferUtils.release(dataBuffer); // 释放资源

                    if (bytes.length > 0) {
                        String requestBody = new String(bytes, StandardCharsets.UTF_8);
                        requestBodyRef.set(requestBody);
                        log.setRequestBody(requestBody);
                    }

                    // 创建新的请求，使用缓存的请求体
                    ServerHttpRequest newRequest = new ServerHttpRequestDecorator(request) {
                        @Override
                        public Flux<DataBuffer> getBody() {
                            if (bytes.length == 0) {
                                return Flux.empty();
                            }
                            DataBuffer buffer = exchange.getResponse().bufferFactory().wrap(bytes);
                            return Flux.just(buffer);
                        }
                    };

                    return Mono.just(exchange.mutate().request(newRequest).build());
                })
                .defaultIfEmpty(exchange); // 如果没有请求体，直接返回原始exchange
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