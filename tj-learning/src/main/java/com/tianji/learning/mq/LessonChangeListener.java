package com.tianji.learning.mq;


import com.tianji.api.dto.trade.OrderBasicDTO;
import com.tianji.common.constants.MqConstants;
import com.tianji.common.utils.CollUtils;
import com.tianji.learning.service.ILearningLessonService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor//使用构造器 Lombok是在编译期生成相应的方法
public class LessonChangeListener {

    //spring在此处不建议使用@Autowired,可以使用构造器注入（配合构造器注解）
    private final ILearningLessonService lessonService;

    @RabbitListener(bindings = @QueueBinding(value = @Queue(value="learning.lesson.pay.queue",durable = "true"),
            exchange = @Exchange(name= MqConstants.Exchange.ORDER_EXCHANGE,type = ExchangeTypes.TOPIC),
            key =MqConstants.Key.ORDER_PAY_KEY ))
    public void onMsg(OrderBasicDTO dto){
        log.info("LessonChangeListener接收到了消息,用户：{}，课程：{}",dto.getUserId(),dto.getCourseIds());
        if(dto.getUserId()==null
                ||dto.getOrderId()==null
                || CollUtils.isEmpty(dto.getCourseIds())
        ){
            log.error("接收到MQ消息有误，订单数据为空");
            //此处不能抛异常，否则MQ会不停的重试。
            return;
        }
        //调用service，保存课程到课表
        lessonService.addUserLesson(dto.getUserId(),dto.getCourseIds());
    }
}
