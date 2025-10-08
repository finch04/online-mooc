package com.tianji.live.handler;

import com.tianji.live.constants.IMConstants;
import com.tianji.live.domain.po.LiveRoom;
import com.tianji.live.mapper.LiveRoomMapper;
import com.tianji.live.service.impl.ChatBusiServiceImpl;
import com.tianji.live.utils.IMCacheKeyBuilder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * @Author: fsq
 * @Date: 2025/10/7 12:56
 * @Version: 1.0
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class ScheduledSyncLikeHandler {

    private Logger logger = LoggerFactory.getLogger(ChatBusiServiceImpl.class);
    private final StringRedisTemplate stringRedisTemplate;
    private final LiveRoomMapper liveRoomMapper;
    private final IMCacheKeyBuilder imCacheKeyBuilder;

    /**
     * 定时同步Redis中的点赞数到MySQL（每天凌晨3点执行，避开业务高峰）
     * 逻辑：查询所有直播间的Redis点赞数，对比数据库值，更新差异
     */
    @Scheduled(cron = "0 0 3 * * ?") // Cron表达式：每天3点执行
    @Transactional // 事务保证，避免更新一半失败
    public void syncLikeCountToDb() {
        try {
            // 1. 查询所有直播间（可优化为分页查询，避免数据量过大）
            List<LiveRoom> allRooms = liveRoomMapper.selectList(null);
            if (allRooms.isEmpty()) {
                log.info("同步点赞数：无直播间数据，无需同步");
                return;
            }

            // 2. 批量处理每个直播间的点赞数同步
            int syncCount = 0;
            for (LiveRoom room : allRooms) {
                Long roomId = room.getId();
                String roomIdStr = roomId.toString();
                String likeCountKey =  imCacheKeyBuilder.buildRoomLikeCountKey(roomIdStr);

                // 2.1 获取Redis中的最新点赞数
                String redisLikeCountStr = stringRedisTemplate.opsForValue().get(likeCountKey);
                if (redisLikeCountStr == null) {
                    continue; // Redis无数据，说明近期无点赞，无需同步
                }
                Long redisLikeCount = Long.parseLong(redisLikeCountStr);

                // 2.2 对比数据库值：Redis值 > 数据库值才更新（避免覆盖数据库最新值）
                Long dbLikeCount = room.getLikeCount();
                if (redisLikeCount > dbLikeCount) {
                    // 2.3 更新数据库
                    LiveRoom updateRoom = new LiveRoom();
                    updateRoom.setId(roomId);
                    updateRoom.setLikeCount(redisLikeCount);
                    liveRoomMapper.updateById(updateRoom);

                    syncCount++;
                    log.debug("同步点赞数：直播间[{}]，数据库旧值[{}]，Redis新值[{}]",
                            roomId, dbLikeCount, redisLikeCount);
                }
            }

            log.info("点赞数同步到MySQL完成，共同步[{}]个直播间", syncCount);
        } catch (Exception e) {
            log.error("点赞数同步到MySQL失败", e);
            // 可选：同步失败可重试（如用定时任务重试机制，避免单次失败丢失数据）
        }
    }
}
