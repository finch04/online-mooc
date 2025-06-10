package com.tianji.common.autoconfigure.mq;

import com.tianji.common.utils.MqUtils;
import groovy.util.logging.Slf4j;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.client.producer.SendStatus;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Slf4j
public class RocketMqHelper {

    private static final Logger log = LoggerFactory.getLogger(RocketMqHelper.class);
    private final RocketMQTemplate rocketMQTemplate;


    public RocketMqHelper(RocketMQTemplate rocketMQTemplate) {
        this.rocketMQTemplate = rocketMQTemplate;
    }

    //根据topic发送消息（同步）
    public <T> boolean sendSync(String topic, T msg) {
        try {
            SendResult result = rocketMQTemplate.syncSend(topic, msg);
            return SendStatus.SEND_OK.equals(result.getSendStatus());
        } catch (Exception e) {
            log.error("发送消息失败", e);
            return false;
        }
    }

    //根据topic:tags发送消息（同步）
    public <T> boolean sendSync(String topic, String tags, T msg) {
        try {
            SendResult result = rocketMQTemplate.syncSend(MqUtils.topicWithTag(topic, tags), msg);
            return SendStatus.SEND_OK.equals(result.getSendStatus());
        } catch (Exception e) {
            log.error("发送消息失败", e);
            return false;
        }
    }
}
