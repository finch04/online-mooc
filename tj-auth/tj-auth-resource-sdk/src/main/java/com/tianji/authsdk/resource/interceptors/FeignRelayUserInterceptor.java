package com.tianji.authsdk.resource.interceptors;

import com.tianji.auth.common.constants.JwtConstants;
import com.tianji.common.utils.UserContext;
import feign.RequestInterceptor;
import feign.RequestTemplate;

public class FeignRelayUserInterceptor implements RequestInterceptor {
    @Override
    public void apply(RequestTemplate template) {
        Long userId = UserContext.getUser();
        if (userId == null) {
            return;
        }
        //将当前系统的登录用户id 重新放入请求头中
        template.header(JwtConstants.USER_HEADER, userId.toString());
    }
}
