package com.tianji.api.client.aigc;

import com.tianji.api.client.aigc.fallback.AigcClientFallback;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(value = "aigc-service", contextId = "aigc", fallbackFactory = AigcClientFallback.class)
public interface AigcClient {

    @PostMapping("/chat/text")
    String chatText(@RequestBody String question);

}
