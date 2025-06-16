package com.tianji.aigc.enums;

import com.tianji.common.enums.BaseEnum;

public enum MessageTypeEnum implements BaseEnum {

    USER(1,"用户提问"),
    ASSISTANT(2,"AI的回答");

    private final int value;
    private final String desc;

    MessageTypeEnum(int value, String desc){
        this.value = value;
        this.desc = desc;
    }

    @Override
    public int getValue() {
        return 0;
    }

    @Override
    public String getDesc() {
        return "";
    }
}
