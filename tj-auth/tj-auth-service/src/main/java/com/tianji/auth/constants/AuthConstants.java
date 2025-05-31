package com.tianji.auth.constants;

import java.time.Duration;

public abstract class AuthConstants {
    /*管理员的角色ID*/
    public static final Long ADMIN_ROLE_ID = 1L;

    // 验证码的Redis key前缀
    public static final String USER_VERIFY_CODE_KEY = "sms:user:code:phone:";
    // 验证码有效期，5分钟
    public static final Duration USER_VERIFY_CODE_TTL = Duration.ofMinutes(5);
}
