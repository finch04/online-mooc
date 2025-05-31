package com.tianji.message.domain.vo;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 用户会话表视图对象
 */
@Data
public class UserConversationVO implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 会话ID
     */
    private Long id;

    /**
     * 对方用户ID
     */
    private Long otherUserId;

    /**
     * 对方用户名
     */
    private String otherUsername;

    /**
     * 对方用户头像
     */
    private String otherAvatar;

    /**
     * 最后一条消息内容
     */
    private String lastMessage;

    /**
     * 最后一条消息时间
     */
    private LocalDateTime lastMessageTime;

    /**
     * 未读消息数
     */
    private Integer unReadCount;

    /**
     * 会话状态（0=正常，1=已屏蔽，2=已删除）
     */
    private Integer status;
}    