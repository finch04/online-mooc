package com.tianji.aigc.config;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.tianji.aigc.domain.vo.AgentConfigVO;
import com.tianji.aigc.domain.vo.SessionVO;
import com.tianji.aigc.service.AgentConfigService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class AgentConfigInitializer implements CommandLineRunner {

    private final AgentConfigService agentConfigService;

    @Override
    public void run(String... args) throws Exception {
        // 检查Redis中是否已有配置，没有则初始化默认值
        if (agentConfigService.getAgentConfig() == null) {
            AgentConfigVO defaultConfig = AgentConfigVO.builder()
                    .title("Hello，我是智慧MOOC学习助理")
                    .describe("我是由智慧MOOC倾力打造的智能助理，我不仅能推荐课程、答疑解惑，还能为您激发创意、畅聊心事。")
                    .hotQuestions(List.of(
                            new AgentConfigVO.Example("课程推荐", "能帮我推荐一个合适的课吗？"),
                            new AgentConfigVO.Example("课程购买", "我想要报名《天津中德职业规划课》"),
                            new AgentConfigVO.Example("课程介绍", "《3天学完考研数学》适合我学习吗？"),
                            new AgentConfigVO.Example("知识讲解", "ArrayList和LinkedList有何区别？")
                    ))
                    .build();
            agentConfigService.saveAgentConfig(defaultConfig);
        }
    }
}