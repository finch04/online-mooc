package com.tianji.gateway.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tianji.common.autoconfigure.mq.RabbitMqHelper;
import com.tianji.common.constants.MqConstants;
import com.tianji.common.domain.vo.LogBusinessVO;
import com.tianji.gateway.constants.RedisConstants;
import com.tianji.gateway.filter.LogTrackingFilter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static com.tianji.gateway.constants.RedisConstants.*;

@Slf4j
@Component
@RequiredArgsConstructor
public class LogCollector {

    private final StringRedisTemplate redisTemplate;
    private final RabbitMqHelper mqHelper;
    private final ObjectMapper objectMapper;

    /**
     * 记录请求响应日志
     */
    public void logResponse(LogBusinessVO vo, ServerHttpResponse response, long startTime, Throwable throwable) {
        long endTime = System.currentTimeMillis();
        // 设置响应状态码
        vo.setResponseCode(response.getStatusCode() != null ? response.getStatusCode().value() : 500);
        // 设置响应消息
        vo.setResponseMsg(throwable == null ? "SUCCESS" : throwable.getMessage());
        // 设置响应时间
        vo.setResponseTime(endTime - startTime);

        // 存入Redis
        saveLogToRedis(vo);
    }

    /**
     * 将日志存入Redis
     */
    private void saveLogToRedis(LogBusinessVO vo) {
        try {
            String logJson = objectMapper.writeValueAsString(vo);
            redisTemplate.opsForList().rightPush(RedisConstants.LOG_QUEUE_KEY, logJson);
            // 设置过期时间，防止内存溢出
            redisTemplate.expire(RedisConstants.LOG_QUEUE_KEY, 24, TimeUnit.HOURS);
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize log: {}", vo, e);
        }
    }

    /**
     * 批量发送日志到MQ
     */
    @Scheduled(fixedDelayString = batchTime) // 每10秒执行一次
    public void sendBatchLogs() {
        try {
            // 批量获取日志并删除
            List<String> logsJson = redisTemplate.execute((RedisCallback<List<String>>) connection -> {
                List<String> result = new ArrayList<>();
                connection.openPipeline();
                connection.lRange(LOG_QUEUE_KEY.getBytes(), 0, batchSize - 1);
                connection.lTrim(LOG_QUEUE_KEY.getBytes(), batchSize, -1);
                List<Object> responses = connection.closePipeline();

                if (responses.size() > 0 && responses.get(0) instanceof List) {
                    @SuppressWarnings("unchecked")
                    List<byte[]> bytesList = (List<byte[]>) responses.get(0);
                    for (byte[] bytes : bytesList) {
                        if (bytes != null) {
                            result.add(new String(bytes, StandardCharsets.UTF_8));
                        }
                    }
                }
                return result;
            });

            // 发送日志到MQ
            if (!logsJson.isEmpty()) {
                List<LogBusinessVO> logs = logsJson.stream()
                        .map(json -> {
                            try {
                                return objectMapper.readValue(json, LogBusinessVO.class);
                            } catch (JsonProcessingException e) {
                                log.error("Failed to parse log: {}", json, e);
                                return null;
                            }
                        })
                        .filter(Objects::nonNull)
                        .collect(Collectors.toList());

                if (!logs.isEmpty()) {
                    mqHelper.sendAsync(
                            MqConstants.Exchange.DATA_EXCHANGE,
                            MqConstants.Key.DATA_LOG_KEY,
                            logs
                    );
                }
            }
        } catch (Exception e) {
            log.error("Failed to send batch logs", e);
        }
    }

    /**
     * 在异常处理器中记录日志
     */
    public Mono<Void> logException(LogBusinessVO vo, ServerHttpResponse response, Throwable ex) {
        logResponse(vo, response, System.currentTimeMillis(), ex);
        return response.setComplete();
    }
}