package com.tianji.live.service.impl;

import com.tianji.live.constants.IMConstants;
import com.tianji.live.domain.po.UserFollow;
import com.tianji.live.mapper.UserFollowMapper;
import com.tianji.live.service.IUserFollowService;
import com.tianji.live.utils.IMCacheKeyBuilder;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;


/**
 * 用户关注服务实现类（优化版：简化Redis操作 + 定期同步MySQL）
 */
@Service
@Slf4j
public class UserFollowServiceImpl implements IUserFollowService {

    @Resource
    private StringRedisTemplate stringRedisTemplate;
    @Resource
    private UserFollowMapper userFollowMapper;
    @Resource
    private IMCacheKeyBuilder cacheKeyBuilder;

    // 缓存过期时间（7天，常量定义）
    private static final long EXPIRE_DAYS = 7;

    // ======================== 核心业务方法（简化Redis操作） ========================
    @Override
    public Boolean follow(Long userId, Long followedUserId) {
        // 1. 参数校验（无效参数直接返回）
        if (userId == null || followedUserId == null || userId.equals(followedUserId)) {
            return false;
        }

        String followKey = cacheKeyBuilder.buildUserFollowCacheKey(userId);
        String fansKey = cacheKeyBuilder.buildUserFansCacheKey(followedUserId);
        String followedUserIdStr = toString(followedUserId);
        String userIdStr = toString(userId);

        // 2. Redis添加关注关系 + 一键设置过期时间（合并操作，简化代码）
        Long followResult = stringRedisTemplate.opsForSet().add(followKey, followedUserIdStr);
        if (followResult != null && followResult > 0) {
            // 关注列表 + 粉丝列表同时设置过期时间（链式调用，一行搞定）
            stringRedisTemplate.opsForSet().add(fansKey, userIdStr);
            stringRedisTemplate.expire(followKey, EXPIRE_DAYS, TimeUnit.DAYS);
            stringRedisTemplate.expire(fansKey, EXPIRE_DAYS, TimeUnit.DAYS);
            return true;
        }
        return false;
    }

    @Override
    public Boolean unfollow(Long userId, Long followedUserId) {
        // 1. 参数校验
        if (userId == null || followedUserId == null || userId.equals(followedUserId)) {
            return false;
        }

        String followKey = cacheKeyBuilder.buildUserFollowCacheKey(userId);
        String fansKey = cacheKeyBuilder.buildUserFansCacheKey(followedUserId);
        String followedUserIdStr = toString(followedUserId);
        String userIdStr = toString(userId);

        // 2. Redis移除关注关系（简化判断逻辑）
        Long unfollowResult = stringRedisTemplate.opsForSet().remove(followKey, followedUserIdStr);
        if (unfollowResult != null && unfollowResult > 0) {
            stringRedisTemplate.opsForSet().remove(fansKey, userIdStr);
            return true;
        }
        return false;
    }

    @Override
    public Boolean isFollowing(Long userId, Long followedUserId) {
        // 1. 参数校验 + Redis判断（一行简化）
        if (userId == null || followedUserId == null || userId.equals(followedUserId)) {
            return false;
        }
        return Boolean.TRUE.equals(stringRedisTemplate.opsForSet().isMember(
                cacheKeyBuilder.buildUserFollowCacheKey(userId), toString(followedUserId)
        ));
    }

    @Override
    public Set<String> getFollowList(Long userId, long limit) {
        // 简化空值判断 + 返回默认空集合
        return userId == null || limit <= 0 ? Collections.emptySet() :
                stringRedisTemplate.opsForSet().distinctRandomMembers(cacheKeyBuilder.buildUserFollowCacheKey(userId), limit);
    }

    @Override
    public Set<String> getFansList(Long userId, long limit) {
        return userId == null || limit <= 0 ? Collections.emptySet() :
                stringRedisTemplate.opsForSet().distinctRandomMembers(cacheKeyBuilder.buildUserFollowCacheKey(userId), limit);
    }

    @Override
    public Long getFollowCount(Long userId) {
        // 简化空值处理（一行返回）
        return userId == null ? 0L :
                Optional.ofNullable(stringRedisTemplate.opsForSet().size(cacheKeyBuilder.buildUserFollowCacheKey(userId))).orElse(0L);
    }

    @Override
    public Long getFansCount(Long userId) {
        return userId == null ? 0L :
                Optional.ofNullable(stringRedisTemplate.opsForSet().size(cacheKeyBuilder.buildUserFollowCacheKey(userId))).orElse(0L);
    }



    /**
     * Long转String（避免空指针）
     */
    private String toString(Long value) {
        return value == null ? "" : value.toString();
    }
}