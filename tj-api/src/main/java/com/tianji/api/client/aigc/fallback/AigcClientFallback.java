package com.tianji.api.client.aigc.fallback;

import com.tianji.api.client.aigc.AigcClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class AigcClientFallback implements FallbackFactory<AigcClient> {

    @Override
    public AigcClient create(Throwable cause) {
        return new AigcClient() {
            @Override
            public String chatText(String question) {
                return "调用aigc服务出错!";
            }
        };
    }

}
