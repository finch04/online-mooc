package com.tianji.aigc.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.tianji.aigc.entity.ChatSession;
import com.tianji.aigc.vo.ChatSessionVO;
import com.tianji.aigc.vo.SessionVO;

import java.util.List;

public interface ChatSessionService extends IService<ChatSession> {

    /**
     * 创建会话session
     *
     * @return 会话信息
     */
    SessionVO createSession();

    /**
     * 更新会话标题
     *
     * @param sessionId 会话id
     * @param title     标题
     */
    void updateTitle(String sessionId, String title);

    /**
     * 查询历史会话列表
     */
    List<ChatSessionVO> queryHistorySession();
}
