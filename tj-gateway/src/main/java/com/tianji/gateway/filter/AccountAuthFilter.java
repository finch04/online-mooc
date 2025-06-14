package com.tianji.gateway.filter;

import com.tianji.authsdk.gateway.util.AuthUtil;
import com.tianji.common.domain.R;
import com.tianji.common.domain.dto.LoginUserDTO;
import com.tianji.gateway.config.AuthProperties;
import com.tianji.gateway.constants.RedisConstants;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static com.tianji.auth.common.constants.JwtConstants.AUTHORIZATION_HEADER;
import static com.tianji.auth.common.constants.JwtConstants.USER_HEADER;

@Component
public class AccountAuthFilter implements GlobalFilter, Ordered {

    private final AuthUtil authUtil;
    private final AuthProperties authProperties;
    private final AntPathMatcher antPathMatcher = new AntPathMatcher();
    private final StringRedisTemplate redisTemplate;

    public AccountAuthFilter(AuthUtil authUtil, AuthProperties authProperties, StringRedisTemplate redisTemplate) {
        this.authUtil = authUtil;
        this.authProperties = authProperties;
        this.redisTemplate = redisTemplate;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        // 1.获取请求request信息
        ServerHttpRequest request = exchange.getRequest();
        String method = request.getMethodValue();
        String path = request.getPath().toString();
        String antPath = method + ":" + path;

        // 2.判断是否是无需登录的路径
        if(isExcludePath(antPath)){
            // 直接放行
            return chain.filter(exchange);
        }

        // 3.尝试获取用户信息
        List<String> authHeaders = exchange.getRequest().getHeaders().get(AUTHORIZATION_HEADER);
        String token = authHeaders == null ? "" : authHeaders.get(0);
        R<LoginUserDTO> r = authUtil.parseToken(token);

        // 4.如果用户是登录状态，尝试更新请求头，传递用户信息
        if(r.success()){
            LoginUserDTO user = r.getData();
            ServerHttpRequest mutatedRequest = request.mutate()
                    .header(USER_HEADER, user.getUserId().toString())
                    .build();
            ServerWebExchange mutatedExchange = exchange.mutate().request(mutatedRequest).build();

            // 校验权限并记录访问（保持 Mono 链的完整性）
            return Mono.just(mutatedExchange)
                    .doOnNext(ex -> authUtil.checkAuth(antPath, r))
                    .flatMap(ex -> recordVisitAsync(ex, chain, user.getUserId().toString()));
        }
        // 4. 处理未登录用户
        String clientIp = request.getRemoteAddress().getAddress().getHostAddress();
        return Mono.just(exchange)
                .doOnNext(ex -> authUtil.checkAuth(antPath, r))
                .flatMap(ex -> recordVisitAsync(ex, chain, clientIp));
    }

    private boolean isExcludePath(String antPath) {
        for (String pathPattern : authProperties.getExcludePath()) {
            if(antPathMatcher.match(pathPattern, antPath)){
                return true;
            }
        }
        return false;
    }

    @Override
    public int getOrder() {
        return 1000;
    }


    private Mono<Void> recordVisitAsync(ServerWebExchange exchange, GatewayFilterChain chain, String identifier) {
        // 异步记录访问量（使用Redis HyperLogLog）
        if (identifier != null) {
            // 异步记录访问（使用 Reactor 线程池）
            Mono.fromRunnable(() -> {
                        String todayKey = RedisConstants.SYSTEM_VISIT_DAILY + LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE);
                        redisTemplate.opsForHyperLogLog().add(todayKey, identifier);
                    }).subscribeOn(Schedulers.boundedElastic())
                    .subscribe();
        }

        // 放行请求
        return chain.filter(exchange);
    }
}
