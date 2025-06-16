package com.tianji.aigc.vo;

import com.tianji.aigc.enums.MessageTypeEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;


@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MessageVO {
    private MessageTypeEnum type;
    private String content;
    private Map<String,Object> params;

}
