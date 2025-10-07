package com.tianji.live.service;

import com.tianji.live.domain.po.LiveRoom;
import com.tianji.live.domain.vo.LiveRoomDetailVO;
import com.tianji.live.domain.vo.LiveRoomStatVO;
import com.tianji.live.domain.vo.LiveRoomVO;

import java.util.List;

public interface ILiveRoomService {

    LiveRoomDetailVO getRoomById(Long roomId);

    List<LiveRoomVO> getLiveRoomList();

    Long like(String roomId);

    LiveRoomStatVO getStat(String roomId);
}
