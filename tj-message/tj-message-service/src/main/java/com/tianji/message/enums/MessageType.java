package com.tianji.message.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum MessageType {
    TEXT(0, "文本"),
    IMG(1, "图片"),
    AUDIO(2, "语音"),
    FILE(3, "文件"),
    ;

    private final int value;
    private final String desc;
}
