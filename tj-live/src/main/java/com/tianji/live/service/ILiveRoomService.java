package com.tianji.live.service;

import com.tianji.live.domain.po.LiveRoom;
import com.tianji.live.domain.vo.LiveRoomVO;

import java.util.List;

public interface ILiveRoomService {

    LiveRoom getRoomById(Long roomId);

    List<LiveRoomVO> getLiveRoomList();
}
