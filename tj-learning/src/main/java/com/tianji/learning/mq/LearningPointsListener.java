package com.tianji.learning.mq;

import com.tianji.common.constants.MqConstants;
import com.tianji.learning.enums.PointsRecordType;
import com.tianji.learning.mq.msg.SignInMessage;
import com.tianji.learning.service.IPointsRecordService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

/**
 * 消费消息，用于保存积分
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class LearningPointsListener {

    private final IPointsRecordService recordService;

    /**
     * 签到增加的积分
     * @param msg
     */
    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(value = "sign.points.queue",durable = "true"),
            exchange = @Exchange(value = MqConstants.Exchange.LEARNING_EXCHANGE,type = ExchangeTypes.TOPIC),
            key = MqConstants.Key.SIGN_IN
    ))
    public void listenSignInListener(SignInMessage msg){
        log.debug("消费到签到增加的积分  消费到消息：{}",msg);
        recordService.addPointsRecord(msg, PointsRecordType.SIGN);
    }


    /**
     * 问答增加的积分
     * @param msg
     */
    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(value = "qa.points.queue",durable = "true"),
            exchange = @Exchange(value = MqConstants.Exchange.LEARNING_EXCHANGE,type = ExchangeTypes.TOPIC),
            key = MqConstants.Key.WRITE_REPLY
    ))
    public void listenReplyListener(SignInMessage msg){
        log.debug("消费到问答增加的积分  消费到消息：{}",msg);
        recordService.addPointsRecord(msg, PointsRecordType.QA);
    }



    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(name = "learning.points.queue", durable = "true"),
            exchange = @Exchange(name = MqConstants.Exchange.LEARNING_EXCHANGE, type = ExchangeTypes.TOPIC),
            key = MqConstants.Key.LEARN_SECTION
    ))
    public void listenLearnSectionMessage(SignInMessage msg){
        log.debug("消费到学习增加的积分  消费到消息：{}",msg);
        recordService.addPointsRecord(msg,  PointsRecordType.LEARNING);
    }

//    @RabbitListener(bindings = @QueueBinding(
//            value = @Queue(name = "note.new.points.queue", durable = "true"),
//            exchange = @Exchange(name = MqConstants.Exchange.LEARNING_EXCHANGE, type = ExchangeTypes.TOPIC),
//            key = MqConstants.Key.WRITE_NOTE
//    ))
//    public void listenWriteNodeMessage(Long userId){
//        recordService.addPointsRecord(userId, 3, PointsRecordType.NOTE);
//    }
//
//    @RabbitListener(bindings = @QueueBinding(
//            value = @Queue(name = "note.gathered.points.queue", durable = "true"),
//            exchange = @Exchange(name = MqConstants.Exchange.LEARNING_EXCHANGE, type = ExchangeTypes.TOPIC),
//            key = MqConstants.Key.NOTE_GATHERED
//    ))
//    public void listenNodeGatheredMessage(Long userId){
//        recordService.addPointsRecord(userId, 2, PointsRecordType.NOTE);
//    }

}
