package com.tianji.message.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author fsq
 * @date 2025/5/21 19:19
 */
@AllArgsConstructor
@Getter
public enum MessageStatus {
    UNREAD(0, "未读"),
    READ(1, "已读"),

    //消息状态
    PIN(1, "置顶消息"),

    //会话状态
    SHIELD(1, "已屏蔽"),
    DELETED(2,"已删除")
    ;

    private final int value;
    private final String desc;
}
