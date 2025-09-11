package com.tianji.aigc.utils;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.tianji.aigc.domain.ChatRecord;
import com.tianji.aigc.domain.ChatSession;
import com.tianji.aigc.domain.vo.MessageVO;
import com.tianji.aigc.enums.MessageTypeEnum;
import com.tianji.aigc.mapper.ChatRecordMapper;
import com.tianji.aigc.mapper.ChatSessionMapper;
import com.tianji.aigc.memory.MyMessage;
import com.tianji.aigc.memory.clean.DataSourceType;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RBlockingQueue;
import org.redisson.api.RDelayedQueue;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static com.tianji.aigc.constants.RedisConstants.*;


@Slf4j
@Component
@RequiredArgsConstructor
@EnableScheduling
public class MessageDelayPersistHandler {

    private final RedissonClient redissonClient;
    private final StringRedisTemplate redisTemplate;
    private final ChatRecordMapper chatRecordMapper;
    private final ChatSessionMapper chatSessionMapper;
    private final MongoTemplate mongoTemplate;  // MongoDB操作模板

    // 新增：从配置文件读取当前使用的数据源类型
    @Value("${tj.ai.memory.type:MySQL}")
    private String memoryType;

    private static volatile boolean begin = true;

    @PostConstruct
    public void init() {
        CompletableFuture.runAsync(this::handleDelayTask);
        CompletableFuture.runAsync(this::handleRetryTask);
        log.info("消息延迟落库处理器初始化完成，当前配置数据源: {}", memoryType);
    }

    @PreDestroy
    public void destroy() {
        begin = false;
        log.debug("延迟任务停止执行！");
    }

    /**
     * 处理延迟队列任务
     */
    public void handleDelayTask() {
        RBlockingQueue<String> queue = redissonClient.getBlockingQueue(CHAT_DELAY_QUEUE);
        handleTask(queue);
    }

    /**
     * 处理重试队列任务
     */
    public void handleRetryTask() {
        RBlockingQueue<String> retryQueue = redissonClient.getBlockingQueue(CHAT_RETRY_QUEUE);
        handleTask(retryQueue);
    }

    /**
     * 通用任务处理逻辑
     */
    private void handleTask(RBlockingQueue<String> queue) {
        while (begin) {
            String task = null;
            try {
                task = queue.take();
                log.info("处理任务：{}", task);

                JSONObject jsonObject = JSONUtil.parseObj(task);
                String conversationId = jsonObject.getStr("conversationId");
                Long num = jsonObject.getLong("num");
                Long userId = jsonObject.getLong("userId");

                DataSourceType dataSourceType = DataSourceType.valueOf(jsonObject.getStr("dataSourceType"));

                // 根据数据源类型处理
                if (dataSourceType == DataSourceType.REDIS) {
                    handleRedisTask(conversationId, num, userId);
                } else if (dataSourceType == DataSourceType.MONGODB) {
                    handleMongoTask(conversationId, num, userId);
                }

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            } catch (Exception e) {
                log.error("处理任务失败，准备重试: {}", task, e);
                if (ObjectUtil.isNotNull(task)) {
                    handleTaskFailure(task);
                }
            }
        }
    }

    /**
     * 处理Redis数据源任务
     */
    private void handleRedisTask(String conversationId, Long num, Long userId) {
        String redisKey = getRedisKey(conversationId);
        Long size = redisTemplate.opsForList().size(redisKey);

        if (size > num) {
            log.info("对话 {} 仍有新消息，重新添加延迟任务", conversationId);
            addDelayedTask(conversationId, DataSourceType.REDIS);
            return;
        }

        List<String> messageJsonList = redisTemplate.opsForList().range(redisKey, 0, -1);
        if (ObjectUtil.isEmpty(messageJsonList)) {
            log.info("对话 {} 无Redis消息数据，删除缓存键", conversationId);
            redisTemplate.delete(redisKey);
            return;
        }

        // 转换并落库
        List<ChatRecord> records = convertRedisMessagesToRecords(messageJsonList, conversationId, userId);
        chatRecordMapper.insertOrUpdate(records);
        redisTemplate.delete(redisKey);
        log.info("对话 {} Redis消息落库完成，共 {} 条", conversationId, records.size());
    }

    /**
     * 处理MongoDB数据源任务
     */
    private void handleMongoTask(String conversationId, Long num, Long userId) {
        // 查询MongoDB中的消息记录
        Query query = Query.query(Criteria.where("conversationId").is(conversationId));
        com.tianji.aigc.memory.mongodb.ChatRecord mongoChatRecord =
                mongoTemplate.findOne(query, com.tianji.aigc.memory.mongodb.ChatRecord.class);

        // 计算当前消息数量
        long currentCount = 0;
        if (mongoChatRecord != null && mongoChatRecord.getMessages() != null) {
            currentCount = mongoChatRecord.getMessages().size();
        }

        if (currentCount > num) {
            log.info("对话 {} 仍有新消息，重新添加延迟任务", conversationId);
            addDelayedTask(conversationId, DataSourceType.MONGODB);
            return;
        }

        if (mongoChatRecord == null || mongoChatRecord.getMessages() == null || mongoChatRecord.getMessages().isEmpty()) {
            log.info("对话 {} 无MongoDB消息数据，删除记录", conversationId);
            mongoTemplate.remove(query, com.tianji.aigc.memory.mongodb.ChatRecord.class);
            return;
        }

        // 转换并落库
        List<MyMessage> messages = convertMongoChatRecordToMyMessages(mongoChatRecord);
        List<ChatRecord> records = messages.stream()
                .map(msg -> convertToChatRecord(msg, conversationId, userId))
                .collect(Collectors.toList());

        chatRecordMapper.insertOrUpdate(records);
        mongoTemplate.remove(query, com.tianji.aigc.memory.mongodb.ChatRecord.class);
        log.info("对话 {} MongoDB消息落库完成，共 {} 条", conversationId, records.size());
    }

    /**
     * 强制将配置文件指定的数据源的聊天数据落库
     */
    public void forcePersist(String conversationId) {
        Long userId = getUserIdByConversationId(conversationId);
        if (userId == null) {
            log.warn("对话 {} 无法获取用户ID，强制落库失败", conversationId);
            return;
        }

        // 根据配置文件的数据源类型处理
        switch (memoryType.toUpperCase()) {
            case "REDIS":
                handleRedisPersist(conversationId, userId);
                break;
            case "MONGODB":
                handleMongoPersist(conversationId, userId);
                break;
            case "MYSQL":
                log.info("对话 {} 使用MySQL数据源，无需额外落库", conversationId);
                break;
            default:
                log.warn("未知数据源类型: {}，不执行强制落库", memoryType);
        }

        // 取消相关延迟任务
        cancelDelayedTask(conversationId);
    }

    /**
     * 处理Redis数据源的强制落库
     */
    private void handleRedisPersist(String conversationId, Long userId) {
        String redisKey = getRedisKey(conversationId);
        Long redisSize;
        try {
            redisSize = redisTemplate.opsForList().size(redisKey);
        } catch (Exception e) {
            log.warn("对话 {} Redis数据源操作失败", conversationId, e);
            return;
        }

        if (redisSize != null && redisSize > 0) {
            List<String> messageJsonList = redisTemplate.opsForList().range(redisKey, 0, -1);
            if (ObjectUtil.isNotEmpty(messageJsonList)) {
                List<ChatRecord> records = convertRedisMessagesToRecords(messageJsonList, conversationId, userId);
                chatRecordMapper.insertOrUpdate(records);
                redisTemplate.delete(redisKey);
                log.info("对话 {} Redis强制落库完成，共 {} 条", conversationId, records.size());
            }
        } else {
            log.info("对话 {} Redis数据源无消息，无需强制落库", conversationId);
        }
    }

    /**
     * 处理MongoDB数据源的强制落库
     */
    private void handleMongoPersist(String conversationId, Long userId) {
        Query mongoQuery = Query.query(Criteria.where("conversationId").is(conversationId));
        try {
            // 查询MongoDB中的ChatRecord
            com.tianji.aigc.memory.mongodb.ChatRecord mongoChatRecord =
                    mongoTemplate.findOne(mongoQuery, com.tianji.aigc.memory.mongodb.ChatRecord.class);

            if (mongoChatRecord == null || mongoChatRecord.getMessages() == null || mongoChatRecord.getMessages().isEmpty()) {
                log.info("对话 {} MongoDB数据源无消息，无需强制落库", conversationId);
                return;
            }

            // 转换MongoDB的ChatRecord为MyMessage列表
            List<MyMessage> messages = convertMongoChatRecordToMyMessages(mongoChatRecord);

            // 转换为MySQL的ChatRecord并落库
            List<ChatRecord> records = messages.stream()
                    .map(msg -> convertToChatRecord(msg, conversationId, userId))
                    .collect(Collectors.toList());

            chatRecordMapper.insertOrUpdate(records);
            mongoTemplate.remove(mongoQuery, com.tianji.aigc.memory.mongodb.ChatRecord.class);
            log.info("对话 {} MongoDB强制落库完成，共 {} 条", conversationId, records.size());
        } catch (Exception e) {
            log.warn("对话 {} MongoDB数据源操作失败", conversationId, e);
        }
    }

    /**
     * 添加延迟任务
     */
    public void addDelayedTask(String conversationId, DataSourceType dataSourceType) {
        Long userId = getUserIdByConversationId(conversationId);
        if (userId == null) {
            log.warn("对话 {} 无法获取用户ID，添加延迟任务失败", conversationId);
            return;
        }

        // 获取当前消息数量
        Long currentSize = 0L;
        if (dataSourceType == DataSourceType.REDIS) {
            String redisKey = getRedisKey(conversationId);
            currentSize = redisTemplate.opsForList().size(redisKey);
        } else if (dataSourceType == DataSourceType.MONGODB) {
            Query query = Query.query(Criteria.where("conversationId").is(conversationId));
            com.tianji.aigc.memory.mongodb.ChatRecord mongoChatRecord =
                    mongoTemplate.findOne(query, com.tianji.aigc.memory.mongodb.ChatRecord.class);
            if (mongoChatRecord != null && mongoChatRecord.getMessages() != null) {
                currentSize = (long) mongoChatRecord.getMessages().size();
            }
        }

        JSONObject task = JSONUtil.createObj()
                .set("conversationId", conversationId)
                .set("userId", userId)
                .set("num", currentSize == null ? 0 : currentSize)
                .set("retryCount", 0)
                .set("dataSourceType", dataSourceType.name());

        RBlockingQueue<String> blockingQueue = redissonClient.getBlockingQueue(CHAT_DELAY_QUEUE);
        RDelayedQueue<String> delayedQueue = redissonClient.getDelayedQueue(blockingQueue);
        delayedQueue.offer(task.toString(), DELAY_TASK_EXECUTE_TIME, TimeUnit.SECONDS);
        log.info("添加延迟任务，对话ID: {}，数据源: {}，延迟: {}秒",
                conversationId, dataSourceType, DELAY_TASK_EXECUTE_TIME);
    }

    /**
     * 处理任务失败
     */
    private void handleTaskFailure(String task) {
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

    /**
     * 添加到重试队列
     */
    private void addRetryTask(String task) {
        RBlockingQueue<String> retryBlockingQueue = redissonClient.getBlockingQueue(CHAT_RETRY_QUEUE);
        RDelayedQueue<String> retryDelayedQueue = redissonClient.getDelayedQueue(retryBlockingQueue);
        retryDelayedQueue.offer(task, RETRY_TASK_EXECUTE_TIME, TimeUnit.SECONDS);
    }

    /**
     * 取消延迟任务
     */
    private void cancelDelayedTask(String conversationId) {
        // 从延迟队列中移除相关任务
        removeTaskFromQueue(redissonClient.getBlockingQueue(CHAT_DELAY_QUEUE), conversationId);
        // 从重试队列中移除相关任务
        removeTaskFromQueue(redissonClient.getBlockingQueue(CHAT_RETRY_QUEUE), conversationId);
        log.info("对话 {} 的延迟任务已取消", conversationId);
    }

    /**
     * 从队列中移除指定对话的任务
     */
    private void removeTaskFromQueue(RBlockingQueue<String> queue, String conversationId) {
        try {
            List<String> remainingTasks = new ArrayList<>();
            String task;
            while ((task = queue.poll()) != null) {
                JSONObject taskJson = JSONUtil.parseObj(task);
                if (!conversationId.equals(taskJson.getStr("conversationId"))) {
                    remainingTasks.add(task);
                }
            }
            // 将非目标任务放回队列
            if (!remainingTasks.isEmpty()) {
                queue.addAll(remainingTasks);
            }
        } catch (Exception e) {
            log.error("从队列移除任务失败", e);
        }
    }

    /**
     * 转换Redis消息为ChatRecord
     */
    private List<ChatRecord> convertRedisMessagesToRecords(List<String> messageJsonList,
                                                           String conversationId, Long userId) {
        return messageJsonList.stream()
                .map(json -> JSONUtil.toBean(json, MyMessage.class))
                .map(myMessage -> convertToChatRecord(myMessage, conversationId, userId))
                .collect(Collectors.toList());
    }

    /**
     * 转换MongoDB的ChatRecord为MyMessage列表
     */
    private List<MyMessage> convertMongoChatRecordToMyMessages(com.tianji.aigc.memory.mongodb.ChatRecord mongoChatRecord) {
        List<MyMessage> myMessages = new ArrayList<>();
        if (mongoChatRecord.getMessages() == null || mongoChatRecord.getMessages().isEmpty()) {
            return myMessages;
        }

        // 遍历MongoDB中存储的消息字符串列表，转换为MyMessage对象
        for (String messageStr : mongoChatRecord.getMessages()) {
            // 假设messageStr是JSON格式字符串，直接解析为MyMessage
            MyMessage myMessage = JSONUtil.toBean(messageStr, MyMessage.class);
            myMessages.add(myMessage);
        }
        return myMessages;
    }

    /**
     * 转换MyMessage为ChatRecord
     */
    private ChatRecord convertToChatRecord(MyMessage myMessage, String conversationId, Long userId) {
        MessageVO messageVO = MessageVO.builder()
                .type(convertMessageType(myMessage.getMessageType()))
                .content(myMessage.getTextContent())
                .params(myMessage.getParams())
                .build();

        return ChatRecord.builder()
                .conversationId(conversationId)
                .data(JSONUtil.toJsonStr(messageVO))
                .createTime(LocalDateTime.now())
                .updateTime(LocalDateTime.now())
                .creater(userId)
                .updater(userId)
                .build();
    }

    /**
     * 从对话ID获取用户ID
     */
    private Long getUserIdByConversationId(String conversationId) {
        // 尝试从对话ID解析（格式：userId_conversationId）
        try {
            return Long.valueOf(conversationId.split("_", 2)[0]);
        } catch (Exception e) {
            // 从数据库查询
            LambdaQueryWrapper<ChatSession> queryWrapper = Wrappers.<ChatSession>lambdaQuery()
                    .eq(ChatSession::getSessionId, conversationId);
            ChatSession session = chatSessionMapper.selectOne(queryWrapper);
            return session != null ? session.getUserId() : null;
        }
    }

    private String getRedisKey(String conversationId) {
        return "CHAT:" + conversationId;
    }

    private MessageTypeEnum convertMessageType(String messageType) {
        switch (messageType) {
            case "USER":
                return MessageTypeEnum.USER;
            case "ASSISTANT":
                return MessageTypeEnum.ASSISTANT;
            default:
                log.warn("未知消息类型: {}", messageType);
                return MessageTypeEnum.USER;
        }
    }
}