package com.tianji.promotion.dubbo.client;

import com.alibaba.csp.sentinel.annotation.SentinelResource;
import com.tianji.api.interfaces.learning.LearningDubboService;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.stereotype.Service;

@Service
public class LearningClient {

    @DubboReference(version = "1.0.0")
    private LearningDubboService learningDubboService;

    @SentinelResource(value = "QUEUE-DATA-DEGRADE", fallback = "degrade")
    public String sayHello(String name) {
        return learningDubboService.sayHello(name);
    }

    private String degrade(String name) {
        return "降级了：name";
    }

    public void testSeata() {
        learningDubboService.testSeata();
    }
}
