package com.tianji.message.domain.po;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 用户会话表实体类
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("user_conversation")
public class UserConversation implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 会话id
     */
    @TableId(value = "id", type = IdType.ASSIGN_ID)
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
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime lastUpdateTime;

    /**
     * 会话状态（0=正常，1=已屏蔽，2=已删除）
     */
    private Integer status;
}    