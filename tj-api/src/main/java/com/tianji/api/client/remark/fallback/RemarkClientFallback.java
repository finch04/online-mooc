package com.tianji.api.client.remark.fallback;

import com.tianji.api.client.learning.LearningClient;
import com.tianji.api.client.remark.RemarkClient;
import com.tianji.api.dto.leanring.LearningLessonDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.openfeign.FallbackFactory;

import java.util.List;
import java.util.Set;

/**
 * remarkClient降级类
 */

@Slf4j
public class RemarkClientFallback implements FallbackFactory<RemarkClient> {
    //如果remark服务没启动，或者其他服务调用remark服务超时则走create降级
    @Override
    public RemarkClient create(Throwable cause) {
        log.error("查询remark服务异常", cause);
        return new RemarkClient() {
            @Override
            public Set<Long> getLikesStatusByBizIds(Iterable<Long> bizIds) {
                return null;
            }
        };
    }
}
