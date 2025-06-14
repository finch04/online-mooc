package com.tianji.trade.handler;/**
 * @author fsq
 * @date 2025/5/25 10:03
 */

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.tianji.api.dto.data.OrderDataVO;
import com.tianji.api.dto.data.TodoDataVO;
import com.tianji.common.autoconfigure.mq.RabbitMqHelper;
import com.tianji.common.constants.MqConstants;
import com.tianji.trade.constants.RefundStatus;
import com.tianji.trade.domain.po.Order;
import com.tianji.trade.domain.po.RefundApply;
import com.tianji.trade.service.IOrderDetailService;
import com.tianji.trade.service.IOrderService;
import com.tianji.trade.service.IPayService;
import com.tianji.trade.service.IRefundApplyService;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @Author: fsq
 * @Date: 2025/5/25 10:03
 * @Version: 1.0
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RefundDataJobHandler {
    private final IRefundApplyService refundApplyService;
    private final RabbitMqHelper mqHelper;

    @XxlJob("refundDataJobHandler")
    public void handleRefundTodoData() {
        Integer count = refundApplyService.lambdaQuery()
                .eq(RefundApply::getStatus, RefundStatus.UN_APPROVE.getValue()).count();
        TodoDataVO todoDataVO = new TodoDataVO();
        todoDataVO.setTodoRefundNum(count);
        //发送给数据微服务
        mqHelper.send(MqConstants.Exchange.DATA_EXCHANGE, MqConstants.Key.DATA_TODO_KEY, todoDataVO);
    }

}
