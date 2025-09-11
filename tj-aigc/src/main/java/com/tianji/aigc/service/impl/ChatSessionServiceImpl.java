package com.tianji.aigc.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollStreamUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.LocalDateTimeUtil;
import cn.hutool.core.stream.StreamUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.tianji.aigc.domain.ChatRecord;
import com.tianji.aigc.domain.ChatSession;
import com.tianji.aigc.domain.vo.AgentConfigVO;
import com.tianji.aigc.domain.vo.ChatSessionVO;
import com.tianji.aigc.domain.vo.MessageVO;
import com.tianji.aigc.domain.vo.SessionVO;
import com.tianji.aigc.enums.MessageTypeEnum;
import com.tianji.aigc.mapper.ChatRecordMapper;
import com.tianji.aigc.mapper.ChatSessionMapper;
import com.tianji.aigc.memory.MyAssistantMessage;
import com.tianji.aigc.service.AgentConfigService;
import com.tianji.aigc.service.ChatService;
import com.tianji.aigc.service.ChatSessionService;
import com.tianji.aigc.utils.MessageDelayPersistHandler;
import com.tianji.common.utils.UserContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.MessageType;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatSessionServiceImpl extends ServiceImpl<ChatSessionMapper, ChatSession> implements ChatSessionService {

    private final AgentConfigService agentConfigService;
    private final ChatMemory chatMemory;
    private final ChatRecordMapper chatRecordMapper;
    private final MessageDelayPersistHandler messageDelayPersistHandler;

    @Override
    public SessionVO createSession(Integer num) {
        try {
            // 获取智能体配置
            AgentConfigVO agentConfig = agentConfigService.getAgentConfig();
            if (agentConfig == null) {
                log.warn("未获取到智能体配置，使用默认值创建会话");
                return createDefaultSession(num);
            }

            // 检查是否存在空会话
            String emptySessionId = findEmptySession();
            if (StrUtil.isNotBlank(emptySessionId)) {
                // 查询空会话并返回
                ChatSession session = getOne(Wrappers.<ChatSession>lambdaQuery()
                        .eq(ChatSession::getSessionId, emptySessionId));
                SessionVO sessionVO = SessionVO.builder()
                        .sessionId(emptySessionId)
                        .title(agentConfig.getTitle())
                        .describe(agentConfig.getDescribe())
                        .examples(RandomUtil.randomEleList(agentConfig.getHotQuestions(), num))
                        .build();
                return sessionVO;
            }

            // 没有空会话则创建新会话
            SessionVO sessionVO = SessionVO.builder()
                    .sessionId(IdUtil.fastSimpleUUID())
                    .title(agentConfig.getTitle())
                    .describe(agentConfig.getDescribe())
                    .examples(RandomUtil.randomEleList(agentConfig.getHotQuestions(), num))
                    .build();

            var chatSession = ChatSession.builder()
                    .sessionId(sessionVO.getSessionId())
                    .userId(UserContext.getUser())
                    .createTime(LocalDateTimeUtil.now())
                    .updateTime(LocalDateTimeUtil.now())
                    .build();
            super.save(chatSession);

            return sessionVO;
        } catch (JsonProcessingException e) {
            log.error("创建会话时获取智能体配置失败", e);
            return createDefaultSession(num);
        }
    }

    /**
     * 获取热门会话
     *
     * @return 热门会话列表
     */
    @Override
    public List<AgentConfigVO.Example> hotExamples(Integer num) {
        try {
            AgentConfigVO agentConfig = agentConfigService.getAgentConfig();
            if (agentConfig != null && CollUtil.isNotEmpty(agentConfig.getHotQuestions())) {
                return RandomUtil.randomEleList(agentConfig.getHotQuestions(), num);
            }
        } catch (JsonProcessingException e) {
            log.error("获取热门问题失败", e);
        }
        return List.of();
    }

//    @Override
//    public List<MessageVO> queryBySessionId(String sessionId) {
//        // 根据会话ID获取对话ID
//        String conversationId = ChatService.getConversationId(sessionId);
//        // 从Redis中获取历史消息
//        List<Message> messageList = this.chatMemory.get(conversationId);
//        // 过滤并转换消息列表
//        return StreamUtil.of(messageList)
//                // 过滤掉非用户消息和助手消息
//                .filter(message -> message.getMessageType() == MessageType.ASSISTANT || message.getMessageType() == MessageType.USER)
//                // 转换为MessageVO对象
//                .map(message -> {
//                    if (message instanceof MyAssistantMessage) {
//                        return MessageVO.builder()
//                                .content(message.getText())
//                                .type(MessageTypeEnum.valueOf(message.getMessageType().name()))
//                                .params(((MyAssistantMessage) message).getParams())
//                                .build();
//                    }
//                    return MessageVO.builder()
//                            .content(message.getText())
//                            .type(MessageTypeEnum.valueOf(message.getMessageType().name()))
//                            .build();
//                })
//                .toList();
//    }

    @Override
    public List<MessageVO> queryBySessionId(String sessionId) {
        // 根据会话ID获取对话ID
        String conversationId = ChatService.getConversationId(sessionId);
        Long userId = UserContext.getUser();

        // 立即将Redis中的数据落库
        messageDelayPersistHandler.forcePersist(conversationId, userId);

        // 从MySQL查询历史记录
        List<ChatRecord> records = chatRecordMapper.selectList(Wrappers.<ChatRecord>lambdaQuery()
                .eq(ChatRecord::getConversationId, conversationId)
                .orderByAsc(ChatRecord::getCreateTime));

        // 转换为MessageVO列表
        return StreamUtil.of(records)
                .map(record -> {
                    MessageVO messageVO = JSONUtil.toBean(record.getData(), MessageVO.class);
                    return messageVO;
                })
                .toList();
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
        var userId = UserContext.getUser();
        // 查询历史会话，限制返回条数
        var list = super.lambdaQuery()
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
        var chatSessionVOS = CollStreamUtil.toList(list, chatSession ->
                ChatSessionVO.builder()
                        .sessionId(chatSession.getSessionId())
                        .title(chatSession.getTitle())
                        .updateTime(chatSession.getUpdateTime())
                        .build()
        );

        final var TODAY = "当天";
        final var LAST_30_DAYS = "最近30天";
        final var LAST_YEAR = "最近1年";
        final var MORE_THAN_YEAR = "1年以上";

        // 当前时间
        var now = LocalDateTime.now().toLocalDate();

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
        var queryWrapper = Wrappers.<ChatSession>lambdaQuery()
                .eq(ChatSession::getSessionId, sessionId)
                .eq(ChatSession::getUserId, UserContext.getUser());
        super.remove(queryWrapper);

        //删除redis中的数据
        var conversationId = ChatService.getConversationId(sessionId);
        this.chatMemory.clear(conversationId);
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
     * 检查是否存在未使用的空会话
     */
    private String findEmptySession() {
        Long userId = UserContext.getUser();
        // 查询未设置标题且创建时间较近的会话（视为空会话）
        List<ChatSession> emptySessions = lambdaQuery()
                .eq(ChatSession::getUserId, userId)
                .isNull(ChatSession::getTitle)
                .orderByAsc(ChatSession::getCreateTime)
                .last("LIMIT 1")
                .list();

        return CollUtil.isNotEmpty(emptySessions) ? emptySessions.get(0).getSessionId() : null;
    }

    /**
     * 创建默认会话（当获取不到智能体配置时使用）
     */
    private SessionVO createDefaultSession(Integer num) {
        // 使用预设的默认智能体配置
        SessionVO sessionVO = SessionVO.builder()
                .sessionId(IdUtil.fastSimpleUUID())
                .title("Hello，我是智慧MOOC学习助理")
                .describe("我是由智慧MOOC倾力打造的智能助理，我不仅能推荐课程、答疑解惑，还能为您激发创意、畅聊心事。")
                .examples(List.of(
                        new AgentConfigVO.Example("课程推荐", "能帮我推荐一个合适的课吗？"),
                        new AgentConfigVO.Example("课程购买", "我想要报名《天津中德职业规划课》"),
                        new AgentConfigVO.Example("知识讲解", "ArrayList和LinkedList有何区别？")
                ))
                .examples(RandomUtil.randomEleList(List.of(
                        new AgentConfigVO.Example("课程推荐", "能帮我推荐一个合适的课吗？"),
                        new AgentConfigVO.Example("课程购买", "我想要报名《天津中德职业规划课》"),
                        new AgentConfigVO.Example("知识讲解", "ArrayList和LinkedList有何区别？")
                ), num))
                .build();

        var chatSession = ChatSession.builder()
                .sessionId(sessionVO.getSessionId())
                .userId(UserContext.getUser())
                .createTime(LocalDateTimeUtil.now())
                .updateTime(LocalDateTimeUtil.now())
                .build();
        super.save(chatSession);

        return sessionVO;
    }
}