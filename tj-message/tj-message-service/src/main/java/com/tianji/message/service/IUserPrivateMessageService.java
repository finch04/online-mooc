package com.tianji.message.service;

import com.tianji.common.domain.dto.PageDTO;
import com.tianji.message.domain.dto.UserPrivateMessageFormDTO;
import com.tianji.message.domain.po.UserPrivateMessage;
import com.tianji.message.domain.query.UserPrivateMessageQuery;
import com.tianji.message.domain.vo.UserPrivateMessageVO;

/**
 * @author fsq
 * @date 2025/5/21 19:03
 */
public interface IUserPrivateMessageService {
    Boolean sendMessage(UserPrivateMessageFormDTO userPrivateMessageFormDTO);

    PageDTO<UserPrivateMessageVO> getMessageHistory(UserPrivateMessageQuery query);

    //根据用户id得到与其最后一条消息
    UserPrivateMessage getLastMessage(Long otherUserId);

    //根据对方用户id得到未读数
    Integer getUnreadCount(Long otherUserId);

    //得到自己总的私信未读数
    Integer getAllUnreadCountBySelf(Long userId);
}
