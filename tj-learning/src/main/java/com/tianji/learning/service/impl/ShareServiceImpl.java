package com.tianji.learning.service.impl;

import com.tianji.api.client.course.CourseClient;
import com.tianji.api.client.user.UserClient;
import com.tianji.api.dto.course.CourseSearchDTO;
import com.tianji.api.dto.user.UserDTO;
import com.tianji.learning.constants.RedisConstants;
import com.tianji.learning.domain.dto.ShareDetailDTO;
import com.tianji.learning.domain.dto.ShareLinkDTO;
import com.tianji.learning.service.IShareService;
import com.tianji.learning.utils.ShortCodeUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.concurrent.TimeUnit;

@Service
public class ShareServiceImpl implements IShareService {

    @Autowired
    private StringRedisTemplate redisTemplate;
    @Autowired
    private CourseClient courseClient;
    @Autowired
    private UserClient userClient;

    @Override
    public ShareLinkDTO generateShareLink(Long userId, Long courseId) {
        // 检查 Redis 中是否已经存在相同用户 ID 和课程 ID 的短码
        String userCourseKey = RedisConstants.SHORT_URL_PREFIX + userId + ":" + courseId;
        String shortCode = redisTemplate.opsForValue().get(userCourseKey);

        if (shortCode == null) {
            // 生成短码
            shortCode = generateUniqueShortCode(userId, courseId);

            // 存储短码映射（使用String类型）
            // 格式：SHORT_URL_PREFIX:userId:courseId -> shortCode
            redisTemplate.opsForValue().set(
                    userCourseKey,
                    shortCode,
                    RedisConstants.EXPIRE_TIME,
                    TimeUnit.SECONDS
            );

            // 存储短码到详情的映射
            // 格式：SHARE_DETAIL_PREFIX:shortCode -> userId:courseId
            String detailKey = RedisConstants.SHARE_DETAIL_PREFIX + shortCode;
            redisTemplate.opsForValue().set(
                    detailKey,
                    userId + ":" + courseId,
                    RedisConstants.EXPIRE_TIME,
                    TimeUnit.SECONDS
            );
        }

        // 构建短链接
        String shortUrl = buildShortUrl(shortCode);

        return new ShareLinkDTO(userId, courseId, shortUrl);
    }

    @Override
    public ShareDetailDTO parseShareLink(String shortCode) {
        // 1. 通过短码获取用户ID和课程ID
        String userCourseStr = redisTemplate.opsForValue().get(RedisConstants.SHARE_DETAIL_PREFIX + shortCode);
        if (userCourseStr == null) {
            return null; // 链接不存在或已过期
        }

        String[] parts = userCourseStr.split(":");
        if (parts.length != 2) {
            return null; // 格式错误
        }

        Long userId = Long.parseLong(parts[0]);
        Long courseId = Long.parseLong(parts[1]);

        // 2. 构建并返回分享详情
        return buildShareDetailDTO(userId, courseId, shortCode);
    }

    private String generateUniqueShortCode(Long userId, Long courseId) {
        // 使用用户ID、课程ID和时间戳生成唯一哈希
        String baseStr = userId + "-" + courseId + "-" + System.currentTimeMillis() / (24 * 60 * 60 * 1000);
        java.security.MessageDigest digest;
        try {
            digest = java.security.MessageDigest.getInstance("MD5");
            byte[] hash = digest.digest(baseStr.getBytes("UTF-8"));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            String hashStr = hexString.toString();

            // 截取部分哈希作为短码（取中间部分以增加随机性）
            int shortCodeLength = 8;
            int start = (hashStr.length() - shortCodeLength) / 2;
            String shortCode = hashStr.substring(start, start + shortCodeLength);

            // 转换为Base62编码，进一步缩短长度
            return ShortCodeUtil.encode(shortCode);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private String buildShortUrl(String shortCode) {
        // 这里应返回完整的短链接，例如：https://tj.com/s/{shortCode}
        return shortCode;
    }

    private ShareDetailDTO buildShareDetailDTO(Long userId, Long courseId, String shortCode) {
        ShareDetailDTO shareDetailDTO = new ShareDetailDTO();
        shareDetailDTO.setUserId(userId);
        shareDetailDTO.setCourseId(courseId);
        shareDetailDTO.setShortCode(shortCode);
        shareDetailDTO.setCreateTime(new Date());

        // 获取用户信息
        UserDTO userDTO = userClient.queryUserById(userId);
        if (userDTO != null) {
            shareDetailDTO.setUserIcon(userDTO.getIcon());
            shareDetailDTO.setUserName(userDTO.getName());
        }

        // 获取课程信息
        CourseSearchDTO courseDTO = courseClient.getSearchInfo(courseId);
        if (courseDTO != null) {
            shareDetailDTO.setCourseName(courseDTO.getName());
            shareDetailDTO.setCoverUrl(courseDTO.getCoverUrl());
        }

        return shareDetailDTO;
    }
}