package com.tianji.learning.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import com.tianji.api.client.course.CourseClient;
import com.tianji.api.client.user.UserClient;
import com.tianji.api.dto.course.CourseSearchDTO;
import com.tianji.api.dto.user.UserDTO;
import com.tianji.learning.domain.dto.ShareDetailDTO;
import com.tianji.learning.domain.dto.ShareLinkDTO;
import com.tianji.learning.service.IShareService;
import com.tianji.learning.utils.ShortCodeUtil;
import com.tianji.learning.utils.SnowflakeIdGenerator;
import org.joda.time.LocalDateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

@Service
public class ShareServiceImpl implements IShareService {
    private static final String SHORT_URL_PREFIX = "short:url:";      // 短码 -> 分享ID
    private static final String SHARE_DETAIL_PREFIX = "share:detail:"; // 分享ID -> 详情
    private static final long EXPIRE_TIME = 3 * 60 * 60; // 3小时

    @Autowired
    private StringRedisTemplate redisTemplate;
    @Autowired
    private CourseClient courseClient;
    @Autowired
    private UserClient userClient;

    @Override
    public ShareLinkDTO generateShareLink(Long userId, Long courseId) {
        // 1. 生成唯一分享ID（雪花算法）
        long shareId = SnowflakeIdGenerator.getInstance().nextId();

        // 2. 生成短码（62进制编码）
        String shortCode = ShortCodeUtil.encode(shareId);

        // 3. 构建短链接
        String shortUrl = buildShortUrl(shortCode);

        // 4. 存储短码映射（使用String类型）
        redisTemplate.opsForValue().set(
                SHORT_URL_PREFIX + shortCode,
                String.valueOf(shareId), // 转换为字符串存储
                EXPIRE_TIME,
                TimeUnit.SECONDS
        );

        // 5. 存储分享详情（使用String类型的JSON）
        String detailJson = buildDetailJson(userId, courseId);
        redisTemplate.opsForValue().set(
                SHARE_DETAIL_PREFIX + shareId,
                detailJson,
                EXPIRE_TIME,
                TimeUnit.SECONDS
        );

        return new ShareLinkDTO(shareId, shortUrl);
    }

    @Override
    public ShareDetailDTO parseShareLink(String shortCode) {
        // 1. 通过短码获取分享ID（返回值为String）
        String shareIdStr = redisTemplate.opsForValue().get(SHORT_URL_PREFIX + shortCode);
        if (shareIdStr == null) {
            return null; // 链接不存在或已过期
        }

        long shareId = Long.parseLong(shareIdStr);

        // 2. 通过分享ID获取详情JSON
        String detailJson = redisTemplate.opsForValue().get(SHARE_DETAIL_PREFIX + shareId);
        if (detailJson == null) {
            return null;
        }

        // 3. 解析JSON为对象
        return parseDetailFromJson(detailJson, shareId);
    }

    private String buildShortUrl(String shortCode) {
        return shortCode;
    }

    private String buildDetailJson(Long userId, Long courseId) {
        Map<String, Object> detailMap = new HashMap<>();
        detailMap.put("userId", userId);
        detailMap.put("courseId", courseId);
        detailMap.put("createTime", System.currentTimeMillis());

        try {
            return new ObjectMapper().writeValueAsString(detailMap);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to serialize share detail", e);
        }
    }

    private ShareDetailDTO parseDetailFromJson(String json, long shareId) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            Map<String, Object> detailMap = mapper.readValue(json, Map.class);

            Long userId = Long.parseLong(detailMap.get("userId").toString());
            Long courseId = Long.parseLong(detailMap.get("courseId").toString());

            ShareDetailDTO shareDetailDTO = new ShareDetailDTO();
            shareDetailDTO.setShareId(shareId);
            shareDetailDTO.setUserId(userId);
            shareDetailDTO.setCourseId(courseId);
            shareDetailDTO.setCreateTime(new Date(Long.parseLong(detailMap.get("createTime").toString())));

            UserDTO dto = userClient.queryUserById(userId);
            shareDetailDTO.setUserIcon(dto.getIcon());
            shareDetailDTO.setUserName(dto.getName());

            CourseSearchDTO searchInfo = courseClient.getSearchInfo(courseId);
            shareDetailDTO.setCourseName(searchInfo.getName());
            shareDetailDTO.setCoverUrl(searchInfo.getCoverUrl());

            return shareDetailDTO;

        } catch (Exception e) {
            throw new RuntimeException("Failed to deserialize share detail", e);
        }
    }
}