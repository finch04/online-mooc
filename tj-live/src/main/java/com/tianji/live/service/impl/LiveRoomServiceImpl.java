package com.tianji.live.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.tianji.api.client.user.UserClient;
import com.tianji.api.dto.user.UserDTO;
import com.tianji.common.utils.BeanUtils;
import com.tianji.live.mapper.LiveRoomMapper;
import com.tianji.live.domain.po.LiveRoom;
import com.tianji.live.domain.vo.LiveRoomVO;
import com.tianji.live.service.ILiveRoomService;
import lombok.RequiredArgsConstructor;
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

    @Override
    public LiveRoom getRoomById(Long roomId) {
        return liveRoomMapper.selectById(roomId);
    }

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
}
