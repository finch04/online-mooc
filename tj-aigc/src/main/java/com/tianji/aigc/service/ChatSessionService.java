package com.tianji.aigc.service;

import com.tianji.aigc.vo.ChatSessionVO;
import com.tianji.aigc.vo.MessageVO;
import com.tianji.aigc.vo.SessionVO;

import java.util.List;
import java.util.Map;

public interface ChatSessionService {

    /**
     * 创建会话session
     *
     * @param num 热门问题的数量
     * @return 会话信息
     */
    SessionVO createSession(Integer num);

    /**
     * 获取热门会话
     *
     * @return 热门会话列表
     */
    List<SessionVO.Example> hotExamples(Integer num);

    /**
     * 根据会话id查询消息列表
     *
     * @param sessionId 会话id
     * @return 消息列表
     */
    List<MessageVO> queryBySessionId(String sessionId);

    /**
     * 更新会话更新时间
     *
     * @param sessionId 会话ID，用于标识特定的聊天会话
     * @param title     新的会话标题，如果为空则不进行更新
     * @param userId    用户ID
     */
    void update(String sessionId, String title, Long userId);

    /**
     * 查询历史会话列表
     */
    Map<String, List<ChatSessionVO>> queryHistorySession();

    /**
     * 删除历史会话
     *
     * @param sessionId 会话id
     */
    void deleteHistorySession(String sessionId);

    /**
     * 更新历史会话标题
     *
     * @param sessionId 会话id
     * @param title     标题
     */
    void updateTitle(String sessionId, String title);
}
