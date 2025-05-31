package com.tianji.message.domain.vo;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 用户私信表视图对象
 */
@Data
public class UserPrivateMessageVO implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 消息ID
     */
    private Long id;
    /**
     * 发送者ID
     */
    private Long senderId;
    /**
     * 发送者用户名
     */
    private String senderName;
    /**
     * 发送者头像
     */
    private String senderIcon;
    /**
     * 接收者ID
     */
    private Long receiverId;
    /**
     * 接收者用户名
     */
    private String receiverName;
    /**
     * 接收者头像
     */
    private String receiverIcon;
    /**
     * 消息内容
     */
    private String content;
    /**
     * 消息类型（0=文本，1=图片，2=语音，3=文件）
     */
    private Integer messageType;
    /**
     * 消息类型描述
     */
    private String messageTypeDesc;
    /**
     * 已读状态（0=未读，1=已读）
     */
    private Integer isRead;
    /**
     * 发送时间
     */
    private LocalDateTime pushTime;

    /**
     * 消息状态（0=正常消息，1=置顶消息）
     */
    private Integer status;
}    