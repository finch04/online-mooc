package com.tianji.aigc.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.tianji.aigc.domain.vo.AgentConfigVO;

public interface AgentConfigService {

    void saveAgentConfig(AgentConfigVO config) throws JsonProcessingException;

    AgentConfigVO getAgentConfig() throws JsonProcessingException;
}
