package com.tianji.aigc.agent;

import com.tianji.aigc.vo.ChatEventVO;
import com.tianji.common.utils.UserContext;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import reactor.core.publisher.Flux;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class ConsultAgentTest {

    @Resource
    private ConsultAgent consultAgent;

    @Test
    public void test(){
        UserContext.setUser(2L);
        Flux<ChatEventVO> flux = this.consultAgent.processStream("查询课程，id为：1589905661084430337", "123");
        flux.subscribe(System.out::println);
    }

}
