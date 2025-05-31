package com.tianji.message.domain.query;

import com.tianji.common.domain.query.PageQuery;
import lombok.Data;

import java.io.Serializable;

/**
 * 用户会话记录查询条件
 */
@Data
public class UserConversationQuery extends PageQuery implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 会话状态（0=正常，1=已屏蔽）
     */
    private Integer status;

    /**
     * 是否有未读消息（true=有未读，false=无未读）
     */
    private Boolean hasUnread;
}    