package com.tianji.learning.controller;


import com.tianji.common.domain.dto.PageDTO;
import com.tianji.learning.domain.dto.QuestionFormDTO;
import com.tianji.learning.domain.dto.ReplyDTO;
import com.tianji.learning.domain.query.QuestionPageQuery;
import com.tianji.learning.domain.query.ReplyPageQuery;
import com.tianji.learning.domain.vo.QuestionVO;
import com.tianji.learning.domain.vo.ReplyVO;
import com.tianji.learning.service.IInteractionReplyService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * <p>
 * 互动问题的回答或评论 前端控制器
 * </p>
 *
 * @author fsq
 * @since 2023-10-23
 */
@Api(tags = "评论相关接口")
@RestController
@RequestMapping("/replies")
@RequiredArgsConstructor
public class InteractionReplyController {

    private final IInteractionReplyService replyService;

    @ApiOperation("新增评论")
    @PostMapping
    public void saveReply(@RequestBody @Validated ReplyDTO dto){
        replyService.saveReply(dto);
    }

    @ApiOperation("分页查询互动评论-用户端")
    @GetMapping("page")
    public PageDTO<ReplyVO> queryReplyVoPage(ReplyPageQuery query){
        return replyService.queryReplyPage(query,false);
    }



}
