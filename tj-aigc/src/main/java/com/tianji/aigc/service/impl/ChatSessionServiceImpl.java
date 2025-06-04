package com.tianji.aigc.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.RandomUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tianji.aigc.config.SessionProperties;
import com.tianji.aigc.entity.ChatSession;
import com.tianji.aigc.mapper.ChatSessionMapper;
import com.tianji.aigc.service.ChatSessionService;
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
    public SessionVO createSession(Integer num) {
        SessionVO sessionVO = BeanUtil.toBean(sessionProperties, SessionVO.class);
        //随机生成指定数量的热门问题
        sessionVO.setExamples(RandomUtil.randomEleList(sessionVO.getExamples(), num));

        // 生成会话id
        sessionVO.setSessionId(IdUtil.simpleUUID());

        // 将会话数据保存到数据
        ChatSession chatSession = ChatSession.builder()
                .sessionId(sessionVO.getSessionId())
                .userId(UserContext.getUser())
                .build();
        super.save(chatSession);

        return sessionVO;
    }

    /**
     * 获取热门会话
     *
     * @return 热门会话列表
     */
    @Override
    public List<SessionVO.Example> hotExamples(Integer num) {
        return RandomUtil.randomEleList(sessionProperties.getExamples(), num);
    }

}
