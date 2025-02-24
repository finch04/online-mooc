package com.tianji.api.client.trade;

import com.tianji.api.client.trade.fallback.CartClientFallback;
import com.tianji.api.dto.trade.CartsAddDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(value = "trade-service", contextId = "cart", path = "carts", fallbackFactory = CartClientFallback.class)
public interface CartClient {

    /**
     * 添加课程到购物车
     */
    @PostMapping
    void addCourse2Cart(@RequestBody CartsAddDTO cartsAddDTO);
}
