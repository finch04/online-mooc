package com.tianji.common.autoconfigure.mvc.advice;

import com.alibaba.csp.sentinel.slots.block.authority.AuthorityException;
import com.alibaba.csp.sentinel.slots.block.degrade.DegradeException;
import com.alibaba.csp.sentinel.slots.block.flow.FlowException;
import com.alibaba.csp.sentinel.slots.block.flow.param.ParamFlowException;
import com.alibaba.csp.sentinel.slots.system.SystemBlockException;
import com.tianji.common.constants.Constant;
import com.tianji.common.domain.R;
import com.tianji.common.exceptions.CommonException;
import com.tianji.common.exceptions.DbException;
import com.tianji.common.exceptions.SentinelException;
import com.tianji.common.utils.WebUtils;
import feign.FeignException;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindException;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.util.NestedServletException;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import java.lang.reflect.UndeclaredThrowableException;
import java.util.stream.Collectors;

import static com.tianji.common.enums.SentinelExceptionType.*;

@RestControllerAdvice
@Slf4j
public class CommonExceptionAdvice {

    /**
     * sentinel限流、熔断灯异常处理
     */
    @ExceptionHandler(UndeclaredThrowableException.class)
    public Object handleUndeclaredThrowableException(UndeclaredThrowableException e) {
        SentinelException se = null;
        if (e.getUndeclaredThrowable() instanceof FlowException) {
            se = new SentinelException(SENTINEL_FLOW.getValue(), SENTINEL_FLOW.getDesc());
        } else if (e.getUndeclaredThrowable() instanceof DegradeException) {
            se = new SentinelException(SENTINEL_DEGRADE.getValue(), SENTINEL_DEGRADE.getDesc());
        } else if (e.getUndeclaredThrowable() instanceof ParamFlowException) {
            se = new SentinelException(SENTINEL_PARAMAS.getValue(), SENTINEL_PARAMAS.getDesc());
        } else if (e.getUndeclaredThrowable() instanceof SystemBlockException) {
            se = new SentinelException(SENTINEL_SYSTEM.getValue(), SENTINEL_SYSTEM.getDesc());
        } else if (e.getUndeclaredThrowable() instanceof AuthorityException) {
            se = new SentinelException(SENTINEL_AUTHORITY.getValue(), SENTINEL_AUTHORITY.getDesc());
        } else {
            se = new SentinelException(SENTINEL_FLOW.getValue(), SENTINEL_FLOW.getDesc());
        }
        return processResponse(se.getStatus(), se.getCode(), se.getMessage());
    }

    @ExceptionHandler(DbException.class)
    public Object handleDbException(DbException e) {
        log.error("mysql数据库操作异常 -> ", e);
        return processResponse(e.getStatus(), e.getCode(), e.getMessage());
    }

    @ExceptionHandler(CommonException.class)
    public Object handleBadRequestException(CommonException e) {
        log.error("自定义异常 -> {} , 状态码：{}, 异常原因：{}  ",e.getClass().getName(), e.getStatus(), e.getMessage());
        log.debug("", e);
        return processResponse(e.getStatus(), e.getCode(), e.getMessage());
    }

    @ExceptionHandler(FeignException.class)
    public Object handleFeignException(FeignException e) {
        log.error("feign远程调用异常 -> ", e);
        return processResponse(e.status(), e.status(), e.contentUTF8());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Object handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {
        String msg = e.getBindingResult().getAllErrors()
                .stream().map(ObjectError::getDefaultMessage)
                .collect(Collectors.joining("|"));
        log.error("请求参数校验异常 -> {}", msg);
        log.debug("", e);
        return processResponse(400, 400, msg);
    }
    @ExceptionHandler(BindException.class)
    public Object handleBindException(BindException e) {
        log.error("请求参数绑定异常 ->BindException， {}", e.getMessage());
        log.debug("", e);
        return processResponse(400, 400, "请求参数格式错误");
    }

    @ExceptionHandler(NestedServletException.class)
    public Object handleNestedServletException(NestedServletException e) {
        log.error("参数异常 -> NestedServletException，{}", e.getMessage());
        log.debug("", e);
        return processResponse(400, 400, "请求参数异常");
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public Object handViolationException(ConstraintViolationException e) {
        log.error("请求参数异常 -> ConstraintViolationException, {}", e.getMessage());

        return processResponse( HttpStatus.OK.value(), HttpStatus.BAD_REQUEST.value(),
                e.getConstraintViolations().stream().map(ConstraintViolation::getMessage).distinct().collect(Collectors.joining("|"))
                );
    }

    @ExceptionHandler(Exception.class)
    public Object handleRuntimeException(Exception e) {
        log.error("其他异常 uri : {} -> ", WebUtils.getRequest().getRequestURI(), e);
        return processResponse(500, 500, "服务器内部异常");
    }

    private Object processResponse(int status, int code, String msg){
        // 1.标记响应异常已处理（避免重复处理）
        WebUtils.setResponseHeader(Constant.BODY_PROCESSED_MARK_HEADER, "true");
        // 2.如果是网关请求，http状态码修改为200返回，前端基于业务状态码code来判断状态
        // 如果是微服务请求，http状态码基于异常原样返回，微服务自己做fallback处理

        return WebUtils.isGatewayRequest() ?
                R.error(code, msg).requestId(MDC.get(Constant.REQUEST_ID_HEADER))
                : ResponseEntity.status(status).body(msg);
    }
}
