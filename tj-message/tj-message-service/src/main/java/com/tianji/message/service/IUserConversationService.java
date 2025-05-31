package com.tianji.message.service;

import cn.hutool.db.PageResult;
import com.baomidou.mybatisplus.extension.service.IService;
import com.tianji.common.domain.dto.PageDTO;
import com.tianji.message.domain.po.NoticeTemplate;
import com.tianji.message.domain.po.UserConversation;
import com.tianji.message.domain.query.UserConversationQuery;
import com.tianji.message.domain.vo.UserConversationVO;

/**
 * @author fsq
 * @date 2025/5/21 19:02
 */
public interface IUserConversationService  extends IService<UserConversation> {

    UserConversation getConversationByUserId(Long user1Id, Long user2Id);

    PageDTO<UserConversationVO> getConversationList(UserConversationQuery query);

    void deleteConversation(Long conversationId);

    void blockConversation(Long conversationId);

    void unBlockConversation(Long conversationId);

    void createConversation(Long user1Id, Long user2Id);
}
