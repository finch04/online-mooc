package com.tianji.chat.utils;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.tianji.chat.domain.po.ChatSession;
import com.tianji.chat.domain.po.UserSession;
import com.tianji.chat.service.IChatSessionService;
import com.tianji.chat.service.IUserSessionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RBlockingQueue;
import org.redisson.api.RDelayedQueue;
import org.redisson.api.RedissonClient;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import static com.tianji.chat.constants.RedisConstants.*;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataDelayTaskHandler {

    private final RedissonClient redissonClient;

    private final StringRedisTemplate redisTemplate;

    private final IChatSessionService chatSessionService;

    private final IUserSessionService userSessionService;

    private static volatile boolean begin = true;

    @PostConstruct
    public void init(){
        CompletableFuture.runAsync(this::handleDelayTask);
        CompletableFuture.runAsync(this::handleRetryTask);
    }

    @PreDestroy
    public void destroy(){
        begin = false;
        log.debug("延迟任务停止执行！");
    }

    public void handleDelayTask() {
        RBlockingQueue<String> queue = redissonClient.getBlockingQueue(CHAT_DELAY_QUEUE);
        handleTask(queue);
    }

    public void handleRetryTask() {
        RBlockingQueue<String> retryQueue = redissonClient.getBlockingQueue(CHAT_RETRY_QUEUE);
        handleTask(retryQueue);
    }

    private void handleTask(RBlockingQueue<String> retryQueue) {
        while (begin) {
            String task = null;
            try {
                task = retryQueue.take();
                System.out.println("处理延迟任务：" + task);

                // 获取延迟队列中的 key 和 num
                JSONObject jsonObject = JSONUtil.parseObj(task);
                String key = jsonObject.getStr("key");
                Long num = jsonObject.getLong("num");
                // 去Redis中获取值
                Long size = redisTemplate.opsForList().size(key);
                // 如果Redis中队列的size > num，则说明用户还在聊天，则不处理
                if (size > num) {
                    continue;
                }
                // size <= num，则说明用户已经结束聊天，则从Redis中队列中取出所有数据，并保存到数据库中
                // 查询Redis中的数据
                List<String> contentList = redisTemplate.opsForList().range(key, 0, -1);
                // 查询数据库中最后保存的数据
                String[] split = key.split(":");

                Integer count = userSessionService.lambdaQuery().eq(UserSession::getSessionId, split[3]).count();
                if(count==0){
                    //这个会话已经被删除了，延迟同步不用做了
                    continue;
                }
                List<ChatSession> lastContents = chatSessionService.lambdaQuery()
                        .eq(ChatSession::getUserId, split[2])
                        .eq(ChatSession::getSessionId, split[3])
                        .orderByDesc(ChatSession::getSegmentIndex)
                        .list(); // 查询所有匹配的数据

                ChatSession lastContent = !lastContents.isEmpty() ? lastContents.get(0) : null; // 取第一条数据或者
                int index = 0;
                if (ObjectUtil.isNotEmpty(lastContent)) {
                    index = lastContent.getSegmentIndex();
                }
                List<ChatSession> chatSessionList = new ArrayList<>();
                for (int i = index + 1; i < contentList.size(); i++) {
                    ChatSession chatSession = ChatSession.builder()
                            .userId(Long.valueOf(split[2]))
                            .sessionId(split[3])
                            .segmentIndex(i)
                            .content(contentList.get(i))
                            .createTime(LocalDateTime.now())
                            .build();
                    chatSessionList.add(chatSession);
                }

                chatSessionService.saveBatch(chatSessionList);

                redisTemplate.delete(key);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            } catch (Exception e) {
                // 可记录日志、发送告警、写入失败队列
                log.error("处理延迟任务失败，准备重试: {}", task, e);
                JSONObject taskJson = JSONUtil.parseObj(task);

                int retryCount = taskJson.getInt("retryCount", 0);
                if (retryCount < 3) {
                    taskJson.set("retryCount", retryCount + 1);
                    // 重试延迟 10 秒
                    addRetryTask(taskJson.toString());
                    log.info("任务 {} 重试第 {} 次", taskJson.getStr("key"), retryCount + 1);
                } else {
                    log.error("任务最终失败，加入死信队列: {}", taskJson);
                    redisTemplate.opsForList().rightPush("chat-dead-letter-queue", taskJson.toString());
                }
            }
        }
    }


    public void addDelayedTask(String task, long delay, TimeUnit unit) {
        RBlockingQueue<String> blockingQueue = redissonClient.getBlockingQueue(CHAT_DELAY_QUEUE);
        RDelayedQueue<String> delayedQueue = redissonClient.getDelayedQueue(blockingQueue);
        delayedQueue.offer(task, delay, unit);
    }

    private void addRetryTask(String task) {

        RBlockingQueue<String> retryBlockingQueue = redissonClient.getBlockingQueue(CHAT_RETRY_QUEUE);
        RDelayedQueue<String> retryDelayedQueue = redissonClient.getDelayedQueue(retryBlockingQueue);
        retryDelayedQueue.offer(task, 10, TimeUnit.SECONDS);
    }

}
