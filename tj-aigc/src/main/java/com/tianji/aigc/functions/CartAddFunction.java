package com.tianji.aigc.functions;

import com.tianji.api.client.trade.CartClient;
import com.tianji.api.dto.trade.CartsAddDTO;
import com.tianji.common.utils.UserContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.function.Function;

@Component
@RequiredArgsConstructor
public class CartAddFunction implements Function<Request.AddCourseToCart, Response.AddCourseToCartResult> {

    private final CartClient cartClient;

    /**
     * 将课程添加到购物车的实现方法
     * 此方法覆盖自接口，用于处理将特定课程添加到用户购物车的请求
     * 它通过调用购物车客户端的服务来实现
     *
     * @param addCourseToCart 包含要添加到购物车的课程信息的请求对象
     * @return 返回添加课程到购物车的结果，成功或失败
     */
    @Override
    public Response.AddCourseToCartResult apply(Request.AddCourseToCart addCourseToCart) {
        try {
            // 设置用户ID，用于身份验证，否在在Feign调用时会出现401错误
            UserContext.setUser(addCourseToCart.userId());
            // 构建添加课程到购物车所需的数据对象，并调用客户端方法执行添加操作
            this.cartClient.addCourse2Cart(CartsAddDTO.builder().courseId(addCourseToCart.id()).build());
            // 如果操作成功，返回成功结果
            return Response.AddCourseToCartResult.ok();
        } catch (Exception e) {
            // 如果操作过程中发生任何异常，返回错误结果
            return Response.AddCourseToCartResult.error();
        }
    }
}
