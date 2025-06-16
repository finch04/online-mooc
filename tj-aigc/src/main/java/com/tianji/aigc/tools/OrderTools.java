package com.tianji.aigc.tools;

import cn.hutool.core.collection.CollStreamUtil;
import cn.hutool.core.convert.Convert;
import cn.hutool.core.util.StrUtil;
import com.tianji.aigc.config.ToolResultHolder;
import com.tianji.aigc.constants.Constant;
import com.tianji.aigc.tools.result.PrePlaceOrder;
import com.tianji.api.client.trade.TradeClient;
import com.tianji.api.dto.trade.OrderConfirmVO;
import com.tianji.common.utils.UserContext;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import okio.Options;
import org.springframework.ai.chat.model.ToolContext;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@Component
public class OrderTools {

    private final TradeClient tradeClient;

    @Tool(description = Constant.Tools.PRE_PLACE_ORDER)
    public PrePlaceOrder prePlaceOrder(@ToolParam(description = Constant.ToolParams.COURSE_IDS)List<Number> ids, ToolContext toolContext){
        //获取从service中传过来的用户id
        Long userId = Convert.toLong(toolContext.getContext().get(Constant.USER_ID));
        UserContext.setUser(userId);
         // 大模型传入的ids，可能是int类型，所以转化为long类型，再调用Feign
        var orderConfirmVO = this.tradeClient.prePlaceOrder(CollStreamUtil.toList(ids, Number::longValue));
        return Optional.ofNullable(orderConfirmVO)
                .map(PrePlaceOrder::of)
                .map( prePlaceOrder -> {
                        var field = StrUtil.lowerFirst(prePlaceOrder.getClass().getSimpleName());
                        var requestId = Convert.toStr(toolContext.getContext().get(Constant.REQUEST_ID));
                        ToolResultHolder.put(requestId,field,prePlaceOrder);
                        return prePlaceOrder;
                        })
                .orElse(null);
    }
}
