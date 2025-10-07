package com.tianji.live.controller;

import com.tianji.common.exceptions.CommonException;
import com.tianji.common.utils.UserContext;
import com.tianji.live.domain.dto.FollowDTO;
import com.tianji.live.domain.vo.LiveRoomDetailVO;
import com.tianji.live.domain.vo.LiveRoomVO;
import com.tianji.live.service.ILiveRoomService;
import com.tianji.live.service.IUserFollowService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

/**
 * @Author: fsq
 * @Date: 2025/6/5 16:03
 * @Version: 1.0
 */

@RestController
@Tag(name = "关注管理")
@RequestMapping("/follow")
public class UserFollowController {

    @Resource
    private IUserFollowService userFollowService;

    @PostMapping
    @Operation(tags = "关注或取消关注")
    public void follow(@RequestBody FollowDTO followDTO) {
        Long userId = UserContext.getUser();
        if(userId==null){
            throw new CommonException("请登录再关注主播");
        }
        if(followDTO.isFollow()){
             userFollowService.follow(userId,followDTO.getAnchorId());
        }else{
             userFollowService.unfollow(userId,followDTO.getAnchorId());
        }
    }
}
