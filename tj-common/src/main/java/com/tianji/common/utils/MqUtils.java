package com.tianji.common.utils;

/**
 * 消息队列工具类
 */
public class MqUtils {

    public static String topicWithTag(String topic, String tag) {
        return String.format("%s:%s", topic, tag);
    }
}
