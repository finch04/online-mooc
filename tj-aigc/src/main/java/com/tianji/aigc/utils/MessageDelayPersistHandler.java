package com.tianji.aigc.utils;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.tianji.aigc.domain.ChatRecord;
import com.tianji.aigc.domain.vo.MessageVO;
import com.tianji.aigc.mapper.ChatRecordMapper;
import com.tianji.aigc.memory.MessageUtil;
import com.tianji.aigc.memory.MyMessage;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RBlockingQueue;
import org.redisson.api.RDelayedQueue;
import org.redisson.api.RedissonClient;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import static com.tianji.aigc.constants.RedisConstants.*;


@Slf4j
@Component
@RequiredArgsConstructor
public class MessageDelayPersistHandler {

    private final RedissonClient redissonClient;
    private final StringRedisTemplate redisTemplate;
    private final ChatRecordMapper chatRecordMapper;

    private static volatile boolean begin = true;

    @PostConstruct
    public void init() {
        CompletableFuture.runAsync(this::handleDelayTask);
        CompletableFuture.runAsync(this::handleRetryTask);
    }

    @PreDestroy
    public void destroy() {
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

    private void handleTask(RBlockingQueue<String> queue) {
        while (begin) {
            String task = null;
            try {
                task = queue.take();
                log.info("处理延迟任务：{}", task);

                JSONObject jsonObject = JSONUtil.parseObj(task);
                String conversationId = jsonObject.getStr("conversationId");
                Long num = jsonObject.getLong("num");
                Long userId = jsonObject.getLong("userId");

                String redisKey = getRedisKey(conversationId);
                Long size = redisTemplate.opsForList().size(redisKey);

                if (size > num) {
                    log.info("对话 {} 仍有新消息，暂不落库", conversationId);
                    continue;
                }

                List<String> messageJsonList = redisTemplate.opsForList().range(redisKey, 0, -1);
                if (ObjectUtil.isEmpty(messageJsonList)) {
                    log.info("对话 {} 无消息数据，删除缓存键", conversationId);
                    redisTemplate.delete(redisKey);
                    continue;
                }

                List<ChatRecord> records = new ArrayList<>();
                for (String messageJson : messageJsonList) {
                    MyMessage myMessage = JSONUtil.toBean(messageJson, MyMessage.class);
                    MessageVO messageVO = MessageVO.builder()
                            .type(convertMessageType(myMessage.getMessageType()))
                            .content(myMessage.getTextContent())
                            .params(myMessage.getParams())
                            .build();

                    ChatRecord record = ChatRecord.builder()
                            .conversationId(conversationId)
                            .data(JSONUtil.toJsonStr(messageVO))
                            .createTime(LocalDateTime.now())
                            .updateTime(LocalDateTime.now())
                            .creater(userId)
                            .updater(userId)
                            .build();
                    records.add(record);
                }

                chatRecordMapper.insertOrUpdate(records);
                redisTemplate.delete(redisKey);
                log.info("对话 {} 消息落库完成，共 {} 条", conversationId, records.size());

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            } catch (Exception e) {
                log.error("处理延迟任务失败，准备重试: {}", task, e);
                if (ObjectUtil.isNotNull(task)) {
                    JSONObject taskJson = JSONUtil.parseObj(task);
                    int retryCount = taskJson.getInt("retryCount", 0);
                    if (retryCount < 3) {
                        taskJson.set("retryCount", retryCount + 1);
                        addRetryTask(taskJson.toString());
                        log.info("任务 {} 重试第 {} 次", taskJson.getStr("conversationId"), retryCount + 1);
                    } else {
                        log.error("任务最终失败，加入死信队列: {}", taskJson);
                        redisTemplate.opsForList().rightPush(CHAT_DEAD_LETTER_QUEUE, taskJson.toString());
                    }
                }
            }
        }
    }

    /**
     * 强制将Redis中的聊天数据落库
     */
    public void forcePersist(String conversationId, Long userId) {
        String redisKey = getRedisKey(conversationId);
        Long size = redisTemplate.opsForList().size(redisKey);

        if (size == null || size == 0) {
            log.info("对话 {} 无缓存消息，无需强制落库", conversationId);
            return;
        }

        List<String> messageJsonList = redisTemplate.opsForList().range(redisKey, 0, -1);
        if (ObjectUtil.isEmpty(messageJsonList)) {
            log.info("对话 {} 无消息数据，删除缓存键", conversationId);
            redisTemplate.delete(redisKey);
            return;
        }

        List<ChatRecord> records = new ArrayList<>();
        for (String messageJson : messageJsonList) {
            MyMessage myMessage = JSONUtil.toBean(messageJson, MyMessage.class);
            MessageVO messageVO = MessageVO.builder()
                    .type(convertMessageType(myMessage.getMessageType()))
                    .content(myMessage.getTextContent())
                    .params(myMessage.getParams())
                    .build();

            ChatRecord record = ChatRecord.builder()
                    .conversationId(conversationId)
                    .data(JSONUtil.toJsonStr(messageVO))
                    .createTime(LocalDateTime.now())
                    .updateTime(LocalDateTime.now())
                    .creater(userId)
                    .updater(userId)
                    .build();
            records.add(record);
        }

        chatRecordMapper.insertOrUpdate(records);
        redisTemplate.delete(redisKey);
        log.info("对话 {} 强制落库完成，共 {} 条", conversationId, records.size());

        // 取消延迟任务
        cancelDelayedTask(conversationId);
    }

    /**
     * 取消延迟任务（简单实现，实际可能需要更完善的任务追踪机制）
     */
    private void cancelDelayedTask(String conversationId) {
        // 这里简化处理，实际项目中可能需要维护任务ID与对话ID的映射关系
        RBlockingQueue<String> queue = redissonClient.getBlockingQueue(CHAT_DELAY_QUEUE);
        RDelayedQueue<String> delayedQueue = redissonClient.getDelayedQueue(queue);
        // 注意：Redisson没有直接取消延迟任务的API，这里只是清空队列的方式
        // 生产环境可能需要更优雅的实现方式
        queue.clear();
        log.info("对话 {} 的延迟任务已取消", conversationId);
    }

    public void addDelayedTask(String conversationId) {
        String redisKey = getRedisKey(conversationId);
        Long currentSize = redisTemplate.opsForList().size(redisKey);
        String userId = conversationId.split("_", 2)[0];
        JSONObject task = JSONUtil.createObj()
                .set("conversationId", conversationId)
                .set("userId", userId)
                .set("num", currentSize == null ? 0 : currentSize)
                .set("retryCount", 0);

        RBlockingQueue<String> blockingQueue = redissonClient.getBlockingQueue(CHAT_DELAY_QUEUE);
        RDelayedQueue<String> delayedQueue = redissonClient.getDelayedQueue(blockingQueue);
        delayedQueue.offer(task.toString(), DELAY_TASK_EXECUTE_TIME,  TimeUnit.SECONDS);
        log.info("添加延迟任务，对话ID: {}，延迟: {} {}", conversationId,  DELAY_TASK_EXECUTE_TIME, TimeUnit.SECONDS);
    }

    private void addRetryTask(String task) {
        RBlockingQueue<String> retryBlockingQueue = redissonClient.getBlockingQueue(CHAT_RETRY_QUEUE);
        RDelayedQueue<String> retryDelayedQueue = redissonClient.getDelayedQueue(retryBlockingQueue);
        retryDelayedQueue.offer(task, RETRY_TASK_EXECUTE_TIME, TimeUnit.SECONDS);
    }

    private String getRedisKey(String conversationId) {
        return "CHAT:" + conversationId;
    }

    private com.tianji.aigc.enums.MessageTypeEnum convertMessageType(String messageType) {
        switch (messageType) {
            case "USER":
                return com.tianji.aigc.enums.MessageTypeEnum.USER;
            case "ASSISTANT":
                return com.tianji.aigc.enums.MessageTypeEnum.ASSISTANT;
            default:
                log.warn("未知消息类型: {}", messageType);
                return com.tianji.aigc.enums.MessageTypeEnum.USER;
        }
    }
}