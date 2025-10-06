package com.tianji.live.controller;
import com.tianji.live.domain.po.LiveRoom;
import com.tianji.live.domain.vo.LiveRoomDetailVO;
import com.tianji.live.domain.vo.LiveRoomVO;
import com.tianji.live.service.ILiveRoomService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
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

    @GetMapping("/{id}")
    public LiveRoomDetailVO getLiveRoomById(@PathVariable Long id) throws IOException {
        return liveRoomService.getRoomById(id);
    }

    @Operation(summary = "直播间查询接口")
    @GetMapping("/list")
    public List<LiveRoomVO> getLiveRoomList()  {
        return liveRoomService.getLiveRoomList();
    }

    /**
     * 根据roomId获取直播间在线人数
     * @param roomId
     */
    @Operation(summary ="获取直播间在线人数")
    @GetMapping("/onlineCount/{roomId}")
    public int getOnlineCount(@PathVariable String roomId) {
        return liveRoomService.getOnlineCount(roomId);
    }

}
