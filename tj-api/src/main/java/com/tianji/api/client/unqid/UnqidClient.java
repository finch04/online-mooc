package com.tianji.api.client.unqid;

import com.tianji.api.client.unqid.fallback.UnqidClientFallback;
import com.tianji.api.dto.leanring.LearningLessonDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(value = "unqid-service", fallbackFactory = UnqidClientFallback.class)
public interface UnqidClient {

    /**
     * 获取有序id
     *
     * @return
     */
    @GetMapping("/unqid/getSeqId")
    Long getSeqId();

    /**
     * 获取无序id
     *
     * @return
     */
    @GetMapping("/unqid/getUnSeqId")
    Long getUnSeqId();

}
