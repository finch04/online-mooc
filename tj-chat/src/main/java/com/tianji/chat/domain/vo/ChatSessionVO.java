package com.tianji.chat.domain.vo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

/**
 * <p>
 * 聊天对话的每个片段记录（分片存储）
 * </p>
 *
 * @author lusy
 * @since 2025-05-06
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
public class ChatSessionVO {

    /**
     * 聊天记录id
     */
    private Long id;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 会话ID
     */
    private String sessionId;

    /**
     * 会话片段序号，从0开始
     */
    private Integer segmentIndex;

    /**
     * 消息内容，JSON 格式，包含 role、type、text 等
     */
    private String content;

    /**
     * 插入时间
     */
    private LocalDateTime createdAt;


}
