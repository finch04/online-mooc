package com.tianji.data.handler;/**
 * @author fsq
 * @date 2025/6/19 14:39
 */

import com.tianji.api.dto.data.OrderDataVO;
import com.tianji.common.constants.MqConstants;
import com.tianji.common.domain.vo.LogBusinessVO;
import com.tianji.data.influxdb.domain.BusinessLog;
import com.tianji.data.influxdb.service.IBusinessLogService;
import com.tianji.data.utils.BeanConv;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @Author: fsq
 * @Date: 2025/6/19 14:39
 * @Version: 1.0
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class LogHandler {

    @Autowired
    private IBusinessLogService businessLogService;


    // 监听订单数据统计消息
    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(name = "data.log.queue", durable = "true"),
            exchange = @Exchange(name = MqConstants.Exchange.DATA_EXCHANGE, type = ExchangeTypes.TOPIC),
            key = MqConstants.Key.DATA_LOG_KEY
    ))
    public void listenLog(LogBusinessVO vo) {
        try {
            log.info("收到日志数据：{}",vo);
            Boolean businessLog = businessLogService.createBusinessLog(BeanConv.toBean(vo, BusinessLog.class));
            log.info("存储日志数据成功：{}",businessLog);
        } catch (Exception e) {
            log.error("处理日志数据失败", e);
            // 可添加重试或补偿逻辑
        }
    }
}
