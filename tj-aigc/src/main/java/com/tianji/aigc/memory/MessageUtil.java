package com.tianji.aigc.memory;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.convert.Convert;
import cn.hutool.json.JSONUtil;
import com.tianji.aigc.config.ToolResultHolder;
import com.tianji.aigc.constants.Constant;
import org.springframework.ai.chat.messages.*;

/**
 * 消息转换工具类，提供消息对象与JSON字符串之间的转换功能，主要用于Redis存储格式转换
 */
public class MessageUtil {

    /**
     * 将Message对象转换为Redis存储格式的JSON字符串
     *
     * @param message 需要转换的原始消息对象
     * @return 符合Redis存储规范的JSON字符串
     */
    public static String toJson(Message message) {
        var myMessage = BeanUtil.toBean(message, MyMessage.class);
        // 设置消息内容
        myMessage.setTextContent(message.getText());
        if (message instanceof AssistantMessage assistantMessage) {
            myMessage.setToolCalls(assistantMessage.getToolCalls());
            //问题，如何获取Tool执行过后的结果对象呢？
            //思路：请求id是我们自己生成的，SpringAI框架是不会传递这个数据的，这里只能获取到消息id
            //如果我们把消息id与请求id关联，可以通过消息id找到请求id,问题就解决了
            var messageId = Convert.toStr(message.getMetadata().get(Constant.ID));
            var requestId = Convert.toStr( ToolResultHolder.get(messageId, Constant.REQUEST_ID));
            var params = ToolResultHolder.get(requestId);
            if (CollUtil.isNotEmpty(params)){
                myMessage.setParams(params);
            }
            ToolResultHolder.remove(messageId);
        }

        if (message instanceof ToolResponseMessage toolResponseMessage) {
            myMessage.setToolResponses(toolResponseMessage.getResponses());
        }

        return JSONUtil.toJsonStr(myMessage);
    }

    /**
     * 将Redis存储的JSON字符串反序列化为对应的Message对象
     *
     * @param json Redis存储的JSON格式消息数据
     * @return 对应类型的Message对象
     * @throws RuntimeException 当无法识别的消息类型时抛出异常
     */
    public static Message toMessage(String json) {
        var myMessage = JSONUtil.toBean(json, MyMessage.class);
        var messageType = MessageType.valueOf(myMessage.getMessageType());
        switch (messageType) {
            case SYSTEM -> {
                return new SystemMessage(myMessage.getTextContent());
            }
            case USER -> {
                return new UserMessage(myMessage.getTextContent(), myMessage.getMedia(), myMessage.getMetadata());
            }
            case ASSISTANT -> {
//                return new AssistantMessage(myMessage.getTextContent(), myMessage.getMetadata(), myMessage.getToolCalls());
                return new MyAssistantMessage(myMessage.getTextContent(), myMessage.getMetadata(), myMessage.getToolCalls(), myMessage.getMedia(), myMessage.getParams());
            }
            case TOOL -> {
                return new ToolResponseMessage(myMessage.getToolResponses(), myMessage.getMetadata());
            }
        }

        throw new RuntimeException("Message data conversion failed.");
    }

}
