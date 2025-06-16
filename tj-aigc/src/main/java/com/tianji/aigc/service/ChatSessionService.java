package com.tianji.aigc.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.tianji.aigc.entity.ChatSession;
import com.tianji.aigc.vo.MessageVO;
import com.tianji.aigc.vo.SessionVO;

import java.util.List;

public interface ChatSessionService extends IService<ChatSession> {

    /**
     * 创建会话
     *
     * @param num 热门问题的数量，默认是3
     * @return 会话信息
     */
    SessionVO createSession(Integer num);

    /**
     * 获取热门会话
     *
     * @return 热门会话列表
     */
    List<SessionVO.Example> hotExamples(Integer num);

    List<MessageVO> queryBySessionId(String sessionId);
}
