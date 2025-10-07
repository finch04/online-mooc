package com.tianji.live.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.tianji.api.client.user.UserClient;
import com.tianji.api.dto.user.UserDTO;
import com.tianji.common.utils.BeanUtils;
import com.tianji.common.utils.UserContext;
import com.tianji.live.domain.vo.LiveRoomDetailVO;
import com.tianji.live.manager.ConnectionManager;
import com.tianji.live.mapper.LiveRoomMapper;
import com.tianji.live.domain.po.LiveRoom;
import com.tianji.live.domain.vo.LiveRoomVO;
import com.tianji.live.service.ILiveRoomService;
import com.tianji.live.service.IUserFollowService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @Author: fsq
 * @Date: 2025/10/3 17:00
 * @Version: 1.0
 */
@Service
@RequiredArgsConstructor
public class LiveRoomServiceImpl implements ILiveRoomService {

    private final LiveRoomMapper liveRoomMapper;
    private final UserClient userClient;
    private final IUserFollowService userFollowService;

    @Override
    public List<LiveRoomVO> getLiveRoomList() {
        List<LiveRoom> liveRooms = liveRoomMapper.selectList(null);
        return liveRooms.stream()
                .map(liveRoom -> {
                    LiveRoomVO liveRoomVO = BeanUtils.copyBean(liveRoom, LiveRoomVO.class);
                    UserDTO dto = userClient.queryUserById(liveRoomVO.getAnchorId());
                    liveRoomVO.setAnchorName(dto.getName());
                    liveRoomVO.setAnchorIcon(dto.getIcon());
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
        //判断用户是否已经关注
        if(UserContext.getUser()!=null){
            vo.setFollowed(userFollowService.isFollowing(UserContext.getUser() ,liveRoom.getAnchorId()));
        }else{
            vo.setFollowed(false);
        }
        vo.setFansCount(userFollowService.getFansCount(liveRoom.getAnchorId()));
        vo.setOnlineCount(ConnectionManager.getRoomUserCount(roomId.toString()));
        return vo;
    }


    @Override
    public int getOnlineCount(String roomId) {
       return ConnectionManager.getRoomUserCount(roomId);
    }


}
