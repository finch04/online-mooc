package com.tianji.aigc.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.stream.StreamUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.RandomUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tianji.aigc.config.SessionProperties;
import com.tianji.aigc.constants.Constant;
import com.tianji.aigc.entity.ChatSession;
import com.tianji.aigc.enums.MessageTypeEnum;
import com.tianji.aigc.mapper.ChatSessionMapper;
import com.tianji.aigc.memory.MyAssistantMessage;
import com.tianji.aigc.service.ChatService;
import com.tianji.aigc.service.ChatSessionService;
import com.tianji.aigc.vo.MessageVO;
import com.tianji.aigc.vo.SessionVO;
import com.tianji.common.utils.UserContext;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.advisor.AbstractChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.MessageType;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ChatSessionServiceImpl extends ServiceImpl<ChatSessionMapper, ChatSession> implements ChatSessionService {

    private final SessionProperties sessionProperties;

    private final ChatMemory chatMemory;

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
        CollUtil.reverse(messageList);
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

}
