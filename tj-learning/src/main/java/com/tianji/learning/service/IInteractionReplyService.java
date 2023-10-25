package com.tianji.learning.service;

import com.tianji.common.domain.dto.PageDTO;
import com.tianji.learning.domain.dto.ReplyDTO;
import com.tianji.learning.domain.po.InteractionReply;
import com.baomidou.mybatisplus.extension.service.IService;
import com.tianji.learning.domain.query.ReplyPageQuery;
import com.tianji.learning.domain.vo.QuestionVO;
import com.tianji.learning.domain.vo.ReplyVO;

/**
 * <p>
 * 互动问题的回答或评论 服务类
 * </p>
 *
 * @author fsq
 * @since 2023-10-23
 */
public interface IInteractionReplyService extends IService<InteractionReply> {

    void saveReply(ReplyDTO dto);

    void hiddenReply(Long id, Boolean hidden);

    PageDTO<ReplyVO> queryReplyPage(ReplyPageQuery pageQuery, boolean forAdmin);

    ReplyVO queryReplyById(Long id);
}
