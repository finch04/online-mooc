package com.tianji.message.domain.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 用户会话表数据传输对象
 */
@Data
public class UserConversationDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 会话ID
     */
    private Long id;

    /**
     * 参与用户1
     */
    private Long userId1;

    /**
     * 参与用户2
     */
    private Long userId2;

    /**
     * 最后一条消息ID
     */
    private Long lastMessageId;

    /**
     * 用户1的未读数量
     */
    private Integer unreadCount1;

    /**
     * 用户2的未读数量
     */
    private Integer unreadCount2;

    /**
     * 最后更新时间
     */
    private String lastUpdateTime;

    /**
     * 会话状态（0=正常，1=已屏蔽，2=已删除）
     */
    private Integer status;
}    