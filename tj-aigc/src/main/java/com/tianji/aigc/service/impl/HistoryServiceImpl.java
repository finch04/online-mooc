package com.tianji.aigc.service.impl;

import cn.hutool.core.collection.CollStreamUtil;
import cn.hutool.core.stream.StreamUtil;
import com.tianji.aigc.enums.MessageTypeEnum;
import com.tianji.aigc.memory.RedisChatMemory;
import com.tianji.aigc.service.ChatService;
import com.tianji.aigc.service.HistoryService;
import com.tianji.aigc.vo.MessageVO;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.MessageType;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class HistoryServiceImpl implements HistoryService {

    private final RedisChatMemory redisChatMemory;

    // 历史消息数量，默认1000条
    public static final int HISTORY_MESSAGE_COUNT = 1000;

    /**
     * 根据会话ID查询历史消息
     * <p>
     * 本方法首先根据会话ID获取对话ID，然后从Redis中获取该对话的历史消息
     * 过滤出其中的用户消息和助手消息，并将其转换为MessageVO对象返回
     *
     * @param sessionId 会话ID，用于标识一个对话会话
     * @return 返回转换后的消息列表，包含用户消息和助手消息
     */
    @Override
    public List<MessageVO> queryHistoryMessage(String sessionId) {
        // 根据会话ID获取对话ID
        String conversationId = ChatService.getConversationId(sessionId);
        // 从Redis中获取历史消息
        List<Message> messageList = this.redisChatMemory.get(conversationId, HISTORY_MESSAGE_COUNT);
        // 过滤并转换消息列表
        return StreamUtil.of(messageList)
                // 过滤掉非用户消息和助手消息
                .filter(message -> message.getMessageType() == MessageType.ASSISTANT || message.getMessageType() == MessageType.USER)
                // 转换为MessageVO对象
                .map(message -> MessageVO.builder()
                        .content(message.getContent())
                        .type(MessageTypeEnum.valueOf(message.getMessageType().name()))
                        .build())
                .toList();
    }
}
