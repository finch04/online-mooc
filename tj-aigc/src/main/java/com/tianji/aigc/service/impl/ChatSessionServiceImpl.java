package com.tianji.aigc.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollStreamUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.date.LocalDateTimeUtil;
import cn.hutool.core.stream.StreamUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tianji.aigc.config.SessionProperties;
import com.tianji.aigc.config.SystemPromptConfig;
import com.tianji.aigc.constants.Constant;
import com.tianji.aigc.entity.ChatSession;
import com.tianji.aigc.enums.MessageTypeEnum;
import com.tianji.aigc.mapper.ChatSessionMapper;
import com.tianji.aigc.memory.MyAssistantMessage;
import com.tianji.aigc.service.ChatService;
import com.tianji.aigc.service.ChatSessionService;
import com.tianji.aigc.vo.ChatSessionVO;
import com.tianji.aigc.vo.MessageVO;
import com.tianji.aigc.vo.SessionVO;
import com.tianji.common.utils.UserContext;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.AbstractChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.MessageType;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ChatSessionServiceImpl extends ServiceImpl<ChatSessionMapper, ChatSession> implements ChatSessionService {

    private final SessionProperties sessionProperties;

    private final ChatMemory chatMemory;
    private final ChatClient chatClient;
    private final SystemPromptConfig systemPromptConfig;
    private final ChatModel chatModel;

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

    @Override
    public List<MessageVO> queryBySessionId(String sessionId) {
        String conversationId = ChatService.getConversationId(sessionId);
        List<Message> messageList = chatMemory.get(conversationId, AbstractChatMemoryAdvisor.DEFAULT_CHAT_MEMORY_RESPONSE_SIZE);
//        CollUtil.reverse(messageList);
        return StreamUtil.of(messageList)
                .filter(message ->
                     message.getMessageType() == MessageType.USER || message.getMessageType() == MessageType.ASSISTANT
                )
                .map(message -> {
                    if(message instanceof MyAssistantMessage myAssistantMessage){
                        return MessageVO.builder()
                                .type(MessageTypeEnum.valueOf(message.getMessageType().name()))
                                .content(message.getText())
                                .params(myAssistantMessage.getParams())
                                .build();
                    }
                    return MessageVO.builder()
                            .type(MessageTypeEnum.valueOf(message.getMessageType().name()))
                            .content(message.getText())
                            .build();
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
        //查询符合条件的聊天记录
        ChatSession chatSession = this.lambdaQuery()
                .eq(ChatSession::getSessionId, sessionId)
                .eq(ChatSession::getUserId, userId)
                .one();

        if(ObjectUtil.isEmpty(chatSession)){
            return;
        }
        //原来的标题字段为空，现在对话的标题不为空
        if(StrUtil.isEmpty(chatSession.getTitle()) && StrUtil.isNotEmpty(title)){
            String userText = StrUtil.format("""
                    对话内容:
                    {}
                    """,title);
            String titleContent = ChatClient.builder(this.chatModel)
                    .build()
                    .prompt()
                    .system(this.systemPromptConfig.getChatTitleMessage().get())
                    .user(userText)
                    .call()
                    .content();
            chatSession.setTitle(titleContent);
//            chatSession.setTitle(StrUtil.sub(title,0,100));
        }
        // 设置更新字段为updateTime为当前时间
        chatSession.setUpdateTime(LocalDateTimeUtil.now());
        // 更新数据库中的聊天会话信息
        super.updateById(chatSession);

    }

    @Async
    @Override
    public void autoUpdate(String sessionId, String title, Long userId) {
        //查询符合条件的聊天记录
        ChatSession chatSession = this.lambdaQuery()
                .eq(ChatSession::getSessionId, sessionId)
                .eq(ChatSession::getUserId, userId)
                .one();

        if(ObjectUtil.isEmpty(chatSession)){
            return;
        }
        //原来的标题字段为空，现在对话的标题不为空
        if(StrUtil.isNotEmpty(title) && ObjectUtil.isEmpty(chatSession.getTitle())){
            chatSession.setTitle(StrUtil.sub(title,0,100));
        }
        // 设置更新字段为updateTime为当前时间
        chatSession.setUpdateTime(LocalDateTimeUtil.now());
        // 更新数据库中的聊天会话信息
        super.updateById(chatSession);

    }

    @Override
    public Map<String, List<ChatSessionVO>> queryHistorySession() {
        Long userId = UserContext.getUser();

        //查询符合条件的ChatSession
        List<ChatSession> list = super.lambdaQuery()
                .eq(ChatSession::getUserId, userId)
                .isNotNull(ChatSession::getTitle)
                .orderByDesc(ChatSession::getUpdateTime)
                .last("LIMIT 30")
                .list();

        if(CollUtil.isEmpty(list)){
            return Map.of();
        }

        //转换为ChatSessionVO
        List<ChatSessionVO> chatSessionVOS = CollStreamUtil.toList(list, chatSession -> ChatSessionVO.builder()
                .sessionId(chatSession.getSessionId())
                .title(chatSession.getTitle())
                .updateTime(chatSession.getUpdateTime())
                .build()
        );

        //定义时间
        final String TODAY = "当天";
        final String LAST_30_DAYS = "最近30天";
        final String LAST_YEAR = "最近1年";
        final String MORE_THAN_YEAR = "1年以上";


        //当前日期
        LocalDate now = LocalDateTime.now().toLocalDate();

        return CollStreamUtil.groupByKey(chatSessionVOS, chatSessionVO -> {
            long between = Math.abs(ChronoUnit.DAYS.between(chatSessionVO.getUpdateTime().toLocalDate(), now));//计算时间差
            if(between == 0){
                return TODAY;
            }else if (between <= 30){
                return LAST_30_DAYS;
            }else if (between <= 365){
                return LAST_YEAR;
            }else{
                return MORE_THAN_YEAR;
            }
        });

    }

    @Override
    public void deleteHistorySession(String sessionId) {

        LambdaQueryWrapper<ChatSession> lambdaQuery = new LambdaQueryWrapper<>();
                lambdaQuery.eq(ChatSession::getSessionId, sessionId)
                           .eq(ChatSession::getUserId, UserContext.getUser());
        //删除数据库中的会话信息
        super.remove(lambdaQuery);

        //删除数据库中的历史记录
        String conversationId = ChatService.getConversationId(sessionId);
        this.chatMemory.clear(conversationId);
    }

    @Override
    public void updateTitle(String sessionId, String title) {
        //查询出符合条件的那一条历史会话
        ChatSession chatSession = super.lambdaQuery()
                .eq(ChatSession::getSessionId, sessionId)
                .one();
        //设置历史会话的标题
        chatSession.setTitle(title);

        super.updateById(chatSession);

    }


    /**
     * 根据第一个回答动态生成标题
     * @param sessionId
     */
    @Override
    public void autoUpdateTitle(String sessionId,String question) {
        var userId = UserContext.getUser();
        String autoTitle = chatClient.prompt()
                .system(promptSystem -> promptSystem.text(this.systemPromptConfig.getChatSystemMessage().get()).param("now",DateUtil.now()).param("message",question))
                .user("帮我根据用户问题总结一个标题出来")
                .call()
                .content();
        this.autoUpdate(sessionId,autoTitle,userId);
    }


    /**
     * 根据所有的对话动态生成最新标题
     * @param sessionId
     */
    @Override
    public void autoUpdateTitle1(String sessionId) {
        var userId = UserContext.getUser();
        List<MessageVO> messageVOS = this.queryBySessionId(sessionId);
        String autoTitle = chatClient.prompt()
                .system(promptSystem -> promptSystem.text(this.systemPromptConfig.getChatSystemMessage().get()).param("now",DateUtil.now()).param("message",messageVOS))
                .user("帮我根据对话总结一个标题出来")
                .call()
                .content();
        this.autoUpdate(sessionId,autoTitle,userId);
    }

}
