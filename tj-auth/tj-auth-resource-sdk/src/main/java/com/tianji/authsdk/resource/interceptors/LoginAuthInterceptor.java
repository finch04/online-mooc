package com.tianji.authsdk.resource.interceptors;

import com.tianji.common.utils.UserContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.servlet.HandlerInterceptor;


@Slf4j
public class LoginAuthInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(jakarta.servlet.http.HttpServletRequest request, jakarta.servlet.http.HttpServletResponse response, Object handler) throws Exception {
        // 1.尝试获取用户信息
        Long userId = UserContext.getUser();
        // 2.判断是否登录
        if (userId == null) {
            response.setStatus(401);
            response.sendError(401, "未登录用户无法访问！");
            // 2.3.未登录，直接拦截
            return false;
        }
        // 3.登录则放行
        return true;
    }

}
