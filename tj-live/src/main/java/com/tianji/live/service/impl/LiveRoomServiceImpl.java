package com.tianji.live.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.tianji.api.client.user.UserClient;
import com.tianji.api.dto.user.UserDTO;
import com.tianji.common.exceptions.CommonException;
import com.tianji.common.utils.BeanUtils;
import com.tianji.common.utils.UserContext;
import com.tianji.live.constants.IMConstants;
import com.tianji.live.domain.vo.LiveRoomDetailVO;
import com.tianji.live.domain.vo.LiveRoomStatVO;
import com.tianji.live.manager.ConnectionManager;
import com.tianji.live.mapper.LiveRoomMapper;
import com.tianji.live.domain.po.LiveRoom;
import com.tianji.live.domain.vo.LiveRoomVO;
import com.tianji.live.service.ILiveRoomService;
import com.tianji.live.service.IUserFollowService;
import com.tianji.live.utils.IMCacheKeyBuilder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @Author: fsq
 * @Date: 2025/10/3 17:00
 * @Version: 1.0
 */
@Service
@RequiredArgsConstructor
@Slf4j // 日志注解，用于打印同步日志
public class LiveRoomServiceImpl implements ILiveRoomService {

    private final LiveRoomMapper liveRoomMapper;
    private final UserClient userClient;
    private final IUserFollowService userFollowService;
    private final StringRedisTemplate stringRedisTemplate;
    private final IMCacheKeyBuilder imCacheKeyBuilder;


    @Override
    public List<LiveRoomVO> getLiveRoomList() {
        List<LiveRoom> liveRooms = liveRoomMapper.selectList(null);
        return liveRooms.stream()
                .map(liveRoom -> {
                    LiveRoomVO liveRoomVO = BeanUtils.copyBean(liveRoom, LiveRoomVO.class);
                    UserDTO dto = userClient.queryUserById(liveRoomVO.getAnchorId());
                    liveRoomVO.setAnchorName(dto.getName());
                    liveRoomVO.setAnchorIcon(dto.getIcon());
                    // 补充：列表查询时返回实时在线人数（可选，根据需求决定是否添加）
                    liveRoomVO.setMaxOnlineCount(getMaxOnlineCount(liveRoom.getId().toString()));
                    // 补充：列表查询时返回实时点赞数（可选，根据需求决定是否添加）
                    liveRoomVO.setLikeCount(getLikeCount(liveRoom.getId().toString()));
                    return liveRoomVO;
                })
                .toList();
    }

    @Override
    public LiveRoomDetailVO getRoomById(Long roomId) {
        LiveRoom liveRoom = liveRoomMapper.selectById(roomId);
        LiveRoomDetailVO vo = BeanUtils.copyBean(liveRoom, LiveRoomDetailVO.class);
        UserDTO dto = userClient.queryUserById(liveRoom.getAnchorId());
        vo.setAnchorName(dto.getName());
        vo.setAnchorIcon(dto.getIcon());

        // 判断用户是否已关注
        if (UserContext.getUser() != null) {
            vo.setFollowed(userFollowService.isFollowing(UserContext.getUser(), liveRoom.getAnchorId()));
        } else {
            vo.setFollowed(false);
        }

        vo.setFansCount(userFollowService.getFansCount(liveRoom.getAnchorId()));
        vo.setOnlineCount(ConnectionManager.getRoomUserCount(roomId.toString()));
        // 获取直播间历史最高在线人数
        vo.setMaxOnlineCount(getMaxOnlineCount(roomId.toString()));
        // 查询实时点赞数（替换原数据库查询，改为Redis优先）
        vo.setLikeCount(getLikeCount(roomId.toString()));
        return vo;
    }

    /**
     * 获取直播间实时在线人数（注意，这是高频调用的方法，尽量不要查库）
     * @param roomId 直播间ID
     * @return 直播间实时在线人数
     */
    @Override
    public LiveRoomStatVO getStat(String roomId) {
        LiveRoomStatVO vo = new LiveRoomStatVO();
        vo.setOnlineCount(ConnectionManager.getRoomUserCount(roomId));
        vo.setMaxOnlineCount(getMaxOnlineCount(roomId));
        vo.setLikeCount(getLikeCount(roomId));
        //这里就不更新主播粉丝数据，否则需要根据roomId查库得到主播id，导致性能下降
        return vo;
    }

    /**
     * 直播间点赞（支持同一用户多次点赞，直接计数）
     * @param roomId 直播间ID（String类型，适配前端传参习惯）
     * @return 点赞后的总点赞数
     */
    @Override
    public Long like(String roomId) {
        // 1. 校验用户登录（可选，根据需求决定是否允许匿名点赞）
        if (UserContext.getUser() == null) {
            throw new CommonException("请先登录再操作"); // 或返回特定错误码，根据全局异常处理调整
        }

        // 2. 构建Redis Key（直播间点赞数Key）
        String likeCountKey = imCacheKeyBuilder.buildRoomLikeCountKey(roomId);

        // 3. 原子自增点赞数（Redis INCR命令，性能极高，支持高频调用）
        Long newLikeCount = stringRedisTemplate.opsForValue().increment(likeCountKey);

        // 4. 设置缓存过期时间（首次点赞时设置，后续自增不重复设置，避免性能损耗）
        if (newLikeCount != null && newLikeCount == 1) {
            stringRedisTemplate.expire(likeCountKey, IMConstants.LIKE_CACHE_EXPIRE_DAYS, TimeUnit.DAYS);
        }

        // 5. 返回点赞后的总点赞数（给前端实时展示）
        return newLikeCount != null ? newLikeCount : 0;
    }


    /**
     * 查询直播间实时点赞数（Redis优先，无数据则查数据库并同步到Redis）
     * @param roomId 直播间ID
     * @return 实时总点赞数
     */
    public Long getLikeCount(String roomId) {
        // 1. 先查Redis
        String likeCountKey = imCacheKeyBuilder.buildRoomLikeCountKey(roomId);
        String likeCountStr = stringRedisTemplate.opsForValue().get(likeCountKey);

        // 2. Redis有数据：直接返回
        if (likeCountStr != null) {
            return Long.parseLong(likeCountStr);
        }

        // 3. Redis无数据：查数据库，并同步到Redis（避免后续查询再查库）
        LiveRoom liveRoom = liveRoomMapper.selectById(roomId);
        Long dbLikeCount = liveRoom != null ? liveRoom.getLikeCount() : 0;

        // 4. 同步到Redis（并设置过期时间）
        stringRedisTemplate.opsForValue().set(likeCountKey, dbLikeCount.toString(),
                IMConstants.LIKE_CACHE_EXPIRE_DAYS, TimeUnit.DAYS);

        // 5. 返回数据库查询结果
        return dbLikeCount;
    }

    /**
     * 获取直播间的最高在线人数（从Redis或数据库）
     */
    public int getMaxOnlineCount(String roomId) {
        // 1. 先查Redis
        String maxOnlineKey = imCacheKeyBuilder.buildRoomMaxOnlineKey(roomId);
        String maxOnlineStr = stringRedisTemplate.opsForValue().get(maxOnlineKey);
        if (maxOnlineStr != null) {
            return Integer.parseInt(maxOnlineStr);
        }

        // 2. Redis没有则查数据库，并同步到Redis
        LiveRoom room = liveRoomMapper.selectById(roomId);
        int maxOnline = room != null && room.getMaxOnlineCount() != null ? room.getMaxOnlineCount() : 0;

        stringRedisTemplate.opsForValue().set(
                maxOnlineKey,
                String.valueOf(maxOnline),
                7,
                TimeUnit.DAYS
        );

        return maxOnline;
    }

}