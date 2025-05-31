package com.tianji.message.service;

import com.tianji.common.domain.dto.PageDTO;
import com.tianji.message.domain.dto.ChatMessageDTO;
import com.tianji.message.domain.po.ChatMessage;
import com.tianji.message.domain.query.ChatHistoryQuery;
import com.tianji.message.domain.vo.ChatMessageVO;

import java.util.List;

public interface IChatMessageService {
    Long sendMessage(ChatMessageDTO messageDTO);
    PageDTO<ChatMessageVO> getHistoryMessages(ChatHistoryQuery query);
    void markMessagesAsRead(List<Long> messageIds);
}