package com.tianji.message.controller;


import com.tianji.common.domain.dto.PageDTO;
import com.tianji.message.domain.dto.UserInboxDTO;
import com.tianji.message.domain.query.UserInboxQuery;
import com.tianji.message.service.IUserInboxService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * <p>
 * 用户通知记录 前端控制器
 * </p>
 *
 * @author 虎哥
 * @since 2022-08-19
 */
@Api(tags = "用户收件箱接口")
@RestController
@RequestMapping("/inboxes")
@RequiredArgsConstructor
public class UserInboxController {

    private final IUserInboxService inboxService;


    @ApiOperation("查询未读消息数")
    @GetMapping("/unread")
    public Integer getUnReadCount(){
        return inboxService.getUnReadCount();
    }

    @ApiOperation("分页查询收件箱")
    @GetMapping
    public PageDTO<UserInboxDTO> queryUserInBoxesPage(UserInboxQuery query){
        return inboxService.queryUserInBoxesPage(query);
    }

    @ApiOperation("标记该消息已读")
    @PostMapping("/mark/{id}")
    public Boolean markMessageAsRead(@PathVariable("id") Long id){
        return inboxService.markMessageAsRead(id);
    }

    @ApiOperation("全部已读")
    @PostMapping("/markAll")
    public Boolean markAllMessagesAsRead(){
        return inboxService.markAllMessagesAsRead();
    }

    @ApiOperation("根据消息类型查询未读数")
    @GetMapping("/unread/{type}")
    public Integer getUnReadCountByType(@PathVariable("type") Integer type){
        return inboxService.getUnReadCountByType(type);
    }


}
