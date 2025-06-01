package com.tianji.trade.service.impl;

import com.tianji.api.client.promotion.PromotionClient;
import com.tianji.api.dto.trade.OrderBasicDTO;
import com.tianji.common.autoconfigure.mq.RabbitMqHelper;
import com.tianji.common.constants.MqConstants;
import com.tianji.common.utils.CollUtils;
import com.tianji.trade.constants.OrderStatus;
import com.tianji.trade.constants.OrderStatusChangeEvent;
import com.tianji.trade.domain.po.Order;
import com.tianji.trade.mapper.OrderMapper;
import com.tianji.trade.service.IOrderDetailService;
import com.tianji.trade.service.IOrderService;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.statemachine.annotation.OnTransition;
import org.springframework.statemachine.annotation.WithStateMachine;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 订单状态改变处理类
 */
@Component("orderStateListener")
@WithStateMachine(name = "orderStateMachine")
@Slf4j
@RequiredArgsConstructor
public class OrderStateListenerImpl {
    private final OrderMapper orderMapper;
    private final RabbitMqHelper rabbitMqHelper;
    private final PromotionClient promotionClient;
    private final IOrderDetailService detailService;
    private final IOrderService orderService;

    /**
     * 支付成功 状态流转
     * @param message
     */
    @OnTransition(source = "NO_PAY", target = "PAYED")
    @Transactional
    public void payTransition(Message<OrderStatusChangeEvent> message) {
        Order order = (Order) message.getHeaders().get("order");
        log.info("支付，状态机反馈信息：{}",  message.getHeaders().toString());

        // 2.更新订单状态
        Order o = new Order();
        o.setId(order.getId());
        o.setStatus(OrderStatus.PAYED.getValue());
        o.setMessage("用户支付成功");
        orderService.updateById(o);

        // 3.更新订单条目
        detailService.markDetailSuccessByOrderId(order.getId(), order.getPayChannel(), order.getPayTime());
        // 4.查询订单包含的课程信息
        List<Long> cIds = detailService.queryCourseIdsByOrderId(order.getId());
        // 5.发送MQ消息，通知报名成功
        rabbitMqHelper.send(
                MqConstants.Exchange.ORDER_EXCHANGE,
                MqConstants.Key.ORDER_PAY_KEY,
                OrderBasicDTO.builder()
                        .orderId(order.getId()).userId(order.getUserId()).courseIds(cIds)
                        .finishTime(order.getPayTime())
                        .build()
        );
    }

    /**
     * 订单取消 状态流转
     * @param message
     */
    @OnTransition(source = "NO_PAY", target = "CLOSED")
    @GlobalTransactional
    public void closeTransition(Message<OrderStatusChangeEvent> message) {
        Order order = (Order) message.getHeaders().get("order");
        log.info("订单取消，状态机反馈信息：{}",  message.getHeaders().toString());

        Long orderId = order.getId();
        // 4.可以更新订单状态为取消了
        boolean success = orderService.lambdaUpdate()
                .set(Order::getStatus, OrderStatus.CLOSED.getValue())
                .set(Order::getMessage, order.getMessage())
                .set(Order::getCloseTime, LocalDateTime.now())
                .eq(Order::getStatus, OrderStatus.NO_PAY.getValue())  //状态机已经替我们判断了状态
                .eq(Order::getId, orderId)
                .update();
        if (!success) {
            return;
        }
        // 5.更新订单条目的状态
        detailService.updateStatusByOrderId(orderId, OrderStatus.CLOSED.getValue());

        // 6.退还优惠券
        if(CollUtils.isNotEmpty(order.getCouponIds())){
            promotionClient.refundCoupon(order.getCouponIds());
        }
        order.setStatus(OrderStatus.CLOSED.getValue());
        orderMapper.updateById(order);
    }

    /**
     * 订单退款 状态流转
     * @param message
     */
    @OnTransition(source = "PAYED", target = "REFUNDED")
    public void refundTransition(Message<OrderStatusChangeEvent> message) {
        Order order = (Order) message.getHeaders().get("order");
        log.info("申请退款，状态机反馈信息：{}",  message.getHeaders().toString());
        //更新订单
        order.setStatus(OrderStatus.REFUNDED.getValue());
        orderMapper.updateById(order);
        //TODO 其他业务
    }
}
