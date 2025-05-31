package com.tianji.message.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tianji.message.domain.po.ChatMessage;
import org.apache.ibatis.annotations.Param;
import java.util.List;

public interface ChatMessageMapper extends BaseMapper<ChatMessage> {
    List<ChatMessage> selectHistoryMessages(
            @Param("chatId") Long chatId,
            @Param("type") Integer type,
            @Param("before") Long before,
            @Param("size") Integer size);
}