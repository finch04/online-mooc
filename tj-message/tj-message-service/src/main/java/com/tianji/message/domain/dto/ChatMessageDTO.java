package com.tianji.message.domain.dto;

import lombok.Data;

@Data
public class ChatMessageDTO {
    private Long senderId;
    private String content;
    private Integer messageType; // 1-私聊 2-群聊
    private Long targetId; // 接收者ID或群ID
}