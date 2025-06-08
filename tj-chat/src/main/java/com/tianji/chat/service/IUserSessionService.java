package com.tianji.chat.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.tianji.chat.domain.dto.UserSessionDTO;
import com.tianji.chat.domain.po.ChatSession;
import com.tianji.chat.domain.po.UserSession;
import com.tianji.common.domain.query.PageQuery;
import reactor.core.publisher.Flux;

import java.util.List;

/**
 * <p>
 * 聊天对话的每个片段记录（分片存储） 服务类
 * </p>
 *
 * @author lusy
 * @since 2025-05-06
 */
public interface IUserSessionService extends IService<UserSession> {


    UserSession createUserSession(UserSessionDTO dto);

    void deleteUserSession(Long id);

    List<UserSession> getUserSessionList();

    void updateUserSession( Long id,UserSessionDTO dto);
}
