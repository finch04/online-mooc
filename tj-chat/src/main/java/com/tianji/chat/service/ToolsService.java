package com.tianji.chat.service;

import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;
import org.springframework.stereotype.Service;

@Service
public class ToolsService {

    @Tool("某个地区有多少个名字的")
    public Integer getNameCount(@P("地区") String area, @P("名字") String name) {
        System.out.println("area = " + area);
        System.out.println("name = " + name);
        return 100;
    }

    @Tool("你是finch吗")
    public String isLike() {
        System.out.println("isLike");
        return "是";
    }
}
