package com.tianji.aigc.functions;

import cn.hutool.core.convert.Convert;
import com.tianji.aigc.constants.Constant;
import com.tianji.api.client.trade.TradeClient;
import com.tianji.api.dto.trade.OrderConfirmVO;
import com.tianji.common.utils.UserContext;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.model.ToolContext;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;

@Component
@RequiredArgsConstructor
public class PrePlaceOrderFunction implements BiFunction<Request.PrePlaceOrder, ToolContext, Response.PrePlaceOrderResult> {

    private final TradeClient tradeClient;

    @Override
    public Response.PrePlaceOrderResult apply(Request.PrePlaceOrder prePlaceOrder, ToolContext toolContext) {
        // 设置用户ID，用于身份验证，否在在Feign调用时会出现401错误
        UserContext.setUser(Convert.toLong(toolContext.getContext().get(Constant.USER_ID)));
        OrderConfirmVO orderConfirmVO = this.tradeClient.prePlaceOrder(Arrays.asList(prePlaceOrder.ids()));
        return Optional.ofNullable(orderConfirmVO)
                .map(Response.PrePlaceOrderResult::of)
                .orElse(null);
    }

}
