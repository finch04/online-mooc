package com.tianji.message.domain.po;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 用户私信表实体类
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("user_private_message")
public class UserPrivateMessage implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 私聊消息id
     */
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 发送者ID
     */
    private Long senderId;

    /**
     * 接收者ID
     */
    private Long receiverId;

    /**
     * 消息内容
     */
    private String content;

    /**
     * 消息类型（0=文本，1=图片，2=语音，3=文件）
     */
    private Integer messageType;

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

    /**
     * 删除标记（0=未删除，1=已删除）
     */
    @TableLogic
    private Integer deleteFlag;

}    