package com.tianji.data.handler;

import com.tianji.api.dto.data.CourseDataVO;
import com.tianji.api.dto.data.OrderDataVO;
import com.tianji.api.dto.data.TodoDataVO;
import com.tianji.common.constants.MqConstants;
import com.tianji.data.service.BoardService;
import com.tianji.data.service.TodoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataHandler {

    private final BoardService boardService;
    private final TodoService todoService;

    // 监听订单数据统计消息
    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(name = "data.order.stat.queue", durable = "true"),
            exchange = @Exchange(name = MqConstants.Exchange.DATA_EXCHANGE, type = ExchangeTypes.TOPIC),
            key = MqConstants.Key.ORDER_DATA_KEY
    ))
    public void listenOrderData(OrderDataVO orderData) {
        try {
            log.info("收到订单数据统计消息：{}", orderData);
            boardService.updateOrderData(orderData);
        } catch (Exception e) {
            log.error("处理订单数据统计消息失败", e);
            // 可添加重试或补偿逻辑
        }
    }

    // 监听课程数据统计消息
    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(name = "data.course.stat.queue", durable = "true"),
            exchange = @Exchange(name = MqConstants.Exchange.DATA_EXCHANGE, type = ExchangeTypes.TOPIC),
            key = MqConstants.Key.COURSE_DATA_KEY
    ))
    public void listenCourseData(CourseDataVO courseData) {
        try {
            log.info("收到课程数据统计消息：{}", courseData);
            boardService.updateCourseData(courseData);
        } catch (Exception e) {
            log.error("处理课程数据统计消息失败", e);
            // 可添加重试或补偿逻辑
        }
    }


    // 监听待办数据统计消息
    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(name = "data.todo.queue", durable = "true"),
            exchange = @Exchange(name = MqConstants.Exchange.DATA_EXCHANGE, type = ExchangeTypes.TOPIC),
            key = MqConstants.Key.DATA_TODO_KEY
    ))
    public void listenTodoData(TodoDataVO vo) {
        try {
            log.info("收到待办数据统计消息：{}", vo);
            todoService.updateTodoData(vo);
        } catch (Exception e) {
            log.error("处理待办数据统计消息失败", e);
            // 可添加重试或补偿逻辑
        }
    }

}
