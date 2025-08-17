package com.tianji.aigc.agent;

import com.tianji.aigc.vo.ChatEventVO;
import com.tianji.common.utils.UserContext;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import reactor.core.publisher.Flux;

@SpringBootTest
class ConsultAgentTest {

    @Resource
    private ConsultAgent consultAgent;

    @Test
    public void processStream() throws InterruptedException {
        String question = "课程多少钱，课程id为：1589905661084430337";
        String sessionId = "123";
        UserContext.setUser(123L);
        Flux<ChatEventVO> flux = consultAgent.processStream(question, sessionId);
        flux.subscribe(System.out::println);

        // 阻塞主线程，防止主线程结束，子线程终止
        Thread.sleep(100000);
    }

}
