package com.tianji.gateway.filter;

import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;

/**
 * 文件上传大小限制过滤器
 */
@Component
public class FileSizeLimitFilter implements GlobalFilter, Ordered {

    // 2MB = 2 * 1024 * 1024 bytes
    private static final long MAX_FILE_SIZE = 2 * 1024 * 1024;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        
        // 只拦截multipart/form-data类型的请求（文件上传）
        if (isMultipartRequest(request)) {
            // 监听请求体数据，计算总大小
            return request.getBody().reduce(0L, (total, buffer) -> {
                long bufferSize = buffer.readableByteCount();
                // 累加并检查是否超过限制
                if (total + bufferSize > MAX_FILE_SIZE) {
                    throw new IllegalStateException("File size exceeds 2MB limit");
                }
                return total + bufferSize;
            }).then(chain.filter(exchange))
              .onErrorResume(e -> {
                  // 超过限制时返回错误响应
                  ServerHttpResponse response = exchange.getResponse();
                  response.setStatusCode(HttpStatus.PAYLOAD_TOO_LARGE);
                  response.getHeaders().setContentType(MediaType.TEXT_PLAIN);
                  String message = "File size cannot exceed 2MB";
                  DataBuffer buffer = response.bufferFactory().wrap(message.getBytes(StandardCharsets.UTF_8));
                  return response.writeWith(Flux.just(buffer));
              });
        }
        
        // 非文件上传请求直接放行
        return chain.filter(exchange);
    }

    /**
     * 判断是否为文件上传请求
     */
    private boolean isMultipartRequest(ServerHttpRequest request) {
        MediaType contentType = request.getHeaders().getContentType();
        return contentType != null && contentType.isCompatibleWith(MediaType.MULTIPART_FORM_DATA);
    }

    @Override
    public int getOrder() {
        // 顺序要早于认证过滤器，避免不必要的认证处理
        return 900;
    }
}