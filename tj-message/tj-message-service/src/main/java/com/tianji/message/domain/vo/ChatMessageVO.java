package com.tianji.message.domain.vo;/**
 * @author fsq
 * @date 2025/5/28 9:18
 */

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * @Author: fsq
 * @Date: 2025/5/28 9:18
 * @Version: 1.0
 */
@Data
public class ChatMessageVO {

    /**
     * 消息ID
     */
    private Long id;

    /**
     * 发送者ID
     */
    private Long senderId;

    /**
     * 发送者名字
     */
    private String senderName;

    /**
     * 发送者头像
     */
    private String senderIcon;

    /**
     * 目标ID（接收者ID或群ID）
     */
    private Long targetId;

    /**
     * 目标姓名
     */
    private String targetName;

    /**
     * 消息内容
     */
    private String content;

    /**
     * 消息类型：1-私聊 2-群聊
     */
    private Integer messageType;

    /**
     * 发送时间（精确到毫秒）
     */
    private LocalDateTime sentAt;

    /**
     * 消息状态：1-已发送 2-已接收 3-已读
     */
    private Integer status;
}
