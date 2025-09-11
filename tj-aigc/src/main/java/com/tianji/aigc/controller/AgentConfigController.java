package com.tianji.aigc.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.tianji.aigc.domain.vo.AgentConfigVO;
import com.tianji.aigc.service.AgentConfigService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/agent/config")
@RequiredArgsConstructor
public class AgentConfigController {

    private final AgentConfigService agentConfigService;

    @Operation(summary = "获取智能体所有配置（标题、描述、热门问题）")
    @GetMapping
    public AgentConfigVO getAgentConfig() throws JsonProcessingException {
        return agentConfigService.getAgentConfig();
    }

    @Operation(summary = "修改智能体所有配置（标题、描述、热门问题）")
    @PutMapping
    public void updateAgentConfig(@RequestBody AgentConfigVO config) throws JsonProcessingException {
        agentConfigService.saveAgentConfig(config);
    }
}