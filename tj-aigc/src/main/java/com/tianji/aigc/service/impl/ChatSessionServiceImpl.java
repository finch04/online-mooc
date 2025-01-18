package com.tianji.aigc.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.IdUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tianji.aigc.config.SessionProperties;
import com.tianji.aigc.entity.ChatSession;
import com.tianji.aigc.mapper.ChatSessionMapper;
import com.tianji.aigc.service.ChatSessionService;
import com.tianji.aigc.vo.ChatSessionVO;
import com.tianji.aigc.vo.SessionVO;
import com.tianji.common.utils.UserContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ChatSessionServiceImpl extends ServiceImpl<ChatSessionMapper, ChatSession> implements ChatSessionService {

    private final SessionProperties sessionProperties;

    @Override
    public SessionVO createSession() {
        SessionVO sessionVO = BeanUtil.toBean(sessionProperties, SessionVO.class);
        // 随机生成sessionId
        sessionVO.setSessionId(IdUtil.fastSimpleUUID());

        // 构建持久化对象，并持久化
        ChatSession chatSession = ChatSession.builder()
                .sessionId(sessionVO.getSessionId())
                .userId(UserContext.getUser())
                .build();
        super.save(chatSession);

        return sessionVO;
    }

    @Override
    public void updateTitle(String sessionId, String title) {
        //更新数据
        super.lambdaUpdate()
                .set(ChatSession::getTitle, title)
                .eq(ChatSession::getSessionId, sessionId)
                .eq(ChatSession::getUserId, UserContext.getUser())
                .isNull(ChatSession::getTitle)
                .update();
    }

    @Override
    public List<ChatSessionVO> queryHistorySession() {
        return super.lambdaQuery()
                .eq(ChatSession::getUserId, UserContext.getUser())
                .isNotNull(ChatSession::getTitle)
                .orderByDesc(ChatSession::getCreateTime)
                .last("LIMIT 100") // 限制返回条数，避免数据量过大，最多返回100条数据
                .list()
                .stream()
                .map(chatSession -> ChatSessionVO.builder()
                        .sessionId(chatSession.getSessionId())
                        .title(chatSession.getTitle())
                        .build())
                .toList();
    }
}
