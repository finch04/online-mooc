package com.tianji.aigc.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatSessionVO {

    /**
     * 会话id
     */
    private String sessionId;

    /**
     * 会话标题
     */
    private String title;

}
