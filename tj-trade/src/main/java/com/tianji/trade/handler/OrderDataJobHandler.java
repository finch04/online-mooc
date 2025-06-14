package com.tianji.trade.handler;/**
 * @author fsq
 * @date 2025/5/25 10:03
 */

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.tianji.api.dto.data.OrderDataVO;
import com.tianji.api.dto.trade.OrderAnalysisDTO;
import com.tianji.api.dto.trade.OrderDetailAnalysisDTO;
import com.tianji.common.autoconfigure.mq.RabbitMqHelper;
import com.tianji.common.constants.MqConstants;
import com.tianji.common.utils.BeanUtils;
import com.tianji.common.utils.StringUtils;
import com.tianji.trade.domain.po.Order;
import com.tianji.trade.domain.po.OrderDetail;
import com.tianji.trade.service.IOrderDetailService;
import com.tianji.trade.service.IOrderService;
import com.tianji.trade.service.IPayService;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @Author: fsq
 * @Date: 2025/5/25 10:03
 * @Version: 1.0
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OrderDataJobHandler {
    private final IOrderService orderService;
    private final IOrderDetailService  orderDetailService;
    private final IPayService payService;
    private final StringRedisTemplate redisTemplate;
    private final RabbitMqHelper mqHelper;

    @XxlJob("orderDataJobHandler")
    public void handleOrderData() {
        // 查询所有未删除的订单
        LambdaQueryWrapper<Order> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Order::getDeleted, 0); // 0表示未删除
        List<Order> list = orderService.list(queryWrapper);

        // 初始化统计结果对象
        OrderDataVO orderDataVO = new OrderDataVO();

        // 定义常量：分转万元的转换因子
        final double FEN_TO_WAN = 10000.0 * 100;

        // 初始化各项统计金额
        double allOrderAmount = 0.0;     // 累计订单金额
        double noPayAmount = 0.0;        // 待支付金额
        double closeAmount = 0.0;        // 已关闭金额
        double refundAmount = 0.0;       // 已退款金额
        double realAmount = 0.0;         // 实收金额

        // 遍历订单列表进行统计
        for (Order order : list) {
            Integer totalAmount = order.getTotalAmount();
            Integer realAmountInFen = order.getRealAmount();

            // 跳过金额为null的订单，避免NPE
            if (totalAmount == null) {
                continue;
            }

            // 累计订单总金额（单位：万元）
            allOrderAmount += totalAmount / FEN_TO_WAN;

            // 根据订单状态统计不同类别的金额
            Integer status = order.getStatus();
            if (status != null) {
                switch (status) {
                    case 1:  // 待支付
                        noPayAmount += totalAmount / FEN_TO_WAN;
                        break;
                    case 3:  // 已关闭
                        closeAmount += totalAmount / FEN_TO_WAN;
                        break;
                    case 6:  // 已申请退款
                        if (realAmountInFen != null) {
                            refundAmount += realAmountInFen / FEN_TO_WAN;
                        }
                        break;
                }
            }

            // 累计实收金额
            if (realAmountInFen != null) {
                realAmount += realAmountInFen / FEN_TO_WAN;
            }
        }

        // 四舍五入保留两位小数
        orderDataVO.setAllOrderAmount(round(allOrderAmount, 2));
        orderDataVO.setNoPayAmount(round(noPayAmount, 2));
        orderDataVO.setCloseAmount(round(closeAmount, 2));
        orderDataVO.setRefundAmount(round(refundAmount, 2));
        orderDataVO.setRealAmount(round(realAmount, 2));

        // 发往数据微服务
        mqHelper.send(MqConstants.Exchange.DATA_EXCHANGE,
                MqConstants.Key.DATA_ORDER_KEY, orderDataVO);

        log.info("订单数据统计任务完成，共处理 {} 条数据", list.size());
        log.info("统计结果：累计订单金额={}万元，待支付金额={}万元，已关闭金额={}万元，已退款金额={}万元，实收金额={}万元",
                orderDataVO.getAllOrderAmount(),
                orderDataVO.getNoPayAmount(),
                orderDataVO.getCloseAmount(),
                orderDataVO.getRefundAmount(),
                orderDataVO.getRealAmount());
    }

    // 辅助方法：四舍五入保留指定位数小数
    private double round(double value, int precision) {
        if (precision < 0) {
            throw new IllegalArgumentException("精度不能为负数");
        }
        long factor = (long) Math.pow(10, precision);
        return (double) Math.round(value * factor) / factor;
    }

}
