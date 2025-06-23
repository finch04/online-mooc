import com.tianji.AIGCApplication;
import com.tianji.aigc.agent.RecommendAgent;
import com.tianji.aigc.vo.ChatEventVO;
import com.tianji.common.utils.UserContext;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import reactor.core.publisher.Flux;

@SpringBootTest(classes = AIGCApplication.class)
class RecommendAgentTest {

    @Resource
    private RecommendAgent recommendAgent;

    @Test
    public void processStream() throws InterruptedException {
        String question = "推荐课程，20岁，本科，对产品运营感兴趣,有编程基础";
        String sessionId = "123";
        UserContext.setUser(123L);
        Flux<ChatEventVO> flux = recommendAgent.processStream(question, sessionId);
        flux.subscribe(System.out::println);

        // 阻塞主线程，防止主线程结束，子线程终止
        Thread.sleep(10000);
    }

}