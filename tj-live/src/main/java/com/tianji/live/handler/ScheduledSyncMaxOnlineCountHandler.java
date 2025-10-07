package com.tianji.live.handler;

import com.tianji.live.domain.po.LiveRoom;
import com.tianji.live.mapper.LiveRoomMapper;
import com.tianji.live.service.ILiveRoomService;
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
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @Author: fsq
 * @Date: 2025/10/7 13:03
 * @Version: 1.0
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class ScheduledSyncMaxOnlineCountHandler {

    private Logger logger = LoggerFactory.getLogger(ChatBusiServiceImpl.class);
    private final StringRedisTemplate stringRedisTemplate;
    private final LiveRoomMapper liveRoomMapper;
    private final IMCacheKeyBuilder imCacheKeyBuilder;


    /**
     * 定期更新直播间最高在线人数（每10秒执行一次）
     * 频率可根据业务需求调整
     */
    @Scheduled(cron = "*/10 * * * * ?")
    public void updateMaxOnlineCount() {
        // 实际应用中可以通过扫描所有直播间ID来批量处理
        // 这里简化处理，假设我们有获取所有活跃直播间ID的方法
         List<String> activeRoomIds =  liveRoomMapper.selectList(null)
                 .stream().map(i->i.getId().toString()).collect(Collectors.toList());

        // 示例：处理单个直播间，实际应循环处理所有活跃直播间
         for (String roomId : activeRoomIds) {
             calculateAndUpdateMaxOnline(roomId);
         }
    }

    /**
     * 计算并更新单个直播间的最高在线人数
     * @param roomId 直播间ID
     */
    public void calculateAndUpdateMaxOnline(String roomId) {
        // 1. 获取当前在线人数
        int currentOnline = getCurrentOnlineCount(roomId);
        if (currentOnline <= 0) {
            return;
        }

        // 2. 获取Redis中存储的最高在线人数
        String maxOnlineKey = imCacheKeyBuilder.buildRoomMaxOnlineKey(roomId);
        String maxOnlineStr = stringRedisTemplate.opsForValue().get(maxOnlineKey);
        int maxOnline = maxOnlineStr != null ? Integer.parseInt(maxOnlineStr) : 0;

        // 3. 如果当前在线人数大于历史最高，更新Redis
        if (currentOnline > maxOnline) {
            stringRedisTemplate.opsForValue().set(
                    maxOnlineKey,
                    String.valueOf(currentOnline),
                    7,
                    TimeUnit.DAYS
            );
            log.debug("直播间[{}]最高在线人数更新为: {}", roomId, currentOnline);
        }
    }

    /**
     * 获取当前在线人数（复用已有方法）
     */
    private int getCurrentOnlineCount(String roomId) {
        String roomUserCacheKey = imCacheKeyBuilder.buildRoomUserCacheKey(roomId);
        Long size = stringRedisTemplate.opsForSet().size(roomUserCacheKey);
        return size != null ? size.intValue() : 0;
    }

    /**
     * 定时将Redis中的最高在线人数同步到MySQL（每天凌晨2点执行）
     */
    @Scheduled(cron = "0 0 2 * * ?")
    @Transactional
    public void syncMaxOnlineToDb() {
//         1. 获取所有直播间ID
         List<LiveRoom> allRooms = liveRoomMapper.selectList(null);

//         2. 循环同步每个直播间的最高在线人数
         for (LiveRoom room : allRooms) {
             String roomId = room.getId().toString();
             syncSingleRoomMaxOnline(roomId);
         }
    }

    /**
     * 同步单个直播间的最高在线人数到数据库
     */
    private void syncSingleRoomMaxOnline(String roomId) {
        // 1. 从Redis获取最高在线人数
        String maxOnlineKey = imCacheKeyBuilder.buildRoomMaxOnlineKey(roomId);
        String maxOnlineStr = stringRedisTemplate.opsForValue().get(maxOnlineKey);
        if (maxOnlineStr == null) {
            return;
        }
        int redisMaxOnline = Integer.parseInt(maxOnlineStr);

        // 2. 从数据库获取当前记录的最高在线人数
        LiveRoom room = liveRoomMapper.selectById(roomId);
        if (room == null) {
            return;
        }
        int dbMaxOnline = room.getMaxOnlineCount() != null ? room.getMaxOnlineCount() : 0;

        // 3. 如果Redis中的值更大，则更新数据库
        if (redisMaxOnline > dbMaxOnline) {
            LiveRoom updateRoom = new LiveRoom();
            updateRoom.setId(Long.parseLong(roomId));
            updateRoom.setMaxOnlineCount(redisMaxOnline);
            liveRoomMapper.updateById(updateRoom);
            log.debug("同步直播间[{}]最高在线人数到数据库: {}", roomId, redisMaxOnline);
        }
    }

}
