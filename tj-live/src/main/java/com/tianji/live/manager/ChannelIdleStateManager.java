package com.tianji.live.manager;

import jakarta.annotation.PreDestroy;
import jakarta.websocket.CloseReason;
import jakarta.websocket.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Author： fsq
 * Description： 链接心跳管理器，自动关闭空闲链接
 **/
@Component
public class ChannelIdleStateManager {

    private Logger logger = LoggerFactory.getLogger(ChannelIdleStateManager.class);
    private static final Map<String, Long> USER_LAST_READ_TIMESTAMP = new ConcurrentHashMap<>();

    @Value("${tj.im.heartbeat.timeout.seconds:30}")
    private long READ_TIMEOUT_SECONDS;
    private final ScheduledThreadPoolExecutor scheduledExecutor;

    public ChannelIdleStateManager() {
        int coreCount = Runtime.getRuntime().availableProcessors();
        scheduledExecutor = new ScheduledThreadPoolExecutor(coreCount, new ThreadPoolExecutor.AbortPolicy());
    }

    public void connect(String userId, Session socketSession) {
        scheduledExecutor.schedule(new ReadTimeOutTask(userId, socketSession), READ_TIMEOUT_SECONDS, TimeUnit.SECONDS);
        USER_LAST_READ_TIMESTAMP.put(socketSession.getId(), System.nanoTime());
    }

    public void read(Session socketSession){
        if (socketSession.isOpen()){
            USER_LAST_READ_TIMESTAMP.put(socketSession.getId(), System.nanoTime());
            return;
        }
        logger.warn("更新心跳异常，连接已关闭");
    }
    public void disconnect(Session session) {
        USER_LAST_READ_TIMESTAMP.remove(session.getId());
    }

    @PreDestroy
    public void destroy() {
        scheduledExecutor.shutdown();
    }


    private final class ReadTimeOutTask implements Runnable {

        private final String userId;
        private final Session socketSession;

        public ReadTimeOutTask(String id, Session socketSession) {
            this.userId = id;
            this.socketSession = socketSession;
        }

        @Override
        public void run() {
            if (!socketSession.isOpen()) {
                USER_LAST_READ_TIMESTAMP.remove(socketSession.getId());
                ConnectionManager.cancel(userId, socketSession);
                return;
            }
            Long lastReadTime = USER_LAST_READ_TIMESTAMP.get(socketSession.getId());
            if (lastReadTime == null) {
                logger.error("ReadTimeOutTask-中无法找到对应channel的lastReadTime");
                return;
            }
            //按纳秒计算超时时间
            long nextDelay = READ_TIMEOUT_SECONDS*1000000000 - (System.nanoTime() - lastReadTime);
            if (nextDelay <= 0) {
                try {
                    // 空闲超时关闭channel
                    if (socketSession.isOpen()) {
                        logger.info("WebSocket通道空闲超时关闭,id:{},socket:{}", userId, socketSession.getId());
                        socketSession.close(new CloseReason(CloseReason.CloseCodes.NORMAL_CLOSURE,"心跳超时，正常关闭链接"));
                    }
                } catch (Throwable throwable) {
                    logger.error("心跳关闭socket发生异常-task:{}", this, throwable);
                } finally {
                    // 确保map中对应的信息一定要被删除，避免内存泄露
                    USER_LAST_READ_TIMESTAMP.remove(socketSession.getId());
                    ConnectionManager.cancel(userId, socketSession);
                }
            } else {
                scheduledExecutor.schedule(new ReadTimeOutTask(userId, socketSession), nextDelay, TimeUnit.NANOSECONDS);
            }
        }
    }
}