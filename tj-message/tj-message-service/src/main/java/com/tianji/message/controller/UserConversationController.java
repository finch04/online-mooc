package com.tianji.message.controller;


import cn.hutool.db.PageResult;
import com.tianji.common.domain.dto.PageDTO;
import com.tianji.message.domain.dto.UserConversationDTO;
import com.tianji.message.domain.query.UserConversationQuery;
import com.tianji.message.domain.vo.UserConversationVO;
import com.tianji.message.service.IUserConversationService;
import io.swagger.annotations.Api;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

/**
 * <p>
 * 用户会话 前端控制器
 * </p>
 *
 * @author 虎哥
 * @since 2022-08-19
 */
@Api(tags = "用户会话接口")
@RestController
@RequestMapping("/conversations")
@RequiredArgsConstructor
public class UserConversationController {

    private final IUserConversationService conversationService;

    /**
     * 查询当前用户的会话列表
     * @param query 查询条件（当前用户ID、状态、是否有未读等）
     * @return 分页会话列表（包含对方用户信息、最新消息、未读数）
     */
    @Operation(summary = "查询会话列表")
    @GetMapping
    public PageDTO<UserConversationVO> getConversationList(@Valid UserConversationQuery query) {
        // 业务逻辑：根据当前用户ID查询会话列表，关联用户信息和最新消息
        return conversationService.getConversationList(query);
    }


    /**
     * 标记会话为已读
     * @param conversationId 会话ID
     * @return 操作结果
     */
    @Operation(summary = "标记会话为已读")
    @PatchMapping("/{conversationId}/read")
    public Boolean markConversationAsRead(@PathVariable Long conversationId) {
        // 业务逻辑：更新会话未读计数，标记所有消息为已读
        return true;
    }

    /**
     * 屏蔽会话
     * @param conversationId 会话ID
     * @return 操作结果
     */
    @Operation(summary = "屏蔽会话")
    @PutMapping("/block/{conversationId}")
    public Boolean blockConversation(@PathVariable Long conversationId) {
        conversationService.blockConversation(conversationId);
        return true;
    }

    /**
     * 取消屏蔽会话
     * @param conversationId 会话ID
     * @return 操作结果
     */
    @Operation(summary = "取消屏蔽会话")
    @PutMapping("/unblock/{conversationId}")
    public Boolean unBlockConversation(@PathVariable Long conversationId) {
        conversationService.unBlockConversation(conversationId);
        return true;
    }


    /**
     * 删除会话（逻辑删除）
     * @param conversationId 会话ID
     * @return 操作结果
     */
    @Operation(summary = "删除会话")
    @DeleteMapping("/{conversationId}")
    public void deleteConversation(@PathVariable Long conversationId) {
         conversationService.deleteConversation(conversationId);
    }

    /**
     * 查询当前用户的未读消息总数
     * @return 未读总数
     */
    @Operation(summary = "查询未读消息总数")
    @GetMapping("/unread-count")
    public Integer getUnreadTotal() {
        // 业务逻辑：汇总所有会话的未读计数
        return 0;
    }


}
