package com.tianji.live.mq;

import com.tianji.live.protocol.GenericMessage;
import com.tianji.live.service.ChatBusiService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

import static com.tianji.common.constants.MqConstants.Exchange.LIVE_EXCHANGE;
import static com.tianji.common.constants.MqConstants.Key.LIVE_IM_MESSAGE;

@Slf4j
@Component
@RequiredArgsConstructor
public class ChatMessageListener {

    private final ChatBusiService chatBusiService;

    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(name = "live.im.queue", durable = "true"),
            exchange = @Exchange(name = LIVE_EXCHANGE, type = ExchangeTypes.TOPIC),
            key = LIVE_IM_MESSAGE
    ))
    public void listenChatMessage(GenericMessage genericMessage){
        log.info("接收到消息：{}",genericMessage);
        chatBusiService.handleMessage(genericMessage);
    }
}
