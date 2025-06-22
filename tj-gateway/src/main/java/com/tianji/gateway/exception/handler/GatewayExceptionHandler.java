package com.tianji.gateway.exception.handler;

import com.tianji.common.constants.Constant;
import com.tianji.common.domain.R;
import com.tianji.common.domain.vo.LogBusinessVO;
import com.tianji.common.exceptions.CommonException;
import com.tianji.common.exceptions.UnauthorizedException;
import com.tianji.common.utils.JsonUtils;
import com.tianji.gateway.utils.LogCollector;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.web.reactive.error.ErrorWebExceptionHandler;
import org.springframework.cloud.gateway.support.NotFoundException;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Objects;

import static com.tianji.common.constants.ErrorInfo.Code.FAILED;
import static com.tianji.common.constants.ErrorInfo.Msg.SERVER_INTER_ERROR;

@Slf4j
@Component
@Order(-1) // 确保优先级高于其他处理器
@Configuration
@RequiredArgsConstructor
public class GatewayExceptionHandler implements ErrorWebExceptionHandler {

    private final LogCollector logCollector;

    @Override
    public Mono<Void> handle(ServerWebExchange exchange, Throwable ex) {
        // 1.获取响应
        ServerHttpResponse response = exchange.getResponse();
        // 2.判断是否已处理
        if (response.isCommitted()) {
            // 如果已经提交，直接结束，避免重复处理
            return Mono.error(ex);
        }

        // 3.记录请求时间
        long startTime = System.currentTimeMillis();

        try {
            // 4.按照异常类型进行翻译处理，翻译的结果易于前端理解
            String message;
            int code = FAILED;
            if (ex instanceof UnauthorizedException) {
                // 登录异常，直接返回状态码
                UnauthorizedException e = (UnauthorizedException) ex;
                return Mono.error(new ResponseStatusException(e.getStatus(), e.getMessage(), e));
            } else if (ex instanceof CommonException) {
                CommonException e = (CommonException) ex;
                code = e.getCode();
                message = e.getMessage();
            } else if (ex instanceof NotFoundException) {
                message = "服务不存在";
            } else if (ex instanceof ResponseStatusException) {
                message = ex.getMessage();
            } else {
                message = SERVER_INTER_ERROR;
            }

            // 5.设置响应结果为 JSON
            response.getHeaders().setContentType(MediaType.APPLICATION_JSON);

            // 6.封装响应结果并写出
            R<Object> r = R.error(code, message);
            List<String> requestIds = response.getHeaders().get(Constant.REQUEST_ID_HEADER);
            if (requestIds != null && !requestIds.isEmpty()) {
                r.requestId(requestIds.get(0));
            }

            byte[] resp = JsonUtils.toJsonStr(r).getBytes(StandardCharsets.UTF_8);

            // 7.记录日志
            ServerHttpRequest request = exchange.getRequest();
            LogBusinessVO vo = createLogBusinessVO(request, response, ex, code, message, startTime);
            if (r.getRequestId() != null) {
                vo.setRequestId(r.getRequestId());
            }
            logCollector.logResponse(vo);

            return response.writeWith(
                    Mono.fromSupplier(
                            () -> response.bufferFactory().wrap(resp)
                    ));
        } catch (Exception e) {
            log.error("处理异常时发生错误", e);
            return Mono.error(e);
        }
    }

    private LogBusinessVO createLogBusinessVO(ServerHttpRequest request, ServerHttpResponse response,
                                              Throwable ex, int code, String message, long startTime) {
        LogBusinessVO vo = new LogBusinessVO();
        //在网关层抛异常（例如服务不存在）是无法被进入到后面过滤器生成request_id，所以这里无法获取到系统生成的request_id，只有request自带本身的id
        vo.setRequestId(Objects.requireNonNullElse(request.getId(), ""));
        vo.setHost(request.getURI().getHost());
        vo.setHostAddress(Objects.requireNonNull(request.getRemoteAddress()).getAddress().getHostAddress());
        vo.setRequestUri(request.getURI().getPath());
        vo.setRequestMethod(Objects.requireNonNull(request.getMethod()).name());
        vo.setResponseCode(code);
        vo.setResponseMsg(message);

        long endTime = System.currentTimeMillis();
        vo.setResponseTime(endTime - startTime);

        return vo;
    }
}