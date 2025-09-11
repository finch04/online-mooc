package com.tianji.aigc.constants;

public class RedisConstants {
    // 智能体配置Hash键（统一存储所有配置）
    public static final String AGENT_CONFIG_KEY = "AGENT:CONFIG";
    
    // Hash中的字段名
    public static final String TITLE_FIELD = "title";
    public static final String DESCRIBE_FIELD = "describe";
    public static final String HOT_QUESTIONS_FIELD = "hotQuestions";


    /**
     * 聊天延迟任务队列
     * 用于存储需要延迟处理的聊天消息落库任务
     */
    public static final String CHAT_DELAY_QUEUE = "chat:delay:queue";

    /**
     * 聊天任务重试队列
     * 用于存储处理失败需要重试的任务
     */
    public static final String CHAT_RETRY_QUEUE = "chat:retry:queue";

    /**
     * 聊天任务死信队列
     * 用于存储达到最大重试次数仍失败的任务
     */
    public static final String CHAT_DEAD_LETTER_QUEUE = "chat:dead:queue";

    /**
     * 延迟任务执行时间  单位秒
     */
    public static final int DELAY_TASK_EXECUTE_TIME = 10;

    /**
    /**
     * 重试任务执行延迟时间（秒）
     * 失败任务放入重试队列后的延迟执行时间
     */
    public static final int RETRY_TASK_EXECUTE_TIME = 10;

    /**
     * 聊天消息在Redis中的存储前缀
     * 完整键名格式: CHAT_MESSAGE_KEY_PREFIX + conversationId
     */
    public static final String CHAT_MESSAGE_KEY_PREFIX = "CHAT:";






}