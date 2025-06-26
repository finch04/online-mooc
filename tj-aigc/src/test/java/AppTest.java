import cn.hutool.core.map.MapUtil;
import com.alibaba.dashscope.app.Application;
import com.alibaba.dashscope.app.ApplicationParam;
import com.alibaba.dashscope.app.ApplicationResult;
import com.alibaba.dashscope.utils.JsonUtils;
import io.reactivex.Flowable;
import org.junit.jupiter.api.Test;

import java.util.Map;

public class AppTest {

    @Test

    public void testAppCall() throws Exception {
        // 构造业务参数
        String token = "eyJ0eXAiOiJKV1QiLCJhbGciOiJSUzI1NiJ9.eyJ1c2VyIjp7InVzZXJJZCI6Miwicm9sZUlkIjoyLCJyZW1lbWJlck1lIjpmYWxzZX0sImV4cCI6MTc1Mjg0NTU1OH0.De80w_dlxqv18NQ6goi237_xu7iRc5b6Ym5K6HB1kEMgsqWcUF_Rd5Sb8fiyJZo37U8_7g4loHAHyqWJn26fkTQdRfCa5oCXXcF7JJvIBLWIBJbThh3cVUiy1IMA9_W5qzRif1J-QDLYjK7XmErX7FrH41efcmq9wPerrEMYbH6py2BG0B4bhvFzJM8ZBKjFwyTCf_-6RAcOPy8Yta0xDPwADKC0Q44jsjqH9KOcKp4kZ0w-A7skf94mtxEffyYem0bpLbqYrtXg0E7B2tafDRxFwCmc9PxOI12Jnfp6kAimztv0dqqFetQBh7B14eHwKg6dpqRnawk_GcogpA64xg";
        Map<String, Object> bizParams = MapUtil.<String, Object>builder()
                .put("user_defined_tokens", MapUtil.of("tool_36286a80-c742-43f5-a369-be9a094a5e36", // 工具id
                        MapUtil.of("user_token", token)))
                .build();


        // bizParams.add("user_defined_tokens", JsonObject);
        ApplicationParam param = ApplicationParam.builder()
                // 若没有配置环境变量，可用百炼API Key将下行替换为：.apiKey("sk-xxx")。但不建议在生产环境中直接将API Key硬编码到代码中，以减少API Key泄露风险。
                .apiKey("sk-d62152ab4c174bfcb6d7c7c4c813fded")
                .appId("0a319defc69845f183e0c03d4325fc97") // 智能体id
                .prompt("查询课程，id为：1880533253575225346")
                .incrementalOutput(true) // 开启增量输出
                .bizParams(JsonUtils.toJsonObject(bizParams))
                .build();

        Application application = new Application();
        Flowable<ApplicationResult> result = application.streamCall(param);

        // 阻塞式的打印内容
        result.blockingForEach(data -> {
            System.out.printf("%s\n",data.getOutput().getText());
        });

    }

}
