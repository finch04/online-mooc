package com.tianji.message.domain.dto;

import lombok.Data;

@Data
public class PrivateMessageDTO {
    private String senderId;     // 发送者ID
    private String recipientId; // 接收者ID
    private String content;     // 消息内容
    private Long timestamp;    // 时间戳
}