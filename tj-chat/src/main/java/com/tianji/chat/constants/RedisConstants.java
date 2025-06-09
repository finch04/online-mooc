package com.tianji.chat.constants;

public interface RedisConstants {

    /**
     * 记录聊天历史Key的前缀
     */
    String CHAT_MEMORY_KEY_PREFIX = "chat:memory:";

    /**
     * 聊天消息-延迟队列-延迟落库
     */
    String CHAT_DELAY_QUEUE = "chat-delay-queue";

    /**
     * 聊天消息-延迟队列-重试
     */
    String CHAT_RETRY_QUEUE = "chat-retry-blocking";

    /**
     * 延迟任务执行时间  单位秒
     */
    int DELAY_TASK_EXECUTE_TIME = 10;

    /**
     * 重试任务执行时间  单位秒
     */
    int RETRY_TASK_EXECUTE_TIME = 10;
}
