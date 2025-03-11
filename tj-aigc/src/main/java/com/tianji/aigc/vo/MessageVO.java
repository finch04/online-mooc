package com.tianji.aigc.vo;

import com.tianji.aigc.enums.MessageTypeEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MessageVO {

    /**
     * 消息类型，USER表示用户提问，ASSISTANT表示AI的回答
     */
    private MessageTypeEnum type;
    /**
     * 消息内容
     */
    private String content;

    /**
     * 附加参数
     */
    private Map<String, Object> params;

}
