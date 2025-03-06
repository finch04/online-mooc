package com.tianji.aigc.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollStreamUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.ListUtil;
import cn.hutool.core.date.LocalDateTimeUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tianji.aigc.config.SessionProperties;
import com.tianji.aigc.entity.ChatSession;
import com.tianji.aigc.mapper.ChatSessionMapper;
import com.tianji.aigc.service.ChatService;
import com.tianji.aigc.service.ChatSessionService;
import com.tianji.aigc.vo.ChatSessionVO;
import com.tianji.aigc.vo.SessionVO;
import com.tianji.common.utils.UserContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatSessionServiceImpl extends ServiceImpl<ChatSessionMapper, ChatSession> implements ChatSessionService {

    private final SessionProperties sessionProperties;
    private final ChatMemory chatMemory;

    @Override
    public SessionVO createSession(Integer num) {
        SessionVO sessionVO = BeanUtil.toBean(sessionProperties, SessionVO.class);
        // 随机获取examples
        sessionVO.setExamples(RandomUtil.randomEleList(sessionProperties.getExamples(), num));

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
    public List<SessionVO.Example> hotExamples(Integer num) {
        return RandomUtil.randomEleList(sessionProperties.getExamples(), num);
    }

    @Override
    public void updateTitle(String sessionId, String title) {
        //更新数据
        super.lambdaUpdate()
                // 设置更新条件, 更新字段为title(最多设置前100个字符)，更新条件为sessionId和userId
                .set(ChatSession::getTitle, StrUtil.sub(title, 0, 100))
                .eq(ChatSession::getSessionId, sessionId)
                .eq(ChatSession::getUserId, UserContext.getUser())
                .update();
    }

    /**
     * 异步更新聊天会话的标题
     *
     * @param sessionId 会话ID，用于标识特定的聊天会话
     * @param title     新的会话标题，如果为空则不进行更新
     * @param userId    用户ID
     */
    @Async
    @Override
    public void update(String sessionId, String title, Long userId) {
        // 查询符合条件的聊天会话列表
        List<ChatSession> list = super.lambdaQuery()
                .eq(ChatSession::getSessionId, sessionId)
                .eq(ChatSession::getUserId, userId)
                .list();
        // 如果列表为空，直接返回，无需进一步处理
        if (CollUtil.isEmpty(list)) {
            return;
        }

        // 获取列表中的第一个聊天会话实例
        ChatSession chatSession = list.get(0);
        // 如果聊天会话的标题为空，并且新标题不为空，则更新标题
        if (StrUtil.isEmpty(chatSession.getTitle()) && !StrUtil.isEmpty(title)) {
            chatSession.setTitle(StrUtil.sub(title, 0, 100));
        }
        // 设置更新字段为updateTime为当前时间
        chatSession.setUpdateTime(LocalDateTimeUtil.now());
        // 更新数据库中的聊天会话信息
        super.updateById(chatSession);
    }

    @Override
    public Map<String, List<ChatSessionVO>> queryHistorySession() {
        Long userId = UserContext.getUser();
        // 查询历史会话，限制返回条数
        List<ChatSession> list = super.lambdaQuery()
                .eq(ChatSession::getUserId, UserContext.getUser())
                .isNotNull(ChatSession::getTitle)
                .orderByDesc(ChatSession::getUpdateTime)
                .last("LIMIT 30")
                .list();

        if (CollUtil.isEmpty(list)) {
            log.info("No chat sessions found for user: {}", userId);
            return Map.of();
        }


        // 转换为 ChatSessionVO 列表
        List<ChatSessionVO> chatSessionVOS = CollStreamUtil.toList(list, chatSession ->
                ChatSessionVO.builder()
                        .sessionId(chatSession.getSessionId())
                        .title(chatSession.getTitle())
                        .updateTime(chatSession.getUpdateTime())
                        .build()
        );

        final String TODAY = "当天";
        final String LAST_30_DAYS = "最近30天";
        final String LAST_YEAR = "最近1年";
        final String MORE_THAN_YEAR = "1年以上";

        // 当前时间
        LocalDate now = LocalDateTime.now().toLocalDate();

        // 按照更新时间分组
        return CollStreamUtil.groupByKey(chatSessionVOS, vo -> {
            // 计算两个日期之间的天数差
            long between = Math.abs(ChronoUnit.DAYS.between(vo.getUpdateTime().toLocalDate(), now));
            if (between == 0) {
                return TODAY;
            } else if (between <= 30) {
                return LAST_30_DAYS;
            } else if (between <= 365) {
                return LAST_YEAR;
            } else {
                return MORE_THAN_YEAR;
            }
        });
    }

    @Override
    public void deleteHistorySession(String sessionId) {
        //删除数据库的数据
        LambdaQueryWrapper<ChatSession> queryWrapper = Wrappers.<ChatSession>lambdaQuery()
                .eq(ChatSession::getSessionId, sessionId)
                .eq(ChatSession::getUserId, UserContext.getUser());
        super.remove(queryWrapper);

        //删除redis中的数据
        String conversationId = ChatService.getConversationId(sessionId);
        this.chatMemory.clear(conversationId);
    }
}
