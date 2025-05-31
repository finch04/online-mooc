package com.tianji.message.controller;

import com.tianji.common.domain.dto.PageDTO;
import com.tianji.message.domain.dto.UserPrivateMessageFormDTO;
import com.tianji.message.domain.query.UserPrivateMessageQuery;
import com.tianji.message.domain.vo.UserPrivateMessageVO;
import com.tianji.message.service.IUserPrivateMessageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

/**
 * 用户私信控制器
 */
@Tag(name = "用户私信管理")
@RestController
@RequestMapping("/messages")
@RequiredArgsConstructor
public class UserPrivateMessageController {

    private final IUserPrivateMessageService userPrivateMessageService;

    /**
     * 发送私信
     * @param userPrivateMessageFormDTO 私信内容
     * @return 发送结果
     */
    @Operation(summary = "发送私信")
    @PostMapping
    public Boolean sendMessage(@RequestBody @Valid UserPrivateMessageFormDTO userPrivateMessageFormDTO) {
        // 业务逻辑：保存消息到数据库，更新会话信息，推送消息给接收方
        return userPrivateMessageService.sendMessage(userPrivateMessageFormDTO);
    }

    /**
     * 查询与指定用户的历史聊天记录
     * @param query 查询条件
     * @return 分页消息列表
     */
    @Operation(summary = "查询历史聊天记录")
    @GetMapping
    public PageDTO<UserPrivateMessageVO> getMessageHistory(@Valid UserPrivateMessageQuery query) {
        // 业务逻辑：查询两个用户之间的消息记录，按时间排序
        return  userPrivateMessageService.getMessageHistory(query);
    }



    /**
     * 删除消息（逻辑删除）
     * @param messageIds 消息ID列表
     * @return 操作结果
     */
    @Operation(summary = "删除消息")
    @DeleteMapping
    public void deleteMessages(@RequestBody List<Long> messageIds) {
        // 业务逻辑：批量标记消息为已删除
        return ;
    }


}    