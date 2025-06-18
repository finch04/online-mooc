package com.tianji.chat.service;

import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;
import org.springframework.stereotype.Service;

@Service
public class ToolsService {

    @Tool("对给定的 2 个数字求和")
    double sum(double a, double b) {
        return a + b;
    }

    @Tool("返回给定数字的平方根")
    double squareRoot(double x) {
        return Math.sqrt(x);
    }
}
