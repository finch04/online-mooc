package com.tianji.live.controller;
import com.tianji.live.domain.po.LiveRoom;
import com.tianji.live.domain.vo.LiveRoomDetailVO;
import com.tianji.live.domain.vo.LiveRoomStatVO;
import com.tianji.live.domain.vo.LiveRoomVO;
import com.tianji.live.service.ILiveRoomService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

/**
 * @Author: fsq
 * @Date: 2025/6/5 16:03
 * @Version: 1.0
 */

@RestController
@Tag(name = "直播管理")
@RequestMapping("/live")
public class LiveController {

    @Resource
    private ILiveRoomService liveRoomService;

    @Operation(summary = "直播间查询接口")
    @GetMapping("/{id}")
    public LiveRoomDetailVO getLiveRoomById(@PathVariable Long id) throws IOException {
        return liveRoomService.getRoomById(id);
    }

    @Operation(summary = "直播间列表查询接口")
    @GetMapping("/list")
    public List<LiveRoomVO> getLiveRoomList()  {
        return liveRoomService.getLiveRoomList();
    }

    /**
     * [轮询接口]获取直播间相关统计信息 (实时)
     * @param roomId
     */
    @Operation(summary ="获取直播间相关统计信息")
    @GetMapping("/stat/{roomId}")
    public LiveRoomStatVO getStat(@PathVariable String roomId) {
        return liveRoomService.getStat(roomId);
    }


    /**
     * 给直播间点赞
     * @param roomId
     */
    @Operation(summary ="直播间点赞")
    @PostMapping ("/like/{roomId}")
    public Long like(@PathVariable String roomId) {
        return liveRoomService.like(roomId);
    }

}
