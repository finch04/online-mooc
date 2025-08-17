package com.tianji.aigc.agent;

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
        String token = "eyJ0eXAiOiJKV1QiLCJhbGciOiJSUzI1NiJ9.eyJ1c2VyIjp7InVzZXJJZCI6Miwicm9sZUlkIjoyLCJyZW1lbWJlck1lIjpmYWxzZX0sImV4cCI6MTc1NTg0Nzk4Mn0.IODscHMZiIzSMB-Zr5fseGCzview0q66PQIBNMZvynLQHMFvQ55NNERUJLTOvDgwNkzc5KxyL_DyzkbSySKICVT7Q_uuWeDNFlJeteaoaJ_fWWAf6xHAhnFi9EnprbXH1fJViHmBVsYmGhJF_LiRgfk0Eh6LaksL6PSm-ACNZ82UsIG66MDij3ubofgEG4ADwZbpuElFZcY0LSuqQrgjkvWbETq8z1vas-0XHURWxWvnPnlBLb-lLeIL84-f2xoCh_sAFs6TM2cOwQdSdtCEIIdmVcida6uoCq8ZEuWgwJbrRPVRkONywwaytP_wI_BPJYelhFTlInOFMoIcGb3BKw";
        Map<String, Object> bizParams = MapUtil.<String, Object>builder()
                .put("user_defined_tokens", MapUtil.of("tool_f14f2d74-1a56-4fc6-93c6-33c52ef4bab7", // 工具id
                        MapUtil.of("user_token", token)))
                .build();

        // bizParams.add("user_defined_tokens", JsonObject);
        ApplicationParam param = ApplicationParam.builder()
                // 若没有配置环境变量，可用百炼API Key将下行替换为：.apiKey("sk-xxx")。但不建议在生产环境中直接将API Key硬编码到代码中，以减少API Key泄露风险。
                .apiKey(System.getenv("ALIYUN_API_KEY"))
                .appId("e3bb1377b29a45979009c89cb07e2041") // 智能体id
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
