package com.tianji.aigc.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatEventVO {
    //聊天返回的文本数据
    private Object eventData;
    //聊天返回的数据类型
    private int eventType;
}
