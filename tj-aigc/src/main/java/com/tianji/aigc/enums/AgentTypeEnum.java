package com.tianji.aigc.enums;

import cn.hutool.core.util.EnumUtil;
import lombok.Getter;

/**
 * 智能体类型
 */
@Getter
public enum AgentTypeEnum {
    ROUTE("ROUTE", "路由智能体"),
    RECOMMEND("RECOMMEND", "课程推荐智能体"),
    CONSULT("CONSULT", "课程咨询智能体"),
    BUY("BUY", "课程购买智能体"),
    KNOWLEDGE("KNOWLEDGE", "知识讲解智能体");

    private final String agentName;
    private final String desc;

    AgentTypeEnum(String agentName, String desc) {
        this.agentName = agentName;
        this.desc = desc;
    }

    @Override
    public String toString() {
        return this.name();
    }


    /**
     * 通过智能体的名称查找枚举
     */
    public static AgentTypeEnum agentNameOf(String agentName) {
        return EnumUtil.getBy(AgentTypeEnum::getAgentName, agentName);
    }

}