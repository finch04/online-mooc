package com.tianji.aigc.tools;

import cn.hutool.core.convert.Convert;
import com.tianji.aigc.config.ToolResultHandler;
import com.tianji.aigc.constants.Constant;
import com.tianji.aigc.tools.result.CourseInfo;
import com.tianji.aigc.tools.result.PrePlaceOrder;
import com.tianji.api.client.trade.TradeClient;
import com.tianji.api.dto.trade.OrderConfirmVO;
import com.tianji.common.utils.UserContext;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.model.ToolContext;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class OrderTools {

    private final TradeClient tradeClient;

    @Tool(description = Constant.Tools.PRE_PLACE_ORDER)
    public PrePlaceOrder prePlaceOrder(@ToolParam(description = Constant.ToolParams.COURSE_IDS) List<Long> ids,
                                       ToolContext toolContext) {

        // 设置用户ID，用于身份验证，否在在Feign调用时会出现401错误
        UserContext.setUser(Convert.toLong(toolContext.getContext().get(Constant.USER_ID)));
        OrderConfirmVO orderConfirmVO = this.tradeClient.prePlaceOrder(ids);
        PrePlaceOrder prePlaceOrder = Optional.ofNullable(orderConfirmVO)
                .map(PrePlaceOrder::of)
                .orElse(null);

        String requestId = Convert.toStr(toolContext.getContext().get(Constant.REQUEST_ID));
        ToolResultHandler.put(requestId, PrePlaceOrder.class.getSimpleName(), prePlaceOrder);

        return prePlaceOrder;
    }
}
