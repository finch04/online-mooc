package com.tianji.message.handler;

import com.tianji.api.dto.sms.SmsInfoDTO;
import com.tianji.common.constants.MqConstants;
import com.tianji.message.service.ISmsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.MessageModel;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@Slf4j
@RequiredArgsConstructor
@RocketMQMessageListener(topic = MqConstants.Topic.MESSAGE_TOPIC,
        consumerGroup = MqConstants.ConsumerGroup.MESSAGE_GROUP,
        messageModel = MessageModel.CLUSTERING)
public class SmsListener implements RocketMQListener<SmsInfoDTO> {
    private final ISmsService smsService;

    @Override
    public void onMessage(SmsInfoDTO msg) {
        log.debug("接收发送验证码的消息：{}", msg);
        smsService.sendMessage(msg);
    }
}
