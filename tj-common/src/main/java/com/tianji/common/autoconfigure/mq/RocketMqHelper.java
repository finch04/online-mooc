//package com.tianji.common.autoconfigure.mq;
//
//import com.tianji.common.utils.MqUtils;
//import groovy.util.logging.Slf4j;
//import org.apache.rocketmq.client.exception.MQBrokerException;
//import org.apache.rocketmq.client.exception.MQClientException;
//import org.apache.rocketmq.client.producer.SendResult;
//import org.apache.rocketmq.client.producer.SendStatus;
//import org.apache.rocketmq.remoting.exception.RemotingException;
//import org.apache.rocketmq.spring.core.RocketMQLocalTransactionListener;
//import org.apache.rocketmq.spring.core.RocketMQTemplate;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.springframework.messaging.support.MessageBuilder;
//import org.springframework.transaction.support.TransactionCallback;
//
//@Slf4j
//public class RocketMqHelper {
//
//    private static final Logger log = LoggerFactory.getLogger(RocketMqHelper.class);
//    private final RocketMQTemplate rocketMQTemplate;
//
//
//    public RocketMqHelper(RocketMQTemplate rocketMQTemplate) {
//        this.rocketMQTemplate = rocketMQTemplate;
//    }
//
//    //根据topic发送消息（同步）
//    public <T> boolean sendSync(String topic, T msg) {
//        try {
//            SendResult result = rocketMQTemplate.syncSend(topic, msg);
//            return SendStatus.SEND_OK.equals(result.getSendStatus());
//        } catch (Exception e) {
//            log.error("发送消息失败", e);
//            return false;
//        }
//    }
//
//    //根据topic:tags发送消息（同步）
//    public <T> boolean sendSync(String topic, String tags, T msg) {
//        try {
//            SendResult result = rocketMQTemplate.syncSend(MqUtils.topicWithTag(topic, tags), msg);
//            return SendStatus.SEND_OK.equals(result.getSendStatus());
//        } catch (Exception e) {
//            log.error("发送消息失败", e);
//            return false;
//        }
//    }
//
//    /**
//     * 根据topic:tags发送事务消息（同步）
//     * @param topic 主题
//     * @param tags 标签
//     * @param msg 消息内容
//     * @param <T> 消息类型
//     * @return 发送结果
//     */
//    public <T> boolean sendTransactionSync(String topic, String tags, T msg) {
//        try {
//            // 构建完整的destination
//            String destination = MqUtils.topicWithTag(topic, tags);
//            // 发送事务消息
//            SendResult result = rocketMQTemplate.sendMessageInTransaction(
//                    destination,
//                    MessageBuilder.withPayload(msg).build(),
//                    null
//            );
//            // 检查发送状态
//            if (result != null && SendStatus.SEND_OK.equals(result.getSendStatus())) {
//                log.info("事务消息发送成功，destination={}, msgId={}", destination, result.getMsgId());
//                return true;
//            } else {
//                log.warn("事务消息发送状态非OK，destination={}, sendStatus={}", destination, result != null ? result.getSendStatus() : "null");
//                return false;
//            }
//        } catch (Exception e) {
//            log.error("发送事务消息时发生异常，topic={}, tags={}", topic, tags, e);
//            return false;
//        }
//    }
//
//    /**
//     * 根据topic:tags发送延迟消息（同步）
//     * @param topic 主题
//     * @param tags 标签
//     * @param msg 消息内容
//     * @param delayLevel 延时消息一共分为18个等级分别为：1s 5s 10s 30s 1m 2m 3m 4m 5m 6m 7m 8m 9m 10m 20m 30m 1h 2h
//     * @param <T> 消息类型
//     * @return 发送结果
//     */
//    public <T> boolean sendDelaySync(String topic, String tags, T msg, int delayLevel) {
//        if (delayLevel <= 0) {
//            log.error("延迟级别必须大于0，无法发送延迟消息");
//            return false;
//        }
//
//        try {
//            // 构建完整的destination
//            String destination = MqUtils.topicWithTag(topic, tags);
//
//            // 创建消息
//            org.springframework.messaging.Message<T> message = MessageBuilder.withPayload(msg).build();
//
//            // 发送延迟消息，设置延迟级别
//            /**
//             * 3000 是超时时间，设置为 3000 毫秒（即 3 秒）。这意味着：
//             * 生产者发送消息后会等待最多 3 秒
//             * 如果 3 秒内没有收到 Broker 的响应，发送操作将失败
//             * 这可以防止生产者长时间阻塞在发送操作上
//             *
//             * 注意！不是延迟时间！
//             */
//            SendResult result = rocketMQTemplate.syncSend(destination, message, 3000, delayLevel);
//
//            // 检查发送状态
//            if (result != null && SendStatus.SEND_OK.equals(result.getSendStatus())) {
//                log.info("延迟消息发送成功，destination={}, delayLevel={}, msgId={}",
//                        destination, delayLevel, result.getMsgId());
//                return true;
//            } else {
//                log.warn("延迟消息发送状态非OK，destination={}, delayLevel={}, sendStatus={}",
//                        destination, delayLevel, result != null ? result.getSendStatus() : "null");
//                return false;
//            }
//        } catch (Exception e) {
//            log.error("发送延迟消息时发生异常，topic={}, tags={}, delayLevel={}",
//                    topic, tags, delayLevel, e);
//            return false;
//        }
//    }
//}
