package com.tianji.message.domain.query;

import com.tianji.common.domain.query.PageQuery;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 用户私信历史记录查询条件
 */
@Data
public class UserPrivateMessageQuery extends PageQuery implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 对方用户ID
     */
    private Long otherUserId;

    /**
     * 已读状态（0=未读，1=已读）
     */
    private Integer isRead;
}    