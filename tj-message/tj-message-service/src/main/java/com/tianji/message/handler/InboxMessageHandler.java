package com.tianji.message.handler;

import com.tianji.api.dto.sms.SmsInfoDTO;
import com.tianji.common.constants.MqConstants;
import com.tianji.message.service.ISmsService;
import com.tianji.message.service.IUserInboxService;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class InboxMessageHandler {

    private final IUserInboxService userInboxService;

//    @RabbitListener(bindings = @QueueBinding(
//            value = @Queue(name = "sms.message.queue", durable = "true"),
//            exchange = @Exchange(MqConstants.Exchange.SMS_EXCHANGE),
//            key = MqConstants.Key.SMS_MESSAGE
//    ))
//    public void listenSmsMessage(SmsInfoDTO smsInfoDTO){
//        smsService.sendMessage(smsInfoDTO);
//    }
}
