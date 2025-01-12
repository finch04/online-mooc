package com.tianji.learning.controller;

import com.tianji.common.domain.dto.PageDTO;
import com.tianji.learning.domain.query.ReplyPageQuery;
import com.tianji.learning.domain.vo.ReplyVO;
import com.tianji.learning.service.IInteractionReplyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 问题管理相关接口
 */
@RestController
@RequestMapping("/admin/replies")
@Tag(name = "问题管理相关接口")
@RequiredArgsConstructor
public class InteractionReplyAdminController {

    private final IInteractionReplyService replyService;

    @Operation(summary = "隐藏或显示评论")
    @PutMapping("/{id}/hidden/{hidden}")
    public void hiddenReply(
            @Parameter(description = "问题id", example = "1") @PathVariable("id") Long id,
            @Parameter(description = "是否隐藏，true/false", example = "true") @PathVariable("hidden") Boolean hidden
    ) {
        replyService.hiddenReply(id, hidden);
    }

    @Operation(summary = "分页查询回答或评论")
    @GetMapping("page")
    public PageDTO<ReplyVO> queryReplyPage(ReplyPageQuery pageQuery) {
        return replyService.queryReplyPage(pageQuery, true);
    }

    @Operation(summary = "根据id查询回答或评论")
    @GetMapping("{id}")
    public ReplyVO queryReplyById(
            @Parameter(description = "问题id", example = "1") @PathVariable("id") Long id
    ) {
        return replyService.queryReplyById(id);
    }
}
