package com.tianji.api.client.unqid.fallback;

import com.tianji.api.client.unqid.UnqidClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.openfeign.FallbackFactory;

@Slf4j
public class UnqidClientFallback implements FallbackFactory<UnqidClient> {

    @Override
    public UnqidClient create(Throwable cause) {
        log.error("ID生成服务调用异常", cause);
        return new UnqidClient() {

            @Override
            public Long getSeqId() {
                return null;
            }

            @Override
            public Long getUnSeqId() {
                return null;
            }
        };
    }
}
