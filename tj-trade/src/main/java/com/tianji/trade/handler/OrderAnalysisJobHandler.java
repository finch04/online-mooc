package com.tianji.trade.handler;/**
 * @author fsq
 * @date 2025/5/25 10:03
 */

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
public class OrderAnalysisJobHandler {
    private final IOrderService orderService;
    private final IOrderDetailService  orderDetailService;
    private final IPayService payService;
    private final StringRedisTemplate redisTemplate;
    private final RabbitMqHelper mqHelper;

    @XxlJob("orderAnalysisJobHandler")
    public void handleOrderAnalysis() {
        String jobParam = XxlJobHelper.getJobParam();
        //参数写法：start=2022-08-01&end=2025-05-26
        log.info("开始处理订单分析任务，参数：{}", jobParam);
        LocalDateTime startTime;
        LocalDateTime endTime;

        // 解析参数，无参数时使用默认区间（昨天 00:00:00 至昨天 23:59:59）
        if (StringUtils.isBlank(jobParam)) {
            startTime = LocalDateTime.now().minusDays(1).withHour(0).withMinute(0).withSecond(0).withNano(0);
            endTime = LocalDateTime.now().minusDays(1).withHour(23).withMinute(59).withSecond(59).withNano(999999999);
        } else {
            // 按参数格式解析（示例格式：start=2023-10-01&end=2023-10-02）
            Map<String, String> paramMap = Arrays.stream(jobParam.split("&"))
                    .map(pair -> pair.split("="))
                    .collect(Collectors.toMap(arr -> arr[0], arr -> arr[1], (k1, k2) -> k2));

            String startStr = paramMap.get("start");
            String endStr = paramMap.get("end");

            // 校验日期格式（示例：yyyy-MM-dd）
            if (StringUtils.isBlank(startStr) || StringUtils.isBlank(endStr)) {
                log.error("参数格式错误，需包含start和end参数（格式：start=yyyy-MM-dd&end=yyyy-MM-dd）");
                return;
            }

            try {
                startTime = LocalDateTime.parse(startStr + "T00:00:00");
                endTime = LocalDateTime.parse(endStr + "T23:59:59");
            } catch (DateTimeParseException e) {
                log.error("日期解析失败，参数：{}", jobParam, e);
                return;
            }
        }

        //查出昨天的数据订单
        List<Order> orders = orderService.queryOrderBetweenTime(startTime, endTime);
        //TODO 可能存入redis便于数据微服务调用
        List<OrderAnalysisDTO> analysisDTOList = BeanUtils.copyList(orders, OrderAnalysisDTO.class);
        for(Order order:orders){
            OrderAnalysisDTO orderAnalysisDTO = BeanUtils.copyBean(order, OrderAnalysisDTO.class);
            analysisDTOList.add(orderAnalysisDTO);
        }
        //发往搜索微服务
        mqHelper.send(MqConstants.Exchange.ORDER_EXCHANGE,  MqConstants.Key.ORDER_ANALYSIS_KEY, analysisDTOList);
        log.info("订单分析任务检索完成，共处理 {} 条数据", orders.size());
    }

    @XxlJob("orderDetailAnalysisJobHandler")
    public void handleOrderDetailAnalysis() {
        String jobParam = XxlJobHelper.getJobParam();
        //参数写法：start=2023-10-01&end=2023-10-02
        log.info("开始处理订单明细分析任务，参数：{}", jobParam);
        LocalDateTime startTime;
        LocalDateTime endTime;

        // 解析参数，无参数时使用默认区间（昨天 00:00:00 至昨天 23:59:59）
        if (StringUtils.isBlank(jobParam)) {
            startTime = LocalDateTime.now().minusDays(1).withHour(0).withMinute(0).withSecond(0).withNano(0);
            endTime = LocalDateTime.now().minusDays(1).withHour(23).withMinute(59).withSecond(59).withNano(999999999);
        } else {
            // 按参数格式解析（示例格式：start=2023-10-01&end=2023-10-02）
            Map<String, String> paramMap = Arrays.stream(jobParam.split("&"))
                    .map(pair -> pair.split("="))
                    .collect(Collectors.toMap(arr -> arr[0], arr -> arr[1], (k1, k2) -> k2));

            String startStr = paramMap.get("start");
            String endStr = paramMap.get("end");

            // 校验日期格式（示例：yyyy-MM-dd）
            if (StringUtils.isBlank(startStr) || StringUtils.isBlank(endStr)) {
                log.error("参数格式错误，需包含start和end参数（格式：start=yyyy-MM-dd&end=yyyy-MM-dd）");
                return;
            }

            try {
                startTime = LocalDateTime.parse(startStr + "T00:00:00");
                endTime = LocalDateTime.parse(endStr + "T23:59:59");
            } catch (DateTimeParseException e) {
                log.error("日期解析失败，参数：{}", jobParam, e);
                return;
            }
        }
        //查出昨天的数据订单
        List<OrderDetail> orderDetails = orderDetailService.queryOrderDetailBetweenTime(startTime, endTime);
        //TODO 可能存入redis便于数据微服务调用
        List<OrderDetailAnalysisDTO> analysisDTOList = BeanUtils.copyList(orderDetails, OrderDetailAnalysisDTO.class);
        for(OrderDetail orderDetail:orderDetails){
            OrderDetailAnalysisDTO orderDetailAnalysisDTO = BeanUtils.copyBean(orderDetail, OrderDetailAnalysisDTO.class);
            analysisDTOList.add(orderDetailAnalysisDTO);
        }
        //发往搜索微服务
        mqHelper.send(MqConstants.Exchange.ORDER_EXCHANGE,  MqConstants.Key.ORDER_DETAIL_ANALYSIS_KEY, analysisDTOList);
        log.info("订单明细分析任务检索完成，共处理 {} 条数据", orderDetails.size());
    }
}
