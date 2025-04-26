package com.tianji.common.utils;

public class TokenContext {
    private static final ThreadLocal<String> TL = new ThreadLocal<>();

    /**
     * 保存token信息
     */
    public static void setToken(String token){
        TL.set(token);
    }

    /**
     * 获取token
     */
    public static String getToken(){
        return TL.get();
    }

    /**
     * 移除用户信息
     */
    public static void removeToken(){
        TL.remove();
    }
}
