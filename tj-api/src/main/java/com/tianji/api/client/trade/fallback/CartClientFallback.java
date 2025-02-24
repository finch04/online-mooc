package com.tianji.api.client.trade.fallback;

import com.tianji.api.client.trade.CartClient;
import com.tianji.api.dto.trade.CartsAddDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class CartClientFallback implements FallbackFactory<CartClient> {

    @Override
    public CartClient create(Throwable cause) {
        log.error("购物车服务异常", cause);
        return new CartClient() {
            @Override
            public void addCourse2Cart(CartsAddDTO cartsAddDTO) {
                log.warn("购物车服务异常，将使用默认值");
            }
        };
    }
}
