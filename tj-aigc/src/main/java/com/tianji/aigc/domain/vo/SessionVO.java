package com.tianji.aigc.domain.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SessionVO {

    /**
     * 会话ID，用于唯一标识当前的AI助手会话。
     */
    private String sessionId;

    /**
     * AI助手的标题，用于显示助手的名称或身份。
     */
    private String title;

    /**
     * AI助手的描述，简要介绍助手的功能或特点。
     */
    private String describe;

    /**
     * 示例列表，包含一些使用助手的示例。
     */
    private List<AgentConfigVO.Example> examples;


}
