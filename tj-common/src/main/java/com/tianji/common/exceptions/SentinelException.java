package com.tianji.common.exceptions;

/**
 * sentinel限流、熔断等规则的异常
 */
public class SentinelException extends CommonException{


    public SentinelException(String message) {
        super(message);
    }
    public SentinelException(int code, String message) {
        super(code, message);
    }

    public SentinelException(int code, String message, Throwable cause) {
        super(code, message, cause);
    }

}
