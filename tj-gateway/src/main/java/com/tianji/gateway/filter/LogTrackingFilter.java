package com.tianji.gateway.filter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tianji.authsdk.gateway.util.AuthUtil;
import com.tianji.common.autoconfigure.mq.RabbitMqHelper;
import com.tianji.common.constants.MqConstants;
import com.tianji.common.domain.R;
import com.tianji.common.domain.dto.LoginUserDTO;
import com.tianji.common.domain.vo.LogBusinessVO;
import com.tianji.gateway.config.AuthProperties;
import com.tianji.gateway.utils.LogCollector;
import com.tianji.gateway.utils.RequestHelper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpRequestDecorator;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import static com.tianji.auth.common.constants.JwtConstants.AUTHORIZATION_HEADER;
import static com.tianji.gateway.constants.RedisConstants.*;

@Component
@RequiredArgsConstructor
@Slf4j
public class LogTrackingFilter implements GlobalFilter, Ordered {

    private final AuthUtil authUtil;
    private final AuthProperties authProperties;
    private final AntPathMatcher antPathMatcher = new AntPathMatcher();
    private final LogCollector logCollector;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        // 处理文件上传:如果是文件上传则不记录日志
        MediaType mediaType = exchange.getRequest().getHeaders().getContentType();
        boolean flag = RequestHelper.isUploadFile(mediaType);

        // 忽略路径处理:获得请求路径然后与logProperties的路进行匹配，匹配上则不记录日志
        String path = exchange.getRequest().getURI().getPath();
        Set<String> ignoreTestUrl = authProperties.getExcludePath();
        for (String testUrl : ignoreTestUrl) {
            if (antPathMatcher.match(testUrl, path)) {
                flag = true;
                break;
            }
        }

        // 无需记录日志：直接放过请求
        if (flag) {
            return chain.filter(exchange);
        }

        // 1.获取请求信息
        ServerHttpRequest request = exchange.getRequest();
        String method = request.getMethodValue();
        String antPath = method + ":" + path;

        // 2.创建日志对象
        LogBusinessVO vo = new LogBusinessVO();
        vo.setRequestId(exchange.getRequest().getId());
        vo.setHost(request.getURI().getHost());
        vo.setHostAddress(request.getRemoteAddress().getAddress().getHostAddress());
        vo.setRequestUri(path);
        vo.setRequestMethod(method);

        // 3.获取并缓存请求体
        return cacheRequestBody(exchange, vo)
                .flatMap(cachedExchange -> {
                    // 4.处理用户信息
                    List<String> authHeaders = request.getHeaders().get(AUTHORIZATION_HEADER);
                    String token = authHeaders == null? "" : authHeaders.get(0);
                    R<LoginUserDTO> r = authUtil.parseToken(token);

                    if (r.success()) {
                        LoginUserDTO user = r.getData();
                        vo.setUserId(user.getUserId());
                    }

                    // 5.记录请求时间
                    long startTime = System.currentTimeMillis();

                    // 6.继续处理请求并记录响应信息
                    return chain.filter(cachedExchange)
                            .doOnSuccess(v -> {
                                // 处理成功响应
                                ServerHttpResponse response = cachedExchange.getResponse();
                                logCollector.logResponse(vo, response, startTime, null);
                            })
                            .doOnError(throwable -> {
                                // 处理错误响应
                                ServerHttpResponse response = cachedExchange.getResponse();
                                logCollector.logResponse(vo, response, startTime, throwable);
                            })
                            .onErrorResume(throwable -> {
                                // 确保错误被捕获但继续传播
                                ServerHttpResponse response = cachedExchange.getResponse();
                                return logCollector.logException(vo, response, throwable);
                            });
                });
    }

    private Mono<ServerWebExchange> cacheRequestBody(ServerWebExchange exchange, LogBusinessVO vo) {

        ServerHttpRequest request = exchange.getRequest();
        // 处理GET请求（没有请求体）
        if (request.getMethodValue().equalsIgnoreCase("GET")) {
            vo.setRequestBody("");
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
                        vo.setRequestBody(requestBody);
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

    @Override
    public int getOrder() {
        // 设置为较高优先级，但低于RequestIdRelayFilter
        return Ordered.HIGHEST_PRECEDENCE + 1;
    }
}