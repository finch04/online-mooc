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
import com.tianji.gateway.config.LogProperties;
import com.tianji.gateway.utils.LogCollector;
import com.tianji.gateway.utils.RequestHelper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.reactivestreams.Publisher;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferFactory;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpRequestDecorator;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.http.server.reactive.ServerHttpResponseDecorator;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

import static com.tianji.auth.common.constants.JwtConstants.AUTHORIZATION_HEADER;

@Component
@RequiredArgsConstructor
@Slf4j
public class LogTrackingFilter implements GlobalFilter, Ordered {

    private final AuthUtil authUtil;
    private final AuthProperties authProperties;
    private final LogProperties logProperties;
    private final AntPathMatcher antPathMatcher = new AntPathMatcher();
    private final LogCollector logCollector;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        // 处理文件上传:如果是文件上传则不记录日志
        MediaType mediaType = exchange.getRequest().getHeaders().getContentType();
        boolean flag = RequestHelper.isUploadFile(mediaType);

        // 忽略路径处理:获得请求路径然后与logProperties的路进行匹配，匹配上则不记录日志
        String path = exchange.getRequest().getURI().getPath();
        Set<String> ignoreTestUrl = logProperties.getExcludePath();
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
                    String token = Objects.requireNonNullElse(request.getHeaders().get(AUTHORIZATION_HEADER), List.of("")).get(0);
                    R<LoginUserDTO> r = authUtil.parseToken(token);

                    if (r.success()) {
                        LoginUserDTO user = r.getData();
                        vo.setUserId(user.getUserId());
                        vo.setRoleId(user.getRoleId());
                    }

                    // 5.记录请求时间
                    long startTime = System.currentTimeMillis();

                    ServerHttpResponse originalResponse = cachedExchange.getResponse();
                    DataBufferFactory bufferFactory = originalResponse.bufferFactory();
                    ServerHttpResponseDecorator decoratedResponse = new ServerHttpResponseDecorator(originalResponse) {
                        @Override
                        public Mono<Void> writeWith(Publisher<? extends DataBuffer> body) {

                            // 检查响应头是否包含 X-Skip-Logging 且值为 true
                            boolean skipLogging = originalResponse.getHeaders().containsKey("X-Skip-Logging") &&
                                    "true".equalsIgnoreCase(originalResponse.getHeaders().getFirst("X-Skip-Logging"));

                            if (skipLogging) {
                                // 跳过日志记录，直接返回
                                return super.writeWith(body);
                            }
                            if (body instanceof Flux) {
                                Flux<? extends DataBuffer> fluxBody = (Flux<? extends DataBuffer>) body;
                                return super.writeWith(fluxBody.map(dataBuffer -> {
                                    byte[] content = new byte[dataBuffer.readableByteCount()];
                                    dataBuffer.read(content);
                                    DataBufferUtils.release(dataBuffer);
                                    String s = new String(content, Charset.forName("UTF-8"));
                                    try {
                                        // 使用 readTree 方法解析 JSON
                                        com.fasterxml.jackson.databind.JsonNode rootNode = objectMapper.readTree(s);

                                        // 提取通用字段
                                        Integer code = rootNode.path("code").asInt();
                                        String msg = rootNode.path("msg").asText();
                                        String requestId = rootNode.path("requestId").asText();

                                        vo.setResponseCode(code);
                                        vo.setResponseMsg(msg);

                                        // 使用 R 对象中的 requestId
                                        if (!requestId.isEmpty()) {
                                            vo.setRequestId(requestId);
                                        }

                                        // 处理响应数据
                                        if (code == 200 && rootNode.has("data")) {
                                            // 将 data 字段作为原始字符串存储，限制最大长度
//                                            String dataStr = rootNode.get("data").toString();
//                                            if (dataStr.length() > 100) {
//                                                dataStr = dataStr.substring(0, 100) + "...[truncated]";
//                                            }
//                                            vo.setResponseBody(dataStr);
                                        }
                                    } catch (JsonProcessingException e) {
//                                        log.error("Failed to parse response JSON: {}", s);
                                        log.info("走JSON解析降级逻辑");
                                        //如果返回内容太多导致json被截断，走正则匹配
                                        parseKeyInfo(s, vo);
                                    }
                                    byte[] uppedContent = s.getBytes();
                                    return bufferFactory.wrap(uppedContent);
                                }));
                            }
                            return super.writeWith(body);
                        }
                    };

                    // 6.继续处理请求并记录响应信息
                    return chain.filter(cachedExchange.mutate().response(decoratedResponse).build())
                            .doFinally(signalType -> {
                                long endTime = System.currentTimeMillis();
                                vo.setResponseTime(endTime - startTime);
                                logCollector.logResponse(vo);
                            });
                });
    }

    //降级方案  如果返回内容太多导致json被截断，走正则匹配，拿到code和msg就行
    private LogBusinessVO parseKeyInfo(String response, LogBusinessVO logBusinessVO) {
        // 解析code
        if (response.contains("\"code\":")) {
            int start = response.indexOf("\"code\":") + 7;
            int end = response.indexOf(",", start);
            if (end > start) {
                try {
                    logBusinessVO.setResponseCode(Integer.parseInt(response.substring(start, end)));
                } catch (NumberFormatException e) {
                    logBusinessVO.setResponseMsg("code格式错误");
                }
            }
        }

        // 解析msg
        if (response.contains("\"msg\":\"")) {
            int start = response.indexOf("\"msg\":\"") + 7;
            int end = response.indexOf("\"", start);
            if (end > start) {
                logBusinessVO.setResponseMsg(response.substring(start, end));
            }
        }
        return logBusinessVO;
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
        return Ordered.HIGHEST_PRECEDENCE;
    }
}