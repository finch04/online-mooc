package com.tianji.live.config;

import com.tianji.live.utils.IMCacheKeyBuilder;
import jakarta.annotation.Resource;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.util.Map;

/**
 * @Author: fsq
 * @Date: 2025/10/3 16:27
 * @Version: 1.0
 */
@Component
public class WebSocketFilter implements Filter {
    private Logger logger = LoggerFactory.getLogger(WebSocketFilter.class);

    @Resource
    private IMCacheKeyBuilder imCacheKeyBuilder;

    @Resource
    private RedisTemplate<String,Object> redisTemplate;
    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest httpServletRequest = (HttpServletRequest) servletRequest;
        HttpServletResponse httpServletResponse = (HttpServletResponse) servletResponse;
        //解析websocket的自定义协议
        String token = httpServletRequest.getHeader("Sec-WebSocket-Protocol");
        System.out.println("token:"+token);
        //解析websocket中?传递的参数
//        Map<String, String[]> parameterMap = httpServletRequest.getParameterMap();
//        System.out.println("=====parameterMap=======");
//        parameterMap.forEach((k,v)->{
//            System.out.println(k+":"+v[0]);
//        });
//        System.out.println("=====parameterMap=======");

        if(StringUtils.hasText(token)){
            String imTokenKey = imCacheKeyBuilder.buildIMTokenCacheKey(token);
            Object tokenCache = redisTemplate.opsForValue().get(imTokenKey);
            if(null == tokenCache){
                throw new RuntimeException("Token请求缓存失效，请先从API模块获取正确请求参数");
            }
            //设置自定义协议后，需要将协议往请求端传递，否则websocket协议会报错，无法建立websocket连接
            httpServletResponse.setHeader("Sec-WebSocket-Protocol",token);
            filterChain.doFilter(servletRequest,httpServletResponse);
        }else{
            filterChain.doFilter(servletRequest,servletResponse);
        }
    }
}
