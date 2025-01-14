package com.tianji.aigc.enums;

import com.tianji.common.enums.BaseEnum;
import lombok.Getter;

/**
 * 消息类型枚举
 */
@Getter
public enum MessageTypeEnum implements BaseEnum {
    USER(1, "用户提问"), ASSISTANT(2, "AI的回答");

    private final int value;
    private final String desc;

    MessageTypeEnum(int value, String desc) {
        this.value = value;
        this.desc = desc;
    }

    @Override
    public String toString() {
        return this.name();
    }
}
