package com.tianji.chat.service.impl;

import cn.hutool.core.lang.UUID;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tianji.chat.config.AiConfig;
import com.tianji.chat.domain.dto.UserSessionDTO;
import com.tianji.chat.domain.po.ChatSession;
import com.tianji.chat.domain.po.UserSession;
import com.tianji.chat.mapper.ChatSessionMapper;
import com.tianji.chat.mapper.UserSessionMapper;
import com.tianji.chat.service.IChatSessionService;
import com.tianji.chat.service.IUserSessionService;
import com.tianji.common.utils.UserContext;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.chat.StreamingChatLanguageModel;
import dev.langchain4j.model.output.Response;
import dev.langchain4j.store.embedding.EmbeddingStore;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;

import java.util.List;

/**
 * <p>
 * 聊天对话的每个片段记录（分片存储） 服务实现类
 * </p>
 *
 * @author lusy
 * @since 2025-05-06
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class UserSessionServiceImpl extends ServiceImpl<UserSessionMapper, UserSession> implements IUserSessionService {

    private final IChatSessionService chatSessionService;

    @Override
    public void createUserSession(UserSessionDTO dto) {
        UserSession userSession = new UserSession();
        userSession.setUserId(dto.getUserId());
        userSession.setName(dto.getName());
        userSession.setTag(dto.getTag());
        userSession.setSessionId(UUID.randomUUID().toString());
        this.baseMapper.insert(userSession);
    }

    @Override
    public List<UserSession> getUserSessionList() {
        Long userId = UserContext.getUser();
        LambdaQueryWrapper<UserSession> wrapper =  new LambdaQueryWrapper<>();
        wrapper.eq(UserSession::getUserId, userId);
        wrapper.orderByDesc(UserSession::getCreateTime);
        return this.baseMapper.selectList(wrapper);
    }

    @Override
    @Transactional
    public void deleteUserSession(Long id) {
        UserSession userSession = this.getById(id);
        List<ChatSession> list = chatSessionService.lambdaQuery()
                .eq(ChatSession::getSessionId, userSession.getSessionId())
                .eq(ChatSession::getUserId, UserContext.getUser()).list();
        for (ChatSession chatSession : list) {
            chatSessionService.removeById(chatSession.getId());
        }
        this.baseMapper.deleteById(id);
    }

    @Override
    public void updateUserSession( Long id,UserSessionDTO dto) {
        UserSession session = getById(id);
        session.setName(dto.getName());
        session.setTag(dto.getTag());
        updateById(session);
    }
}
