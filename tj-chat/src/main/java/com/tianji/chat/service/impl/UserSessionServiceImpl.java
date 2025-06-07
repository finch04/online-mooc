package com.tianji.chat.service.impl;

import cn.hutool.core.lang.UUID;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tianji.chat.config.AiConfig;
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
    @Override
    public void createUserSession(Long userId) {
        UserSession userSession = new UserSession();
        userSession.setUserId(userId);
        userSession.setSessionId(UUID.randomUUID().toString());
        this.baseMapper.insert(userSession);
    }

    @Override
    public List<UserSession> getUserSessionList() {
        Long userId = UserContext.getUser();
        LambdaQueryWrapper<UserSession> wrapper =  new LambdaQueryWrapper<>();
        wrapper.eq(UserSession::getUserId, userId);
        wrapper.orderByDesc(UserSession::getCreatedAt);
        return this.baseMapper.selectList(wrapper);
    }

    @Override
    public void deleteUserSession(Long id) {
        this.baseMapper.deleteById(id);
    }
}
