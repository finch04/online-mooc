package com.tianji.gateway.constants;

/**
 * @ClassName RedisConstants
 * @Author wusongsong
 * @Date 2022/10/10 17:02
 * @Version
 **/
public class RedisConstants {

    //系统每日访问量
    public static final String SYSTEM_VISIT_DAILY = "SYSTEM:VISIT:DAILY:";

    public static final int batchSize = 20; // 批量发送的日志数量阈值

    public static final String batchTime = "30000"; // 批量发送的时间间隔(毫秒)

    public static final String LOG_QUEUE_KEY = "api:logs:queue"; // Redis队列键名


}
