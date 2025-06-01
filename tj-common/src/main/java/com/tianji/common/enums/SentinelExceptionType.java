package com.tianji.common.enums;

/**
 * sentinel异常类型
 */
public enum SentinelExceptionType implements BaseEnum{

    SENTINEL_FLOW(2046, "您访问过快-限流中"),
    SENTINEL_DEGRADE(2047, "您访问过快-降级中"),
    SENTINEL_PARAMAS(2048, "热点参数限流"),
    SENTINEL_SYSTEM(2049, "系统规则（负载/...不满足要求）"),
    SENTINEL_AUTHORITY(2050, "授权规则不通过");

    private int code;
    private String message;

    SentinelExceptionType(int code, String message) {
        this.code = code;
        this.message = message;
    }

    @Override
    public int getValue() {
        return code;
    }

    @Override
    public String getDesc() {
        return message;
    }
}
