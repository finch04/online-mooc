package com.tianji.live.handler;

import com.tianji.live.domain.po.UserFollow;
import com.tianji.live.mapper.UserFollowMapper;
import com.tianji.live.utils.IMCacheKeyBuilder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;


/**
 * @Author: fsq
 * @Date: 2025/10/7 11:13
 * @Version: 1.0
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class ScheduledSyncFollowHandler {

    private final StringRedisTemplate stringRedisTemplate;
    private final UserFollowMapper userFollowMapper;
    private final IMCacheKeyBuilder imCacheKeyBuilder;


    // ======================== 定期同步Redis到MySQL（定时任务） ========================
    /**
     * 定时同步Redis关注关系到MySQL（每天凌晨2点执行，避开业务高峰）
     * 逻辑：1. 扫描Redis中所有关注Key 2. 批量查询关注关系 3. 幂等插入MySQL
     */
    @Scheduled(cron = "0 0 2 * * ?") // cron表达式：每天2点执行
    public void syncRedisToMysql() {
        try {
            // 1. 扫描Redis中所有用户的关注Key（匹配前缀：user:follow:*）
            Set<String> followKeys = stringRedisTemplate.keys(imCacheKeyBuilder.buildUserFollowCachePattern());
            if (CollectionUtils.isEmpty(followKeys)) {
                return; // 无数据直接返回
            }

            // 2. 批量处理每个用户的关注关系
            List<UserFollow> followList = new ArrayList<>();
            for (String followKey : followKeys) {
                // 2.1 提取用户ID（从Key中截取：user:follow:123 → 123）
                Long userId =imCacheKeyBuilder.parseUserIdFromCacheKey(followKey);
                // 2.2 获取该用户的所有关注ID（Redis Set全部元素）
                Set<String> followedUserIdStrs = stringRedisTemplate.opsForSet().members(followKey);
                if (CollectionUtils.isEmpty(followedUserIdStrs)) {
                    continue;
                }

                // 2.3 转换为UserFollow实体（准备批量插入）
                List<UserFollow> userFollows = followedUserIdStrs.stream()
                        .map(followedUserIdStr -> new UserFollow(userId, Long.valueOf(followedUserIdStr)))
                        .collect(Collectors.toList());
                followList.addAll(userFollows);
            }

            // 3. 批量同步到MySQL（幂等处理：避免重复插入，依赖数据库唯一索引uk_user_followed）
            if (!CollectionUtils.isEmpty(followList)) {
                // 分批插入（避免SQL过长，每批500条）
                int batchSize = 500;
                for (int i = 0; i < followList.size(); i += batchSize) {
                    int end = Math.min(i + batchSize, followList.size());
                    List<UserFollow> batchList = followList.subList(i, end);
                    try {
                        userFollowMapper.insert(batchList);
                    } catch (Exception e) {
                        // 捕获唯一索引冲突异常（已存在的关注关系忽略）
                        log.warn("批量插入关注关系到MySQL时忽略重复数据，批次：{}", i / batchSize + 1, e);
                    }
                }
            }

            log.info("Redis关注关系同步MySQL完成，同步总数：{}", followList.size());
        } catch (Exception e) {
            log.error("Redis关注关系同步MySQL失败", e);
        }
    }

}
